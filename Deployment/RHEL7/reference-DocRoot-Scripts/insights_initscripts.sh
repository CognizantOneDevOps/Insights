# This script copies all required scripts to /etc/init.d
echo "Copying the init.d scripts"
sudo mkdir initscripts
sudo wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/initscripts/initscripts.zip
sudo unzip initscripts.zip && cd initscripts chmod +x *.sh
sudo cp -rp * /etc/init.d
