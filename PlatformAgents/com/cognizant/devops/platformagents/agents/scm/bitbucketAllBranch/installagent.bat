REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketAllBranch
IF /I "%1%" == "UNINSTALL" (
	net stop BitBucketAgent
	sc delete BitBucketAgent
) ELSE (
	net stop BitBucketAgent
	sc delete BitBucketAgent
    nssm install BitBucketAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketAllBranch\BitBucketAgentAllBranches.bat
    net start BitBucketAgent
)