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

import java.util.List;

/**
 * @author Vishal Ganjare (vganjare)
 *
 */
public class CorrelationNode {
	private String toolName;
	private String toolCategory;
	private String labelName ;
	private List<String> fields;
	private boolean enrichAlmData;
	private String almkeyPattern = "-";
	private String almKeyProcessedIndex = "jiraKeyProcessed";
	private String almKeysIndex = "jiraKeys";

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

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public boolean isEnrichAlmData() {
		return enrichAlmData;
	}

	public void setEnrichAlmData(boolean enrichAlmData) {
		this.enrichAlmData = enrichAlmData;
	}

	public String getAlmKeyProcessedIndex() {
		return almKeyProcessedIndex;
	}

	public void setAlmKeyProcessedIndex(String almKeyProcessedIndex) {
		this.almKeyProcessedIndex = almKeyProcessedIndex;
	}

	public String getAlmKeysIndex() {
		return almKeysIndex;
	}

	public void setAlmKeysIndex(String almKeysIndex) {
		this.almKeysIndex = almKeysIndex;
	}

	
	 /** @Override public String toString() { return
	 * "CorrelationNode [sourceToolName=" + sourceToolName + ", sourceToolCategory="
	 * + sourceToolCategory + ", sourceLabelName=" + sourceLabelName +
	 * ", sourceFields=" + sourceFields + ", destinationToolName=" +
	 * destinationToolName + ", destinationToolCategory=" + destinationToolCategory
	 * + ", destinationLabelName=" + destinationLabelName + ", destinationFields=" +
	 * destinationFields + ", toolCategory=" + toolCategory + ", enrichAlmData=" +
	 * enrichAlmData + ", almKeyProcessedIndex=" + almKeyProcessedIndex +
	 * ", almKeysIndex=" + almKeysIndex + "]"; }*/
	 
}
