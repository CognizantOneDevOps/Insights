# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsSnowAgent stop
				sudo rm -R /etc/init.d/InSightsSnowAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "Snow Running on Linux..."
				sudo cp -xp InSightsSnowAgent.sh  /etc/init.d/InSightsSnowAgent
				sudo chmod +x /etc/init.d/InSightsSnowAgent
				sudo chkconfig InSightsSnowAgent on
				sudo service  InSightsSnowAgent status
				sudo service  InSightsSnowAgent stop
				sudo service  InSightsSnowAgent status
				sudo service  InSightsSnowAgent start
				sudo service  InSightsSnowAgent status
				
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	      case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsSnowAgent
				sudo rm -R /etc/systemd/system/InSightsSnowAgent.service
				echo "Service un-installation step completed"				
			    ;;
			 *)
                echo "Snow Running on Ubuntu..."
				sudo cp -xp InSightsSnowAgent.service /etc/systemd/system
				sudo systemctl enable InSightsSnowAgent
				sudo systemctl start InSightsSnowAgent
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        centos)
                echo "Snow Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac