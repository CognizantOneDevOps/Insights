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
	private String sourceToolName = null;
	private String sourceToolCategory = null;
	private String sourceLabelName;
	private String destinationToolName;
	private String destinationToolCategory;
	private String destinationLabelName;
	private List<String> sourceFields;
	private List<String> destinationFields;
	private String toolCategory = null;
	private boolean enrichAlmData = false;
	private String almKeyProcessedIndex = "jiraKeyProcessed";
	private String almKeysIndex = "jiraKeys";

	public List<String> getSourceFields() {
		return sourceFields;
	}

	public void setSourceFields(List<String> sourceFields) {
		this.sourceFields = sourceFields;
	}

	public List<String> getDestinationFields() {
		return destinationFields;
	}

	public void setDestinationFields(List<String> destinationFields) {
		this.destinationFields = destinationFields;
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

	public void setSourceToolCategory(String sourcetoolCategory) {
		this.sourceToolCategory = sourcetoolCategory;
	}

	public String getSourceLabelName() {
		return sourceLabelName;
	}

	public void setSourceLabelName(String sourceLabelName) {
		this.sourceLabelName = sourceLabelName;
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

	@Override
	public String toString() {
		return "CorrelationNode [sourceToolName=" + sourceToolName + ", sourceToolCategory=" + sourceToolCategory
				+ ", sourceLabelName=" + sourceLabelName + ", sourceFields=" + sourceFields + ", destinationToolName="
				+ destinationToolName + ", destinationToolCategory=" + destinationToolCategory
				+ ", destinationLabelName=" + destinationLabelName + ", destinationFields=" + destinationFields
				+ ", toolCategory=" + toolCategory + ", enrichAlmData=" + enrichAlmData + ", almKeyProcessedIndex="
				+ almKeyProcessedIndex + ", almKeysIndex=" + almKeysIndex + "]";
	}
}
