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
REM pushd %INSIGHTS_AGENT_HOME%\AgentDaemon
IF /I "%1%" == "UNINSTALL" (
	net stop DaemonAgent
	sc delete DaemonAgent
) ELSE (
    net stop DaemonAgent
	sc delete DaemonAgent
    nssm install DaemonAgent %INSIGHTS_AGENT_HOME%\AgentDaemon\DaemonAgent.bat 
    net start DaemonAgent
)

    @echo off 
	setlocal enableextensions enabledelayedexpansion
	set "search=FOO"
	set configPath=%INSIGHTS_AGENT_HOME%\AgentDaemon\com\cognizant\devops\platformagents\agents\agentdaemon\config.json
	echo %configPath%
	set psqlpath=%INSIGHTS_HOME%\..\postgresql-9.5.4-1\pgsql\bin
	echo %psqlpath%

	set data=
	for /f "delims=" %%x in (%configPath%) do set "data=!data!%%x"

	set textFile=Daemonagent.sql

	del %textFile%

	echo INSERT INTO public.agent_configuration(id, agent_id, agent_json, agent_key, agent_status, agent_version,data_update_supported, os_version, tool_category, tool_name,unique_key, update_date) VALUES (100, 0,'FOO','daemon-1523257126' ,'' ,'' , FALSE,'' , 'DAEMONAGENT', 'AGENTDAEMON', 'daemon-1523257126', current_date); >> %textFile%

	for /f "delims=" %%i in ('type "%textFile%" ^& break ^> "%textFile%" ') do (
		set "line=%%i"
		setlocal enabledelayedexpansion
		 >> %textFile% echo(!line:%search%=%data%!
		endlocal
	)
	IF /I "%1%" == "UNINSTALL" (
		echo "Uninstall no need to updare DB"
	) ELSE (
		%psqlpath%\psql -U postgres -d "insight" -f %~dp0Daemonagent.sql
	)
