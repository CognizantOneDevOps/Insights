# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
             case $action in 
                [uU][nN][iI][nN][sS][tT][aA][lL][lL])
			          sudo service InSightsBitBucketAgent stop
					  sudo rm -R /etc/init.d/InSightsBitBucketAgent 
					  echo "Service un-installation step completed"
                      ;;
                *)  
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
             esac 
	         ;;
	      
        [uU][bB][uU][nN][tT][uU])
	         case $action in 
                [uU][nN][iI][nN][sS][tT][aA][lL][lL]) 
					sudo systemctl stop InSightsBitBucketAgent
					sudo rm -R /etc/systemd/system/InSightsBitBucketAgent.service
					echo "Service un-installation step completed"				
			        ;;
					
				*)
                   echo "BitBucket Running on Ubuntu..."
				   sudo cp -xp InSightsBitBucketAgent.service /etc/systemd/system
				   sudo systemctl enable InSightsBitBucketAgent
				   sudo systemctl start InSightsBitBucketAgent
				   echo "Service installaton steps completed"
                   ;;
		     esac
		     ;;

        centos)
                echo "BitBucket Running on centso..."
                ;;
        *)
        	    echo "Please provide correct OS input"
		        ;;
esac