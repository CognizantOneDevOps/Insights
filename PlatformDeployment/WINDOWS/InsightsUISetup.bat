goto comment
Copyright 2020 Cognizant Technology Solutions
  
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

echo "Setting up InsightsUI as Windows service"
echo "Setting up InSights Service as Windows service"
echo %INSIGHTS_HOME%
FOR %%A IN ("%INSIGHTS_HOME%") DO SET parentfolder=%%~dpA
echo %parentfolder%
echo %parentfolder%\nssm-2.24\win64\nssm
call %parentfolder%\nssm-2.24\win64\nssm install InsightsUI node "%parentfolder%\Configs\UI\UI.js"
Timeout 5
