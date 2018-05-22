REM pushd %INSIGHTS_AGENT_HOME%\AgentDaemon
nssm install DaemonAgent %INSIGHTS_AGENT_HOME%\AgentDaemon\DaemonAgent.bat
sleep 2
net start DaemonAgent
