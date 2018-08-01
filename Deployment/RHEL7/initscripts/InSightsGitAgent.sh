#! /bin/sh
# /etc/init.d/InSightsGitAgent

### BEGIN INIT INFO
# Provides: Runs a Python script on startup
# Required-Start: BootPython start
# Required-Stop: BootPython stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run python program at boot
# Description: Runs a python program at boot
### END INIT INFO

case "$1" in
  start)
    if [[ $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}') ]]; then
     echo "InSightsGitAgent already running"
    else
     echo "Starting InSightsGitAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents
     python -c "from com.cognizant.devops.platformagents.agents.scm.git.GitAgent import GitAgent; GitAgent()" &
    fi
    if [[ $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}') ]]; then
     echo "InSightsGitAgent Started Sucessfully"
    else
     echo "InSightsGitAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsGitAgent"
    if [[ $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}')
    else
     echo "InSIghtsGitAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}') ]]; then
     echo "InSightsGitAgent Failed to Stop"
    else
     echo "InSightsGitAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsGitAgent"
    if [[ $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}') ]]; then
     echo "InSightsGitAgent stopping"
     sudo kill -9 $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}')
     echo "InSightsGitAgent stopped"
     echo "InSightsGitAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents
     python -c "from com.cognizant.devops.platformagents.agents.scm.git.GitAgent import GitAgent; GitAgent()" &
     echo "InSightsGitAgent started"
    else
     echo "InSightsGitAgent already in stopped state"
     echo "InSightsGitAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents
     python -c "from com.cognizant.devops.platformagents.agents.scm.git.GitAgent import GitAgent; GitAgent()" &
     echo "InSightsGitAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsGitAgent"
    if [[ $(ps aux | grep '[s]cm.git.GitAgent' | awk '{print $2}') ]]; then
     echo "InSightsGitAgent is running"
    else
     echo "InSightsGitAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsGitAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
