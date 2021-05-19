#-------------------------------------------------------------------------------
# Copyright 2020 Cognizant Technology Solutions
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
# /etc/init.d/InsightsPromtail

### BEGIN INIT INFO
# Provides: Runs a Promtail script on startup
# Required-Start: Promtail start
# Required-Stop: Promtail stop
# Short-Description: Simple script to run Promtail program at boot
# Description: Runs a Promtail program at boot
### END INIT INFO

source /etc/environment
source /etc/profile
case "$1" in
  start)
    if [[ $(ps aux | grep '[p]romtail' | awk '{print $2}') ]]; then
     echo "Promtail already running"
    else
     echo "Starting Promtail"
     cd $INSIGHTS_APP_ROOT_DIRECTORY/Promtail
     sudo nohup ./promtail-linux-amd64 -config.file=promtail-local-config.yaml -config.expand-env=true &
     echo $! > promatail-pid.txt
    fi
    if [[ $(ps aux | grep '[p]romtail' | awk '{print $2}') ]]; then
     echo "Promtail Started Successfully"
    else
     echo "Promtail Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping Promtail"
    if [[ $(ps aux | grep '[p]romtail' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[p]romtail' | awk '{print $2}')
    else
     echo "Promtail already in stopped state"
    fi
    if [[ $(ps aux | grep '[p]romtail' | awk '{print $2}') ]]; then
     echo "Promtail Failed to Stop"
    else
     echo "Promtail Stopped"
    fi
    ;;
  status)
    echo "Checking the Status of Promtail"
    if [[ $(ps aux | grep '[p]romtail' | awk '{print $2}') ]]; then
     echo "Promtail is running"
    else
     echo "Promtail is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InsightsPromtail {start|stop|status}"
    exit 1
    ;;
esac
exit 0