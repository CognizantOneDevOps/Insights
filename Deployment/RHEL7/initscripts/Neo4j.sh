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
MyVar=$MyVar/neo4j-community-3.3.0
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
