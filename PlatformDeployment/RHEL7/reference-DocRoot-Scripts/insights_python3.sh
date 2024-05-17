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
# Pythonecho "#################### Installing Python with Virtual Env ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir python3 && cd python3 && sudo wget https://www.python.org/ftp/python/3.10.2/Python-3.10.2.tgz
sudo mv Python-3.10.2.tgz Python.tgz
sudo tar -zxf Python.tgz
sudo mv Python-3.10.2 Python
cd Python
sudo yum groupinstall "Development Tools" -y
sudo yum install gcc -y
sudo yum install gcc-c++ -y
sudo yum install zlib -y  
sudo yum install zlib-devel -y
sudo yum install openssl-devel -y 
sudo yum install openssl11-devel -y 
sudo yum install bzip2-devel -y  
sudo yum install sqlite-devel -y
sudo yum install libffi -y
sudo yum install libffi-devel -y
export LD_LIBRARY_PATH=/usr/local/lib:/usr/local/lib64
openssl_minversion=1.1.1
if echo -e "$(openssl version|awk '{print $2}')\n${openssl_minversion}" | sort -V | head -1 | grep -q ^${openssl_minversion}$;then
  openssl version
else
  cd /usr/
  sudo wget https://ftp.openssl.org/source/openssl-1.1.1k.tar.gz
  sudo tar -xzvf openssl-1.1.1k.tar.gz
  cd openssl-1.1.1k
  sudo ./config --prefix=/usr --openssldir=/etc/ssl --libdir=lib no-shared zlib-dynamic
  sudo make
  sudo make test
  sudo make install
  openssl version
fi
cd $INSIGHTS_APP_ROOT_DIRECTORY/python3/Python
sudo ./configure --with-openssl=/usr --enable-optimizations
sudo make
sudo make altinstall
sudo rm -f /usr/bin/python3
sudo rm -f /usr/bin/python
sudo ln -s $INSIGHTS_APP_ROOT_DIRECTORY/python3/Python/python /usr/bin/python3
sudo ln -s $INSIGHTS_APP_ROOT_DIRECTORY/python3/Python/python /usr/bin/python
cd $INSIGHTS_APP_ROOT_DIRECTORY/python3 && sudo wget https://bootstrap.pypa.io/get-pip.py
python3 --version
sudo python3 get-pip.py
sudo python3 -m pip install setuptools -U
sudo python3 -m pip install pika
sudo python3 -m pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime neo4j neobolt elasticsearch pathvalidate
python3 --version
sleep 5
