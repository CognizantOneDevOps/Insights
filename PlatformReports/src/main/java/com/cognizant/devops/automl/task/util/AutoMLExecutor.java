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
package com.cognizant.devops.automl.task.util;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.AutoMLEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AutoMLExecutor implements Callable<JsonObject> {

	private static Logger log = LogManager.getLogger(AutoMLExecutor.class);
	private TrainModelsUtils trainUtils = new TrainModelsUtils();
	private AutoMLConfigDAL autoMlDAL = new AutoMLConfigDAL();
	private AutoMLConfig autoMlConfig;
	private String usecaseCSVFilePath;

	public AutoMLExecutor(AutoMLConfig autoMlConfig) {
		super();
		this.autoMlConfig = autoMlConfig;
	}

	@Override
	public JsonObject call() throws Exception {

		JsonObject response = new JsonObject();
		try {
			usecaseCSVFilePath = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR
					+ autoMlConfig.getUseCaseName() + ConfigOptions.FILE_SEPERATOR + autoMlConfig.getUseCaseFile();
			File useCasecsvFile = new File(usecaseCSVFilePath);			
			JsonObject extractedCSVData = extractCSVData(useCasecsvFile);
			if (!extractedCSVData.keySet().isEmpty()) {
				parseAndUploadDataToH2o(extractedCSVData.getAsJsonArray("Contents"));
				String splitFrameResponse = splitFrames();
				log.info("AutoML Executor === Spliting frames for usecase {} is completed successfully",
						autoMlConfig.getUseCaseName());
				runAutoML(splitFrameResponse);
				response.addProperty("Status", "Success");
			}
		} catch (InsightsCustomException e) {
			log.error("AutoML Executor === Error while executing AutoML on usecase {}", autoMlConfig.getUseCaseName());
			response.addProperty("Status", "Failure");
			response.addProperty("errorLog", e.getMessage());
		}
		return response;
	}

	/**
	 * Step 1 extract headers and content from csv
	 * 
	 * @param file
	 * @return
	 */
	private JsonObject extractCSVData(File file) {
		JsonObject response = new JsonObject();
		String usecase = autoMlConfig.getUseCaseName();
		try {
			JsonObject extractedDataFromCSV = trainUtils.getHeaders(file, usecase);
			log.error("AutoML Executor === CSV data extracted succssfully for usecase {}", usecase);
			if (extractedDataFromCSV.has("Header") && extractedDataFromCSV.has("Contents")) {
				log.debug("AutoML Executor === CSV data extracted  {}", extractedDataFromCSV);
				return extractedDataFromCSV;
			}
		} catch (InsightsCustomException e) {
			log.error("AutoML Executor === Error while extracting data from CSV for usecase {}", usecase);
		}
		return response;
	}

	/**
	 * Step 2 3 4 perform nlp if any , if nlp then perform and transform W2Vector ,
	 * parse data and upload to h2o
	 * 
	 * @param csvContents
	 * @throws InsightsCustomException
	 */
	private void parseAndUploadDataToH2o(JsonArray csvContents) throws InsightsCustomException {
		trainUtils.uploadData(csvContents, autoMlConfig.getUseCaseName(), autoMlConfig.getConfigJson(),usecaseCSVFilePath);
	}

	/**
	 * Step 5 Split frames into training and test frame
	 * 
	 * @return
	 * @throws InsightsCustomException
	 */
	private String splitFrames() throws InsightsCustomException {
		double traingratio = Double.parseDouble(String.valueOf(autoMlConfig.getTrainingPerc())) / 100;
		return trainUtils.splitData(autoMlConfig.getUseCaseName(), traingratio);
	}

	/**
	 * step 6 RunAutoML to prepare leaderboard and prediction
	 * 
	 * @param dataFrameResponse
	 * @throws InsightsCustomException
	 */
	private void runAutoML(String dataFrameResponse) throws InsightsCustomException {

		log.info("AutoML Executor === AutoML is running on usecase {} wait for a while to finish",
				autoMlConfig.getUseCaseName());
		JsonObject responseJson = new JsonParser().parse(dataFrameResponse).getAsJsonObject();
		JsonArray destinationFrames = responseJson.get("destination_frames").getAsJsonArray();
		String trainingFrame = destinationFrames.get(0).getAsJsonObject().get("name").getAsString();
		JsonObject mlResponse = trainUtils.runAutoML(autoMlConfig.getUseCaseName(), trainingFrame,
				autoMlConfig.getPredictionColumn(), autoMlConfig.getNumOfModels());
		int status = mlResponse.get("status").getAsInt();
		if (status == 200) {
			String modelId = mlResponse.get("name").getAsString();
			autoMlConfig.setStatus(AutoMLEnum.Status.LEADERBOARD_READY.name());
			autoMlConfig.setModelId(modelId);
			autoMlDAL.updateMLConfig(autoMlConfig);
			log.info(
					"AutoML Executor === AutoML on usecase {} is completed successfully and usecase is ready for leaderboard and prediction for modelId {}",
					autoMlConfig.getUseCaseName(), modelId);
		}
	}

}
