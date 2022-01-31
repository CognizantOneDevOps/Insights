goto comment
Copyright 2021 Cognizant Technology Solutions
  
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
pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\github2
setlocal ENABLEDELAYEDEXPANSION
:agent_type will be set after UI registration
set AGENTTYPE=__AGENT_TYPE__
for /f "delims=" %%i in ('python -V ^2^>^&^1') do (
   set PYTHON_VERSION=%%i
      if "!PYTHON_VERSION:~0,8!" EQU "Python 3" ( 
         echo Detected python 3 version
         echo Running Agent Python 3 ------------------------------------------------
		 python -c "from  __AGENT_KEY__.com.cognizant.devops.platformagents.agents.scm.github2.GitAgentV2 import GitAgentV2; GitAgentV2()"
      ) else ( 
         echo python version not supported 
      )
)
