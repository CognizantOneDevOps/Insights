REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\svn
IF /I "%1%" == "UNINSTALL" (
	net stop svnAgent
	sc delete svnAgent
) ELSE (
   net stop svnAgent
   sc delete svnAgent
   nssm install svnAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\svn\SvnAgent.bat
   net start svnAgent
)