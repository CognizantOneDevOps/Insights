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
package com.cognizant.devops.platformservice.insights.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.core.enums.KPIJobResultAttributes;
import com.cognizant.devops.platformcommons.core.enums.KPISentiment;
import com.cognizant.devops.platformcommons.core.enums.KPITrends;
import com.cognizant.devops.platformcommons.core.enums.ResultOutputType;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.NodeData;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;

@Service("insightsInferenceService")
public class InsightsInferenceServiceImpl implements InsightsInferenceService {

	private static final Logger log = LogManager.getLogger(InsightsInferenceServiceImpl.class);

	@Autowired
	private ReloadableResourceBundleMessageSource messageSource;

	@Autowired
	public void setMessageSource(ReloadableResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public List<InsightsInference> getInferenceDetails(String schedule) {
		return getInferences(schedule, "");
	}

	@Override
	public List<InsightsInference> getInferenceDetailsVectorWise(String schedule, String vectorType) {
		return getInferences(schedule, vectorType);
	}

	private List<InsightsInference> getInferences(String schedule, String vectorType) {
		List<InsightsInference> inferences = new ArrayList<>(0);

		List<InferenceResult> results = new ArrayList<>();
		try {
			results = getInferenceDataFromNeo4j(schedule.toUpperCase(), vectorType);
			//results = getInferenceData(schedule);

		} catch (Exception e) {
			log.error("Problem getting results", e);
			return inferences;
		}

		Map<String, List<InsightsInferenceDetail>> tempMap = getSortedByVector(schedule, results);



		Set<String> vectorKeys = tempMap.keySet();
		for (String vector : vectorKeys) {
			InsightsInference insightsInference = new InsightsInference();
			insightsInference.setHeading(vector);
			insightsInference.setInferenceDetails(tempMap.get(vector));
			insightsInference.setRanking(1);
			inferences.add(insightsInference);
		}

		return inferences;
	}

	private Map<String, List<InsightsInferenceDetail>> getSortedByVector(String schedule,
			List<InferenceResult> results) {

		Map<String, List<InsightsInferenceDetail>> tempMap = new HashMap<>();
		for (InferenceResult inferenceResult : results) {
			List<InferenceResultDetails> inferenceResultDetailsList = inferenceResult.getDetails();
			InferenceResultDetails resultFirstData = inferenceResultDetailsList.get(0);
			String trend = "No Change";
			KPISentiment sentiment = KPISentiment.NEUTRAL;
			Object[] values = new Object[2];
			if (!resultFirstData.getIsComparisionKpi().booleanValue()) {

				if (resultFirstData.getIsGroupBy().booleanValue()) {
					values[0] = resultFirstData.getGroupByFieldVal();
				} else {
					values[0] = resultFirstData.getResult();
				}
				values[1] = resultFirstData.getResult();

			} else {
				if (inferenceResultDetailsList.size() < 2) {
					continue;
				}
				InferenceResultDetails result2 = inferenceResultDetailsList.get(1);
				sentiment = getSentiment(result2.getResult(), resultFirstData.getResult(),
						resultFirstData.getExpectedTrend());

				values[0] = resultFirstData.getResult();
				values[1] = result2.getResult();
				trend = getTrend(values[0], values[1]);
			}
			Long kpiID = resultFirstData.getKpiID();
			String inferenceName = resultFirstData.getName();
			//String action = resultFirstData.getAction();
			String jobSchedule = resultFirstData.getSchedule();
			String resultOutputType = resultFirstData.getResultOutPutType();
			boolean isComparison = resultFirstData.getIsComparisionKpi();
			//Date lastRunDate = new Date(resultFirstData.getResultTime());
			String vector = resultFirstData.getVector();

			String inferenceText = getInferenceText(vector, kpiID, sentiment, schedule, values,
					isComparison, resultOutputType);

			log.debug("  inferenceText {}  sentiment {} ", inferenceText, sentiment);
			List<ResultSetModel> resultValues = new ArrayList<>();
			int recordcounter = 1;
			for (InferenceResultDetails details : inferenceResultDetailsList) {
				if (recordcounter <= 5) {
					ResultSetModel model = new ResultSetModel();
					model.setValue(details.getResult());
					model.setResultDate(new Date(details.getResultTime()));
					resultValues.add(model);
					recordcounter++;
				}
				
			}
			Collections.reverse(resultValues);
			List<InsightsInferenceDetail> detailsList = getInferenceDetails(inferenceName, sentiment, trend,
					inferenceText, jobSchedule, resultValues, kpiID);
			//log.debug(" inference detail for kpi {} detailsList  {} ", kpiID, detailsList);
			if (tempMap.containsKey(vector)) {
				tempMap.get(vector).addAll(detailsList);
			} else {
				tempMap.put(vector, detailsList);
			}
		}
		return tempMap;

	}

	private List<InsightsInferenceDetail> getInferenceDetails(String name, KPISentiment sentiment, String trend,
			String inferenceLine, String jobSchedule, List<ResultSetModel> result,
			Long kpiID) {

		List<InsightsInferenceDetail> details = new ArrayList<>(10);

		InsightsInferenceDetail detail = new InsightsInferenceDetail();
		detail.setKpiName(name);
		detail.setSentiment(sentiment.toString().toUpperCase());
	//	detail.setAction(ExecutionActions.valueOf(action));
		detail.setTrendline(trend);
		detail.setInference(inferenceLine);
		detail.setSchedule(jobSchedule);
	//	detail.setLastRun(lastRunDate);
		detail.setResultSet(result);
		detail.setKpiId(kpiID);
		details.add(detail);

		return details;
	}

	/*private List<InferenceResult> getInferenceData(String inputSchedule) throws Exception {
		String esQuery = getQuery();
	
		esQuery = getUpdatedQueryWithDate(esQuery, inputSchedule, 5); // Since  should  come  from  config  file
	
		ElasticSearchDBHandler esDBHandler = new ElasticSearchDBHandler();
		String sparkElasticSearchHost = ApplicationConfigProvider.getInstance().getSparkConfigurations()
				.getSparkElasticSearchHost();
		String sparkElasticSearchPort = ApplicationConfigProvider.getInstance().getSparkConfigurations()
				.getSparkElasticSearchPort();
		String sparkElasticSearchResultIndex = ApplicationConfigProvider.getInstance().getSparkConfigurations()
				.getSparkElasticSearchResultIndex();
		JsonObject jsonObj = esDBHandler.queryES(sparkElasticSearchHost + ":" + sparkElasticSearchPort + "/"
				+ sparkElasticSearchResultIndex + "/_search?filter_path=aggregations", esQuery);
		log.debug(" ES query result " + jsonObj);
		List<InferenceResult> inferenceResults = JsonToObjectConverter.getInferenceResult(jsonObj);
	
		return inferenceResults;
	}*/

	private String getInferenceText(String vector, Long kpiId, KPISentiment sentiment,
			String schedule, Object[] values, boolean isComparison, String resultOutputType) {
		boolean zeroVal = false;
		boolean currentZeroVal = false;
		String messageId = "";
		String msgZeroCode = "";

		if (isComparison && values[0] instanceof Long && values[1] instanceof Long) {
				if ((Long) values[0] == 0 && (Long) values[1] > 0) {
					zeroVal = true;
					currentZeroVal = true;
				} else if ((Long) values[0] > 0 && (Long) values[1] == 0) {
					zeroVal = true;
				} else if ((Long) values[0] > 0 && (Long) values[1] >= 0) {
					if (resultOutputType.toLowerCase().contains(ResultOutputType.TIMERESULTOUTPUT.toString())) {
						Long result = (Long) values[0];
						Long secVal = TimeUnit.MILLISECONDS.toSeconds(result.longValue());//(Long) values[i] / 1000.0f;
						values[0] = secVal;

						result = (Long) values[1];
						secVal = TimeUnit.MILLISECONDS.toSeconds(result.longValue());
						values[1] = secVal;
					}
				} else if ((Long) values[0] == 0 && (Long) values[1] == 0) {
					log.info(" all value are zero ");
				}
			}
		if (zeroVal) {
			if (currentZeroVal) {
				msgZeroCode = InsightsMessageEnum.CURRENTZEROVALMSG.toString();
				messageId = vector.toLowerCase() + "." + kpiId + "." + sentiment.toString() + "."
						+ schedule.toLowerCase() + "." + msgZeroCode;
			} else {
				msgZeroCode = InsightsMessageEnum.PREVIOUSVALZERO.toString();
				messageId = vector.toLowerCase() + "." + kpiId + "." + sentiment.toString() + "."
						+ schedule.toLowerCase() + "." + msgZeroCode;
			}

		} else {
			messageId = vector.toLowerCase() + "." + kpiId + "." + sentiment.toString() + "." + schedule.toLowerCase();
		}
		String inferenceText = messageSource.getMessage(messageId, values, Locale.getDefault());
		return inferenceText;
	}

	private KPISentiment getSentiment(Object object1, Object object2, String trend) {
		Long lastWeekVal = (Long) object1;
		Long previousWeek = (Long) object2;
		if (trend.equalsIgnoreCase(KPITrends.DOWNWARDS.getValue())) {
			if (lastWeekVal > previousWeek) {
				return KPISentiment.POSITIVE;
			} else if (lastWeekVal < previousWeek) {
				return KPISentiment.NEGATIVE;
			} else {
				return KPISentiment.NEUTRAL;
			}
		} else if (trend.equalsIgnoreCase(KPITrends.UPWARDS.getValue())) {
			if (lastWeekVal < previousWeek) {
				return KPISentiment.POSITIVE;
			} else if (lastWeekVal > previousWeek) {
				return KPISentiment.NEGATIVE;
			} else {
				return KPISentiment.NEUTRAL;
			}
		} else {
			return KPISentiment.NEUTRAL;
		}

	}

	private String getTrend(Object object1, Object object2) {
		Long lastWeekVal = (Long) object1;
		Long previousWeek = (Long) object2;

		if (lastWeekVal >= previousWeek) {
			return "Low to High";
		} else if (0 == lastWeekVal.compareTo(previousWeek)) {
			return "No Change";
		} else {
			return "High to Low";
		}

	}

	/*
		private String getQuery() {
	
			return "{\r\n" + "  \"size\": 0,\r\n" + "  \"query\": {\r\n" + "    \"bool\": {\r\n" + "      \"must\": [\r\n"
					+ "        {\r\n" + "          \"match\": {\r\n" + "            \"schedule\": \"__schedule__\"\r\n"
					+ "          }\r\n" + "        },\r\n" + "        {\r\n" + "          \"bool\": {\r\n"
					+ "            \"must\": [\r\n" + "              {\r\n" + "                \"range\": {\r\n"
					+ "                  \"resultTime\": {\r\n" + "                    \"gte\": \"__FromDate__\",\r\n"
					+ "                    \"lte\": \"__ToDate__\",\r\n"
					+ "                    \"format\": \"epoch_millis\"\r\n" + "                  }\r\n"
					+ "                }\r\n" + "              }\r\n" + "            ]\r\n" + "          }\r\n"
					+ "        }\r\n" + "      ]\r\n" + "    }\r\n" + "  },\r\n" + "  \"aggs\": {\r\n"
					+ "    \"terms\": {\r\n" + "      \"terms\": {\r\n" + "\"size\":30,"
					+ "        \"field\": \"kpiID\"\r\n" + "      },\r\n" + "      \"aggs\": {\r\n"
					+ "        \"top_tag_hits\": {\r\n" + "          \"top_hits\": {\r\n" + "            \"_source\": {\r\n"
					+ "              \"include\": [\r\n" + "                \"vector\",\r\n"
					+ "                \"expectedTrend\",\r\n" + "                \"result\",\r\n"
					+ "                \"schedule\",\r\n" + "                \"action\",\r\n"
					+ "                \"kpiID\",\r\n" + "                \"name\",\r\n"
					+ "                \"toolName\",\r\n" + "                \"resultTime\",\r\n"
					+ "                \"resultOutPutType\",\r\n" + "                \"isComparisionKpi\",\r\n"
					+ "                \"isGroupBy\",\r\n" + "                \"groupByFieldVal\",\r\n"
					+ "                \"groupByName\"\r\n" + "              ]\r\n" + "            },\r\n"
					+ "            \"sort\": {\r\n" + "              \"resultTime\": {\r\n"
					+ "                \"order\": \"desc\"\r\n" + "              }\r\n" + "            }\r\n"
					+ "          }\r\n" + "        }\r\n" + "      }\r\n" + "    }\r\n" + "  }\r\n" + "}";
		}
	
		private String getUpdatedQueryWithDate(String esQuery, String schedule, Integer since) {
			esQuery = esQuery.replace("__FromDate__", InsightsUtils.getDataFromTime(schedule, since) + "");
			esQuery = esQuery.replace("__ToDate__", InsightsUtils.getTodayTime().toString());
			esQuery = esQuery.replace("__schedule__", schedule);
			return esQuery;
		}
	*/
	private String getNeo4jQueryWithDates(String inputSchedule, String vectorType, Integer since) {
		String cypherQuery = "MATCH (n:INFERENCE:RESULTS) where exists(n.kpiID) AND n.schedule= '" + inputSchedule
				+ "' ";
		cypherQuery = cypherQuery + " AND n.resultTime >  " + InsightsUtils.getDataFromTime(inputSchedule, since)
				+ " AND n.resultTime < " + InsightsUtils.getTodayTime().toString();//+ "'"
		if (!"".equalsIgnoreCase(vectorType)) {
			cypherQuery = cypherQuery + " and n.vector='" + vectorType + "' ";
		}
		cypherQuery = cypherQuery + " RETURN n  order by n.kpiID,n.resultTime desc ";
		return cypherQuery;
	}

	private List<InferenceResult> getInferenceDataFromNeo4j(String inputSchedule, String vectorType)
			throws InsightsCustomException {
		List<InferenceResult> inferenceResults = null;
		GraphDBHandler graphDBHandler = new GraphDBHandler();
		try {
			String graphQuery = getNeo4jQueryWithDates(inputSchedule, vectorType, 5000);
			log.debug(" graphQuery {}  ", graphQuery);
			GraphResponse graphResp = graphDBHandler.executeCypherQuery(graphQuery);
			JsonArray errorMessage = graphResp.getJson().getAsJsonArray("errors");
			if (errorMessage.size() >= 1) {
				String errorMessageText = errorMessage.get(0).getAsJsonObject().get("message").getAsString();
				log.error(" error while executing neo4j query and error is {}  ", errorMessageText);
				throw new InsightsCustomException(errorMessageText);
			}
			inferenceResults = parseNeo4jResponse(graphResp);
		} catch (Exception e) {
			log.error(" error while executing neo4j query in getInferenceDataFromNeo4j {} ,{} ", e.getCause(),
					e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

		return inferenceResults;
	}

	private List<InferenceResult> parseNeo4jResponse(GraphResponse graphResp) {

		List<InferenceResult> inferenceResults = new ArrayList<>(0);
		List<InferenceResultDetails> inferenceDetailList = new ArrayList<>(0);

		try {

			List<NodeData> nodesList = graphResp.getNodes();
			for (NodeData nodeData : nodesList) {
				InferenceResultDetails nodemapping = mapNodeData(nodeData);
				inferenceDetailList.add(nodemapping);
			}
			Map<Long, List<InferenceResultDetails>> kpiInferenceResultGrouped = inferenceDetailList.stream()
					.collect(Collectors.groupingBy(InferenceResultDetails::getKpiID));

			log.debug(" kpiInferenceResulGrouped {} ", kpiInferenceResultGrouped);

			for (Map.Entry<Long, List<InferenceResultDetails>> kipInferanceResult : kpiInferenceResultGrouped
					.entrySet()) {
				InferenceResult ir = new InferenceResult();
				ir.setKpiID(kipInferanceResult.getKey());
				ir.setDetails(kipInferanceResult.getValue());
				ir.setTotalDocuments((long) kipInferanceResult.getValue().size());
				inferenceResults.add(ir);
			}
		} catch (Exception e) {
			log.error(" Error while parsing neo4j responce {} {} ", e.getCause(), e.getMessage());
		}
		return inferenceResults;
	}

	private InferenceResultDetails mapNodeData(NodeData node) {
		InferenceResultDetails neo4jDef = new InferenceResultDetails();
		neo4jDef.setKpiID(Long.parseLong(node.getPropertyMap().get("kpiID")));
		neo4jDef.setName(node.getPropertyMap().get(KPIJobResultAttributes.NAME.toString()));
		neo4jDef.setVector(node.getPropertyMap().get(KPIJobResultAttributes.VECTOR.toString()));
		neo4jDef.setExpectedTrend(node.getPropertyMap().get(KPIJobResultAttributes.EXPECTEDTREND.toString()));
		neo4jDef.setResult(Long.parseLong(node.getPropertyMap().get("result")));
		neo4jDef.setSchedule(node.getPropertyMap().get(KPIJobResultAttributes.SCHEDULE.toString()));
		neo4jDef.setAction(node.getPropertyMap().get(KPIJobResultAttributes.ACTION.toString()));
		neo4jDef.setResultTime(Long.parseLong(node.getPropertyMap().get(KPIJobResultAttributes.RESULTTIME.toString())));
		neo4jDef.setToolName(node.getPropertyMap().get(KPIJobResultAttributes.TOOLNAME.toString()));
		neo4jDef.setIsComparisionKpi(
				Boolean.parseBoolean(node.getPropertyMap().get(KPIJobResultAttributes.ISCOMPARISIONKPI.toString())));
		neo4jDef.setIsGroupBy(
				Boolean.parseBoolean(node.getPropertyMap().get(KPIJobResultAttributes.ISGROUPBY.toString())));
		neo4jDef.setGroupByName(node.getPropertyMap().get(KPIJobResultAttributes.GROUPBYFIELDNAME.toString()));
		neo4jDef.setGroupByField(node.getPropertyMap().get(KPIJobResultAttributes.GROUPBYFIELDID.toString()));
		neo4jDef.setGroupByFieldVal(node.getPropertyMap().get(KPIJobResultAttributes.GROUPBYFIELDVAL.toString()));
		neo4jDef.setResultOutPutType(node.getPropertyMap().get(KPIJobResultAttributes.RESULTOUTPUTTYPE.toString()));
		neo4jDef.setResultTimeX(node.getPropertyMap().get(KPIJobResultAttributes.RESULTTIMEX.toString()));

		return neo4jDef;
	}
}
