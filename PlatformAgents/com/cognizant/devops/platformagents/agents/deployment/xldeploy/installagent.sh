# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsXLDeployAgent stop
				sudo rm -R /etc/init.d/InSightsXLDeployAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "XLDeploy Running on Linux..."
				sudo cp -xp InSightsXLDeployAgent.sh  /etc/init.d/InSightsXLDeployAgent
				sudo chmod +x /etc/init.d/InSightsXLDeployAgent
				sudo chkconfig InSightsXLDeployAgent on
				sudo service  InSightsXLDeployAgent status
				sudo service  InSightsXLDeployAgent stop
				sudo service  InSightsXLDeployAgent status
				sudo service  InSightsXLDeployAgent start
				sudo service  InSightsXLDeployAgent status
				
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	      case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsXLDeployAgent
				sudo rm -R /etc/systemd/system/InSightsXLDeployAgent.service
				echo "Service un-installation step completed"				
			    ;;
			 *)
                echo "XLDeploy Running on Ubuntu..."
				sudo cp -xp InSightsXLDeployAgent.service /etc/systemd/system
				sudo systemctl enable InSightsXLDeployAgent
				sudo systemctl start InSightsXLDeployAgent
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        centos)
                echo "XLDeploy Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac