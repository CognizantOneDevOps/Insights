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
/**
 * 
 */
package com.cognizant.devops.platformreports.assessment.dal;

import java.util.List;

import com.cognizant.devops.platformreports.assessment.datamodel.ContentConfigDefinition;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIConfigDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsKPIResultDetails;
import com.cognizant.devops.platformreports.assessment.datamodel.QueryModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author 716660
 *
 */
public interface ReportDataHandler {

	public void saveData(List<JsonObject> resultList);

	public List<JsonObject> fetchKPIData(String query, InsightsKPIConfigDTO kpiDefinition, QueryModel qModel);

	public List<InsightsKPIResultDetails> fetchKPIResultData(ContentConfigDefinition contentConfigDefinition);

	public void saveContentResult(JsonObject contentResult);
	
	public JsonArray fetchVisualizationResults(String query);
	
	public JsonArray fetchVisualizationResults(long executionId, int kpiId, int assessmentId);
	

}
