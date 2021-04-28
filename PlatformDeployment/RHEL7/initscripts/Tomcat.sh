#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
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

#!/bin/sh
# /etc/init.d/Tomcat

### BEGIN INIT INFO
# Provides: Runs a Tomcat script on startup
# Required-Start: Tomcat start
# Required-Stop: Tomcat stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run Tomcat program at boot
# Description: Runs a Tomcat program at boot
### END INIT INFO
source /etc/environment
source /etc/profile
export TOMCAT_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat
SHUTDOWN_WAIT_TIME=50

tomcat_pid() {
    echo `ps aux | grep org.apache.catalina.startup.Bootstrap | grep -v grep | awk '{ print $2 }'`
}

start() {
    pid=$(tomcat_pid)
    if [ -n "$pid" ]
    then
        echo "Tomcat is already running (pid: $pid)"
    else
        # Start tomcat
        cd $TOMCAT_HOME/bin
		sudo sh startup.sh        
    fi
    return 0
}

stop(){
 pid=$(tomcat_pid)
    if [ -n "$pid" ]
    then
        echo "Stopping Tomcat"
        cd $TOMCAT_HOME/bin
		sudo sh shutdown.sh

    let kwait=$SHUTDOWN_WAIT_TIME
    count=0
    count_by=5
    until [ `ps -p $pid | grep -c $pid` = '0' ] || [ $count -gt $kwait ]
    do
        echo "Waiting for processes to exit. Timeout before we kill the pid: ${count}/${kwait}"
        sleep $count_by
        let count=$count+$count_by;
    done

    if [ $count -gt $kwait ]; then
        echo "Killing processes which didn't stop after $SHUTDOWN_WAIT_TIME seconds"
        kill -9 $pid
    fi
    else
        echo "Tomcat is not running"
    fi

    return 0
}

case "$1" in
	start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        stop
        start
        ;;
	status)
    echo "Checking the Status of Tomcat"
     pid=$(tomcat_pid)
        if [ -n "$pid" ]
        then
           echo "Tomcat is running with pid: $pid"
        else
           echo "Tomcat is not running"
        fi
        ;;
esac
exit 0