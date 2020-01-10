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
package com.cognizant.devops.platformservice.correlationbuilder.service;

public class CorrelationJson {
	private CorrelationNode  source;
	private CorrelationNode  destination;
	private String relationName;
	private String[] relationship_properties;
	private boolean enableCorrelation= true;
	
	
	public CorrelationNode getSource() {
		return source;
	}
	public void setSource(CorrelationNode source) {
		this.source = source;
	}
	public CorrelationNode getDestination() {
		return destination;
	}
	public void setDestination(CorrelationNode destination) {
		this.destination = destination;
	}
	public String getRelationName() {
		return relationName;
	}
	public void setPropertyList(String[] relationship_properties) {
		this.relationship_properties = relationship_properties;
	}
	public String[] getPropertyList() {
		return relationship_properties;
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
	
	@Override
	public String toString() {
		return "CorrelationJson [source=" + source + ", destination=" + destination + ", relationName=" + relationName
				+ ", correlationFlag=" + enableCorrelation + ", getSource()=" + getSource() + ", getDestination()="
				+ getDestination() + ", getRelationName()=" + getRelationName() + ", getPropertyList()=" + getPropertyList() + ", isCorrelationFlag()="
				+ isEnableCorrelation() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	
	}
