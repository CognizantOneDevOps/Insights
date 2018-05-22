# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "CITFS Running on Linux..."
				sudo cp -xp InSightsCITFSAgent.sh  /etc/init.d/InSightsCITFSAgent
				sudo chmod +x /etc/init.d/InSightsCITFSAgent
				sudo chkconfig InSightsCITFSAgent on
				sudo service  InSightsCITFSAgent status
				sudo service  InSightsCITFSAgent stop
				sudo service  InSightsCITFSAgent status
				sudo service  InSightsCITFSAgent start
				sudo service  InSightsCITFSAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "CITFS Running on Ubuntu..."
				sudo cp -xp InSightsCITFSAgent.service /etc/systemd/system
				sudo systemctl enable InSightsCITFSAgent
				sudo systemctl start InSightsCITFSAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "CITFS Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac