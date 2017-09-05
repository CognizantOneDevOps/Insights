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

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;

public class AgentConfigDAL extends BaseDAL {

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
			getSession().update(agentConfig);
		} else {
			agentConfig = new AgentConfig();
			agentConfig.setAgentId(agentId);
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
	
	public List<AgentConfig> getAllAgentConfigurations() {
		Query<AgentConfig> createQuery = getSession().createQuery("FROM AgentConfig AC", AgentConfig.class);
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
