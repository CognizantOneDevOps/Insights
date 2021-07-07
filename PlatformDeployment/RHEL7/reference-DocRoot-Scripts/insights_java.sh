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
# Install Java
echo "#################### Installing Java with Env Variable ####################"
yum update
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/java/jdklinux.tar.gz
sudo tar xzf jdklinux.tar.gz
export JAVA_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux
echo JAVA_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux  | sudo tee -a /etc/environment
echo "export" JAVA_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux | sudo tee -a /etc/profile
export JRE_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/jre
echo JRE_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/jre | sudo tee -a /etc/environment
echo "export" JRE_HOME=$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/jre | sudo tee -a /etc/profile
export PATH=$PATH:$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin:$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/jre/bin
echo PATH=$PATH:$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin:$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/jre/bin | sudo tee -a /etc/environment
sudo update-alternatives --install /usr/bin/java java $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/java 20000
sudo update-alternatives --install "/usr/bin/java" "java" "$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/java" 1
sudo update-alternatives --install "/usr/bin/javac" "javac" "$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/javac" 1
sudo update-alternatives --install "/usr/bin/javaws" "javaws" "$INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/javaws" 1
sudo update-alternatives --set java $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/java
sudo update-alternatives --set javac $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/javac
sudo update-alternatives --set javaws $INSIGHTS_APP_ROOT_DIRECTORY/jdklinux/bin/javaws
source /etc/environment
source /etc/profile
