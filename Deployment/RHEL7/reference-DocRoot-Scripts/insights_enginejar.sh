# get insights engine jar
echo "#################### Getting Insights Engine Jar ####################"
sudo mkdir /opt/insightsengine
cd /opt/insightsengine
export INSIGHTS_ENGINE=`pwd`
sudo echo INSIGHTS_ENGINE=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_ENGINE=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/PlatformEngine.jar -O PlatformEngine.jar
