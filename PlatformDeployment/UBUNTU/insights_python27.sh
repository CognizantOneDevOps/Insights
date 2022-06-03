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
# Python 2.7.11

echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/Ubuntu/packages/python/dependencies.zip
#sudo unzip dependencies.zip
#cd dependencies
sudo apt update
sudo apt install build-essential zlib1g-dev libncurses5-dev libgdbm-dev libnss3-dev libssl-dev libreadline-dev libffi-dev libsqlite3-dev wget libbz2-dev
wget https://www.python.org/ftp/python/2.7.10/Python-2.7.10.tgz
tar -xf Python-2.7.10.*.tgz
cd Python-2.7.10.*/
./configure --enable-optimizations
make -j $(nproc)
sudo make altinstall
python2.7 --version
sleep 5
