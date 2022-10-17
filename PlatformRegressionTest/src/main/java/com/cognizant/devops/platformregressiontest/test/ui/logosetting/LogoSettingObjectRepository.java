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
package com.cognizant.devops.platformregressiontest.test.ui.logosetting;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

/**
 * @author NivethethaS
 * 
 *         Class contains the objects used for logo setting module test cases
 *
 */
public class LogoSettingObjectRepository  extends LoginAndSelectModule{

	@FindBy(xpath = "//b[contains(text(),'Logo Setting')]")
	WebElement landingPage;

	@FindBy(xpath = "//input[@id='file']")
	WebElement chooseFileButton;
	
	@FindBy(xpath = "//b[contains(text(), 'Preview')]")
	WebElement previewLogo;
	
	@FindBy(xpath = "//span[contains(text(),' uploaded successfully. Please LOGOUT and LOGIN again in to the Insights Application to see the uploaded logo.')]")
	WebElement successMessage;
	
	@FindBy(xpath = "//span[text()='Please select a of file size less than 1Mb']")
	WebElement errorMessage;
	
	@FindBy(xpath = "//span[text()= 'OKAY']")
	WebElement okButton;
	
	@FindBy(xpath = "//button[@id='crossClose']")
	WebElement crossClose;
	
	@FindBy(xpath = "//mat-icon[@svgicon='upload']")
	WebElement uploadImage;
	
	@FindBy(xpath = "//mat-icon[@svgicon='cross']")
	WebElement cancelUpload;
}
