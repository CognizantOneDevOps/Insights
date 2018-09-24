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
# https://yum.postgresql.org/9.5/redhat/rhel-7-x86_64/
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/postgres/pg_hba.conf
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/postgres/dbscript.sql


echo "#################### Installing Postgres with configs , Databases and Roles ####################"
cd /opt/

rpm -i  /usr/Offline_Installation/Postgres/postgresql95-libs-9.5.13-1PGDG.rhel7.x86_64.rpm
rpm -i  /usr/Offline_Installation/Postgres/postgresql95-9.5.13-1PGDG.rhel7.x86_64.rpm
rpm -i  /usr/Offline_Installation/Postgres/postgresql95-server-9.5.13-1PGDG.rhel7.x86_64.rpm
rpm -i  /usr/Offline_Installation/Postgres/postgresql95-contrib-9.5.13-1PGDG.rhel7.x86_64.rpm
sudo /usr/pgsql-9.5/bin/postgresql95-setup initdb
sudo systemctl enable postgresql-9.5.service
sudo chkconfig postgresql-9.5 on
sudo cp /usr/Offline_Installation/Postgres/pg_hba.conf  /var/lib/pgsql/9.5/data/pg_hba.conf
#sudo cp pg_hba.conf  /var/lib/pgsql/9.5/data/pg_hba.conf
sudo systemctl start postgresql-9.5.service
sudo useradd grafana
sudo usermod --password C0gnizant@1 grafana
sudo cp /usr/Offline_Installation/Postgres/dbscript.sql ./
sudo chmod +x dbscript.sql
psql -U postgres -f dbscript.sql
