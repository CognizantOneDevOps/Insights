# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "Concourse Running on Linux..."
				sudo cp -xp InSightsConcourseAgent.sh  /etc/init.d/InSightsConcourseAgent
				sudo chmod +x /etc/init.d/InSightsConcourseAgent
				sudo chkconfig InSightsConcourseAgent on
				sudo service  InSightsConcourseAgent status
				sudo service  InSightsConcourseAgent stop
				sudo service  InSightsConcourseAgent status
				sudo service  InSightsConcourseAgent start
				sudo service  InSightsConcourseAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "Concourse Running on Ubuntu..."
				sudo cp -xp InSightsConcourseAgent.service /etc/systemd/system
				sudo systemctl enable InSightsConcourseAgent
				sudo systemctl start InSightsConcourseAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "Concourse Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac