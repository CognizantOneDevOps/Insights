echo "Start Grafana"
call %~dp0nssm-2.24\win64\nssm start Grafana
Timeout 5
echo "Start Tomcat"
net start Tomcat
Timeout 5

