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
package com.cognizant.devops.platformservice.test.CorrelationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;

public class CorrelationBuilderTestData {

	String saveDataConfig = "{\"destination\":{\"toolName\":\"PIVOTALTRACKER\",\"toolCategory\":\"ALM\",\"labelName\":\"PIVOTALTRACKER\",\"fields\":[\"projectId\"]},\"source\":{\"toolName\":\"PIVOTALTRACKER\",\"toolCategory\":\"ALM\",\"labelName\":\"PIVOTALTRACKER\",\"fields\":[\"storyId\"]},\"relationName\":\"FROM_PIVOTALTRACKER_TO_PIVOTALTRACKER_Testing\",\"relationship_properties\":[],\"selfRelation\":true}";
	String getConfigDetails = "[{\"destination\":{\"toolName\":\"JIRA\",\"toolCategory\":\"ALM\",\"fields\":[\"jir_priority\"]},\"source\":{\"toolName\":\"BITBUCKET\",\"toolCategory\":\"SCM\",\"fields\":[\"bit_commiTime\"]},\"relationName\":\"FROM_BITBUCKET_TO_JIRA_test\"}]";
	String UpdateConfigDetails = "{\"relationName\":\"FROM_PIVOTALTRACKER_TO_PIVOTALTRACKER_Testing\",\"correlationFlag\":true}";
	String DeleteConfigDetails = "{\"relationName\":\"FROM_PIVOTALTRACKER_TO_PIVOTALTRACKER_Testing\",\"correlationFlag\":false}";

	public static void resetConfig(String configDetails) throws IOException {
		// TODO Auto-generated method stub

		String configFilePath = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR;
		File configFile = null;
		Path dir = Paths.get(configFilePath);
		Stream<Path> paths = Files.find(dir, Integer.MAX_VALUE,
				(path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(ConfigOptions.CORRELATION_TEMPLATE));
		configFile = paths.limit(1).findFirst().get().toFile();
		FileWriter file = new FileWriter(configFile);
		file.write(configDetails);
		file.flush();
		file.close();

	}
}
