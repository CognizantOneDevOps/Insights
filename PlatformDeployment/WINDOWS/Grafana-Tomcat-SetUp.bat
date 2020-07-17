echo "Setting up Grafana as Windows service"
call %~dp0nssm-2.24\win64\nssm install Grafana "%~dp0grafana-6.1.6\bin\grafana-server.exe"
Timeout 5
echo "Setting up Tomcat 9 as Windows service"
call %~dp0apache-tomcat\bin\service install
Timeout 5


REM Not using setx for setting path variable as it has limitaion of 255 charaters and editing registry to increase this is not a good option
REM echo "Set PATH for NSSM"
REM setx -m Path="%Path%;%~dp0nssm-2.24\win64"
