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
