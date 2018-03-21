#! /bin/sh
# /etc/init.d/InSightsJenkinsAgent

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
    if [[ $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}') ]]; then
     echo "InSightsJenkinsAgent already running"
    else
     echo "Starting InSightsJenkinsAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/jenkins
     python -c "from com.cognizant.devops.platformagents.agents.ci.jenkins.JenkinsAgent import JenkinsAgent; JenkinsAgent()" &
    fi
    if [[ $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}') ]]; then
     echo "InSightsJenkinsAgent Started Sucessfully"
    else
     echo "InSightsJenkinsAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsJenkinsAgent"
    if [[ $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}')
    else
     echo "InSIghtsJenkinsAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}') ]]; then
     echo "InSightsJenkinsAgent Failed to Stop"
    else
     echo "InSightsJenkinsAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsJenkinsAgent"
    if [[ $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}') ]]; then
     echo "InSightsJenkinsAgent stopping"
     sudo kill -9 $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}')
     echo "InSightsJenkinsAgent stopped"
     echo "InSightsJenkinsAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/jenkins
     python -c "from com.cognizant.devops.platformagents.agents.ci.jenkins.JenkinsAgent import JenkinsAgent; JenkinsAgent()" &
     echo "InSightsJenkinsAgent started"
    else
     echo "InSightsJenkinsAgent already in stopped state"
     echo "InSightsJenkinsAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/jenkins
     python -c "from com.cognizant.devops.platformagents.agents.ci.jenkins.JenkinsAgent import JenkinsAgent; JenkinsAgent()" &
     echo "InSightsJenkinsAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsJenkinsAgent"
    if [[ $(ps aux | grep '[c]i.jenkins.JenkinsAgent' | awk '{print $2}') ]]; then
     echo "InSightsJenkinsAgent is running"
    else
     echo "InSightsJenkinsAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsJenkinsAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
