# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Circleci Running on Linux..."
				sudo cp -xp InSightsCircleciAgent.sh  /etc/init.d/InSightsCircleciAgent
				sudo chmod +x /etc/init.d/InSightsCircleciAgent
				sudo chkconfig InSightsCircleciAgent on
				sudo service  InSightsCircleciAgent status
				sudo service  InSightsCircleciAgent stop
				sudo service  InSightsCircleciAgent status
				sudo service  InSightsCircleciAgent start
				sudo service  InSightsCircleciAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Circleci Running on Ubuntu..."
				sudo cp -xp InSightsCircleciAgent.service /etc/systemd/system
				sudo systemctl enable InSightsCircleciAgent
				sudo systemctl start InSightsCircleciAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Circleci Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac