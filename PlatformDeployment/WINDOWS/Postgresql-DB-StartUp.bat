echo "Start postgresql server"
start call %~dp0postgresql-9.5.4-1\pgsql\start-psql.bat
Timeout 10
echo "Create DB and Role for Grafana PostgreSQL"
call %~dp0postgresql-9.5.4-1\pgsql\createdb.bat
Timeout 5
echo "Create DB InSights PostgreSQL"
call %~dp0postgresql-9.5.4-1\pgsql\insightdb.bat
Timeout 5