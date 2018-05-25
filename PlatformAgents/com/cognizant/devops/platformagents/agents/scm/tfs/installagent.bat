REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\tfs
IF /I "%1%" == "UNINSTALL" (
	net stop TFSAgent
	sc delete TFSAgent
) ELSE (
	net stop TFSAgent
	sc delete TFSAgent
    nssm install TFSAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\tfs\TFSAgent.bat
    net start TFSAgent
)