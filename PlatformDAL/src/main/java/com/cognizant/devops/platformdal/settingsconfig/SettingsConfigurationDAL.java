/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.settingsconfig;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class SettingsConfigurationDAL extends BaseDAL {
	public static final String SETTINGCONFIGURATIONQUERY= "FROM SettingsConfiguration SC WHERE SC.settingsType = :settingsType";
	public static final String SETTINGSTYPE="settingsType";
	private static Logger log = LogManager.getLogger(SettingsConfigurationDAL.class.getName());

	public Boolean saveSettingsConfiguration(SettingsConfiguration settingsConfiguration) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(SETTINGSTYPE, settingsConfiguration.getSettingsType());
			List<SettingsConfiguration> resultList =  getResultList(SETTINGCONFIGURATIONQUERY,
					SettingsConfiguration.class, parameters);

			SettingsConfiguration settingsConfig = null;
			if (resultList != null && !resultList.isEmpty()) {
				settingsConfig = resultList.get(0);
			}
			if (settingsConfig != null) {
				settingsConfig.setSettingsJson(settingsConfiguration.getSettingsJson());
				settingsConfig.setSettingsType(settingsConfiguration.getSettingsType());
				settingsConfig.setActiveFlag(settingsConfiguration.getActiveFlag());
				settingsConfig.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
				settingsConfig.setLastModifiedByUser(settingsConfiguration.getLastModifiedByUser());
				update(settingsConfig);
			} else {
				settingsConfiguration.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
				save(settingsConfiguration);
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public SettingsConfiguration loadSettingsConfiguration(String settingsType) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(SETTINGSTYPE, settingsType);
			List<SettingsConfiguration> results =   getResultList(SETTINGCONFIGURATIONQUERY,
					SettingsConfiguration.class, parameters);
			SettingsConfiguration settingsConfiguration = null;
			if (results != null && !results.isEmpty()) {
				settingsConfiguration = results.get(0);
			}
			return settingsConfiguration;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public String getSettingsJsonObject(String settingsType) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(SETTINGSTYPE, settingsType);
			List<SettingsConfiguration> results =   getResultList(SETTINGCONFIGURATIONQUERY,
					SettingsConfiguration.class, parameters);
			
			SettingsConfiguration settingsConfiguration = null;
			if (results != null && !results.isEmpty()) {
				settingsConfiguration = results.get(0);
				if (settingsConfiguration != null) {
					return settingsConfiguration.getSettingsJson();
				}
			}
			return null;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public void updateSettingJson(String modifiedSettingJson)throws InsightsCustomException {
		try  {
			SettingsConfiguration settingsConfiguration = loadSettingsConfiguration(ConfigOptions.DATAPURGING_SETTINGS_TYPE);
			if (settingsConfiguration != null) {
				settingsConfiguration.setSettingsJson(modifiedSettingJson);
				update(settingsConfiguration);
			}
		}		
		catch(Exception e){
			log.error("Error in updating setting_json column of settings_configuration table", e);
			throw new InsightsCustomException(e.toString());
		}		
	}
		
}
