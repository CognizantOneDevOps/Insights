#! /bin/sh
# /etc/init.d/InSightsPivotalTrackerAgent

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
    if [[ $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}') ]]; then
     echo "InSightsPivotalTrackerAgent already running"
    else
     echo "Starting InSightsPivotalTrackerAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/pivotalTracker
     python -c "from com.cognizant.devops.platformagents.agents.alm.pivotalTracker.PivotalTrackerAgent import PivotalTrackerAgent; PivotalTrackerAgent()" &
    fi
    if [[ $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}') ]]; then
     echo "InSightsPivotalTrackerAgent Started Sucessfully"
    else
     echo "InSightsPivotalTrackerAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsPivotalTrackerAgent"
    if [[ $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}')
    else
     echo "InSightsPivotalTrackerAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}') ]]; then
     echo "InSightsPivotalTrackerAgent Failed to Stop"
    else
     echo "InSightsPivotalTrackerAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsPivotalTrackerAgent"
    if [[ $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}') ]]; then
     echo "InSightsPivotalTrackerAgent stopping"
     sudo kill -9 $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}')
     echo "InSightsPivotalTrackerAgent stopped"
     echo "InSightsPivotalTrackerAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/pivotalTracker
     python -c "from com.cognizant.devops.platformagents.agents.alm.pivotalTracker.PivotalTrackerAgent import PivotalTrackerAgent; PivotalTrackerAgent()" &
     echo "InSightsPivotalTrackerAgent started"
    else
     echo "InSightsPivotalTrackerAgent already in stopped state"
     echo "InSightsPivotalTrackerAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/pivotalTracker
     python -c "from com.cognizant.devops.platformagents.agents.alm.pivotalTracker.PivotalTrackerAgent import PivotalTrackerAgent; PivotalTrackerAgent()" &
     echo "InSightsPivotalTrackerAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsPivotalTrackerAgent"
    if [[ $(ps aux | grep '[a]lm.pivotalTracker.PivotalTrackerAgent' | awk '{print $2}') ]]; then
     echo "InSightsPivotalTrackerAgent is running"
    else
     echo "InSightsPivotalTrackerAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsPivotalTrackerAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0