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
package com.cognizant.devops.automl.service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface ITrainModelsService {
    public JsonObject validateUsecaseName(String usecase) throws InsightsCustomException;

    public JsonObject getHeaders(MultipartFile file, String usecase) throws InsightsCustomException;

    public JsonObject uploadData(JsonArray contents, String usecase, String config) throws InsightsCustomException;

    public int splitData(String usecase, String splitRatio) throws InsightsCustomException;

    public JsonObject runAutoML(String usecase, String predictionColumn, String numOfModels) throws InsightsCustomException;

    public JsonObject getAutoMLProgress(String usecase, String url) throws InsightsCustomException;

    public JsonObject getLeaderBoard(String usecase) throws InsightsCustomException;

    public JsonObject getPrediction(String usecase, String modelName) throws InsightsCustomException;

    public JsonObject downloadMojo(String usecase, String modelName) throws InsightsCustomException;

    public JsonObject deleteUsecase(String usecase) throws InsightsCustomException;

    public JsonObject getUsecases() throws InsightsCustomException;
    
    public int saveAutoMLConfig(MultipartFile file,String usecase,
			 String configuration, Integer trainingPerc,  String predictionColumn,
			 String numOfModels,String taskDetails) throws InsightsCustomException;
}
