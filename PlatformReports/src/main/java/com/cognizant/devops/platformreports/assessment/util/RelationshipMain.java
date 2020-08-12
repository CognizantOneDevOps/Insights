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
package com.cognizant.devops.platformreports.assessment.util;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.ValidationException;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformdal.dal.ElasticSearchNativeHandler;

public class RelationshipMain {
//	private static Logger log = LogManager.getLogger(RelationshipMain.class);
//	static ElasticSearchNativeHandler esClientDeo;
//	public static void main(String[] args) {

//		try {
//			ApplicationConfigCache.loadConfigCache();
//			//String indexName = "neo4jsDataCopy-relation-m".toLowerCase();
//			/*String indexName = "twitter-family";
//			String index_ES = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint() + "/"
//					+ indexName + "/uuid"; //+ "/" + "/" + "configs"
//			log.debug(index_ES);
//			//PlatformInsightsDataExecutor insightsData = new PlatformInsightsDataExecutor();
//			//insightsData.getAndSaveEsCachedResultsWithRelationship(index_ES);
//			
//			RelationshipMain relationshipMain = new RelationshipMain();
//			relationshipMain.mappingCreate(indexName);*/
//
//			String graphQuery = "{\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":\"%kpiId\" } } ]}}";
//			graphQuery = graphQuery.replaceAll("%kpiId", String.valueOf(2)).replaceAll("$executionId",
//					String.valueOf(3));
//
//			esClientDeo = new ElasticSearchNativeHandler();
//
//			String esQuery5 = " {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":111 } },{ \"match\":{ \"executionId\":1592476841871 } } ]}}";
//
//			esClientDeo.fetchByJsonQuery(graphQuery, "kpi-results", 10);
//
//			String apiQuery = "{\r\n" + "  \"size\": 20,\r\n" + "    \"sort\": [\r\n"
//					+ "        { \"executionId\": \"desc\" }\r\n" + "    ],\r\n" + "  \"query\": {\r\n"
//					+ "    \"bool\":{\r\n" + "           \"must\":[\r\n"
//					+ "              { \"match\":{ \"kpiId\":111 } }\r\n" + "           ]\r\n" + "      }\r\n"
//					+ "  }\r\n" + "}";
//			esClientDeo.getESResult(apiQuery, ReportEngineUtils.ES_KPI_RESULT_INDEX);
//
//			//relationshipMain.mappingCreateGitToJenkins("git_to_jenkins");
//
//			/*relationshipMain.testQuery();*/
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			log.error(e);
//		}
//
//	}
//
//	@SuppressWarnings("unused")
//	public void mappingCreate(String indexName) {
//		try {
//
//			log.debug(" mappingCreate ");
//			XContentBuilder mapping = XContentFactory.jsonBuilder()
//					.startObject()
//					//.startObject("settings").startObject("index").field("number_of_shards", "2")
//					//.field("number_of_replicas", "2").endObject().endObject()
//					//.startObject("mappings")
//					//.startObject("twitter-family")
//					.startObject("properties")
//					/*.startObject("key")
//	                                    .field("type", "string")
//	                                    .field("search_analyzer", "standard")
//	                                    .field("index_analyzer", "partial_filename_analyzer")
//	                                .endObject()*/
//					.startObject("firstName").field("type", "text").endObject()
//
//					.startObject("lastName").field("type", "text").endObject()
//
//					.startObject("gender").field("type", "text").endObject()
//
//					.startObject("isAlive").field("type", "boolean").endObject()
//
//					.startObject("relation_type").field("type", "join").field("eager_global_ordinals", "true")
//					.startObject("relations")
//					.field("parent", "child").endObject().endObject()
//
//					.endObject()
//					//.endObject()
//					//.endObject()
//					.endObject();
//
//			String mappingJson = Strings.toString(mapping);
//
//			log.debug("mappingJson  " + mappingJson);
//
//			GetIndexRequest requestGet = new GetIndexRequest(indexName);
//			boolean exists = esClientDeo.getClient().indices().exists(requestGet, RequestOptions.DEFAULT);
//
//			if (!exists) {
//				log.debug(" Index " + indexName + " not exists so creting index ");
//				CreateIndexRequest ir = new CreateIndexRequest(indexName);
//				ir.mapping(mappingJson.toString(), XContentType.JSON);
//				//ir.routing(indexName);
//				//IndexResponse indexRes = esClient.index(ir, RequestOptions.DEFAULT);
//				CreateIndexResponse createIndexResponse = esClientDeo.getClient().indices().create(ir,
//						RequestOptions.DEFAULT);
//				log.debug("indexRes  " + createIndexResponse);
//			}
//
//			GetMappingsRequest requestGetMapping = new GetMappingsRequest();
//			requestGetMapping.indices(indexName);
//			GetMappingsResponse getMappingResponse = esClientDeo.getClient().indices().getMapping(requestGetMapping,
//					RequestOptions.DEFAULT);
//			Map<String, MappingMetaData> allMappings = getMappingResponse.mappings();
//
//			for (Map.Entry<String, MappingMetaData> mappingMap : allMappings.entrySet()) {
//
//				log.debug(" key Map " + mappingMap.getKey());
//
//			}
//
//			MappingMetaData indexMapping = allMappings.get(indexName);
//			Map<String, Object> mappingGetData = indexMapping.sourceAsMap();
//			log.debug(" mappingGetData  " + mappingGetData.size());
//			//If mapping not found than create new one
//			if (mappingGetData.isEmpty()) {
//				log.debug("Mapping for indexName " + indexName + " is empty " + "Need to create that ");
//				PutMappingRequest request = new PutMappingRequest(indexName + "/_mapping");
//				request.setTimeout(TimeValue.timeValueMinutes(2));
//				request.setMasterTimeout(TimeValue.timeValueMinutes(1));
//				//request.type("default");
//				request.source(mapping);
//				//request.timeout(TimeValue.timeValueMinutes(2));
//
//				Optional<ValidationException> validationException = request.validate();
//				//log.debug(" validationExceptionData   " + validationException.get());
//				org.elasticsearch.action.support.master.AcknowledgedResponse putMappingResponse = esClientDeo
//						.getClient().indices()
//						.putMapping(request, RequestOptions.DEFAULT);
//				log.debug(" putMappingResponse " + putMappingResponse.toString() + "   "
//						+ putMappingResponse.isAcknowledged() + "  " + putMappingResponse.isFragment());
//			}
//			//Add parent data
//
//			IndexRequest irParent = new IndexRequest(indexName);
//			irParent.source(
//					"{      \"firstName\":\"Darren\",      \"lastName\":\"Ford\",      \"gender\":\"Male\",      \"isAlive\":false,      \"relation_type\":{          \"name\":\"parent\"      }  }",
//					XContentType.JSON);
//			irParent.routing(indexName);
//			irParent.id("1");
//			IndexResponse createIndexResponseParent = esClientDeo.getClient().index(irParent, RequestOptions.DEFAULT);
//
//			IndexRequest irParent2 = new IndexRequest(indexName);
//			irParent2.source(
//					"{      \"firstName\":\"t\",      \"lastName\":\"PFord\",      \"gender\":\"Male\",      \"isAlive\":false,      \"relation_type\":{          \"name\":\"parent\"      }  }",
//					XContentType.JSON);
//			irParent2.routing(indexName);
//			irParent2.id("111");
//			IndexResponse createIndexResponseParent2 = esClientDeo.getClient().index(irParent2, RequestOptions.DEFAULT);
//
//			//add child data
//
//			IndexRequest irchild1 = new IndexRequest(indexName);
//			irchild1.source(
//					"{ \"firstName\":\"Pearl\",      \"lastName\":\"Ford\",      \"gender\":\"Female\",      \"isAlive\":true,      \"relation_type\":{          \"name\":\"child\",          \"parent\":\"1\"      }  }",
//					XContentType.JSON);
//			irchild1.routing(indexName);
//			irchild1.id("2");
//			IndexResponse createIndexResponseChild1 = esClientDeo.getClient().index(irchild1, RequestOptions.DEFAULT);
//
//			IndexRequest irchild2 = new IndexRequest(indexName);
//			irchild2.source(
//					"{      \"firstName\":\"andrew\",      \"lastName\":\"Ford\",      \"gender\":\"Male\",      \"isAlive\":true,      \"relation_type\":{          \"name\":\"child\",          \"parent\":\"1\"      }  }",
//					XContentType.JSON);
//			irchild2.routing(indexName);
//			irchild2.id("11");
//			IndexResponse createIndexResponseChild2 = esClientDeo.getClient().index(irchild2, RequestOptions.DEFAULT);
//
//			IndexRequest irchild3 = new IndexRequest(indexName);
//			irchild3.source(
//					"{      \"firstName\":\"t\",      \"lastName\":\"Ford\",      \"gender\":\"Male\",      \"isAlive\":true,      \"relation_type\":{          \"name\":\"child\",          \"parent\":\"111\"      }  }",
//					XContentType.JSON);
//			irchild3.routing(indexName);
//			irchild3.id("112");
//			IndexResponse createIndexResponseChild3 = esClientDeo.getClient().index(irchild3, RequestOptions.DEFAULT);
//
//			IndexRequest irchild4 = new IndexRequest(indexName);
//			irchild4.source(
//					"{      \"firstName\":\"Darren2\",      \"lastName\":\"Ford2\",      \"gender\":\"Male\",      \"isAlive\":false,   \"Portfolio\": \"InSights\",     \"SprintID\": \"S75\",     \"ProgramName\": \"ALLADININT\",     \"uuid\": \"043a9180-c386-11e6-875c-005056b1008e\",     \"execId\": \"e094be91-c385-11e6-8a9c-5cc5d4d3e5e0\",     \"repoType\": \"testing\",     \"git_commitId\": \"9dcc92e315421da0cb271cf7105039b8b46b88bb\",     \"inSightTimeX\": \"2016-12-15T02:45:47Z\",     \"ToolName\": \"GIT\",     \"git_authorName\": \"mayankdevops\",     \"git_reponame\": \"AnyBank\",     \"dataCopyProcessedFlag\": false,     \"inSightsTime\": 1481770487.0,     \"git_commiTime\": \"2016-12-15T08:45:47Z\",     \"PortfolioName\": \"IWM\",     \"toolName\": \"GIT\"     , \"relation_type\":{          \"name\":\"child\",          \"parent\":\"111\"      }  }",
//					XContentType.JSON);
//			irchild4.routing(indexName);
//			irchild4.id("114");
//			IndexResponse createIndexResponseChild4 = esClientDeo.getClient().index(irchild4, RequestOptions.DEFAULT);
//
//			log.debug(" All data created with mapping field ");
//
//
//			fetchAllData(indexName);
//		} catch (Exception e) {
//			log.debug(e);
//			log.error(e);
//		}
//
//	}
//
//	@SuppressWarnings("unused")
//	public void mappingCreateGitToJenkins(String indexName) {
//		try {
//
//			log.debug(" mappingCreate GitToJenkins ");
//			XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("properties")
//					.startObject("toolName").field("type", "text").endObject().startObject("git_to_jenkins")
//					.field("type", "join").field("eager_global_ordinals", "true").startObject("relations")
//					.field("git", "jenkins").endObject().endObject().endObject().endObject();
//
//			String mappingJson = Strings.toString(mapping);
//
//			log.debug("mappingJson GitToJenkins " + mappingJson);
//
//			GetIndexRequest requestGet = new GetIndexRequest(indexName);
//			boolean exists = esClientDeo.getClient().indices().exists(requestGet, RequestOptions.DEFAULT);
//
//			if (!exists) {
//				log.debug(" Index " + indexName + " not exists so creting index ");
//				CreateIndexRequest ir = new CreateIndexRequest(indexName);
//				ir.mapping(mappingJson.toString(), XContentType.JSON);
//				CreateIndexResponse createIndexResponse = esClientDeo.getClient().indices().create(ir,
//						RequestOptions.DEFAULT);
//				log.debug("indexRes  " + createIndexResponse);
//			}
//
//			GetMappingsRequest requestGetMapping = new GetMappingsRequest();
//			requestGetMapping.indices(indexName);
//			GetMappingsResponse getMappingResponse = esClientDeo.getClient().indices().getMapping(requestGetMapping,
//					RequestOptions.DEFAULT);
//			Map<String, MappingMetaData> allMappings = getMappingResponse.mappings();
//
//			for (Map.Entry<String, MappingMetaData> mappingMap : allMappings.entrySet()) {
//
//				log.debug(" key Map " + mappingMap.getKey());
//
//			}
//
//			MappingMetaData indexMapping = allMappings.get(indexName);
//			Map<String, Object> mappingGetData = indexMapping.sourceAsMap();
//			log.debug(" mappingGetData  " + mappingGetData.size());
//			//If mapping not found than create new one
//			if (mappingGetData.isEmpty()) {
//				log.debug("Mapping for indexName " + indexName + " is empty " + "Need to create that ");
//				PutMappingRequest request = new PutMappingRequest(indexName + "/_mapping");
//				request.setTimeout(TimeValue.timeValueMinutes(2));
//				request.setMasterTimeout(TimeValue.timeValueMinutes(1));
//
//				request.source(mapping);
//				Optional<ValidationException> validationException = request.validate();
//				//log.debug(" validationExceptionData   " + validationException.get());
//				org.elasticsearch.action.support.master.AcknowledgedResponse putMappingResponse = esClientDeo
//						.getClient().indices().putMapping(request, RequestOptions.DEFAULT);
//				log.debug(" putMappingResponse " + putMappingResponse.toString() + "   "
//						+ putMappingResponse.isAcknowledged() + "  " + putMappingResponse.isFragment());
//			}
//			//Add parent data
//
//			IndexRequest irParent = new IndexRequest(indexName);
//			irParent.source(
//					"{     \"jiraKey\": \"LS-1482478349\",    \"repoName\": \"Insights\",    \"gitReponame\": \"InsightsTest\",    \"gitCommiTime\": \"2018-05-20T23:56:39Z\",    \"maxCorrelationTime\": 1574321340,    \"commitId\": \"CM-9796279396\",    \"inSightsTimeX\": \"2018-05-20T23:56:39Z\",    \"message\": \"This commit is associated with jira-key : LS-1482478349\",    \"uuid\": \"dda8b388-0c27-11ea-ad12-2016b9b2130e\",    \"categoryName\": \"SCM\",    \"execId\": \"5f067cde-0c27-11ea-a8c4-106530e9217d\",    \"gitAuthorName\": \"Vishwajit\",    \"inSightsTime\": 1526840799,    \"correlationTime\": 1574317740,    \"gitCommitId\": \"DXYctog1VaD9scUICaOv6KhokDwrS5tn\",    \"toolName\": \"GIT\",      \"git_to_jenkins\":{          \"name\":\"git\"      }  }",
//					XContentType.JSON);
//			irParent.routing(indexName);
//			irParent.id("dda8b388-0c27-11ea-ad12-2016b9b2130e");
//			IndexResponse createIndexResponseParent = esClientDeo.getClient().index(irParent, RequestOptions.DEFAULT);
//
//
//			//add child data
//
//			IndexRequest irchild1 = new IndexRequest(indexName);
//			irchild1.source(
//					"{    \"jobName\": \"ClaimValidated\",    \"maxCorrelationTime\": 1574321340,    \"inSightsTimeX\": \"2018-05-20T23:58:39Z\",    \"buildNumber\": \"0664848441\",    \"uuid\": \"e3011c07-0c27-11ea-ad12-2016b9b2130e\",    \"categoryName\": \"CI\",    \"master\": \"master2\",    \"scmcommitId\": \"CM-9796279396\",    \"execId\": \"5f067cde-0c27-11ea-a8c4-106530e9217d\",    \"result\": \"SUCCESS\",    \"duration\": 334,    \"environment\": \"PROD\",    \"jenkins_date\": \"2018-05-20 23:58:39\",    \"buildUrl\": \"productv4.3.devops.com\",    \"startTime\": \"2018-05-20T23:58:39Z\",    \"inSightsTime\": 1526840919,    \"endTime\": \"2018-05-21T00:04:13Z\",    \"correlationTime\": 1574317740,    \"projectName\": \"ClaimFinder\",    \"projectID\": \"1001\",    \"toolName\": \"JENKINS\",    \"status\": \"Failure\",      \"git_to_jenkins\":{          \"name\":\"jenkins\",          \"parent\":\"CM-9796279396\"      }  }",
//					XContentType.JSON);
//			irchild1.routing(indexName);
//			irchild1.id("e3011c07-0c27-11ea-ad12-2016b9b2130e");
//			IndexResponse createIndexResponseChild1 = esClientDeo.getClient().index(irchild1, RequestOptions.DEFAULT);
//
//			IndexRequest irParent2 = new IndexRequest(indexName);
//			irParent2.source(
//					"{    \"jiraKey\": \"LS-1482478349\",    \"repoName\": \"Insights\",    \"gitReponame\": \"InsightsDemo\",    \"gitCommiTime\": \"2018-05-20T23:58:54Z\",    \"maxCorrelationTime\": 1574321340,    \"commitId\": \"CM-7292072118\",    \"inSightsTimeX\": \"2018-05-20T23:58:54Z\",    \"message\": \"This commit is associated with jira-key : LS-1482478349\",    \"uuid\": \"dda8b38d-0c27-11ea-ad12-2016b9b2130e\",    \"categoryName\": \"SCM\",    \"execId\": \"5f067cde-0c27-11ea-a8c4-106530e9217d\",    \"gitAuthorName\": \"Mayank\",    \"inSightsTime\": 1526840934,    \"correlationTime\": 1574317740,    \"gitCommitId\": \"DXYctog1VaD9scUICaOv6KhokDwrS5tn\",    \"toolName\": \"GIT\",      \"git_to_jenkins\":{          \"name\":\"git\"   }  }",
//					XContentType.JSON);
//			irParent2.routing(indexName);
//			irParent2.id("dda8b38d-0c27-11ea-ad12-2016b9b2130e");
//			IndexResponse createIndexResponseParent2 = esClientDeo.getClient().index(irParent2, RequestOptions.DEFAULT);
//
//			IndexRequest irchild2 = new IndexRequest(indexName);
//			irchild2.source(
//					"{    \"jobName\": \"ClaimProcessed\",    \"maxCorrelationTime\": 1574321340,    \"inSightsTimeX\": \"2018-05-21T00:00:54Z\",    \"buildNumber\": \"2143741557\",    \"uuid\": \"e3011c0b-0c27-11ea-ad12-2016b9b2130e\",    \"categoryName\": \"CI\",    \"scmcommitId\": \"CM-7292072118\",    \"master\": \"master2\",    \"execId\": \"5f067cde-0c27-11ea-a8c4-106530e9217d\",    \"result\": \"ABORTED\",    \"duration\": 453,    \"environment\": \"PROD\",    \"jenkins_date\": \"2018-05-21 00:00:54\",    \"buildUrl\": \"productv4.2.devops.com\",    \"startTime\": \"2018-05-21T00:00:54Z\",    \"inSightsTime\": 1526841054,    \"endTime\": \"2018-05-21T00:08:27Z\",    \"correlationTime\": 1574317740,    \"projectName\": \"PaymentServices\",    \"projectID\": \"1002\",    \"toolName\": \"JENKINS\",    \"status\": \"Failure\",      \"git_to_jenkins\":{          \"name\":\"jenkins\",          \"parent\":\"CM-7292072118\"      }  }",
//					XContentType.JSON);
//			irchild2.routing(indexName);
//			irchild2.id("e3011c0b-0c27-11ea-ad12-2016b9b2130e");
//			IndexResponse createIndexResponseChild2 = esClientDeo.getClient().index(irchild2, RequestOptions.DEFAULT);
//
//			IndexRequest irchild3 = new IndexRequest(indexName);
//			irchild3.source(
//					"{    \"jobName\": \"ClaimProcessed\",    \"maxCorrelationTime\": 1574321340,    \"inSightsTimeX\": \"2018-05-21T00:00:54Z\",    \"buildNumber\": \"82143741557\",    \"uuid\": \"e3011c0b-0c28-11ea-ad12-2016b9b2130e\",    \"categoryName\": \"CI\",    \"scmcommitId\": \"CM-7292072118\",    \"master\": \"master2\",    \"execId\": \"5f067cde-0c27-11ea-a8c4-106530e9217d\",    \"result\": \"ABORTED\",    \"duration\": 453,    \"environment\": \"PROD\",    \"jenkins_date\": \"2018-05-21 00:00:54\",    \"buildUrl\": \"productv4.2.devops.com\",    \"startTime\": \"2018-05-21T00:00:54Z\",    \"inSightsTime\": 1526841054,    \"endTime\": \"2018-05-21T00:08:27Z\",    \"correlationTime\": 1574317740,    \"projectName\": \"PaymentServices\",    \"projectID\": \"1002\",    \"toolName\": \"JENKINS\",    \"status\": \"Failure\",      \"git_to_jenkins\":{          \"name\":\"jenkins\",          \"parent\":\"CM-7292072118\"      }  }",
//					XContentType.JSON);
//			irchild3.routing(indexName);
//			irchild3.id("e3011c0b-0c28-11ea-ad12-2016b9b2130e");
//			IndexResponse createIndexResponseChild3 = esClientDeo.getClient().index(irchild3, RequestOptions.DEFAULT);
//
//			/*IndexRequest irchild4 = new IndexRequest(indexName);
//			irchild4.source(
//					"{      \"firstName\":\"Darren2\",      \"lastName\":\"Ford2\",      \"gender\":\"Male\",      \"isAlive\":false,   \"Portfolio\": \"InSights\",     \"SprintID\": \"S75\",     \"ProgramName\": \"ALLADININT\",     \"uuid\": \"043a9180-c386-11e6-875c-005056b1008e\",     \"execId\": \"e094be91-c385-11e6-8a9c-5cc5d4d3e5e0\",     \"repoType\": \"testing\",     \"git_commitId\": \"9dcc92e315421da0cb271cf7105039b8b46b88bb\",     \"inSightTimeX\": \"2016-12-15T02:45:47Z\",     \"ToolName\": \"GIT\",     \"git_authorName\": \"mayankdevops\",     \"git_reponame\": \"AnyBank\",     \"dataCopyProcessedFlag\": false,     \"inSightsTime\": 1481770487.0,     \"git_commiTime\": \"2016-12-15T08:45:47Z\",     \"PortfolioName\": \"IWM\",     \"toolName\": \"GIT\"     , \"relation_type\":{          \"name\":\"child\",          \"parent\":\"111\"      }  }",
//					XContentType.JSON);
//			irchild4.routing(indexName);
//			irchild4.id("114");
//			IndexResponse createIndexResponseChild4 = esClientDeo.getClient().index(irchild4, RequestOptions.DEFAULT);
//			 */
//			log.debug(" All data created with mapping field ");
//
//			String esQuery = "{\"match_all\":{}}";
//
//			String esQuery3 = "{  \"has_parent\":{     \"parent_type\":\"git\",     \"query\":{  \"match\":{     \"toolName\":\"GIT\"   }      }  }     }";
//
//			String esQuery4 = "{ \"parent_id\":{  \"type\":\"jenkins\",     \"id\":\"CM-7292072118\"    } }";
//
//			log.debug(" wrapperQuery Single record===== ");
//			esClientDeo.fetchByJsonQuery(esQuery, indexName, 10);
//
//			esClientDeo.fetchByJsonQuery(esQuery3, indexName, 10);
//
//			esClientDeo.fetchByJsonQuery(esQuery4, indexName, 10);
//
//		} catch (Exception e) {
//			log.debug(e);
//			log.error(e);
//		}
//
//	}
//
//	public void fetchAllData(String indexName) throws IOException {
//		try {
//			log.debug(" matchAllQuery ===== ");
//			SearchRequest searchRequest = new SearchRequest(indexName);
//			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//			searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//			searchRequest.source(searchSourceBuilder);
//			searchRequest.routing(indexName);
//			SearchResponse searchResponse = esClientDeo.getClient().search(searchRequest, RequestOptions.DEFAULT);
//
//			esClientDeo.searchResponseAnalyze(searchResponse);
//
//			log.debug(" simpleQueryStringQuery ===== ");
//			//SearchRequest searchRequestQuery = new SearchRequest(indexName);
//			String esQuery = "{    \"has_child\" : {       \"type\" : \"child\",       \"query\" : {    \"match_all\" : {}       }   }       }";
//			String esQuery2 = "{ \"parent_id\":{  \"type\":\"child\", \"id\":\"1\"  }      }";
//			String esQuery3 = "{    \"has_parent\":{ \"parent_type\":\"parent\", \"query\":{  \"match\":{   \"gender\":\"Male\"    }  }  }   }";
//			String esQuery4 = "{\"match_all\":{}}";
//
//			log.debug(" wrapperQuery Single record===== ");
//			esClientDeo.fetchByJsonQuery(esQuery, indexName, 10);
//
//			esClientDeo.fetchByJsonQuery(esQuery2, indexName, 10);
//
//			esClientDeo.fetchByJsonQuery(esQuery3, indexName, 10);
//
//			esClientDeo.fetchByJsonQuery(esQuery4, indexName, 10);
//
//			log.debug(" simpleQueryStringQuery Single record===== ");
//			SearchRequest searchRequestQuerySingle = new SearchRequest(indexName);
//			String esQuerySingle2 = "{  \"query\": {  \"has_child\":{  \"type\":\"child\",  \"query\":{   \"bool\":{   \"must\":[  { \"match\":{ \"gender\":\"Male\" } },  { \"match\":{ \"isAlive\":true } }    ]   }    }    }      }  }";
//
//			SearchSourceBuilder searchSourceBuilderQuerySingle2 = new SearchSourceBuilder();
//			searchSourceBuilderQuerySingle2.query(QueryBuilders.simpleQueryStringQuery(esQuerySingle2));
//			searchRequestQuerySingle.source(searchSourceBuilderQuerySingle2);
//			searchRequestQuerySingle.routing(indexName);
//
//			SearchResponse searchResponseQuerySingle = esClientDeo.getClient().search(searchRequestQuerySingle,
//					RequestOptions.DEFAULT);
//
//			esClientDeo.searchResponseAnalyze(searchResponseQuerySingle);
//
//			SearchRequest searchRequestParentChild = new SearchRequest(indexName);
//
//			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//			boolQuery.must(JoinQueryBuilders.hasChildQuery("child", QueryBuilders.termQuery("gender", "Female"),
//					ScoreMode.None)).must(QueryBuilders.termQuery("isAlive", true));
//
//			HasChildQueryBuilder qHasChildQueryBuilder = new HasChildQueryBuilder(indexName, boolQuery, ScoreMode.None);
//
//			log.debug(" BoolQueryBuilder qHasChildQueryBuilder ===== " + qHasChildQueryBuilder);
//
//			JoinQueryBuilders.hasChildQuery(indexName, QueryBuilders.termQuery("tag", "something"), ScoreMode.None);
//
//			log.debug(" BoolQueryBuilder ===== ");
//
//			log.debug(" boolQuery  " + boolQuery.queryName());
//			SearchSourceBuilder searchSourceBuilderParentChild = new SearchSourceBuilder();
//			//searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//			searchSourceBuilderParentChild.query(boolQuery);
//			searchRequestParentChild.source(searchSourceBuilderParentChild);
//			searchRequestParentChild.routing(indexName);
//			SearchResponse searchResponseParentChild = esClientDeo.getClient().search(searchRequestParentChild,
//					RequestOptions.DEFAULT);
//
//			esClientDeo.searchResponseAnalyze(searchResponseParentChild);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			log.error(e);
//		}
//	}
//
//	public void testQuery() {
//		String esQuery4 = "{    \"bool\": {      \"should\": [        {          \"bool\": {            \"must\": [              {                \"match\": {                  \"toolName.keyword\": \"GIT\"                }              },              {                \"match\": {                  \"jiraKey.keyword\": \"LS-1482478349\"                }              }            ]          }        },        {          \"bool\": {            \"must\": [              {                \"match\": {                  \"jiraKey.keyword\": \"LS-1482478349\"                }              }            ]          }        }      ]    }  }";
//		esClientDeo.fetchByJsonQuery(esQuery4, "from_jira_to_git", 100);
//
//	}


}
