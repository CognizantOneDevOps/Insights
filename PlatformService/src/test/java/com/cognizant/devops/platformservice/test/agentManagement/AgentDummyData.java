package com.cognizant.devops.platformservice.test.agentManagement;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class AgentDummyData {

	String toolName = "toolName";
	
	String agentVersion = "AGENT_VERSION";
	String osversion = "OS_VERSION";
	String configDetails = "{\n  \"mqConfig\": {\n    \"user\": \"iSight\",\n    \"password\": \"iSight\",\n    \"host\": \"127.0.0.1\",\n    \"exchange\": \"iSight\",\n    \"agentControlXchg\":\"iAgent\"    \n  },\n  \"subscribe\": {\n    \"config\": \"CI.JENKINS.config\"\n  },\n  \"publish\": {\n    \"data\": \"CI.JENKINS.DATA\",\n    \"health\": \"CI.JENKINS.HEALTH\"\n  },\n  \"communication\": {\n    \"type\": \"REST\",\n    \"sslVerify\": true,\n    \"responseType\": \"JSON\"\n  },\n  \"dynamicTemplate\": {\n  \t\"timeFieldMapping\" : {\n\t\t\"startDate\" : \"%Y-%m-%d\"\n  \t},\n\t\"responseTemplate\": {\n\t  \"actions\": [\n\t    {\n\t      \"causes\": [\n\t        {\n\t          \"shortDescription\": \"shortDescription\"\n\t        }\n\t      ]\n\t    },\n\t    {\n\t      \"remoteUrls\": [\n\t        \"scmUrl\"\n\t      ]\n\t    },\n\t    {\n\t    \"url\": \"sonarUrl\"\n\t    }\n\t  ],\n\t  \"changeSet\": {\n\t    \"items\": [\n\t      {\n\t        \"commitId\": \"scmCommitId\",\n\t        \"author\": {\n\t          \"fullName\": \"scmAuthor\"\n\t        },\n\t        \"date\": \"buildDate\"\n\t      }\n\t    ],\n\t    \"kind\": \"scmKind\"\n\t  },\n\t  \"duration\": \"duration\",\n\t  \"id\": \"buildNumber\",\n\t  \"number\": \"number\",\n\t  \"result\": \"result\",\n\t  \"timestamp\": \"buildTimestamp\",\n\t  \"url\": \"buildUrl\"\n\t }\n  },\n\"jobDetails\" : {\n\t\"rundeckJobId\" : \"maven2-moduleset/publishers/org.jenkinsci.plugins.rundeck.RundeckNotifier/jobId\",\n\t\"scmRemoteUrl\" : \"maven2-moduleset/scm/userRemoteConfigs/hudson.plugins.git.UserRemoteConfig/url\",\n\t\"nexusRepoUrl\" : \"maven2-moduleset/publishers/hudson.maven.RedeployPublisher/url\",\n\t\"groupId\" : \"maven2-moduleset/rootModule/groupId\",\n\t\"artifactId\" : \"maven2-moduleset/rootModule/artifactId\"\n},\n\"agentId\":\"\",\n\"toolCategory\" : \"CI\",\n\"toolsTimeZone\" : \"Asia/Kolkata\",\n\"enableDataValidation\": true,\n\"useResponseTemplate\" : true,\n\"useAllBuildsApi\" : true,\n\"isDebugAllowed\" : false,\n\"enableValueArray\": false,\n\"userid\": \"username\",\n\"passwd\": \"password\",\n\"runSchedule\": 30,\n\"baseUrl\": \"http://127.0.0.1:8080/\",\n\"jenkinsMasters\" : {\n  \t\"master1\" : \"http://127.0.0.1:8080/\",\n  \t\"master2\" : \"http://127.0.0.1:8080/\"\n  },\n\"timeStampField\":\"buildTimestamp\",\n\"timeStampFormat\":\"epoch\",\n\"isEpochTimeFormat\" : true,\n\"startFrom\" : \"2019-03-01 15:46:33\",\n\"loggingSetting\" : {\n\t\"logLevel\" : \"WARN\",\n\t\"maxBytes\" : 5000000,\n\t\"backupCount\" : 1000\n}\n}";
	
	String trackingDetails = "TRACKING_DETAILS";
	
	Date updateDate = Timestamp.valueOf(LocalDateTime.now());
	
	String agentId = "agentId_123";
	String toolCategory = "toolCategory";
	
}