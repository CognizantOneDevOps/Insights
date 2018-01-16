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
package com.cognizant.devops.platformdal.hierarchy.details;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "HIERARCHY_DETAILS")
public class HierarchyDetails {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	//@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name ="ROW_ID")
	private int rowId;

	@Column(name = "LEVEL_1")
	private String level_1;

	@Column(name = "LEVEL_2")
	private String level_2;

	@Column(name = "LEVEL_3")
	private String level_3;

	@Column(name = "LEVEL_4")
	private String level_4;

	@Column(name = "LEVEL_5")
	private String level_5;

	@Column(name = "LEVEL_6")
	private String level_6;

	@Column(name = "HIERARCHY_NAME")
	private String hierarchyName;
	
	@Column(name = "TOOLNAME")
	private String toolName;
	
	@Column(name = "LABELS")
	private String labels;

	@Column(name = "PROPERTY")
	private String toolProperty;
	
	@Column(name = "VALUE")
	private String propertyValue;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}

	public String getLevel_1() {
		return level_1;
	}

	public void setLevel_1(String level_1) {
		this.level_1 = level_1;
	}

	public String getLevel_2() {
		return level_2;
	}

	public void setLevel_2(String level_2) {
		this.level_2 = level_2;
	}

	public String getLevel_3() {
		return level_3;
	}

	public void setLevel_3(String level_3) {
		this.level_3 = level_3;
	}

	public String getLevel_4() {
		return level_4;
	}

	public void setLevel_4(String level_4) {
		this.level_4 = level_4;
	}

	public String getLevel_5() {
		return level_5;
	}

	public void setLevel_5(String level_5) {
		this.level_5 = level_5;
	}

	public String getLevel_6() {
		return level_6;
	}

	public void setLevel_6(String level_6) {
		this.level_6 = level_6;
	}

	public String getHierarchyName() {
		return hierarchyName;
	}

	public void setHierarchyName(String hierarchyName) {
		this.hierarchyName = hierarchyName;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getToolProperty() {
		return toolProperty;
	}

	public void setToolProperty(String toolProperty) {
		this.toolProperty = toolProperty;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}


}
