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
sudo apt-get install wget
sudo apt-get install unzip
sudo apt-get update
echo "Enter INSIGHTS_HOME_ROOT_DIRECTORY root directory example: /usr/Insights or /apps/Insights"
read INSIGHTS_HOME_ROOT_DIRECTORY
echo "Enter INSIGHTS_APP_ROOT_DIRECTORY root directory example: /opt/Insights or /apps/Insights"
read INSIGHTS_APP_ROOT_DIRECTORY
touch /etc/profile.d/insightsenvvar.sh

# Remove trailing \\ - INSIGHTS_HOME_ROOT_DIRECTORY and NSIGHTS_APP_ROOT_DIRECTORY
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
sudo echo "export" INSIGHTS_HOME_ROOT_DIRECTORY=$INSIGHTS_HOME_ROOT_DIRECTORY | sudo tee -a /etc/profile.d/insightsenvvar.sh
sudo chmod -R 777 "$INSIGHTS_HOME_ROOT_DIRECTORY"

echo INSIGHTS_APP_ROOT_DIRECTORY $INSIGHTS_APP_ROOT_DIRECTORY
export INSIGHTS_APP_ROOT_DIRECTORY=$INSIGHTS_APP_ROOT_DIRECTORY
sudo echo INSIGHTS_APP_ROOT_DIRECTORY=$INSIGHTS_APP_ROOT_DIRECTORY | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_APP_ROOT_DIRECTORY=$INSIGHTS_APP_ROOT_DIRECTORY | sudo tee -a /etc/profile.d/insightsenvvar.sh
sudo chmod -R 777 "$INSIGHTS_APP_ROOT_DIRECTORY"
. /etc/environment
. /etc/profile.d/insightsenvvar.sh

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
echo "update the server-config details in" $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json
. /etc/environment
. /etc/profile.d/insightsenvvar.sh
