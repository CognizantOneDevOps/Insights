#!/bin/sh
# /etc/init.d/Tomcat8

### BEGIN INIT INFO
# Provides: Runs a Tomcat script on startup
# Required-Start: Tomcat start
# Required-Stop: Tomcat stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run Tomcat program at boot
# Description: Runs a Tomcat program at boot
### END INIT INFO
TOMCAT_HOME="/opt/apache-tomcat-8.5.27/bin"
case "$1" in
  start)
    if [[ $(ps aux | grep '[t]omcat' | awk '{print $2}') ]]; then
     echo "Tomcat already running"
    else
     echo "Starting Tomcat"
     cd $TOMCAT_HOME
     nohup sudo sh startup.sh &
     echo $! > tomcat-pid.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[t]omcat' | awk '{print $2}') ]]; then
     echo "Tomcat Started Successfully"
    else
     echo "Tomcat Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping Tomcat"
    if [[ $(ps aux | grep '[t]omcat' | awk '{print $2}') ]]; then
     cd $TOMCAT_HOME
     nohup sudo sh shutdown.sh &
     sudo kill -9 $(ps aux | grep '[t]omcat' | awk '{print $2}')
     sleep 10
    else
     echo "Tomcat already in stopped state"
    fi
    if [[ $(ps aux | grep '[t]omcat' | awk '{print $2}') ]]; then
     echo "Tomcat Failed to Stop"
    else
     echo "Tomcat Stopped"
    fi
    ;;
  status)
    echo "Checking the Status of Tomcat"
    if [[ $(ps aux | grep '[t]omcat' | awk '{print $2}') ]]; then
     echo "Tomcat is running"
    else
     echo "Tomcat is stopped"
    fi
    ;;
  *)
    echo "--"
    exit 1
    ;;
esac
exit 0

