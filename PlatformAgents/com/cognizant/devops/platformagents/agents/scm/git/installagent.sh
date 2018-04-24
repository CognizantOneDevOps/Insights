# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Git Running on Linux..."
				sudo cp -xp InSightsGitAgent.sh  /etc/init.d/InSightsGitAgent
				sudo chmod +x /etc/init.d/InSightsGitAgent
				sudo chkconfig InSightsGitAgent on
				sudo service  InSightsGitAgent status
				sudo service  InSightsGitAgent stop
				sudo service  InSightsGitAgent status
				sudo service  InSightsGitAgent start
				sudo service  InSightsGitAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Git Running on Ubuntu..."
				sudo cp -xp InSightsGitAgent.service /etc/systemd/system
				sudo systemctl enable InSightsGitAgent
				sudo systemctl start InSightsGitAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Git Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac