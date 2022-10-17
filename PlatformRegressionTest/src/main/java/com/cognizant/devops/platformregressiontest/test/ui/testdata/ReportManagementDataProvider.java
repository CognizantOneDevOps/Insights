/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformregressiontest.test.ui.testdata;

import java.io.IOException;

import org.testng.annotations.DataProvider;

import com.cognizant.devops.platformregressiontest.test.ui.utility.ReadExcelData;

public class ReportManagementDataProvider {

	ReadExcelData readExceldata = ReadExcelData.getInstance();

	// KPI
	public static final String KPI_CREATION = "KPI_SCREEN_CREATE_DATA";
	public static final String KPI_EDIT = "KPI_SCREEN_EDIT_DATA";
	public static final String KPI_DELETE = "KPI_SCREEN_DELETE_DATA";
	public static final String KPI_UPLOAD_JSON = "KPI_SCREEN_UPLOAD_JSON";
	public static final String KPI_SEARCH = "KPI_SCREEN_SEARCH";
	public static final String KPI_VALIDATE = "KPI_SCREEN_VALIDATE_DATA";
	public static final String KPI_EDIT_VALIDATE = "KPI_SCREEN_EDIT_VALIDATE_DATA";

	// Content
	public static final String CONTENT_CREATION = "CONTENT_SCREEN_CREATE_DATA";
	public static final String CONTENT_EDIT = "CONTENT_SCREEN_EDIT_DATA";
	public static final String CONTENT_DELETE = "CONTENT_SCREEN_DELETE_DATA";
	public static final String CONTENT_CREATE_VALIDATE = "CONTENT_SCREEN_CREATE_VALIDATE";
	public static final String CONTENT_SCREEN_SEARCH = "CONTENT_SCREEN_SEARCH";
	public static final String CONTENT_UPLOAD_JSON = "CONTENT_SCREEN_UPLOAD_JSON";

	// Report Template
	public static final String REPORT_TEMPLATE_CREATION = "REPORT_SCREEN_CREATE_DATA";
	public static final String REPORT_UPLOAD_JSON = "REPORT_UPLOAD_JSON";
	public static final String REPORT_EDIT = "REPORT_SCREEN_EDIT_DATA";
	public static final String REPORT_DELETE = "REPORT_SCREEN_DELETE_DATA";
	public static final String REPORT_TEMPLATE_CREATE_VALIDATE = "REPORT_CREATE_VALIDATE";
	public static final String REPORT_UPLOAD_CONFIGS = "REPORT_UPLOAD_CONFIG_FILES";
	public static final String REPORT_DETAILS = "REPORT_DETAILS";

	// Assessment Report
	public static final String ASSESSMENT_REPORT_CREATE = "ASSESSMENT_REPORT_CREATE";
	
	//Data Archival
	public static final String DATA_ARCHIVAL="ADD_DATAARCHIVAL";
 

	// KPI Data Provider
	@DataProvider(name = "createKPIdataprovider")
	String[][] getCreateKPIData() throws IOException {

		return (ReadExcelData.readExelData(KPI_CREATION));

	}

	@DataProvider(name = "validateKPIdataprovider")
	String[][] getValidateKPIData() throws IOException {

		return (ReadExcelData.readExelData(KPI_VALIDATE));

	}

	@DataProvider(name = "editKPIdataprovider")
	String[][] getEditKPIData() throws IOException {

		return (ReadExcelData.readExelData(KPI_EDIT));

	}

	@DataProvider(name = "editKPIValidatedataprovider")
	String[][] getEditValidateKPIData() throws IOException {

		return (ReadExcelData.readExelData(KPI_EDIT_VALIDATE));

	}

	@DataProvider(name = "deleteKPIdataprovider")
	String[][] getDeleteKPIData() throws IOException {

		return (ReadExcelData.readExelData(KPI_DELETE));

	}

	@DataProvider(name = "uploadJsonKPIdataprovider")
	String[][] getKPIUploadJsonData() throws IOException {

		return (ReadExcelData.readExelData(KPI_UPLOAD_JSON));

	}

	@DataProvider(name = "searchKPIdataprovider")
	String[][] getSearchKPIJsonData() throws IOException {

		return (ReadExcelData.readExelData(KPI_SEARCH));

	}

	// Content Data Provider
	@DataProvider(name = "createContentdataprovider")
	String[][] getCreateContentData() throws IOException {

		return (ReadExcelData.readExelData(CONTENT_CREATION));

	}

	@DataProvider(name = "createContentvalidatedataprovider")
	String[][] getCreateValidateContentData() throws IOException {

		return (ReadExcelData.readExelData(CONTENT_CREATE_VALIDATE));

	}

	@DataProvider(name = "uploadJsonContentdataprovider")
	String[][] getContentUploadJsonData() throws IOException {

		return (ReadExcelData.readExelData(CONTENT_UPLOAD_JSON));

	}

	@DataProvider(name = "searchContentdataprovider")
	String[][] getSearchContentData() throws IOException {

		return (ReadExcelData.readExelData(CONTENT_SCREEN_SEARCH));

	}

	@DataProvider(name = "editContentdataprovider")
	String[][] getEditContentData() throws IOException {

		return (ReadExcelData.readExelData(CONTENT_EDIT));

	}

	@DataProvider(name = "deleteContentdataprovider")
	String[][] getDeleteContentData() throws IOException {

		return (ReadExcelData.readExelData(CONTENT_DELETE));

	}

	// Report Template Data Provider
	@DataProvider(name = "createReportTemplatedataprovider")
	String[][] getCreateReportTemplateData() throws IOException {

		return (ReadExcelData.readExelData(REPORT_TEMPLATE_CREATION));

	}

	@DataProvider(name = "uploadJsonReportTemplatedataprovider")
	String[][] getReportTemplateUploadJsonData() throws IOException {

		return (ReadExcelData.readExelData(REPORT_UPLOAD_JSON));

	}

	@DataProvider(name = "editReportTemplatedataprovider")
	String[][] getReportTemplateEdit() throws IOException {

		return (ReadExcelData.readExelData(REPORT_EDIT));
	}

	@DataProvider(name = "deleteReportTemplatedataprovider")
	String[][] getReportTemplateDelete() throws IOException {

		return (ReadExcelData.readExelData(REPORT_DELETE));
	}

	@DataProvider(name = "createValidateReportTemplatedataprovider")
	String[][] getReportTemplateCreateValidate() throws IOException {

		return (ReadExcelData.readExelData(REPORT_TEMPLATE_CREATE_VALIDATE));
	}

	@DataProvider(name = "reportConfigFilesdataprovider")
	String[][] getReportConfigFiles() throws IOException {

		return (ReadExcelData.readExelData(REPORT_UPLOAD_CONFIGS));
	}
	
	@DataProvider(name = "reportDetailsChecking")
	String[][] getReportDetails() throws IOException {

		return (ReadExcelData.readExelData(REPORT_DETAILS));
	}

	// AssessmentReport Data Provider
	@DataProvider(name = "assessmentReportCreatedataprovider")
	String[][] getAssessmentReportCreateData() throws IOException {

		return (ReadExcelData.readExelData(ASSESSMENT_REPORT_CREATE));
	}
	@DataProvider(name = "dataArchivalProvider")
	String[][] addDataArchival() throws IOException {
		return (ReadExcelData.readExelData(DATA_ARCHIVAL));
	}

}
