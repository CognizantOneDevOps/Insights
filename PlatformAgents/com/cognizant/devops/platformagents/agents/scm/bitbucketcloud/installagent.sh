# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
           case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
                sudo service InSightsBitBucketCloudAgent stop
			    sudo rm -R /etc/init.d/InSightsBitBucketCloudAgent 
				echo "Service un-installation step completed"
	            ;;
            *)
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
		   esac
		   ;;
        [uU][bB][uU][nN][tT][uU])
	       case $action in 
	        [uU][nN][iI][nN][sS][tT][aA][lL][lL])
                sudo systemctl stop InSightsBitBucketCloudAgent
				sudo rm -R /etc/systemd/system/InSightsBitBucketCloudAgent.service 
				echo "Service un-installation step completed"
	            ;;
            *)

                echo "BitBucketCloud Running on Ubuntu..."
				sudo cp -xp InSightsBitBucketCloudAgent.service /etc/systemd/system
				sudo systemctl enable InSightsBitBucketCloudAgent
				sudo systemctl start InSightsBitBucketCloudAgent
				echo "Service installaton steps completed"
                ;;
		   esac
		   ;;
        centos)
                echo "BitBucketCloud Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
esac