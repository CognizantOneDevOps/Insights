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
package com.cognizant.devops.platformworkflow.workflowtask.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;



public class InsightsStatusProvider extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(InsightsStatusProvider.class);
	HealthUtil healthUtil = new HealthUtil();
	
	static InsightsStatusProvider instance=null;
	private InsightsStatusProvider() {
		
	}
	
	public static InsightsStatusProvider getInstance() {
		if(instance==null) {
			instance=new InsightsStatusProvider();
		}
		return instance;
	}
	
	/**
	 * Methood create service health node in database
	 * 
	 * @param message
	 * @param status
	 * @return
	 */
	public boolean createInsightStatusNode(String message,String status) {
	try {
			String version = "";
			version = InsightsStatusProvider.class.getPackage().getImplementationVersion();
			log.debug(" Insights version {} ", version);
			healthUtil.createComponentHealthDetails(ServiceStatusConstants.PLATFORM_WORKFLOW,version, message, status);			
		
		} catch (Exception e) {
			log.error(" Unable to create Component Health records for PlatformWorkflow {} ", e.getMessage());

		}
			return Boolean.TRUE;
	}
}
