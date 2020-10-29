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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.dal.ai.H2oApiCommunicator;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.autoML.AutoMLConfigDAL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TrainModelsUtils {
	private static final Logger log = LogManager.getLogger(TrainModelsUtils.class);
	String FILE_SEPERATOR = File.separator;
	AutoMLConfigDAL autoMLConfigDAL;
	File MLDirectory = new File(ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH);
	H2oApiCommunicator h2oApiCommunicator = new H2oApiCommunicator();

	public TrainModelsUtils() {
		/*
		 * autoMLConfigDAL = new AutoMLConfigDAL(); if (!MLDirectory.exists()) {
		 * MLDirectory.mkdir(); }
		 */
	}

	/**
	 * Validate if usecase name is unique from postgres
	 *
	 * @param usecase
	 * @return
	 * @throws InsightsCustomException
	 */

	public JsonObject validateUsecaseName(String usecase) throws InsightsCustomException {
		try {
			log.debug("Validating usecase: " + usecase);
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
	 * Method performs multipart file to File conversion and does CSV file checks
	 * and calls getHeader()
	 *
	 * @param file
	 * @param usecase
	 * @return
	 * @throws InsightsCustomException
	 */

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

	/**
	 * Get the header row from input csv
	 *
	 * @param csvParser
	 * @return
	 */

	private JsonArray getHeader(CSVParser csvParser) {
		Map<String, Integer> headerMap = csvParser.getHeaderMap();
		JsonArray header = new JsonArray();
		headerMap.keySet().forEach(eachKey->header.add(eachKey));		
		return header;
	}

	/**
	 * Parse the input csv into JsonArray
	 *
	 * @param csvParser
	 * @param header
	 * @return
	 */

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
			log.debug("Completed parsing for csv contents"+csvContents.size());
		} catch (IOException e) {
		  log.error("Error:{}", e.getMessage());
		}
		return csvContents;
	}
	
	 

	/*
	*//**
		 * Perform NLP if configured, upload and parse the training data. Add a record
		 * into Postgres.
		 *
		 * @param contents
		 * @param usecase
		 * @param config
		 * @return
		 * @throws InsightsCustomException
		 */

	public JsonObject uploadData(JsonArray contents, String usecase, String config) throws InsightsCustomException {
		
		JsonArray configuration = new Gson().fromJson(config, JsonArray.class);
		JsonObject response = new JsonObject();
		String updatedContents = performNLP(contents, usecase, configuration);
		String res = h2oApiCommunicator.uploadH2o(updatedContents, usecase);
		if (res != null) {
			log.debug("AutoML Executor === uploaded parsed data to H2o complete");
			JsonObject payload = new Gson().fromJson(res, JsonObject.class);
			String sourceFrame = payload.get("destination_frame").getAsString();
			if (h2oApiCommunicator.postparsedCSVData(sourceFrame, usecase, getNumberOfColumn(configuration),
					getColumnTypes(configuration), getColumnNames(updatedContents), "44", 1) == 200) {
				response.addProperty("message", "Training data uploaded successfully ");
				response.add("columns", getOriginalColumns(configuration));
				response.addProperty("usecase", usecase);
			} else {
				log.error("AutoML Executor === unable to parse data");
				throw new InsightsCustomException("Error parsing data: " + usecase);
			}
		} else {
			log.error("Unable to upload data");
			throw new InsightsCustomException("Error uploading data: " + usecase);
		}
		return response;
	} 

	
	

	/**
	 * Split the uploaded training data into part0 and part1
	 *
	 * @param usecase
	 * @param splitRatio
	 * @return
	 * @throws InsightsCustomException
	 */
	
	  public String splitData(String usecase, double splitRatio) throws InsightsCustomException 
	  { 
		  return h2oApiCommunicator.splitFrameNew(usecase,splitRatio);
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
	  
	  
	  public JsonObject getLeaderBoard(String modelId) throws InsightsCustomException {	       
	      
		JsonObject leaderboard = h2oApiCommunicator.getLeaderBoard(modelId);
		if (leaderboard == null) {
			log.error("Error getting leaderboard:{} " , modelId);
			throw new InsightsCustomException("Error getting leaderboard: " + modelId);
		}

		return leaderboard;
	    }
	  
	  
	  public void importFile(String path) throws InsightsCustomException
	  {
		  h2oApiCommunicator.importFiles(path);
	  }
	  
	  public JsonObject getPrediction(String modelName,String usecaseName) throws InsightsCustomException {
	        JsonObject prediction = h2oApiCommunicator.predict(modelName, usecaseName+"_part1");
	        JsonArray data = h2oApiCommunicator.getDataFrame(usecaseName+"_part1");
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
	  
	    /*
		 * Start the AutoML build and update the PredictionColumn in Postgres. Returns a
		 * Url to check the progress.
		 *
		 * @param usecase
		 * @param predictionColumn
		 * @return
		 * @throws InsightsCustomException
		 */
	/*
	 * @Override public JsonObject runAutoML(String usecase, String
	 * predictionColumn, String numOfModels) throws InsightsCustomException {
	 * JsonObject response = new JsonObject(); if
	 * (autoMLConfigDAL.createOrUpdate(usecase, null, predictionColumn, null)) {
	 * String url = h2oApiCommunicator.runAutoML(usecase + "AutoML", usecase +
	 * "ParsedTrainingData_part0", usecase + "ParsedTrainingData_part1",
	 * predictionColumn, numOfModels); if (url != null) { //
	 * response.addProperty("ResponseCode", 200); response.addProperty("PollingUrl",
	 * url); response.addProperty("Usecase", usecase); return response; } else {
	 * log.error("Error running AutoML"); throw new
	 * InsightsCustomException("Error running AutoML"); } } else {
	 * log.error("Error updating Postgres"); throw new
	 * InsightsCustomException("Error updating Postgres"); } }
	 * 
	 *//**
		 * Fetch the AutoML build status and progress%
		 *
		 * @param usecase
		 * @param url
		 * @return
		 * @throws InsightsCustomException
		 */
	/*
	 * @Override public JsonObject getAutoMLProgress(String usecase, String url)
	 * throws InsightsCustomException { JsonObject response =
	 * h2oApiCommunicator.getAutoMLProgress(url); response.addProperty("Usecase",
	 * usecase); return response; }
	 * 
	 *//**
		 * Fetch the AutoML leaderboard after completion (Status:DONE) of the build
		 *
		 * @param usecase
		 * @return
		 * @throws InsightsCustomException
		 */
	/*
	 * @Override public JsonObject getLeaderBoard(String usecase) throws
	 * InsightsCustomException { String predictionColumn =
	 * autoMLConfigDAL.getPredictionColumn(usecase); JsonObject response = new
	 * JsonObject(); JsonObject leaderboard =
	 * h2oApiCommunicator.getLeaderBoard(usecase + "AutoML", predictionColumn); if
	 * (leaderboard != null) { //response.addProperty("ResponseCode", 200);
	 * response.addProperty("Usecase", usecase); response.add("Leaderboard",
	 * leaderboard); return response; } else {
	 * log.error("Error getting leaderboard: " + usecase); throw new
	 * InsightsCustomException("Error getting leaderboard: " + usecase); } }
	 * 
	 *//**
		 * Perform Prediction on part1 of split training data using the model selected.
		 * Returns actual data and prediction value.
		 *
		 * @param usecase
		 * @param modelName
		 * @return
		 * @throws InsightsCustomException
		 */
	/*
	 * @Override public JsonObject getPrediction(String usecase, String modelName)
	 * throws InsightsCustomException { JsonObject prediction =
	 * h2oApiCommunicator.predict(modelName, usecase + "ParsedTrainingData_part1");
	 * JsonArray data = h2oApiCommunicator.getDataFrame(usecase +
	 * "ParsedTrainingData_part1"); //System.out.println("======DATA=====\n" +
	 * data); //System.out.println("====Prediction==========\n" + prediction);
	 * JsonObject response = new JsonObject(); JsonArray fields = new JsonArray();
	 * Set<Map.Entry<String, JsonElement>> es =
	 * data.get(0).getAsJsonObject().entrySet(); for (Iterator<Map.Entry<String,
	 * JsonElement>> it = es.iterator(); it.hasNext(); ) {
	 * fields.add(it.next().getKey()); } if (data.size() ==
	 * prediction.getAsJsonArray("predict").size()) { //fields.add("predict"); for
	 * (int i = 0; i < data.size(); i++) {
	 * //data.get(i).getAsJsonObject().addProperty("Prediction",
	 * prediction.get(i).getAsString()); for (Map.Entry<String, JsonElement> entry :
	 * prediction.entrySet()) { String key = entry.getKey(); if (i == 0)
	 * fields.add(key); String val =
	 * entry.getValue().getAsJsonArray().get(i).getAsString(); int max =
	 * val.length() < 5 ? val.length() : 5;
	 * data.get(i).getAsJsonObject().addProperty(key, val.substring(0, max));
	 * //data.get(i).getAsJsonObject().addProperty(key,
	 * entry.getValue().getAsJsonArray().get(i).getAsString().substring(0, 5)); } }
	 * //response.addProperty("ResponseCode", 200); response.add("Fields", fields);
	 * //response.add("Fields", fields); response.add("Data", data); return
	 * response; } else { log.error("Mismatch in test data and prediction: " +
	 * usecase); throw new
	 * InsightsCustomException("Mismatch in test data and prediction: " + usecase);
	 * } }
	 * 
	 *//**
		 * Download the selected model and store the zip in file directory. Update the
		 * selected modelname in Postgres
		 *
		 * @param usecase
		 * @param modelName
		 * @return
		 * @throws InsightsCustomException
		 */
	/*
	 * @Override public JsonObject downloadMojo(String usecase, String modelName)
	 * throws InsightsCustomException { JsonObject response = new JsonObject(); try
	 * { int code = h2oApiCommunicator.downloadMojo(usecase, modelName); if (code ==
	 * 200) { autoMLConfigDAL.createOrUpdate(usecase, null, null, modelName);
	 * //response.addProperty("ResponseCode", code); response.addProperty("Message",
	 * "Successfully saved Mojo"); return response; } else {
	 * log.error("Error downloading Mojo: " + usecase); throw new
	 * InsightsCustomException("Error downloading Mojo: " + usecase); } } catch
	 * (InsightsCustomException e) {
	 * log.error("Error downloading Mojo/ Updating MOJO details in Postgres: " +
	 * usecase); throw new
	 * InsightsCustomException("Error downloading Mojo/ Updating MOJO details in Postgres: "
	 * + usecase); } }
	 * 
	 *//**
		 * Delete the usecase directory from file directory and entry from Postgres.
		 *
		 * @param usecase
		 * @return
		 * @throws InsightsCustomException
		 */
	/*
	 * @Override public JsonObject deleteUsecase(String usecase) throws
	 * InsightsCustomException { JsonObject response = new JsonObject();
	 * //response.addProperty("ResponseCode", 200); response.addProperty("Message",
	 * "Successfully deleted"); if (!autoMLConfigDAL.deleteUsecase(usecase)) {
	 * //response.addProperty("ResponseCode", 300);
	 * response.addProperty("Database Error", "Unable to delete from Database");
	 * log.error("Unable to delete from Database: " + usecase); throw new
	 * InsightsCustomException("Unable to delete from Database: " + usecase); } File
	 * directory = new File(ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH +
	 * FILE_SEPERATOR + usecase); if (!FileSystemUtils.deleteRecursively(directory))
	 * { //response.addProperty("ResponseCode", 300);
	 * response.addProperty("File storage Error",
	 * "Unable to delete contents from File Storage");
	 * log.error("Unable to delete contents from File Storage: " + usecase); throw
	 * new InsightsCustomException("Unable to delete contents from File Storage: " +
	 * usecase); } return response; }
	 * 
	 *//**
		 * Fetch the listof usecases from Postgres.
		 *
		 * @return
		 */

	/*
	 * @Override public JsonObject getUsecases() throws InsightsCustomException {
	 * JsonObject response = new JsonObject(); JsonArray responseArray = new
	 * JsonArray(); try { List<AutoMLConfig> results =
	 * autoMLConfigDAL.fetchUsecases(); if (!results.isEmpty()) {
	 * results.forEach(eachMLConfig->{ JsonObject eachObject = new JsonObject();
	 * eachObject.addProperty("", value); responseArray.add(element); });
	 * response.add("Usecases", new Gson().toJsonTree(results)); } else {
	 * response.addProperty("Message", "No usecases found"); } return response; }
	 * catch (Exception e) { throw new InsightsCustomException(e.getMessage()); } }
	 * 
	 *//**
		 * Get the number of columns from configurtion. Add 100 columns if NLP is
		 * enabled for any 1 column.
		 *
		 * @param config
		 * @return
		 */

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

	/**
	 * Get the column data types from configuration and add type "Numeric" if NLP is
	 * enabled.
	 *
	 * @param config
	 * @return
	 */

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

	/**
	 * Get the column names from configuration.
	 *
	 * @param config
	 * @return
	 */

	private JsonArray getOriginalColumns(JsonArray config) {
		JsonArray ja = new JsonArray();
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			ja.add(elem.get("FieldName").getAsString());
		}
		return ja;
	}

	/**
	 * Get the column names from the content. This could include NLP vector columns
	 * if enabled.
	 *
	 * @param content
	 * @return
	 */

	private String getColumnNames(String content) {
		return content.split("\n")[0];
	}

	/**
	 * Fetch the columns to perform NLP on, combine into a single column, upload to
	 * H2o, train Word2Vec algorithm and transform the same column. Combine the
	 * vectors with the input data frame.
	 *
	 * @param contents
	 * @param usecase
	 * @param config
	 * @return
	 * @throws InsightsCustomException
	 */

	private String performNLP(JsonArray contents, String usecase, JsonArray config) throws InsightsCustomException {
		List<String> nlpColumns = new ArrayList<>();
		

		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			if (elem.get("EnableNLP").getAsBoolean()) {
				nlpColumns.add(elem.get("FieldName").getAsString());
			}
		}
		JsonArray vectors = new JsonArray();
		if (!nlpColumns.isEmpty()) {
			List<String> nlpInputData = new ArrayList<>();
			for (JsonElement e : contents) {
				StringBuilder sb = new StringBuilder();
				for (String column : nlpColumns) {
					sb.append(e.getAsJsonObject().get(column).getAsString()).append(" ");
				}
				nlpInputData.add(sb.toString());
			}
			String tokenizedNlpData = tokenize(nlpInputData);
			log.debug("AutoML Executor === Completed tokenization: {}", tokenizedNlpData);
			
			String res=h2oApiCommunicator.uploadH2o(tokenizedNlpData, usecase + "Tokens");
			if (res != null) {
				log.debug("AutoML Executor === upload parsed data to H2o Complete");
				JsonObject payload = new Gson().fromJson(res, JsonObject.class);
				String sourceFrame = payload.get("destination_frame").getAsString();
				log.debug("Upload Success");
				if (h2oApiCommunicator.postparsedCSVData(sourceFrame, usecase + "ParsedTokens", 1, "String",
						usecase + "ParsedTokens", "000", 0) == 200) {
					log.debug("Parsing Success");
					if (h2oApiCommunicator.trainWord2Vec(usecase + "ParsedTokens", usecase + "W2V") ==200) {
						log.debug("Word2VecModel Trained successfully!");
						vectors =h2oApiCommunicator.transformWord2Vec(usecase + "ParsedTokens", usecase + "W2V",
								contents.size());
					}
				}
			}
			
		}
		return getFormattedData(contents, vectors, config);
	}

	/**
	 * Combine the vectors with the original training data.
	 *
	 * @param contents
	 * @param vectors
	 * @param config
	 * @return
	 */

	private String getFormattedData(JsonArray contents, JsonArray vectors, JsonArray config) {
		StringBuilder formattedData = new StringBuilder();
		// Append headers
		for (JsonElement e : config) {
			formattedData.append(e.getAsJsonObject().get("FieldName").getAsString()).append(",");
		}
		for (JsonElement e : vectors) {
			JsonObject vector = e.getAsJsonObject();
			formattedData.append(vector.get("label").getAsString()).append(",");
		}
		formattedData.replace(formattedData.length() - 1, formattedData.length(), "");
		formattedData.append("\n");
		log.debug("AutoML Executor === Formatted Headers: {}", formattedData);
		// Append data
		for (int i = 0; i < contents.size(); i++) {
			JsonObject jo = contents.get(i).getAsJsonObject();
			for (JsonElement e : config) {				
				formattedData.append(jo.get(e.getAsJsonObject().get("FieldName").getAsString().replace(",", " "))).append(",");
			}
			for (JsonElement e : vectors) {
				JsonObject vector = e.getAsJsonObject();
				JsonArray vec = vector.get("data").getAsJsonArray();
				formattedData.append(vec.get(0).getAsString()).append(",");
			}
			formattedData.replace(formattedData.length() - 1, formattedData.length(), "");
			formattedData.append("\n");
		}
		log.debug("AutoML Executor === Completed formatting");
		return String.valueOf(formattedData);
	}

	/**
	 * Split input into words, remove stop-words and return the tokens
	 *
	 * @param input
	 * @return
	 */

	private String tokenize(List<String> input) {
		StringBuilder output = new StringBuilder();
		try {
			InputStream stopwordsStream = getClass().getClassLoader().getResourceAsStream("english-stopwords.txt");
			List<String> stopwords = new ArrayList<>();
			BufferedReader r = new BufferedReader(new InputStreamReader(stopwordsStream));
			String line;
			while ((line = r.readLine()) != null) {
				stopwords.add(line);
			}
			for (String s : input) {
				StringTokenizer tokenizer = new StringTokenizer(s, " \t\n\r\f,.:;?![]'");
				StringBuilder sb = new StringBuilder();
				while (tokenizer.hasMoreTokens()) {
					String word = tokenizer.nextToken().toLowerCase();
					if (stopwords.contains(word) || word.length() < 2) {
						continue;
					}
					sb.append("\"").append(word.replaceAll("[^a-zA-Z]", "")).append("\"").append("\n");
				}
				output.append(String.valueOf(sb)).append("\n");
			}
		} catch (IOException e) {
			log.error("AutoML Executor === something went wrong while to");
		}
		return String.valueOf(output);
	}

	/**
	 * Save the contents of csv file into directory
	 *
	 * @param contents
	 * @param usecase
	 */
	/*
	 * private void saveContents(JsonArray contents, String usecase) { try { File
	 * directory = new File(MLDirectory + FILE_SEPERATOR + usecase); if
	 * (!directory.exists()) { directory.mkdir(); } BufferedWriter writer = new
	 * BufferedWriter(new FileWriter(ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH +
	 * FILE_SEPERATOR + usecase + FILE_SEPERATOR + "Input.csv"));
	 * writer.write(String.valueOf(contents)); writer.close(); } catch (IOException
	 * e) { e.printStackTrace(); } }
	 * 
	 *//**
		 * Save the updated contents into file directory
		 *
		 * @param content
		 * @param usecase
		 */
	/*
	 * private void saveParsedContents(String content, String usecase) { try { File
	 * directory = new File(MLDirectory + FILE_SEPERATOR + usecase); if
	 * (!directory.exists()) { directory.mkdir(); } BufferedWriter writer = new
	 * BufferedWriter(new FileWriter(ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH +
	 * FILE_SEPERATOR + usecase + FILE_SEPERATOR + "Parsed.csv"));
	 * writer.write(String.valueOf(content)); writer.close(); } catch (IOException
	 * e) { e.printStackTrace(); } }
	 * 
	 *//**
		 * convert multipart file to file
		 *
		 * @param multipartFile
		 * @return File
		 * @throws IOException
		 *//*
			 * private File convertToFile(MultipartFile multipartFile) throws IOException {
			 * File file = new File(multipartFile.getOriginalFilename()); try
			 * (FileOutputStream fos = new FileOutputStream(file)) {
			 * fos.write(multipartFile.getBytes()); } return file; }
			 * 
			 * (non-Javadoc)
			 * 
			 * @see com.cognizant.devops.platformservice.autoML.service.ITrainModels#
			 * saveAutoMLConfig(org.springframework.web.multipart.MultipartFile,
			 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
			 * java.lang.String)
			 * 
			 * @Override public int saveAutoMLConfig(MultipartFile file, String usecase,
			 * String configuration, String splitRatio, String predictionColumn, String
			 * numOfModels ,String taskDetails) throws InsightsCustomException {
			 * 
			 * int id = -1; boolean isExists = autoMLConfigDAL.isUsecaseExisting(usecase);
			 * if (isExists) { log.
			 * error("AutoMLSerive======= unable to save record as usecase {} already exists"
			 * , usecase); throw new InsightsCustomException("usecase already exists " +
			 * usecase); } else {
			 * 
			 * Save file to Insights_Home
			 * 
			 * String folderPath = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH +
			 * ConfigOptions.FILE_SEPERATOR + usecase + ConfigOptions.FILE_SEPERATOR; File
			 * destfolder = new File(folderPath); if (destfolder.mkdirs()) {
			 * log.debug("AutoML========== Usecasefolder is created {}",
			 * destfolder.getAbsolutePath()); } String filePath = folderPath +
			 * file.getOriginalFilename(); destfolder = new File(filePath); try {
			 * file.transferTo(destfolder); } catch (Exception e) {
			 * log.debug("AutoML========== Exception while creating folder {}",
			 * e.getMessage()); throw new
			 * InsightsCustomException("Unable to create folder"); }
			 * 
			 * 
			 * Save record in DB and create workflow
			 * 
			 * JsonParser parser = new JsonParser(); JsonArray taskList =
			 * parser.parse(taskDetails).getAsJsonArray(); boolean runImmediate = true;
			 * boolean reoccurence = false; String schedule =
			 * WorkflowTaskEnum.WorkflowSchedule.ONETIME.name(); boolean isActive = true;
			 * String workflowStatus = WorkflowTaskEnum.WorkflowStatus.NOT_STARTED.name();
			 * String workflowType = "AUTOML"; String workflowId =
			 * WorkflowTaskEnum.WorkflowType.AUTOML.getValue() + "_" +
			 * InsightsUtils.getCurrentTimeInSeconds(); InsightsWorkflowConfiguration
			 * workflowConfig = workflowService.saveWorkflowConfig(workflowId, isActive,
			 * reoccurence, schedule, workflowStatus, workflowType, taskList, 0, null,
			 * runImmediate); AutoMLConfig mlConfig = new AutoMLConfig();
			 * mlConfig.setUseCaseName(usecase); mlConfig.setConfigJson(configuration);
			 * mlConfig.setIsActive(true); mlConfig.setPredictionColumn(predictionColumn);
			 * mlConfig.setNumOfModels(numOfModels); mlConfig.setSplitRatio(splitRatio);
			 * mlConfig.setMojoDeployed(null);
			 * mlConfig.setCreatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			 * mlConfig.setUseCaseFilePath(file.getOriginalFilename());
			 * mlConfig.setWorkflowConfig(workflowConfig); id =
			 * autoMLConfigDAL.saveMLConfig(mlConfig); log.debug(id);
			 * 
			 * } return id; }
			 */
}
