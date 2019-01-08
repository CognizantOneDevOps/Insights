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
# /etc/init.d/InSightsAwsCodeDeployAgent

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
    if [[ $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeDeployAgent already running"
    else
     echo "Starting InSightsAwsCodeDeployAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodedeploy
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodedeploy.AwsCodeDeployAgent import AwsCodeDeployAgent; AwsCodeDeployAgent()" &
    fi
    if [[ $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeDeployAgent Started Sucessfully"
    else
     echo "InSightsAwsCodeDeployAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsAwsCodeDeployAgent"
    if [[ $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}')
    else
     echo "InSIghtsUCDAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeDeployAgent Failed to Stop"
    else
     echo "InSightsAwsCodeDeployAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsAwsCodeDeployAgent"
    if [[ $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeDeployAgent stopping"
     sudo kill -9 $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}')
     echo "InSightsAwsCodeDeployAgent stopped"
     echo "InSightsAwsCodeDeployAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodedeploy
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodedeploy.AwsCodeDeployAgent import AwsCodeDeployAgent; AwsCodeDeployAgent()" &
     echo "InSightsAwsCodeDeployAgent started"
    else
     echo "InSightsAwsCodeDeployAgent already in stopped state"
     echo "InSightsAwsCodeDeployAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/awscodedeploy
     python -c "from com.cognizant.devops.platformagents.agents.deployment.awscodedeploy.AwsCodeDeployAgent import AwsCodeDeployAgent; AwsCodeDeployAgent()" &
     echo "InSightsAwsCodeDeployAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsAwsCodeDeployAgent"
    if [[ $(ps aux | grep '[d]eployment.awscodedeploy.AwsCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsAwsCodeDeployAgent is running"
    else
     echo "InSightsAwsCodeDeployAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsAwsCodeDeployAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0