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
# /etc/init.d/InSightsUCDAgent

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
    if [[ $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeCommitAgent already running"
    else
     echo "Starting InSightsAwsCodeCommitAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodecommit
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodecommit.AwsCodeCommitAgent import AwsCodeCommitAgent; AwsCodeCommitAgent()" &
    fi
    if [[ $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeCommitAgent Started Sucessfully"
    else
     echo "InSightsAwsCodeCommitAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsAwsCodeCommitAgent"
    if [[ $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}')
    else
     echo "InSIghtsUCDAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeCommitAgent Failed to Stop"
    else
     echo "InSightsAwsCodeCommitAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsAwsCodeCommitAgent"
    if [[ $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeCommitAgent stopping"
     sudo kill -9 $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}')
     echo "InSightsAwsCodeCommitAgent stopped"
     echo "InSightsAwsCodeCommitAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodecommit
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodecommit.AwsCodeCommitAgent import AwsCodeCommitAgent; AwsCodeCommitAgent()" &
     echo "InSightsAwsCodeCommitAgent started"
    else
     echo "InSightsAwsCodeCommitAgent already in stopped state"
     echo "InSightsAwsCodeCommitAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodecommit
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodecommit.AwsCodeCommitAgent import AwsCodeCommitAgent; AwsCodeCommitAgent()" &
     echo "InSightsAwsCodeCommitAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsAwsCodeCommitAgent"
    if [[ $(ps aux | grep '[s]cm.awscodecommit.AwsCodeCommitAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeCommitAgent is running"
    else
     echo "InSightsAwsCodeCommitAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsAwsCodeCommitAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0