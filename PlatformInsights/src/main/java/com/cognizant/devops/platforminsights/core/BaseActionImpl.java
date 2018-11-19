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
package com.cognizant.devops.platforminsights.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;
import com.cognizant.devops.platformcommons.core.enums.KPIJobResultAttributes;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platforminsights.core.job.config.SparkJobConfigHandler;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsSparkJobFailedException;

public abstract class BaseActionImpl {
	
	private static Logger log = LogManager.getLogger(BaseActionImpl.class);
	protected JavaPairRDD<String,Map<String,Object>> esRDD;
	protected KPIDefinition kpiDefinition;
	
	public BaseActionImpl(KPIDefinition kpiDefinition){
		this.kpiDefinition = kpiDefinition;
		if(ExecutionActions.AVERAGE == kpiDefinition.getAction()){
			buildSparkJob(kpiDefinition);
		}
		//Map<String, Object> result = execute();
		//saveResult(result);
	}
	
	private void buildSparkJob(KPIDefinition kpiDefinition){
		Map<String, String> jobConf = new HashMap<String, String>();
		String esQuery = kpiDefinition.getEsquery();
		esQuery = getEsQueryWithDates(kpiDefinition.getSchedule(),esQuery);
		log.debug("KPI query - "+esQuery);
		jobConf.put("es.query", esQuery);
		jobConf.put("es.resource", kpiDefinition.getEsResource());
		jobConf.put("es.index.read.missing.as.empty", Boolean.TRUE.toString());
		JavaSparkContext sparkContext = JavaSparkContextProvider.getJavaSparkContext();
		esRDD = JavaEsSpark.esRDD(sparkContext, jobConf);
	}
	
	protected Map<String, Object> getResultMap(Long result,String groupByValue){
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(KPIJobResultAttributes.KPIID.toString(), kpiDefinition.getKpiID());
		resultMap.put(KPIJobResultAttributes.NAME.toString(), kpiDefinition.getName());
		resultMap.put(KPIJobResultAttributes.EXPECTEDTREND.toString(), kpiDefinition.getExpectedTrend());
		resultMap.put(KPIJobResultAttributes.ISGROUPBY.toString(), kpiDefinition.isGroupBy());
		resultMap.put(KPIJobResultAttributes.RESULT.toString(),result);
		resultMap.put(KPIJobResultAttributes.VECTOR.toString(),kpiDefinition.getVector());
		resultMap.put(KPIJobResultAttributes.TOOLNAME.toString(), kpiDefinition.getToolName());
		resultMap.put(KPIJobResultAttributes.SCHEDULE.toString(), kpiDefinition.getSchedule().name());
		resultMap.put(KPIJobResultAttributes.ACTION.toString(), kpiDefinition.getAction().name());
		resultMap.put(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString(), kpiDefinition.getResultOutPutType());
		resultMap.put(KPIJobResultAttributes.ISCOMPARISIONKPI.toString(), kpiDefinition.isComparisionKpi());
		if(kpiDefinition.isGroupBy()) {
			resultMap.put(KPIJobResultAttributes.GROUPBYFIELDNAME.toString(), kpiDefinition.getGroupByFieldName());
			resultMap.put(KPIJobResultAttributes.GROUPBYFIELDID.toString(), kpiDefinition.getGroupByField());
			resultMap.put(KPIJobResultAttributes.GROUPBYFIELDVAL.toString(),groupByValue);
		}
		resultMap.put(KPIJobResultAttributes.RESULTTIME.toString(),InsightsUtils.getTodayTime());
		//resultMap.put(KPIJobResultAttributes.RESULTTIMEX.toString(),InsightsUtils.getTodayTimeX());
		return resultMap;
	}
	
	protected abstract Map<String, Object> execute() throws InsightsSparkJobFailedException;
	
	protected void saveResult(Map<String, Object> result){
		SparkJobConfigHandler configHandler = new SparkJobConfigHandler();
		configHandler.saveJobResultInES(result);
	}
	
	protected void saveResult(List<Map<String, Object>> resultList){
		SparkJobConfigHandler configHandler = new SparkJobConfigHandler();
		configHandler.saveJobResultInES(resultList);
	}
	
	protected String getEsQueryWithDates(JobSchedule schedule, String esQuery) {
		
		Long fromDate = InsightsUtils.getDataFromTime(schedule.name());
		esQuery = esQuery.replace("__dataFromTime__", fromDate.toString());
		Long toDate = InsightsUtils.getDataToTime(schedule.name());
		esQuery = esQuery.replace("__dataToTime__", toDate.toString());
		return esQuery;
	}
}
