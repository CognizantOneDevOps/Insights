# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Running on Linux..."
				sudo cp -xp InSightsGitAgent.sh  /etc/init.d/InSightsGitAgent
				sudo chmod +x /etc/init.d/InSightsGitAgent
				sudo chkconfig InSightsGitAgent on
				sudo service  InSightsGitAgent status
				echo "Service installed and started"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Running on Ubuntu..."
				sudo cp -xp InSightsGitAgent.service /etc/systemd/system
				sudo systemctl enable InSightsGitAgent
				sudo systemctl start InSightsGitAgent
				echo "Service Installed and started"
                ;;
        centos)
                echo "Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac