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
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformreports.assessment.content.ContentExecutor;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineEnum;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class KPIExecutor implements Callable<JsonObject> {

	private static final Logger log = LogManager.getLogger(KPIExecutor.class);
	private static final String STATUS= "Status";
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
		long startTime = System.nanoTime();
		try {
			kpiId = executeKPIJob(_kpiConfigDTO);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					_kpiConfigDTO.getExecutionId(),_kpiConfigDTO.getWorkflowId(),_kpiConfigDTO.getReportId() ,"-",_kpiConfigDTO.getKpiId(),_kpiConfigDTO.getCategory()
					,0,ReportStatusConstants.USECASENAME +_kpiConfigDTO.getUsecaseName() + ReportStatusConstants.SCHEDULE +_kpiConfigDTO.getSchedule());			
		} catch (InsightsJobFailedException e) {
			response.addProperty(STATUS, "Failure");
			failedjobs.add(_kpiConfigDTO.getKpiId());
			response.add("kpiArray", failedjobs);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					_kpiConfigDTO.getExecutionId(),_kpiConfigDTO.getWorkflowId(),_kpiConfigDTO.getReportId() ,"-",_kpiConfigDTO.getKpiId(),_kpiConfigDTO.getCategory()
					,0,ReportStatusConstants.USECASENAME +_kpiConfigDTO.getUsecaseName() + ReportStatusConstants.SCHEDULE +_kpiConfigDTO.getSchedule());			
		}
		if (kpiId != ReportEngineEnum.StatusCode.ERROR.getValue()) {
			ReportConfigDAL reportConfigAL = new ReportConfigDAL();
			List<InsightsContentConfig> contentConfigList = reportConfigAL
					.getActiveContentConfigByKPIId(_kpiConfigDTO.getKpiId());
			if (!contentConfigList.isEmpty()) {
				/* Execute content on the same thread */
				failedjobs = ContentExecutor.executeContentJob(contentConfigList, _kpiConfigDTO);

			}
			/* If none of the kpi or content is failed then simply return Status as success */
			if (failedjobs.size() > 0) {
				response.addProperty(STATUS, "Failure");
				response.add("contentArray", failedjobs);
			} else {
				response.addProperty(STATUS, "Success");
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					_kpiConfigDTO.getExecutionId(),_kpiConfigDTO.getWorkflowId(),_kpiConfigDTO.getReportId() ,"-",_kpiConfigDTO.getKpiId(),_kpiConfigDTO.getCategory()
					,processingTime,ReportStatusConstants.USECASENAME +_kpiConfigDTO.getUsecaseName() + " schedule: " +_kpiConfigDTO.getSchedule());
			
		}

		return response;
	}

	private int executeKPIJob(InsightsKPIConfigDTO kpiDefinition) {
		log.debug("Worlflow Detail ==== In KPI id ==== {} KPI category {} KPI Name is ==== {} executionId ==== {} datasource ==== {} ",
				kpiDefinition.getKpiId(), kpiDefinition.getCategory(), kpiDefinition.getGroupName(),
				kpiDefinition.getExecutionId(),kpiDefinition.getDatasource());
		long startTime = System.nanoTime();
		if (!kpiDefinition.getdBQuery().equalsIgnoreCase("")) {
			InsightsKPIProcessor kpiProcessor = new InsightsKPIProcessor(kpiDefinition);
			if(ReportEngineEnum.StatusCode.NO_DATA.getValue()==kpiProcessor.processKPI(kpiDefinition))
			{
			   return ReportEngineEnum.StatusCode.NO_DATA.getValue();
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					_kpiConfigDTO.getExecutionId(),_kpiConfigDTO.getWorkflowId(),_kpiConfigDTO.getReportId() ,"-",_kpiConfigDTO.getKpiId(),_kpiConfigDTO.getCategory()
					,processingTime," usecasename: " +_kpiConfigDTO.getUsecaseName() + " schedule: " +_kpiConfigDTO.getSchedule());	
			return kpiDefinition.getKpiId();
			

		} else {
			log.error("Worlflow Detail ====   No neo4j query defined for KPI {} With Id {} ",
					kpiDefinition.getGroupName(),
					kpiDefinition.getKpiId());
			log.error(StringExpressionConstants.STR_EXP_TASK,
					_kpiConfigDTO.getExecutionId(),_kpiConfigDTO.getWorkflowId(),_kpiConfigDTO.getReportId() ,"-",_kpiConfigDTO.getKpiId(),_kpiConfigDTO.getCategory()
					,0,ReportStatusConstants.USECASENAME +_kpiConfigDTO.getUsecaseName() + ReportStatusConstants.SCHEDULE +_kpiConfigDTO.getSchedule()+ "No neo4j query defined for KPI");
			
			throw new InsightsJobFailedException("Worlflow Detail ==== No neo4j query defined for KPI");
		}
	}

}
