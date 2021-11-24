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
pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\xlrelease
setlocal ENABLEDELAYEDEXPANSION
for /f "delims=" %%i in ('python -V ^2^>^&^1') do (
   set PYTHON_VERSION=%%i
   if "!PYTHON_VERSION:~0,8!" EQU "Python 2" ( 
      echo Detected python 2 version
   	   python -c "from __AGENT_KEY__.com.cognizant.devops.platformagents.agents.releasemanagement.xlrelease.XlReleaseAgent import XlReleaseAgent; XlReleaseAgent()"
  	) else (
      if "!PYTHON_VERSION:~0,8!" EQU "Python 3" ( 
         echo Detected python 3 version
	   python -c "from __AGENT_KEY__.com.cognizant.devops.platformagents.agents.releasemanagement.xlrelease.XlReleaseAgent3 import XlReleaseAgent; XlReleaseAgent()"
      ) else (
         echo python version not supported 
      )
   )
)
