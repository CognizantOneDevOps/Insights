# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "SonarAgent Running on Linux..."
				sudo cp -xp InSightsSonarAgent.sh  /etc/init.d/InSightsSonarAgent
				sudo chmod +x /etc/init.d/InSightsSonarAgent
				sudo chkconfig InSightsSonarAgent on
				sudo service  InSightsSonarAgent status
				sudo chkconfig InSightsSonarAgent stop
				sudo service  InSightsSonarAgent status
				sudo chkconfig InSightsSonarAgent start
				sudo service  InSightsSonarAgent status
				echo "Service installed.."
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "SonarAgent Running on Ubuntu..."
				sudo cp -xp InSightsSonarAgent.service /etc/systemd/system
				sudo systemctl enable InSightsSonarAgent
				sudo systemctl start InSightsSonarAgent
				echo "Service Installed..."
                ;;
        centos)
                echo "SonarAgent Running on centso..."
                ;;
        *)
        	    echo "SonarAgent Please provide correct OS input"
esac