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

package com.cognizant.devops.platformdal.relationshipconfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;

@Entity
@Table(name = "\"INSIGHTS_RELATIONSHIP_CONFIGURATIONS\"")
public class RelationshipConfiguration {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "RID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int rid;

	@Column(name = "RELATION_NAME")
	String relationname;

	@Column(name = "OPERATION_JSON")
	String operationjson;

	@Column(name = "OPERATION")
	String operation;

	@Column(name = "RELATIONSHIP_FIELD_VALUE")
	String fieldValue;

	@ManyToOne
	CorrelationConfiguration correlationConfig;

	public String getRelationname() {
		return relationname;
	}

	public void setRelationname(String relationname) {
		this.relationname = relationname;
	}

	public String getOperationjson() {
		return operationjson;
	}

	public void setOperationjson(String operationjson) {
		this.operationjson = operationjson;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public CorrelationConfiguration getCorrelationConfig() {
		return correlationConfig;
	}

	public void setCorrelationConfig(CorrelationConfiguration correlationConfig) {
		this.correlationConfig = correlationConfig;
	}

	public int getRid() {
		return rid;
	}

	public void setRid(int rid) {
		this.rid = rid;
	}

}
