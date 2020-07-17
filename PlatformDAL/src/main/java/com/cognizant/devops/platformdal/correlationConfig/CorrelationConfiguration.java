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
package com.cognizant.devops.platformdal.correlationConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.cognizant.devops.platformdal.relationshipconfig.RelationshipConfiguration;

@Entity
@Table(name = "\"INSIGHTS_CORRELATION_CONFIGURATIONS\"")
public class CorrelationConfiguration {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "RELATIONSHIP_ID")
	private Set<RelationshipConfiguration> relationshipConfig = new HashSet<>();

	@Column(name = "DESTINATION_TOOLNAME")
	private String destinationToolName;

	@Column(name = "DESTINATION_TOOLCATEGORY")
	private String destinationToolCategory;

	@Column(name = "DESTINATION_LABELNAME")
	private String destinationLabelName;

	@Column(name = "DESTINATION_FIELDS")
	private String destinationFields;

	@Column(name = "RELATIONSHIP_PROPERTY_FIELDS")
	private String relationship_properties;

	@Column(name = "SOURCE_TOOLNAME")
	private String sourceToolName;

	@Column(name = "SOURCE_TOOLCATEGORY")
	private String sourceToolCategory;

	@Column(name = "SOURCE_LABELNAME")
	private String sourceLabelName;

	@Column(name = "SOURCE_FIELDS")
	private String sourceFields;

	@Column(name = "ENABLE_CORRELATION")
	private boolean enableCorrelation;

	@Column(name = "RELATION_NAME", unique = true, nullable = false)
	private String relationName;

	@Column(name = "IS_SELF_TOOL_RELATION")
	private boolean isSelfRelation = false;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDestinationToolName() {
		return destinationToolName;
	}

	public void setDestinationToolName(String destinationToolName) {
		this.destinationToolName = destinationToolName;
	}

	public String getDestinationToolCategory() {
		return destinationToolCategory;
	}

	public void setDestinationToolCategory(String destinationToolCategory) {
		this.destinationToolCategory = destinationToolCategory;
	}

	public String getDestinationLabelName() {
		return destinationLabelName;
	}

	public void setDestinationLabelName(String destinationLabelName) {
		this.destinationLabelName = destinationLabelName;
	}

	public String getDestinationFields() {
		return destinationFields;
	}

	public void setDestinationFields(String destinationFields) {
		this.destinationFields = destinationFields;
	}

	public void setPropertyList(String relationship_properties) {
		this.relationship_properties = relationship_properties;
	}

	public String getPropertyList() {
		return relationship_properties;
	}

	public String getSourceToolName() {
		return sourceToolName;
	}

	public void setSourceToolName(String sourceToolName) {
		this.sourceToolName = sourceToolName;
	}

	public String getSourceToolCategory() {
		return sourceToolCategory;
	}

	public void setSourceToolCategory(String sourceToolCategory) {
		this.sourceToolCategory = sourceToolCategory;
	}

	public String getSourceLabelName() {
		return sourceLabelName;
	}

	public void setSourceLabelName(String sourceLabelName) {
		this.sourceLabelName = sourceLabelName;
	}

	public String getSourceFields() {
		return sourceFields;
	}

	public void setSourceFields(String sourceFields) {
		this.sourceFields = sourceFields;
	}

	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	public boolean isEnableCorrelation() {
		return enableCorrelation;
	}

	public void setEnableCorrelation(boolean enableCorrelation) {
		this.enableCorrelation = enableCorrelation;
	}

	public boolean isSelfRelation() {
		return isSelfRelation;
	}

	public void setSelfRelation(boolean isSelfRelation) {
		this.isSelfRelation = isSelfRelation;
	}

	public Set<RelationshipConfiguration> getRelationshipConfig() {
		return relationshipConfig;
	}

	public void setRelationshipConfig(Set<RelationshipConfiguration> relationshipConfig) {
		this.relationshipConfig = relationshipConfig;
	}

}
