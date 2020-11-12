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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
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
	 * @throws IOException
	 * @throws PredictException
	 */
	public static List<JsonObject> predictRegression(List<JsonObject> data, JsonArray columnNames, String usecaseName)
			throws IOException, PredictException {
		List<JsonObject> predictionData = new ArrayList<>();
		try {
		    RegressionModelPrediction p = null;
			AutoMLConfig autoMLConfig = autoMlDAL.getMLConfigByUsecase(usecaseName);
			String deployedMojoName = autoMLConfig.getMojoDeployed();
			String predectionColumn = autoMLConfig.getPredictionColumn();

			String mojoPath = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR + usecaseName
					+ ConfigOptions.FILE_SEPERATOR + deployedMojoName + ".zip";

			EasyPredictModelWrapper model = new EasyPredictModelWrapper(MojoModel.load(mojoPath));

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
					String value = object.get(column).getAsString();
					if (!value.equals("")) {
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
			log.debug(e.getMessage());
			return predictionData;
		}

		return predictionData;
	}
	
}
