/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformneo4jbackuputility.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformneo4jbackuputility.neo4j.tool.Neo4jDataCopyService;
import com.cognizant.devops.platformneo4jbackuputility.utils.InsightsUtils;

public class Application {
	private static Logger LOG = LogManager.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		LOG.debug(" Neo4j data backup /store backup start  ");
		InsightsUtils.loadProperties();
		if (!InsightsUtils.isPropertyFetch()) {
			String sourceDir = InsightsUtils.readProperty("source_db_dir");
			String targetDir = InsightsUtils.readProperty("target_db_dir");
			LOG.debug(" targetDir  " + targetDir);
			if (sourceDir.equalsIgnoreCase("") || targetDir.equalsIgnoreCase("")) {
				LOG.error("Please select source and target dir");
				throw new IllegalArgumentException("Please provide source and target dir");
			} else {
				LOG.debug("Data copy started ");
				Neo4jDataCopyService neo4jDataCopyService = new Neo4jDataCopyService();
				neo4jDataCopyService.startCopy(sourceDir, targetDir);
				LOG.debug(" Neo4j data backup /store backup completed  ");
			}
		} else {
			LOG.error("Unable to read property file");
			throw new IllegalArgumentException("Unable to read property file " + InsightsUtils.isPropertyFetch());
		}
	}

}
