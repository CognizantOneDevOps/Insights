goto comment
Copyright 2017 Cognizant Technology Solutions
  
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy
of the License at
  
http://www.apache.org/licenses/LICENSE-2.0
  
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
:comment
echo "Setting up Grafana as Windows service"
call %~dp0nssm-2.24\win64\nssm install Grafana "%~dp0grafana-7.1.0\bin\grafana-server.exe"
Timeout 5
echo "Setting up Tomcat 9 as Windows service"
call %~dp0apache-tomcat\bin\service install
Timeout 5


REM Not using setx for setting path variable as it has limitaion of 255 charaters and editing registry to increase this is not a good option
REM echo "Set PATH for NSSM"
REM setx -m Path="%Path%;%~dp0nssm-2.24\win64"
