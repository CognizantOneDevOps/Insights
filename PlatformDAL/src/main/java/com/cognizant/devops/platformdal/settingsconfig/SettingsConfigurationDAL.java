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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class SettingsConfigurationDAL extends BaseDAL {
	public static final String SETTINGCONFIGURATIONQUERY= "FROM SettingsConfiguration SC WHERE SC.settingsType = :settingsType";
	public static final String SETTINGSTYPE="settingsType";
	private static Logger log = LogManager.getLogger(SettingsConfigurationDAL.class.getName());

	public Boolean saveSettingsConfiguration(SettingsConfiguration settingsConfiguration) {
		
		try (Session session = getSessionObj()) {

			Query<SettingsConfiguration> createQuery = session.createQuery(
					SETTINGCONFIGURATIONQUERY, SettingsConfiguration.class);
			createQuery.setParameter(SETTINGSTYPE, settingsConfiguration.getSettingsType());
			List<SettingsConfiguration> resultList = createQuery.getResultList();
			SettingsConfiguration settingsConfig = null;
			if (resultList != null && !resultList.isEmpty()) {
				settingsConfig = resultList.get(0);
			}
			session.beginTransaction();
			if (settingsConfig != null) {
				settingsConfig.setSettingsJson(settingsConfiguration.getSettingsJson());
				settingsConfig.setSettingsType(settingsConfiguration.getSettingsType());
				settingsConfig.setActiveFlag(settingsConfiguration.getActiveFlag());
				settingsConfig.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
				settingsConfig.setLastModifiedByUser(settingsConfiguration.getLastModifiedByUser());
				session.update(settingsConfig);
			} else {
				settingsConfiguration.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
				session.save(settingsConfiguration);
			}
			session.getTransaction().commit();
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public SettingsConfiguration loadSettingsConfiguration(String settingsType) {
		try (Session session = getSessionObj()) {
			Query<SettingsConfiguration> loadQuery = session.createQuery(
					"FROM SettingsConfiguration SC WHERE SC.settingsType = :settingsType", SettingsConfiguration.class);
			loadQuery.setParameter(SETTINGSTYPE, settingsType);
			List<SettingsConfiguration> results = loadQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<SettingsConfiguration> createQuery = session.createQuery(
					SETTINGCONFIGURATIONQUERY, SettingsConfiguration.class);
			createQuery.setParameter(SETTINGSTYPE, settingsType);
			List<SettingsConfiguration> results = createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			SettingsConfiguration settingsConfiguration = loadSettingsConfiguration(ConfigOptions.DATAPURGING_SETTINGS_TYPE);
			session.beginTransaction();
			if (settingsConfiguration != null) {
				settingsConfiguration.setSettingsJson(modifiedSettingJson);
				session.update(settingsConfiguration);
			}
			session.getTransaction().commit();
		}		
		catch(Exception e){
			log.error("Error in updating setting_json column of settings_configuration table", e);
			throw new InsightsCustomException(e.toString());
		}		
	}
		
}
