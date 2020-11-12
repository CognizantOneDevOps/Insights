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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;

public class PlatformServiceStatusProvider extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(PlatformServiceStatusProvider.class);
	
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
			String version = "";
			version = PlatformServiceInitializer.class.getPackage().getSpecificationVersion();
			log.debug(" Insights version {} ", version);
			Map<String, String> extraParameter = new HashMap<>(0);
			createComponentStatusNode("HEALTH:INSIGHTS_PLATFORMSERVICE", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node {} ", e.getMessage());
		}
	}

}
