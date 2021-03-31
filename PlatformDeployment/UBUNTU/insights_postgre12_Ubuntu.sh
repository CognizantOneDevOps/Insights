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
echo "#################### Installing Postgres 12 with configs , Databases and Roles ####################"
cd /opt
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/postgres/postgres12_dependencies_Ubuntu.zip
sleep 5
unzip postgres12_dependencies_Ubuntu.zip
cd postgres12_dependencies_Ubuntu
sudo dpkg -i ssl-cert_1.0.37_all.deb pgdg-keyring_2018.2_all.deb postgresql-client-common_225.pgdg16.04+1_all.deb libpq5_13.2-1.pgdg16.04+1_amd64.deb postgresql-common_225.pgdg16.04+1_all.deb postgresql-client-12_12.6-1.pgdg16.04+1_amd64.deb libllvm6.0_6.0-1ubuntu16.04.1_amd64.deb postgresql-12_12.6-1.pgdg16.04+1_amd64.deb libpq-dev_13.2-1.pgdg16.04+1_amd64.deb 
cd /opt/postgres12_dependencies_Ubuntu/pgAdmin4
sudo apt-get update --fix-missing
sudo cp /opt/postgres12_dependencies_Ubuntu/Conf/pg_hba.conf /etc/postgresql/12/main/pg_hba.conf
sudo cp /opt/postgres12_dependencies_Ubuntu/Conf/postgresql.conf /etc/postgresql/12/main/postgresql.conf
sudo systemctl stop postgresql@12-main
sleep 15
sudo systemctl start postgresql@12-main
sudo useradd grafana
echo "Native system user 'grafana' is created. Need to set password for 'grafana' user."
echo -n "Password:"
read -s NATIVE_SYSTEM_USER_GRAFANA_PASSWORD
sudo usermod --password $NATIVE_SYSTEM_USER_GRAFANA_PASSWORD grafana
chmod +x /opt/postgres12_dependencies_Ubuntu/script/dbscript.sql
psql -U postgres -f /opt/postgres12_dependencies_Ubuntu/script/dbscript.sql
#rm -rf postgres*
