/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.CorrelationBuilder;

public class CorrelationBuilderTestData {

	String saveDataConfig = "{\"data\":[{\"destination\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"fields\":[\"jir_priority\"]},\"source\":{\"toolName\":\"BITBUCKET\",\"toolCategory\":\"SCM\",\"fields\":[\"bit_commiTime\"]},\"relationName\":\"FROM_BITBUCKET_TO_JIRA_test\"}]}";
	String getConfigDetails = "[{\"destination\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"fields\":[\"jir_priority\"]},\"source\":{\"toolName\":\"BITBUCKET\",\"toolCategory\":\"SCM\",\"fields\":[\"bit_commiTime\"]},\"relationName\":\"FROM_BITBUCKET_TO_JIRA_test\"}]";
	String UpdateConfigDetails = "{\"data\":[{\"destination\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"fields\":[\"jir_priority\"]},\"source\":{\"toolName\":\"BITBUCKET\",\"toolCategory\":\"SCM\",\"fields\":[\"bit_commiTime\"]},\"relationName\":\"FROM_BITBUCKET_TO_JIRA_test\"},{\"destination\":{\"toolName\":\"GIT\",\"toolCategory\":\"SCM\",\"fields\":[\"\"]},\"source\":{\"toolName\":\"JENKINS\",\"toolCategory\":\"CI\",\"fields\":[\"\"]},\"relationName\":\"FROM_JENKINS_TO_GIT_new\"},{\"destination\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"fields\":[\"\"]},\"source\":{\"toolName\":\"PIVOTALTRACKER\",\"toolCategory\":\"ALM\",\"fields\":[\"storyId\"]},\"relationName\":\"FROM_PIVOTALTRACKER_TO_JIRA_new_test\"}]}";
	String DeleteConfigDetails = "{\"data\":[{\"destination\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"fields\":[\"jir_priority\"]},\"source\":{\"toolName\":\"BITBUCKET\",\"toolCategory\":\"SCM\",\"fields\":[\"bit_commiTime\"]},\"relationName\":\"FROM_BITBUCKET_TO_JIRA_test\"}]}";
}
