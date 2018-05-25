REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketcloud
IF /I "%1%" == "UNINSTALL" (
	net stop BitBucketCloudAgent
	sc delete BitBucketCloudAgent
) ELSE (
	net stop BitBucketCloudAgent
	sc delete BitBucketCloudAgent
    nssm install BitBucketCloudAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketcloud\BitBucketCloudAgent.bat
    net start BitBucketCloudAgent
)
