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
# /etc/init.d/UI

### BEGIN INIT INFO
# Provides: Runs a UI script on startup
# Required-Start: UI start
# Required-Stop: UI stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run UI program at boot
# Description: Runs a UI program at boot
### END INIT INFO
source /etc/environment
source /etc/profile
case "$1" in
  start)
    if [[ $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}') ]]; then
     echo "UI already running"
    else
     echo "Starting UI"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/UI
     sudo node UI.js &  >UIlog.txt 2>UIerrorlog.txt
     echo $! > UI-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}') ]]; then
     echo "UI Started Successfully"
    else
     echo "UI Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping UI"
    if [[ $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}')
     sleep 10
    else
     echo "UI already in stopped state"
    fi
    if [[ $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}') ]]; then
     echo "UI Failed to Stop"
    else
     echo "UI Stopped"
    fi
    ;;
  restart)
    echo "Restarting UI"
    if [[ $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}') ]]; then
     echo "UI stopping"
     sudo kill -9 $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}')
     sleep 10
     echo "UI stopped"
     echo "UI starting"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/UI
     sudo node UI.js &  >UIlog.txt 2>UIerrorlog.txt
     echo $! > UI-pid.txt
	 sleep 10
     echo "UI started"
    else
     echo "UI already in stopped state"
     echo "UI starting"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/UI
     sudo node UI.js &  >UIlog.txt 2>UIerrorlog.txt
     echo $! > UI-pid.txt
	 sleep 10
     echo "UI started"
    fi
    ;;
  status)
    echo "Checking the Status of UI"
    if [[ $(ps aux | grep "UI.js" | grep -v grep | awk '{print $2}') ]]; then
     echo "UI is running"
    else
     echo "UI is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/UI {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
