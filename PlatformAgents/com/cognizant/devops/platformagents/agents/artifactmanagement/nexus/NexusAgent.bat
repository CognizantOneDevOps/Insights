pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\nexus
python -c "from com.cognizant.devops.platformagents.agents.artifactmanagement.nexus.NexusAgent import NexusAgent; NexusAgent()"