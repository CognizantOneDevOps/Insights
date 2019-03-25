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
toolName=$3
agentservice=$4
echo "$opt"

if [ "$#" -lt 2 ]; then
  echo "insufficient arguments" >&2
  exit 1
fi

if [ "$agentservice" == "" ]; then
   echo "Agent service name cannot be null"
   exit 1
fi

case $opt in
        [lL][Ii][nN][uU][Xx])
                case $action in 
                	[uU][nN][iI][nN][sS][tT][aA][lL][lL])
                		sudo service  $agentservice stop
						sudo rm -R /etc/init.d/$agentservice
						echo "Service un-installation step completed"
		            ;;
		         	*)
		         		echo "Git Running on Linux..."
						sudo cp -xp $INSIGHTS_AGENT_HOME/PlatformAgents/$toolName/$agentservice/$agentservice.sh  /etc/init.d/$agentservice
						sudo chmod +x /etc/init.d/$agentservice
						sudo chkconfig $agentservice on
						sudo service  $agentservice status
						sudo service  $agentservice stop
						sudo service  $agentservice status
						sudo service  $agentservice start
						sudo service  $agentservice status
						
						echo "Service installaton steps completed"
		        	;;   
				esac
		        ;;
        [uU][bB][uU][nN][tT][uU])
		     case $action in 
                [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
					sudo systemctl stop $agentservice
					sudo rm -R /etc/systemd/system/$agentservice.service
					echo "Service un-installation step completed"				
			        ;;
				*)
                   echo "Git Running on Ubuntu..."
					sudo cp -xp $INSIGHTS_AGENT_HOME/PlatformAgents/$toolName/$agentservice/$agentservice.service /etc/systemd/system
					sudo systemctl enable $agentservice
					sudo systemctl start $agentservice
					echo "Service installaton steps completed"
        			;;
			 esac
			 ;;
        centos)
               echo "Git Running on centso..."
               ;;
        *)
        	    echo "Please provide correct OS input"
esac