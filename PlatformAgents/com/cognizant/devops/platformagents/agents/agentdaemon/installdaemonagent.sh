# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	            sudo service InSightsDaemonAgent stop
				sudo rm -R /etc/init.d/InSightsDaemonAgent
				echo "Service un-installation step completed"
		        ;;
		     *)
                echo "Daemon Running on Linux..."
				sudo cp -xp InSightsDaemonAgent.sh  /etc/init.d/InSightsDaemonAgent
				sudo chmod +x /etc/init.d/InSightsDaemonAgent
				sudo chkconfig InSightsDaemonAgent on
				sudo service  InSightsDaemonAgent status
				sudo service  InSightsDaemonAgent stop
				sudo service  InSightsDaemonAgent status
				sudo service  InSightsDaemonAgent start
				sudo service  InSightsDaemonAgent status
				echo "Service installaton steps completed"
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	       case $action in 
             [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	            sudo systemctl stop InSightsDaemonAgent
				sudo rm -R /etc/systemd/system/InSightsDaemonAgent.service
				echo "Service un-installation step completed"
		        ;;
		     *)
                echo "Daemon Running on Ubuntu..."
				sudo cp -xp InSightsDaemonAgent.service /etc/systemd/system
				sudo systemctl enable InSightsDaemonAgent
				sudo systemctl start InSightsDaemonAgent
				echo "Service installaton steps completed"
                ;;
		   esac
		   ;;
        centos)
                echo "Daemon Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
				;;
esac