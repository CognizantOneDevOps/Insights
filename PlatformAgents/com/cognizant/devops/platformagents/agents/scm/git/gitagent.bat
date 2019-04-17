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

pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\git
python -V> tmpfile.txt 2>&1
FINDSTR /C:"Python 2" tmpfile.txt > nul
if %ERRORLEVEL% EQU 0 (
   echo "Detected Python2 version"
   del tmpfile.txt
   python -c "from com.cognizant.devops.platformagents.agents.scm.git.GitAgent import GitAgent; GitAgent()"
) else (
   echo "Detected Python3 version"
   del tmpfile.txt
   python -c "from com.cognizant.devops.platformagents.agents.scm.git.GitAgent3 import GitAgent; GitAgent()"
)
