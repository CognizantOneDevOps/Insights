pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\hp
python -c "from com.cognizant.devops.platformagents.agents.alm.hp.HpAlmAgent import HpAlmAgent; HpAlmAgent()"