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
package com.cognizant.devops.platformworkflow.workflowtask.utils;

import java.io.File;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowUtils {

	private static final Logger log = LogManager.getLogger(WorkflowUtils.class);

	public static final String INSIGHTS_HOME = "INSIGHTS_HOME";
	public static final String CONFIG_DIR = "workflowjar";
	public static final String INSIGHTS_CONFIG_DIR = ".InSights";
	public static final String WORKFLOW_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + File.separator + CONFIG_DIR
			+ File.separator; // + INSIGHTS_CONFIG_DIR + File.separator

	public static final String RETRY_JSON_PROPERTY = "isWorkflowTaskRetry";

	public static final String EXECUTION_HISTORY_JOSN_PROPERTY = "exectionHistoryId";

	public static Map<String, Object> convertJsonObjectToMap(String json) {
		ObjectMapper oMapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> resultMap = null;
		try {
			resultMap = oMapper.readValue(json, new TypeReference<Map<String, Object>>() {
			});
		} catch (JsonProcessingException e) {
			log.error(e);
		}
		return resultMap;
	}
}