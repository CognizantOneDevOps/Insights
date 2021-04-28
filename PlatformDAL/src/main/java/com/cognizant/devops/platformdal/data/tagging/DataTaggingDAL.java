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
package com.cognizant.devops.platformdal.data.tagging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class DataTaggingDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(DataTaggingDAL.class);
	
	
	public boolean addEntityData(DataTagging dataTagging) {
		try  {
			save(dataTagging);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<DataTagging> fetchEntityData(String hierarchyName) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("hierarchyName", hierarchyName);
			return getResultList(
					"FROM DataTagging DT WHERE DT.hierarchyName = :hierarchyName",
					DataTagging.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public List<DataTagging> fetchAllEntityData() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM DataTagging DT",
					DataTagging.class,
					parameters);
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteEntityData(String hierarchyName) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("hierarchyName", hierarchyName);
			DataTagging dataTagging =  getSingleResult(
					"FROM DataTagging DT WHERE DT.hierarchyName = :hierarchyName",
					DataTagging.class,
					parameters);
			delete(dataTagging);
			return true;
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
}
