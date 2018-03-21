# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Svn Running on Linux..."
				sudo cp -xp InSightsSvnAgent.sh  /etc/init.d/InSightsSvnAgent
				sudo chmod +x /etc/init.d/InSightsSvnAgent
				sudo chkconfig InSightsSvnAgent on
				sudo service  InSightsSvnAgent status
				sudo service  InSightsSvnAgent stop
				sudo service  InSightsSvnAgent status
				sudo service  InSightsSvnAgent start
				sudo service  InSightsSvnAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Svn Running on Ubuntu..."
				sudo cp -xp InSightsSvnAgent.service /etc/systemd/system
				sudo systemctl enable InSightsSvnAgent
				sudo systemctl start InSightsSvnAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Svn Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac