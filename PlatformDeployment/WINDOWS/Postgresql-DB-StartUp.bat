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
echo "Start postgresql server"
start call %~dp0postgresql-9.5.4-1\pgsql\start-psql.bat
Timeout 10
echo "Create DB and Role for Grafana PostgreSQL"
call %~dp0postgresql-9.5.4-1\pgsql\createdb.bat
Timeout 5
echo "Create DB InSights PostgreSQL"
call %~dp0postgresql-9.5.4-1\pgsql\insightdb.bat
Timeout 5