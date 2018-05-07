#! /bin/sh
# /etc/init.d/InSightsNexusAgent

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
    if [[ $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}') ]]; then
     echo "InSightsNexusAgent already running"
    else
     echo "Starting InSightsNexusAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/nexus
     python -c "from com.cognizant.devops.platformagents.agents.artifactmanagement.nexus.NexusAgent import NexusAgent; NexusAgent()" &
    fi
    if [[ $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}') ]]; then
     echo "InSightsNexusAgent Started Sucessfully"
    else
     echo "InSightsNexusAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsNexusAgent"
    if [[ $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}')
    else
     echo "InSightsNexusAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}') ]]; then
     echo "InSightsNexusAgent Failed to Stop"
    else
     echo "InSightsNexusAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsNexusAgent"
    if [[ $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}') ]]; then
     echo "InSightsNexusAgent stopping"
     sudo kill -9 $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}')
     echo "InSightsNexusAgent stopped"
     echo "InSightsNexusAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/nexus
     python -c "from com.cognizant.devops.platformagents.agents.artifactmanagement.nexus.NexusAgent import NexusAgent; NexusAgent()" &
     echo "InSightsNexusAgent started"
    else
     echo "InSightsNexusAgent already in stopped state"
     echo "InSightsNexusAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/nexus
     python -c "from com.cognizant.devops.platformagents.agents.artifactmanagement.nexus.NexusAgent import NexusAgent; NexusAgent()" &
     echo "InSightsNexusAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsNexusAgent"
    if [[ $(ps aux | grep '[a]rtifactmanagement.nexus.NexusAgent' | awk '{print $2}') ]]; then
     echo "InSightsNexusAgent is running"
    else
     echo "InSightsNexusAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsNexusAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
