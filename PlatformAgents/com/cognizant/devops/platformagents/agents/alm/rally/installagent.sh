# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "RallyAgent Running on Linux..."
				sudo cp -xp InSightsRallyAgent.sh  /etc/init.d/InSightsRallyAgent
				sudo chmod +x /etc/init.d/InSightsRallyAgent
				sudo chkconfig InSightsRallyAgent on
				sudo service  InSightsRallyAgent status
				sudo chkconfig InSightsRallyAgent stop
				sudo service  InSightsRallyAgent status
				sudo chkconfig InSightsRallyAgent start
				sudo service  InSightsRallyAgent status
				echo "Service installed.."
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "RallyAgent Running on Ubuntu..."
				sudo cp -xp InSightsRallyAgent.service /etc/systemd/system
				sudo systemctl enable InSightsRallyAgent
				sudo systemctl start InSightsRallyAgent
				echo "Service Installed..."
                ;;
        centos)
                echo "RallyAgent Running on centso..."
                ;;
        *)
        	    echo "RallyAgent Please provide correct OS input"
esac