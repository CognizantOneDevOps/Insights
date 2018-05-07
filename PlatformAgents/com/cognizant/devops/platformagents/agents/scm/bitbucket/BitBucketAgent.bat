pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\bitbucket
python -c "from com.cognizant.devops.platformagents.agents.scm.bitbucket.BitBucketAgent import BitBucketAgent; BitBucketAgent()"