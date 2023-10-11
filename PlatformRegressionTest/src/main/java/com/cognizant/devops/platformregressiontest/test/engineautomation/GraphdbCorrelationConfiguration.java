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
package com.cognizant.devops.platformregressiontest.test.engineautomation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformregressiontest.test.common.LoginAndSelectModule;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GraphdbCorrelationConfiguration extends LoginAndSelectModule {

	private static final Logger log = LogManager.getLogger(GraphdbCorrelationConfiguration.class);

	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();

	BaseDAL baseDAL = new BaseDAL();

	public GraphdbCorrelationConfiguration() {
		String timer;
		try {
			timer = new String(Files.readAllBytes(Paths.get(new File(LoginAndSelectModule.testData.get("dataenrichmentmodule")).getCanonicalPath())),
					StandardCharsets.UTF_8);
			Binding binding = new Binding();
			GroovyShell shell = new GroovyShell(binding);
			shell.evaluate(timer);
		} catch (IOException e) {
			log.debug("Exception occured while loading timer ");
		}
	}

	public boolean testAgentLabelNode() {
		if (checkQuery("MATCH(n:DUMMYDATA) RETURN COUNT(n) as Total")
				&& checkQuery("MATCH(n:JENKINS) RETURN COUNT(n) as Total")
				&& checkQuery("MATCH(n:NEXUS) RETURN COUNT(n) as Total")) {
			return true;
		}
		return false;
	}

	public boolean testCorrelation() {
		String query = "MATCH p=(a:GIT)-[r:TEST_FROM_GIT_TO_JENKINS]->(b:JENKINS) where exists(a.commitId) and exists(b.scmcommitId) return count(p)";
		return checkQuery(query);
	}

	public boolean testDataEnrichment() {
		String query = "MATCH p=(epic:JIRA:DATA)-[r:EPIC_HAS_ISSUES] ->(issues:JIRA:DATA) where exists (issues.epicKey) return count(p)";
		return checkQuery(query);
	}

	public boolean testTraceability() {
		if (checkQuery("MATCH (n:JIRA) WHERE n.epicKey='PS-5' RETURN count(n.epicKey)")
				&& checkQuery("MATCH(n:GIT) Where n.jiraKey='PS-12' RETURN count(n.jiraKey)")) {
			return true;
		}
		return false;
	}

	public List<String> testCorrelationProperties() {
		String query = "MATCH(n:GIT) UNWIND keys(n) AS keys RETURN DISTINCT keys";
		return checkQueryData(query);
	}

	public List<String> testAgentNameInDatabase() {
		String query = "MATCH (n) WHERE EXISTS(n.agentId) RETURN DISTINCT n.agentId";
		return checkQueryData(query);
	}

	private boolean checkQuery(String query) {
		GraphResponse neo4jResponse;
		try(GraphDBHandler dbHandler = new GraphDBHandler()) {
			neo4jResponse = dbHandler.executeCypherQuery(query);
			String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
					.get("data").getAsJsonArray().get(0).getAsJsonObject().get("row").toString().replace("[", "")
					.replace("]", "");

			log.debug("finalJson  {} ", finalJson);

			if (Integer.parseInt(finalJson) > 0) {
				return true;
			}

		} catch (InsightsCustomException | AssertionError e) {

			log.error("InsightsCustomException : or AssertionError {}", e);
		} catch (Exception ex) {
			log.error("Exception {}", ex);
		}
		return false;
	}

	private List<String> checkQueryData(String query) {
		GraphResponse neo4jResponse;
		List<String> data = new ArrayList<>();
		try(GraphDBHandler dbHandler = new GraphDBHandler()) {
			int i = 0;
			neo4jResponse = dbHandler.executeCypherQuery(query);
			log.debug("neo4j Response  {} ", neo4jResponse.getJson());
			int length = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
					.getAsJsonArray().size();
			while (i < length) {
				String finalJson = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
						.get("data").getAsJsonArray().get(i).getAsJsonObject().get("row").toString().replace("[", "")
						.replace("]", "");
				finalJson = finalJson.replace("\"", "");
				data.add(finalJson);
				i++;
			}

		} catch (InsightsCustomException | AssertionError e) {

			log.error("InsightsCustomException : or AssertionError {}", e);
		} catch (Exception ex) {
			log.error("Exception {}", ex);
		}
		return data;
	}

	public boolean testAgentIdInDatabase(String agentId) throws InsightsCustomException {
		List<AgentConfig> registeredAgents = getRegisteredAgents();
		log.info("agent size {}", registeredAgents.size());
		for (AgentConfig agentConfig : registeredAgents) {
			if ((agentConfig.getAgentKey()).contains(agentId))
				return true;
			else
				continue;
		}
		return false;
	}

	public boolean testRegisterAgentInDatabase() throws InsightsCustomException {
		List<AgentConfig> registeredAgents = getRegisteredAgents();
		log.info("agent size {}", registeredAgents.size());
		for (AgentConfig agentConfig : registeredAgents) {
			log.info("Tool name: {}", agentConfig.getToolName());
		}
		if (!registeredAgents.isEmpty()) {
			return true;
		}
		return false;
	}

	public List<AgentConfig> getRegisteredAgents() throws InsightsCustomException {
		List<AgentConfig> agentConfigList;
		try {
			agentConfigList = agentConfigDAL.getAllDataAgentConfigurations();
		} catch (Exception e) {
			log.error("Error getting all agent config ", e);
			throw new InsightsCustomException(e.toString());
		}
		return agentConfigList;
	}
}
