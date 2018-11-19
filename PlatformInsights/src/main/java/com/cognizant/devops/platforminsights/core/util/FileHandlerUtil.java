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
package com.cognizant.devops.platforminsights.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platforminsights.core.SparkJobExecutor;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileHandlerUtil {
	private static final Logger log = LogManager.getLogger(FileHandlerUtil.class);
	
	public static JsonObject loadJsonFile(String path) {
		InputStream in = SparkJobExecutor.class.getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
		try {
			reader.close();
		} catch (IOException e) {
			log.error("Unable to read file : "+path, e);
		}
		return json;
	}
}
