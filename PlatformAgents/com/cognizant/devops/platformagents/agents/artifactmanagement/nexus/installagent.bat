REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\nexus
IF /I "%1%" == "UNINSTALL" (
	net stop NexusAgent
	sc delete NexusAgent
) ELSE (
	net stop NexusAgent
	sc delete NexusAgent
    nssm install NexusAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\nexus\NexusAgent.bat
    net start NexusAgent
)
