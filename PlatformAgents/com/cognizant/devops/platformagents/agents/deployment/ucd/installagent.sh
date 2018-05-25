# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsUCDAgent stop
				sudo rm -R /etc/init.d/InSightsUCDAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "UCD Running on Linux..."
				sudo cp -xp InSightsUCDAgent.sh  /etc/init.d/InSightsUCDAgent
				sudo chmod +x /etc/init.d/InSightsUCDAgent
				sudo chkconfig InSightsUCDAgent on
				sudo service  InSightsUCDAgent status
				sudo service  InSightsUCDAgent stop
				sudo service  InSightsUCDAgent status
				sudo service  InSightsUCDAgent start
				sudo service  InSightsUCDAgent status
				
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	       case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsUCDAgent
				sudo rm -R /etc/systemd/system/InSightsUCDAgent.service
				echo "Service un-installation step completed"				
			    ;;
			 *)
                echo "UCD Running on Ubuntu..."
				sudo cp -xp InSightsUCDAgent.service /etc/systemd/system
				sudo systemctl enable InSightsUCDAgent
				sudo systemctl start InSightsUCDAgent
				echo "Service installaton steps completed"
                ;;
		   esac
		   ;;
        centos)
                echo "UCD Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac