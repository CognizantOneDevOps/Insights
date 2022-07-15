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
# Input Parameters prompt
# 1. INSIGHTS_HOME_ROOT_DIRECTORY
# 2. INSIGHTS_APP_ROOT_DIRECTORY
################################################################################
sudo yum update -y
sudo yum install wget -y
sudo yum install unzip -y
sudo yum install dos2unix -y
echo "Enter INSIGHTS_HOME_ROOT_DIRECTORY root directory example: /usr/Insights or /apps/Insights"
read INSIGHTS_HOME_ROOT_DIRECTORY
echo "Enter INSIGHTS_APP_ROOT_DIRECTORY root directory example: /opt/Insights or /apps/Insights"
read INSIGHTS_APP_ROOT_DIRECTORY

# Remove trailing \\ - INSIGHTS_HOME_ROOT_DIRECTORY and NSIGHTS_APP_ROOT_DIRECTORY
HDS_HOME=$INSIGHTS_HOME_ROOT_DIRECTORY
hds_length=${#HDS_HOME}
hds_last_char=${HDS_HOME:hds_length-2:2}

ADS_APP=$INSIGHTS_APP_ROOT_DIRECTORY
ads_length=${#ADS_APP}
ads_last_char=${ADS_APP:ads_length-2:2}
if [[ $hds_last_char == "//" && $ads_last_char == "//" ]]; then
  INSIGHTS_HOME_ROOT_DIRECTORY=${HDS_HOME:0:hds_length-2};
  INSIGHTS_APP_ROOT_DIRECTORY=${ADS_APP:0:ads_length-2};
elif [[ $hds_last_char == "//" ]]; then
  INSIGHTS_HOME_ROOT_DIRECTORY=${HDS_HOME:0:hds_length-2};
  echo "INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY"
elif [[ $ads_last_char == "//" ]]; then
  INSIGHTS_APP_ROOT_DIRECTORY=${ADS_APP:0:ads_length-2};
else
echo "No trailing double slashes found."
fi

#To avoid overwriting system directories/files for INSIGHTS_HOME_ROOT_DIRECTORY INSIGHTS_APP_ROOT_DIRECTORY such as /usr, /opt
# Set '/' as the delimiter
IFS='/'
#Read the split words into an array based on '/' delimiter
read -a strhome <<< "$INSIGHTS_HOME_ROOT_DIRECTORY"
read -a strapp <<< "$INSIGHTS_APP_ROOT_DIRECTORY"

if [[ -z "${INSIGHTS_HOME_ROOT_DIRECTORY}" ||  "${#strhome[*]}" -le "2" ]] && [[ -z "${INSIGHTS_APP_ROOT_DIRECTORY}" || "${#strapp[*]}" -le "2" ]]
then
  echo "INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY is not valid"
  echo "INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY is not valid "
  exit
elif [[ -z "${INSIGHTS_HOME_ROOT_DIRECTORY}" || "${#strhome[*]}" -le "2" ]]
then
  echo "INSIGHTS_HOME_ROOT_DIRECTORY $INSIGHTS_HOME_ROOT_DIRECTORY is not valid"
  exit
#To avoid using system directories. such as /usr, /opt
elif [[ -z "${INSIGHTS_APP_ROOT_DIRECTORY}" || "${#strapp[*]}" -le "2" ]]
then
 	echo "INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY is not valid "
 	exit
else
  echo "Valid Input Path"
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
cd $INSIGHTS_HOME_ROOT_DIRECTORY
sudo mkdir INSIGHTS_HOME
cd INSIGHTS_HOME
export INSIGHTS_HOME=`pwd`
sudo echo INSIGHTS_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_HOME=`pwd` | sudo tee -a /etc/profile
mkdir .InSights
cd .InSights
sudo wget  https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformService/src/main/resources/server-config-template.json -O server-config.json
sudo chmod -R 777 $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/
source /etc/environment
source /etc/profile
myextip=$(wget -qO- icanhazip.com)
echo $myextip
sed -i -e "s|localhost:3000|${myextip}:3000|g" $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
sed -i -e '/"trustedHosts":/ a "hostip",'  $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
sed -i -e "s|hostip|${myextip}|g" $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
#Install Json Processor JQ
wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64
chmod +x ./jq
sudo cp jq /usr/bin
sudo rm -rf jq
