# install tomcat 7
echo "#################### Installing Tomcat7 ####################"
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/InSightsUI.zip -O InSightsUI.zip
sudo unzip InSightsUI.zip && sudo rm -rf InSightsUI.zip
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/PlatformService.war -O PlatformService.war
sudo yum install tomcat -y
sudo yum install tomcat-webapps tomcat-admin-webapps -y
sudo cp -R InSightsUI/app /usr/share/tomcat/webapps
sudo rm -rf InSightsUI
sudo cp PlatformService.war /usr/share/tomcat/webapps
sudo rm -rf PlatformService.war
source /etc/environment && source /etc/profile
sudo chmod 777 /usr/share/tomcat/conf/tomcat.conf
sudo echo INSIGHTS_HOME=$INSIGHTS_HOME >> /usr/share/tomcat/conf/tomcat.conf
sleep 10
sudo systemctl enable tomcat && sudo systemctl start tomcat

