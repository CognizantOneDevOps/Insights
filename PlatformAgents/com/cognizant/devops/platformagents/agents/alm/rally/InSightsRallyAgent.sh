#! /bin/sh
# /etc/init.d/InSightsRallyAgent

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
    if [[ $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}') ]]; then
     echo "InSightsRallyAgent already running"
    else
     echo "Starting InSightsRallyAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/rally
     python -c "from com.cognizant.devops.platformagents.agents.alm.rally.RallyAgent import RallyAgent; RallyAgent()" &
    fi
    if [[ $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}') ]]; then
     echo "InSightsRallyAgent Started Sucessfully"
    else
     echo "InSightsRallyAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsRallyAgent"
    if [[ $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}')
    else
     echo "InSightsRallyAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}') ]]; then
     echo "InSightsRallyAgent Failed to Stop"
    else
     echo "InSightsRallyAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsRallyAgent"
    if [[ $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}') ]]; then
     echo "InSightsRallyAgent stopping"
     sudo kill -9 $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}')
     echo "InSightsRallyAgent stopped"
     echo "InSightsRallyAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/rally
     python -c "from com.cognizant.devops.platformagents.agents.alm.rally.RallyAgent import RallyAgent; RallyAgent()" &
     echo "InSightsRallyAgent started"
    else
     echo "InSightsRallyAgent already in stopped state"
     echo "InSightsRallyAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/rally
     python -c "from com.cognizant.devops.platformagents.agents.alm.rally.RallyAgent import RallyAgent; RallyAgent()" &
     echo "InSightsRallyAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsRallyAgent"
    if [[ $(ps aux | grep '[a]lm.rally.RallyAgent' | awk '{print $2}') ]]; then
     echo "InSightsRallyAgent is running"
    else
     echo "InSightsRallyAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsRallyAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
