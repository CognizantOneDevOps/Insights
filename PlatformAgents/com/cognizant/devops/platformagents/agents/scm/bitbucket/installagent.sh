# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "BitBucket Running on Linux..."
				sudo cp -xp InSightsBitBucketAgent.sh  /etc/init.d/InSightsBitBucketAgent
				sudo chmod +x /etc/init.d/InSightsBitBucketAgent
				sudo chkconfig InSightsBitBucketAgent on
				sudo service  InSightsBitBucketAgent status
				sudo service  InSightsBitBucketAgent stop
				sudo service  InSightsBitBucketAgent status
				sudo service  InSightsBitBucketAgent start
				sudo service  InSightsBitBucketAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "BitBucket Running on Ubuntu..."
				sudo cp -xp InSightsBitBucketAgent.service /etc/systemd/system
				sudo systemctl enable InSightsBitBucketAgent
				sudo systemctl start InSightsBitBucketAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "BitBucket Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac