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
package com.cognizant.devops.platformreports.assessment.pdf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.assessmentreport.InsightsReportsKPIConfig;
import com.cognizant.devops.platformreports.assessment.dal.PDFDataProcessor;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PDFKPIVisualizationProcesser implements Callable<JsonObject> {
	private static final Logger log = LogManager.getLogger(PDFKPIVisualizationProcesser.class);

	private static final long serialVersionUID = -4343203101560316774L;
	InsightsReportsKPIConfig reportKpiConfig;
	InsightsAssessmentConfigurationDTO assessmentReportDTO;

	public PDFKPIVisualizationProcesser(InsightsReportsKPIConfig reportKpiConfig,
			InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		super();
		this.reportKpiConfig = reportKpiConfig;
		this.assessmentReportDTO = assessmentReportDTO;
	}

	@Override
	public JsonObject call() throws Exception {
		JsonObject returnMessage = new JsonObject();
		try {
			JsonObject visualizationData = genrateVisualizationResposeFromKPI();
			returnMessage.addProperty("status", "success");
			returnMessage.add("data", visualizationData);
		} catch (Exception e) {
			log.error(e);
			returnMessage.addProperty("status", "failure");
			returnMessage.addProperty("kpiId", reportKpiConfig.getKpiConfig().getKpiId());

		}
		return returnMessage;
	}

	private JsonObject genrateVisualizationResposeFromKPI() {

		JsonObject eachKPIObject = new JsonObject();
		JsonArray eachKPIVisualizationResult = new JsonArray();
		int kpiId = reportKpiConfig.getKpiConfig().getKpiId();
		eachKPIObject.addProperty("kpiId", kpiId);
		JsonArray vConfig = new JsonParser().parse(reportKpiConfig.getvConfig()).getAsJsonArray();
		PDFDataProcessor dataProcessor = new PDFDataProcessor();
		for (JsonElement eachConfig : vConfig) {
			JsonObject vResultObject = new JsonObject();
			JsonObject configObject = eachConfig.getAsJsonObject();
			String vType = configObject.get("vType").getAsString();
			String vQuery = configObject.get("vQuery").getAsString();
			vQuery = addFieldInVQuery(vQuery, kpiId);
			vResultObject.addProperty("vType", vType);
			JsonArray kpiResultArray = dataProcessor.fetchAndFormatKPIResult(vQuery);
			vResultObject.add("KpiResult", kpiResultArray);
			if (kpiResultArray.size() == 0) {
				log.debug("Worlflow Detail ====  No able to fetch record for kpi {} ====== query {} ", kpiId, vQuery);
			}
			eachKPIVisualizationResult.add(vResultObject);
		}
		eachKPIObject.add("visualizationresult", eachKPIVisualizationResult);
		JsonArray contentResultArray = dataProcessor.fetchAndFormatContentResult(assessmentReportDTO.getExecutionId(),
				kpiId, assessmentReportDTO.getConfigId());
		if (contentResultArray.size() > 0) {
			eachKPIObject.add("contentResult", contentResultArray);
		}
		log.debug("Worlflow Detail ==== prepared Visualization responce for kpi {} ",
				reportKpiConfig.getKpiConfig().getKpiId());
		return eachKPIObject;
	}

	private String addFieldInVQuery(String vQuery, int kpiId) {
		Map<String, Long> dateReplaceMap = new HashMap<>();
		dateReplaceMap.put("assessmentId", Long.parseLong(String.valueOf(assessmentReportDTO.getConfigId())));
		dateReplaceMap.put("executionId",assessmentReportDTO.getExecutionId());
		dateReplaceMap.put("kpiId", Long.parseLong(String.valueOf(kpiId)));
		StringSubstitutor sub = new StringSubstitutor(dateReplaceMap, "{", "}");
		return sub.replace(vQuery);
	}

}
