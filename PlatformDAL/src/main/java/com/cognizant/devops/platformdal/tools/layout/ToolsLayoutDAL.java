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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;

public class ToolsLayoutDAL extends BaseDAL{

	private static Logger log = LogManager.getLogger(ToolsLayoutDAL.class);
	
	public boolean saveToolLayout(String toolName, String toolCategory, JsonObject toolLayoutSettingJson) {
		try (Session session = getSessionObj()) {
			Query<ToolsLayout> createQuery = session.createQuery(
					"FROM ToolsLayout a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory",
					ToolsLayout.class);
			createQuery.setParameter(AgentCommonConstant.TOOLNAME, toolName);
			createQuery.setParameter(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			List<ToolsLayout> resultList = createQuery.getResultList();
			ToolsLayout toolLayout = null;
			if (resultList.size() > 0) {
				toolLayout = resultList.get(0);
			}
			session.beginTransaction();
			if (toolLayout != null) {
				toolLayout.setSettingsJson(toolLayoutSettingJson.toString());
				session.update(toolLayout);
			} else {
				toolLayout = new ToolsLayout();
				toolLayout.setToolName(toolName);
				toolLayout.setToolCategory(toolCategory);
				toolLayout.setSettingsJson(toolLayoutSettingJson.toString());
				session.save(toolLayout);
			}
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public ToolsLayout getToolLayout(String toolName, String toolCategory) {
		
		try (Session session = getSessionObj()) {
		Query<ToolsLayout> createQuery = session.createQuery(
				"FROM ToolsLayout TL WHERE TL.toolName = :toolName AND TL.toolCategory = :toolCategory",
				ToolsLayout.class);
		createQuery.setParameter(AgentCommonConstant.TOOLNAME, toolName);
		createQuery.setParameter(AgentCommonConstant.TOOLCATEGORY, toolCategory);
		ToolsLayout toolLayout = null;
		try{
			toolLayout = createQuery.getSingleResult();
		}catch(Exception e){
			throw new RuntimeException("Exception while retrieving data"+e);
		}
		return toolLayout;
		}
		 catch (Exception e) {
				log.error(e.getMessage());
				throw e;
			}
	}
	
	public List<ToolsLayout> getAllToolLayouts() {
		
		try (Session session = getSessionObj()) {
			Query<ToolsLayout> createQuery = session.createQuery("FROM ToolsLayout", ToolsLayout.class);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteToolLayout(String toolName, String toolCategory, int agentId) {
		
		try (Session session = getSessionObj()) {
			Query<ToolsLayout> createQuery = session.createQuery(
					"FROM ToolsLayout TL WHERE TL.toolName = :toolName AND TL.toolCategory = :toolCategory",
					ToolsLayout.class);
			createQuery.setParameter(AgentCommonConstant.TOOLNAME, toolName);
			createQuery.setParameter(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			ToolsLayout toolLayout = createQuery.getSingleResult();
			session.beginTransaction();
			session.delete(toolLayout);
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public List<String> getDistinctToolNames() {
		try (Session session = getSessionObj()) {
			Query<String> createQuery = session.createQuery("SELECT DISTINCT TL.toolName FROM ToolsLayout TL",
					String.class);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	
}
