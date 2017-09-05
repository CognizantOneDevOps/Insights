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
package com.cognizant.devops.platformdal.data.tagging;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DATA_TAGGING")
public class DataTagging {
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@Column(name ="ROW_ID")
	private int rowId;

	@Column(name = "LEVEL1")
	private String level1;

	@Column(name = "LEVEL2")
	private String level2;

	@Column(name = "LEVEL3")
	private String level3;

	@Column(name = "LEVEL4")
	private String level4;

	@Column(name = "LEVEL5")
	private String level5;

	@Column(name = "LEVEL6")
	private String level6;

	@Column(name = "HIERARCHY_NAME")
	private String hierarchyName;
	
	@Column(name = "CYPHER_QUERY")
	private String cypherQuery;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLevel1() {
		return level1;
	}

	public void setLevel1(String level1) {
		this.level1 = level1;
	}

	public String getLevel2() {
		return level2;
	}

	public void setLevel2(String level2) {
		this.level2 = level2;
	}

	public String getLevel3() {
		return level3;
	}

	public void setLevel3(String level3) {
		this.level3 = level3;
	}

	public String getLevel4() {
		return level4;
	}

	public void setLevel4(String level4) {
		this.level4 = level4;
	}

	public String getLevel5() {
		return level5;
	}

	public void setLevel5(String level5) {
		this.level5 = level5;
	}

	public String getLevel6() {
		return level6;
	}

	public void setLevel6(String level6) {
		this.level6 = level6;
	}

	public String getHierarchyName() {
		return hierarchyName;
	}

	public void setHierarchyName(String hierarchyName) {
		this.hierarchyName = hierarchyName;
	}

	public String getCypherQuery() {
		return cypherQuery;
	}

	public void setCypherQuery(String cypherQuery) {
		this.cypherQuery = cypherQuery;
	}

	public int getRowId() {
		return rowId;
	}

	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
}
