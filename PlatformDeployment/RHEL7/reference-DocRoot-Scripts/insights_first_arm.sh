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
#Get INSIGHTS_HOME_ROOT_DIRECTORY and INSIGHTS_APP_ROOT_DIRECTORY
#echo "Enter INSIGHTS_HOME_ROOT_DIRECTORY root directory example: /usr/Insights/ or /apps/Insights/"
#read INSIGHTS_HOME_ROOT_DIRECTORY
INSIGHTS_HOME_ROOT_DIRECTORY="/usr/Insights"
#echo "Enter INSIGHTS_APP_ROOT_DIRECTORY root directory example: /opt/Insights/ or /apps/Insights/"
#read INSIGHTS_APP_ROOT_DIRECTORY
INSIGHTS_APP_ROOT_DIRECTORY="/mnt/Insights"

# If INSIGHTS_HOME_ROOT_DIRECTORY variable is empty
if [ -z "${INSIGHTS_HOME_ROOT_DIRECTORY}" ]; then
  echo INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY "is not valid"
  exit
fi

# If INSIGHTS_APP_ROOT_DIRECTORY variable is empty
if [ -z "${INSIGHTS_APP_ROOT_DIRECTORY}" ]; then
  echo INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY "is not valid"
  exit
fi

#To avoid using system directories for INSIGHTS_HOME_ROOT_DIRECTORY such as /usr/, /opt/
# Set '/' as the delimiter
IFS='/'
#Read the split words into an array based on '/' delimiter
read -a strarr <<< "$INSIGHTS_HOME_ROOT_DIRECTORY"
#To avoid using system directories. such as /usr/, /opt/
if [ "${#strarr[*]}" -le "2" ]; then
echo "INSIGHTS_HOME_ROOT_DIRECTORY" "$INSIGHTS_HOME_ROOT_DIRECTORY" "is not valid "
exit
fi

#To avoid using system directories for INSIGHTS_APP_ROOT_DIRECTORY such as /usr/, /opt/
#Read the split words into an array based on '/' delimiter
read -a strarr <<< "$INSIGHTS_APP_ROOT_DIRECTORY"
#To avoid using system directories. such as /usr/, /opt/
if [ "${#strarr[*]}" -le "2" ]; then
echo "INSIGHTS_APP_ROOT_DIRECTORY" "$INSIGHTS_APP_ROOT_DIRECTORY" "is not valid "
exit
fi
#To turn off IFS delimiter '/'.
IFS=''

# If INSIGHTS_HOME_ROOT_DIRECTORY directory doesn't exists
if [ ! -d "$INSIGHTS_HOME_ROOT_DIRECTORY" ]; then
  # Control will enter here if $DIRECTORY doesn't exist.
  mkdir -p $INSIGHTS_HOME_ROOT_DIRECTORY
  if [ $? -eq 0 ]; then
    echo INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY "Directory created Succesfully"
  else
    echo INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY "Directory creation Failed"
    exit
  fi
fi

# If INSIGHTS_APP_ROOT_DIRECTORY directory doesn't exists
if [ ! -d "$INSIGHTS_APP_ROOT_DIRECTORY" ]; then
  # Control will enter here if $DIRECTORY doesn't exist.
  mkdir -p $INSIGHTS_APP_ROOT_DIRECTORY
  if [ $? -eq 0 ]; then
    echo INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY "Directory created Succesfully"
  else
    echo INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY "Directory creation Failed"
    exit
  fi
fi

echo INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY
export INSIGHTS_HOME_ROOT_DIRECTORY=$INSIGHTS_HOME_ROOT_DIRECTORY
sudo echo INSIGHTS_HOME_ROOT_DIRECTORY=$INSIGHTS_HOME_ROOT_DIRECTORY | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_HOME_ROOT_DIRECTORY=$INSIGHTS_HOME_ROOT_DIRECTORY | sudo tee -a /etc/profile
sudo chmod -R 777 "$INSIGHTS_HOME_ROOT_DIRECTORY"

echo INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY
export INSIGHTS_APP_ROOT_DIRECTORY=$INSIGHTS_APP_ROOT_DIRECTORY
sudo echo INSIGHTS_APP_ROOT_DIRECTORY=$INSIGHTS_APP_ROOT_DIRECTORY | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_APP_ROOT_DIRECTORY=$INSIGHTS_APP_ROOT_DIRECTORY | sudo tee -a /etc/profile
sudo chmod -R 777 "$INSIGHTS_APP_ROOT_DIRECTORY"
source /etc/environment
source /etc/profile

# Set InSights Home
echo "#################### Setting up Insights Home ####################"
sudo yum install wget -y
sudo yum install unzip -y
sudo yum install dos2unix -y
cd $INSIGHTS_HOME_ROOT_DIRECTORY
sudo mkdir INSIGHTS_HOME
cd INSIGHTS_HOME
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/InSightsConfig.zip
sudo unzip InSightsConfig.zip && sudo rm -rf InSightsConfig.zip
sudo cp -R InSightsConfig/.InSights/ .
export INSIGHTS_HOME=`pwd`
sudo echo INSIGHTS_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_HOME=`pwd` | sudo tee -a /etc/profile
sudo chmod -R 777 $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/
source /etc/environment
source /etc/profile
dos2unix $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
grafanadbpass=$1
export myextip=$(wget -qO- icanhazip.com)
echo $myextip
sed -i -e "s|localhost:3000|${myextip}:3000|g" $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
sed -i -e "s|hostip|${myextip}|g" $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
#Postgres grafana user credentials using jq
jq --arg user "grafana" '.postgre.userName=$user' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
jq --arg pass ${grafanadbpass} '.postgre.password=$pass' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
#Add IP in trusted host
sed -i -e '/"trustedHosts":/ a "hostip",'  $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
sed -i -e "s|hostip|${myextip}|g"  $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
