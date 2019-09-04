echo "Enter the Offline Folder location"
read DIRECTORY
if [ -d "$DIRECTORY" ]; then
        echo "Choose what you want to install type the option example "1" for All Insights components
		1) All Insights components
		2) InsightsHome
		3) Java
		4) Neo4j
		5) Postgres
		6) Grafana
		7) Python
		8) RabbitMQ
		9) Tomcat8
		10) Agents
		11) Elasticsearch
		12) EngineJar"
		installInsightsHome()
		{
				echo "InsightsHome"
				echo "#################### Setting up Insights Home ####################"
				cd /usr/
				mkdir INSIGHTS_HOME
				cd INSIGHTS_HOME
				cp -R $DIRECTORY/Offline_Installation/.InSights/ ./
				export INSIGHTS_HOME=`pwd`
				echo INSIGHTS_HOME=`pwd` |  tee -a /etc/environment
				echo "export" INSIGHTS_HOME=`pwd` |  tee -a /etc/profile
				chmod -R 777 /usr/INSIGHTS_HOME/
				source /etc/environment
				source /etc/profile
				sleep 20
				if [[ -z "${INSIGHTS_HOME}" ]]; then
						insightshomeStatus="FALLURE: Error in Setting Environment variable INSIGHTS_HOME"
				else
						insightshomeStatus="SUCCESS: INSIGHTS_HOME is set"
				fi
		}
		installJava()
		{
				echo "Java"
				echo "#################### Installing Java with Env Variable ####################"
				cd /opt/
				cp $DIRECTORY/Offline_Installation/Java/jdklinux.tar.gz  ./
				tar xzf jdklinux.tar.gz
				export JAVA_HOME=/opt/jdklinux
				echo JAVA_HOME=/opt/jdklinux  |  tee -a /etc/environment
				echo "export" JAVA_HOME=/opt/jdklinux |  tee -a /etc/profile
				export JRE_HOME=/opt/jdklinux/jre
				echo JRE_HOME=/opt/jdklinux/jre |  tee -a /etc/environment
				echo "export" JRE_HOME=/opt/jdklinux/jre |  tee -a /etc/profile
				export PATH=$PATH:/opt/jdklinux/bin:/opt/jdklinux/jre/bin
				echo PATH=$PATH:/opt/jdklinux/bin:/opt/jdklinux/jre/bin |  tee -a /etc/environment
				alternatives --install /usr/bin/java java /opt/jdklinux/bin/java 20000
				update-alternatives --install "/usr/bin/java" "java" "/opt/jdklinux/bin/java" 1
				update-alternatives --install "/usr/bin/javac" "javac" "/opt/jdklinux/bin/javac" 1
				update-alternatives --install "/usr/bin/javaws" "javaws" "/opt/jdklinux/bin/javaws" 1
				update-alternatives --set java /opt/jdklinux/bin/java
				update-alternatives --set javac /opt/jdklinux/bin/javac
				update-alternatives --set javaws /opt/jdklinux/bin/javaws
				source /etc/environment
				source /etc/profile
				sleep 20
		}
		installElasticsearch()
		{
				echo "Elasticsearch"
				cd /opt/
				mkdir elasticsearch
				cd elasticsearch
				cp $DIRECTORY/Offline_Installation/Elasticsearch/elasticsearch.rpm ./
				rpm -Uvh elasticsearch.rpm
				cp -R  $DIRECTORY/Offline_Installation/Elasticsearch/ElasticSearch/ ./
				cp ElasticSearch/elasticsearch.yml /etc/elasticsearch/elasticsearch.yml
				cp ElasticSearch/log4j2.properties /etc/elasticsearch/log4j2.properties
				systemctl daemon-reload
				systemctl enable elasticsearch.service
				systemctl start elasticsearch.service
				sleep 20
				curl -XPUT 'localhost:9200/_template/neo4j-*' -d '{"order" : 0, "template": "neo4j-*","settings": {"index.number_of_shards": "5"},"mappings": {"_default_": {"dynamic_templates": [{"string_fields" : {"mapping" : {"index" : "analyzed","omit_norms" : true,"type" : "string","fields" : {"raw" : {"ignore_above" : 256,"index" : "not_analyzed","type" : "string"}}},"match_mapping_type" : "string","match" : "*"}}]}},"aliases": {}}}'
				sleep 20
		}
		installNeo4j()
		{
				echo "Neo4j"
				cd /opt/
				echo "#################### Installing Neo4j with configs and user creation ####################"
				sed  -i '$ a root   soft    nofile  40000' /etc/security/limits.conf
				sed  -i '$ a root   hard    nofile  40000' /etc/security/limits.conf
				source /etc/environment
				mkdir NEO4J_HOME
				cd NEO4J_HOME
				cp $DIRECTORY/Offline_Installation/Neo4j/neo4j-Insights.tar.gz ./
				tar -xf neo4j-Insights.tar.gz
				chmod -R 755 neo4j-Insights
				cd neo4j-Insights
				./bin/neo4j start
				sleep 40
				curl -X POST -u neo4j:neo4j -H "Content-Type: application/json" -d '{"password":"C0gnizant@1"}' http://localhost:7474/user/neo4j/password
				sleep 10
				cd ../
				export NEO4J_INIT_HOME=`pwd`
				echo NEO4J_INIT_HOME=`pwd` |  tee -a /etc/environment
				echo "export" NEO4J_INIT_HOME=`pwd` |  tee -a /etc/profile
				source /etc/environment
				source /etc/profile
				cd /etc/init.d/
				cp $DIRECTORY/Offline_Installation/Neo4j/Neo4j.sh Neo4j
				[[ -z "${NEO4J_INIT_HOME}" ]] && MyVar=sudo env | grep NEO4J_INIT_HOME | cut -d'=' -f2 || MyVar="${NEO4J_INIT_HOME}"
				sed -i -e "s|neo4j_envValue|${MyVar}|g" ./Neo4j

				chmod +x Neo4j
				chkconfig Neo4j on
				service Neo4j start
				sleep 20
		}
		installPostgres()
		{
				echo "Postgres"
				echo "#################### Installing Postgres with configs , Databases and Roles ####################"
				cd /opt/
				rpm -ivh  $DIRECTORY/Offline_Installation/Postgres/*.rpm
				/usr/pgsql-9.5/bin/postgresql95-setup initdb
				systemctl enable postgresql-9.5.service
				chkconfig postgresql-9.5 on
				cp $DIRECTORY/Offline_Installation/Postgres/pg_hba.conf  /var/lib/pgsql/9.5/data/pg_hba.conf
				systemctl start postgresql-9.5.service
				useradd grafana
				usermod --password C0gnizant@1 grafana
				cp $DIRECTORY/Offline_Installation/Postgres/dbscript.sql ./
				chmod +x dbscript.sql
				psql -U postgres -f dbscript.sql
				sleep 20
		}
		installGrafana()
		{
				echo "Grafana"
				echo "#################### Installing Grafana (running as BG process) with user creation ####################"
				cd /opt/
				mkdir grafana
				cd grafana
				cp $DIRECTORY/Offline_Installation/Grafana/grafana.tar.gz ./
				tar -zxvf grafana.tar.gz
				cp $DIRECTORY/Offline_Installation/Grafana/ldap.toml ./conf/ldap.toml
				cp $DIRECTORY/Offline_Installation/Grafana/defaults.ini ./conf/defaults.ini
				nohup ./bin/grafana-server &
				echo $! > grafana-pid.txt
				sleep 10
				curl -X POST -u admin:admin -H "Content-Type: application/json" -d '{"name":"PowerUser","email":"PowerUser@PowerUser.com","login":"PowerUser","password":"C0gnizant@1"}' http://localhost:3000/api/admin/users
				cd ..
				export GRAFANA_HOME=`pwd`
				echo GRAFANA_HOME=`pwd` |  tee -a /etc/environment
				echo "export" GRAFANA_HOME=`pwd` |  tee -a /etc/profile
				source /etc/environment
				source /etc/profile
				cd /etc/init.d/
				cp $DIRECTORY/Offline_Installation/Grafana/Grafana5.sh Grafana
				[[ -z "${GRAFANA_HOME}" ]] && GRAFANA_HOME_VAR=sudo env | grep GRAFANA_HOME | cut -d'=' -f2 || GRAFANA_HOME_VAR="${GRAFANA_HOME}"
				echo $GRAFANA_HOME_VAR
				sed -i -e "s|grafana_envValue|${GRAFANA_HOME_VAR}|g" ./Grafana
				chmod +x Grafana
				chkconfig Grafana on
				service Grafana start
				sleep 20
		}
		installPython()
		{
				echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
				cd /opt/
				cp -R $DIRECTORY/Offline_Installation/Python/python_dependencies ./
				cd python_dependencies
				rpm -Uvh *.rpm 
				cd ../ && rm -rf python_dependencies
				echo $(pwd)
				cp $DIRECTORY/Offline_Installation/Python/Python.tar.gz ./
				tar -zxf Python.tar.gz && cd Python
				./configure --enable-optimizations
				make altinstall
				rm -f /usr/bin/python
				ln -s /opt/Python/python /usr/bin/python
				python --version
				cp $DIRECTORY/Offline_Installation/Python/Libraries/pip-18.0-py2.py3-none-any.whl ./
				python pip-18.0-py2.py3-none-any.whl/pip install --no-index pip-18.0-py2.py3-none-any.whl
				ln -s /usr/local/bin/pip /bin/pip
				pip install $DIRECTORY/Offline_Installation/Python/Libraries/*.whl
				source /etc/environment
				source /etc/profile
				sleep 5
		}
		installRabbitMQ()
		{
				echo "RabbitMQ"
				echo "#################### Installing Erlang , required for Rabbit MQ ####################"
				cd /opt/
				mkdir erlang && cd erlang
				rpm -i $DIRECTORY/Offline_Installation/Rabbitmq/erlang.rpm
				echo "#################### Installing Rabbit MQ with configs and user creation ####################"
				mkdir rabbitmq && cd rabbitmq
				rpm -i $DIRECTORY/Offline_Installation/Rabbitmq/rabbitmq-server.noarch.rpm
				cp $DIRECTORY/Offline_Installation/Rabbitmq/rabbitmq-signing-key-public.asc ./
				cp  -R $DIRECTORY/Offline_Installation/Rabbitmq/RabbitMQ/ ./
				cd RabbitMQ  && cp rabbitmq.config /etc/rabbitmq/
				chkconfig rabbitmq-server on &&  service rabbitmq-server start
				rabbitmq-plugins enable rabbitmq_management
				sleep 15
				curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
				sleep 15
				curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"
		}
		installTomcat8()
		{
				if [[ -z "${INSIGHTS_HOME}" ]]; then
						echo Insights_Home or Java is not set to install Tomcat8
				else
						echo "#################### Installing Tomcat8 ####################"
						cd /opt
						cp $DIRECTORY/Offline_Installation/Tomcat8/apache-tomcat.tar.gz ./
						tar -zxvf apache-tomcat.tar.gz
						cp -R $DIRECTORY/Offline_Installation/Insights_artifacts/uiApp/app /opt/apache-tomcat/webapps
						cp $DIRECTORY/Offline_Installation/Insights_artifacts/serviceWar/PlatformService.war /opt/apache-tomcat/webapps
						cd apache-tomcat
						chmod -R 777 /opt/apache-tomcat
						cd /etc/init.d/
						cp $DIRECTORY/Offline_Installation/Tomcat8/Tomcat8.sh Tomcat8
						chmod +x Tomcat8
						chkconfig Tomcat8 on
						service Tomcat8 start
						sleep 20
				fi
		}
		installAgents()
		{
				echo "Agents"
				echo "################Set up Agent_Daemon#####################"
				cd /opt/ && mkdir insightsagents
				chmod -R 755 insightsagents/
				cd insightsagents
				export INSIGHTS_AGENT_HOME=`pwd`
				echo INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/environment
				echo "export" INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/profile
				mkdir OfflineAgent
				chmod -R 777 OfflineAgent
				mkdir PlatformAgents
				chmod -R 755 PlatformAgents
				echo $INSIGHTS_AGENT_HOME
				mkdir AgentDaemon
				cd AgentDaemon
				cp -R $DIRECTORY/Offline_Installation/Insights_artifacts/agentdaemon/* ./
				source /etc/environment
				source /etc/profile
				sed -i -e "s|extractionpath|$INSIGHTS_AGENT_HOME/PlatformAgents|g" $INSIGHTS_AGENT_HOME/agentdaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json
				chmod +x installdaemonagent.sh
				mkdir /opt/agentunzip/
				chmod -R 777 /opt/agentunzip
				sh ./installdaemonagent.sh Linux
				sleep 20
		}
		installEngineJar()
		{
				if [[ -z "${INSIGHTS_HOME}" ]]; then
						installInsightsHome
				else
						echo "Engine"
						echo "#################### Getting Insights Engine Jar ####################"
						mkdir /opt/insightsengine
						cd /opt/insightsengine
						export INSIGHTS_ENGINE=`pwd`
						echo INSIGHTS_ENGINE=`pwd` |  tee -a /etc/environment
						echo "export" INSIGHTS_ENGINE=`pwd` |  tee -a /etc/profile
						source /etc/environment
						source /etc/profile
						cp $DIRECTORY/Offline_Installation/Insights_artifacts/engineJar/PlatformEngine.jar ./
						sleep 2
						#nohup java -jar PlatformEngine.jar &
				fi
		}
		while :
		do
				read INPUT_STRING
				case $INPUT_STRING in
						1)
								echo "All"
								installInsightsHome
								if [[ -z "${INSIGHTS_HOME}" ]]; then
										echo FALLURE: Error in Setting Environment variable INSIGHTS_HOME
										break
								fi
								installJava
								if [[ -z "${JAVA_HOME}" ]]; then
										echo Failure: Error in Setting Environment variable JAVA_HOME
										break
								fi
								installNeo4j
					neo4j_response="curl -S http://localhost:7474/browser/"
						response=`$neo4j_response`
								if [[ $response = *"Neo4j"* ]]; then
										neo4jStatus="SUCCESS: Neo4j is up and running"
								else
										echo neo4j is down
								break
								fi
								if [[ -z "${NEO4J_INIT_HOME}" ]]; then
										echo Failure: Error in Setting Environment variable NEO4J_INIT_HOME
										break
								fi
								installPostgres
								installGrafana
								if [[ -z "${GRAFANA_HOME}" ]]; then
										grafanaEnvStatus="Failure: Error in Setting Environment variable GRAFANA_HOME"
										break
								fi
								installPython
								installRabbitMQ
								rabbitMQ_response="curl -S http://localhost:15672"
								response=`$rabbitMQ_response`
								if [[ $response = *"RabbitMQ"* ]]; then
										rabbitMQStatus="RabbitMQ is up and running "
								else
										rabbitMQStatus="RabbitMQ is dowm"
								fi
								installTomcat8
								installAgents
					installElasticsearch
								installEngineJar
								break
						;;
						2)
								installInsightsHome
								break
						;;
						3)
								installJava
								break
						;;
						4)
								installNeo4j
								break
						;;
						5)
								installPostgres
								break
						;;
						6)
								installGrafana
								break
						;;
						7)
								installPython
								break
						;;
						8)
								installRabbitMQ
								break
						;;
						9)
								installTomcat8
								break
						;;
						10)
								installAgents
								break
						;;
						11)
								installElasticsearch
								break
						;;
						12)
								installEngineJar
								break
						;;
				esac
		done
		status()
		{
				RED='\033[0;31m'
				NC='\033[0m' 
				if [[ -z "${INSIGHTS_HOME}" ]]; then
						insightshomeEnvStatus="${RED} FALLURE: Error in Setting Environment variable INSIGHTS_HOME${NC}"
				else
						insightshomeEnvStatus="SUCCESS: INSIGHTS_HOME is set"
				fi
				if [[ -z "${JAVA_HOME}" ]]; then
						javaEnvStatus="${RED} Failure: Error in Setting Environment variable JAVA_HOME${NC}"
				else
						javaEnvStatus="SUCCESS: JAVA_HOME is set"
				fi
				if [[ -z "${JRE_HOME}" ]]; then
						jreEnvStatus="${RED}Failure: Error in Setting Environment variable JAVA_HOME${NC}"
				else
						jreEnvStatus="SUCCESS: JAVA_HOME is set"
				fi
				if [[ -z "${NEO4J_INIT_HOME}" ]]; then
						neo4jEnvStatus="${RED}Failure: Error in Setting Environment variable NEO4J_INIT_HOME ${NC}"
				else
						neo4jEnvStatus="SUCCESS: NEO4J_INIT_HOME is set"
				fi
				neo4j_response="curl -S http://localhost:7474/browser/"
				response=`$neo4j_response`
				if [[ $response = *"Neo4j"* ]]; then
				neo4jStatus="SUCCESS: Neo4j is up and running"
				else
				neo4jStatus="${RED}Failure: neo4j is down ${NC}"
				fi
				if [[ -z "${GRAFANA_HOME}" ]]; then
						grafanaEnvStatus="${RED}Failure: Error in Setting Environment variable GRAFANA_HOME ${NC}"
				else
						grafanaEnvStatus="SUCCESS: GRAFANA_HOME is set"
				fi
				grafana_up="curl -S http://localhost:3000"
				response=`$grafana_up`
				if [[ $response = *"Found"* ]]; then
				grafanaStatus="grafana is up and running "
				else
				grafanaStatus="${RED}Failure: grafana is dowm ${NC} "
				fi
				rabbitMQ_response="curl -S http://localhost:15672"
				response=`$rabbitMQ_response`
				if [[ $response = *"RabbitMQ"* ]]; then
				rabbitMQStatus="RabbitMQ is up and running "
				else
				rabbitMQStatus="${RED} Failure:RabbitMQ is dowm"
				fi
				if [[ -z "${INSIGHTS_AGENT_HOME}" ]]; then
						agentEnvStatus="${RED}Failure: Error in Setting Environment variable INSIGHTS_AGENT_HOME ${NC}"
				else
						agentEnvStatus="SUCCESS: INSIGHTS_AGENT_HOME is set"
				fi
				insights_response="curl -S http://localhost:8080/app/"
				response=`$insights_response`
				if [[ $response = *"Insights"* ]]; then
						insightsStatus="SUCCESS: Insights is up and running "
		                else
						insightsStatus="${RED}Failure:Insights is down ${NC}"
				fi
				echo -e $insightshomeEnvStatus
				echo -e $javaEnvStatus
				echo -e $jreEnvStatus
				echo -e $neo4jEnvStatus
				echo -e $grafanaEnvStatus
				echo -e $neo4jStatus
				echo -e $grafanaEnvStatus
				echo -e $grafanaStatus
				echo -e $rabbitMQStatus
				echo -e $agentEnvStatus
				echo -e $insightsStatus
		}
		status
		echo "Important Note: After installing all the commands grafana enpoint to be changed in the location /usr/INSIGHTS_HOME/.InSights/server-config.json , change localhost:3000 to ***the server Public IP***:3000,Ignore if already done"
else
        echo "Type valid location"
fi


