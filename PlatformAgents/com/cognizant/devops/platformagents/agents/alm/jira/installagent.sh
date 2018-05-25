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