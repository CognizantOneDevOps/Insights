pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\aws
python -c "from com.cognizant.devops.platformagents.agents.environment.aws.AwsAgent import AwsAgent; AwsAgent()"