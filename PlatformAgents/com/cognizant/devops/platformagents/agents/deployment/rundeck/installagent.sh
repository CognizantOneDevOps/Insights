# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Rundeck Running on Linux..."
				sudo cp -xp InSightsRundeckAgent.sh  /etc/init.d/InSightsRundeckAgent
				sudo chmod +x /etc/init.d/InSightsRundeckAgent
				sudo chkconfig InSightsRundeckAgent on
				sudo service  InSightsRundeckAgent status
				sudo service  InSightsRundeckAgent stop
				sudo service  InSightsRundeckAgent status
				sudo service  InSightsRundeckAgent start
				sudo service  InSightsRundeckAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Rundeck Running on Ubuntu..."
				sudo cp -xp InSightsRundeckAgent.service /etc/systemd/system
				sudo systemctl enable InSightsRundeckAgent
				sudo systemctl start InSightsRundeckAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Rundeck Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac