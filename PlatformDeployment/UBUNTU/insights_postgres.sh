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
# install postgresql
echo "#################### Installing Postgres with configs , Databases and Roles ####################"
cd /cd
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/postgres/postgres.zip
sleep 5
unzip postgres.zip
cd postgres
sudo dpkg -i *.deb
sudo cp pg_hba.conf /etc/postgresql/9.5/main/
sudo systemctl stop postgresql.service
sleep 15
sudo systemctl start postgresql.service
sudo useradd grafana
sudo usermod --password C0gnizant@1 grafana
chmod +x dbscript.sql
psql -U postgres -f dbscript.sql
rm -rf postgres*
