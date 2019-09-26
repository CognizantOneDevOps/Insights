echo "Setting up Grafana as Windows service"
call %~dp0nssm-2.24\win64\nssm install Grafana "%~dp0grafana-5.2.2\bin\grafana-server.exe"
Timeout 5 
echo "Setting up Tomcat 8 as Windows service"
call %~dp0apache-tomcat-8.5.32\bin\service install
Timeout 5


REM Not using setx for setting path variable as it has limitaion of 255 charaters and editing registry to increase this is not a good option
REM echo "Set PATH for NSSM"
REM setx -m Path="%Path%;%~dp0nssm-2.24\win64"
