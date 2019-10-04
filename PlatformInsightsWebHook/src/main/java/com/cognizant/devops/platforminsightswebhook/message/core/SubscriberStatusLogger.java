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
package com.cognizant.devops.platforminsightswebhook.message.core;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.core.util.ComponentHealthLogger;

public class SubscriberStatusLogger extends ComponentHealthLogger{

	private static Logger log = LogManager.getLogger(SubscriberStatusLogger.class.getName());
	static SubscriberStatusLogger instance = null;

	private SubscriberStatusLogger() {

	}

	public static SubscriberStatusLogger getInstance() {
		if (instance == null) {
			instance = new SubscriberStatusLogger();
		}
		return instance;
	}

	public boolean createSubsriberStatusNode(String message, String status) {
	try {
			String version = "";
			version = SubscriberStatusLogger.class.getPackage().getImplementationVersion();
			log.debug(" Subscriber version " + version);
			Map<String, String> extraParameter = new HashMap<String, String>(0);
			createComponentStatusNode("HEALTH:WEBHOOKSUBSCRIBER", version, message, status, extraParameter);
		} catch (Exception e) {
			log.error(" Unable to create node " + e.getMessage());
		}
		return Boolean.TRUE;
	}
}
