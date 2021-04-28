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
package com.cognizant.devops.platformdal.mapping.hierarchy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class HierarchyMappingDAL extends BaseDAL {
	private static final String HIERARCHYNAME = "hierarchyName";

	public List<HierarchyMapping> fetchAllHierarchyMapping() {
		Map<String, Object> parameters = new HashMap<>();
		return getResultList("FROM HierarchyMapping HM", HierarchyMapping.class, parameters);
	}

	public boolean saveHierarchyMapping(int rowId, String hierarchyName, String orgName, int orgId) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(HIERARCHYNAME, hierarchyName);
		parameters.put("orgName", orgName);
		parameters.put("rowId", rowId);
		parameters.put("orgId", orgId);
		List<HierarchyMapping> resultList = getResultList(
				"FROM HierarchyMapping a WHERE a.hierarchyName = :hierarchyName AND a.orgName = :orgName AND a.rowId = :rowId AND a.orgId = :orgId",
				HierarchyMapping.class, parameters);

		HierarchyMapping hierarchyMapping = null;
		if (!resultList.isEmpty()) {
			hierarchyMapping = resultList.get(0);
		}
		if (hierarchyMapping == null) {
			hierarchyMapping = new HierarchyMapping();
			hierarchyMapping.setHierarchyName(hierarchyName);
			hierarchyMapping.setOrgName(orgName);
			hierarchyMapping.setRowId(rowId);
			hierarchyMapping.setOrgId(orgId);
			save(hierarchyMapping);
		}
		return true;
	}

	public List<String> getHierarchyMapping(String hierarchyName) {		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(HIERARCHYNAME, hierarchyName);
		return getResultList("SELECT HM.orgName FROM HierarchyMapping HM WHERE HM.hierarchyName = :hierarchyName", String.class, parameters);
	}

	public boolean deleteHierarchyMapping(String hierarchyName, String orgName) {
		Map<String,Object> parameters = new HashMap<>();
		parameters.put(HIERARCHYNAME, hierarchyName);
		parameters.put("orgName", orgName);
		HierarchyMapping hierarchyMapping = getSingleResult(
				"FROM HierarchyMapping a WHERE a.hierarchyName = :hierarchyName AND a.orgName = :orgName",
				HierarchyMapping.class,
				parameters);
		delete(hierarchyMapping);
		return true;
	}
}
