# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "UCD Running on Linux..."
				sudo cp -xp InSightsUCDAgent.sh  /etc/init.d/InSightsUCDAgent
				sudo chmod +x /etc/init.d/InSightsUCDAgent
				sudo chkconfig InSightsUCDAgent on
				sudo service  InSightsUCDAgent status
				sudo service  InSightsUCDAgent stop
				sudo service  InSightsUCDAgent status
				sudo service  InSightsUCDAgent start
				sudo service  InSightsUCDAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "UCD Running on Ubuntu..."
				sudo cp -xp InSightsUCDAgent.service /etc/systemd/system
				sudo systemctl enable InSightsUCDAgent
				sudo systemctl start InSightsUCDAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "UCD Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac