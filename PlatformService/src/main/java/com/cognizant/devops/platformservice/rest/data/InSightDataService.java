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
import java.util.HashMap;
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

	@RequestMapping(value = "/db/graph", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public JsonObject addProjectMapping(@RequestParam String cypher) {
		if (cypher == null || !cypher.contains(":PROJECT_TEMPLATE")) {
			return PlatformServiceUtil.buildFailureResponse("Project template is not specified");
		}
		Map<String, String> grafanaResponseCookies = new HashMap();

		List<String> projectLabels = new ArrayList<String>();
		if (!grafanaResponseCookies.isEmpty()) {
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
