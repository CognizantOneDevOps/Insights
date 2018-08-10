#!/bin/sh
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
echo "======= Postgres Health check Started  ========" >> postgres_health.txt
HOST=127.0.0.1
PORT=5432
if [ $# -eq 0 ]; then
    echo "No arguments supplied"
fi
if [ ! -z "$1" ]; then HOST="$1"; fi
if [ ! -z "$2" ]; then PORT="$2"; fi

echo "***** Script run  at " $(date '+%F %T')"*****"   >> postgres_health.txt
echo "Host "$HOST" PORT "$PORT  >> postgres_health.txt

echo "Check Postgres service status  " >> postgres_health.txt ;
systemctl is-active postgresql-9.5.service >> postgres_health.txt

servicestatus=$?
echo "	Postgres service status : "$servicestatus >> postgres_health.txt
if [ "$servicestatus" -eq 0 ]; then
  echo "	Postgres is installed and Running fine" >> postgres_health.txt
else
  echo "	Postgres is not installed or not Running as service "  >> postgres_health.txt
fi

db_psql_connetion=$(psql -A --quiet --no-align --tuples-only --dbname=postgres  -v ON_ERROR_STOP=1 --username=postgres --host=$HOST --port=$PORT  --command=" select 1 ")
echo $db_psql_connetion

if [ -z "$db_psql_connetion" ];then 
echo "A"
db_psql_connetion=11
fi 

if [ "$db_psql_connetion" -eq "1" ]; then
	echo "	pgsgl command working fine" >> postgres_health.txt
	echo "Run command to check DB file" >> postgres_health.txt

	database_names=$(psql -A --quiet --no-align --tuples-only --dbname=postgres  -v ON_ERROR_STOP=1 --username=postgres --host=$HOST --port=$PORT  --command="select datname FROM pg_database where datname in ('grafana','insight')")

	grafana_user_count=$(psql -X --host=$HOST --port=$PORT -d 'grafana' --username=grafana --tuples-only --set ON_ERROR_STOP=on --command "select count(*) from public.user u where u.login='admin'" )

	grafana_user=$(psql -X --host=$HOST --port=$PORT -d 'grafana' --username=grafana --tuples-only -v ON_ERROR_STOP=1 --command "select u.login from public.user u" )

	echo "	Database created with name as :"$database_names >> postgres_health.txt

	echo "	Number of user created in Grafana = "$grafana_user_count >> postgres_health.txt

	echo "	Grafana user details :"$grafana_user >> postgres_health.txt

	dashboard_count=$(psql --quiet --no-align --tuples-only --dbname=grafana --username=grafana --host=$HOST --port=$PORT   --command="select count(*) from public.dashboard d ")
	echo "	Number of dashboard configured = " $dashboard_count >> postgres_health.txt

	echo "	Datasource information : " >> postgres_health.txt
	psql -A --quiet --no-align --dbname=grafana --username=grafana --host=$HOST --port=$PORT   --command="select ds.type as Database_Type, ds.Name as Datasouce_Name, ds.url as  Datasouce_Url , ds.json_data as Datasouce_configuration from public.data_source ds " | while read -a Record ; do
	   datasouce_information=${Record}
	   echo "		"$datasouce_information  >> postgres_health.txt
	done
else 
	echo "	psql command not working,Please check connection or hostname or port " >> postgres_health.txt
fi

echo "=======Postgres Health check completed ======== " >> postgres_health.txt

echo "Usage: postgresCheck.sh Host PORT "
