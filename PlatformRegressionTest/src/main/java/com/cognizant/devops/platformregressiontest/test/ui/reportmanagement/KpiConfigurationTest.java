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

import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;
import com.cognizant.devops.platformregressiontest.test.ui.testdata.ReportManagementDataProvider;

/**
 * @author Ankita
 *
 */

public class KpiConfigurationTest extends LoginAndSelectModule {

	KpiConfigurationPage kpiConfigurationPage;

	@BeforeTest
	public void setUp() {
		initialization();
		selectModuleKPIConfiguration("Kpi Configuration");
		kpiConfigurationPage = new KpiConfigurationPage();

	}

	@Test
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
	 */
	@Test(priority = 1, enabled = true, dataProvider = "createKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class)
	public void saveKpiTest(String kpiId, String kpiName, String toolName, String category, String resultField,
			String groupName, String datasource, String dbQuery, String isActive) {

		Assert.assertEquals(kpiId, kpiConfigurationPage.saveKPI(kpiId, kpiName, toolName, category, resultField,
				groupName, datasource, dbQuery, isActive));

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
	 */
	@Test(priority = 2, enabled = true, dataProvider = "validateKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class)
	public void validateKpiTest(String kpiId, String kpiName, String toolName, String category, String resultField,
			String groupName, String datasource, String dbQuery, String isActive) {

		Assert.assertEquals(kpiConfigurationPage.validateKPI(kpiId, kpiName, toolName, category, resultField, groupName,
				datasource, dbQuery, isActive), true);

	}

	/**
	 * This method tests edit screen functionality
	 * 
	 * @param kpiId
	 * @param category
	 */
	@Test(priority = 3, dataProvider = "editKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void editKpiTest(String kpiId, String category) {
		Assert.assertEquals(kpiConfigurationPage.editKPI(kpiId, category), true);
	}

	/**
	 * This method tests non-editable kpi fields in kpi edit screen
	 * 
	 * @param kpiId
	 */
	@Test(priority = 4, dataProvider = "searchKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void testNonEditableKPIFields(String kpiId) {
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
	 */
	@Test(priority = 5, dataProvider = "editKPIValidatedataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void editValidateKpiTest(String kpiId, String category, String resultField, String dbQuery,
			String datasource) {
		Assert.assertEquals(kpiConfigurationPage.editValidateKPI(kpiId, category, resultField, dbQuery, datasource),
				true);
	}

	/**
	 * This method tests upload json functionality
	 * 
	 * @param fileName
	 */
	@Test(priority = 6, dataProvider = "uploadJsonKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void uploadJsonTest(String fileName) {

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
	 */
	@Test(priority = 9, dataProvider = "deleteKPIdataprovider", dataProviderClass = ReportManagementDataProvider.class, enabled = true)
	public void deleteKpiTest(String kpiId) {
		Assert.assertEquals(kpiConfigurationPage.deleteKPI(kpiId), true);
	}

}
