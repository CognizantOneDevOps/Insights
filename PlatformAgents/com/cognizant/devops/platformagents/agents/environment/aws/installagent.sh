# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Aws Running on Linux..."
				sudo cp -xp InSightsAwsAgent.sh  /etc/init.d/InSightsAwsAgent
				sudo chmod +x /etc/init.d/InSightsAwsAgent
				sudo chkconfig InSightsAwsAgent on
				sudo service  InSightsAwsAgent status
				sudo service  InSightsAwsAgent stop
				sudo service  InSightsAwsAgent status
				sudo service  InSightsAwsAgent start
				sudo service  InSightsAwsAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Aws Running on Ubuntu..."
				sudo cp -xp InSightsAwsAgent.service /etc/systemd/system
				sudo systemctl enable InSightsAwsAgent
				sudo systemctl start InSightsAwsAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Aws Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac