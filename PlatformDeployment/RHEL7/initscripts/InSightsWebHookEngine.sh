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
# /etc/init.d/InSightsWebHookEngine

### BEGIN INIT INFO
# Provides: Runs a InSightsWebHookEngine script on startup
# Required-Start: InSightsWebHookEngine start
# Required-Stop: InSightsWebHookEngine stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run InSightsWebHookEngine program at boot
# Description: Runs a InSightsWebHookEngine program at boot
### END INIT INFO
[[ -z "${INSIGHTS_WEBHOOK}" ]] && INSIGHTS_WEBHOOK=env | grep INSIGHTS_WEBHOOK | cut -d'=' -f2 || INSIGHTS_WEBHOOK="${INSIGHTS_WEBHOOK}"
echo $INSIGHTS_WEBHOOK
case "$1" in
  start)
    if [[ $(ps aux | grep ' [P]latformWebhookEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHookEngine already running"
    else
     echo "Starting InSightsWebHookEngine"
     cd $INSIGHTS_WEBHOOK
     sudo nohup java -jar PlatformWebhookEngine.jar &
     echo $! > InSightsWebHookEngine-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHookEngine Started Successfully"
    else
     echo "InSightsWebHookEngine Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsWebHookEngine"
    if [[ $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}')
     sleep 10
    else
     echo "InSightsWebHookEngine already in stopped state"
    fi
    if [[ $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHookEngine Failed to Stop"
    else
     echo "InSightsWebHookEngine Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsWebHookEngine"
    if [[ $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHookEngine stopping"
     sudo kill -9 $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}')
     sleep 10
     echo "InSightsWebHookEngine stopped"
     echo "InSightsWebHookEngine starting"
     cd $INSIGHTS_WEBHOOK
     sudo nohup java -jar PlatformWebhookEngine.jar &
     echo $! > InSightsWebHookEngine-pid.txt
         sleep 10
     echo "InSightsWebHookEngine started"
    else
     echo "InSightsWebHookEngine already in stopped state"
     echo "InSightsWebHookEngine starting"
     cd $INSIGHTS_WEBHOOK
     sudo nohup java -jar PlatformWebhookEngine.jar &
     echo $! > InSightsWebHookEngine-pid.txt
         sleep 10
     echo "InSightsWebHookEngine started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsWebHookEngine"
    if [[ $(ps aux | grep '[P]latformWebhookEngine.jar' | awk '{print $2}') ]]; then
     echo "InSightsWebHookEngine is running"
    else
     echo "InSightsWebHookEngine is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsWebHookEngine {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
