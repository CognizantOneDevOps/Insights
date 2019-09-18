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
package com.cognizant.devops.platformdal.tools.layout;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;

public class ToolsLayoutDAL extends BaseDAL{
	
	public boolean saveToolLayout(String toolName, String toolCategory, JsonObject toolLayoutSettingJson) {
		Query<ToolsLayout> createQuery = getSession().createQuery(
				"FROM ToolsLayout a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory",
				ToolsLayout.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		List<ToolsLayout> resultList = createQuery.getResultList();
		ToolsLayout toolLayout = null;
		if(resultList.size()>0){
			toolLayout = resultList.get(0);
		}
		getSession().beginTransaction();
		if (toolLayout != null) {
			toolLayout.setSettingsJson(toolLayoutSettingJson.toString());
			getSession().update(toolLayout);
		} else {
			toolLayout = new ToolsLayout();
			toolLayout.setToolName(toolName);
			toolLayout.setToolCategory(toolCategory);
			toolLayout.setSettingsJson(toolLayoutSettingJson.toString());
			getSession().save(toolLayout);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public ToolsLayout getToolLayout(String toolName, String toolCategory) {
		Query<ToolsLayout> createQuery = getSession().createQuery(
				"FROM ToolsLayout TL WHERE TL.toolName = :toolName AND TL.toolCategory = :toolCategory",
				ToolsLayout.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		ToolsLayout toolLayout = null;
		try{
			toolLayout = createQuery.getSingleResult();
		}catch(Exception e){
			throw new RuntimeException("Exception while retrieving data"+e);
		}
		terminateSession();
		terminateSessionFactory();
		return toolLayout;
	}
	
	public List<ToolsLayout> getAllToolLayouts() {
		Query<ToolsLayout> createQuery = getSession().createQuery(
				"FROM ToolsLayout",
				ToolsLayout.class);
		List<ToolsLayout> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public boolean deleteToolLayout(String toolName, String toolCategory, int agentId) {
		Query<ToolsLayout> createQuery = getSession().createQuery(
				"FROM ToolsLayout TL WHERE TL.toolName = :toolName AND TL.toolCategory = :toolCategory",
				ToolsLayout.class);
		createQuery.setParameter("toolName", toolName);
		createQuery.setParameter("toolCategory", toolCategory);
		ToolsLayout toolLayout = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(toolLayout);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
	
	public List<String> getDistinctToolNames() {
		Query<String> createQuery = getSession().createQuery(
				"SELECT DISTINCT TL.toolName FROM ToolsLayout TL",
				String.class);
		List<String> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}
	
	
}
