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
log_file="delete_script.log"

# To log messages
log_message() {
    local message="$1"
    echo "$(date +'%Y-%m-%d %H:%M:%S') = $message" >> $INSIGHTS_HOME/ReplicaDaemon/$log_file
}

# To check if passwordless SSH is enabled
check_ssh() {
    local replica_ip="$1"
    if ! ssh -o PasswordAuthentication=no replica_user@$replica_ip exit >/dev/null 2>&1; then
        log_message "Error: Passwordless SSH is not enabled for replica_user on replica- $replica_ip"
        echo "Passwordless SSH is not enabled for for replica_user on replica- $replica_ip"
        exit 1
    fi
}

# To stop & drop replica server
    drop_replica() {
        local replica_ip="$1"
        log_message "Info: Stopping $service_name service on replica - $replica_ip"
        ssh replica_user@$replica_ip "sudo service $service_name stop"
		log_message "Info: Deleting neo4j streams.conf on replica - $replica_ip"
        ssh replica_user@$replica_ip "rm -rf /opt/NEO4J_HOME/neo4j-Insights/conf/streams.conf"
        log_message "Info: Deleting neo4j data folder on replica - $replica_ip"
        ssh replica_user@$replica_ip "rm -rf /opt/NEO4J_HOME/neo4j-Insights/data"
        if [ $? -ne 0 ]; then
            log_message "Error: Failed to stop service on replica - $replica_ip"
            echo "Failed to stop service on replica - $replica_ip"
            exit 1
        fi
    }

# To perform all operations
perform_operations() {

    # Get command line arguments
    replica_server="$1"

    # Delete existing log file
    rm -rf $INSIGHTS_HOME/ReplicaDaemon/$log_file

    log_message "Info: Task - Removing replica initiated..."

    check_ssh "$replica_server"
    
    service_name="Neo4j"
    drop_replica "$replica_server"
    
    log_message "Info: Replica - $replica_server removed successfully"

}
		   
# Call the function with command line arguments and redirect stdout to a variable
error_message=$(perform_operations "$@" 2>&1)

# Check if an error occurred
if [ $? -ne 0 ]; then
    echo "$error_message"
fi