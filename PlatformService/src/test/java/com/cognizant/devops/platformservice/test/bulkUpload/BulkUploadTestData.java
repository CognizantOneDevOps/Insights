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

package com.cognizant.devops.platformservice.test.bulkUpload;

import java.io.File;

public class BulkUploadTestData {

	ClassLoader classLoader = ClassLoader.getSystemClassLoader();

	String toolJson = "[{ \"toolName\":\"GIT\",\"label\":\"SCM:GIT:DATA\"}]";

	File file = new File(classLoader.getResource("BulkUploadTest_GIT.csv").getFile());
	File fileWithVariedEpochTimes = new File(
			classLoader.getResource("BulkUploadTest_GIT_VariedEpochTime.csv").getFile());
	File fileWithZFormatEpochTimes = new File(classLoader.getResource("BulkUploadTest_GIT_ZFormat.csv").getFile());
	File fileWithTimeZoneFormatEpochTimes = new File(
			classLoader.getResource("BulkUploadTest_GIT_TimeZoneFormat.csv").getFile());
	File fileWithNullEpochTime = new File(classLoader.getResource("BulkUploadTest_GIT_NullEpochTime.csv").getFile());
	File fileWithNumericValues = new File(classLoader.getResource("Bulk_Upload_Numeric_Values.csv").getFile());
	String toolName = "GIT";
	String label = "SCM:GIT:DATA";
	String nullLabel = null;
	String insightTimeField = "commitTime";
	String wrongInsightTimeField = "commitTome";
	String nullInsightTimeField = null;
	String wrongInsightTimeFormat = "commitTome";
	String nullInsightTimeFormat = null;
	String insightTimeZFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	String insightTimeWithTimeZoneFormat = "yyyy-MM-dd'T'HH:mm:ssXXX";
	String fileWithNumericValues_insighstimeField = "completionDateEpochTime";
	String labelForNumericCheck ="SCM:NUMERIC_CHECK:DATA";
	long filesizeMaxValue = 2097152;

	File fileSize = new File(classLoader.getResource("BulkUploadTest_Size.csv").getFile());
	File fileFormat = new File(classLoader.getResource("BulkUploadTest_GIT.txt").getFile());
	File incorrectDataFile = new File(classLoader.getResource("BulkUploadTest_JIRA.csv").getFile());

}
