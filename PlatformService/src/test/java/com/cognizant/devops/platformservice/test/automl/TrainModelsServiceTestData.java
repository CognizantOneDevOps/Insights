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
package com.cognizant.devops.platformservice.test.automl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.AutMLEnum;
import com.cognizant.devops.platformcommons.dal.ai.H2oApiCommunicator;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowTask;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TrainModelsServiceTestData {
	
	private static final Logger log = LogManager.getLogger(TrainModelsServiceTestData.class);

	
	WorkflowDAL workflowConfigDAL = new WorkflowDAL();
	AutoMLConfigDAL autoMLConfigDAL = new AutoMLConfigDAL();
	H2oApiCommunicator h2oApiCommunicator;
	ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	
	String usecase = "automl_usecase_00172";
	Integer trainingPercent = 80;
	String predictionColumn = "Commits";
	String numOfModels = "6";
	File file = new File(classLoader.getResource("GitAuthorData.csv").getFile());
	String configuration = "[{\"FieldName\":\"Date\",\"DataType\":\"Time\",\"EnableNLP\":false},{\"FieldName\":\"AuthorName\",\"DataType\":\"Enum\",\"EnableNLP\":false},{\"FieldName\":\"Experience\",\"DataType\":\"Numeric\",\"EnableNLP\":false},{\"FieldName\":\"RepoName\",\"DataType\":\"Numeric\",\"EnableNLP\":false},{\"FieldName\":\"Commits\",\"DataType\":\"Numeric\",\"EnableNLP\":false}]";
	String workflowTask = "{\"description\":\"H2O_AutoML_Execute\",\"mqChannel\":\"WORKFLOW.TASK.AUTOML.EXCECUTION\",\"componentName\":\"com.cognizant.devops.automl.task.core.AutoMLSubscriber\",\"dependency\":-1,\"workflowType\":\"AutoML\"}";
	JsonObject workflowTaskJson = new JsonParser().parse(workflowTask).getAsJsonObject();
	boolean isTaskExists = false;
	String mqChannel = "WORKFLOW.TASK.AUTOML.EXCECUTION";
	String modelName = null;
	
	int numOfColumn = 5;
	String columnTypes = "Time,Enum,Numeric,Numeric,Numeric";
	String columnNames = "Date,AuthorName,Experience,RepoName,Commits";

	
	public String getTaskList() {
		List<InsightsWorkflowTask> listofTasks = workflowConfigDAL.getTaskLists("AUTOML");
		JsonArray jsonarray = new JsonArray();
		int i = 0;
		for (InsightsWorkflowTask taskDetail : listofTasks) {
			JsonObject jsonobject = new JsonObject();
			jsonobject.addProperty("taskId", taskDetail.getTaskId());
			jsonobject.addProperty("sequence", i);
			jsonarray.add(jsonobject);
			i++;
		}
		return jsonarray.toString();
	}
	
	public void executeAutomlConfig(String usecase) throws InsightsCustomException {
		h2oApiCommunicator = new H2oApiCommunicator();
		AutoMLConfig autoMlConfig = autoMLConfigDAL.getMLConfigByUsecase(usecase);
		String usecaseCSVFilePath = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR
				+ usecase + ConfigOptions.FILE_SEPERATOR + "GitAuthorData.csv";
		File useCasecsvFile = new File(usecaseCSVFilePath);
		JsonObject extractedData = getHeaders(useCasecsvFile, usecase);
		uploadData(extractedData.getAsJsonArray("Contents"), usecase, autoMlConfig.getConfigJson());
		String splitFrameResponse = h2oApiCommunicator.splitFrame(usecase, 0.8);
		JsonObject responseJson = new JsonParser().parse(splitFrameResponse).getAsJsonObject();
		JsonArray destinationFrames = responseJson.get("destination_frames").getAsJsonArray();
		String trainingFrame = destinationFrames.get(0).getAsJsonObject().get("name").getAsString();
		JsonObject mlResponse = runAutoML(usecase, trainingFrame,
				predictionColumn, numOfModels);
		int status = mlResponse.get("status").getAsInt();
		if (status == 200) {
			String modelId = mlResponse.get("name").getAsString();
			autoMlConfig.setStatus(AutMLEnum.Status.LEADERBOARD_READY.name());
			autoMlConfig.setModelId(modelId);
			autoMLConfigDAL.updateMLConfig(autoMlConfig);
			
		}
		
	}
	
	public JsonObject runAutoML(String usecase, String trainingFrame,String predictionColumn, String numOfModels) throws InsightsCustomException {
        
		String httpResponse = h2oApiCommunicator.runAutoML(usecase + "AutoML", trainingFrame, trainingFrame,
				predictionColumn, numOfModels);
		log.info(httpResponse);
		JsonObject response = new JsonParser().parse(httpResponse).getAsJsonObject();
		String pollingURL = response.get("job").getAsJsonObject().get("key").getAsJsonObject().get("URL").getAsString();
		String autoMLName = response.get("job").getAsJsonObject().get("dest").getAsJsonObject().get("name")
				.getAsString();
		int statusCode = h2oApiCommunicator.pollRequestStatus(pollingURL);
		response = new JsonObject();
		response.addProperty("status", statusCode);
		response.addProperty("name", autoMLName);

		return response;
	     
	    }
	
	public JsonObject getHeaders(File file, String usecase) throws InsightsCustomException {
		JsonArray header = null;
		JsonArray contents = null;
		JsonObject response = new JsonObject();
		CSVParser csvParser = null;
		Reader reader = null;
		String originalFilename = file.getName();
		String fileExt = FilenameUtils.getExtension(originalFilename);
		try {
			if (fileExt.equalsIgnoreCase("csv")) {
				CSVFormat format = CSVFormat.newFormat(',').withHeader();
				reader = new FileReader(file);
				csvParser = new CSVParser(reader, format);
				header = getHeader(csvParser);
				contents = getcsvContents(csvParser, header);
				response.add("Header", header);
				response.add("Contents", contents);
				response.addProperty("Usecase", usecase);
				log.debug("TrainModelsUtils ==== Header from input CSV is: {}", header);

			} else {
				throw new InsightsCustomException("Invalid file format.");
			}
		} catch (IOException ex) {
			log.error("TrainModelsUtils === exception while creating csv on server.{} ", ex.getMessage());
			throw new InsightsCustomException("TrainModelsUtils === exception while creating csv on server");
		} catch (ArrayIndexOutOfBoundsException ex) {
			log.error("TrainModelsUtils === error in file. {}", ex.getMessage());
			throw new InsightsCustomException("Error in File Format");
		} catch (InsightsCustomException ex) {
			log.error("TrainModelsUtils ===  error in csv file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		} catch (Exception ex) {
			log.error("TrainModelsUtils ===  error in uploading csv file {} ", ex.getMessage());
			throw new InsightsCustomException(ex.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (csvParser != null) {
					csvParser.close();
				}
			} catch (IOException e) {
				log.error("trainModelsUtils === Exception {}",e.getMessage());
			}
		}
		return response;
	}
	
	private JsonArray getHeader(CSVParser csvParser) {
		Map<String, Integer> headerMap = csvParser.getHeaderMap();
		JsonArray header = new JsonArray();
		headerMap.keySet().forEach(eachKey->header.add(eachKey));		
		return header;
	}

	private JsonArray getcsvContents(CSVParser csvParser, JsonArray header) {
		JsonArray csvContents = new JsonArray();
		try {
			for (CSVRecord record : csvParser.getRecords()) {
				JsonObject csvRow = new JsonObject();
				Map<String, String> row = record.toMap();
				for (JsonElement h : header) {
					csvRow.addProperty(h.getAsString(), row.get(h.getAsString()));
				}
				csvContents.add(csvRow);
			}
			log.debug("Completed parsing for csv contents");
		} catch (IOException e) {
		  log.error("Error:{}", e.getMessage());
		}
		return csvContents;
	}
	
	private String getFormattedData(JsonArray contents, JsonArray config) {
		StringBuilder formattedData = new StringBuilder();
		// Append headers
		for (JsonElement e : config) {
			formattedData.append(e.getAsJsonObject().get("FieldName").getAsString()).append(",");
		}
		formattedData.replace(formattedData.length() - 1, formattedData.length(), "");
		formattedData.append("\n");
		log.debug("Formatted Headers: {}", formattedData);
		// Append data
		for (int i = 0; i < contents.size(); i++) {
			JsonObject jo = contents.get(i).getAsJsonObject();
			for (JsonElement e : config) {
				formattedData.append(jo.get(e.getAsJsonObject().get("FieldName").getAsString())).append(",");
			}
			formattedData.replace(formattedData.length() - 1, formattedData.length(), "");
			formattedData.append("\n");
		}
		log.debug("Completed formatting");
		return String.valueOf(formattedData);
	}
	
	public JsonObject uploadData(JsonArray contents, String usecase, String config) throws InsightsCustomException {
		
		JsonArray configuration = new Gson().fromJson(config, JsonArray.class);
		JsonObject response = new JsonObject();
		String updatedContents = getFormattedData(contents, configuration);
		String res = h2oApiCommunicator.uploadH2o(updatedContents, usecase);
		if (res != null) {
			log.debug("Uploaded parsed data to H2o====== Complete");
			JsonObject payload = new Gson().fromJson(res, JsonObject.class);
			String sourceFrame = payload.get("destination_frame").getAsString();
			if (h2oApiCommunicator.postparsedCSVData(sourceFrame, usecase, getNumberOfColumn(configuration),
					getColumnTypes(configuration), getColumnNames(updatedContents), "044", 1) == 200) {
				response.addProperty("message", "Training data uploaded successfully");
				response.add("columns", getOriginalColumns(configuration));
				response.addProperty("usecase", usecase);
			} else {
				log.error("Unable to parse data");
				throw new InsightsCustomException("Error parsing data: " + usecase);
			}
		} else {
			log.error("Unable to upload data");
			throw new InsightsCustomException("Error uploading data: " + usecase);
		}
		return response;
	}
	
	private int getNumberOfColumn(JsonArray config) {
		int number = config.size();
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			if (elem.get("EnableNLP").getAsBoolean()) {
				number += 100;
				break;
			}
		}
		return number;
	}


	private String getColumnTypes(JsonArray config) {
		StringBuilder sb = new StringBuilder();
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			sb.append(elem.get("DataType").getAsString()).append(",");
		}
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			if (elem.get("EnableNLP").getAsBoolean()) {
				for (int i = 0; i < 100; i++)
					sb.append("Numeric").append(",");
				break;
			}
		}
		sb.replace(sb.length() - 1, sb.length(), "");
		return String.valueOf(sb);
	}
	
	private JsonArray getOriginalColumns(JsonArray config) {
		JsonArray ja = new JsonArray();
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			ja.add(elem.get("FieldName").getAsString());
		}
		return ja;
	}
	
	private String getColumnNames(String content) {
		return content.split("\n")[0];
	}
}
