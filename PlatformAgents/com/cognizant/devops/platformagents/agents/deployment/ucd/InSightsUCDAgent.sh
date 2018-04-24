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
    if [[ $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsUCDAgent already running"
    else
     echo "Starting InSightsUCDAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/ucd
     python -c "from com.cognizant.devops.platformagents.agents.deployment.ucd.UrbanCodeDeployAgent import UrbanCodeDeployAgent; UrbanCodeDeployAgent()" &
    fi
    if [[ $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsUCDAgent Started Sucessfully"
    else
     echo "InSightsUCDAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsUCDAgent"
    if [[ $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}')
    else
     echo "InSIghtsUCDAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsUCDAgent Failed to Stop"
    else
     echo "InSightsUCDAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsUCDAgent"
    if [[ $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsUCDAgent stopping"
     sudo kill -9 $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}')
     echo "InSightsUCDAgent stopped"
     echo "InSightsUCDAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/ucd
     python -c "from com.cognizant.devops.platformagents.agents.deployment.ucd.UrbanCodeDeployAgent import UrbanCodeDeployAgent; UrbanCodeDeployAgent()" &
     echo "InSightsUCDAgent started"
    else
     echo "InSightsUCDAgent already in stopped state"
     echo "InSightsUCDAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/ucd
     python -c "from com.cognizant.devops.platformagents.agents.deployment.ucd.UrbanCodeDeployAgent import UrbanCodeDeployAgent; UrbanCodeDeployAgent()" &
     echo "InSightsUCDAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsUCDAgent"
    if [[ $(ps aux | grep '[d]eployment.ucd.UrbanCodeDeployAgent' | awk '{print $2}') ]]; then
     echo "InSightsUCDAgent is running"
    else
     echo "InSightsUCDAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsUCDAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
