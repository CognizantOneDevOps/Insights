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

import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.core.enums.AGENTACTION;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;

public class AgentConfigDAL extends BaseDAL {

	/**
	 * New method for updating the existing agent info in database.
	 * 
	 * @param agentId
	 * @param toolCategory
	 * @param toolName
	 * @param agentJson
	 * @param agentVersion
	 * @param osversion
	 * @param updateDate
	 * @param vault
	 * @return
	 */
	public boolean updateAgentConfigFromUI(String agentId, String toolCategory, String labelName, String toolName,
			JsonObject agentJson, String agentVersion, String osversion, Date updateDate, boolean vault) {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig a WHERE a.agentKey = :agentId ",
				AgentConfig.class);
		createQuery.setParameter("agentId", agentId);
		AgentConfig agentConfig = createQuery.uniqueResult();
		if (agentConfig != null) {
			getSession().beginTransaction();
			setAgentConfigValues(agentConfig, toolCategory, labelName, agentId, toolName, agentJson, agentVersion,
					osversion, updateDate, vault);
			getSession().update(agentConfig);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	/**
	 * Method to check whether the agent id is existing in the Database
	 * 
	 * @param agentId
	 * @return
	 */
	public boolean isAgentIdExisting(String agentId) {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig a WHERE a.agentKey = :agentId ",
				AgentConfig.class);
		createQuery.setParameter("agentId", agentId);
		if (createQuery.getResultList().isEmpty()) {
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}
	}

	/**
	 * Method Called for saving agent's info in database
	 * 
	 * @param agentId
	 * @param toolCategory
	 * @param labelName
	 * @param toolName
	 * @param agentJson
	 * @param agentVersion
	 * @param osversion
	 * @param updateDate
	 * @param vault
	 * @return
	 */
	public boolean saveAgentConfigFromUI(String agentId, String toolCategory, String labelName, String toolName,
			JsonObject agentJson, String agentVersion, String osversion, Date updateDate, boolean vault) {
		AgentConfig agentConfig = new AgentConfig();
		getSession().beginTransaction();
		setAgentConfigValues(agentConfig, toolCategory, labelName, agentId, toolName, agentJson, agentVersion,
				osversion, updateDate, vault);
		getSession().save(agentConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}

	private void setAgentConfigValues(AgentConfig agentConfig, String toolCategory, String labelName, String agentId,
			String toolName, JsonObject agentJson, String agentVersion, String osversion, Date updateDate,
			boolean vault) {

		agentConfig.setToolCategory(toolCategory.toUpperCase());
		agentConfig.setToolName(toolName);
		agentConfig.setLabelName(labelName.toUpperCase());
		agentConfig.setAgentJson(agentJson.toString());
		agentConfig.setOsVersion(osversion);
		agentConfig.setAgentVersion(agentVersion);
		agentConfig.setUpdatedDate(updateDate);
		agentConfig.setAgentKey(agentId);
		agentConfig.setAgentStatus(AGENTACTION.START.name());
		agentConfig.setVault(vault);
	}

	/**
	 * for read action i.e. select query
	 * 
	 * @param toolName
	 * @param toolCategory
	 * @return
	 */
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

	public AgentConfig updateAgentRunningStatus(String agentId, AGENTACTION action) {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
				AgentConfig.class);
		createQuery.setParameter("agentKey", agentId);
		AgentConfig agentConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		if (agentConfig != null) {
			agentConfig.setAgentStatus(action.name());
			getSession().update(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return agentConfig;
	}

	public List<AgentConfig> deleteAgentConfigurations(String agentKey) {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig a WHERE a.agentKey = :agentKey",
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
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
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

	public List<AgentConfig> getAllEngineAggregatorAgentConfigurations() {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig AC WHERE AC.toolCategory != '"
				+ DataArchivalConstants.TOOLCATEGORY + "'",
				AgentConfig.class);
		List<AgentConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public List<AgentConfig> getAllDataAgentConfigurations() {
		Query<AgentConfig> createQuery = getSession().createQuery(
				"FROM AgentConfig AC WHERE AC.toolCategory != 'DAEMONAGENT' AND AC.toolName != 'AGENTDAEMON'",
				AgentConfig.class);
		List<AgentConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public boolean updateAgentSubscriberConfigurations(List<AgentConfig> agentConfigs) {
		getSession().beginTransaction();
		for (AgentConfig agentConfig : agentConfigs) {
			getSession().update(agentConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	/**
	 * for delete action i.e. delete query
	 * 
	 * @param toolName
	 * @param toolCategory
	 * @param agentId
	 * @return
	 */
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