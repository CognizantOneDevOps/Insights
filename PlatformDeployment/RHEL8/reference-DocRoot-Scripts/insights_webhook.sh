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
# get insights webhook jar
# Input parameters will be prompted to set in webhook_subscriber.properties
# 1. mqHost
# 2. mqUser
# 3. mqPassword
# 4. mqExchangeName
################################################################################

echo "#################### Getting Insights WebHook Jar ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir insightsWebHook
cd $INSIGHTS_APP_ROOT_DIRECTORY/insightsWebHook
export INSIGHTS_WEBHOOK=`pwd`
sudo echo INSIGHTS_WEBHOOK=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_WEBHOOK=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
echo -n "Enter Release Version: " 
read releaseVersion
sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/PlatformInsightsWebHook-${releaseVersion}.jar -O PlatformInsightsWebHook.jar
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformInsightsWebHook/src/main/resources/webhook_subscriber.properties -O webhook_subscriber.properties

sudo chmod a+w $INSIGHTS_APP_ROOT_DIRECTORY/insightsWebHook/webhook_subscriber.properties
sudo yum install dos2unix
sudo dos2unix $INSIGHTS_APP_ROOT_DIRECTORY/insightsWebHook/webhook_subscriber.properties

echo "To set mqHost"
echo -n "mqHost: "
read mqHost

echo "To set mqUser"
echo -n "mqUser: "
read mqUser

echo "To set mqPassword read silent"
echo -n "mqPassword: "
read -s mqPassword

echo "To set mqExchangeName"
echo -n "mqExchangeName: "
read mqExchangeName

setProperty(){
  awk -v pat="^$1=" -v value="$1=$2" '{ if ($0 ~ pat) print value; else print $0; }' $3 > $3.tmp
  mv $3.tmp $3
}

setProperty app.mqHost $mqHost webhook_subscriber.properties
setProperty app.mqUser $mqUser webhook_subscriber.properties
setProperty app.mqPassword $mqPassword webhook_subscriber.properties
setProperty app.mqExchangeName $mqExchangeName webhook_subscriber.properties

sleep 2
sudo nohup java -jar PlatformInsightsWebHook.jar > /dev/null 2>&1 &
sleep 10
sudo chmod -R 777 $INSIGHTS_APP_ROOT_DIRECTORY/insightsWebHook
sudo cp -r $INSIGHTS_APP_ROOT_DIRECTORY/insightsWebHook/webhook_subscriber.properties $INSIGHTS_HOME/.InSights/
cd /etc/init.d/
sudo wget  https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/InSightsWebHook.sh
sudo mv InSightsWebHook.sh InSightsWebHook
sudo chmod +x InSightsWebHook
sudo chkconfig InSightsWebHook on
sudo yum install dos2unix
sudo dos2unix /etc/init.d/InSightsWebHook
sleep 10
sudo service InSightsWebHook stop
sudo service InSightsWebHook start
