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
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo yum install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-8-x86_64/pgdg-redhat-repo-latest.noarch.rpm
# Disable the built-in PostgreSQL module:
sudo yum -qy module disable postgresql
# Install PostgreSQL:
sudo yum install -y postgresql12-server  postgresql12-contrib postgresql12-libs postgresql12
sudo yum install -y https://ftp.postgresql.org/pub/pgadmin/pgadmin4/yum/pgadmin4-redhat-repo-2-1.noarch.rpm
sudo yum install pgadmin4-web -y
sudo yum install policycoreutils-python-utils
sudo /usr/pgsql-12/bin/postgresql-12-setup initdb
sudo systemctl enable postgresql-12.service
sudo chkconfig postgresql-12 on
sudo /usr/pgadmin4/bin/setup-web.sh
sudo sed -i  '/^host / s/ident/trust/' /var/lib/pgsql/12/data/pg_hba.conf
sudo sed -i  '/^local / s/ident/trust/' /var/lib/pgsql/12/data/pg_hba.conf
sudo sed -i  '/^host / s/peer/trust/' /var/lib/pgsql/12/data/pg_hba.conf
sudo sed -i  '/^local / s/peer/trust/' /var/lib/pgsql/12/data/pg_hba.conf
sudo systemctl start postgresql-12.service
sudo useradd grafana
echo "Native system user 'grafana' is created. Need to set password for 'grafana' user."
echo -n "Password:"
read -s NATIVE_SYSTEM_USER_GRAFANA_PASSWORD
sudo usermod --password $NATIVE_SYSTEM_USER_GRAFANA_PASSWORD grafana
echo  :> dbscript.sql
chmod +x dbscript.sql
printf '\n'
printf 'Writing to dbscript.sql file'
echo "CREATE USER grafana WITH PASSWORD '"$NATIVE_SYSTEM_USER_GRAFANA_PASSWORD"' SUPERUSER;">dbscript.sql
echo "CREATE DATABASE grafana WITH OWNER grafana TEMPLATE template0 ENCODING 'SQL_ASCII' TABLESPACE  pg_default LC_COLLATE  'C' LC_CTYPE  'C' CONNECTION LIMIT  -1;">>dbscript.sql
echo "CREATE DATABASE insight WITH OWNER grafana TEMPLATE template0 ENCODING 'SQL_ASCII' TABLESPACE  pg_default LC_COLLATE  'C' LC_CTYPE  'C' CONNECTION LIMIT  -1;">>dbscript.sql
printf '\n'
printf 'dbscript.sql is ready'
sudo chmod +x dbscript.sql
psql -U postgres -f dbscript.sql
cd ../../
sudo rm -rf postgres12_dependencies*
