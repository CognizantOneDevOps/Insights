# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
		    case $action in 
                	[uU][nN][iI][nN][sS][tT][aA][lL][lL])
					   sudo service InSightsSvnAgent stop
					   sudo rm -R /etc/init.d/InSightsSvnAgent 
					   echo "Service un-installation step completed"				
					   ;;
					
				    *)
                      echo "Svn Running on Linux..."
				      sudo cp -xp InSightsSvnAgent.sh  /etc/init.d/InSightsSvnAgent
				      sudo chmod +x /etc/init.d/InSightsSvnAgent
				      sudo chkconfig InSightsSvnAgent on
				      sudo service  InSightsSvnAgent status
				      sudo service  InSightsSvnAgent stop
				      sudo service  InSightsSvnAgent status
				      sudo service  InSightsSvnAgent start
				      sudo service  InSightsSvnAgent status
				      echo "Service installaton steps completed"
                      ;;
	        esac
			;;
				
        [uU][bB][uU][nN][tT][uU])
		    case $action in 
                	[uU][nN][iI][nN][sS][tT][aA][lL][lL])
					  sudo systemctl stop InSightsSvnAgent  
					  sudo rm -R /etc/systemd/system/InSightsSvnAgent.service
					  echo "Service un-installation step completed"	
                      ;;					  
					
				    *)
                      echo "Svn Running on Ubuntu..."
				      sudo cp -xp InSightsSvnAgent.service /etc/systemd/system
				      sudo systemctl enable InSightsSvnAgent
				      sudo systemctl start InSightsSvnAgent
				      echo "Service installaton steps completed"
                      ;;
			esac
			;;
        centos)
            echo "Svn Running on centso..."
            ;;
        *)
        	echo "Please provide correct OS input"
			;;
esac