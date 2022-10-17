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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformcommons.constants.DataArchivalConstants;
import com.cognizant.devops.platformcommons.core.enums.AGENTACTION;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;

public class AgentConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(AgentConfigDAL.class);

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
			JsonObject agentJson, String agentVersion, String osversion, Date updateDate, boolean vault,
			boolean isWebhook) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.AGENTID, agentId);
			AgentConfig agentConfig = getUniqueResult("FROM AgentConfig a WHERE a.agentKey = :agentId",
					AgentConfig.class, parameters);

			if (agentConfig != null) {
				setAgentConfigValues(agentConfig, toolCategory, labelName, agentId, toolName, agentJson, agentVersion,
						osversion, updateDate, vault, isWebhook);
				update(agentConfig);
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Method to check whether the agent id is existing in the Database
	 * 
	 * @param agentId
	 * @return
	 */
	public boolean isAgentIdExisting(String agentId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.AGENTID, agentId);
			List<AgentConfig> agentList = getResultList("FROM AgentConfig a WHERE a.agentKey = :agentId  ",
					AgentConfig.class, parameters);
			if (agentList.isEmpty()) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
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
			JsonObject agentJson, String agentVersion, String osversion, Date updateDate, boolean vault,
			boolean isWebhook) {
		AgentConfig agentConfig = new AgentConfig();
		try {
			setAgentConfigValues(agentConfig, toolCategory, labelName, agentId, toolName, agentJson, agentVersion,
					osversion, updateDate, vault, isWebhook);
			save(agentConfig);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		return Boolean.TRUE;
	}

	private void setAgentConfigValues(AgentConfig agentConfig, String toolCategory, String labelName, String agentId,
			String toolName, JsonObject agentJson, String agentVersion, String osversion, Date updateDate,
			boolean vault, boolean isWebhook) {

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
		agentConfig.setIswebhook(isWebhook);
	}

	/**
	 * for read action i.e. select query
	 * 
	 * @param toolName
	 * @param toolCategory
	 * @return
	 */
	public List<AgentConfig> getAgentConfigurations(String toolName, String toolCategory) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.TOOLNAME, toolName);
			parameters.put(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			return getResultList(
					"FROM AgentConfig AC WHERE AC.toolName = :toolName AND AC.toolCategory = :toolCategory ",
					AgentConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public AgentConfig updateAgentRunningStatus(String agentId, AGENTACTION action) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.AGENTKEY, agentId);
			AgentConfig agentConfig = getSingleResult("FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
					AgentConfig.class, parameters);
			if (agentConfig != null) {
				agentConfig.setAgentStatus(action.name());
				update(agentConfig);
			}
			return agentConfig;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> deleteAgentConfigurations(String agentKey) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.AGENTKEY, agentKey);
			AgentConfig agentConfig = getSingleResult("FROM AgentConfig a WHERE a.agentKey = :agentKey",
					AgentConfig.class, parameters);
			delete(agentConfig);
			return getAllDataAgentConfigurations();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public AgentConfig getAgentConfigurations(String agentId) {

		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.AGENTKEY, agentId);
			return getSingleResult("FROM AgentConfig AC WHERE AC.agentKey = :agentKey", AgentConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> getAllAgentConfigurations() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM AgentConfig AC", AgentConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> getAllEngineAggregatorAgentConfigurations() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList(
					"FROM AgentConfig AC WHERE AC.toolCategory != '" + DataArchivalConstants.TOOLCATEGORY + "'",
					AgentConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> getAllDataAgentConfigurations() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList(
					"FROM AgentConfig AC WHERE AC.toolCategory != 'DAEMONAGENT' AND AC.toolName != 'AGENTDAEMON' ORDER By AC.toolName, AC.agentKey",
					AgentConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
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
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("toolName", toolName);
			parameters.put("toolCategory", toolCategory);
			parameters.put("agentId", agentId);
			AgentConfig agentConfig = getSingleResult(
					"FROM AgentConfig a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory AND a.agentId = :agentId",
					AgentConfig.class, parameters);
			delete(agentConfig);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}