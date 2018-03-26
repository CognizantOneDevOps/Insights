# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
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
        [uU][bB][uU][nN][tT][uU])
                echo "Daemon Running on Ubuntu..."
				sudo cp -xp InSightsDaemonAgent.service /etc/systemd/system
				sudo systemctl enable InSightsDaemonAgent
				sudo systemctl start InSightsDaemonAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Daemon Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac