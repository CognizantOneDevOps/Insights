/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.grafanadashboard;

import java.util.HashMap;
import java.util.Map;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;

public class GrafanaDashboardReportData extends AbstractTestNGSpringContextTests{
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	int taskID = 0;
	int emailTaskID = 0;
	int relationTaskID = 0;
	Map<String, String> testAuthData = new HashMap<>();
	String cookiesString="";
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();
	public static final String GRAFANA_PDF_SOURCE = "GRAFANADASHBOARDPDFREPORT";
}
