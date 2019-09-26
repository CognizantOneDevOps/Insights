echo "Stop the PostgresDB"
taskkill /fi "WindowTitle eq PSQLPROMPT"
Timeout 2
echo "Stop the Grafana service"
call %~dp0nssm-2.24\win64\nssm stop Grafana
Timeout 2
echo "Stop Tomcat service"
net stop Tomcat8
