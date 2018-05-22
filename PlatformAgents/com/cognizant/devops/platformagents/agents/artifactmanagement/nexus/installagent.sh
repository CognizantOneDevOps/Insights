# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "NexusAgent Running on Linux..."
				sudo cp -xp InSightsNexusAgent.sh  /etc/init.d/InSightsNexusAgent
				sudo chmod +x /etc/init.d/InSightsNexusAgent
				sudo chkconfig InSightsNexusAgent on
				sudo service  InSightsNexusAgent status
				sudo service InSightsNexusAgent stop
				sudo service  InSightsNexusAgent status
				sudo service InSightsNexusAgent start
				sudo service  InSightsNexusAgent status
				echo "Service installed.."
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "NexusAgent Running on Ubuntu..."
				sudo cp -xp InSightsNexusAgent.service /etc/systemd/system
				sudo systemctl enable InSightsNexusAgent
				sudo systemctl start InSightsNexusAgent
				echo "Service Installed..."
                ;;
        centos)
                echo "NexusAgent Running on centso..."
                ;;
        *)
        	    echo "NexusAgent Please provide correct OS input"
esac