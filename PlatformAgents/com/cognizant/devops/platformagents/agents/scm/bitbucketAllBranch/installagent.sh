# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
action=$2
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
          case $action in 
            [uU][nN][iI][nN][sS][tT][aA][lL][lL])
                sudo service InSightsBitBucketAgentAllbranch stop
			    sudo rm -R /etc/init.d/InSightsBitBucketAgentAllbranch 
				echo "Service un-installation step completed"
	            ;;
            *)
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
		  esac	
	      ;; 
    
        [uU][bB][uU][nN][tT][uU])
	      case $action in 
	        [uU][nN][iI][nN][sS][tT][aA][lL][lL])
                sudo systemctl stop InSightsBitBucketAgentAllbranch
				sudo rm -R /etc/systemd/system/InSightsBitBucketAgentAllbranch.service 
				echo "Service un-installation step completed"
	            ;;
            *)

                echo "BitBucket Running on Ubuntu..."
				sudo cp -xp InSightsBitBucketAgentAllbranch.service /etc/systemd/system
				sudo systemctl enable InSightsBitBucketAgentAllbranch
				sudo systemctl start InSightsBitBucketAgentAllbranch
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