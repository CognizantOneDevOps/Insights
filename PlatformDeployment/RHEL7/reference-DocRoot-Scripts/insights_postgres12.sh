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
cd /opt
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/postgres/postgres12_dependencies.zip
sudo unzip postgres12_dependencies.zip && cd postgres12_dependencies
sudo yum localinstall pgdg-redhat-repo-latest.noarch.rpm postgresql12-libs-12.5-1PGDG.rhel7.x86_64.rpm postgresql12-12.5-1PGDG.rhel7.x86_64.rpm postgresql12-server-12.5-1PGDG.rhel7.x86_64.rpm -y
cd /opt/postgres12_dependencies/pgAdmin4
sudo yum localinstall pgadmin4-python3-mod_wsgi-4.7.1-2.el7.x86_64.rpm pgadmin4-4.30-1.el7.noarch.rpm pgadmin4-desktop-4.30-1.el7.x86_64.rpm pgadmin4-server-4.30-1.el7.x86_64.rpm pgadmin4-web-4.30-1.el7.noarch.rpm -y
sudo /usr/pgsql-12/bin/postgresql-12-setup initdb
sudo systemctl enable postgresql-12.service
sudo chkconfig postgresql-12 on
sudo /usr/pgadmin4/bin/setup-web.sh
sudo cp /opt/postgres12_dependencies/Conf/pg_hba.conf /var/lib/pgsql/12/data/pg_hba.conf
sudo cp /opt/postgres12_dependencies/Conf/postgresql.conf /var/lib/pgsql/12/data/postgresql.conf
sudo systemctl start postgresql-12.service
sudo useradd grafana
echo "Native system user 'grafana' is created. Need to set password for 'grafana' user."
echo -n "Password:"
read -s NATIVE_SYSTEM_USER_GRAFANA_PASSWORD
sudo usermod --password $NATIVE_SYSTEM_USER_GRAFANA_PASSWORD grafana
sudo chmod +x /opt/postgres12_dependencies/script/dbscript.sql
psql -U postgres -f /opt/postgres12_dependencies/script/dbscript.sql
cd ../
#sudo rm -rf postgres_dependencies*
