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
cd /opt/
wget  https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/java/jdk-8u151-linux-x64.tar.gz
tar xzf jdk-8u151-linux-x64.tar.gz
export JAVA_HOME=/opt/jdk1.8.0_151
echo JAVA_HOME=/opt/jdk1.8.0_151  | tee -a /etc/environment
echo "export" JAVA_HOME=/opt/jdk1.8.0_151 | tee -a /etc/profile
export JRE_HOME=/opt/jdk1.8.0_151/jre
echo JRE_HOME=/opt/jdk1.8.0_151/jre | tee -a /etc/environment
echo "export" JRE_HOME=/opt/jdk1.8.0_151/jre | tee -a /etc/profile
export PATH=$PATH:/opt/jdk1.8.0_151/bin:/opt/jdk1.8.0_151/jre/bin
echo PATH=$PATH:/opt/jdk1.8.0_151/bin:/opt/jdk1.8.0_151/jre/bin | tee -a /etc/environment
update-alternatives --install /usr/bin/java java /opt/jdk1.8.0_151/bin/java 20000
sudo -E source /etc/environment
sudo -E source /etc/profile

