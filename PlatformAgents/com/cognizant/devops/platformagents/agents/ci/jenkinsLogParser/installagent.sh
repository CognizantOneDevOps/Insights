# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "JenkinsLogParser Running on Linux..."
				sudo cp -xp InSightsJenkinsLogParserAgent.sh  /etc/init.d/InSightsJenkinsLogParserAgent
				sudo chmod +x /etc/init.d/InSightsJenkinsLogParserAgent
				sudo chkconfig InSightsJenkinsLogParserAgent on
				sudo service  InSightsJenkinsLogParserAgent status
				sudo service  InSightsJenkinsLogParserAgent stop
				sudo service  InSightsJenkinsLogParserAgent status
				sudo service  InSightsJenkinsLogParserAgent start
				sudo service  InSightsJenkinsLogParserAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "JenkinsLogParser Running on Ubuntu..."
				sudo cp -xp InSightsJenkinsLogParserAgent.service /etc/systemd/system
				sudo systemctl enable InSightsJenkinsLogParserAgent
				sudo systemctl start InSightsJenkinsLogParserAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "JenkinsLogParser Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac