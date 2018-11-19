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
package com.cognizant.devops.platforminsights.core.job.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.elasticsearch.spark.rdd.api.java.JavaEsSpark;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platforminsights.configs.ConfigConstants;
import com.cognizant.devops.platforminsights.core.JavaSparkContextProvider;
import com.cognizant.devops.platforminsights.core.util.FileHandlerUtil;
import com.google.gson.JsonObject;

/**
 * 
 * @author 146414
 * This class provides following functionalities:
 * 1. Loading the spark jobs from ES
 * 2. Updating next run in Job Configuration
 * 3. Saving the JOb configuration in ES
 *
 */
public class SparkJobConfigHandler {
	private static final Logger log = LogManager.getLogger(SparkJobConfigHandler.class);
	
	public List<SparkJobConfiguration> loadJobsFromES(){		
		log.debug("Loading jobs from Elasticsearch");
		Map<String, String> jobConf = new HashMap<String, String>();
		jobConf.put("es.resource",ConfigConstants.SPARK_ES_CONFIGINDEX);
		jobConf.put("es.index.read.missing.as.empty", String.valueOf(true));
		JsonObject jobConfigQuery = FileHandlerUtil.loadJsonFile("/job-config-es-query.json");
		String json = jobConfigQuery.toString();
		/*json = json.replace("__start__", String.valueOf(InsightsUtils.getStartEpochTime()));
		json = json.replace("__end__", String.valueOf(InsightsUtils.getEndEpochTime()));*/
		jobConf.put("es.query", json);
		JavaPairRDD<String,Map<String,Object>> esRDD = JavaEsSpark.esRDD(JavaSparkContextProvider.getJavaSparkContext(), jobConf);
		JavaRDD<SparkJobConfiguration> jobsRDD = esRDD.map(new SparkJobMapFunction());
		return jobsRDD.collect();
	}
	
	public void updateJobsInES(List<SparkJobConfiguration> jobs){
		log.debug("Updating jobs in Elasticsearch");
		JavaRDD<SparkJobConfiguration> jobsRDD = JavaSparkContextProvider.getJavaSparkContext().parallelize(jobs);
		Map<String, String> updateJobConf = new HashMap<String, String>();
		updateJobConf.put("es.write.operation", "update");
		updateJobConf.put("es.mapping.id", "id");
		updateJobConf.put("es.resource", ConfigConstants.SPARK_ES_CONFIGINDEX);
		updateJobConf.put("es.nodes", ConfigConstants.SPARK_ES_HOST);
		updateJobConf.put("es.port", ConfigConstants.SPARK_ES_PORT);
		JavaEsSpark.saveToEs(jobsRDD, updateJobConf);
	}
	
	public void saveJobResultInES(Map<String, Object> result){
		List<Map<String, Object>> resultList = new ArrayList<>();
		resultList.add(result);
		saveJobResultInES(resultList);
	}
	
	public void saveJobResultInES(List<Map<String, Object>> resultList){
		log.debug("Saving jobs in Elasticsearch. ResultList size - "+resultList.size());
		JavaRDD<Map<String, Object>> resultRDD = JavaSparkContextProvider.getJavaSparkContext().parallelize(resultList);
		Map<String, String> updateJobConf = new HashMap<String, String>();
		updateJobConf.put("es.resource", ConfigConstants.SPARK_ES_RESULTINDEX);
		updateJobConf.put("es.nodes", ConfigConstants.SPARK_ES_HOST);
		updateJobConf.put("es.port", ConfigConstants.SPARK_ES_PORT);
		JavaEsSpark.saveToEs(resultRDD, updateJobConf);
	}
	
	
	public void updateNextRun(SparkJobConfiguration config){
		config.setNextRun(String.valueOf(InsightsUtils.getNextRun(config.getSchedule())));
	}
	
	/**
	 * Not using this method. Dates will be replaces in query before execution
	 * Sets next run data collection dates based on schedule
	 * @param config
	 */
	public String updateDataSearchDates(SparkJobConfiguration config){
		Long dataCollectFromTime = InsightsUtils.getDataFromTime(config.getSchedule());
		Long dataCollectToTime = InsightsUtils.getDataToTime(config.getSchedule());
		Map<String,String> kpiDefinition = config.getKpiDefinition();
		
		String esQuery = kpiDefinition.get("esquery").replace("dataFromTime", dataCollectFromTime.toString());
		esQuery = esQuery.replace("dataToTime", dataCollectToTime.toString());
		return esQuery;
	}
	
}
