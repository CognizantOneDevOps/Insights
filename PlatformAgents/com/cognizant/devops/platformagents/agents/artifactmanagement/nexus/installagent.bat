REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\nexus
nssm install NexusAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\nexus\NexusAgent.bat
sleep 2
net start NexusAgent
