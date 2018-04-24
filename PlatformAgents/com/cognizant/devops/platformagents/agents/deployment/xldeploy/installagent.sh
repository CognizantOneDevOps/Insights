# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "XLDeploy Running on Linux..."
				sudo cp -xp InSightsXLDeployAgent.sh  /etc/init.d/InSightsXLDeployAgent
				sudo chmod +x /etc/init.d/InSightsXLDeployAgent
				sudo chkconfig InSightsXLDeployAgent on
				sudo service  InSightsXLDeployAgent status
				sudo service  InSightsXLDeployAgent stop
				sudo service  InSightsXLDeployAgent status
				sudo service  InSightsXLDeployAgent start
				sudo service  InSightsXLDeployAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "XLDeploy Running on Ubuntu..."
				sudo cp -xp InSightsXLDeployAgent.service /etc/systemd/system
				sudo systemctl enable InSightsXLDeployAgent
				sudo systemctl start InSightsXLDeployAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "XLDeploy Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac