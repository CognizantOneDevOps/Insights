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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

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
			JsonObject agentJson, String agentVersion, String osversion, Date updateDate, boolean vault) {
		
		
		try (Session session = getSessionObj()) {

			Query<AgentConfig> createQuery = session.createQuery("FROM AgentConfig a WHERE a.agentKey = :agentId ",
					AgentConfig.class);
			createQuery.setParameter(AgentCommonConstant.AGENTID, agentId);
			AgentConfig agentConfig = createQuery.uniqueResult();
			if (agentConfig != null) {
				session.beginTransaction();
				setAgentConfigValues(agentConfig, toolCategory, labelName, agentId, toolName, agentJson, agentVersion,
						osversion, updateDate, vault);
				session.update(agentConfig);
				session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery("FROM AgentConfig a WHERE a.agentKey = :agentId ",
					AgentConfig.class);
			createQuery.setParameter(AgentCommonConstant.AGENTID, agentId);
			if (createQuery.getResultList().isEmpty()) {
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
		JsonObject agentJson, String agentVersion, String osversion, Date updateDate, boolean vault) {
		AgentConfig agentConfig = new AgentConfig();
		try (Session session = getSessionObj()) {
		session.beginTransaction();
		setAgentConfigValues(agentConfig, toolCategory, labelName, agentId, toolName, agentJson, agentVersion,
				osversion, updateDate, vault);
		session.save(agentConfig);
		session.getTransaction().commit();
		}catch(Exception e) {
			log.error(e.getMessage());
			throw e;
		}
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
		
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery(
					"FROM AgentConfig AC WHERE AC.toolName = :toolName AND AC.toolCategory = :toolCategory",
					AgentConfig.class);
			createQuery.setParameter(AgentCommonConstant.TOOLNAME, toolName);
			createQuery.setParameter(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}

	public AgentConfig updateAgentRunningStatus(String agentId, AGENTACTION action) {
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery("FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
					AgentConfig.class);
			createQuery.setParameter(AgentCommonConstant.AGENTKEY, agentId);
			AgentConfig agentConfig = createQuery.getSingleResult();
			session.beginTransaction();
			if (agentConfig != null) {
				agentConfig.setAgentStatus(action.name());
				session.update(agentConfig);
			}
			session.getTransaction().commit();

			return agentConfig;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> deleteAgentConfigurations(String agentKey) {
		
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery("FROM AgentConfig a WHERE a.agentKey = :agentKey",
					AgentConfig.class);
			createQuery.setParameter(AgentCommonConstant.AGENTKEY, agentKey);
			AgentConfig agentConfig = createQuery.getSingleResult();
			session.beginTransaction();
			session.delete(agentConfig);
			session.getTransaction().commit();
			return getAllDataAgentConfigurations();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public AgentConfig getAgentConfigurations(String agentId) {
		
		try (Session session = getSessionObj()) {

			Query<AgentConfig> createQuery = session.createQuery("FROM AgentConfig AC WHERE AC.agentKey = :agentKey",
					AgentConfig.class);
			createQuery.setParameter(AgentCommonConstant.AGENTKEY, agentId);
			AgentConfig result = createQuery.getSingleResult();
			return result;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> getAllAgentConfigurations() {
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery("FROM AgentConfig AC", AgentConfig.class);
			return createQuery.getResultList();
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> getAllEngineAggregatorAgentConfigurations() {
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery(
					"FROM AgentConfig AC WHERE AC.toolCategory != '" + DataArchivalConstants.TOOLCATEGORY + "'",
					AgentConfig.class);
			return createQuery.getResultList();

		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<AgentConfig> getAllDataAgentConfigurations() {
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery(
					"FROM AgentConfig AC WHERE AC.toolCategory != 'DAEMONAGENT' AND AC.toolName != 'AGENTDAEMON'",
					AgentConfig.class);
			return createQuery.getResultList();

		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean updateAgentSubscriberConfigurations(List<AgentConfig> agentConfigs) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			for (AgentConfig agentConfig : agentConfigs) {
				session.update(agentConfig);
			}
			session.getTransaction().commit();
			return true;
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
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery(
					"FROM AgentConfig a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory AND a.agentId = :agentId",
					AgentConfig.class);
			createQuery.setParameter("toolName", toolName);
			createQuery.setParameter("toolCategory", toolCategory);
			createQuery.setParameter("agentId", agentId);
			AgentConfig agentConfig = createQuery.getSingleResult();
			session.beginTransaction();
			session.delete(agentConfig);
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public AgentConfig downloadAgentConfigurations(String toolName, String toolCategory, int agentId) {
		try (Session session = getSessionObj()) {
			Query<AgentConfig> createQuery = session.createQuery(
					"FROM AgentConfig a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory AND a.agentId = :agentId",
					AgentConfig.class);
			createQuery.setParameter("toolName", toolName);
			createQuery.setParameter("toolCategory", toolCategory);
			createQuery.setParameter("agentId", agentId);
			return createQuery.getSingleResult();

		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}