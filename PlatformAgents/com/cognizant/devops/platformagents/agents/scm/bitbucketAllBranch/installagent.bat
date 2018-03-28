REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketAllBranch
nssm install BitBucketAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketAllBranch\BitBucketAgentAllBranches.bat
sleep 2
net start BitBucketAgent
