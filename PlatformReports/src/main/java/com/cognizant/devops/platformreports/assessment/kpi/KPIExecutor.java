/********************************************************************************
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
 *******************************************************************************/
package com.cognizant.devops.platformreports.assessment.kpi;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformreports.assessment.content.ContentExecutor;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class KPIExecutor implements Callable<JsonObject> {

	private static final Logger log = LogManager.getLogger(KPIExecutor.class);

	private static final long serialVersionUID = -4343203101560318074L;

	private InsightsKPIConfigDTO _kpiConfigDTO;

	public KPIExecutor(InsightsKPIConfigDTO kpiConfigDTO) {
		this._kpiConfigDTO = kpiConfigDTO;
	}

	public KPIExecutor() {
	}

	@Override
	public JsonObject call() throws Exception {
		JsonObject response = new JsonObject();
		JsonArray failedjobs = new JsonArray();
		int kpiId = ReportEngineEnum.StatusCode.ERROR.getValue();

		try {
			kpiId = executeKPIJob(_kpiConfigDTO);
		} catch (InsightsJobFailedException e) {
			response.addProperty("Status", "Failure");
			failedjobs.add(_kpiConfigDTO.getKpiId());
			response.add("kpiArray", failedjobs);
		}
		/*if (kpiId == ReportEngineEnum.StatusCode.NO_DATA.getValue()) {
			response.addProperty("Status", "Failure");
			failedjobs.add(_kpiConfigDTO.getKpiId());
			response.add("kpiArray", failedjobs);
		
			return response;
		
		} else*/ if (kpiId != ReportEngineEnum.StatusCode.ERROR.getValue()) {
			ReportConfigDAL reportConfigAL = new ReportConfigDAL();
			List<InsightsContentConfig> contentConfigList = reportConfigAL
					.getActiveContentConfigByKPIId(_kpiConfigDTO.getKpiId());
			if (!contentConfigList.isEmpty()) {
				/* Execute content on the same thread */
				failedjobs = ContentExecutor.executeContentJob(contentConfigList, _kpiConfigDTO);

			}
			/* If none of the kpi or content is failed then simply return Status as success */
			if (failedjobs.size() > 0) {
				response.addProperty("Status", "Failure");
				response.add("contentArray", failedjobs);
			} else {
				response.addProperty("Status", "Success");
			}

		}

		return response;
	}

	private int executeKPIJob(InsightsKPIConfigDTO kpiDefinition) {
		log.debug("Worlflow Detail ==== In KPI id ==== {} KPI category {} KPI Name is ==== {} executionId ==== {} datasource ==== {} ",
				kpiDefinition.getKpiId(), kpiDefinition.getCategory(), kpiDefinition.getGroupName(),
				kpiDefinition.getExecutionId(),kpiDefinition.getDatasource());

		if (!kpiDefinition.getdBQuery().equalsIgnoreCase("")) {
			InsightsKPIProcessor kpiProcessor = new InsightsKPIProcessor(kpiDefinition);
			if(ReportEngineEnum.StatusCode.NO_DATA.getValue()==kpiProcessor.processKPI(kpiDefinition))
			{
			   return ReportEngineEnum.StatusCode.NO_DATA.getValue();
			}				
			return kpiDefinition.getKpiId();		

		} else {
			log.error("Worlflow Detail ====   No neo4j query defined for KPI {} With Id {} ",
					kpiDefinition.getGroupName(),
					kpiDefinition.getKpiId());
			throw new InsightsJobFailedException("Worlflow Detail ==== No neo4j query defined for KPI");
		}
	}

}
