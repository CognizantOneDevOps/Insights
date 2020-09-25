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
package com.cognizant.devops.platformreports.assessment.dal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;

public class PDFDataProcessor {

	private static Logger log = LogManager.getLogger(PDFDataProcessor.class);

	protected ReportGraphDataHandler reportGraphDBHandler = new ReportGraphDataHandler();

	protected ReportDataHandler kPIAndContentResultDataHandler = ReportDataHandlerFactory
			.getDataSource(ApplicationConfigProvider.getInstance().getAssessmentReport().getOutputDatasource());

	public JsonArray fetchAndFormatKPIResult(String vQuery) {
		JsonArray result = new JsonArray();
		try {
			result = kPIAndContentResultDataHandler.fetchVisualizationResults(vQuery);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException("Worlflow Detail ====  Error while fetching KPI visualization result");
		}

		return result;

	}

	public JsonArray fetchAndFormatContentResult(long executionId, int kpiId, int assessmentId) {
		JsonArray result = new JsonArray();
		try {
			result = kPIAndContentResultDataHandler.fetchVisualizationResults(executionId, kpiId, assessmentId);
		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException(
					"Worlflow Detail ====  Error while fetching content visualization result");
		}
		return result;
	}

}
