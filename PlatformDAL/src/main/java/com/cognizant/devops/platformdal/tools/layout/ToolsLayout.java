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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TOOLS_LAYOUT")
public class ToolsLayout {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name = "SETTINGS_JSON", length = 16384)
	private String settingsJson;
	
	@Column(name = "TOOL_NAME")
	private String toolName;

	@Column(name = "TOOL_CATEGORY")
	private String toolCategory;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSettingsJson() {
		return settingsJson;
	}

	public void setSettingsJson(String settingsJson) {
		this.settingsJson = settingsJson;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getToolCategory() {
		return toolCategory;
	}

	public void setToolCategory(String toolCategory) {
		this.toolCategory = toolCategory;
	}
}
