/*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.healthStatus;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

public class HealthStatusTestData extends AbstractTestNGSpringContextTests{
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	String categoryGit = "SCM";
	String categoryService = "Platform Service";
	String categoryEngine = "Platform Engine";
	String categoryWorkflow = "Platform Workflow";
	String categoryGitWrong = "";
	String toolName = "github2";
	String agentId = "SJ_GITHUB2_June24";
	String failedAgentId = "Git_Failed";
	String emptyString = "   ";
	String PLATFORM_SERVICE = "Platform Service";
	String PLATFORM_ENGINE = "Platform Engine";
	String PLATFORM_WORKFLOW = "Platform Workflow";
}
