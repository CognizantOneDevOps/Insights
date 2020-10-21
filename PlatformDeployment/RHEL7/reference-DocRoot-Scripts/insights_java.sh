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
#  Copyright 2017 Cognizant Technology Solutions
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License.  You may obtain a copy
#  of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
#  License for the specific language governing permissions and limitations under
#  the License.
# Install Java
echo "#################### Installing Java with Env Variable ####################"
cd /opt
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/java/jdklinux.tar.gz
sudo tar xzf jdklinux.tar.gz
export JAVA_HOME=/opt/jdklinux
echo JAVA_HOME=/opt/jdklinux  | sudo tee -a /etc/environment
echo "export" JAVA_HOME=/opt/jdklinux | sudo tee -a /etc/profile
export JRE_HOME=/opt/jdklinux/jre
echo JRE_HOME=/opt/jdklinux/jre | sudo tee -a /etc/environment
echo "export" JRE_HOME=/opt/jdklinux/jre | sudo tee -a /etc/profile
export PATH=$PATH:/opt/jdklinux/bin:/opt/jdklinux/jre/bin
echo PATH=$PATH:/opt/jdklinux/bin:/opt/jdklinux/jre/bin | sudo tee -a /etc/environment
sudo update-alternatives --install /usr/bin/java java /opt/jdklinux/bin/java 20000
sudo update-alternatives --install "/usr/bin/java" "java" "/opt/jdklinux/bin/java" 1
sudo update-alternatives --install "/usr/bin/javac" "javac" "/opt/jdklinux/bin/javac" 1
sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/opt/jdklinux/bin/javaws" 1
sudo update-alternatives --set java /opt/jdklinux/bin/java
sudo update-alternatives --set javac /opt/jdklinux/bin/javac
sudo update-alternatives --set javaws /opt/jdklinux/bin/javaws
source /etc/environment
source /etc/profile
