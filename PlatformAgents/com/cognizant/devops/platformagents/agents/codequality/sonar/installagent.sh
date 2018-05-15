# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsSonarAgent stop
				sudo rm -R /etc/init.d/InSightsSonarAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "SonarAgent Running on Linux..."
				sudo cp -xp InSightsSonarAgent.sh  /etc/init.d/InSightsSonarAgent
				sudo chmod +x /etc/init.d/InSightsSonarAgent
				sudo chkconfig InSightsSonarAgent on
				sudo service  InSightsSonarAgent status
				sudo service InSightsSonarAgent stop
				sudo service  InSightsSonarAgent status
				sudo service InSightsSonarAgent start
				sudo service  InSightsSonarAgent status
				echo "Service installed.."
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	      case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsSonarAgent
				sudo rm -R /etc/systemd/system/InSightsSonarAgent.service
				echo "Service un-installation step completed"				
			    ;;
			 *)
                echo "SonarAgent Running on Ubuntu..."
				sudo cp -xp InSightsSonarAgent.service /etc/systemd/system
				sudo systemctl enable InSightsSonarAgent
				sudo systemctl start InSightsSonarAgent
				echo "Service Installed..."
                ;;
		  esac
        centos)
                echo "SonarAgent Running on centso..."
                ;;
        *)
        	    echo "SonarAgent Please provide correct OS input"
esac