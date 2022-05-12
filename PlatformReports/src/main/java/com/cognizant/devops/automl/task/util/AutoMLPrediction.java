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
package com.cognizant.devops.automl.task.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.enums.AutoMLEnum;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import hex.genmodel.MojoModel;
import hex.genmodel.TmpMojoReaderBackend;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;

public class AutoMLPrediction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -889238233275591510L;
	private static AutoMLConfigDAL autoMlDAL = new AutoMLConfigDAL();
	private static Logger log = LogManager.getLogger(AutoMLPrediction.class);
	
	
	private AutoMLPrediction() {
	}

	/** Predict regression on specified input data
	 * @param data
	 * @param columnNames
	 * @param usecaseName
	 * @return
	 * @throws PredictException 
	 * @throws IOException 
	 */
	
	public static List<JsonObject> getPrediction(List<JsonObject> data, JsonArray columnNames, String usecaseName) throws IOException, PredictException
	{
		AutoMLConfig autoMLConfig = autoMlDAL.getMLConfigByUsecase(usecaseName);
		String predictionType = autoMLConfig.getPredictionType();
		if (predictionType.equalsIgnoreCase(AutoMLEnum.PredictionType.REGRESSION.name())) {
		  return predictRegression(data, columnNames, usecaseName);
		} else {
			return predictClassification(data, columnNames, usecaseName);
		}
	}
	public static List<JsonObject> predictRegression(List<JsonObject> data, JsonArray columnNames, String usecaseName)
			throws IOException, PredictException {
		List<JsonObject> predictionData = new ArrayList<>();
		AutoMLConfig autoMLConfig = new AutoMLConfig();
		
		try {
			long startTime = System.nanoTime();
		    RegressionModelPrediction p = null;
		    autoMLConfig = autoMlDAL.getMLConfigByUsecase(usecaseName);
			String deployedMojoName = autoMLConfig.getMojoDeployed();
			String predectionColumn = autoMLConfig.getPredictionColumn();			
			/* get mojo from database and write to temporary location */
			String path =FileUtils.getTempDirectoryPath();
			byte[] mojoData=autoMLConfig.getMojoDeployedZip();
			File file = new File(path+usecaseName+".zip");
			FileUtils.writeByteArrayToFile(file, mojoData);			
			EasyPredictModelWrapper model = new EasyPredictModelWrapper(MojoModel.load(new TmpMojoReaderBackend(file)));
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Worlflow Detail ====  Mojo {}  Loaded Successfully",deployedMojoName);
			log.debug(StringExpressionConstants.STR_EXP_TASKEXECUTION,"-",autoMLConfig.getWorkflowConfig().getWorkflowId(),autoMLConfig.getWorkflowConfig().getAssessmentConfig().getId(),autoMLConfig.getWorkflowConfig().getWorkflowType(),"-","-",processingTime,
					ReportStatusConstants.MODELID +autoMLConfig.getModelId() + ReportStatusConstants.USECASENAME +autoMLConfig.getUseCaseName() + ReportStatusConstants.PREDICTIONCOLUMN +autoMLConfig.getPredictionColumn()
					 +ReportStatusConstants.PREDICTIONTYPE +autoMLConfig.getPredictionType()
					+ ReportStatusConstants.TRAININGPERCENTAGE + autoMLConfig.getTrainingPerc() + ReportStatusConstants.STATUS + autoMLConfig.getStatus() +"Mojo Loaded Successfully");
			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, String>>() {

			}.getType();
			for (JsonElement eachObject : data) {
				
				JsonObject object = eachObject.getAsJsonObject();
				RowData row = new RowData();
				JsonObject rowObject = new JsonObject();
				for (JsonElement eachColumn : columnNames) {
					String column = eachColumn.getAsString();
					String value = object.get(column)==null ? "" :object.get(column).getAsString();
					if (!value.equals("") ) {
						rowObject.addProperty(column, value);
					}
				}
				Map<String, String> eachRow = gson.fromJson(rowObject, type);
				row.putAll(eachRow);
				p = model.predictRegression(row);
				object.addProperty("predictedColumn",predectionColumn);
				object.addProperty("predictedValue", String.valueOf(p.value));
				predictionData.add(object);			
				
			}
			
		} catch (Exception e) {
			log.error(e);
			log.error(StringExpressionConstants.STR_EXP_TASKEXECUTION,"-",autoMLConfig.getWorkflowConfig().getWorkflowId(),autoMLConfig.getWorkflowConfig().getAssessmentConfig().getId(),autoMLConfig.getWorkflowConfig().getWorkflowType(),"-","-",0,
					ReportStatusConstants.MODELID +autoMLConfig.getModelId() + ReportStatusConstants.USECASENAME +autoMLConfig.getUseCaseName() + ReportStatusConstants.PREDICTIONCOLUMN +autoMLConfig.getPredictionColumn()
					 +ReportStatusConstants.PREDICTIONTYPE +autoMLConfig.getPredictionType()
					+ ReportStatusConstants.TRAININGPERCENTAGE + autoMLConfig.getTrainingPerc() + ReportStatusConstants.STATUS + autoMLConfig.getStatus()
					+"Something went wrong while executing regression prediction" + e.getMessage());
			throw new PredictException("Something went wrong while executing regression prediction for "+ usecaseName + " " + e.getMessage());
		}

		return predictionData;
	}
	
	public static List<JsonObject> predictClassification(List<JsonObject> data, JsonArray columnNames, String usecaseName) throws PredictException
	{
		List<JsonObject> predictionData = new ArrayList<>();
		AutoMLConfig autoMLConfig = new AutoMLConfig();		
		try {
			long startTime = System.nanoTime();
			BinomialModelPrediction  p = null;
			autoMLConfig=autoMlDAL.getMLConfigByUsecase(usecaseName);
			String deployedMojoName = autoMLConfig.getMojoDeployed();
			String predectionColumn = autoMLConfig.getPredictionColumn();
			/* get mojo from database and write to temporary location */
			String path =FileUtils.getTempDirectoryPath();
			byte[] mojoData=autoMLConfig.getMojoDeployedZip();
			File file = new File(path+usecaseName+".zip");
			FileUtils.writeByteArrayToFile(file, mojoData);			
			EasyPredictModelWrapper model = new EasyPredictModelWrapper(MojoModel.load(new TmpMojoReaderBackend(file)));					
			log.debug("Worlflow Detail ====  Mojo {}  Loaded Successfully",deployedMojoName);
			Gson gson = new Gson();
			Type type = new TypeToken<Map<String, String>>() {

			}.getType();
			for (JsonElement eachObject : data) {
				
				JsonObject object = eachObject.getAsJsonObject();
				RowData row = new RowData();
				JsonObject rowObject = new JsonObject();
				for (JsonElement eachColumn : columnNames) {
					String column = eachColumn.getAsString();
					String value = object.get(column)==null ? "" :object.get(column).getAsString();
					if (!value.equals("")) {
						rowObject.addProperty(column, value);
					}
				}
				Map<String, String> eachRow = gson.fromJson(rowObject, type);
				row.putAll(eachRow);
				p = model.predictBinomial(row);
				object.addProperty("predictedColumn",predectionColumn);
				object.addProperty("predictedValue", String.valueOf(p.label));
				predictionData.add(object);

			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASKEXECUTION,"-",autoMLConfig.getWorkflowConfig().getWorkflowId(),autoMLConfig.getWorkflowConfig().getAssessmentConfig().getId(),autoMLConfig.getWorkflowConfig().getWorkflowType(),"-","-",processingTime,
					ReportStatusConstants.MODELID +autoMLConfig.getModelId() + ReportStatusConstants.USECASENAME +autoMLConfig.getUseCaseName() + ReportStatusConstants.PREDICTIONCOLUMN +autoMLConfig.getPredictionColumn()
					 +ReportStatusConstants.PREDICTIONTYPE +autoMLConfig.getPredictionType()
					+ ReportStatusConstants.TRAININGPERCENTAGE + autoMLConfig.getTrainingPerc() + ReportStatusConstants.STATUS + autoMLConfig.getStatus());
		} catch (Exception e) {
			log.error(e);
			log.error(StringExpressionConstants.STR_EXP_TASKEXECUTION,"-",autoMLConfig.getWorkflowConfig().getWorkflowId(),autoMLConfig.getWorkflowConfig().getAssessmentConfig().getId(),autoMLConfig.getWorkflowConfig().getWorkflowType(),"-","-",0,
					ReportStatusConstants.MODELID +autoMLConfig.getModelId() + ReportStatusConstants.USECASENAME +autoMLConfig.getUseCaseName() + ReportStatusConstants.PREDICTIONCOLUMN +autoMLConfig.getPredictionColumn()
					 +ReportStatusConstants.PREDICTIONTYPE +autoMLConfig.getPredictionType()
					+ ReportStatusConstants.TRAININGPERCENTAGE + autoMLConfig.getTrainingPerc() + ReportStatusConstants.STATUS + autoMLConfig.getStatus()+
					"Something went wrong while executing classification prediction" +e.getMessage());
			throw new PredictException("Something went wrong while executing classification prediction for "+ usecaseName + " " + e.getMessage());
		}
		return predictionData;
	}
	
}
