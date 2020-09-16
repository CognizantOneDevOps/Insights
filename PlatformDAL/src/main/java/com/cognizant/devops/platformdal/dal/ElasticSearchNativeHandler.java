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
package com.cognizant.devops.platformdal.dal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/*
 * import org.elasticsearch.action.bulk.BulkRequest;
 * import org.elasticsearch.action.bulk.BulkResponse;
 * import org.elasticsearch.action.delete.DeleteRequest;
 * import org.elasticsearch.action.index.IndexRequest;
 * import org.elasticsearch.action.search.SearchRequest;
 * import org.elasticsearch.action.search.SearchResponse;
 * import org.elasticsearch.client.RequestOptions;
 * import org.elasticsearch.client.RestClient;
 * import org.elasticsearch.client.RestClientBuilder;
 * import org.elasticsearch.client.RestHighLevelClient;
 * import org.elasticsearch.common.text.Text;
 * import org.elasticsearch.common.xcontent.XContentType;
 * import org.elasticsearch.index.query.QueryBuilder;
 * import org.elasticsearch.index.query.QueryBuilders;
 * import org.elasticsearch.search.SearchHit;
 * import org.elasticsearch.search.SearchHits;
 * import org.elasticsearch.search.builder.SearchSourceBuilder;
 * import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
 * import org.elasticsearch.search.sort.SortBuilder;
 * import org.elasticsearch.search.sort.SortBuilders;
 * import org.elasticsearch.search.sort.SortOrder;
 */

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.dal.elasticsearch.ElasticSearchDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticSearchNativeHandler {
	private static Logger log = LogManager.getLogger(ElasticSearchNativeHandler.class.getName());
	//private RestHighLevelClient esClient;

	/* public ElasticSearchNativeHandler() {
		createClient();
	}
	
		public void createClient() {
			String esUrl = ApplicationConfigProvider.getInstance().getEndpointData().getElasticSearchEndpoint();
			log.debug(" esUrl ==== {} ", esUrl);
			RestClientBuilder builder = RestClient.builder(HttpHost.create(esUrl));
			this.esClient = new RestHighLevelClient(builder);
		}
	
		public RestHighLevelClient getClient() {
			if (this.esClient == null) {
				createClient();
			}
			return this.esClient;
		}
	
		public void save(String indexName, JsonObject data, String id) throws IOException {
			byte[] bytes = data.toString().getBytes();
			IndexRequest indexRequest = new IndexRequest(indexName).id(id).source(bytes, XContentType.JSON);
			esClient.index(indexRequest, RequestOptions.DEFAULT);
		}
	
		public List<String> saveBulk(String indexName, JsonArray rows) throws IOException {
			BulkRequest bulkreq = new BulkRequest();
			List<String> uuidUpdatedList = new ArrayList<>(0);
			try {
				IndexRequest ir;
				for (JsonElement bulkItem : rows) {
					JsonObject toolData = bulkItem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject();
					JsonElement uuidJson = toolData.get("uuid");
					ir = new IndexRequest(indexName);
					ir.id(uuidJson.getAsString());
					ir.source(toolData.toString(), XContentType.JSON);
					ir.routing(indexName);
					bulkreq.add(ir);
					uuidUpdatedList.add(uuidJson.getAsString());
				}
				if (!uuidUpdatedList.isEmpty()) {
					BulkResponse response = esClient.bulk(bulkreq, RequestOptions.DEFAULT);
					if (response.hasFailures()) {
						log.error(" Bulk Response has some error ==== {} ", response.buildFailureMessage());
						uuidUpdatedList.clear();
					} else {
						log.debug(" Bulk record created in ElasticSerach for indexName {} , Bulk Response ==== {} ",
								indexName, response.status());
					}
				}
			} catch (Exception e) {
				log.error("Error while bulk upload of data {} ", e);
				log.error(e);
			}
			return uuidUpdatedList;
		}
	
		public List<String> saveBulk(String indexName, List<JsonObject> rows) throws IOException {
			BulkRequest bulkreq = new BulkRequest();
			List<String> uuidUpdatedList = new ArrayList<>(0);
			try {
				if (this.esClient == null) {
					createClient();
				}
				IndexRequest ir;
				for (JsonElement bulkItem : rows) {
					log.debug(" bulkItem  {} ", bulkItem);
					String uuid = UUID.randomUUID().toString();
					bulkItem.getAsJsonObject().addProperty("uuid", uuid);
					ir = new IndexRequest(indexName);
					ir.id(uuid);
					ir.source(bulkItem.toString(), XContentType.JSON);
					ir.routing(indexName);
					bulkreq.add(ir);
					uuidUpdatedList.add(uuid);
				}
				if (!uuidUpdatedList.isEmpty()) {
					BulkResponse response = esClient.bulk(bulkreq, RequestOptions.DEFAULT);
	
					if (response.hasFailures()) {
						log.error(" Bulk Response has some error ==== {} ", response.buildFailureMessage());
						uuidUpdatedList.clear();
					} else {
						log.debug(" Bulk record created in ElasticSerach for indexName {} , Bulk Response ==== {} ",
								indexName, response.status());
						refreshIndex(indexName);
					}
				}
				esClient.close();
				esClient = null;
			} catch (Exception e) {
				log.error("Error while bulk upload of data {} ", e);
				log.error(e);
			}
	
			return uuidUpdatedList;
		}*/

	private void refreshIndex(String indexName) {

		try {
			ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
			String sourceESCacheUrl = ApplicationConfigProvider.getInstance().getEndpointData()
					.getElasticSearchEndpoint() + "/" + indexName + "/_refresh?ignore_unavailable=true";
			JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl, "{}");
			log.debug("Index refresh response {} ", esResponse);
		} catch (Exception e) {

		}
	}

	/*	public void delete(String id, String indexName) throws IOException {
			DeleteRequest deleteReqst = new DeleteRequest(indexName, id);
			esClient.delete(deleteReqst, RequestOptions.DEFAULT);
		}
	
		public SearchResponse search(QueryBuilder query, Integer size, String indexName) throws IOException {
			log.debug("elasticsearch query: {}", query.toString());
			SearchResponse response = esClient.search(
					new SearchRequest(indexName).source(new SearchSourceBuilder().query(query)), RequestOptions.DEFAULT);//.trackTotalHits(true) .from(from).size(size)
			log.debug("elasticsearch response: {} hits", response.getHits().getTotalHits());
			//log.trace("elasticsearch response: {} hits", response.toString());
			return response;
		}
	
		public SearchResponse fetchByJsonQuery(String esQuery4, String indexName, int rowCount) {
			log.debug("indexName {} runJsonQuery  =====  {} ", indexName, esQuery4);
			SearchResponse searchResponseQuery = null;
			try {
				SearchRequest searchRequestWrapperQuery = new SearchRequest(indexName);
				SortBuilder sort = SortBuilders.fieldSort("executionId").order(SortOrder.DESC);
				SearchSourceBuilder searchSourceBuilderWrapperQuery = new SearchSourceBuilder();
				searchSourceBuilderWrapperQuery.query(QueryBuilders.wrapperQuery(esQuery4));
				searchSourceBuilderWrapperQuery.from(0);
				searchSourceBuilderWrapperQuery.sort(sort);
				searchSourceBuilderWrapperQuery.size(rowCount);
				searchRequestWrapperQuery.source(searchSourceBuilderWrapperQuery);
				SearchResponse searchResponseWrapperQuery = esClient.search(searchRequestWrapperQuery,
						RequestOptions.DEFAULT);
				//SearchResponse SR = builder.setQuery(QB).addAggregation(AB).get();
				log.debug(" searchResponseWrapperQuery.toString() {} ", searchResponseWrapperQuery.toString());
	
				searchResponseAnalyze(searchResponseWrapperQuery);
			} catch (Exception e) {
				log.error(e);
			}
			return searchResponseQuery;
		}
	
		public List<JsonObject> fetchByJsonQueryAsList(String esQuery4, String indexName, String sortColumn) {
			log.debug("indexName {} runJsonQuery  =====  {} ", indexName, esQuery4);
			List<JsonObject> recordlist = new ArrayList<>();
			SearchResponse searchResponseQuery = null;
			try {
				SearchRequest searchRequestWrapperQuery = new SearchRequest(indexName);
				SortBuilder sort = SortBuilders.fieldSort("executionId").order(SortOrder.DESC);
				SearchSourceBuilder searchSourceBuilderWrapperQuery = new SearchSourceBuilder();
				searchSourceBuilderWrapperQuery.query(QueryBuilders.wrapperQuery(esQuery4));
				searchSourceBuilderWrapperQuery.sort(sort);
				searchSourceBuilderWrapperQuery.size(10000);
				searchRequestWrapperQuery.source(searchSourceBuilderWrapperQuery);
				SearchResponse searchResponseWrapperQuery = esClient.search(searchRequestWrapperQuery,
						RequestOptions.DEFAULT);
				log.debug(" searchResponseWrapperQuery.toString() {} ", searchResponseWrapperQuery.toString());
	
				searchResponseAnalyze(searchResponseWrapperQuery, recordlist);
			} catch (Exception e) {
				log.error(e);
			}
			return recordlist;
		}
	
		public List<JsonObject> fetchByJsonQueryAsSortListWithLimit(String esQuery4, String indexName, String sortColumn,
				int rowcont) {
			log.debug("indexName {} runJsonQuery  =====  {} ", indexName, esQuery4);
			List<JsonObject> recordlist = new ArrayList<>();
			SearchResponse searchResponseQuery = null;
			try {
				SearchRequest searchRequestWrapperQuery = new SearchRequest(indexName);
				SortBuilder sort = SortBuilders.fieldSort(sortColumn).order(SortOrder.DESC);
				SearchSourceBuilder searchSourceBuilderWrapperQuery = new SearchSourceBuilder();
				searchSourceBuilderWrapperQuery.query(QueryBuilders.wrapperQuery(esQuery4));
				searchSourceBuilderWrapperQuery.sort(sort);
				searchSourceBuilderWrapperQuery.size(rowcont);
				searchRequestWrapperQuery.source(searchSourceBuilderWrapperQuery);
				SearchResponse searchResponseWrapperQuery = esClient.search(searchRequestWrapperQuery,
						RequestOptions.DEFAULT);
				log.debug(" searchResponseWrapperQuery.toString() {} ", searchResponseWrapperQuery.toString());
	
				searchResponseAnalyze(searchResponseWrapperQuery, recordlist);
			} catch (Exception e) {
				log.error(e);
			}
			return recordlist;
		}
	
		public void searchResponseAnalyze(SearchResponse searchResponseQuery) {
			SearchHits hits = searchResponseQuery.getHits();
			log.debug(" searchResponseAnalyze  ============= {} ", hits.getTotalHits());
			for (SearchHit hit : hits.getHits()) {
				log.debug(" hit {} ", hit);
				JsonObject SRJSON = new JsonParser().parse(hit.toString()).getAsJsonObject();
				log.debug(" SRJSON  {} ", SRJSON);
				JsonObject recordKPI = SRJSON.get("_source").getAsJsonObject();
				log.debug(" recordKPI {} ", recordKPI);
				if (hit.hasSource()) {
					Map<String, Object> sourceMap = hit.getSourceAsMap();
					log.debug("id  {} sourceMap {} ", hit.getId(), sourceMap.toString());
				}
				Map<String, HighlightField> highlightFields = hit.getHighlightFields();
				if (!highlightFields.isEmpty()) {
					HighlightField highlight = highlightFields.get("title");
					Text[] fragments = highlight.fragments();
					String fragmentString = fragments[0].string();
					log.debug(" fragmentString  {} ", fragmentString);
				}
			}
		}
	
		public void searchResponseAnalyze(SearchResponse searchResponseQuery, List<JsonObject> recordlist) {
			SearchHits hits = searchResponseQuery.getHits();
			log.debug(" searchResponseAnalyze  ============= {} ", hits.getTotalHits());
			for (SearchHit hit : hits.getHits()) {
				JsonObject SRJSON = new JsonParser().parse(hit.toString()).getAsJsonObject();
				JsonObject recordKPI = SRJSON.get("_source").getAsJsonObject();
				log.debug(" recordKPI {} ", recordKPI);
				recordlist.add(recordKPI);
			}
		}*/

	public List<JsonObject> getESResult(String esQuery, String indexName) {
		ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
		List<JsonObject> recordlist = new ArrayList<>();
		try {
			String sourceESCacheUrl = ApplicationConfigProvider.getInstance().getEndpointData()
					.getElasticSearchEndpoint() + "/" + indexName;

			JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl + "/_search", esQuery);

			if (esResponse.has("status") && esResponse.get("status").getAsInt() == 404) {
				log.debug("Worlflow Detail ====  Elastic Serach data not retirved . Message is {} ", esResponse);
			} else {
				JsonArray esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
				for (JsonElement esResponseSource : esResponseArray) {
					///log.debug(" hit {} " , hit.toString());
					JsonObject SRJSON = new JsonParser().parse(esResponseSource.toString()).getAsJsonObject();
					//log.debug(" SRJSON {} " , SRJSON);
					JsonObject recordKPI = SRJSON.get("_source").getAsJsonObject();
					log.debug(" recordKPI {} ", recordKPI);
					recordlist.add(recordKPI);
				}
			}
			log.debug(" esResponse API response {} ", esResponse);
		} catch (InsightsCustomException e) {

		}
		return recordlist;
	}

	public List<JsonObject> saveESResult(String indexName, List<JsonObject> rows) {
		ElasticSearchDBHandler esDbHandler = new ElasticSearchDBHandler();
		List<JsonObject> recordlist = new ArrayList<>();
		try {
			String sourceESCacheUrl = ApplicationConfigProvider.getInstance().getEndpointData()
					.getElasticSearchEndpoint() + "/" + indexName + "/_bulk";
			StringBuffer bulkESJsons = new StringBuffer();
			for (JsonElement bulkItem : rows) {
				JsonObject bulkItemJson = new JsonObject();
				String uuid = UUID.randomUUID().toString();
				bulkItem.getAsJsonObject().addProperty("uuid", uuid);
				bulkItemJson.add("create", bulkItem.getAsJsonObject());
				bulkESJsons.append(bulkItemJson);
			}
			bulkESJsons = bulkESJsons.append("\\n");
			log.debug(" bulk Item Json {} ", bulkESJsons);
			JsonObject esResponse = esDbHandler.queryES(sourceESCacheUrl, bulkESJsons.toString());

			if (esResponse.has("status") && esResponse.get("status").getAsInt() == 404) {
				log.debug("Worlflow Detail ====  Elastic Serach data not retirved . Message is {} ", esResponse);
			} else {
				JsonArray esResponseArray = esResponse.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
				for (JsonElement esResponseSource : esResponseArray) {
					JsonObject SRJSON = new JsonParser().parse(esResponseSource.toString()).getAsJsonObject();
					JsonObject recordKPI = SRJSON.get("_source").getAsJsonObject();
					log.debug(" recordKPI {} ", recordKPI);
					recordlist.add(recordKPI);
				}
			}
			log.debug(" esResponse API response {} ", esResponse);
		} catch (InsightsCustomException e) {

		}
		return recordlist;
	}
}