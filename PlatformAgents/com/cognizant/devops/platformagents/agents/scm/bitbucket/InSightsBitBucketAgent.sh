#! /bin/sh
# /etc/init.d/InSightsBitBucketAgent

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
    if [[ $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketAgent already running"
    else
     echo "Starting InSightsBitBucketAgent"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/bitbucket
     python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucket.BitBucketAgent import BitBucketAgent; BitBucketAgent()" &
    fi
    if [[ $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketAgent Started Sucessfully"
    else
     echo "InSightsBitBucketAgent Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping InSightsBitBucketAgent"
    if [[ $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}')
    else
     echo "InSIghtsBitBucketAgent already in stopped state"
    fi
    if [[ $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketAgent Failed to Stop"
    else
     echo "InSightsBitBucketAgent Stopped"
    fi
    ;;
  restart)
    echo "Restarting InSightsBitBucketAgent"
    if [[ $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketAgent stopping"
     sudo kill -9 $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}')
     echo "InSightsBitBucketAgent stopped"
     echo "InSightsBitBucketAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/bitbucket
     python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucket.BitBucketAgent import BitBucketAgent; BitBucketAgent()" &
     echo "InSightsBitBucketAgent started"
    else
     echo "InSightsBitBucketAgent already in stopped state"
     echo "InSightsBitBucketAgent starting"
     cd $INSIGHTS_AGENT_HOME/PlatformAgents/bitbucket
     python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucket.BitBucketAgent import BitBucketAgent; BitBucketAgent()" &
     echo "InSightsBitBucketAgent started"
    fi
    ;;
  status)
    echo "Checking the Status of InSightsBitBucketAgent"
    if [[ $(ps aux | grep '[s]cm.bitbucket.BitBucketAgent' | awk '{print $2}') ]]; then
     echo "InSightsBitBucketAgent is running"
    else
     echo "InSightsBitBucketAgent is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/InSightsBitBucketAgent {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
