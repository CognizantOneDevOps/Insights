#! /bin/sh
# /etc/init.d/InSightsJiraAgent

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
    if [[ $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}') ]]; then
     echo "InSightsJiraAgent already running"
    else
     echo "Starting InSightsJiraAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/jira
     python -c "from com.cognizant.devops.platformagents.agents.alm.jira.JiraAgent import JiraAgent; JiraAgent()" &
    fi
    if [[ $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}') ]]; then
     echo "InSightsJiraAgent Started Sucessfully"
    else
     echo "InSightsJiraAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsJiraAgent"
    if [[ $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}')
    else
     echo "InSightsJiraAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}') ]]; then
     echo "InSightsJiraAgent Failed to Stop"
    else
     echo "InSightsJiraAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsJiraAgent"
    if [[ $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}') ]]; then
     echo "InSightsJiraAgent stopping"
     sudo kill -9 $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}')
     echo "InSightsJiraAgent stopped"
     echo "InSightsJiraAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/jira
     python -c "from com.cognizant.devops.platformagents.agents.alm.jira.JiraAgent import JiraAgent; JiraAgent()" &
     echo "InSightsJiraAgent started"
    else
     echo "InSightsJiraAgent already in stopped state"
     echo "InSightsJiraAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/jira
     python -c "from com.cognizant.devops.platformagents.agents.alm.jira.JiraAgent import JiraAgent; JiraAgent()" &
     echo "InSightsJiraAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsJiraAgent"
    if [[ $(ps aux | grep '[a]lm.jira.JiraAgent' | awk '{print $2}') ]]; then
     echo "InSightsJiraAgent is running"
    else
     echo "InSightsJiraAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsJiraAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
