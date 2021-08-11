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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
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

import au.com.bytecode.opencsv.CSVReader;

public class TrainModelsUtils {
	public static final String ENABLE_NLP ="EnableNLP";
	public static final String FIELDNAME ="FieldName";
	public static final String PARSEDTOKENS="ParsedTokens";
	private static final Logger log = LogManager.getLogger(TrainModelsUtils.class);
	AutoMLConfigDAL autoMLConfigDAL;	
	H2oApiCommunicator h2oApiCommunicator = new H2oApiCommunicator();	

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
				CSVFormat format = CSVFormat.RFC4180.withHeader();
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
			log.debug("Completed parsing for csv contents {}",csvContents.size());
		} catch (IOException e) {
		  log.error("Error:{}", e.getMessage());
		}
		return csvContents;
	}
	
	 

	/**
		 * Perform NLP if configured, upload and parse the training data. Add a record
		 * into Postgres.
		 *
		 * @param contents
		 * @param usecase
		 * @param config
		 * @return
		 * @throws InsightsCustomException
		 */

	public JsonObject uploadData(JsonArray contents, String usecase, String config,String usecaseCSVFilePath) throws InsightsCustomException {
		
		JsonArray configuration = new Gson().fromJson(config, JsonArray.class);
		JsonObject response = new JsonObject();
		JsonObject updatedContents = performNLP(contents, usecase, configuration);
		if (updatedContents.get("NLP").getAsBoolean()) {
			usecaseCSVFilePath = saveUpdatedCSV(updatedContents.get("data").getAsString(), usecase);
		}
		String res = h2oApiCommunicator.importFiles(usecaseCSVFilePath);
		if (res != null) {
			log.debug("AutoML Executor === uploaded parsed data to H2o complete");
			JsonObject payload = new Gson().fromJson(res, JsonObject.class);
			String sourceFrame = payload.get("destination_frames").getAsString();
			if (h2oApiCommunicator.postparsedCSVData(sourceFrame, usecase, getNumberOfColumn(configuration),
					getColumnTypes(configuration), getColumnNames(updatedContents.get("data").getAsString()), "044",
					1) == 200) {
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
	
	/**write back  csv with combined original data + NLP data 
	 * @param data
	 * @param usecaseName
	 * @return
	 */
	private String saveUpdatedCSV(String data, String usecaseName) {

		String path = ConfigOptions.ML_DATA_STORAGE_RESOLVED_PATH + ConfigOptions.FILE_SEPERATOR
				+ usecaseName + ConfigOptions.FILE_SEPERATOR +usecaseName+"W2V.csv" ;
		CSVFormat csvFileFormat = CSVFormat.RFC4180.withHeader();
		try (FileWriter fileWriter = new FileWriter(path);
				CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
				CSVReader reader = new CSVReader(new StringReader(data))) {
				csvFilePrinter.printRecords(reader.readAll());
			
		} catch (Exception e) {
          log.error("Error{}", e.getMessage());
		}
      return path;
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
		  return h2oApiCommunicator.splitFrame(usecase,splitRatio);
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
	  
	 
	  
	  /**Import file to h2o from specified path
	 * @param path
	 * @throws InsightsCustomException
	 */
	public void importFile(String path) throws InsightsCustomException
	  {
		  h2oApiCommunicator.importFiles(path);
	  }
	   

	private int getNumberOfColumn(JsonArray config) {
		int number = config.size();
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			if (elem.get(ENABLE_NLP).getAsBoolean()) {
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
			if (elem.get(ENABLE_NLP).getAsBoolean()) {
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
			ja.add(elem.get(FIELDNAME).getAsString());
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

	private JsonObject performNLP(JsonArray contents, String usecase, JsonArray config) throws InsightsCustomException {
		
		Boolean nlpFlag=false;
		JsonObject response = new JsonObject();
		
		List<String> nlpColumns = new ArrayList<>();		
		for (JsonElement e : config) {
			JsonObject elem = e.getAsJsonObject();
			if (elem.get(ENABLE_NLP).getAsBoolean()) {
				nlpColumns.add(elem.get(FIELDNAME).getAsString());
				nlpFlag=true;
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
				if (h2oApiCommunicator.postparsedCSVData(sourceFrame, usecase + PARSEDTOKENS, 1, "String",
						usecase + PARSEDTOKENS, "000", 0) == 200) {
					log.debug("Parsing Success");
					if (h2oApiCommunicator.trainWord2Vec(usecase + "ParsedTokens.hex", usecase + "W2V") ==200) {
						log.debug("Word2VecModel Trained successfully!");
						vectors =h2oApiCommunicator.transformWord2Vec(usecase + PARSEDTOKENS, usecase + "W2V",
								contents.size());
					}
				}
			}
			
		}
		 
			String data=getFormattedData(contents, vectors, config);
			response.addProperty("NLP", nlpFlag);
			response.addProperty("data", data);
			return response;
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
			formattedData.append(e.getAsJsonObject().get(FIELDNAME).getAsString()).append(",");
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
				formattedData.append(jo.get(e.getAsJsonObject().get(FIELDNAME).getAsString().replace(",", " "))).append(",");
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
	
}
