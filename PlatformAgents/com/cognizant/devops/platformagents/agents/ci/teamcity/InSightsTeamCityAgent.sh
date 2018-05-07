#! /bin/sh
# /etc/init.d/InSightsTeamCityAgent

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
    if [[ $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}') ]]; then
     echo "InSightsTeamCityAgent already running"
    else
     echo "Starting InSightsTeamCityAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/teamcity
     python -c "from com.cognizant.devops.platformagents.agents.ci.teamcity.TeamCityAgent import TeamCityAgent; TeamCityAgent()" &
    fi
    if [[ $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}') ]]; then
     echo "InSightsTeamCityAgent Started Sucessfully"
    else
     echo "InSightsTeamCityAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsTeamCityAgent"
    if [[ $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}')
    else
     echo "InSIghtsTeamCityAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}') ]]; then
     echo "InSightsTeamCityAgent Failed to Stop"
    else
     echo "InSightsTeamCityAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsTeamCityAgent"
    if [[ $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}') ]]; then
     echo "InSightsTeamCityAgent stopping"
     sudo kill -9 $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}')
     echo "InSightsTeamCityAgent stopped"
     echo "InSightsTeamCityAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/teamcity
     python -c "from com.cognizant.devops.platformagents.agents.ci.teamcity.TeamCityAgent import TeamCityAgent; TeamCityAgent()" &
     echo "InSightsTeamCityAgent started"
    else
     echo "InSightsTeamCityAgent already in stopped state"
     echo "InSightsTeamCityAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/teamcity
     python -c "from com.cognizant.devops.platformagents.agents.ci.teamcity.TeamCityAgent import TeamCityAgent; TeamCityAgent()" &
     echo "InSightsTeamCityAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsTeamCityAgent"
    if [[ $(ps aux | grep '[c]i.teamcity.TeamCityAgent' | awk '{print $2}') ]]; then
     echo "InSightsTeamCityAgent is running"
    else
     echo "InSightsTeamCityAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsTeamCityAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
