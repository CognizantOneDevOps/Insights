REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\hp
nssm install HpAlmAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\hp\HpAlmAgent.bat
sleep 2
net start HpAlmAgent
