echo "Setting up Neo4j as windows service"
call %~dp0neo4j-community-3.3.0\bin\neo4j install-service
Timeout 2