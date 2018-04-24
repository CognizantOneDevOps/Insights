# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "TFS Running on Linux..."
				sudo cp -xp InSightsTFSAgent.sh  /etc/init.d/InSightsTFSAgent
				sudo chmod +x /etc/init.d/InSightsTFSAgent
				sudo chkconfig InSightsTFSAgent on
				sudo service  InSightsTFSAgent status
				sudo service  InSightsTFSAgent stop
				sudo service  InSightsTFSAgent status
				sudo service  InSightsTFSAgent start
				sudo service  InSightsTFSAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "TFS Running on Ubuntu..."
				sudo cp -xp InSightsTFSAgent.service /etc/systemd/system
				sudo systemctl enable InSightsTFSAgent
				sudo systemctl start InSightsTFSAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "TFS Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac