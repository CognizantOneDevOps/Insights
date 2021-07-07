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
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo cp -R $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Python/python2.7.11_dependencies ./
cd python2.7.11_dependencies
sudo rpm -Uvh mpfr-3.1.1-4.el7.x86_64.rpm
sudo rpm -Uvh libmpc-1.0.1-3.el7.x86_64.rpm
sudo rpm -Uvh cpp-4.8.5-44.el7.x86_64.rpm
sudo rpm -Uvh glibc-2.17-324.el7_9.x86_64.rpm glibc-common-2.17-324.el7_9.x86_64.rpm
sudo rpm -Uvh kernel-headers-3.10.0-1160.31.1.el7.x86_64.rpm
sudo rpm -Uvh glibc-headers-2.17-324.el7_9.x86_64.rpm
sudo rpm -Uvh glibc-devel-2.17-324.el7_9.x86_64.rpm
sudo rpm -Uvh libgcc-4.8.5-44.el7.x86_64.rpm
sudo rpm -Uvh libgomp-4.8.5-44.el7.x86_64.rpm
sudo rpm -Uvh gcc-4.8.5-44.el7.x86_64.rpm
sudo rpm -Uvh keyutils-libs-devel-1.5.8-3.el7.x86_64.rpm
sudo rpm -Uvh libcom_err-1.42.9-19.el7.x86_64.rpm e2fsprogs-libs-1.42.9-19.el7.x86_64.rpm libss-1.42.9-19.el7.x86_64.rpm e2fsprogs-1.42.9-19.el7.x86_64.rpm
sudo rpm -Uvh libcom_err-devel-1.42.9-19.el7.x86_64.rpm
sudo rpm -Uvh libkadm5-1.15.1-50.el7.x86_64.rpm krb5-libs-1.15.1-50.el7.x86_64.rpm
sudo rpm -Uvh libsepol-devel-2.5-10.el7.x86_64.rpm
sudo rpm -Uvh pcre-devel-8.32-17.el7.x86_64.rpm
sudo rpm -Uvh libselinux-devel-2.5-15.el7.x86_64.rpm
sudo rpm -Uvh libverto-devel-0.2.5-4.el7.x86_64.rpm
sudo rpm -Uvh krb5-libs-1.15.1-50.el7.x86_64.rpm
sudo rpm -Uvh krb5-devel-1.15.1-50.el7.x86_64.rpm
sudo rpm -Uvh zlib-1.2.7-19.el7_9.x86_64.rpm
sudo rpm -Uvh zlib-devel-1.2.7-19.el7_9.x86_64.rpm
sudo rpm -Uvh bzip2-devel-1.0.6-13.el7.x86_64.rpm
sudo rpm -Uvh libffi-devel-3.0.13-19.el7.x86_64.rpm
sudo rpm -Uvh openssl-1.0.2k-21.el7_9.x86_64.rpm openssl-libs-1.0.2k-21.el7_9.x86_64.rpm openssl-devel-1.0.2k-21.el7_9.x86_64.rpm
cd ../ 
rm -rf python2.7.11_dependencies
echo $(pwd)
sudo cp $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Python/Python-2.7.11.tgz ./
tar -zxf Python-2.7.11.tgz && cd Python-2.7.11
./configure --enable-optimizations
sudo make altinstall
sudo rm -f /usr/bin/python
sudo ln -s $INSIGHTS_APP_ROOT_DIRECTORY/Python-2.7.11/python /usr/bin/python
python --version
cp $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Python/pip2.7_libraries/pip-20.3.4-py2.py3-none-any.whl ./
sudo python pip-20.3.4-py2.py3-none-any.whl/pip install --no-index pip-20.3.4-py2.py3-none-any.whl
sudo ln -s /usr/local/bin/pip /bin/pip
sudo pip -V
sudo pip install $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Python/pip2.7_libraries/*
source /etc/environment
source /etc/profile
sleep 5
