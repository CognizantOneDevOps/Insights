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

import java.io.File;

public interface ConfigOptions {
	String CONFIG_DIR = ".InSights";
	String CONFIG_FILE = "server-config.json";
	String TOOLS_CONFIG_FILE = "toolsConfig.json";
	String CORRELATION_TEMPLATE = "correlation.json";
	String ENDPOINT_DATA = "endpointData";
	String USER_DATA = "userData";
	String PROPERTY_USER_HOME = "user.home";
	String INSIGHTS_HOME = "INSIGHTS_HOME";
	String TRANSFORMATION = PlatformServiceConstants.TRANSFORMATION_DECODED;
	String SP = PlatformServiceConstants.SP_DECODED;
	String RSA = PlatformServiceConstants.RSA_DECODED;
	String FILE_SEPERATOR = File.separator;
	//String CONFIG_FILE_RESOLVED_PATH = System.getProperty(ConfigOptions.PROPERTY_USER_HOME) + "\\" + ConfigOptions.CONFIG_DIR + "\\" + ConfigOptions.CONFIG_FILE;
	String CONFIG_FILE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR + ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.CONFIG_FILE;
	String CORRELATION_FILE_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR + ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.CORRELATION_TEMPLATE;
	String TOOLS_CONFIG_FILE_RESOLVED_PATH = System.getProperty(ConfigOptions.PROPERTY_USER_HOME) + FILE_SEPERATOR + ConfigOptions.CONFIG_DIR + FILE_SEPERATOR + ConfigOptions.TOOLS_CONFIG_FILE;
	String FAILURE_RESPONSE = "FAILURE";
	String SUCCESS_RESPONSE = "SUCCESS";
	String CONFIG_FILE_DIR = System.getenv().get(INSIGHTS_HOME) + FILE_SEPERATOR + ConfigOptions.CONFIG_DIR;
	String TRANSFORMATION_ENCODED = "UlNBL05PTkUvTm9QYWRkaW5n";
	String SP_ENCODED = "QkM=";
	String RSA_ENCODED = "UlNB";
	String CLIENT_NAME = "clientName";
	String CLIENT_ADDRESS = "address";
	String MAC_ADDRESS = "macAddress";
	String VALID_TILL = "validTill";
	String DATE_FORMAT = "MM/dd/yyyy";
}
