echo "Setting up PostgreSQL for Grafana as windows service"
call %~dp0postgresql-9.5.4-1\pgsql\setup-psql.bat
Timeout 5

