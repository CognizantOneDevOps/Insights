#-------------------------------------------------------------------------------
# Copyright 2024 Cognizant Technology Solutions
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
#! /bin/sh
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	            sudo service InSightsReplicaDaemon stop
				sudo rm -R /etc/init.d/InSightsReplicaDaemon
				echo "Service un-installation step completed"
		        ;;
		     *)
                echo "Replica Daemon Running on Linux..."
				sudo cp -xp InSightsReplicaDaemon.sh  /etc/init.d/InSightsReplicaDaemon
				sudo chmod +x /etc/init.d/InSightsReplicaDaemon
				sudo chkconfig InSightsReplicaDaemon on
				sudo service  InSightsReplicaDaemon status
				sudo service  InSightsReplicaDaemon stop
				sudo service  InSightsReplicaDaemon status
				sudo service  InSightsReplicaDaemon start
				sudo service  InSightsReplicaDaemon status
				echo "Service installation steps completed"
                ;;
		  esac
		  ;;
		[dD][oO][cC][kK][eE][rR][aA][lL][pP][iI][nN][eE])
                case $action in 
                	[uU][nN][iI][nN][sS][tT][aA][lL][lL])
                		sh +x /etc/init.d/InSightsReplicaDaemon stop
						rm -R /etc/init.d/InSightsReplicaDaemon
						echo "Service un-installation step completed"
		            ;;
		         	*)
		         		echo "Replica Daemon Running on Alpine..."
						cp -p InSightsReplicaDaemon.sh  /etc/init.d/InSightsReplicaDaemon
						chmod +x /etc/init.d/InSightsReplicaDaemon
						sh +x /etc/init.d/InSightsReplicaDaemon status
						sh +x /etc/init.d/InSightsReplicaDaemon stop
						sh +x /etc/init.d/InSightsReplicaDaemon status
						sh +x /etc/init.d/InSightsReplicaDaemon start
						sh +x /etc/init.d/InSightsReplicaDaemon status
						
						echo "Service installaton steps completed"
		        	;;   
				esac
		        ;;
        [uU][bB][uU][nN][tT][uU])
	       case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	            sudo systemctl stop InSightsReplicaDaemon
				sudo rm -R /etc/systemd/system/InSightsReplicaDaemon.service
				echo "Service un-installation step completed"
		        ;;
		     *)
                echo "Replica Daemon Running on Ubuntu..."
				cp -xp InSightsReplicaDaemon.service /etc/systemd/system
				systemctl enable InSightsReplicaDaemon
				systemctl start InSightsReplicaDaemon
				echo "Service installation steps completed"
                ;;
		   esac
		   ;;
        centos)
                echo "Replica Daemon Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
				;;
esac
