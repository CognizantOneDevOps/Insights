/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.test;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;

public class TestDal extends BaseDAL {
	final static Logger logger = LogManager.getLogger(TestDal.class);

	static boolean isInteger(double number) {
		return number % 1 == 0;// if the modulus(remainder of the division) of the argument(number) with 1 is 0 then return true otherwise false.
	}
	
	public static void main(String args[]) {
		try {
			logger.debug(" In TestDal");
			ApplicationConfigCache.loadConfigCache();
			TestDal testDALFeature = new TestDal();
			testDALFeature.createWorkflowType();
		} catch (Exception e) {
			logger.error("======================================");
			e.fillInStackTrace();
		}
	}

	private void createWorkflowType() {
		InsightsWorkflowType workflowType = new InsightsWorkflowType();
		WorkflowDAL workflowDAL = new WorkflowDAL();
		workflowType.setWorkflowType("SYSTEM2");
		try {
			
			List<InsightsWorkflowConfiguration> worlflowList = workflowDAL.getAllActiveWorkflowConfiguration();
			logger.debug("Object save  worlflowList {} ",worlflowList);
			int workflowTypeId = workflowDAL.saveWorkflowType(workflowType);
			logger.debug("Object save {} ",workflowTypeId);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
}
