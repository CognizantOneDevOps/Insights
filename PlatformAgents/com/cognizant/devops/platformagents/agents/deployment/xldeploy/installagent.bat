REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\xldeploy
IF /I "%1%" == "UNINSTALL" (
	net stop XLDeployAgent
	sc delete XLDeployAgent
) ELSE (
	net stop XLDeployAgent
	sc delete XLDeployAgent 
    nssm install XLDeployAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\xldeploy\XLDeployAgent.bat
    net start XLDeployAgent
)
