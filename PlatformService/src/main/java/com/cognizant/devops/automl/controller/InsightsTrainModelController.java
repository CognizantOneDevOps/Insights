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
package com.cognizant.devops.automl.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.cognizant.devops.automl.service.TrainModelsServiceImpl;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/admin/trainmodels")
public class InsightsTrainModelController {
	public static final String INFRASTRUCTURE_ERROR = "Infrastructure Error";
	static Logger log = LogManager.getLogger(InsightsTrainModelController.class.getName());

	@Autowired
	TrainModelsServiceImpl trainModelsService;

	/**
	 * Check for Unique usecase name
	 *
	 * @param usecase
	 * @return
	 */
	@GetMapping(value = "/validateUsecaseName", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject validateUsecaseName(@RequestParam String usecase) {
		JsonObject response = null;
		try {
			response = trainModelsService.validateUsecaseName(usecase);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}



	/**
	 * Upload csv data and configuration to run NLP and upload the frame into H2o
	 * Save the usecase name, configuration and created date in postgres
	 *
	 * @param file
	 * @param usecase
	 * @param configuration
	 * @return
	 * @throws IOException
	 */
	@PostMapping(value = "/saveUsecase", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<JsonObject> saveUsecase(@RequestParam("file") MultipartFile file,
			@RequestParam String usecase, @RequestParam String configuration, @RequestParam Integer trainingPerc,
			@RequestParam String predictionColumn, @RequestParam String numOfModels, @RequestParam String taskDetails) {
		try {
			boolean checkValidFile = PlatformServiceUtil.validateFile(file.getOriginalFilename());
			log.debug("checkValidFile: {} ", checkValidFile);
			if (checkValidFile) {
				trainModelsService.saveAutoMLConfig(file, usecase, configuration, trainingPerc, predictionColumn,
						numOfModels, taskDetails);
				return new ResponseEntity<>(PlatformServiceUtil.buildSuccessResponse(), HttpStatus.OK);
			} else {
				return new ResponseEntity<>(
						PlatformServiceUtil.buildFailureResponse("Error while parsing file , Please try again !"),
						HttpStatus.OK);
			}
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponse(e.getMessage()), HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(
					PlatformServiceUtil.buildFailureResponseWithStatusCode(INFRASTRUCTURE_ERROR, "500"),
					HttpStatus.OK);
		}

	}




	/**
	 * Get the leaderboard result of a successful AutoML build
	 *
	 * @param usecase
	 * @return
	 */
	@GetMapping(value = "/getLeaderboard", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getLeaderBoard(@RequestParam String usecase) {
		JsonObject response = null;
		try {
			response = trainModelsService.getLeaderBoard(usecase);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response.get("data").getAsJsonArray());
	}

	/**
	 * Predict based on the model selected and the test data that was split from the
	 * original training data set
	 *
	 * @param usecase
	 * @param modelName
	 * @return
	 */
	@GetMapping(value = "/getPrediction",produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getPrediction(@RequestParam String usecase, @RequestParam String modelName) {
		JsonObject response = null;
		try {
			response = trainModelsService.getPrediction(modelName, usecase);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}

	/**
	 * Download the MOJO(zip) for any trained model into
	 * $INSIGHTS_HOME/MLData/<<usecase>>/ Save the Mojo name in postgres
	 *
	 * @param usecase
	 * @param modelId
	 * @return
	 */
	@GetMapping(value = "/downloadMojo", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject downloadMojo(@RequestParam String usecase, @RequestParam String modelId) {
		JsonObject response = null;
		try {
			response = trainModelsService.downloadMojo(usecase, modelId);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}

	/**
	 * Delete the usecase from file directory and postgres
	 *
	 * @param usecase
	 * @return
	 */
	@PostMapping(value = "/deleteusecase", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<JsonObject> deleteUsecase(@RequestParam String usecase) {
		JsonObject response = null;
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(usecase);
			response = trainModelsService.deleteUsecase(validatedResponse);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			if (e.getMessage().equals("Usecase cannot be deleted as it is attached to kpi.")){
				return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponseWithStatusCode(e.getMessage(), "409"),HttpStatus.OK);
			} else {
				return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponseWithStatusCode(INFRASTRUCTURE_ERROR, "500"),HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(PlatformServiceUtil.buildSuccessResponseWithData(response), HttpStatus.OK);
	}

	/**
	 * Get the list of all usecases from Postgres
	 *
	 * @return
	 */
	@GetMapping(value = "/getUsecases", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<JsonObject> getUsecases() {
		JsonObject response = null;
		try {
			response = trainModelsService.getUsecases();
			if (response.has("nodata")) {
				return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponseWithStatusCode("Not Found", "204"),HttpStatus.OK);
			}
			return new ResponseEntity<>(PlatformServiceUtil.buildSuccessResponseWithData(response), HttpStatus.OK);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponseWithStatusCode(INFRASTRUCTURE_ERROR, "500"),HttpStatus.OK);
		}

	}
	
	/**
	 * Fetch all active and mojo_deployed usecase list from Postgres.
	 *
	 * @return
	 */
	@GetMapping(value = "/getMojoDeployedUsecases", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject getMojoDeployedUsecases() {
		JsonArray response = null;
		try {
			response = trainModelsService.getMojoDeployedUsecases();
			return PlatformServiceUtil.buildSuccessResponseWithData(response);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}

	}
	
	/**
	 * change usecase state to active or inactive
	 *
	 * @return
	 */
	@PostMapping(value = "/updateUsecaseState", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody JsonObject updateUsecaseState(@RequestBody String usecaseConfig) {
		try {
			String validatedResponse = ValidationUtils.validateRequestBody(usecaseConfig);
			JsonParser parser = new JsonParser();
			JsonObject usecaseJson = (JsonObject) parser.parse(validatedResponse);
			String message= trainModelsService.updateUsecaseState(usecaseJson);
			return PlatformServiceUtil.buildSuccessResponseWithData(message);
		} catch (InsightsCustomException e) {
			log.error(e);
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		} catch (Exception e) {
			log.error(e);
			return PlatformServiceUtil
					.buildFailureResponse("Unable to update usecase state. Please check log for details.");
		}
	}
}
