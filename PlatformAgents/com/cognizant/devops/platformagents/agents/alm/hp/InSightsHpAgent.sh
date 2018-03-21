#! /bin/sh
# /etc/init.d/InSightsHpAgent

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
    if [[ $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent already running"
    else
     echo "Starting InSightsHpAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/hp
     python -c "from com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()" &
    fi
    if [[ $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent Started Sucessfully"
    else
     echo "InSightsHpAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsHpAgent"
    if [[ $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}')
    else
     echo "InSightsHpAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent Failed to Stop"
    else
     echo "InSightsHpAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsHpAgent"
    if [[ $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent stopping"
     sudo kill -9 $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}')
     echo "InSightsHpAgent stopped"
     echo "InSightsHpAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/hp
     python -c "from com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()" &
     echo "InSightsHpAgent started"
    else
     echo "InSightsHpAgent already in stopped state"
     echo "InSightsHpAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/hp
     python -c "from com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()" &
     echo "InSightsHpAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsHpAgent"
    if [[ $(ps aux | grep '[a]lm.hp.HpAlmAgent' | awk '{print $2}') ]]; then
     echo "InSightsHpAgent is running"
    else
     echo "InSightsHpAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsHpAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
