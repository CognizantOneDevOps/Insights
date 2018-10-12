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
#! /bin/sh
# /etc/init.d/InSightsGitAgent

### BEGIN INIT INFO
# Provides: Runs a Python script on startup
# Required-Start: BootPython start
# Required-Stop: BootPython stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run python program at boot
# Description: Runs a python program at boot
### END INIT INFO
#export INSIGHTS_AGENT_HOME=/home/ec2-user/insightsagents
source /etc/profile

case "$1" in
  start)
    if [[ $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}') ]]; then
     echo "InSightsVersionOneAgent already running"
    else
     echo "Starting InSightsVersionOneAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/versionone
     python -c "from com.cognizant.devops.platformagents.agents.alm.versionone.VersionOneAgent import VersionOneAgent; VersionOneAgent()" &
    fi
    if [[ $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}') ]]; then
     echo "InSightsGitAgent Started Sucessfully"
    else
     echo "InSightsVersionOneAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsVersionOneAgent"
    if [[ $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}')
    else
     echo "InSIghtsGitAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}') ]]; then
     echo "InSightsVersionOneAgent Failed to Stop"
    else
     echo "InSightsVersionOneAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsVersionOneAgent"
    if [[ $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}') ]]; then
     echo "InSightsVersionOneAgent stopping"
     sudo kill -9 $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}')
     echo "InSightsVersionOneAgent stopped"
     echo "InSightsVersionOneAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/versionone
     python -c "from com.cognizant.devops.platformagents.agents.alm.versionone.VersionOneAgent import VersionOneAgent; VersionOneAgent()" &
     echo "InSightsVersionOneAgent started"
    else
     echo "InSightsVersionOneAgent already in stopped state"
     echo "InSightsVersionOneAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/versionone
     python -c "from com.cognizant.devops.platformagents.agents.alm.versionone.VersionOneAgent import VersionOneAgent; VersionOneAgent()" &
     echo "InSightsVersionOneAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsVersionOneAgent"
    if [[ $(ps aux | grep '[a]lm.versionone.VersionOneAgent' | awk '{print $2}') ]]; then
     echo "InSightsVersionOneAgent is running"
    else
     echo "InSightsVersionOneAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsVersionOneAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
