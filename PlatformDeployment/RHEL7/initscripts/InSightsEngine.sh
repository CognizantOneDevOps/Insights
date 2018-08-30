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
# /etc/init.d/InSightsEngine

### BEGIN INIT INFO
# Provides: Runs a InSightsEngine script on startup
# Required-Start: InSightsEngine start
# Required-Stop: InSightsEngine stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run InSightsEngine program at boot
# Description: Runs a InSightsEngine program at boot
### END INIT INFO
[[ -z "${INSIGHTS_ENGINE}" ]] && INSIGHTS_ENGINE=sudo env | grep INSIGHTS_ENGINE | cut -d'=' -f2 || INSIGHTS_ENGINE="${INSIGHTS_ENGINE}"
echo $INSIGHTS_ENGINE
case "$1" in
  start)
    if [[ $(ps aux | grep ' [P]latformEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsEngine already running"
    else
     echo "Starting InSightsEngine"
     cd $INSIGHTS_ENGINE
     sudo nohup java -jar PlatformEngine.jar &
     echo $! > InSightsEngine-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[P]latformEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsEngine Started Successfully"
    else
     echo "InSightsEngine Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsEngine"
    if [[ $(ps aux | grep '[P]latformEngine.jar' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[P]latformEngine.jar' | awk '{print $2}')
     sleep 10
    else
     echo "InSightsEngine already in stopped state"
    fi
    if [[ $(ps aux | grep '[P]latformEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsEngine Failed to Stop"
    else
     echo "InSightsEngine Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsEngine"
    if [[ $(ps aux | grep ' P]latformEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsEngine stopping"
     sudo kill -9 $(ps aux | grep '[P]latformEngine.jar' | awk '{print $2}')
     sleep 10
     echo "InSightsEngine stopped"
     echo "InSightsEngine starting"
     cd $INSIGHTS_ENGINE
     sudo nohup java -jar PlatformEngine.jar &
     echo $! > InSightsEngine-pid.txt
	 sleep 10
     echo "InSightsEngine started"
    else
     echo "InSightsEngine already in stopped state"
     echo "InSightsEngine starting"
     cd $INSIGHTS_ENGINE
     sudo nohup java -jar PlatformEngine.jar &
     echo $! > InSightsEngine-pid.txt
	 sleep 10
     echo "InSightsEngine started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsEngine"
    if [[ $(ps aux | grep '[P]latformEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsEngine is running"
    else
     echo "InSightsEngine is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsEngine {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
