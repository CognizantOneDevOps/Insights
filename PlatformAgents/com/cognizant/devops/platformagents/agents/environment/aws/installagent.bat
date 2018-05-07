REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\aws
nssm install AwsAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\aws\AwsAgent.bat
sleep 2
net start AwsAgent
