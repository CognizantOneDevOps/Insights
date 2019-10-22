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
package com.cognizant.devops.platformenterpriseengine.message.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;
//import com.cognizant.devops.platformwebhookengine.modules.users.EngineUsersModule;

public class EnterpriseEngineStatusLogger extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(EnterpriseEngineStatusLogger.class.getName());
	static EnterpriseEngineStatusLogger instance = null;

	private EnterpriseEngineStatusLogger() {

	}

	public static EnterpriseEngineStatusLogger getInstance() {
		if (instance == null) {
			instance = new EnterpriseEngineStatusLogger();
		}
		return instance;
	}

	public boolean createEngineStatusNode(String message, String status) {
	try {
			String version = "";
			version = EnterpriseEngineStatusLogger.class.getPackage().getImplementationVersion();
			log.debug("Platform Webhook Engine version " + version);
			Map<String, String> extraParameter = new HashMap<String, String>(0);
			createComponentStatusNode("HEALTH:WEBHOOKENGINE", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node " + e.getMessage());
		}
		return Boolean.TRUE;
	}
}
