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
package com.cognizant.devops.platformneo4jbackuputility.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformneo4jbackuputility.app.Application;

public class InsightsUtils {

	static Properties properties = new Properties();
	private static Logger LOG = LogManager.getLogger(Application.class);

	public static void loadProperties() {
		FileInputStream input = null;

		try {
			String path = "./neo4j.properties";
			input = new FileInputStream(path);

			// load the properties file String config/config.properties
			properties.load(input);

		} catch (Exception ex) {
			LOG.error("Unable to read property file");
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					LOG.error("Unable to read property file");
				}
			}
		}
	}

	public static String getArgument(String[] args, int index, Properties properties, String key) {
		if (args.length > index)
			return args[index];
		return properties.getProperty(key);
	}

	public static String readProperty(String key) {
		return properties.getProperty(key);
	}

	public static Boolean isPropertyFetch() {
		return properties.isEmpty();
	}

	public static Set<String> splitToSet(String value) {
		if (value == null || value.trim().isEmpty())
			return emptySet();
		return new HashSet<>(asList(value.trim().split(", *")));
	}

}
