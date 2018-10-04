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
# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
config=$(cat com/cognizant/devops/platformagents/agents/agentdaemon/config.json| tr -d '\n'| tr -d '\r') 
echo $config
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	            sudo service InSightsDaemonAgent stop
				sudo rm -R /etc/init.d/InSightsDaemonAgent
				echo "Service un-installation step completed"
		        ;;
		     *)
                echo "Daemon Running on Linux..."
				sudo cp -xp InSightsDaemonAgent.sh  /etc/init.d/InSightsDaemonAgent
				sudo chmod +x /etc/init.d/InSightsDaemonAgent
				sudo chkconfig InSightsDaemonAgent on
				sudo service  InSightsDaemonAgent status
				sudo service  InSightsDaemonAgent stop
				sudo service  InSightsDaemonAgent status
				sudo service  InSightsDaemonAgent start
				sudo service  InSightsDaemonAgent status
				sudo rm Daemonagent_db.sql
				sudo echo  :> Daemonagent_db.sql
				sudo chmod +x Daemonagent_db.sql
				echo 'write file '
				sudo echo "INSERT INTO public.agent_configuration(id, agent_id, agent_json, agent_key, agent_status, agent_version,data_update_supported, os_version, tool_category, tool_name,unique_key, update_date) VALUES (100, 0,'$config','daemon-1523257126' ,'' ,'' , FALSE,'' , 'DAEMONAGENT', 'AGENTDAEMON', 'daemon-1523257126', current_date);">Daemonagent_db.sql
				psql -U postgres -d "insight" -f Daemonagent_db.sql
				echo "Service installation steps completed"
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	       case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	            sudo systemctl stop InSightsDaemonAgent
				sudo rm -R /etc/systemd/system/InSightsDaemonAgent.service
				echo "Service un-installation step completed"
		        ;;
		     *)
                echo "Daemon Running on Ubuntu..."
				cp -xp InSightsDaemonAgent.service /etc/systemd/system
				systemctl enable InSightsDaemonAgent
				systemctl start InSightsDaemonAgent
				sudo rm Daemonagent_db.sql
				sudo echo  :> Daemonagent_db.sql
				sudo chmod +x Daemonagent_db.sql
				echo 'write file '
				sudo echo "INSERT INTO public.agent_configuration(id, agent_id, agent_json, agent_key, agent_status, agent_version,data_update_supported, os_version, tool_category, tool_name,unique_key, update_date) VALUES (100, 0,'$config','daemon-1523257126' ,'' ,'' , FALSE,'' , 'DAEMONAGENT', 'AGENTDAEMON', 'daemon-1523257126', current_date);">Daemonagent_db.sql
				psql -U postgres -d "insight" -f Daemonagent_db.sql
				echo "Service installation steps completed"
                ;;
		   esac
		   ;;
        centos)
                echo "Daemon Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
				;;
esac
