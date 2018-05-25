# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
	          	sudo service InSightsHpAgent stop
				sudo rm -R /etc/init.d/InSightsHpAgent
				echo "Service un-installation step completed"
		        ;;
		    *)
                echo "HpAlmAgent Running on Linux..."
				sudo cp -xp InSightsHpAgent.sh  /etc/init.d/InSightsHpAgent
				sudo chmod +x /etc/init.d/InSightsHpAgent
				sudo chkconfig InSightsHpAgent on
				sudo service  InSightsHpAgent status
				sudo service InSightsHpAgent stop
				sudo service  InSightsHpAgent status
				sudo service InSightsHpAgent start
				sudo service  InSightsHpAgent status
				echo "Service installed.."
                ;;
		  esac
		  ;;
        [uU][bB][uU][nN][tT][uU])
	        case $action in 
                [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
					sudo systemctl stop InSightsHpAgent
					sudo rm -R /etc/systemd/system/InSightsHpAgent.service
					echo "Service un-installation step completed"				
			        ;;
				*)
                	echo "HpAlmAgent Running on Ubuntu..."
					sudo cp -xp InSightsHpAgent.service /etc/systemd/system
					sudo systemctl enable InSightsHpAgent
					sudo systemctl start InSightsHpAgent
					echo "Service Installed..."
                	;;
		    esac
			;;
        centos)
                echo "HpAlmAgent Running on centso..."
                ;;
        *)
        	    echo "HpAlmAgent Please provide correct OS input"
esac