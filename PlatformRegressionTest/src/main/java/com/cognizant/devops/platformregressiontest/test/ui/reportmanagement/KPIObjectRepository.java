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

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class KPIObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//div[contains(text(),' KPI Configuration ')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@title='Add New KPI']")
	WebElement addNewKPIButton;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-KpiId')]")
	List<WebElement> kpiListEl;

	@FindBy(xpath = "//input[@name='kpiId']")
	WebElement kpiIdEl;

	@FindBy(xpath = "//input[@name='kpiName']")
	WebElement kpiNameEl;

	@FindBy(xpath = "//input[@name='resultField']")
	WebElement resultFieldEl;

	@FindBy(xpath = "//input[@name='groupName']")
	WebElement groupNameEl;

	@FindBy(xpath = "//textarea[@name='dbQuery']")
	WebElement dbQueryEl;

	@FindBy(xpath = "//mat-select[@name='selectedTool']")
	WebElement toolNameEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> toolNameListEl;

	@FindBy(xpath = "//mat-select[@name='category']")
	WebElement categoryNameEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> categoryNameListEl;

	@FindBy(xpath = "//span[text()='Category']/parent::td/following-sibling::td/child::mat-select/child::div/child::div[1]")
	public WebElement categoryEl;

	@FindBy(xpath = "//mat-select[@name='dataSource']")
	public WebElement datasourceEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> datasourceListEl;

	@FindBy(xpath = "//mat-slide-toggle[contains(@class,'mat-slide-toggle')]")
	public WebElement isActiveEl;

	@FindBy(xpath = "//mat-icon[text()=' save ']")
	public WebElement saveEl;

	@FindBy(xpath = "//span[text()='YES']")
	public WebElement yesBtnEl;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	public WebElement btnOKEl;

	@FindBy(xpath = "//mat-icon[@title='Edit']")
	public WebElement btnEditEl;

	@FindBy(xpath = "//mat-icon[@title='Delete']")
	public WebElement btnDeleteEl;

	@FindBy(xpath = "//mat-icon[@title='Upload JSON']")
	public WebElement uploadBtnE1;

	@FindBy(xpath = "//input[@type='file']")
	public WebElement chooseFileBtnE1;

	@FindBy(xpath = "//mat-icon[@title='Upload ']")
	public WebElement uploadJsonBtnE1;

	@FindBy(xpath = "//span[text()='OK']")
	public WebElement okBtnE1;

	@FindBy(xpath = "//input[@placeholder='Search']")
	public WebElement searchKPIEl;

	@FindBy(xpath = "//div[contains(text(),'already exists')]")
	public WebElement kpiExistsEl;

	@FindBy(xpath = "//b[contains(text(),'deleted')]")
	public WebElement kpiDeletedEl;

	@FindBy(xpath = "//div[contains(text(),'Error')]")
	public WebElement kpiValidateEl;

	@FindBy(xpath = "//b[contains(text(),'updated')]")
	public WebElement kpiUpdateEl;

	@FindBy(xpath = "//img[contains(@alt,'Cognizant Insights log')]")
	public WebElement homeEl;

	@FindBy(xpath = ".//preceding::mat-radio-button")
	public WebElement checkedRadioEl;

	@FindBy(xpath = "//mat-icon[@title='Refresh']")
	WebElement refreshBtnE1;
}
