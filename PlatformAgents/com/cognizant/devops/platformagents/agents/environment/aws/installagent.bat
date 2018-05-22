REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\aws
IF /I "%1%" == "UNINSTALL" (
	net stop AwsAgent
	sc delete AwsAgent
) ELSE (
	net stop AwsAgent
	sc delete AwsAgent
    nssm install AwsAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\aws\AwsAgent.bat
    net start AwsAgent
)
