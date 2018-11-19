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
package com.cognizant.devops.platforminsights.core.avg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;

import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.core.function.ESMapFunction;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsSparkJobFailedException;

import scala.Tuple2;

public class AverageActionImpl extends BaseActionImpl {
	
	private static Logger log = LogManager.getLogger(AverageActionImpl.class);

	public AverageActionImpl(KPIDefinition kpiDefinition) {
		super(kpiDefinition);
	}

	@Override
	protected Map<String, Object> execute() throws InsightsSparkJobFailedException {
		log.debug("Calculating KPI Average");
		
		try {
			if(kpiDefinition.isGroupBy()) {
				log.debug("GroupBy found true. Entering GroupBy method");
				JavaRDD<Map<String, Object>> data =  esRDD.values();
				String groupByField = kpiDefinition.getGroupByField(); //Lamda Function fails if you try to set value directly. Serialization error
				String avgField = kpiDefinition.getAverageField();
				JavaPairRDD<String, Tuple2<Long, Integer>> valueCount = data.mapToPair( x -> new Tuple2<String, Long>(x.get(groupByField).toString(),
						Long.valueOf(x.get(avgField).toString()))).mapValues(value -> new Tuple2<Long, Integer>(value,1));
				JavaPairRDD<String, Tuple2<Long, Integer>> reducedCount = valueCount.reduceByKey((tuple1,tuple2) ->  new Tuple2<Long, Integer>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
				
				//calculate average
		        JavaPairRDD<String, Long> averagePair = reducedCount.mapToPair(new InsightsAverageFunction());
		        //print averageByKey
		        Map<String, Long> result =  averagePair.collectAsMap();
		       //HashMap<String, Object> resultMap
		        Set<String> resultKeys = result.keySet();
		        List<Map<String, Object>> resultList = new ArrayList<>();
		        for(String key:resultKeys) {
		        	log.debug(key + "--- "+result.get(key));
		        	Map<String, Object> resultMap = getResultMap(result.get(key),key);
		        	resultList.add(resultMap);
		        }
		        saveResult(resultList);
			} else {
				log.debug("GroupBy found false. Calculating KPI Average");
				JavaRDD<Long> map = esRDD.map(new ESMapFunction(kpiDefinition));
		        Average initial = new Average(0l, 0l);
		        Average avgResult = map.aggregate(initial, new AddToAverage(), new CombineAverage());
		        Map<String, Object> resultMap = getResultMap(avgResult.avg(),null);
		        saveResult(resultMap);
			}
		} catch (Exception e) {
			log.error("Average calculation job failed for kpiID - "+kpiDefinition.getKpiID(), e);
			throw new InsightsSparkJobFailedException("Average calculation job failed for kpiID - "+kpiDefinition.getKpiID(), e);
		}
		return null;
	}

}
