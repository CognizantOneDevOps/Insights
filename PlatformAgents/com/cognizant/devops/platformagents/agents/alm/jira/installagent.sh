# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "JiraAgent Running on Linux..."
				sudo cp -xp InSightsJiraAgent.sh  /etc/init.d/InSightsJiraAgent
				sudo chmod +x /etc/init.d/InSightsJiraAgent
				sudo chkconfig InSightsJiraAgent on
				sudo service  InSightsJiraAgent status
				sudo chkconfig InSightsJiraAgent stop
				sudo service  InSightsJiraAgent status
				sudo chkconfig InSightsJiraAgent start
				sudo service  InSightsJiraAgent status
				echo "Service installed.."
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "JiraAgent Running on Ubuntu..."
				sudo cp -xp InSightsJiraAgent.service /etc/systemd/system
				sudo systemctl enable InSightsJiraAgent
				sudo systemctl start InSightsJiraAgent
				echo "Service Installed..."
                ;;
        centos)
                echo "JiraAgent Running on centso..."
                ;;
        *)
        	    echo "JiraAgent Please provide correct OS input"
esac