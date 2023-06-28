/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;


public class LoadServerConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent>{
	
	static Logger log = LogManager.getLogger(LoadServerConfig.class);
	
	
	@Override
	  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationEvent) {
		
		log.debug(" Inside LoadServerConfig onApplicationEvent  ");
		loadServerConfig();
	  }
	
	
	private void loadServerConfig() {
		try {
			log.debug(" Inside LoadServerConfig  loadServerConfig  ");
			ApplicationConfigCache.loadInitialConfigCache();
		} catch (InsightsCustomException e) {
			log.error(e);
		}
	}
	

}
