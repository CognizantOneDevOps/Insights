REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\hp
IF /I "%1%" == "UNINSTALL" (
	net stop HpAlmAgent
	sc delete HpAlmAgent
) ELSE (
	net stop HpAlmAgent
	sc delete HpAlmAgent
    nssm install HpAlmAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\hp\HpAlmAgent.bat
    net start HpAlmAgent
)