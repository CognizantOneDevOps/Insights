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
package com.cognizant.devops.platformregressiontest.test.ui.reportmanagement;


import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ReportConfigurationDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ReportManagementDataProvider;
import com.cognizant.devops.platformregressiontest.test.ui.testdatamodel.ReportConfigurationDataModel;

/**
 * @author Ankita
 *
 */

public class KpiConfigurationTest extends LoginAndSelectModule {

	KpiConfigurationPage kpiConfigurationPage;

	@BeforeTest
	public void setUp() {
		initialization();
		selectReportConfiguration("Kpi Configuration");
		kpiConfigurationPage = new KpiConfigurationPage();

	}

	public void testKPILandingPage() {
		kpiConfigurationPage.navigateToKPIConfigurationLandingPage();

	}

	/**
	 * This test case tests the save kpi test functionality
	 * 
	 * @param kpiId
	 * @param kpiName
	 * @param toolName
	 * @param category
	 * @param resultField
	 * @param groupName
	 * @param datasource
	 * @param dbQuery
	 * @param isActive
	 * @throws InterruptedException 
	 */
	@Test(priority = 1, enabled = true, dataProvider = "saveKPI", dataProviderClass = ReportConfigurationDataProvider.class)
	public void saveKPI(ReportConfigurationDataModel data) throws InterruptedException {

		Assert.assertTrue(kpiConfigurationPage.saveKPI(data),"kpi data saved successfully.");

	}

	/**
	 * This method tests the KPI validation with various usecases
	 ** 
	 * @param kpiId
	 * @param kpiName
	 * @param toolName
	 * @param category
	 * @param resultField
	 * @param groupName
	 * @param datasource
	 * @param dbQuery
	 * @param isActive
	 * @throws InterruptedException 
	 */
	@Test(priority = 2, enabled = true, dataProvider = "validateKPI", dataProviderClass = ReportConfigurationDataProvider.class)
	public void validateKpiTest(ReportConfigurationDataModel data) throws InterruptedException {

		Assert.assertTrue(kpiConfigurationPage.validateKPI(data));

	}

	/**
	 * This method tests edit screen functionality
	 * 
	 * @param kpiId
	 * @param category
	 * @throws InterruptedException 
	 */
	@Test(priority = 3, dataProvider = "editKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void editKpiTest(String kpiId, String category) throws InterruptedException {
		Assert.assertEquals(kpiConfigurationPage.editKPI(kpiId, category), true);
	}

	/**
	 * This method tests non-editable kpi fields in kpi edit screen
	 * 
	 * @param kpiId
	 * @throws InterruptedException 
	 */
	@Test(priority = 4, dataProvider = "searchKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void testNonEditableKPIFields(String kpiId) throws InterruptedException {
		Assert.assertEquals(kpiConfigurationPage.nonEditableFields(kpiId), true);
	}

	/**
	 * This method tests kpi screen edit validation screen.
	 * 
	 * @param kpiId
	 * @param category
	 * @param resultField
	 * @param dbQuery
	 * @param datasource
	 * @throws InterruptedException 
	 */
	@Test(priority = 5, dataProvider = "editKPIValidatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void editValidateKpiTest(String kpiId, String category, String resultField, String dbQuery,
			String datasource) throws InterruptedException {
		Assert.assertEquals(kpiConfigurationPage.editValidateKPI(kpiId, category, resultField, dbQuery, datasource),
				true);
	}

	/**
	 * This method tests upload json functionality
	 * 
	 * @param fileName
	 * @throws InterruptedException 
	 */
	@Test(priority = 6, dataProvider = "uploadJsonKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void uploadJsonTest(String fileName) throws InterruptedException {

		Assert.assertEquals(kpiConfigurationPage.uploadJson(fileName), true);
	}

	/**
	 * This method tests kpi search functionality
	 * 
	 * @param kpiId
	 */
	@Test(priority = 7, dataProvider = "searchKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void searchKPITest(String kpiId) {
		Assert.assertEquals(kpiConfigurationPage.searchKPI(kpiId), true);
	}

	/**
	 * This method tests refresh button functionality
	 * 
	 * @param kpiId
	 */
	@Test(priority = 8, dataProvider = "searchKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void testRefresh(String kpiId) {
		Assert.assertEquals(kpiConfigurationPage.checkRefreshButton(kpiId), true);
	}

	/**
	 * This method tests delete kpi functionality
	 * 
	 * @param kpiId
	 * @throws InterruptedException 
	 */
	@Test(priority = 9, dataProvider = "deleteKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void deleteKpiTest(String kpiId) throws InterruptedException {
		Assert.assertEquals(kpiConfigurationPage.deleteKPI(kpiId), true);
	}

}
