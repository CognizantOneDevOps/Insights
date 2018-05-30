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
pushd PlatformUI2.0
bower install --config.strict-ssl=false --config.proxy= --config.https-proxy= --force
npm install
pushd PlatformUI2.0
grunt
REM chage the below command for actual path
Xcopy /S /I /E /Y PlatformUI2.0\app C:\INSIGHTS_RELEASE\InSightsCodeBase\app
