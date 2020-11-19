/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformreports.assessment.datamodel;

public class InsightsReportPdfTableConfig {
	
	//Common font
	private String font="";
	
	//Table caption
	private String tableCaptionFontSize="20";
	
	//TableHeader
	private String tableHeaderFillColor="";
	private String tableHeaderTextColor="";
	private String tableHeaderFontSize="14";
	
	//TableRows
	private String tableRowsFontSize="10";
	
	//Observations
	private String observationFontSize="15";
	private String observationListFontSize="10";

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public String getTableCaptionFontSize() {
		return tableCaptionFontSize;
	}

	public void setTableCaptionFontSize(String tableCaptionFontSize) {
		this.tableCaptionFontSize = tableCaptionFontSize;
	}

	public String getTableHeaderFillColor() {
		return tableHeaderFillColor;
	}

	public void setTableHeaderFillColor(String tableHeaderFillColor) {
		this.tableHeaderFillColor = tableHeaderFillColor;
	}

	public String getTableHeaderTextColor() {
		return tableHeaderTextColor;
	}

	public void setTableHeaderTextColor(String tableHeaderTextColor) {
		this.tableHeaderTextColor = tableHeaderTextColor;
	}

	public String getTableHeaderFontSize() {
		return tableHeaderFontSize;
	}

	public void setTableHeaderFontSize(String tableHeaderFontSize) {
		this.tableHeaderFontSize = tableHeaderFontSize;
	}

	public String getTableRowsFontSize() {
		return tableRowsFontSize;
	}

	public void setTableRowsFontSize(String tableRowsFontSize) {
		this.tableRowsFontSize = tableRowsFontSize;
	}

	public String getObservationFontSize() {
		return observationFontSize;
	}

	public void setObservationFontSize(String observationFontSize) {
		this.observationFontSize = observationFontSize;
	}

	public String getObservationListFontSize() {
		return observationListFontSize;
	}

	public void setObservationListFontSize(String observationListFontSize) {
		this.observationListFontSize = observationListFontSize;
	}
	
	
	
}
