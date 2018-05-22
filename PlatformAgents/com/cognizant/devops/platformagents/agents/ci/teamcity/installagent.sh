# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsTeamCityAgent stop
				sudo rm -R /etc/init.d/InSightsTeamCityAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "TeamCity Running on Linux..."
				sudo cp -xp InSightsTeamCityAgent.sh  /etc/init.d/InSightsTeamCityAgent
				sudo chmod +x /etc/init.d/InSightsTeamCityAgent
				sudo chkconfig InSightsTeamCityAgent on
				sudo service  InSightsTeamCityAgent status
				sudo service  InSightsTeamCityAgent stop
				sudo service  InSightsTeamCityAgent status
				sudo service  InSightsTeamCityAgent start
				sudo service  InSightsTeamCityAgent status
				
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	      case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsTeamCityAgent
				sudo rm -R /etc/systemd/system/InSightsTeamCityAgent.service
				echo "Service un-installation step completed"				
			    ;;
			 *)
                echo "TeamCity Running on Ubuntu..."
				sudo cp -xp InSightsTeamCityAgent.service /etc/systemd/system
				sudo systemctl enable InSightsTeamCityAgent
				sudo systemctl start InSightsTeamCityAgent
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        centos)
                echo "TeamCity Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac