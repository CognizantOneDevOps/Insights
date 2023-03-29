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
# /etc/init.d/PlatformService

### BEGIN INIT INFO
# Provides: Runs a PlatformService script on startup
# Required-Start: PlatformService start
# Required-Stop: PlatformService stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run PlatformService program at boot
# Description: Runs a PlatformService program at boot
### END INIT INFO
source /etc/environment
source /etc/profile
case "$1" in
  start)
    if [[ $(ps aux | grep ' [P]latformService.jar' | awk '{print $2}') ]]; then
     echo "PlatformService already running"
    else
     echo "Starting PlatformService"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService
     sudo nohup java -jar PlatformService.jar > /dev/null 2>&1 &
     echo $! > PlatformService-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[P]latformService.jar' | awk '{print $2}') ]]; then
     echo "PlatformService Started Successfully"
    else
     echo "PlatformService Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping PlatformService"
    if [[ $(ps aux | grep '[P]latformService.jar' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[P]latformService.jar' | awk '{print $2}')
     sleep 10
    else
     echo "PlatformService already in stopped state"
    fi
    if [[ $(ps aux | grep '[P]latformService.jar' | awk '{print $2}') ]]; then
     echo "PlatformService Failed to Stop"
    else
     echo "PlatformService Stopped"
    fi
    ;;
  restart)
    echo "Restarting PlatformService"
    if [[ $(ps aux | grep ' P]latformService.jar' | awk '{print $2}') ]]; then
     echo "PlatformService stopping"
     sudo kill -9 $(ps aux | grep '[P]latformService.jar' | awk '{print $2}')
     sleep 10
     echo "PlatformService stopped"
     echo "PlatformService starting"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService
     sudo nohup java -jar PlatformService.jar > /dev/null 2>&1 &
     echo $! > PlatformService-pid.txt
	 sleep 10
     echo "PlatformService started"
    else
     echo "PlatformService already in stopped state"
     echo "PlatformService starting"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService
     sudo nohup java -jar PlatformService.jar > /dev/null 2>&1 &
     echo $! > PlatformService-pid.txt
	 sleep 10
     echo "PlatformService started"
    fi
    ;;
  status)
    echo "Checking the Status of PlatformService"
    if [[ $(ps aux | grep '[P]latformService.jar' | awk '{print $2}') ]]; then
     echo "PlatformService is running"
    else
     echo "PlatformService is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/PlatformService {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
