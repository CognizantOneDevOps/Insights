REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\artifactory
nssm install ArtifactoryAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\artifactory\ArtifactoryAgent.bat
sleep 2
net start ArtifactoryAgent