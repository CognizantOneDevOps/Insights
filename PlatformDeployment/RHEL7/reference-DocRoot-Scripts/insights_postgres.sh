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
sudo rpm -ivh https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/postgres/pgdg.noarch.rpm -y
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/postgres/postgres_dependencies.zip
sudo unzip postgres_dependencies.zip && cd postgres_dependencies
sudo rpm -ivh *.rpm
sudo /usr/pgsql-9.5/bin/postgresql95-setup initdb
sudo systemctl enable postgresql-9.5.service
sudo chkconfig postgresql-9.5 on
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/postgres/pg_hba.conf
sudo cp pg_hba.conf /var/lib/pgsql/9.5/data/pg_hba.conf
sudo systemctl start  postgresql-9.5.service
sudo useradd grafana
sudo usermod --password C0gnizant@1 grafana
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/postgres/dbscript.sql
sudo chmod +x dbscript.sql
psql -U postgres -f dbscript.sql
cd ../
sudo rm -rf postgres_dependencies*
