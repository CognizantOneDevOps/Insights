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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformneo4jbackuputility.neo4j.tool.StoreCopy;

public class Application {
	private static Logger LOG = LogManager.getLogger(Application.class);
	static Properties properties = new Properties();
	public static void main(String[] args) throws Exception {
		LOG.debug(" Neo4j data backup /store backup start  ");
		getPropValues();
		String sourceDir = StoreCopy.getArgument(args, 0, properties, "source_db_dir");
		String targetDir = StoreCopy.getArgument(args, 1, properties, "target_db_dir");
		LOG.debug(" targetDir  " + targetDir);
		if (sourceDir.equalsIgnoreCase("") || targetDir.equalsIgnoreCase("")) {
			LOG.error("Please select source and target dir");
			throw new IllegalArgumentException("Please provide source and target dir");
		} else {
			Set<String> ignoreRelTypes = StoreCopy
					.splitToSet(StoreCopy.getArgument(args, 2, properties, "rel_types_to_ignore"));
			Set<String> ignoreProperties = StoreCopy
					.splitToSet(StoreCopy.getArgument(args, 3, properties, "properties_to_ignore"));
			Set<String> ignoreLabels = StoreCopy
					.splitToSet(StoreCopy.getArgument(args, 4, properties, "labels_to_ignore"));
			Set<String> deleteNodesWithLabels = StoreCopy
					.splitToSet(StoreCopy.getArgument(args, 5, properties, "labels_to_delete"));
			String keepNodeIdsParam = StoreCopy.getArgument(args, 6, properties, "keep_node_ids");
			boolean keepNodeIds = !("false".equalsIgnoreCase(keepNodeIdsParam));
			LOG.debug(
					"Copying from {} to {} ingoring rel-types {} ignoring properties {} ignoring labels {} removing nodes with labels {} keep node ids {} ",
					sourceDir, targetDir, ignoreRelTypes, ignoreProperties, ignoreLabels, deleteNodesWithLabels,
					keepNodeIds);
			StoreCopy.copyStore(sourceDir, targetDir, ignoreRelTypes, ignoreProperties, ignoreLabels,
					deleteNodesWithLabels, keepNodeIds);
			LOG.debug(" Neo4j data backup /store backup completed  ");
		}
	}

	public static void getPropValues() throws IOException {
		FileInputStream input = null;

		try {
			String path = "./neo4j.properties";
			input = new FileInputStream(path);

			// load the properties file String config/config.properties
			properties.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.error("Unable to read property file");
				}
			}
		}
	}

}
