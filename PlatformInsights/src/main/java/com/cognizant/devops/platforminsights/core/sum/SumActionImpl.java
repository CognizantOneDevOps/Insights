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
package com.cognizant.devops.platforminsights.core.sum;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
/*
 * import org.apache.spark.api.java.JavaPairRDD;
 * import org.apache.spark.api.java.JavaRDD;
 */

import com.cognizant.devops.platforminsights.core.BaseActionImpl;
import com.cognizant.devops.platforminsights.core.function.Neo4jDBImp;
import com.cognizant.devops.platforminsights.datamodel.KPIDefinition;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;

// import scala.Tuple2;

public class SumActionImpl extends BaseActionImpl {

	private static Logger log = Logger.getLogger(SumActionImpl.class);

	public SumActionImpl(KPIDefinition kpiDefinition) {
		super(kpiDefinition);
	}

	public SumActionImpl(Neo4jKPIDefinition neo4jKpiDefinition) {
		super(neo4jKpiDefinition);
	}

	@Override
	protected Map<String, Object> execute() throws InsightsJobFailedException {
		log.debug("Calculating KPI Sum");
		if (kpiDefinition.getDbType() == null) {
			executeESQuery();
		} else if (kpiDefinition.getDbType().equalsIgnoreCase("neo4j")) {
			executeGraphQuery();
		} else if (kpiDefinition.getDbType().equalsIgnoreCase("elasticsearch")) {
			executeESQuery();
		}

		return null;
	}

	private void executeGraphQuery() {
		if (kpiDefinition.getDbType().equalsIgnoreCase("neo4j")) {
			try {
				Neo4jDBImp graphDb = new Neo4jDBImp(kpiDefinition);
				List<Map<String, Object>> graphResposne = graphDb.getNeo4jResult();
				saveResult(graphResposne);

			} catch (Exception e) {
				log.error("Sum calculation job failed for kpiID - " + kpiDefinition.getKpiID(), e);
			}
		}
	}

	private void executeESQuery() throws InsightsJobFailedException {
		/*if (kpiDefinition.getDbType() == null || kpiDefinition.getDbType().equalsIgnoreCase("elasticsearch")) {
			try {
				if (kpiDefinition.isGroupBy()) {
					log.debug("GroupBy found true. Entering GroupBy method");
					JavaRDD<Map<String, Object>> data = esRDD.values();
					String groupByField = kpiDefinition.getGroupByField(); // Lamda Function fails if you try to set
																			// value directly. Serialization error
					String sumField = kpiDefinition.getSumCalculationField();
					JavaPairRDD<String, Tuple2<Long, Integer>> valueCount = data
							.mapToPair(x -> new Tuple2<String, Long>(x.get(groupByField).toString(),
									Long.valueOf(x.get(sumField).toString())))
							.mapValues(value -> new Tuple2<Long, Integer>(value, 1));
					JavaPairRDD<String, Tuple2<Long, Integer>> reducedCount = valueCount.reduceByKey((tuple1,
							tuple2) -> new Tuple2<Long, Integer>(tuple1._1 + tuple2._1, tuple1._2 + tuple2._2));
		
					// calculate average
					JavaPairRDD<String, Long> sumPair = reducedCount.mapToPair(new InsightsSumFunction());
					// print averageByKey
					Map<String, Long> result = sumPair.collectAsMap();
					// HashMap<String, Object> resultMap
					Set<String> resultKeys = result.keySet();
					List<Map<String, Object>> resultList = new ArrayList<>();
					for (String key : resultKeys) {
						log.debug(key + "--- " + result.get(key));
						Map<String, Object> resultMap = getResultMap(result.get(key), key);
						resultList.add(resultMap);
					}
					saveResult(resultList);
				} else {
					log.debug("GroupBy found false. Calculating KPI Sum");
					JavaRDD<Long> map = esRDD.map(new ESMapFunction(kpiDefinition));
					Sum initial = new Sum(0l, 0l);
					Sum avgResult = map.aggregate(initial, new AddToSum(), new CombineSum());
					Map<String, Object> resultMap = getResultMap(avgResult.avg(), null);
					saveResult(resultMap);
				}
			} catch (Exception e) {
				log.error("Sum calculation job failed for kpiID - " + kpiDefinition.getKpiID(), e);
				throw new InsightsSparkJobFailedException(
						"Sum calculation job failed for kpiID - " + kpiDefinition.getKpiID(), e);
			}
		}*/
	}

	@Override
	public void executeNeo4jGraphQuery() {
		//if (kpiDefinition.getDbType().equalsIgnoreCase("neo4j")) {
		try {
			Neo4jDBImp graphDb = new Neo4jDBImp(neo4jKpiDefinition);
			List<Map<String, Object>> graphResposne = graphDb.getNeo4jResult();
			log.debug(" graphResposne  " + graphResposne);
			saveResultInNeo4j(graphResposne);

		} catch (Exception e) {
			log.error("Sum calculation job failed for kpiID - " + kpiDefinition.getKpiID(), e);
		}
		//}
	}

}
