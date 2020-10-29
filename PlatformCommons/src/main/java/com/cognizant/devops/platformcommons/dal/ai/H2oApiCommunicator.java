/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.dal.ai;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.dal.RestApiHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class H2oApiCommunicator {
	private static final Logger log = LogManager.getLogger(H2oApiCommunicator.class);
	String h2oEndpoint;

	public H2oApiCommunicator() {
		h2oEndpoint = ApplicationConfigProvider.getInstance().getMlConfiguration().getH2oEndpoint();
	}

	/**
	 * Upload csv data into H2o
	 *
	 * @param data
	 * @param name
	 * @return
	 */
	public int uploadToH2O(String data, String name) throws InsightsCustomException {
		try {
			
			String url =h2oEndpoint + "/3/PostFile";
			
			
			log.debug("Uploading data into H2O: {} ", name);
			HttpPost httpPost = new HttpPost(h2oEndpoint + "/3/PostFile?destination_frame=" + name);
			StringBody postData = new StringBody(data, ContentType.MULTIPART_FORM_DATA);			
			HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("filename", postData).build();
			httpPost.setEntity(reqEntity);
			CloseableHttpClient httpClient = HttpClients.createDefault();			
			HttpResponse response = httpClient.execute(httpPost);
			log.debug("Upload: {} Response code: {} ", name, response.getStatusLine().getStatusCode());
			return response.getStatusLine().getStatusCode();
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}		
	}

	
	public String uploadH2o(String data, String name) throws InsightsCustomException {
		try {
			String response=null;			
			String url =h2oEndpoint + H2ORestApiUrls.POST_FILE;			
			log.debug("Uploading data into H2O: {} ", name);
			FileUtils.writeStringToFile(new File("C:\\MLWork\\data.txt"), data);
			HashMap<String,String> map=new HashMap<>();
			map.put("filename", data);
			map.put("destination_frame", name);			
			response=RestApiHandler.uploadMultipartFileWithData(url, null, map, null, "multipart/form-data");
			log.debug("Upload: {} Response code: {} ", name, response);
			return response;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}		
	}

	
	/**
	 * Parse the uploaded data. All parameters are required for proper parsing.
	 *
	 * @param source
	 * @param destination
	 * @param numColumns
	 * @param columnTypes
	 * @param columnNames
	 * @param separator
	 * @param checkHeader
	 * @return
	 */
	public int parseData(String source, String destination, int numColumns, String columnTypes, String columnNames,
			String separator, int checkHeader) throws InsightsCustomException {
		try {
			log.debug("Parsing data frame: {} ", source);
			HttpPost httpPost = new HttpPost(/*h2oEndpoint*/"http://localhost:54321" + "/3/Parse?source_frames=" + source + "&destination_frame="
					+ destination + "&number_columns=" + numColumns + "&parse_type=csv&check_header=" + checkHeader
					+ "&delete_on_done=true&separator=" + separator + "&column_types=%5B" + columnTypes
					+ "%5D&column_names=%5B" + columnNames + "%5D");
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200)
				return pollRequest(EntityUtils.toString(response.getEntity()));
		
			return response.getStatusLine().getStatusCode();
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	public int postparsedCSVData(String source, String destination, int numColumns, String columnTypes, String columnNames,
			String separator, int checkHeader) throws InsightsCustomException
	{
		String url=h2oEndpoint+H2ORestApiUrls.PARSE_DATA;
		JsonObject queryParams= new JsonObject();
		queryParams.addProperty("source_frames", source);	
		queryParams.addProperty("destination_frame", destination+".hex");	
		queryParams.addProperty("parse_type", "CSV");	
		queryParams.addProperty("separator", separator);	
		queryParams.addProperty("number_columns",numColumns);	
		queryParams.addProperty("single_quotes", "false");
		queryParams.addProperty("column_names",Arrays.toString(columnNames.split(",")));	
		queryParams.addProperty("column_types",Arrays.toString(columnTypes.split(",")));	
		queryParams.addProperty("check_header", checkHeader);	
		queryParams.addProperty("delete_on_done","true");		
		String httpResponse=RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Post");
		 JsonObject response=  new JsonParser().parse(httpResponse).getAsJsonObject();
         String pollingURL= response.get("job").getAsJsonObject().get("key")
      		   .getAsJsonObject().get("URL").getAsString();         
		return pollRequestStatus(pollingURL);	
		
	}
	
	
	public String importFiles(String filePath) throws InsightsCustomException
	{			
		String url="http://localhost:54321/3/ImportFiles";			
		JsonObject queryParams= new JsonObject();
		queryParams.addProperty("path", filePath);			
		return RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Get");			
		
	}
	
	public int uploadFileToH2O(String filePath) throws InsightsCustomException  {
		
		/*	
			String url="http://localhost:54321/3/ImportFiles";			
			JsonObject queryParams= new JsonObject();
			queryParams.addProperty("path", filePath);			
			RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Get");
			*/
			
		String url="http://localhost:54321/3/Parse";	
		
		String [] _sourceFrames= {"nfs:\\C:\\InSights_Windows\\Server2\\INSIGHTS_HOME\\MLData\\newTest3\\GitAuthorDataNew.txt.csv"};
		String destination_frame="GitAuthorDataNew.hex";
		String parse_type ="CSV";
		String separator="44";
		String number_columns="5";
		String single_quotes="false";
		String [] column_names= {"Date","AuthorName","Experience","RepoName","Commits"};
		String [] column_types= {"Time","Enum","Numeric","Numeric","Numeric"};
		String check_header="1";
		String delete_on_done="true";
		String chunk_size="4096";
		
		
		JsonObject queryParams= new JsonObject();
		queryParams.addProperty("source_frames", Arrays.toString(_sourceFrames));	
		queryParams.addProperty("destination_frame", destination_frame);	
		queryParams.addProperty("parse_type", parse_type);	
		queryParams.addProperty("separator", separator);	
		queryParams.addProperty("source_frames", Arrays.toString(_sourceFrames));	
		queryParams.addProperty("number_columns",number_columns);	
		queryParams.addProperty("single_quotes", single_quotes);
		queryParams.addProperty("column_names", Arrays.toString(column_names));	
		queryParams.addProperty("column_types",Arrays.toString(column_types));	
		queryParams.addProperty("check_header", check_header);	
		queryParams.addProperty("delete_on_done",delete_on_done);	
		queryParams.addProperty("chunk_size", chunk_size);	
		String response=RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Post");
		int i=pollRequest(response);	
			log.debug("Uploading data into H2O: {} ", i);
			/*HttpPost httpPost = new HttpPost(h2oEndpoint"http://localhost:54321" + "/3/ImportFiles");
			 List<NameValuePair> params = new ArrayList<NameValuePair>();
			    params.add(new BasicNameValuePair("path", filePath));			   
			    httpPost.setEntity(new UrlEncodedFormEntity(params));
			*/
		/*	//	StringBody postData = new StringBody(data, ContentType.MULTIPART_FORM_DATA);
		//	HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("filename", postData).build();
		//	httpPost.setEntity(reqEntity);
			CloseableHttpClient httpClient = HttpClients.createDefault();			
		//	HttpResponse response = httpClient.execute(httpPost);
			log.debug("Upload: {} Response code: {} ", filePath, response.getStatusLine().getStatusCode());
			return response.getStatusLine().getStatusCode();*/
		/*} catch (IOException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}	*/	
			
			return 0;
	}


	/**
	 * Train a Word2Vec algorithm using the training frame which needs to be already
	 * uploaded and parsed The training frame must be a single column data frame of
	 * type string.
	 *
	 * @param trainingFrame
	 * @param modelId
	 * @return
	 */
	public int trainWord2Vec(String trainingFrame, String modelId) throws InsightsCustomException {
		try {
			log.debug("Training Word2Vec algorithm : {} ", trainingFrame);
			String url=h2oEndpoint+H2ORestApiUrls.TRAIN_WORD2VEC;			
			JsonObject queryParams= new JsonObject();
			queryParams.addProperty("vec_size", 100);	
			queryParams.addProperty("model_id", modelId);	
			queryParams.addProperty("training_frame", trainingFrame);	
			String httpResponse=RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Post");
			JsonObject response = new JsonParser().parse(httpResponse).getAsJsonObject();
			String pollingURL = response.get("job").getAsJsonObject().get("key").getAsJsonObject().get("URL").getAsString();
			return pollRequestStatus(pollingURL);			
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Transform the data required using a trained Word2Vec model.
	 *
	 * @param inputFrame
	 * @param model
	 * @param rowCount
	 * @return
	 */
	public JsonArray transformWord2Vec(String inputFrame, String model, int rowCount) throws InsightsCustomException {
		try {			
			log.debug("Transforming frame using Word2Vec algorithm: {} ", model);
			
			String url=h2oEndpoint+H2ORestApiUrls.TRANSFORM_WORD2VEC;			
			JsonObject queryParams= new JsonObject();
			queryParams.addProperty("model", model);	
			queryParams.addProperty("words_frame", inputFrame);	
			queryParams.addProperty("aggregate_method", "AVERAGE");	
			String response=RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Get");
			
			/*Get the vector from the url mentioned in the response*/
			
			JsonObject payload = new Gson().fromJson(response, JsonObject.class);
			JsonObject vectors = payload.get("vectors_frame").getAsJsonObject();
			String frameUrl = vectors.get("URL").getAsString();
			url=h2oEndpoint+frameUrl;
			queryParams= new JsonObject();
			queryParams.addProperty("row_count", rowCount);	
			response=RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Get");			
			return getVectorColumns(response);			
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Split an already uploaded and parsed frame into 2 based on the ratio
	 * provided. The resultant frames are named to be: inputFrameName_part0 and
	 * inputFrameName_part1
	 *
	 * @param inputFrame
	 * @param splitRatio
	 * @return
	 */
	public int splitFrame(String inputFrame, double splitRatio) throws InsightsCustomException {
		try {
			log.debug("Splitting frame: {} ", inputFrame);
			HttpPost httpPost = new HttpPost(
					h2oEndpoint + "/3/SplitFrame?dataset=" + inputFrame + "&ratios=" + String.valueOf(splitRatio));
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpResponse response = httpClient.execute(httpPost);
			return response.getStatusLine().getStatusCode();
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}
	
	public String splitFrameNew(String inputFrame, double splitRatio) throws InsightsCustomException {
		try {
			String url = h2oEndpoint+H2ORestApiUrls.SPLIT_FRAME;
			JsonObject queryParams= new JsonObject();
			queryParams.addProperty("dataset", inputFrame+".hex");	
			queryParams.addProperty("ratios", String.valueOf(splitRatio));			
			return RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Post");		
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}
	

	/**
	 * Run AutoML using the Training and validation frame that were split from input
	 * data. If the response column type is Numeric, it performs Regression, if it
	 * is Enum, it performs Classification. Maximum of 15 models with each having a
	 * max runtime of 200 seconds is configured as default.
	 *
	 * @param modelName
	 * @param trainingFrame
	 * @param validationFrame
	 * @param responseColumn
	 * @return
	 */
	public String runAutoML(String modelName, String trainingFrame, String validationFrame, String responseColumn,
			String numOfModels) throws InsightsCustomException {
		try {
			log.debug("Building AutoML: {} ", modelName);
			String url = h2oEndpoint + H2ORestApiUrls.BUILD_AUTOML;

			JsonObject buildControl = new JsonObject();
			buildControl.addProperty("project_name", modelName);
			buildControl.addProperty("keep_cross_validation_models", false);
			buildControl.addProperty("keep_cross_validation_predictions",false);
			buildControl.addProperty("keep_cross_validation_fold_assignment",false);			  

			JsonObject stoppingCriteria = new JsonObject();
			stoppingCriteria.addProperty("max_models", Integer.parseInt(numOfModels));
			stoppingCriteria.addProperty("max_runtime_secs_per_model", 180);
			stoppingCriteria.addProperty("max_runtime_secs", 0);
			stoppingCriteria.addProperty("stopping_rounds", 3);
			stoppingCriteria.addProperty("stopping_metric", "AUTO");
			stoppingCriteria.addProperty("stopping_tolerance", -1);
			
			buildControl.add("stopping_criteria", stoppingCriteria);
			buildControl.addProperty("nfolds", 5);
			buildControl.addProperty("balance_classes", false);

			JsonObject inputSpec = new JsonObject();
			inputSpec.addProperty("training_frame", trainingFrame);
			inputSpec.addProperty("validation_frame", trainingFrame);
			inputSpec.addProperty("response_column", responseColumn);
			JsonObject payload = new JsonObject();
			payload.add("build_control", buildControl);
			payload.add("input_spec", inputSpec);

			return RestApiHandler.doPost(url, payload, null);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Get the progress/status of the AutoML build using the job url.
	 *
	 * @param url
	 * @return
	 */
	public JsonObject getAutoMLProgress(String url) throws InsightsCustomException {
		try {
			JsonObject response = new JsonObject();
			log.debug("Get progress of AutoML: {} ", url);
			HttpGet httpGet = new HttpGet(h2oEndpoint + url);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpResponse pollingResponse = httpClient.execute(httpGet);
			response.addProperty("ResponseCode", pollingResponse.getStatusLine().getStatusCode());
			if (pollingResponse.getStatusLine().getStatusCode() == 200) {
				JsonObject parsedjson = new Gson().fromJson(EntityUtils.toString(pollingResponse.getEntity()),
						JsonObject.class);
				JsonArray jobs = parsedjson.get("jobs").getAsJsonArray();
				JsonObject job = jobs.get(0).getAsJsonObject();
				String status = job.get("status").getAsString();
				double progress = Math.floor((job.get("progress").getAsDouble()) * 100);
				response.addProperty("Progress", progress);
				response.addProperty("Status", status);
				return response;
			} else {
				response.addProperty("Message", "Error in polling progress");
				log.error(EntityUtils.toString(pollingResponse.getEntity()));
				return response;
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	public int pollRequestStatus(String url) throws InsightsCustomException {
		JsonObject response = new JsonObject();
		try {
			log.debug("Get progress of AutoML: {} ", url);
			String h20url = h2oEndpoint + url;
			String status = "RUNNING";
			do {
				String httpResponse = RestApiHandler.doGet(h20url, null);
				response = new JsonParser().parse(httpResponse).getAsJsonObject();
				JsonArray jobs = response.get("jobs").getAsJsonArray();
				JsonObject job = jobs.get(0).getAsJsonObject();
				status = job.get("status").getAsString();
				// double progress = Math.floor((job.get("progress").getAsDouble()) * 100);
				Thread.sleep(3000);
			} while (status.equals("RUNNING"));
			if (status.equals("DONE")) {
				return 200;
			} else {
				return 400;
			}

		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}

	}
	
	/**
	 * Get the Leaderboard for the AutoML run. It also contains the metrics for each
	 * model.
	 *
	 * @param modelId
	 * @param responseColumn
	 * @return
	 */
	/*public JsonObject getLeaderBoard(String modelName, String responseColumn) throws InsightsCustomException {
		try {
			log.debug("Get Leaderboard: {} ", modelName);
			HttpGet httpGet = new HttpGet(h2oEndpoint + "/99/AutoML/" + modelName + "@@" + responseColumn);
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpResponse Response = httpClient.execute(httpGet);
			JsonObject jsonResponse = new Gson().fromJson(EntityUtils.toString(Response.getEntity()), JsonObject.class);
			JsonObject leaderboardTable = new Gson().fromJson(jsonResponse.get("leaderboard_table"), JsonObject.class);
			JsonArray columns = new Gson().fromJson(leaderboardTable.get("columns"), JsonArray.class);
			JsonArray data = new Gson().fromJson(leaderboardTable.get("data"), JsonArray.class);
			JsonObject leaderboard = new JsonObject();
			for (int i = 1; i < columns.size(); i++) {
				JsonObject colmn = new Gson().fromJson(columns.get(i), JsonObject.class);
				leaderboard.add(colmn.get("name").getAsString(), data.get(i));
			}
			return leaderboard;
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}
	*/
	
	public JsonObject getLeaderBoard(String modelId) throws InsightsCustomException {
		try {
			log.debug("Get Leaderboard: {} ", modelId);			
			String url = h2oEndpoint+H2ORestApiUrls.LEADERBOARD_URL+modelId;			
			String response = RestApiHandler.doGet(url, null);
			JsonObject jsonResponse = new Gson().fromJson(response, JsonObject.class);
			JsonObject leaderboardTable = new Gson().fromJson(jsonResponse.get("leaderboard_table"), JsonObject.class);
			JsonArray columns = new Gson().fromJson(leaderboardTable.get("columns"), JsonArray.class);
			columns.remove(0);			
			JsonArray data = new Gson().fromJson(leaderboardTable.get("data"), JsonArray.class);
			data.remove(0);
			JsonArray leaderboardArray = new JsonArray();
			for (int i = 0; i < columns.size(); i++) {
				JsonObject leaderboard = new JsonObject();
				for(int j=0;j<columns.size();j++)
				{
					JsonObject colmn =columns.get(j).getAsJsonObject();
					String colName=colmn.get("name").getAsString();
					String colData=data.get(j).getAsJsonArray().get(i).getAsString();
					leaderboard.addProperty(colName, colData);					
				}
				leaderboardArray.add(leaderboard);
			
			}			
			JsonObject finalResponse= new JsonObject();
			finalResponse.add("data", leaderboardArray);			
			return finalResponse;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Perform prediction using an already trained model and an uploaded and parsed
	 * data frame
	 *
	 * @param modelName
	 * @param testFrame
	 * @return
	 */
	public JsonObject predict(String modelName, String testFrame) throws InsightsCustomException {
		try {
			
			String url = h2oEndpoint + H2ORestApiUrls.PREDICTION_URL+modelName+"/frames/"+testFrame;			
			
			log.debug("Predicting for : {}", modelName);
			
			JsonObject emptyObject = new JsonObject();
			
			String httpResponse = RestApiHandler.doPost(url, emptyObject, null);
			JsonObject predictionData = new JsonObject();
			
				JsonObject responseObject = new Gson().fromJson(httpResponse,
						JsonObject.class);
				JsonObject model = responseObject.getAsJsonArray("model_metrics").get(0).getAsJsonObject();
				JsonArray data = model.getAsJsonObject("predictions").getAsJsonArray("columns");
				for (JsonElement e : data) {
					predictionData.add(e.getAsJsonObject().get("label").getAsString(),
							e.getAsJsonObject().getAsJsonArray("data"));
				}
				return predictionData;
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Fetch any DataFrame from H2o
	 *
	 * @param frameName
	 * @return
	 */
	public JsonArray getDataFrame(String frameName) throws InsightsCustomException {
		try {
			log.debug("Getting data frame:{} ", frameName);
			
			String url = h2oEndpoint+H2ORestApiUrls.DATA_FRAME+frameName;			
			String httpResponse = RestApiHandler.doGet(url, null);		
			JsonArray data = new JsonArray();
			JsonObject jsonResponse = new Gson().fromJson(httpResponse, JsonObject.class);
			JsonObject frame = jsonResponse.getAsJsonArray("frames").get(0).getAsJsonObject();
			int rowCount = frame.get("row_count").getAsInt();
			JsonArray columns = frame.getAsJsonArray("columns");
			for (int i = 0; i < rowCount; i++) {
				JsonObject row = new JsonObject();
				for (JsonElement c : columns) {
					JsonObject column = c.getAsJsonObject();
					String label = column.get("label").getAsString();
					if (label.equals("C1"))
						break;
					JsonArray columnData = new JsonArray();
					if (column.get("type").getAsString().equals("string")
							|| column.get("type").getAsString().equals("uuid"))
						columnData = column.getAsJsonArray("string_data");
					else if (column.get("type").getAsString().equals("enum")) {
						JsonArray domain = column.getAsJsonArray("domain");
						JsonArray encodedData = column.getAsJsonArray("data");
						for (JsonElement e : encodedData) {
							if (e.getAsString().equals("NaN"))
								columnData.add("NaN");
							else {
								columnData.add(domain.get(e.getAsInt()));
							}
						}
					} else if (column.get("type").getAsString().equals("time")) {
						for (JsonElement e : column.getAsJsonArray("data")) {
							if (e.getAsString().equals("NaN"))
								columnData.add("NaN");
							else {
								columnData.add(
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(e.getAsLong())));
							}
						}
					} else
					{
						columnData = column.getAsJsonArray("data");
					}
					row.add(label, columnData.get(i));
				}
				data.add(row);
			}
			return data;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
	}

	/**
	 * Download MOJO for a trained model and Store it in $INSIGHTS_HOME/ML_DATA
	 *
	 * @param usecase
	 * @param modelId
	 * @return
	 */
	public JsonObject downloadMojo(String usecase, String modelId) {
		JsonObject response = new JsonObject();
		try {
			log.debug(" Downloading MOJO: {} ", modelId);
			String url = h2oEndpoint + H2ORestApiUrls.DOWNLOAD_MOJO + modelId + "/mojo";
			InputStream httpResponse = RestApiHandler.downloadMultipartFile(url, null);
			String filePath = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + File.separator + usecase + File.separator
					+ modelId + ".zip";
			File destFile = new File(filePath);
			FileUtils.copyInputStreamToFile(httpResponse, destFile);
			response.addProperty("Status", "200");
			response.addProperty("Path", filePath);
			return response;
		} catch (Exception e) {
			log.error("unable to donwload mojo {}", e.getMessage());
			response.addProperty("Status", "500");
			return response;
		}
	}

	/**
	 * Poll a running request until DONE or ERROR
	 *
	 * @param response
	 * @return
	 */
	/*private int pollRequest(String response) {
		try {
			JsonObject parsedjson = new Gson().fromJson(response, JsonObject.class);
			JsonObject job = parsedjson.get("job").getAsJsonObject();
			JsonObject key = job.get("key").getAsJsonObject();
			String url = key.get("URL").getAsString();
			HttpGet request = new HttpGet(h2oEndpoint + url);
			String status = "RUNNING";
			do {
				CloseableHttpClient httpClient = HttpClients.createDefault();
				HttpResponse pollingResponse = httpClient.execute(request);
				if (pollingResponse.getStatusLine().getStatusCode() == 200) {
					parsedjson = new Gson().fromJson(EntityUtils.toString(pollingResponse.getEntity()),
							JsonObject.class);
					JsonArray jobs = parsedjson.get("jobs").getAsJsonArray();
					job = jobs.get(0).getAsJsonObject();
					status = job.get("status").getAsString();
				if (status.equals("DONE"))
						return pollingResponse.getStatusLine().getStatusCode();
				} else {
					return pollingResponse.getStatusLine().getStatusCode();
				}
				TimeUnit.SECONDS.sleep(10);
			} while (status.equals("RUNNING"));
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
		return 0;
	}
	*/
	
	private int pollRequest(String response) {
		try {
			JsonObject parsedJson = new Gson().fromJson(response, JsonObject.class);
			JsonObject job = parsedJson.get("job").getAsJsonObject();
			JsonObject key = job.get("key").getAsJsonObject();
			String url = key.get("URL").getAsString();
			String httpURL=h2oEndpoint + url;
			String status = "RUNNING";
			do {
				
				String httpResponse=RestApiHandler.doGet(httpURL, null);
				
				/*	JsonArray jobs = parsedjson.get("jobs").getAsJsonArray();
					job = jobs.get(0).getAsJsonObject();
					status = job.get("status").getAsString();*/
				
				TimeUnit.SECONDS.sleep(10);
			} while (status.equals("RUNNING"));
		} catch (Exception e) {
			log.error(e.getMessage());
		} 
		return 0;
	}
	

	/**
	 * Get the vectors from transformed frame
	 *
	 * @param input
	 * @return
	 */
	private JsonArray getVectorColumns(String input) {
		JsonObject parsedjson = new Gson().fromJson(input, JsonObject.class);
		JsonArray frames = parsedjson.get("frames").getAsJsonArray();
		JsonObject frame = frames.get(0).getAsJsonObject();
		return frame.get("columns").getAsJsonArray();
	}

}
