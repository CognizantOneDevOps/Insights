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
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
           case $action in 
                [uU][nN][iI][nN][sS][tT][aA][lL][lL])
                	sudo service InSightsJiraAgent stop
					sudo rm -R /etc/init.d/InSightsJiraAgent
					echo "Service un-installation step completed"
		            ;;
		        *)
                   echo "JiraAgent Running on Linux..."
				   sudo cp -xp InSightsJiraAgent.sh  /etc/init.d/InSightsJiraAgent
				   sudo chmod +x /etc/init.d/InSightsJiraAgent
				   sudo chkconfig InSightsJiraAgent on
				   sudo service  InSightsJiraAgent status
				   sudo service InSightsJiraAgent stop
				   sudo service  InSightsJiraAgent status
				   sudo service InSightsJiraAgent start
				   sudo service  InSightsJiraAgent status
				   echo "Service installed.."
                   ;;
		   esac
		   ;;
        [uU][bB][uU][nN][tT][uU])
	        case $action in 
                [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
					sudo systemctl stop InSightsJiraAgent
					sudo rm -R /etc/systemd/system/InSightsJiraAgent.service
					echo "Service un-installation step completed"				
			        ;;
					
				*)
                   echo "JiraAgent Running on Ubuntu..."
				   sudo cp -xp InSightsJiraAgent.service /etc/systemd/system
				   sudo systemctl enable InSightsJiraAgent
				   sudo systemctl start InSightsJiraAgent
				   echo "Service Installed..."
                   ;;
		    esac
			;;
        centos)
                echo "JiraAgent Running on centso..."
                ;;
        *)
        	    echo "JiraAgent Please provide correct OS input"
esac
