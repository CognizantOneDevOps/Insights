# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "BitBucket Running on Linux..."
				sudo cp -xp InSightsBitBucketAgentAllbranch.sh  /etc/init.d/InSightsBitBucketAgentAllbranch
				sudo chmod +x /etc/init.d/InSightsBitBucketAgentAllbranch
				sudo chkconfig InSightsBitBucketAgentAllbranch on
				sudo service  InSightsBitBucketAgentAllbranch status
				sudo service  InSightsBitBucketAgentAllbranch stop
				sudo service  InSightsBitBucketAgentAllbranch status
				sudo service  InSightsBitBucketAgentAllbranch start
				sudo service  InSightsBitBucketAgentAllbranch status
				
				echo "Service installaton steps completed"
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "BitBucket Running on Ubuntu..."
				sudo cp -xp InSightsBitBucketAgentAllbranch.service /etc/systemd/system
				sudo systemctl enable InSightsBitBucketAgentAllbranch
				sudo systemctl start InSightsBitBucketAgentAllbranch
				echo "Service installaton steps completed"
                ;;
        centos)
                echo "BitBucket Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac