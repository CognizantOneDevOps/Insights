#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
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
