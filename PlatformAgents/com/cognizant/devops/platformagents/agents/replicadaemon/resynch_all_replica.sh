#! /bin/sh
#-------------------------------------------------------------------------------
# Copyright 2024 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------

# Define log file
log_file="resync_script.log"


# To log messages
log_message() {
    local message="$1"
    echo "$(date +'%Y-%m-%d %H:%M:%S') = $message" >> $INSIGHTS_HOME/ReplicaDaemon/$log_file
}


# To check if passwordless SSH is enabled 
check_ssh() {
    local ip_address="$1"
    if ! ssh -o PasswordAuthentication=no replica_user@$ip_address exit >/dev/null 2>&1; then
        log_message "Error: Passwordless SSH is not enabled for replica_user on $server_name - $ip_address"
        echo "Passwordless SSH is not enabled for replica_user on $server_name - $ip_address"
        exit 1
    fi
}

# To check the status of kafka on the server
kafka_health_check(){
    nc -z $kafka_endpoint 
    output=$(echo "$?")
    if [ $output -eq 1 ]; then
        log_message "Error: (Prerequisite check failed) Kafka broker is not up & running on $kafka_endpoint"
        echo "Error: (Prerequisite check failed) Kafka broker is not up & running on $kafka_endpoint"
        exit 1
    else
        echo "(Prerequisite check) Kafka broker is up & running on $kafka_endpoint"
    fi
}

# To check the status of neo4j service on master server
master_neo4j_health_check(){
    local ip_address="$1"
    nc -z $ip_address 7474 
    output=$(echo "$?")
    if [ $output -eq 0 ]; then
        log_message "Error: (Prerequisite check failed) $service_name is up & running on $server_name - $ip_address"
        echo "(Prerequisite check failed) $service_name is up & running on $server_name - $ip_address"
        exit 1
    else
         echo "(Prerequisite check) $service_name is not up & running on $server_name - $ip_address"
    fi
}

# To check the status of neo4j service on replica server
health_check(){
    local ip_address="$1"
    nc -z $ip_address 7474 
    output=$(echo "$?")
    if [ $output -eq 1 ]; then
        log_message "Error: (Prerequisite check failed) $service_name is not up & running on $server_name - $ip_address"
        echo "Error: (Prerequisite check failed) $service_name is not up & running on $server_name - $ip_address"
        exit 1
    else
        echo "(Prerequisite check) $service_name is up & running on $server_name - $ip_address"
    fi
}

# To stop the service on a server
stop_service() {
    local ip_address="$1"
    log_message "Info: Stopping $service_name service on $server_name - $ip_address"
    ssh replica_user@$ip_address "sudo service $service_name stop"
    if [ $? -ne 0 ]; then
        log_message "Error: Failed to stop $service_name service on $server_name - $ip_address"
        echo "Failed to stop $service_name service on $server_name - $ip_address"
        exit 1
    fi
}

# To start the service on a server
start_service() {
    local ip_address="$1"
    log_message "Info: Starting $service_name service on $server_name - $ip_address"
    ssh replica_user@$ip_address "sudo service $service_name start"
    if [ $? -ne 0 ]; then
        log_message "Error: Failed to start $service_name service on $server_name - $ip_address"
        echo "Failed to start $service_name service on $server_name - $ip_address"
        exit 1
    fi
}
   
# To delete data folder from Replica Servers
delete_data_folder() {
    local ip_address="$1"
    log_message "Info: Deleting neo4j data folder on replica - $ip_address"
    ssh replica_user@$ip_address "rm -rf /opt/NEO4J_HOME/neo4j-Insights/data"
    if [ $? -ne 0 ]; then
        log_message "Error: Failed to delete neo4j data folder on replica - $ip_address"
        echo "Failed to delete neo4j data folder on replica - $ip_address"
        exit 1
    fi
}

# To copy neo4j data folder from Master Server to Replica Servers
copy_data_folder() {
    local replica_ip="$1"
    log_message "Info: Copying neo4j data folder from master - $master_server to replica - $replica_ip"
    ssh replica_user@$replica_ip "scp -r replica_user@$master_server:/opt/NEO4J_HOME/neo4j-Insights/data/ /opt/NEO4J_HOME/neo4j-Insights/"
    if [ $? -ne 0 ]; then
        log_message "Error: Failed to copy neo4j data folder from master - $master_server to replica - $replica_ip"
        echo "Failed to copy neo4j data folder from master - $master_server to replica - $replica_ip"
        exit 1
    fi
}

# To copy neo4j streams file to Master Servers
copy_streamsFile_to_master() {
    log_message "Info: Copying source neo4j streams file to master - $master_server"
    scp $INSIGHTS_HOME/ReplicaDaemon/SourceStreams.conf replica_user@$master_server:/opt/NEO4J_HOME/neo4j-Insights/conf/streams.conf
    if [ $? -ne 0 ]; then
        log_message "Error: Failed to copy source neo4j streams file to master - $master_server"
        echo "Failed to copy source neo4j streams file to master - $master_server"
        exit 1
    fi
}

# To copy neo4j streams file to Replica Servers
copy_streamsFile_to_replicas() {
    local replica_ip="$1"
    local replica_file="$2"
    log_message "Info: Copying sink neo4j streams file to replica - $replica_ip"
    scp $INSIGHTS_HOME/ReplicaDaemon/$replica_file replica_user@$replica_ip:/opt/NEO4J_HOME/neo4j-Insights/conf/streams.conf
    if [ $? -ne 0 ]; then
        log_message "Error: Failed to copy sink neo4j streams file to replica - $replica_ip"
        echo "Failed to copy sink neo4j streams file to replica - $replica_ip"
        exit 1
    fi
}

#======================================= MAIN METHOD =======================================
# To perform all operations 
perform_operations() {

    # Get command line arguments
    master_server="$1"
    kafka_endpoint="$2"
    read -a replica_servers <<< "$3"
    read -a replica_files <<< "$4"
    
    # Delete existing log file 
    rm -rf $INSIGHTS_HOME/ReplicaDaemon/$log_file

    log_message "Info: Task - Resyncing replicas initiated..."

    # To check the replica arguments
    if [ "${#replica_servers[@]}" != "${#replica_files[@]}" ]; then
        log_message "Error: Count of Replica ip & file path mismatched"
        echo "Count of Replica ip & file path mismatched"
        exit 1
    fi

    # To check master ip & replica ip should not be the same
    for replica_server in "${replica_servers[@]}"; do
        if [[ $replica_server == $master_server ]]; then 
            log_message "Error: Master- $master_server & replica- $replica_ip should not be the same"
            exit 1
        fi
    done

    # Check passwordless SSH is enabled for all servers  =========== [Task 1] ===========
    server_name="master"
    check_ssh "$master_server"
    
    for replica_server in "${replica_servers[@]}"; do
        server_name="replica"
        check_ssh "$replica_server"
    done


    # Prerequisites - Health Checks =========== [Task 2] ===========
    kafka_health_check "$kafka_endpoint"

    service_name="Neo4j"
    server_name="master"
    master_neo4j_health_check "$master_server"

    for replica_server in "${replica_servers[@]}"; do
        server_name="replica"
        health_check "$replica_server" 
    done 


    # Stop the NEO4J service on replica servers =========== [Task 5] ===========
    for replica_server in "${replica_servers[@]}"; do
	    service_name="Neo4j"
        server_name="replica"
        stop_service "$replica_server" 
    done 


    # Copy neo4j source stream file to Master Server =========== [Task 6] ===========
    copy_streamsFile_to_master 
    

    # Copy neo4j sink stream file to Replica Servers =========== [Task 7] ===========
    for (( i=0; i<${#replica_servers[@]}; i++ )); do
        copy_streamsFile_to_replicas "${replica_servers[$i]}" "${replica_files[$i]}" 
    done


    # Delete neo4j data folder on Replica servers =========== [Task 8] ===========
    for replica_server in "${replica_servers[@]}"; do
        delete_data_folder "$replica_server"
    done


    # Copy neo4j data folder from Master to Replica Servers =========== [Task 9] ===========
    for replica_server in "${replica_servers[@]}"; do
        copy_data_folder "$replica_server"
    done


    # Start the Neo4j service on replica servers =========== [Task 10] ===========   
    for replica_server in "${replica_servers[@]}"; do
	    service_name="Neo4j"
        server_name="replica"
        start_service "$replica_server" 
    done

    log_message "Info: Replicas resynced successfully"

}

# Call the function with command line arguments and redirect stdout to a variable
error_message=$(perform_operations "$@" 2>&1)

# Check if an error occurred
if [ $? -ne 0 ]; then
    echo "$error_message"
fi
