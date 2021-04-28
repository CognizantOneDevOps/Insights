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
package com.cognizant.devops.platformdal.mapping.projects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class ProjectMappingDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(ProjectMappingDAL.class);

	public List<ProjectMapping> fetchProjectMapping(String projectName, String projectId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("projectName", projectName);
			parameters.put("projectId", projectId);
			return getResultList(
					"FROM ProjectMapping PM WHERE PM.projectName = :projectName AND PM.projectId = :projectId",
					ProjectMapping.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<ProjectMapping> fetchProjectMappingByOrgId(int orgId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("orgId", orgId);
			return getResultList("FROM ProjectMapping PM WHERE PM.orgId = :orgId", ProjectMapping.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<ProjectMapping> fetchAllProjectMapping() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM ProjectMapping PM", ProjectMapping.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean addProjectMapping(ProjectMapping projectMapping) {
		try {
			save(projectMapping);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}
