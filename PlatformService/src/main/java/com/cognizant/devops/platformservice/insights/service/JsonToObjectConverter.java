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
package com.cognizant.devops.platformservice.insights.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.cognizant.devops.platformcommons.core.enums.KPIJobResultAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * This class help capture query output in POJO object. Class might need to be changed every time query output changes.
 * Introduce to remove hardcoding and logic of capturing output from service object. 
 * Keeping the object in same package for now as it's very specific to Insights service
 *
 */
public final class JsonToObjectConverter {
	
	public static List<InferenceResult> getInferenceResult(JsonObject jsonObj){
		
		List<InferenceResult> results = new LinkedList<>();
		
		JsonObject rootObj = jsonObj.get("aggregations").getAsJsonObject().get("terms").getAsJsonObject();
		JsonArray array = rootObj.get("buckets").getAsJsonArray();
		for(JsonElement element : array){
			InferenceResult inferenceresult = new InferenceResult();
			
			JsonObject output = element.getAsJsonObject();
			
			Long kpiID = output.get("key").getAsLong();
			inferenceresult.setKpiID(kpiID);
			inferenceresult.setTotalDocuments(output.get("doc_count").getAsLong());
			
			JsonObject hits = output.get("top_tag_hits").getAsJsonObject().get("hits").getAsJsonObject();
			JsonArray hitsArray = hits.get("hits").getAsJsonArray();
			
			List<InferenceResultDetails> details = new ArrayList<>(5);
			
			for(JsonElement hitElement : hitsArray) {
				JsonObject kpiData = hitElement.getAsJsonObject().get("_source").getAsJsonObject();
				InferenceResultDetails inferenceDetails = new InferenceResultDetails();
				
				inferenceDetails.setKpiID(kpiID);
				inferenceDetails.setName(kpiData.get(KPIJobResultAttributes.NAME.toString()).getAsString());
				inferenceDetails.setAction(kpiData.get(KPIJobResultAttributes.ACTION.toString()).getAsString());
				inferenceDetails.setExpectedTrend(kpiData.get(KPIJobResultAttributes.EXPECTEDTREND.toString()).getAsString());
				if(kpiData.get(KPIJobResultAttributes.ISCOMPARISIONKPI.toString()) != null && !kpiData.get(KPIJobResultAttributes.ISCOMPARISIONKPI.toString()).isJsonNull()) {
					inferenceDetails.setIsComparisionKpi(kpiData.get(KPIJobResultAttributes.ISCOMPARISIONKPI.toString()).getAsBoolean());
				}
				inferenceDetails.setResult(kpiData.get(KPIJobResultAttributes.RESULTS.toString()).getAsLong());
				if(kpiData.get(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString()) != null && !kpiData.get(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString()).isJsonNull()) {
					inferenceDetails.setResultOutPutType(kpiData.get(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString()).getAsString());
				}
				inferenceDetails.setResultTime(kpiData.get(KPIJobResultAttributes.RESULTTIME.toString()).getAsLong());
				inferenceDetails.setSchedule(kpiData.get(KPIJobResultAttributes.SCHEDULE.toString()).getAsString());
				inferenceDetails.setVector(kpiData.get(KPIJobResultAttributes.VECTOR.toString()).getAsString());
				if(!kpiData.get(KPIJobResultAttributes.TOOLNAME.toString()).isJsonNull()) {
					inferenceDetails.setToolName(kpiData.get(KPIJobResultAttributes.TOOLNAME.toString()).getAsString());
				}
				
				if(!kpiData.get(KPIJobResultAttributes.ISGROUPBY.toString()).isJsonNull() && kpiData.get(KPIJobResultAttributes.ISGROUPBY.toString()).getAsBoolean()) {
					inferenceDetails.setIsGroupBy(kpiData.get(KPIJobResultAttributes.ISGROUPBY.toString()).getAsBoolean());
					inferenceDetails.setGroupByName(kpiData.get(KPIJobResultAttributes.GROUPBYFIELDNAME.toString()).getAsString());
					inferenceDetails.setGroupByFieldVal(kpiData.get(KPIJobResultAttributes.GROUPBYFIELDVAL.toString()).getAsString());
				} else {
					inferenceDetails.setIsGroupBy(Boolean.FALSE);
				}
				details.add(inferenceDetails);
			}
			inferenceresult.setDetails(details);
			results.add(inferenceresult);
		}
		return results;
	}

}
