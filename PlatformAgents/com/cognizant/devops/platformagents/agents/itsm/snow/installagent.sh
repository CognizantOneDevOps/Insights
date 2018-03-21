# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Snow Running on Linux..."
				sudo cp -xp InSightsSnowAgent.sh  /etc/init.d/InSightsSnowAgent
				sudo chmod +x /etc/init.d/InSightsSnowAgent
				sudo chkconfig InSightsSnowAgent on
				sudo service  InSightsSnowAgent status
				sudo service  InSightsSnowAgent stop
				sudo service  InSightsSnowAgent status
				sudo service  InSightsSnowAgent start
				sudo service  InSightsSnowAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Snow Running on Ubuntu..."
				sudo cp -xp InSightsSnowAgent.service /etc/systemd/system
				sudo systemctl enable InSightsSnowAgent
				sudo systemctl start InSightsSnowAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Snow Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac