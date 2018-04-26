echo "#################### Installing Tomcat8 ####################"
cd /opt
sudo wget  http://platform.cogdevops.com/insights_install/release/latest/InSightsUI.zip -O InSightsUI.zip
sudo unzip InSightsUI.zip && sudo rm -rf InSightsUI.zip
sudo wget http://platform.cogdevops.com/insights_install/release/latest/PlatformService.war -O PlatformService.war
sudo wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/tomcat/apache-tomcat-8.5.27.tar.gz
sudo tar -zxvf apache-tomcat-8.5.27.tar.gz
sudo cp -R InSightsUI/app /opt/apache-tomcat-8.5.27/webapps
sudo rm -rf InSightsUI
sudo cp PlatformService.war /opt/apache-tomcat-8.5.27/webapps
sudo rm -rf PlatformService.war
cd apache-tomcat-8.5.27
sudo chmod -R 777 /opt/apache-tomcat-8.5.27
cd /etc/init.d/
sudo wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/initscripts/Tomcat8.sh
sudo mv Tomcat8.sh Tomcat8
sudo chmod +x Tomcat8
sudo chkconfig Tomcat8 on
sudo service Tomcat8 start
