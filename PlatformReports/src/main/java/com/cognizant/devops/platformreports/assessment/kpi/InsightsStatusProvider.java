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
package com.cognizant.devops.platformreports.assessment.kpi;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;

public class InsightsStatusProvider extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(InsightsStatusProvider.class);
	
	static InsightsStatusProvider instance=null;
	private InsightsStatusProvider() {
		
	}
	
	public static InsightsStatusProvider getInstance() {
		if(instance==null) {
			instance=new InsightsStatusProvider();
		}
		return instance;
	}
	
	public boolean createInsightStatusNode(String message,String status) {
	try {
			String version = "";
			version = InsightsStatusProvider.class.getPackage().getImplementationVersion();
			log.debug( " Insights version " +  version    );
			Map<String,String> extraParameter= new HashMap<String,String>(0);
			createComponentStatusNode("HEALTH:INSIGHTS",version,message,status,extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node " + e.getMessage());
		}
			return Boolean.TRUE;
	}
}
