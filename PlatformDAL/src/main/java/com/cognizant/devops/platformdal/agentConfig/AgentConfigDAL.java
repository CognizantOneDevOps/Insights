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
package com.cognizant.devops.platformdal.agentConfig;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.core.enums.AGENTACTION;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;


public class AgentConfigDAL extends BaseDAL {
	
	
	@Deprecated
	public boolean saveAgentConfigFromUI(String agentId, String toolCategory, String toolName, 
			JsonObject agentJson, boolean isDataUpdateSupported, String uniqueKey, String agentVersion, String osversion, Date updateDate) {
		
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig a WHERE a.toolName = :toolName  AND a.agentKey = :agentId",
				AgentConfig.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("agentId", agentId);
		List<AgentConfig> resultList = createQuery.getResultList();
		AgentConfig agentConfig = null;
		if(!resultList.isEmpty()){
			agentConfig = resultList.get(0);
		}
		getSession().beginTransaction();
		if (agentConfig != null) {
			setAgentConfigValues(agentConfig, toolCategory, agentId, toolName, agentJson, isDataUpdateSupported, uniqueKey, agentVersion, osversion, updateDate);
			getSession().update(agentConfig);
		} else {
			agentConfig = new AgentConfig();
			setAgentConfigValues(agentConfig, toolCategory, agentId, toolName, agentJson, isDataUpdateSupported, uniqueKey, agentVersion, osversion, updateDate);
			getSession().save(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
	
	/**
	 * New method 
	 * @param agentId
	 * @param toolCategory
	 * @param toolName
	 * @param agentJson
	 * @param agentVersion
	 * @param osversion
	 * @param updateDate
	 * @return
	 */
	public boolean saveAgentConfigFromUI(String agentId, String toolCategory, String toolName, 
			JsonObject agentJson, String agentVersion, String osversion, Date updateDate) {
		
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig a WHERE a.toolName = :toolName  AND a.agentKey = :agentId",
				AgentConfig.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("agentId", agentId);
		List<AgentConfig> resultList = createQuery.getResultList();
		AgentConfig agentConfig = null;
		if(!resultList.isEmpty()){
			agentConfig = resultList.get(0);
		}
		getSession().beginTransaction();
		if (agentConfig != null) {
			setAgentConfigValues(agentConfig, toolCategory, agentId, toolName, agentJson, agentVersion, osversion, updateDate);
			getSession().update(agentConfig);
		} else {
			agentConfig = new AgentConfig();
			setAgentConfigValues(agentConfig, toolCategory, agentId, toolName, agentJson, agentVersion, osversion, updateDate);
			getSession().save(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
	
	@Deprecated
	private void setAgentConfigValues(AgentConfig agentConfig,String toolCategory,String agentId, String toolName, JsonObject agentJson, 
			boolean isDataUpdateSupported, String uniqueKey, String agentVersion, String osversion, Date updateDate) {
		
		agentConfig.setToolCategory(toolCategory.toUpperCase());
		agentConfig.setToolName(toolName);
		agentConfig.setDataUpdateSupported(isDataUpdateSupported);
		agentConfig.setAgentJson(agentJson.toString());
		agentConfig.setOsVersion(osversion);
		agentConfig.setAgentVersion(agentVersion);
		agentConfig.setUpdatedDate(updateDate);
		agentConfig.setUniqueKey(uniqueKey);
		agentConfig.setAgentKey(agentId);
		agentConfig.setAgentStatus(AGENTACTION.START.name());
	}

	private void setAgentConfigValues(AgentConfig agentConfig,String toolCategory,String agentId, String toolName, JsonObject agentJson, 
			String agentVersion, String osversion, Date updateDate) {
		
		agentConfig.setToolCategory(toolCategory.toUpperCase());
		agentConfig.setToolName(toolName);
		agentConfig.setAgentJson(agentJson.toString());
		agentConfig.setOsVersion(osversion);
		agentConfig.setAgentVersion(agentVersion);
		agentConfig.setUpdatedDate(updateDate);
		agentConfig.setAgentKey(agentId);
		agentConfig.setAgentStatus(AGENTACTION.START.name());
	}

	// for update action i.e. Insert
	public boolean saveAgentConfigurationData(int agentId, String toolName, String toolCategory, JsonObject agentJson, 
			boolean isDataUpdateSupported, String uniqueKey) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory AND a.agentId = :agentId",
				AgentConfig.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		createQuery.setParameter("agentId", agentId);
		List<AgentConfig> resultList = createQuery.getResultList();
		AgentConfig agentConfig = null;
		if(resultList.size()>0){
			agentConfig = resultList.get(0);
		}
		getSession().beginTransaction();
		if (agentConfig != null) {
			agentConfig.setAgentJson(agentJson.toString());
			agentConfig.setDataUpdateSupported(isDataUpdateSupported);
			agentConfig.setUniqueKey(uniqueKey);
			if(agentConfig.getAgentKey() == null) {
				agentConfig.setAgentKey(toolName + "-" + Instant.now().toEpochMilli());
			}
			getSession().update(agentConfig);
		} else {
			agentConfig = new AgentConfig();
			agentConfig.setAgentId(agentId);
			agentConfig.setAgentKey(toolName + "-" + Instant.now().toEpochMilli());
			agentConfig.setToolName(toolName);
			agentConfig.setToolCategory(toolCategory);
			agentConfig.setAgentJson(agentJson.toString());
			agentConfig.setDataUpdateSupported(isDataUpdateSupported);
			agentConfig.setUniqueKey(uniqueKey);
			getSession().save(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	// for read action i.e. select query
	public List<AgentConfig> getAgentConfigurations(String toolName, String toolCategory) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig AC WHERE AC.toolName = :toolName AND AC.toolCategory = :toolCategory",
				AgentConfig.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		List<AgentConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
	public AgentConfig updateAgentRunningStatus(String agentId,AGENTACTION action) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
				AgentConfig.class);
		createQuery.setParameter("agentKey", agentId);
		AgentConfig agentConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		if(agentConfig != null) {
			agentConfig.setAgentStatus(action.name());
			getSession().update(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return agentConfig;
	}

	public List<AgentConfig> deleteAgentConfigurations(String agentKey) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig a WHERE a.agentKey = :agentKey",
				AgentConfig.class);
		createQuery.setParameter("agentKey", agentKey);
		AgentConfig agentConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(agentConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return getAllDataAgentConfigurations();
	}
	
	public AgentConfig getAgentConfigurations(String agentId) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
				AgentConfig.class);
		createQuery.setParameter("agentKey", agentId);
		AgentConfig result = createQuery.getSingleResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public List<AgentConfig> getAllAgentConfigurations() {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig AC", AgentConfig.class);
		List<AgentConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
	public List<AgentConfig> getAllDataAgentConfigurations() {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig AC WHERE AC.toolCategory != 'DAEMONAGENT' AND AC.toolName != 'AGENTDAEMON'", AgentConfig.class);
		List<AgentConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public boolean updateAgentSubscriberConfigurations(List<AgentConfig> agentConfigs) {
		getSession().beginTransaction();
		for(AgentConfig agentConfig : agentConfigs){
			getSession().update(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	// for delete action i.e. delete query
	public boolean deleteAgentConfigurations(String toolName, String toolCategory, int agentId) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory AND a.agentId = :agentId",
				AgentConfig.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		createQuery.setParameter("agentId", agentId);
		AgentConfig agentConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(agentConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public AgentConfig downloadAgentConfigurations(String toolName, String toolCategory, int agentId) {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory AND a.agentId = :agentId",
				AgentConfig.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		createQuery.setParameter("agentId", agentId);
		AgentConfig result = createQuery.getSingleResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

}