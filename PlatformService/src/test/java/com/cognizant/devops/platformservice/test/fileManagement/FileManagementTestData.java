/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.test.fileManagement;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.google.gson.JsonObject;

public class FileManagementTestData extends AbstractTestNGSpringContextTests{
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	String fileName = "Test_Data";
	String fileName1 = "Test1_Data";
	String fileType = "JSON";
	String module = "DATAENRICHMENT";
	String module1 = "DATA1ENRICHMENT";
	File file = new File(classLoader.getResource("FileManagement_test.json").getFile());
	String fileDetails = "{\"fileName\":\"Test_Data\",\"fileType\":\"JSON\"}";
	JsonObject fileDetailsJson = JsonUtils.parseStringAsJsonObject(fileDetails);
	String input= fileDetailsJson.toString().replace("\n", "").replace("\r", "");
	String encodeString = new String(Base64.getEncoder().encodeToString(input.getBytes()));
}	
