/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.platformservice.test.offlineDataProcessing;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.cognizant.devops.platformservice.test.assessmentReports.AssessmentReportServiceData;

public class OfflineDataProcessingTestData extends AbstractTestNGSpringContextTests {

	private static final Logger log = LogManager.getLogger(AssessmentReportServiceData.class);
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	File offlineFile = new File(classLoader.getResource("OfflineDataProcessing_test.json").getFile());

}
