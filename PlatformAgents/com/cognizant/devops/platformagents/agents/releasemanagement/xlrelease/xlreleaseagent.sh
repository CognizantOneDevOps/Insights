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
python_version="$(python -V 2>&1)"
detectPythonVersion()
{
     if echo "$1" | grep -q "Python 2"; then
      echo "Detected python 2 version";
      python -c "from  __AGENT_KEY__.com.cognizant.devops.platformagents.agents.releasemanagement.xlrelease.XlReleaseAgent import XlReleaseAgent; XlReleaseAgent()" &
  	 elif echo "$1" | grep -q "Python 3"; then
      echo "Detected python 3 version";
	  python -c "from  __AGENT_KEY__.com.cognizant.devops.platformagents.agents.releasemanagement.xlrelease.XlReleaseAgent3 import XlReleaseAgent; XlReleaseAgent()" &
	 else
      echo "python version not supported"
	  exit 1;
     fi

}

case "$1" in
  start)
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InsightsXlReleaseAgent already running"
    else
     echo "Starting InsightsXlReleaseAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/xlrelease
	 echo $python_version
     detectPythonVersion "$python_version"
    fi
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InsightsXlReleaseAgent Started Sucessfully"
    else
     echo "InsightsXlReleaseAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InsightsXlReleaseAgent"
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '__PS_KEY__' | awk '{print $2}')
    else
     echo "InsightsXlReleaseAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InsightsXlReleaseAgent Failed to Stop"
    else
     echo "InsightsXlReleaseAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InsightsXlReleaseAgent"
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InsightsXlReleaseAgent stopping"
     sudo kill -9 $(ps aux | grep '__PS_KEY__' | awk '{print $2}')
     echo "InsightsXlReleaseAgent stopped"
     echo "InsightsXlReleaseAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/xlrelease
	 echo $python_version
     detectPythonVersion "$python_version"
     echo "InsightsXlReleaseAgent started"
    else
     echo "InsightsXlReleaseAgent already in stopped state"
     echo "InsightsXlReleaseAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/xlrelease
	 echo $python_version
     detectPythonVersion "$python_version"
     echo "InsightsXlReleaseAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InsightsXlReleaseAgent"
    if [[ $(ps aux | grep '__PS_KEY__' | awk '{print $2}') ]]; then
     echo "InsightsXlReleaseAgent is running"
    else
     echo "InsightsXlReleaseAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/__AGENT_KEY__ {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
