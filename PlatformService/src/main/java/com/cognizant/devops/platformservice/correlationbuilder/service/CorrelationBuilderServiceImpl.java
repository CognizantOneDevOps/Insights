/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.correlationbuilder.service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfigDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

@Service("correlationBuilderService")
public class CorrelationBuilderServiceImpl implements CorrelationBuilderService {
	private static Logger log = LogManager.getLogger(CorrelationBuilderServiceImpl.class);

	@Override
	public List<CorrelationConfiguration> getAllCorrelations() throws InsightsCustomException {
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		List<CorrelationConfiguration> correlationList = new ArrayList<>();
		try {
			correlationList = correlationConfigDAL.getAllCorrelations();
		} catch (Exception e) {
			log.error("Error getting all relationShips", e.getMessage());
			throw new InsightsCustomException(e.getMessage());
		}
		return correlationList;
	}

	@Override
	public Boolean saveConfig(String config) throws InsightsCustomException {

		CorrelationJson correlation = loadCorrelation(config);
		CorrelationConfiguration correlationConfig = new CorrelationConfiguration();
		correlationConfig.setSourceToolName(correlation.getSource().getToolName());
		correlationConfig.setSourceToolCategory(correlation.getSource().getToolCategory());
		if (null == correlation.getSource().getLabelName()) {
			correlationConfig.setSourceLabelName(correlation.getSource().getToolName());
		} else {
			correlationConfig.setSourceLabelName(correlation.getSource().getLabelName());
		}
		correlationConfig.setSourceFields(String.join(",", correlation.getSource().getFields()));
		correlationConfig.setDestinationToolName(correlation.getDestination().getToolName());
		correlationConfig.setDestinationToolCategory(correlation.getDestination().getToolCategory());
		correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
		if (null == correlation.getDestination().getLabelName()) {
			correlationConfig.setDestinationLabelName(correlation.getDestination().getToolName());
		} else {
			correlationConfig.setDestinationLabelName(correlation.getDestination().getLabelName());
		}
		correlationConfig.setDestinationFields(String.join(",", correlation.getDestination().getFields()));
		correlationConfig.setRelationName(correlation.getRelationName());
		if (correlation.getPropertyList().length > 0) {
			correlationConfig.setPropertyList(String.join(",", correlation.getPropertyList()));
		}
		correlationConfig.setEnableCorrelation(correlation.isEnableCorrelation());
		correlationConfig.setSelfRelation(correlation.isSelfRelation());

		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		return correlationConfigDAL.saveCorrelationConfig(correlationConfig);

	}

	@Override
	public Boolean updateCorrelationStatus(String flagDeatils) throws InsightsCustomException {

		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(flagDeatils);
		String relationName = json.get("relationName").getAsString();
		Boolean flag = json.get("correlationFlag").getAsBoolean();
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		return correlationConfigDAL.updateCorrelationConfig(relationName, flag);

	}

	@Override
	public Boolean deleteCorrelation(String relationName) throws InsightsCustomException {

		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(relationName);
		String relationNameValue = json.get("relationName").getAsString();
		CorrelationConfigDAL correlationConfigDAL = new CorrelationConfigDAL();
		return correlationConfigDAL.deleteCorrelationConfig(relationNameValue);

	}

	private CorrelationJson loadCorrelation(String config) {
		JsonParser parser = new JsonParser();
		JsonObject json = (JsonObject) parser.parse(config);
		CorrelationJson correlation = new Gson().fromJson(json, CorrelationJson.class);
		return correlation;
	}

}