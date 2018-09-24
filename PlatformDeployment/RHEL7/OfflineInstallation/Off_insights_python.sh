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

#
# arfifacts
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/Python-2.7.11.tgz
# http://www.rpmseek.com/rpm-pl/gcc.html?hl=com&cx=0:-:0:0:0:0:0
# https://pypi.org/project/pip/#files
# https://pypi.org/project/pytz/#files
# https://pypi.org/project/pika/#files
# https://pypi.org/project/python-dateutil/#files
# https://pypi.org/project/xmltodict/#files




echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
sudo mkdir python && cd python
sudo cp /usr/Offline_Installation/Python/Python-2.7.11.tgz ./
sudo tar -zxf Python-2.7.11.tgz && cd Python-2.7.11
rpm -i /usr/Offline_Installation/Python/gcc-2.96-113.src.rpm
sudo cp /usr/Offline_Installation/Python/Libraries/pip-18.0-py2.py3-none-any.whl ./
sudo python pip-18.0-py2.py3-none-any.whl/pip install --no-index pip-18.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/futures-3.2.0-py2-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/funcsigs-1.0.2-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/six-1.11.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/setuptools-40.0.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/pytz-2018.5-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/tzlocal-1.5.1.tar.gz
pip install /usr/Offline_Installation/Python/Libraries/ntlm_auth-1.2.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/pycparser-2.18.tar.gz
pip install /usr/Offline_Installation/Python/Libraries/asn1crypto-0.24.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/cffi-1.11.5-cp27-cp27mu-manylinux1_x86_64.whl
pip install /usr/Offline_Installation/Python/Libraries/idna-2.7-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/enum34-1.1.6-py2-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/cryptography-2.3-cp27-cp27mu-manylinux1_x86_64.whl
pip install /usr/Offline_Installation/Python/Libraries/APScheduler-3.5.1-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/requests_ntlm-1.1.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/pika-0.12.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/python_dateutil-2.7.3-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/xmltodict-0.11.0-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/certifi-2018.4.16-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/chardet-3.0.4-py2.py3-none-any.whl
pip install /usr/Offline_Installation/Python/Libraries/urllib3-1.23-py2.py3-none-any.whl
sudo cp /usr/Offline_Installation/Python/Libraries/requests-2.19.1-py2.py3-none-any.whl ./
pip install --no-index --find-links requests-2.19.1-py2.py3-none-any.whl requests
source /etc/environment
source /etc/profile
sleep 5