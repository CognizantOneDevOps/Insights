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
package com.cognizant.devops.platformservice.rest.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformdal.mapping.projects.ProjectMapping;
import com.cognizant.devops.platformdal.mapping.projects.ProjectMappingDAL;
import com.cognizant.devops.platformservice.rest.neo4j.GraphDBService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/admin/data")
public class InSightDataService {
	static Logger log = LogManager.getLogger(GraphDBService.class.getName());

	@Autowired
	private HttpServletRequest request;

	@RequestMapping(value = "/addProjectMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject addProjectMapping(@RequestParam int orgId, @RequestParam int rowId, @RequestParam String category,
			@RequestParam String toolName, @RequestParam String fieldName, @RequestParam String fieldValue,
			@RequestParam String projectName, @RequestParam String projectId, @RequestParam String businessUnit,
			@RequestParam String hierarchyName) {

		String[] hierarchyNameArray = hierarchyName.split("_");
		StringBuffer hierarchyCypherQuery = new StringBuffer();
		for(String name : hierarchyNameArray){
			hierarchyCypherQuery.append(":").append(name);
		}
		StringBuffer cypherQuery = new StringBuffer();
		cypherQuery.append("MATCH (n:RAW").append(":").append(category).append(":").append(toolName).append("{")
				.append(fieldName).append(": '").append(fieldValue).append("'})").append(" remove n:RAW set n")
				.append(hierarchyCypherQuery).append(" return count(n)");

		ProjectMapping projectMapping = new ProjectMapping();
		projectMapping.setOrgId(orgId);
		projectMapping.setRowId(rowId);
		projectMapping.setCategory(category);
		projectMapping.setToolName(toolName);
		projectMapping.setFieldName(fieldName);
		projectMapping.setFieldValue(fieldValue);
		projectMapping.setProjectName(projectName);
		projectMapping.setProjectId(projectId);
		projectMapping.setBusinessUnit(businessUnit);
		projectMapping.setHierarchyName(hierarchyName);
		projectMapping.setCypherQuery(cypherQuery.toString());
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		boolean status = projectMappingDAL.addProjectMapping(projectMapping);
		if (status) {
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to add Project Mapping for the request");
		}
	}

	@RequestMapping(value = "/removeProjectMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject removeProjectMapping(@RequestParam int orgId) {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		return PlatformServiceUtil.buildSuccessResponseWithData(projectMappingDAL.removeProjectMapping(orgId));
	}

	@RequestMapping(value = "/fetchProjectMappingByOrgId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchPrjtMappingByOrgId(@RequestParam int orgId) {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		List<ProjectMapping> results = projectMappingDAL.fetchProjectMappingByOrgId(orgId);
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}
	
	@RequestMapping(value = "/fetchProjectMappingByHierarchy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchProjectMappingByHierarchy(@RequestParam String hierarchyName) {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		List<ProjectMapping> results = projectMappingDAL.fetchProjectMappingByHierarchy(hierarchyName);
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	@RequestMapping(value = "/fetchAllProjectMapping", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject fetchAllPrjtMapping() {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		List<ProjectMapping> results = projectMappingDAL.fetchAllProjectMapping();
		return PlatformServiceUtil.buildSuccessResponseWithData(results);
	}

	@RequestMapping(value = "/deleteToolMapping", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject deleteToolMapping(@RequestParam int orgId, @RequestParam String category,
			@RequestParam String toolName, @RequestParam int rowId) {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		return PlatformServiceUtil.buildSuccessResponseWithData(
				projectMappingDAL.deleteToolCOnfiguration(orgId, toolName, category, rowId));
	}

	@RequestMapping(value = "/db/graph", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject addProjectMapping(@RequestParam String cypher) {
		if (cypher == null || !cypher.contains(":PROJECT_TEMPLATE")) {
			return PlatformServiceUtil.buildFailureResponse("Project template is not specified");
		}
		Object responseHeadersAttr = request.getAttribute("responseHeaders");
		List<String> projectLabels = new ArrayList<String>();
		if (responseHeadersAttr != null) {
			Map<String, String> grafanaResponseCookies = (Map<String, String>) responseHeadersAttr;
			String grafanaOrg = grafanaResponseCookies.get("grafanaOrg");
			// String grafanaUser = grafanaResponseCookies.get("grafana_user");
			ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
			List<ProjectMapping> projectMappings = projectMappingDAL
					.fetchProjectMappingByOrgId(Integer.valueOf(grafanaOrg));
			for (ProjectMapping projectMapping : projectMappings) {
				projectLabels.add(projectMapping.getProjectName());
			}
		}
		if (projectLabels.size() == 0) {
			return PlatformServiceUtil.buildFailureResponse("User is not onboarded to any project");
		}
		StringBuffer query = new StringBuffer();
		boolean isFirstDone = false;
		for (String projectLabel : projectLabels) {
			query.append("UNION ");
			query.append(cypher.replace("PROJECT_TEMPLATE", projectLabel));
		}
		return null;
	}
}
