#! /bin/sh
# /etc/init.d/InSightsCircleciAgent

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
    if [[ $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}') ]]; then
     echo "InSightsCircleciAgent already running"
    else
     echo "Starting InSightsCircleciAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/circleci
     python -c "from com.cognizant.devops.platformagents.agents.ci.circleci.CircleAgent import CircleAgent; CircleAgent()" &
    fi
    if [[ $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}') ]]; then
     echo "InSightsCircleciAgent Started Sucessfully"
    else
     echo "InSightsCircleciAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsCircleciAgent"
    if [[ $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}')
    else
     echo "InSIghtsCircleciAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}') ]]; then
     echo "InSightsCircleciAgent Failed to Stop"
    else
     echo "InSightsCircleciAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsCircleciAgent"
    if [[ $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}') ]]; then
     echo "InSightsCircleciAgent stopping"
     sudo kill -9 $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}')
     echo "InSightsCircleciAgent stopped"
     echo "InSightsCircleciAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/circleci
     python -c "from com.cognizant.devops.platformagents.agents.ci.circleci.CircleAgent import CircleAgent; CircleAgent()" &
     echo "InSightsCircleciAgent started"
    else
     echo "InSightsCircleciAgent already in stopped state"
     echo "InSightsCircleciAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/circleci
     python -c "from com.cognizant.devops.platformagents.agents.ci.circleci.CircleAgent import CircleAgent; CircleAgent()" &
     echo "InSightsCircleciAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsCircleciAgent"
    if [[ $(ps aux | grep '[c]i.circleci.CircleAgent' | awk '{print $2}') ]]; then
     echo "InSightsCircleciAgent is running"
    else
     echo "InSightsCircleciAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsCircleciAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
