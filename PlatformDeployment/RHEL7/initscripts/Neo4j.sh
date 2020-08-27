#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
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
# /etc/init.d/Neo4j

### BEGIN INIT INFO
# Provides: Runs a Neo4j script on startup
# Required-Start: Neo4j start
# Required-Stop: Neo4j stop
# Default-Start: 2 3 4 5
# Default-stop: 0 1 6
# Short-Description: Simple script to run Neo4j program at boot
# Description: Runs a Neo4j program at boot
### END INIT INFO 
 
[[ -z "${NEO4J_INIT_HOME}" ]] && MyVar=sudo env | grep NEO4J_INIT_HOME | cut -d'=' -f2 || MyVar="${NEO4J_INIT_HOME}"
MyVar=$MyVar/neo4j-Insights
echo $MyVar

case "$1" in
  start)
   cd $MyVar
   ./bin/neo4j start
    ;;
  stop)
   cd $MyVar
   ./bin/neo4j stop
    ;;
  restart)
   cd $MyVar
   ./bin/neo4j restart
    ;;
  status)
   cd $MyVar
   ./bin/neo4j status
    ;;
  *)
    echo "Usage: /etc/init.d/Neo4j {start|stop|restart|status}"
    exit 1
    ;;
esac
exit 0
