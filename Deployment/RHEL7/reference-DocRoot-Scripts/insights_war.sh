echo "#################### Updating Insights WAR ####################"
cd /usr/share/tomcat/webapps
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/PlatformService.war -O PlatformService.war
sudo systemctl stop tomcat
sleep 10
sudo systemctl start tomcat