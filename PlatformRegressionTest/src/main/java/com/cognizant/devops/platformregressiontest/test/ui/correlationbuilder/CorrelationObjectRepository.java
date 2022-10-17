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
package com.cognizant.devops.platformregressiontest.test.ui.correlationbuilder;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

public class CorrelationObjectRepository extends LoginAndSelectModule {

	@FindBy(xpath = "//span[contains(text(),' Co-Relation Builder')]")
	WebElement landingPage;

	@FindBy(xpath = "//span[contains(text(),'Select Source Tool')]")
	WebElement selectSourceTool;

	@FindBy(xpath = "//mat-select[@name='selectedLabels']")
	WebElement selectSourceLabel;

	@FindBy(xpath = "//span[contains(text(),'Select Destination Tool')]")
	WebElement selectDestinationTool;

	@FindBy(xpath = "//mat-select[@name='selectedDestinationLabel']")
	WebElement selectDestinationLabel;

	@FindBy(xpath = "//input[@placeholder='Please input a Co-Relation name']")
	WebElement correlationName;

	@FindBy(xpath = "//span[contains(text(),'Add Relationship Properties')]")
	WebElement addRelationshipProperties;

	@FindBy(xpath = "//pre[@class='headingTextinDiaglog']")
	WebElement addRelationshipPropBox;

	@FindBy(xpath = "//div[@formarrayname='property_points']/div[1]/label/input")
	WebElement propertyName;

	@FindBy(xpath = "//mat-icon[@svgicon='add']")
	WebElement addCircle;

	@FindBy(xpath = "//mat-icon[@svgicon='save']")
	WebElement save;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cancelBlkUpld']")
	WebElement cancelUpload;

	@FindBy(xpath = "//span[contains(text(),'Save')]")
	WebElement savePropertyNames;

	@FindBy(xpath = "//span[contains(text(),'Yes')]")
	WebElement yes;

	@FindBy(xpath = "//div[@class='textPadding']")
	WebElement afterClickingSaveMsg;

	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement ok;

	@FindBy(xpath = "//span[contains(text(), 'Relation Name already exists.')]")
	WebElement duplicateError;

	@FindBy(xpath = "//mat-icon[@svgicon='flag']")
	WebElement disableCorrelation;

	@FindBy(xpath = "//mat-icon[@svgicon='flag']")
	WebElement enableCorrelation;

	@FindBy(xpath = "//td[contains(@class,'ToolName')]")
	WebElement webhookToolName;

	@FindBy(xpath = "//mat-icon[@svgicon='trash']")
	WebElement delete;

	@FindBy(xpath = "//div[contains(text(),' Delete Correlation ')]")
	WebElement deleteCorrelationMessage;

	@FindBy(xpath = "//mat-icon[@svgicon='correlation']")
	WebElement viewCorrelation;

	@FindBy(xpath = "//pre[@class='showDialogHeadingCss']")
	WebElement relationshipDetailsMsg;

	@FindBy(xpath = "//mat-icon[@svgicon='close_dialog']")
	WebElement close;

	@FindBy(xpath = "//mat-icon[@svgicon='cancelBlkUpld']")
	WebElement cancel;

	@FindBy(xpath = "//b[@id='sourceTool']")
	WebElement viewCorrelationSourceTool;

	@FindBy(xpath = "//b[@id='destinationTool']")
	WebElement viewCorrelationDestinationTool;

	@FindBy(xpath = "//td[contains(@class,'cdk-column-ToolName')]")
	public List<WebElement> toolNameList;

	@FindBy(xpath = "//td[contains(@class,'LabelName')]")
	public List<WebElement> webhookLabelNameList;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> sourceToolList;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> sourceLabelList;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> destinationToolList;

	@FindBy(xpath = "//span[contains(@class, 'mat-option-text')]")
	public List<WebElement> destinationeLabelList;

	@FindBy(xpath = "//div[@class='property_source']/div/div/div[2]/table/tbody/tr")
	public List<WebElement> sourceToolPropList;

	@FindBy(xpath = "//div[@class='property_destination']/div/div/div[2]/table/tbody/tr")
	public List<WebElement> destinationToolPropList;

	@FindBy(xpath = "//div[@class='tableText']")
	public List<WebElement> relationsList;
	
	@FindBy(xpath = "//button[@id='crossClose']")  //
	WebElement crossClose;
}
