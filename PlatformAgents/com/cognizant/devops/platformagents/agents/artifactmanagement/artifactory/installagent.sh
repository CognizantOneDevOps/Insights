# Turn on a case-insensitive matching (-s set nocasematch)
opt=$1
echo "$opt"
case $opt in
        [lL][Ii][nN][uU][Xx])
                echo "ArtifactoryAgent Running on Linux..."
				sudo cp -xp InSightsArtifactoryAgent.sh  /etc/init.d/InSightsArtifactoryAgent
				sudo chmod +x /etc/init.d/InSightsArtifactoryAgent
				sudo chkconfig InSightsArtifactoryAgent on
				sudo service  InSightsArtifactoryAgent status
				sudo service InSightsArtifactoryAgent stop
				sudo service  InSightsArtifactoryAgent status
				sudo service InSightsArtifactoryAgent start
				sudo service  InSightsArtifactoryAgent status
				echo "Service installed.."
                ;;
        [uU][bB][uU][nN][tT][uU])
                echo "ArtifactoryAgent Running on Ubuntu..."
				sudo cp -xp InSightsArtifactoryAgent.service /etc/systemd/system
				sudo systemctl enable InSightsArtifactoryAgent
				sudo systemctl start InSightsArtifactoryAgent
				echo "Service Installed..."
                ;;
        centos)
                echo "ArtifactoryAgent Running on centso..."
                ;;
        *)
        	    echo "ArtifactoryAgent Please provide correct OS input"
esac