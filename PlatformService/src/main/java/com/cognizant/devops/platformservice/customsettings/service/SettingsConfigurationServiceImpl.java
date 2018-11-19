/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.customsettings.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.core.util.DataPurgingUtils;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfiguration;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfigurationDAL;
import com.google.gson.JsonObject;


@Service("settingsConfigurationService")
public class SettingsConfigurationServiceImpl implements SettingsConfigurationService{
	
	private static Logger LOG = LogManager.getLogger(SettingsConfigurationServiceImpl.class);

	@Override
	public Boolean saveSettingsConfiguration(String settingsJson,String settingsType,String activeFlag,String lastModifiedByUser) {		
		SettingsConfiguration settingsConfiguration = populateSettingsConfiguration(settingsJson,settingsType, activeFlag, lastModifiedByUser);
		SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();		
		return settingsConfigurationDAL.saveSettingsConfiguration(settingsConfiguration);		
	}	

	@Override
	public SettingsConfiguration loadSettingsConfiguration(String settingsType) {
		SettingsConfigurationDAL settingsConfigurationDAL = new SettingsConfigurationDAL();		
		return settingsConfigurationDAL.loadSettingsConfiguration(settingsType);	
	}
	

	private SettingsConfiguration populateSettingsConfiguration(String settingsJson, String settingsType,
			String activeFlag, String lastModifiedByUser) {
		SettingsConfiguration settingsConfiguration = new SettingsConfiguration();
		String updatedSettingsJson = updateNextRunTimeValue(settingsJson);
		settingsConfiguration.setSettingsJson(updatedSettingsJson);
		settingsConfiguration.setSettingsType(settingsType);
		settingsConfiguration.setActiveFlag(activeFlag);
		settingsConfiguration.setLastModifiedByUser(lastModifiedByUser);
		return settingsConfiguration;
	}
	
	/**
	 * Updates settingJson string coming from UI with nextRunTime value
	 * and saved into the database 
	 * @param settingsJson
	 * @return
	 */
	private String updateNextRunTimeValue(String settingsJson) {
		String updatedSettingsJson = null;
		JsonObject settingsJsonObject = DataPurgingUtils.convertSettingsJsonObject(settingsJson);
		String dataArchivalFrequency = DataPurgingUtils.getDataArchivalFrequency(settingsJsonObject);
		String nextRunTime = DataPurgingUtils.calculateNextRunTime(dataArchivalFrequency);
		settingsJsonObject = DataPurgingUtils.updateNextRunTime(settingsJsonObject,nextRunTime);
		if (settingsJsonObject != null) {
		  updatedSettingsJson = settingsJsonObject.toString();			
		}
		return updatedSettingsJson;
	}
	
}
