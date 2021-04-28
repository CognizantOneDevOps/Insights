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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.AgentCommonConstant;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.google.gson.JsonObject;

public class ToolsLayoutDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(ToolsLayoutDAL.class);

	public boolean saveToolLayout(String toolName, String toolCategory, JsonObject toolLayoutSettingJson) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.TOOLNAME, toolName);
			parameters.put(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			List<ToolsLayout> resultList = getResultList(
					"FROM ToolsLayout a WHERE a.toolName = :toolName AND a.toolCategory = :toolCategory",
					ToolsLayout.class, parameters);
			ToolsLayout toolLayout = null;
			if (!resultList.isEmpty()) {
				toolLayout = resultList.get(0);
			}
			if (toolLayout != null) {
				toolLayout.setSettingsJson(toolLayoutSettingJson.toString());
				update(toolLayout);
			} else {
				toolLayout = new ToolsLayout();
				toolLayout.setToolName(toolName);
				toolLayout.setToolCategory(toolCategory);
				toolLayout.setSettingsJson(toolLayoutSettingJson.toString());
				save(toolLayout);
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public ToolsLayout getToolLayout(String toolName, String toolCategory) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.TOOLNAME, toolName);
			parameters.put(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			return getSingleResult(
					"FROM ToolsLayout TL WHERE TL.toolName = :toolName AND TL.toolCategory = :toolCategory",
					ToolsLayout.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<ToolsLayout> getAllToolLayouts() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM ToolsLayout",
					ToolsLayout.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteToolLayout(String toolName, String toolCategory, int agentId) {

		try {
			
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(AgentCommonConstant.TOOLNAME, toolName);
			parameters.put(AgentCommonConstant.TOOLCATEGORY, toolCategory);
			ToolsLayout toolLayout = getSingleResult(
					"FROM ToolsLayout TL WHERE TL.toolName = :toolName AND TL.toolCategory = :toolCategory",
					ToolsLayout.class, parameters);
			
			delete(toolLayout);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<String> getDistinctToolNames() {
		try  {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList(
					"SELECT DISTINCT TL.toolName FROM ToolsLayout TL",
					String.class, parameters);

		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}
