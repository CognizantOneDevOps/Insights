#! /bin/sh
# /etc/init.d/InSightsSonarAgent

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
    if [[ $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}') ]]; then
     echo "InSightsSonarAgent already running"
    else
     echo "Starting InSightsSonarAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/sonar
     python -c "from com.cognizant.devops.platformagents.agents.codequality.sonar.SonarAgent import SonarAgent; SonarAgent()" &
    fi
    if [[ $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}') ]]; then
     echo "InSightsSonarAgent Started Sucessfully"
    else
     echo "InSightsSonarAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsSonarAgent"
    if [[ $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}')
    else
     echo "InSIghtsSonarAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}') ]]; then
     echo "InSightsSonarAgent Failed to Stop"
    else
     echo "InSightsSonarAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsSonarAgent"
    if [[ $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}') ]]; then
     echo "InSightsSonarAgent stopping"
     sudo kill -9 $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}')
     echo "InSightsSonarAgent stopped"
     echo "InSightsSonarAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/sonar
     python -c "from com.cognizant.devops.platformagents.agents.codequality.sonar.SonarAgent import SonarAgent; SonarAgent()" &
     echo "InSightsSonarAgent started"
    else
     echo "InSightsSonarAgent already in stopped state"
     echo "InSightsSonarAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/sonar
     python -c "from com.cognizant.devops.platformagents.agents.codequality.sonar.SonarAgent import SonarAgent; SonarAgent()" &
     echo "InSightsSonarAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsSonarAgent"
    if [[ $(ps aux | grep '[c]odequality.sonar.SonarAgent' | awk '{print $2}') ]]; then
     echo "InSightsSonarAgent is running"
    else
     echo "InSightsSonarAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsSonarAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
