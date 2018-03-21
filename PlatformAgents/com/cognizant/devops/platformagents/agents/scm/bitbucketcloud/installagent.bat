REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketcloud
nssm install BitBucketCloudAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucketcloud\BitBucketCloudAgent.bat
sleep 2
net start BitBucketCloudAgent
