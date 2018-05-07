# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "TeamCity Running on Linux..."
				sudo cp -xp InSightsTeamCityAgent.sh  /etc/init.d/InSightsTeamCityAgent
				sudo chmod +x /etc/init.d/InSightsTeamCityAgent
				sudo chkconfig InSightsTeamCityAgent on
				sudo service  InSightsTeamCityAgent status
				sudo service  InSightsTeamCityAgent stop
				sudo service  InSightsTeamCityAgent status
				sudo service  InSightsTeamCityAgent start
				sudo service  InSightsTeamCityAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "TeamCity Running on Ubuntu..."
				sudo cp -xp InSightsTeamCityAgent.service /etc/systemd/system
				sudo systemctl enable InSightsTeamCityAgent
				sudo systemctl start InSightsTeamCityAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "TeamCity Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac