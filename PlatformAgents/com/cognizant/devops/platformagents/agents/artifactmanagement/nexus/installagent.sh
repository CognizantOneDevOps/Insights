# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsNexusAgent stop
				sudo rm -R /etc/init.d/InSightsNexusAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "NexusAgent Running on Linux..."
				sudo cp -xp InSightsNexusAgent.sh  /etc/init.d/InSightsNexusAgent
				sudo chmod +x /etc/init.d/InSightsNexusAgent
				sudo chkconfig InSightsNexusAgent on
				sudo service  InSightsNexusAgent status
				sudo service InSightsNexusAgent stop
				sudo service  InSightsNexusAgent status
				sudo service InSightsNexusAgent start
				sudo service  InSightsNexusAgent status
				echo "Service installed.."
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	       case $action in 
              [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
				sudo systemctl stop InSightsNexusAgent
				sudo rm -R /etc/systemd/system/InSightsNexusAgent.service
				echo "Service un-installation step completed"				
			    ;;
			  *)
                echo "NexusAgent Running on Ubuntu..."
				sudo cp -xp InSightsNexusAgent.service /etc/systemd/system
				sudo systemctl enable InSightsNexusAgent
				sudo systemctl start InSightsNexusAgent
				echo "Service Installed..."
                ;;
		   esac
		   ;;
        centos)
                echo "NexusAgent Running on centso..."
                ;;
        *)
        	    echo "NexusAgent Please provide correct OS input"
esac