pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\concourse
python -c "from com.cognizant.devops.platformagents.agents.ci.concourse.ConcourseAgent import ConcourseAgent; ConcourseAgent()"
