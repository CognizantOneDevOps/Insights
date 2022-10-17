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

public class ReportTemplateObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//div[contains(text(),'Add Report Template')]")
	WebElement landingPage;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addBtnEl;

	@FindBy(xpath = "//input[contains(@placeholder,'template name')]")
	WebElement templateNameEl;

	@FindBy(xpath = "//input[contains(@placeholder,'description')]")
	WebElement descriptionEl;

	@FindBy(xpath = "//mat-select[contains(@placeholder,'visualization')]")
	WebElement visualizationEl;
	
	@FindBy(xpath = "//mat-select[contains(@placeholder,'template')]")
	WebElement templateTypeEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> visualizationElList;

	@FindBy(xpath = "//mat-icon[@svgicon='search']")
	WebElement searchKpiEl;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-kpiId')]")
	List<WebElement> kpiListEl;

	@FindBy(xpath = "//input[@placeholder='Search']")
	WebElement kpiInputEl;

	@FindBy(xpath = "//span[contains(text(), 'OK')]")
	WebElement kpiSelectBtnEl;

	@FindBy(xpath = "//span[contains(text(),'Failed to save')]")
	WebElement reportExitsBtnEl;

	@FindBy(xpath = "//span[contains(text(),'mandatory fields')]")
	WebElement fillMandatoryDialogEl;
	
	@FindBy(xpath = "//div[contains(text(), ' Error')]")
	WebElement errorMessage;

	@FindBy(xpath = "//span[contains(text(),'add kpi details')]")
	WebElement addKPIDialogEl;

	@FindBy(xpath = "//mat-select[contains(@placeholder,'Select VType')]")
	WebElement vTypeEl;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> vTypeElList;

	@FindBy(xpath = "//textarea")
	WebElement vQueryEl;

	@FindBy(xpath = "//span[contains(text(), 'ADD')]")
	WebElement addKpiBtn;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement saveEl;

	@FindBy(xpath = "//span[contains(text(),'Yes')]")
	WebElement yesBtnEl;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement btnOKEl;
	
	@FindBy(xpath = "//button[@id='crossClose']")  //
	WebElement crossClose;

	@FindBy(xpath = "//mat-icon[@svgicon='upload']")
	WebElement uploadBtnE1;

	@FindBy(xpath = "//mat-icon[@svgicon='paperclip']")
	WebElement attachFilesBtnE1;

	@FindBy(xpath = "//input[@type='file']")
	public WebElement chooseFileBtnE1;

	@FindBy(xpath = "//mat-icon[@title='Upload ']")
	public WebElement uploadJsonBtnE1;

	@FindBy(xpath = "//td[contains(@class, 'mat-column-reportTemplateName')]")
	List<WebElement> reportTemplateListEl;

	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	public WebElement btnDeleteEl;

	@FindBy(xpath = "//mat-icon[@svgicon='retry']")
	WebElement refreshBtnE1;

	@FindBy(xpath = "//mat-icon[@svgicon='edit']")
	public WebElement btnEditEl;

	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	public WebElement btnCloseEl;
	
	@FindBy(xpath = "//div[contains(text(), 'KPI Details')]")
	WebElement workfloHistoryDetail;
	
	@FindBy(xpath="//mat-icon[@svgicon='healthcheck_show_details']")
	WebElement healthcheckDetails;
}
