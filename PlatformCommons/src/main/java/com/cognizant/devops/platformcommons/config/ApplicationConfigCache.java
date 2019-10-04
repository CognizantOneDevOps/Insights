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
package com.cognizant.devops.platformcommons.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

public class ApplicationConfigCache {
	static Logger log = LogManager.getLogger(ApplicationConfigCache.class.getName());
	
	private ApplicationConfigCache(){
		
	}
	
	public static boolean updateConfigCahe(){
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		if(!configFile.exists()){
			configFile.getParentFile().mkdirs();
			log.warn("iSight config file created at : "+configFile.getAbsolutePath());
		}else{
			log.warn("iSight config file found at : "+configFile.getAbsolutePath());
		}
		try (JsonWriter writer = new JsonWriter(new FileWriter(configFile))) {
			writer.setIndent("  ");
			new GsonBuilder().disableHtmlEscaping().create().toJson(ApplicationConfigProvider.getInstance(), ApplicationConfigProvider.class, writer);
		} catch(IOException e){
			log.error(e);
		}
		return false;
	}
	
	
	public static boolean loadConfigCache() {
		if(System.getenv().get(ConfigOptions.INSIGHTS_HOME) == null){
			log.error("INSIGHTS_HOME environment variable is not set.");
			// System.exit(0);
		}
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		StringBuffer json = new StringBuffer();
		if(configFile.exists()){
			try(BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)) {
				String line;
				while ((line = reader.readLine()) != null) {
					json.append(line);
				}
			} catch (FileNotFoundException e) {
				log.error("Config file not found", e);
			} catch (IOException e) {
				log.error("Unable to read the file", e);
			}
		}else{
			try(BufferedReader reader = new BufferedReader(
					new InputStreamReader(
							ApplicationConfigCache.class.getResourceAsStream("/"+ConfigOptions.CONFIG_FILE)), 1024)){
				String line;
				while ((line = reader.readLine()) != null) {
					json.append(line);
				}
			} catch (IOException e) {
				log.error("Unable to read the file", e);
			}
		}
		return initialize(json.toString());
	}

	public static boolean initConfigCacheFromResources() {
		URL resource = ApplicationConfigCache.class.getClassLoader().getResource(ConfigOptions.CONFIG_FILE);
		File configFile = new File(resource.getFile());
		StringBuffer json = new StringBuffer();
		try(BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)) {
			String line;
			while ((line = reader.readLine()) != null) {
				json.append(line);
			}
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return initialize(json.toString());
	}

	private static boolean initialize(String json) {
		try {
			Gson gson = new Gson();
			JsonParser parser = new JsonParser();
			JsonElement parsedJson = parser.parse(json);
			JsonElement jsonElement = new JsonParser().parse(json);
			if(jsonElement != null && jsonElement.isJsonObject()){
				ApplicationConfigProvider config = gson.fromJson(jsonElement.getAsJsonObject(), ApplicationConfigProvider.class);
				ApplicationConfigProvider.updateConfig(config);
			}
		} catch (JsonIOException e) {
			log.error(ConfigOptions.CONFIG_FILE + " is not formatted,Json parsing issue in file " + e);
		} catch (JsonSyntaxException e) {
			log.error(ConfigOptions.CONFIG_FILE + " is not formatted,Json parsing issue in file " + e);
		}
		return false;
	}
	
	public static String readConfigFile(){
		File configFile = new File(ConfigOptions.CONFIG_FILE_RESOLVED_PATH);
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile), 1024)){
			String s = null;
			StringBuffer sb = new StringBuffer();
			while( (s = reader.readLine()) != null){
				sb.append(s);
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return "";
	}
}
