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
# /etc/init.d/__AGENT_KEY__

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
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent already running"
    else
     echo "Starting InSightsHpAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/hp
     python -c "from __AGENT_KEY__.com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()" &
    fi
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent Started Sucessfully"
    else
     echo "InSightsHpAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsHpAgent"
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '__PS_KEY__' | awk '{print $2}')
    else
     echo "InSightsHpAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent Failed to Stop"
    else
     echo "InSightsHpAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsHpAgent"
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent stopping"
     sudo kill -9 $(ps aux | grep '__PS_KEY__' | awk '{print $2}')
     echo "InSightsHpAgent stopped"
     echo "InSightsHpAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/hp
     python -c "from __AGENT_KEY__.com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()" &
     echo "InSightsHpAgent started"
    else
     echo "InSightsHpAgent already in stopped state"
     echo "InSightsHpAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/hp
     python -c "from __AGENT_KEY__.com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()" &
     echo "InSightsHpAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsHpAgent"
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent is running"
    else
     echo "InSightsHpAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/__AGENT_KEY__ {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
