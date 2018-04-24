# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "HpAlmAgent Running on Linux..."
				sudo cp -xp InSightsHpAgent.sh  /etc/init.d/InSightsHpAgent
				sudo chmod +x /etc/init.d/InSightsHpAgent
				sudo chkconfig InSightsHpAgent on
				sudo service  InSightsHpAgent status
				sudo service InSightsHpAgent stop
				sudo service  InSightsHpAgent status
				sudo service InSightsHpAgent start
				sudo service  InSightsHpAgent status
				echo "Service installed.."
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "HpAlmAgent Running on Ubuntu..."
				sudo cp -xp InSightsHpAgent.service /etc/systemd/system
				sudo systemctl enable InSightsHpAgent
				sudo systemctl start InSightsHpAgent
				echo "Service Installed..."
                ;;
        centos)
                echo "HpAlmAgent Running on centso..."
                ;;
        *)
        	    echo "HpAlmAgent Please provide correct OS input"
esac