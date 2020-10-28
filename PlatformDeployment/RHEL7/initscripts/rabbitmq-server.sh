# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------

#!/bin/sh
# /etc/init.d/rabbitmq-server

### BEGIN INIT INFO
# Provides: Runs a rabbitmq script on startup
# Required-Start: rabbitmq start
# Required-Stop: rabbitmq stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run rabbitmq-server program at boot
# Description: Runs a rabbitmq-server program at boot
### END INIT INFO
case "$1" in
  start)
    if [[ $(ps aux | grep '[r]abbitmq' | awk '{print $2}') ]]; then
     echo "rabbitmq already running"
    else
     echo "Starting rabbitmq"
     sudo nohup /usr/sbin/rabbitmq-server &
     echo $! > /usr/sbin/rabbitmq-server-pid.txt.txt
     sleep 10
    fi
    if [[ $(ps aux | grep '[r]abbitmq' | awk '{print $2}') ]]; then
     echo "rabbitmq-server Started Successfully"
    else
     echo "rabbitmq-server Failed to Start"
    fi
    ;;
  stop)
    echo "Stopping rabbitmq-server"
    if [[ $(ps aux | grep '[r]abbitmq' | awk '{print $2}') ]]; then
     sudo kill -9 $(ps aux | grep '[r]abbitmq' | awk '{print $2}')
     sleep 10
    else
     echo "rabbitmq-server already in stopped state"
    fi
    if [[ $(ps aux | grep '[r]abbitmq' | awk '{print $2}') ]]; then
     echo "rabbitmq-server Failed to Stop"
    else
     echo "rabbitmq-server Stopped"
    fi
    ;;
  restart)
    echo "Restarting rabbitmq-server"
    if [[ $(ps aux | grep '[r]abbitmq' | awk '{print $2}') ]]; then
     echo "rabbitmq-server stopping"
     sudo kill -9 $(ps aux | grep '[r]abbitmq' | awk '{print $2}')
     sleep 10
     echo "rabbitmq-server stopped"
     echo "rabbitmq-server starting"
     sudo nohup java -jar /usr/sbin/rabbitmq-server &
     echo $! > rabbitmq-server-pid.txt
         sleep 10
     echo "rabbitmq-server started"
    else
     echo "rabbitmq-server already in stopped state"
     echo "rabbitmq-server starting"
     sudo nohup java -jar /usr/sbin/rabbitmq-server &
     echo $! > rabbitmq-server-pid.txt
         sleep 10
     echo "rabbitmq-server started"
    fi
    ;;
  status)
    echo "Checking the Status of rabbitmq-server"
    if [[ $(ps aux | grep '[r]abbitmq' | awk '{print $2}') ]]; then
     echo "rabbitmq-server is running"
    else
     echo "rabbitmq-server is stopped"
    fi
    ;;
  *)
    echo "Usage: /etc/init.d/rabbitmq-server {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
