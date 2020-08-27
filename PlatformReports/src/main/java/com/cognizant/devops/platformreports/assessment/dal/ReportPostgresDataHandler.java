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
package com.cognizant.devops.platformreports.assessment.dal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReportPostgresDataHandler {
	private static final Logger log = LogManager.getLogger(ReportPostgresDataHandler.class);

	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	WorkflowDAL workflowDAL = new WorkflowDAL();

	public InsightsContentConfig getContentConfig(int contentId) {
		return reportConfigDAL.getContentConfig(contentId);
	}

	public ContentConfigDefinition convertJsonToContentConfig(InsightsContentConfig contentDBConfig) {
		ContentConfigDefinition contentConfig = null;
		try {
			ObjectMapper oMapper = new ObjectMapper();
			JsonNode contentNode = oMapper.readTree(contentDBConfig.getContentJson());
			if (contentNode.isObject()) {
				Map<String, Object> configMap = new HashMap<>();
				for (Iterator<Map.Entry<String, JsonNode>> it = contentNode.fields(); it.hasNext();) {
					Map.Entry<String, JsonNode> field = it.next();
					String key = field.getKey();
					JsonNode valueResponse = field.getValue();

					Object value = ReportEngineUtils.getNodeValue(key, valueResponse);
					configMap.put(key, value);
				}

				if (!configMap.isEmpty()) {
					contentConfig = oMapper.convertValue(configMap, ContentConfigDefinition.class);
					contentConfig.setContentId(contentDBConfig.getContentId());
					contentConfig.setKpiId(contentDBConfig.getKpiConfig().getKpiId());
					if (configMap.get("action") != null) {
						contentConfig.setAction(
								ReportEngineEnum.ExecutionActions.valueOf(String.valueOf(configMap.get("action"))));

					}
					if (configMap.get("directionOfThreshold") != null) {
						contentConfig.setDirectionOfThreshold(ReportEngineEnum.DirectionOfThreshold
								.valueOf(String.valueOf(configMap.get("directionOfThreshold"))));
					}
					contentConfig.setCategory(ReportEngineEnum.ContentCategory.valueOf(contentDBConfig.getCategory()));
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		return contentConfig;
	}

}
