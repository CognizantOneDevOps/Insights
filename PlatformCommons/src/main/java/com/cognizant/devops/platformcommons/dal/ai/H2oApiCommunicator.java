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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
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
	public static final String COLUMNS="columns";
	private static final Logger log = LogManager.getLogger(H2oApiCommunicator.class);
	String h2oEndpoint;

	public H2oApiCommunicator() {
		h2oEndpoint = ApplicationConfigProvider.getInstance().getMlConfiguration().getH2oEndpoint();
	}

	
	/** upload file to h2o
	 * @param data
	 * @param name
	 * @return
	 * @throws InsightsCustomException
	 */
	public String uploadH2o(String data, String name) throws InsightsCustomException {
		try {
			String response=null;			
			String url =h2oEndpoint + H2ORestApiUrls.POST_FILE;			
			log.debug("Uploading data into H2O:{}", name);			
			HashMap<String,String> map=new HashMap<>();
			map.put("filename", data);
			map.put("destination_frame", name);			
			response=RestApiHandler.uploadMultipartFileWithData(url, null, map, null, "multipart/form-data");
			log.debug("Upload:{} Response code:{} ", name, response);
			return response;
		} catch (Exception e) {
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
		queryParams.addProperty("chunk_size","50455");	
		String httpResponse=RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Post");
		 JsonObject response=  new JsonParser().parse(httpResponse).getAsJsonObject();
         String pollingURL= response.get("job").getAsJsonObject().get("key")
      		   .getAsJsonObject().get("URL").getAsString();         
		return pollRequestStatus(pollingURL);	
		
	}
	
	
	public String importFiles(String filePath) throws InsightsCustomException
	{			
		String url=h2oEndpoint+H2ORestApiUrls.IMPORT_FILE;			
		JsonObject queryParams= new JsonObject();
		queryParams.addProperty("path", filePath);			
		return RestApiHandler.httpQueryParamRequest(url, queryParams, null,"Get");		
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
			queryParams.addProperty("words_frame", inputFrame+".hex");	
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

	
	
	/** split frame into training and test frame with specified splitRatio
	 * @param inputFrame
	 * @param splitRatio
	 * @return
	 * @throws InsightsCustomException
	 */
	public String splitFrame(String inputFrame, double splitRatio) throws InsightsCustomException {
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
			log.debug("Building AutoML:{} ", modelName);
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

	
	public int pollRequestStatus(String url) throws InsightsCustomException {
		JsonObject response = new JsonObject();
		try {
			log.debug("Get progress of AutoML: {}", url);
			String h20url = h2oEndpoint + url;
			String status = "RUNNING";
			do {
				String httpResponse = RestApiHandler.doGet(h20url, null);
				response = new JsonParser().parse(httpResponse).getAsJsonObject();
				JsonArray jobs = response.get("jobs").getAsJsonArray();
				JsonObject job = jobs.get(0).getAsJsonObject();
				status = job.get("status").getAsString();
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
	
	/** Get the leaderboard from modelId
	 * @param modelId
	 * @return
	 * @throws InsightsCustomException
	 */
	public JsonObject getLeaderBoard(String modelId) throws InsightsCustomException {
		try {
			log.debug("Get Leaderboard: {}", modelId);			
			String url = h2oEndpoint+H2ORestApiUrls.LEADERBOARD_URL+modelId;			
			String response = RestApiHandler.doGet(url, null);
			JsonObject jsonResponse = new Gson().fromJson(response, JsonObject.class);
			JsonObject leaderboardTable = new Gson().fromJson(jsonResponse.get("leaderboard_table"), JsonObject.class);
			JsonArray columns = new Gson().fromJson(leaderboardTable.get(COLUMNS), JsonArray.class);
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

			String url = h2oEndpoint + H2ORestApiUrls.PREDICTION_URL + modelName + "/frames/" + testFrame;
			log.debug("Predicting for : {}", modelName);
			JsonObject emptyObject = new JsonObject();
			String httpResponse = RestApiHandler.doPost(url, emptyObject, null);
			JsonObject predictionData = new JsonObject();

			JsonObject responseObject = new Gson().fromJson(httpResponse, JsonObject.class);
			JsonObject model = responseObject.getAsJsonArray("model_metrics").get(0).getAsJsonObject();
			JsonArray data = model.getAsJsonObject("predictions").getAsJsonArray(COLUMNS);
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
			JsonArray columns = frame.getAsJsonArray(COLUMNS);
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
			log.debug(" Downloading MOJO:{} ", modelId);
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
	 * Get the vectors from transformed frame
	 *
	 * @param input
	 * @return
	 */
	private JsonArray getVectorColumns(String input) {
		JsonObject parsedjson = new Gson().fromJson(input, JsonObject.class);
		JsonArray frames = parsedjson.get("frames").getAsJsonArray();
		JsonObject frame = frames.get(0).getAsJsonObject();
		return frame.get(COLUMNS).getAsJsonArray();
	}

}
