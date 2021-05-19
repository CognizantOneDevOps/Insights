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
# /etc/init.d/InsightsLoki

### BEGIN INIT INFO
# Provides: Runs a Loki script on startup
# Required-Start: Loki start
# Required-Stop: Loki stop
# Short-Description: Simple script to run Loki program at boot
# Description: Runs a Loki program at boot
### END INIT INFO

source /etc/environment
source /etc/profile
case "$1" in
  start)
    if [[ $(ps aux | grep '[l]oki' | awk '{print $2}') ]]; then
     echo "Loki already running"
    else
     echo "Starting Loki"
     cd $INSIGHTS_APP_ROOT_DIRECTORY
	 cd Loki
     sudo nohup ./loki-linux-amd64 -config.file=loki-local-config.yaml &
     echo $! > loki-pid.txt
    fi
    if [[ $(ps aux | grep '[l]oki' | awk '{print $2}') ]]; then
     echo "Loki Started Successfully"
    else
     echo "Loki Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping Loki"
    if [[ $(ps aux | grep '[l]oki' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[l]oki' | awk '{print $2}')
    else
     echo "Loki already in stopped state"
    fi
    if [[ $(ps aux | grep '[l]oki' | awk '{print $2}') ]]; then
     echo "Loki Failed to Stop"
    else
     echo "Loki Stopped"
    fi
    ;;
  status)
    echo "Checking the Status of Loki"
    if [[ $(ps aux | grep '[l]oki' | awk '{print $2}') ]]; then
     echo "Loki is running"
    else
     echo "Loki is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InsightsLoki {start|stop|status}"
    exit 1
    ;;
esac
exit 0