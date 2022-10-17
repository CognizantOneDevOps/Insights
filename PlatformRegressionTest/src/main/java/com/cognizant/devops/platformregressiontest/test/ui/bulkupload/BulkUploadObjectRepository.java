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
package com.cognizant.devops.platformregressiontest.test.ui.bulkupload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for bulk upload module test cases
 *
 */
public class BulkUploadObjectRepository extends LoginAndSelectModule{

	Map<String, String> testData = new HashMap<>();

	@FindBy(xpath = "//b[contains(text(),'Bulk Upload')]")
	WebElement landingPage;
	
	@FindBy(xpath = "//mat-select[@name ='selectedTool']")
	WebElement selectTool ;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	List<WebElement> toolnameList;
	
	@FindBy(xpath = "//div[@id='0']//mat-select[@name ='selectedTool']")
	WebElement clickTool1 ;
	
	@FindBy(xpath = "//div[@id='1']//mat-select[@name ='selectedTool']")
	WebElement clickTool2 ;

	@FindBy(xpath = "//div[@id='2']//mat-select[@name ='selectedTool']")
	WebElement clickTool3 ;

	@FindBy(xpath = "//div[@id='3']//mat-select[@name ='selectedTool']")
	WebElement clickTool4 ;

	@FindBy(xpath = "//div[@id='4']//mat-select[@name ='selectedTool']")
	WebElement clickTool5 ;

	@FindBy(xpath = "//div[@id='0']//div//span[contains(text(),'Tool')]")
	WebElement selectTool1;
	
	@FindBy(xpath = "//div[@id='1']//div//span[contains(text(),'Tool')]")
	WebElement selectTool2;
	
	@FindBy(xpath = "//div[@id='2']//div//span[contains(text(),'Tool')]")
	WebElement selectTool3;
	
	@FindBy(xpath = "//div[@id='3']//div//span[contains(text(),'Tool')]")
	WebElement selectTool4;
	
	@FindBy(xpath = "//div[@id='4']//div//span[contains(text(),'Tool')]")
	WebElement selectTool5;
	
	@FindBy(xpath = "//div[@id='0']//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField1;
	
	@FindBy(xpath = "//div[@id='1']//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField2;
	
	@FindBy(xpath = "//div[@id='2']//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField3;
	
	@FindBy(xpath = "//div[@id='3']//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField4;
	
	@FindBy(xpath = "//div[@id='4']//div//input[contains(@placeholder,'InsightsTimeField')]")
	WebElement timeField5;
	
	@FindBy(xpath = "//div[@id='0']//div//input[contains(@type,'file')]")
	WebElement file1;
	
	@FindBy(xpath = "//div[@id='1']//div//input[contains(@type,'file')]")
	WebElement file2;
	
	@FindBy(xpath = "//div[@id='2']//div//input[contains(@type,'file')]")
	WebElement file3;
	
	@FindBy(xpath = "//div[@id='3']//div//input[contains(@type,'file')]")
	WebElement file4;
	
	@FindBy(xpath = "//div[@id='4']//div//input[contains(@type,'file')]")
	WebElement file5;
	
	@FindBy(xpath = "//div[@id='0']//div//input[@name='InsightsTimeFormat']")
	WebElement timeFormatfield;
	
	@FindBy(xpath = "//div[@id='3']//mat-icon[@svgicon='successBlk']")
	WebElement successTickrow4;
	
	@FindBy(xpath = "//div[@id='1']//mat-icon[@svgicon='successBlk']")
	WebElement successTickrow2;
	
	@FindBy(xpath = "//div[@id='0']//mat-icon[@svgicon='successBlk']")
	WebElement successTickrow1;
	
	@FindBy(xpath = "//mat-icon[@svgicon='failureBlk']")
	WebElement failureStatus;
	
	@FindBy(xpath = "//div[@id='0']//mat-icon[@svgicon='failureBlk']")
	WebElement failureStatusRow1;
	
	@FindBy(xpath = "//div[@id='2']//mat-icon[@svgicon='failureBlk']")
	WebElement failureStatusRow3;
	
	@FindBy(xpath = "//mat-icon[@svgicon='upload']")
	WebElement saveButton;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cancelBlkUpld']")
	WebElement resetButton;
	
	@FindBy(xpath = "//div[contains(text(),'Upload the Data')]")
	WebElement uploadMessage;
	
	@FindBy(xpath = "//button[@id= 'yesBtn']")
	WebElement yesButton;
	
	@FindBy(xpath = "//span[contains(text(), 'You have successfully uploaded the file to Neo4J for GIT')]")
	WebElement successMessage;
	
}
