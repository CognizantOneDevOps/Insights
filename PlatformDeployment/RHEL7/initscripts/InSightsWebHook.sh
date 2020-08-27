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
# /etc/init.d/InSightsWebHook

### BEGIN INIT INFO
# Provides: Runs a InSightsWebHook script on startup
# Required-Start: InSightsWebHook start
# Required-Stop: InSightsWebHook stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run InSightsWebHook program at boot
# Description: Runs a InSightsWebHook program at boot
### END INIT INFO
[[ -z "${INSIGHTS_WEBHOOK}" ]] && INSIGHTS_WEBHOOK=env | grep INSIGHTS_WEBHOOK | cut -d'=' -f2 || INSIGHTS_WEBHOOK="${INSIGHTS_WEBHOOK}"
echo $INSIGHTS_WEBHOOK
case "$1" in
  start)
    if [[ $(ps aux | grep ' [P]latformInsightsWebHook.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHook already running"
    else
     echo "Starting InSightsWebHook"
     cd $INSIGHTS_WEBHOOK
	 sudo nohup java -jar PlatformInsightsWebHook.jar -config.file.location=$INSIGHTS_WEBHOOK &
     echo $! > InSightsWebHook-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHook Started Successfully"
    else
     echo "InSightsWebHook Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsWebHook"
    if [[ $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}')
     sleep 10
    else
     echo "InSightsWebHook already in stopped state"
    fi
    if [[ $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHook Failed to Stop"
    else
     echo "InSightsWebHook Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsWebHook"
    if [[ $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHook stopping"
     sudo kill -9 $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}')
     sleep 10
     echo "InSightsWebHook stopped"
     echo "InSightsWebHook starting"
     cd $INSIGHTS_WEBHOOK
	 sudo nohup java -jar PlatformInsightsWebHook.jar -config.file.location=$INSIGHTS_WEBHOOK &
     echo $! > InSightsWebHook-pid.txt
	 sleep 10
     echo "InSightsWebHook started"
    else
     echo "InSightsWebHook already in stopped state"
     echo "InSightsWebHook starting"
     cd $INSIGHTS_WEBHOOK
	 sudo nohup java -jar PlatformInsightsWebHook.jar -config.file.location=$INSIGHTS_WEBHOOK &
     echo $! > InSightsWebHook-pid.txt
	 sleep 10
     echo "InSightsWebHook started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsWebHook"
    if [[ $(ps aux | grep '[P]latformInsightsWebHook.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHook is running"
    else
     echo "InSightsWebHook is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsWebHook {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
