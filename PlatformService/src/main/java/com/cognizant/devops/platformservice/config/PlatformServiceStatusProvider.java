/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ServiceStatusConstants;
import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;
import com.cognizant.devops.platformdal.healthutil.HealthUtil;

public class PlatformServiceStatusProvider extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(PlatformServiceStatusProvider.class);
	HealthUtil healthUtil = new HealthUtil();
	
	static PlatformServiceStatusProvider instance=null;
	private PlatformServiceStatusProvider() {
		
	}
	
	public static PlatformServiceStatusProvider getInstance() {
		if(instance==null) {
			instance=new PlatformServiceStatusProvider();
		}
		return instance;
	}
	
	public void createPlatformServiceStatusNode(String message,String status) {
		try {
			String version = PlatformServiceInitializer.class.getPackage().getSpecificationVersion();
			log.debug(" Insights version {} ", version);
			healthUtil.createComponentHealthDetails(ServiceStatusConstants.PLATFORM_SERVICE,version, message, status);
			
		} catch (Exception e) {
			log.error("Unable to create Component Health records for PlatformService {} ", e.getMessage());

		}
	}

}
