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
    if [[ $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodePipelineAgent already running"
    else
     echo "Starting InSightsAwsCodePipelineAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodepipeline
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodepipeline.AwsCodePipelineAgent import AwsCodePipelineAgent; AwsCodePipelineAgent()" &
    fi
    if [[ $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodePipelineAgent Started Sucessfully"
    else
     echo "InSightsAwsCodePipelineAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsAwsCodePipelineAgent"
    if [[ $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}')
    else
     echo "InSIghtsUCDAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodePipelineAgent Failed to Stop"
    else
     echo "InSightsAwsCodePipelineAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsAwsCodePipelineAgent"
    if [[ $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodePipelineAgent stopping"
     sudo kill -9 $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}')
     echo "InSightsAwsCodePipelineAgent stopped"
     echo "InSightsAwsCodePipelineAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodepipeline
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodepipeline.AwsCodePipelineAgent import AwsCodePipelineAgent; AwsCodePipelineAgent()" &
     echo "InSightsAwsCodePipelineAgent started"
    else
     echo "InSightsAwsCodePipelineAgent already in stopped state"
     echo "InSightsAwsCodePipelineAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodepipeline
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodepipeline.AwsCodePipelineAgent import AwsCodePipelineAgent; AwsCodePipelineAgent()" &
     echo "InSightsAwsCodePipelineAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsAwsCodePipelineAgent"
    if [[ $(ps aux | grep '[d]eployment.awscodepipeline.AwsCodePipelineAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodePipelineAgent is running"
    else
     echo "InSightsAwsCodePipelineAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsAwsCodePipelineAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0