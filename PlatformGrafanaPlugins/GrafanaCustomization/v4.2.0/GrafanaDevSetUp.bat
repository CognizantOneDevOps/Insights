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
go get github.com/grafana/grafana
set GRFANA_DEV_PATH=%GOPATH%\src\github.com\grafana\grafana
pushd %GRFANA_DEV_PATH%
git reset
git checkout .
git clean -fdx
git checkout tags/v4.2.0
go run build.go setup
go run build.go build
set GRAFANA_CUSTOMIZATION_PATH=%INSIGHTS_REPO_PATH%\PlatformGrafanaPlugins\GrafanaCustomization\v4.2.0
set GRAFANA_APPS_PATH=%INSIGHTS_REPO_PATH%\PlatformGrafanaPlugins
copy /y %GRAFANA_CUSTOMIZATION_PATH%\app.ts %GRFANA_DEV_PATH%\public\app\app.ts
copy /y %GRAFANA_CUSTOMIZATION_PATH%\system.conf.js %GRFANA_DEV_PATH%\public\app\system.conf.js
copy /y %GRAFANA_CUSTOMIZATION_PATH%\test-main.js %GRFANA_DEV_PATH%\public\test\test-main.js
copy /y %GRAFANA_CUSTOMIZATION_PATH%\index.html %GRFANA_DEV_PATH%\public\views\index.html
copy /y %GRAFANA_CUSTOMIZATION_PATH%\default_task.js %GRFANA_DEV_PATH%\tasks\default_task.js
copy /y %GRAFANA_CUSTOMIZATION_PATH%\concat.js %GRFANA_DEV_PATH%\tasks\options\concat.js
copy /y %GRAFANA_CUSTOMIZATION_PATH%\tslint.json %GRFANA_DEV_PATH%\tslint.json
copy /y %GRAFANA_APPS_PATH%\ScriptedDashboard\iSight.js %GRFANA_DEV_PATH%\public\dashboards\iSight.js

Xcopy /S /I /E /Y %GRAFANA_APPS_PATH%\Panels\insightscharts %GRFANA_DEV_PATH%\public\app\plugins\panel\insightscharts
Xcopy /S /I /E /Y %GRAFANA_APPS_PATH%\Panels\insightsCore %GRFANA_DEV_PATH%\public\app\plugins\panel\insightsCore
Xcopy /S /I /E /Y %GRAFANA_APPS_PATH%\Panels\pipeline %GRFANA_DEV_PATH%\public\app\plugins\panel\pipeline
Xcopy /S /I /E /Y %GRAFANA_APPS_PATH%\Panels\toolsinsights %GRFANA_DEV_PATH%\public\app\plugins\panel\toolsinsights

Xcopy /S /I /E /Y %GRAFANA_APPS_PATH%\DataSources\neo4j %GRFANA_DEV_PATH%\public\app\plugins\datasource\neo4j

call bower install angular-animate#1.6.1 --save
call bower install angular-aria#1.6.1 --save
call bower install angular-messages#1.6.1 --save
call bower install angular-material#1.1.1 --save

call npm install -g yarn
call yarn install --pure-lockfile
call grunt
