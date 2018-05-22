# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsRallyAgent stop
				sudo rm -R /etc/init.d/InSightsRallyAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "RallyAgent Running on Linux..."
				sudo cp -xp InSightsRallyAgent.sh  /etc/init.d/InSightsRallyAgent
				sudo chmod +x /etc/init.d/InSightsRallyAgent
				sudo chkconfig InSightsRallyAgent on
				sudo service  InSightsRallyAgent status
				sudo service InSightsRallyAgent stop
				sudo service  InSightsRallyAgent status
				sudo service InSightsRallyAgent start
				sudo service  InSightsRallyAgent status
				echo "Service installed.."
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
		   case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsRallyAgent
				sudo rm -R /etc/systemd/system/InSightsRallyAgent.service
				echo "Service un-installation step completed"				
			    ;;
					
			 *)
                echo "RallyAgent Running on Ubuntu..."
				sudo cp -xp InSightsRallyAgent.service /etc/systemd/system
				sudo systemctl enable InSightsRallyAgent
				sudo systemctl start InSightsRallyAgent
				echo "Service Installed..."
                ;;
		   esac
		   ;;
        centos)
                echo "RallyAgent Running on centso..."
                ;;
        *)
        	    echo "RallyAgent Please provide correct OS input"
esac