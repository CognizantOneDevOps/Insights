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
package com.cognizant.devops.platformdal.hierarchy.details;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class HierarchyDetailsDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(HierarchyDetailsDAL.class);

	public boolean addHierarchyDetails(HierarchyDetails hierarchyDetails) {
		try {
			save(hierarchyDetails);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<String> fetchDistinctHierarchyName() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("SELECT DISTINCT HD.hierarchyName FROM HierarchyDetails HD", String.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<HierarchyDetails> fetchAllEntityData() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM HierarchyDetails HD", HierarchyDetails.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteHierarchyDetails(String hierarchyName) {
		try {

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("hierarchyName", hierarchyName);
			HierarchyDetails hierarchyDetails = getSingleResult(
					"FROM HierarchyDetails HD WHERE HD.hierarchyName = :hierarchyName", HierarchyDetails.class,
					parameters);
			delete(hierarchyDetails);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean addHierarchyDetailsList(List<HierarchyDetails> hiearchyList) {
		try {
			for (HierarchyDetails details : hiearchyList) {
				save(details);
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
}
