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
package com.cognizant.devops.engines.platformengine.message.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;

public class EngineStatusLogger extends ComponentHealthLogger {
	private static Logger log = LogManager.getLogger(EngineStatusLogger.class);
	static EngineStatusLogger instance = null;

	private EngineStatusLogger() {

	}

	public static EngineStatusLogger getInstance() {
		if (instance == null) {
			instance = new EngineStatusLogger();
		}
		return instance;
	}

	public boolean createEngineStatusNode(String message, String status) {
		try {
			String version = "";
			version = EngineStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Engine version for createEngineStatusNode {} ", version);
			Map<String, String> extraParameter = new HashMap<>(0);
			createComponentStatusNode("HEALTH:ENGINE", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node {} ", e.getMessage());
		}
		return Boolean.TRUE;
	}

	public boolean createDataArchivalStatusNode(String message, String status) {
		try {
			String version = "";
			version = EngineStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Engine version for createDataArchivalStatusNode {} ", version);
			Map<String, String> extraParameter = new HashMap<>(0);
			createComponentStatusNode("HEALTH:DATAARCHIVALENGINE", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node {}", e.getMessage());
		}
		return Boolean.TRUE;
	}

	public boolean createWebhookEngineStatusNode(String message, String status) {
		try {
			String version = "";
			version = EngineStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Engine version createWebhookEngineStatusNode {}", version);
			Map<String, String> extraParameter = new HashMap<>(0);
			createComponentStatusNode("HEALTH:WEBHOOKENGINE", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node {}", e.getMessage());
		}
		return Boolean.TRUE;
	}

	public boolean createAuditStatusNode(String message, String status) {
		try {
			String version = "";
			version = EngineStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Engine version createAuditStatusNode {} ", version);
			Map<String, String> extraParameter = new HashMap<>(0);
			createComponentStatusNode("HEALTH:AUDITENGINE", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node {} ", e.getMessage());
		}
		return Boolean.TRUE;
	}
}
