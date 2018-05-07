# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "BitBucketCloud Running on Linux..."
				sudo cp -xp InSightsBitBucketCloudAgent.sh  /etc/init.d/InSightsBitBucketCloudAgent
				sudo chmod +x /etc/init.d/InSightsBitBucketCloudAgent
				sudo chkconfig InSightsBitBucketCloudAgent on
				sudo service  InSightsBitBucketCloudAgent status
				sudo service  InSightsBitBucketCloudAgent stop
				sudo service  InSightsBitBucketCloudAgent status
				sudo service  InSightsBitBucketCloudAgent start
				sudo service  InSightsBitBucketCloudAgent status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "BitBucketCloud Running on Ubuntu..."
				sudo cp -xp InSightsBitBucketCloudAgent.service /etc/systemd/system
				sudo systemctl enable InSightsBitBucketCloudAgent
				sudo systemctl start InSightsBitBucketCloudAgent
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "BitBucketCloud Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac