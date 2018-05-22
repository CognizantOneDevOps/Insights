# Set InSights Home
echo "#################### Setting up Insights Home ####################"
cd /usr/
sudo mkdir INSIGHTS_HOME
cd INSIGHTS_HOME
sudo wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/InSightsConfig.zip
sudo unzip InSightsConfig.zip && sudo rm -rf InSightsConfig.zip
sudo cp -R InSightsConfig/.InSights/ .
export INSIGHTS_HOME=`pwd`
sudo echo INSIGHTS_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile

