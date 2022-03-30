#-------------------------------------------------------------------------------
# Copyright 2022 Cognizant Technology Solutions
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
# Install Java
echo "#################### Installing Java with Env Variable ####################"
yum update
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL8/java/jdklinux.tar.gz
sudo tar xzf jdklinux.tar.gz
mv jdk-11.0.2 jdklinux
export JAVA_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin
echo JAVA_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin  | sudo tee -a /etc/environment
echo "export" JAVA_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin | sudo tee -a /etc/profile

export PATH=$PATH:$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin
echo PATH=$PATH:$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin | sudo tee -a /etc/environment

sudo update-alternatives --install /usr/bin/java java $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/java 20000
sudo update-alternatives --install "/usr/bin/java" "java" "$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/java" 1
sudo update-alternatives --install "/usr/bin/javac" "javac" "$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/javac" 1
sudo update-alternatives --set java $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/java
sudo update-alternatives --set javac $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/javac
source /etc/environment
source /etc/profile
