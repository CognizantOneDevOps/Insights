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
echo "Installing All Insights components"
echo "Installing First"
sudo sh ./insights_first.sh
echo "Installing Java"
sudo sh ./insights_java.sh
echo "Installing Postgres"
sudo sh ./insights_postgres.sh
echo "Installing Grafana"
sudo sh ./insights_grafana.sh
echo "Installing Python 2.7.11 with required libraries needed for Insights"
sudo sh ./insights_python.sh
echo "Installing Erlang and RabbitMQ"
sudo sh ./insights_rabbitmq.sh
echo "Installing Tomcat "
sudo sh ./insights_tomcat.sh
echo "Installing Insights Engine"
sudo sh ./insights_enginejar.sh
sleep 30
echo "Installing Insights Agents"
sudo sh ./insights_agents.sh

