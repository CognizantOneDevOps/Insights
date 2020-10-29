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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.automl.service.TrainModelsServiceImpl;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/trainmodels")
public class InsightsTrainModelController {
	static Logger log = LogManager.getLogger(InsightsTrainModelController.class.getName());

	@Autowired
	TrainModelsServiceImpl trainModelsService;

	/**
	 * Check for Unique usecase name
	 *
	 * @param usecase
	 * @return
	 */
	@RequestMapping(value = "/validateUsecaseName", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
	 * <<Not used>> entry point to upload Training Data and return headers and
	 * contents of csv
	 *
	 * @param file
	 * @return JsonObject
	 */
	@RequestMapping(value = "/getHeaders", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getHeaders(@RequestParam("file") MultipartFile file, @RequestParam String usecase) {
		JsonObject response = null;
		try {
			response = trainModelsService.getHeaders(file, usecase);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}

	/**
	 * entry point to upload Training Data
	 *
	 * @param contents
	 * @return JsonObject
	 */
	/*
	 * @RequestMapping(value = "/uploadData", method = RequestMethod.POST, produces
	 * = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes =
	 * MediaType.MULTIPART_FORM_DATA_VALUE) public @ResponseBody
	 * DeferredResult<JsonObject> uploadData(@RequestParam("contents") String
	 * contents, @RequestParam("usecase") String
	 * usecase, @RequestParam("configuration") String configuration) { JsonObject
	 * response = new JsonObject(); DeferredResult<JsonObject> output = new
	 * DeferredResult<>(); try {
	 */
	/*
	 * status = bulkUploadService.getHeaders(file, toolName, label,
	 * insightsTimeField, insightsTimeFormat);
	 *//*
		 * response = trainModelsService.uploadData(contents, usecase, configuration);
		 * if (!response.isJsonNull()) output.setResult(response); return output; }
		 * catch (Exception e
		 *//* , InsightsCustomException e *//*
											 * ) { log.error(e.getMessage()); return output; //return
											 * PlatformServiceUtil.buildFailureResponse(e.getMessage()); } }
											 */

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
					PlatformServiceUtil.buildFailureResponseWithStatusCode("Infrastructure Error", "500"),
					HttpStatus.OK);
		}

	}

/*	@PostMapping(value = "/saveUsecase", produces = MediaType.APPLICATION_JSON_VALUE) 
	public @ResponseBody ResponseEntity < JsonObject > uploadData(@RequestParam("file") MultipartFile file, @RequestParam String usecase,
		    @RequestParam String configuration, @RequestParam String splitRatio, @RequestParam String predictionColumn,
		    @RequestParam String numOfModels, @RequestParam String taskDetails) throws
		    IOException {
		    Map < String, Object > map = new HashMap < > ();
		    JsonObject response
		        = null;
		    JsonObject resp = null;
		    int id = -1;
		    DeferredResult < JsonObject > output = new DeferredResult < > ();
		    try {
		        boolean checkValidFile =
		            PlatformServiceUtil.validateFile(file.getOriginalFilename());
		        log.debug("checkValidFile: {} ", checkValidFile); // boolean isValid =
		        PlatformServiceUtil.checkFileForHTML(new
		            if (checkValidFile) { // && isValid
		                // Store it in table id= trainModelsService.saveAutoMLConfig(file, usecase,
		                configuration,
		                splitRatio,
		                predictionColumn,
		                numOfModels,
		                taskDetails); 
		        JsonObject parsedCsv = trainModelsService.getHeaders(file, usecase); 
		        response = trainModelsService.uploadData(parsedCsv.getAsJsonArray("Contents"), usecase, configuration);
		            if (!response.isJsonNull())
		                output.setResult(PlatformServiceUtil.buildSuccessResponseWithData(response));
		        }
		        else {
		            output.setResult(PlatformServiceUtil.buildFailureResponse("Invalid file content. "));
		        }
		        if (trainModelsService.splitData(usecase, splitRatio) == 200) { // run automl
		            resp = trainModelsService.runAutoML(usecase, predictionColumn, numOfModels);
		        } else {
		            resp.addProperty("ResponseCode", 300);
		            resp.addProperty("Message",
		                "Failed to split and run AutoML");
		        }
		    } catch (InsightsCustomException e) {
		        log.error(e.getMessage());
		        return map.put("exeception",
		            output.setResult(PlatformServiceUtil.buildFailureResponse(e.getMessage())));

		    } catch (IOException e) {
		        log.error(e.getMessage());
		        return
		        map.put("execption",
		            output.setResult(PlatformServiceUtil.buildFailureResponse(e.getMessage())));
		    }

		    map.put("updateData", output);
		    map.put("splitData",
		        PlatformServiceUtil.buildSuccessResponseWithData(resp));

		    map.put("response", output.setResult(PlatformServiceUtil.buildSuccessResponseWithData(" ML config saved with Id " + id))); // split
		   // and get train and test data sets

		    return map;
		}*/
	/**
	 * Split the training frame and start AutoML build using the training data. Save
	 * the prediction column name in postgres. Returns an url to check for the
	 * progress of AutoML build.
	 *
	 * @param usecase
	 * @param splitRatio
	 * @param predictionColumn
	 * @return
	 */
	@RequestMapping(value = "/splitAndTrain", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject splitAndTrain(@RequestParam String usecase, @RequestParam String splitRatio,
			@RequestParam String predictionColumn, @RequestParam String numOfModels) {
		JsonObject response = null;
		// split and get train and test data sets
		try {
			if (trainModelsService.splitData(usecase, splitRatio) == 200) {
				// run automl
				response = trainModelsService.runAutoML(usecase, predictionColumn, numOfModels);
			} else {
				response.addProperty("ResponseCode", 300);
				response.addProperty("Message", "Failed to split and run AutoML");
			}
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
	}

	/**
	 * Get the progress % and status of AutoML build
	 *
	 * @param usecase
	 * @param url
	 * @return
	 */
	@RequestMapping(value = "/getAutoMLProgress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getAutoMLProgress(@RequestParam String usecase, @RequestParam String url) {
		JsonObject response = null;
		try {
			response = trainModelsService.getAutoMLProgress(usecase, url);
		} catch (InsightsCustomException e) {
			log.error(e.getMessage());
			return PlatformServiceUtil.buildFailureResponse(e.getMessage());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(response);
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
			return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponseWithStatusCode("Infrastructure Error", "500"),HttpStatus.OK);
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
			return new ResponseEntity<>(PlatformServiceUtil.buildFailureResponseWithStatusCode("Infrastructure Error", "500"),HttpStatus.OK);
		}

	}
}
