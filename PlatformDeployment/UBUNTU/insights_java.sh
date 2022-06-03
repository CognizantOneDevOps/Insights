#!/bin/bash
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
touch /etc/profile.d/insightsenvvar.sh
sudo wget https://download.java.net/openjdk/jdk11/ri/openjdk-11+28_linux-x64_bin.tar.gz
sudo tar xvf openjdk*.tar.gz
mv jdk-11 jdklinux
export JAVA_HOME=/opt/jdklinux/bin
echo JAVA_HOME=/opt/jdklinux/bin  | sudo tee -a /etc/environment
echo "export" JAVA_HOME=/opt/jdklinux/bin | sudo tee -a /etc/profile.d/insightsenvvar.sh

export PATH=$PATH:/opt/jdklinux/bin
echo PATH=$PATH:/opt/jdklinux/bin | sudo tee -a /etc/environment

sudo update-alternatives --install /usr/bin/java java /opt/jdklinux/bin/java 20000
sudo update-alternatives --install "/usr/bin/java" "java" "/opt/jdklinux/bin/java" 1
sudo update-alternatives --install "/usr/bin/javac" "javac" "/opt/jdklinux/bin/javac" 1
sudo update-alternatives --set java /opt/jdklinux/bin/java
sudo update-alternatives --set javac /opt/jdklinux/bin/javac

. /etc/environment
. /etc/profile.d/insightsenvvar.sh
