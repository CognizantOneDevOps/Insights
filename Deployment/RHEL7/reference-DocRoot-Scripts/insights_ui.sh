echo "#################### Updating Insights WAR ####################"
cd /usr/share/tomcat/webapps
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/InSightsUI.zip -O InSightsUI.zip
sudo unzip InSightsUI.zip && sudo rm -rf InSightsUI.zip
sudo cp -R InSightsUI/app /usr/share/tomcat/webapps
sudo rm -rf InSightsUI
sudo systemctl stop tomcat
sleep 10
sudo systemctl start tomcat