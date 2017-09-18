echo "Get required env varidables for Insights"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_first.sh -O insights_first.sh  && sh insights_first.sh
echo "Installing Java"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_java.sh -O insights_java.sh && sh insights_java.sh
echo "Insitalling Elastic Search"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_es.sh -O insights_es.sh  && sh insights_es.sh
echo "Installing Neo4j"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_neo4j.sh -O insights_neo4j.sh && sh insights_neo4j.sh
echo "Install Postgres"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_postgres.sh -O insights_postgres.sh && sh insights_postgres.sh
echo "Install Grafana"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_grafana.sh -O insights_grafana.sh && sh insights_grafana.sh
echo "Install Python 2.7.11 with required libraries needed for Insights"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_python.sh -O insights_python.sh && sh insights_python.sh
echo "Install Erlang and RabbitMQ"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_rabbitmq.sh -O insights_rabbitmq.sh && sh insights_rabbitmq.sh
echo "Install Tomcat 7"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_tomcat7.sh -O insights_tomcat7.sh && sh insights_tomcat7.sh
#echo "Deploy Insights WAR"
#wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_war.sh -O insights_war.sh && sh insights_war.sh
#echo "Deploy Insights UI"
#wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_ui.sh -O insights_ui.sh  && sh insights_ui.sh
echo "Get Insights Agents"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_agents.sh -O insights_agents.sh && sh insights_agents.sh
echo "Get Insights Engine"
wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_engine.sh -O insights_engine.sh && sh insights_engine.sh
#echo "Get Insights Initd scripts"
#wget http://platform.cogdevops.com/InSightsV1.0/scripts/insights_initscripts.sh -O insights_initscripts.sh && sh insights_initscripts.sh
