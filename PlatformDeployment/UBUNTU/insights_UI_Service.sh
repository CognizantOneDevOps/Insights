#!/bin/bash
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
echo "#################### Setting up PlatformService ####################"
cd /opt
echo -n "Enter Release Version: " 
read releaseVersion
mkdir PlatformService
cd $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService/
sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/PlatformService-${releaseVersion}.jar -O PlatformService.jar
sleep 10
sudo nohup java -jar PlatformService.jar &
