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

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class SettingsConfigurationDAL extends BaseDAL {
	
	

	public Boolean saveSettingsConfiguration(SettingsConfiguration settingsConfiguration) {
		Query<SettingsConfiguration> createQuery = getSession().createQuery(
				"FROM SettingsConfiguration SC WHERE SC.id = :id",
				SettingsConfiguration.class);
		createQuery.setParameter("id", settingsConfiguration.getId());
		List<SettingsConfiguration> resultList = createQuery.getResultList();
		SettingsConfiguration settingsConfig = null;
		if(resultList != null && !resultList.isEmpty()){
			settingsConfig = resultList.get(0);
		}
		getSession().beginTransaction();
		if (settingsConfig != null) {
			settingsConfig.setSettingsJson(settingsConfiguration.getSettingsJson());
			settingsConfig.setSettingsType(settingsConfiguration.getSettingsType());
			settingsConfig.setActiveFlag(settingsConfiguration.getActiveFlag());
			settingsConfig.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
			settingsConfig.setLastModifiedByUser(settingsConfiguration.getLastModifiedByUser());
			getSession().update(settingsConfig);
		} else {
			settingsConfiguration.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
			getSession().save(settingsConfiguration);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}

	public SettingsConfiguration loadSettingsConfiguration(int id) {
		Query<SettingsConfiguration> loadQuery = getSession().createQuery("FROM SettingsConfiguration SC WHERE SC.id = :id", SettingsConfiguration.class);
		loadQuery.setParameter("id", id);
		SettingsConfiguration settingsConfiguration = new SettingsConfiguration();
		List<SettingsConfiguration> settingsConfigurationList = loadQuery.getResultList();
		if(settingsConfigurationList != null  && !settingsConfigurationList.isEmpty()){
			settingsConfiguration = settingsConfigurationList.get(0);
		}
		terminateSession();
		terminateSessionFactory();
		return settingsConfiguration;
	}
	
	public String getSettingsJsonObject(String settingsType) {
		Query<SettingsConfiguration> createQuery = getSession().createQuery(
				"FROM SettingsConfiguration SC WHERE SC.settingsType = :settingsType",
				SettingsConfiguration.class);
		createQuery.setParameter("settingsType",settingsType );
		List<SettingsConfiguration> resultList = createQuery.getResultList();
		SettingsConfiguration settingsConfiguration = null;
		if(resultList != null && !resultList.isEmpty()){
			settingsConfiguration = resultList.get(0);
			if (settingsConfiguration != null) {
				return settingsConfiguration.getSettingsJson();				
			}
		}
		return null;
	}
		
}
