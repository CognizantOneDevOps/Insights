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
package com.cognizant.devops.platformcommons.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.util.Strings;

public interface LogLevelConstants {

	String ROLLING_FILE_APPENDER = "RollingFile";
	String CONSOLE_APPENDER = "Console";
	String PlatformWebhookSubscriber = "PlatformWebhookSubscriber";
	String PlatformEngine = "PlatformEngine";
	String PlatformService = "PlatformService";
	String PlatformReport = "PlatformReport";
	String PlatformRegressionTest = "PlatformRegressionTest";
	public static final List<String> UPDATED_LOGGER_LIST = Collections
			.unmodifiableList(Arrays.asList("com.cognizant", Strings.EMPTY));
}
