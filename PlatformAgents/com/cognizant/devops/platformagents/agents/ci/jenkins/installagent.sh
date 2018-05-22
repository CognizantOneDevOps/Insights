# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Jenkins Running on Linux..."
				sudo cp -xp InSightsJenkinsAgent.sh  /etc/init.d/InSightsJenkinsAgent
				sudo chmod +x /etc/init.d/InSightsJenkinsAgent
				sudo chkconfig InSightsJenkinsAgent on
				sudo service  InSightsJenkinsAgent status
				sudo service  InSightsJenkinsAgent stop
				sudo service  InSightsJenkinsAgent status
				sudo service  InSightsJenkinsAgent start
				sudo service  InSightsJenkinsAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Jenkins Running on Ubuntu..."
				sudo cp -xp InSightsJenkinsAgent.service /etc/systemd/system
				sudo systemctl enable InSightsJenkinsAgent
				sudo systemctl start InSightsJenkinsAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Jenkins Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac