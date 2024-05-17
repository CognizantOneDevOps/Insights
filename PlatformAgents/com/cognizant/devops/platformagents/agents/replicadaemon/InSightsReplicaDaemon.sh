#-------------------------------------------------------------------------------
# Copyright 2024 Cognizant Technology Solutions
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
# /etc/init.d/InSightsReplicaDaemon

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
    if [[ $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}') ]]; then
     echo "InSightsReplicaDaemon already running"
    else
     echo "Starting InSightsReplicaDaemon"
     cd $INSIGHTS_AGENT_HOME/ReplicaDaemon
     python -c "from com.cognizant.devops.platformagents.agents.replicadaemon.ReplicaDaemonExecutor import ReplicaDaemonExecutor; ReplicaDaemonExecutor()" &
    fi
    if [[ $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}') ]]; then
     echo "InSightsReplicaDaemon Started Sucessfully"
    else
     echo "InSightsReplicaDaemon Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsReplicaDaemon"
    if [[ $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}')
    else
     echo "InSightsReplicaDaemon already in stopped state"
    fi
    if [[ $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}') ]]; then
     echo "InSightsReplicaDaemon Failed to Stop"
    else
     echo "InSightsReplicaDaemon Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsReplicaDaemon"
    if [[ $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}') ]]; then
     echo "InSightsReplicaDaemon stopping"
     sudo kill -9 $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}')
     echo "InSightsReplicaDaemon stopped"
     echo "InSightsReplicaDaemon starting"
     cd $INSIGHTS_AGENT_HOME/ReplicaDaemon
     python -c "from com.cognizant.devops.platformagents.agents.replicadaemon.ReplicaDaemonExecutor import ReplicaDaemonExecutor; ReplicaDaemonExecutor()" &
     echo "InSightsReplicaDaemon started"
    else
     echo "InSightsReplicaDaemon already in stopped state"
     echo "InSightsReplicaDaemon starting"
     cd $INSIGHTS_AGENT_HOME/ReplicaDaemon
     python -c "from com.cognizant.devops.platformagents.agents.replicadaemon.ReplicaDaemonExecutor import ReplicaDaemonExecutor; ReplicaDaemonExecutor()" &
     echo "InSightsReplicaDaemon started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsReplicaDaemon"
    if [[ $(ps aux | grep '[r]eplicadaemon.ReplicaDaemonExecutor' | awk '{print $2}') ]]; then
     echo "InSightsReplicaDaemon is running"
    else
     echo "InSightsReplicaDaemon is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsReplicaDaemon {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
