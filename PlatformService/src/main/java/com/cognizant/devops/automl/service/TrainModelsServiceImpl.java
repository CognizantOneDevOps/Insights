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


import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.AutoMLEnum;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.ai.H2oApiCommunicator;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformservice.workflow.service.WorkflowServiceImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service("trainModelsService")
public class TrainModelsServiceImpl implements ITrainModelsService {
    private static final Logger log = LogManager.getLogger(TrainModelsServiceImpl.class);
    public static final String USECASE_NAME = "usecaseName";
    String FILE_SEPERATOR = File.separator;
    AutoMLConfigDAL autoMLConfigDAL;
    H2oApiCommunicator h2oApiCommunicator;
    File MLDIRECTORY = new File(ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH);
    WorkflowServiceImpl workflowService = new WorkflowServiceImpl();
    ReportConfigDAL reportConfigDAL = new ReportConfigDAL();

    public TrainModelsServiceImpl() {
        h2oApiCommunicator = new H2oApiCommunicator();
        autoMLConfigDAL = new AutoMLConfigDAL();
        if (!MLDIRECTORY.exists()) {
            MLDIRECTORY.mkdir();
        }
    }

    /**
     * Validate if usecase name is unique from postgres
     *
     * @param usecase
     * @return
     * @throws InsightsCustomException
     */
    @Override
    public JsonObject validateUsecaseName(String usecase) throws InsightsCustomException {
        try {
            log.debug("Validating usecase: {}" , usecase);
            JsonObject response = new JsonObject();
            response.addProperty("UniqueUsecase", autoMLConfigDAL.isUsecaseExisting(usecase));
            response.addProperty("Usecase", usecase);
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InsightsCustomException(e.getMessage());
        }
    }


    /**
     * Fetch the AutoML leaderboard after completion (Status:DONE) of the build
     *
     * @param usecase
     * @return
     * @throws InsightsCustomException
     */
    @Override
    public JsonObject getLeaderBoard(String usecase) throws InsightsCustomException {
		try {
			AutoMLConfig mlConfig=autoMLConfigDAL.getMLConfigByUsecase(usecase);
			String modelId = mlConfig.getModelId();
			return h2oApiCommunicator.getLeaderBoard(modelId);

		} catch (Exception e) {
			log.error("Error getting leaderboard: {} " , usecase);
			throw new InsightsCustomException("Error getting leaderboard: " + usecase);
		}
      
    }

    /**
     * Perform Prediction on part1 of split training data using the model selected. Returns actual data and prediction value.
     *
     * @param usecase
     * @param modelName
     * @return
     * @throws InsightsCustomException
     */
    @Override
    public JsonObject getPrediction(String modelName,String usecaseName) throws InsightsCustomException {
        JsonObject prediction = h2oApiCommunicator.predict(modelName, usecaseName+"_part1.hex");
        JsonArray data = h2oApiCommunicator.getDataFrame(usecaseName+"_part1.hex");
        JsonObject response = new JsonObject();
        JsonArray fields = new JsonArray();
        Set<Map.Entry<String, JsonElement>> es = data.get(0).getAsJsonObject().entrySet();
        for (Iterator<Map.Entry<String, JsonElement>> it = es.iterator(); it.hasNext(); ) {
            fields.add(it.next().getKey());
        }
        if (data.size() == prediction.getAsJsonArray("predict").size()) {
         
            for (int i = 0; i < data.size(); i++) {
           
                for (Map.Entry<String, JsonElement> entry : prediction.entrySet()) {
                    String key = entry.getKey();
                    if (i == 0)
                        fields.add(key);
                    String val = entry.getValue().getAsJsonArray().get(i).getAsString();
                    int max = val.length() < 5 ? val.length() : 5;
                    data.get(i).getAsJsonObject().addProperty(key, val.substring(0, max));
                 
                }
            }	           
            response.add("Fields", fields);	           
            response.add("Data", data);
            return response;
        } else {
            log.error("Mismatch in test data and prediction: ");
            throw new InsightsCustomException("Mismatch in test data and prediction: ");
        }
    }

    /**
     * Download the selected model and store the zip in file directory. Update the selected modelname in Postgres
     *
     * @param usecase
     * @param modelId
     * @return
     * @throws InsightsCustomException
     */
    @Override
    public JsonObject downloadMojo(String usecase, String modelId) throws InsightsCustomException {
		JsonObject response = new JsonObject();
		try {
			InputStream fileStream = h2oApiCommunicator.downloadMojo(usecase, modelId);
			if (fileStream != null) {
				byte[] mojoByteArray = IOUtils.toByteArray(fileStream);
				response = new JsonObject();
				response.addProperty("Message", "Mojo has been saved successfully.");
				AutoMLConfig mlConfig = autoMLConfigDAL.getMLConfigByUsecase(usecase);
				mlConfig.setMojoDeployed(modelId);
				mlConfig.setStatus(AutoMLEnum.Status.MOJO_DEPLOYED.name());
				mlConfig.setMojoDeployedZip(mojoByteArray);
				autoMLConfigDAL.updateMLConfig(mlConfig);
				return response;
			} else {
				log.error("Error downloading Mojo:{} ", usecase);
				throw new InsightsCustomException("Unable to download mojo. Refer logs for more information");
			}
		} catch (Exception e) {
			log.error("Error updating MOJO details in Postgres:{} due to {} ", usecase, e.getMessage());
			throw new InsightsCustomException("Unable to download mojo. Refer logs for more information");
		}
    }

    /**
     * Delete the usecase directory from file directory and entry from Postgres.
     *
     * @param usecase
     * @return
     * @throws InsightsCustomException
     */
    @Override
    public JsonObject deleteUsecase(String usecase) throws InsightsCustomException {

		/*
		 * status code -1 -- Unable to delete record from DB status code 0 -- deleted
		 * db and file from system successfully. status code 1 -- deleted from db but
		 * failed to delete file *
		 * 
		 */
		final String STATUSCODE = "statusCode";
		JsonObject response = new JsonObject();
		
		if(reportConfigDAL.getKpiConfigByUsecase(usecase).isEmpty()) {
			if (!autoMLConfigDAL.deleteUsecase(usecase)) {
				response.addProperty(STATUSCODE, -1);
				log.error("Unable to delete usecase {} from Database ", usecase);
				return response;
			}
			response.addProperty(STATUSCODE, 0);
			File directory = new File(ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + FILE_SEPERATOR + usecase);
			if (!FileSystemUtils.deleteRecursively(directory)) {
				log.error("Unable to delete usecase {} directory ", usecase);
			}
			response.addProperty(STATUSCODE, 1);
			return response;
		} else {
			throw new InsightsCustomException("Usecase cannot be deleted as it is attached to kpi.");
		}
		
    }

  
    /* Get the all usecases
     * @see com.cognizant.devops.automl.service.ITrainModelsService#getUsecases()
     */
    @Override
    public JsonObject getUsecases() throws InsightsCustomException {
        JsonObject response = new JsonObject();
        JsonArray responseArray = new JsonArray();
        try {
            List<AutoMLConfig> results = autoMLConfigDAL.fetchUsecases();
            if (!results.isEmpty()) {
            	results.forEach(eachMLConfig->{
            		JsonObject eachObject = new JsonObject();
					eachObject.addProperty("workflowId", eachMLConfig.getWorkflowConfig().getWorkflowId());
            		eachObject.addProperty(USECASE_NAME,eachMLConfig.getUseCaseName());
            		eachObject.addProperty("predictionColumn", eachMLConfig.getPredictionColumn());
            		eachObject.addProperty("splitRatio",eachMLConfig.getTrainingPerc()+"/"+(100-eachMLConfig.getTrainingPerc()));
            		eachObject.addProperty("modelName", eachMLConfig.getMojoDeployed());
            		eachObject.addProperty("createdAt", eachMLConfig.getCreatedDate());
            		eachObject.addProperty("updatedAt", eachMLConfig.getUpdatedDate());
            		eachObject.addProperty("status", eachMLConfig.getStatus());
            		eachObject.addProperty("isActive", eachMLConfig.getIsActive());
            		eachObject.addProperty("predictionType", eachMLConfig.getPredictionType());
            		responseArray.add(eachObject);
            	});
                response.add("usecases", responseArray);
            } else {
                response.addProperty("nodata",404);
            }
            return response;
        } catch (Exception e) {
            throw new InsightsCustomException(e.getMessage());
        }
    }  
   	
	/* Save Automl config and place usecase file to specified location in InsightsHome
	 * @see com.cognizant.devops.automl.service.ITrainModelsService#saveAutoMLConfig(org.springframework.web.multipart.MultipartFile, java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public int saveAutoMLConfig(MultipartFile file, String usecase, String configuration, Integer trainingPerc,
			String predictionColumn, String numOfModels ,String taskDetails, String predictionType) throws InsightsCustomException {
			
		int id = -1;
		byte[] fileBytes;
		boolean isExists = autoMLConfigDAL.isUsecaseExisting(usecase);
		if (isExists) {
			log.error("AutoMLSerive======= unable to save record as usecase {} already exists", usecase);
			throw new InsightsCustomException("usecase already exists " + usecase);
		} else {
			
			/* Save file to Insights_Home */
			
			String folderPath = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + usecase
					+ ConfigOptions.FILE_SEPERATOR;
			File destfolder = new File(folderPath);
			if (destfolder.mkdirs()) {
				log.debug("AutoML========== Usecasefolder is created {}", destfolder.getAbsolutePath());
			}
			String filePath = folderPath + file.getOriginalFilename();
			destfolder = new File(filePath);
			try {
				file.transferTo(destfolder);
				fileBytes= FileUtils.readFileToByteArray(destfolder);
			} catch (Exception e) {
				log.debug("AutoML========== Exception while creating folder {}", e.getMessage());
				throw new InsightsCustomException("Unable to create folder");
			}		
			
			/* Save record in DB and create workflow */
			
			JsonParser parser = new JsonParser();
			JsonArray taskList = parser.parse(taskDetails).getAsJsonArray();
			boolean runImmediate = true;
			boolean reoccurence = false;
			String schedule = WorkflowTaskEnum.WorkflowSchedule.ONETIME.name();
			boolean isActive = true;
			String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			String workflowType = "AUTOML";
			String workflowId = WorkflowTaskEnum.WorkflowType.AUTOML.getValue() + "_"
					+ InsightsUtils.getCurrentTimeInSeconds();
			InsightsWorkflowConfiguration workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
					reoccurence, schedule, workflowStatus, workflowType, taskList, 0, null, runImmediate);
			AutoMLConfig mlConfig = new AutoMLConfig();
			mlConfig.setUseCaseName(usecase);
			mlConfig.setConfigJson(configuration);
			mlConfig.setIsActive(true);
			mlConfig.setPredictionColumn(predictionColumn);
			mlConfig.setNumOfModels(numOfModels);
			mlConfig.setTrainingPerc(trainingPerc);
			mlConfig.setMojoDeployed("");
			mlConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			mlConfig.setUseCaseFile(file.getOriginalFilename());
			mlConfig.setWorkflowConfig(workflowConfig);
			mlConfig.setStatus(workflowStatus);
			mlConfig.setPredictionType(predictionType);
			mlConfig.setFile(fileBytes);
			id = autoMLConfigDAL.saveMLConfig(mlConfig);
			log.debug(id);

		}
		return id;
	}

	/**
     * Fetch all active and mojo_deployed usecase list from Postgres.
     *
     * @return
     */
	@Override
	public JsonArray  getMojoDeployedUsecases() throws InsightsCustomException {
        JsonArray responseArray = new JsonArray();
        try {
        	List<AutoMLConfig> results = autoMLConfigDAL.getActiveUsecaseList();
            if (!results.isEmpty()) {
            	for(AutoMLConfig eachMLConfig: results){
            		JsonObject eachObject = new JsonObject();
            		eachObject.addProperty(USECASE_NAME,eachMLConfig.getUseCaseName());
            		eachObject.addProperty("predictionColumn", eachMLConfig.getPredictionColumn());
            		responseArray.add(eachObject);
            	}
            } else {
            	throw new InsightsCustomException("No Mojo_deployed Usecase found.");
            }
            return responseArray;
        } catch (Exception e) {
            throw new InsightsCustomException(e.getMessage());
        }
	}
	
	/**
	 * Method use to make usecase state active or inactive
	 * 
	 * @param usecaseJson
	 * @return
	 * @throws InsightsCustomException
	 */
	@Override
	public String updateUsecaseState(JsonObject usecaseJson) throws InsightsCustomException {
		String message = "";
		try {
			String usecase = usecaseJson.get(USECASE_NAME).getAsString();
			Boolean state = usecaseJson.get("isActive").getAsBoolean();
			AutoMLConfig mlConfig = autoMLConfigDAL.getMLConfigByUsecase(usecase);
			if (mlConfig == null) {
				throw new InsightsCustomException("Usecase not found." + usecase);
			} else {
				if(reportConfigDAL.getKpiConfigByUsecase(usecase).isEmpty()) {
					mlConfig.setIsActive(state);
					autoMLConfigDAL.updateMLConfig(mlConfig);
					message = usecase + " state updated successfully."; 
				} else {
					if(Boolean.TRUE.equals(state)) {
						mlConfig.setIsActive(state);
						autoMLConfigDAL.updateMLConfig(mlConfig);
						message = usecase + " state updated successfully."; 
					} else {
						throw new InsightsCustomException("Usecase cannot be deactivated as it is attached to kpi.");
					}
				}	
			}
		} catch (NoResultException e) {
			log.error(e);
			throw new InsightsCustomException("Usecase does not exists in database.");
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException(e.getMessage());
		}
		return message;
	}
	
	
	/**
	 * @return
	 */
	public List<String> getPredictionTypes() {
		List<String> listOfPredictionTypes = new ArrayList<>();
		AutoMLEnum.PredictionType[] predictionType = AutoMLEnum.PredictionType.values();
		for (AutoMLEnum.PredictionType type : predictionType) {
			listOfPredictionTypes.add(type.toString().toUpperCase());
		}
		return listOfPredictionTypes;
	}

}
