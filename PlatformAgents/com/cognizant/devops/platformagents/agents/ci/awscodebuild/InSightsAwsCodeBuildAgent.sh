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
    if [[ $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeBuildAgent already running"
    else
     echo "Starting InSightsAwsCodeBuildAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodebuild
     python -c "from com.cognizant.devops.platformagents.agents.ci.awscodebuild.AwsCodeBuildAgent import AwsCodeBuildAgent; AwsCodeBuildAgent()" &
    fi
    if [[ $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeBuildAgent Started Sucessfully"
    else
     echo "InSightsAwsCodeBuildAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsAwsCodeBuildAgent"
    if [[ $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}')
    else
     echo "InSIghtsUCDAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeBuildAgent Failed to Stop"
    else
     echo "InSightsAwsCodeBuildAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsAwsCodeBuildAgent"
    if [[ $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeBuildAgent stopping"
     sudo kill -9 $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}')
     echo "InSightsAwsCodeBuildAgent stopped"
     echo "InSightsAwsCodeBuildAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodebuild
     python -c "from com.cognizant.devops.platformagents.agents.ci.awscodebuild.AwsCodeBuildAgent import AwsCodeBuildAgent; AwsCodeBuildAgent()" &
     echo "InSightsAwsCodeBuildAgent started"
    else
     echo "InSightsAwsCodeBuildAgent already in stopped state"
     echo "InSightsAwsCodeBuildAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodebuild
     python -c "from com.cognizant.devops.platformagents.agents.ci.awscodebuild.AwsCodeBuildAgent import AwsCodeBuildAgent; AwsCodeBuildAgent()" &
     echo "InSightsAwsCodeBuildAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsAwsCodeBuildAgent"
    if [[ $(ps aux | grep '[c]i.awscodebuild.AwsCodeBuildAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeBuildAgent is running"
    else
     echo "InSightsAwsCodeBuildAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsAwsCodeBuildAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0