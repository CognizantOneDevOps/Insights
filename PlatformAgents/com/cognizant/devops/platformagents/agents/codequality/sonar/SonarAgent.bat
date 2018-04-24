pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\sonar
python -c "from com.cognizant.devops.platformagents.agents.codequality.sonar.SonarAgent import SonarAgent; SonarAgent()"