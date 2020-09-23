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
#
#!/bin/sh
# /etc/init.d/InSightsWorkflow
#
### BEGIN INIT INFO
# Provides: Runs a InSightsWorkflow script on startup
# Required-Start: InSightsWorkflow start
# Required-Stop: InSightsWorkflow stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run InSightsWorkflow program at boot
# Description: Runs a InSightsWorkflow program at boot
### END INIT INFO
[[ -z "${INSIGHTS_WORKFLOW}" ]] && INSIGHTS_WORKFLOW=env | grep INSIGHTS_WORKFLOW | cut -d'=' -f2 || INSIGHTS_WORKFLOW="${INSIGHTS_WORKFLOW}"
echo $INSIGHTS_WORKFLOW
INSIGHTS_WORKFLOW=/opt/insightsworkflow
INSIGHTS_HOME=/usr/INSIGHTS_HOME
case "$1" in
  start)
    if [[ $(ps aux | grep ' [P]latformWorkflow.jar' | awk '{print $2}') ]]; then
     echo "InSightsWorkflow already running"
    else
     echo "Starting InSightsWorkflow"
     cd $INSIGHTS_WORKFLOW
	 sudo nohup java -cp PlatformWorkflow.jar:$INSIGHTS_HOME/workflowjar/* com.cognizant.devops.platformworkflow.workflowtask.app.PlatformWorkflowApplication &
     echo $! > InSightsWorkflow-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}') ]]; then
     echo "InSightsWorkflow Started Successfully"
    else
     echo "InSightsWorkflow Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsWorkflow"
    if [[ $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}')
     sleep 10
    else
     echo "InSightsWorkflow already in stopped state"
    fi
    if [[ $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}') ]]; then
     echo "InSightsWorkflow Failed to Stop"
    else
     echo "InSightsWorkflow Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsWorkflow"
    if [[ $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}') ]]; then
     echo "InSightsWorkflow stopping"
     sudo kill -9 $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}')
     sleep 10
     echo "InSightsWorkflow stopped"
     echo "InSightsWorkflow starting"
     cd $INSIGHTS_WORKFLOW
	 sudo nohup java -cp PlatformWorkflow.jar:$INSIGHTS_HOME/workflowjar/* com.cognizant.devops.platformworkflow.workflowtask.app.PlatformWorkflowApplication &
     echo $! > InSightsWorkflow-pid.txt
	 sleep 10
     echo "InSightsWorkflow started"
    else
     echo "InSightsWorkflow already in stopped state"
     echo "InSightsWorkflow starting"
     cd $INSIGHTS_WORKFLOW
	 sudo nohup java -cp PlatformWorkflow.jar:$INSIGHTS_HOME/workflowjar/* com.cognizant.devops.platformworkflow.workflowtask.app.PlatformWorkflowApplication &
     echo $! > InSightsWorkflow-pid.txt
	 sleep 10
     echo "InSightsWorkflow started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsWorkflow"
    if [[ $(ps aux | grep '[P]latformWorkflow.jar' | awk '{print $2}') ]]; then
     echo "InSightsWorkflow is running"
    else
     echo "InSightsWorkflow is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsWorkflow {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
