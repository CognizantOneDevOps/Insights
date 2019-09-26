echo "Stop NeO4J Service"
net stop neo4j
Timeout 2
echo "Stop Elastic Search Service"
net stop elasticsearch-service-x64
Timeout 2