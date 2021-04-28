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
package com.cognizant.devops.platformdal.relationshipconfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;

@Deprecated
public class RelationshipConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(RelationshipConfigDAL.class);
	
	@Deprecated
	public List<RelationshipConfiguration> getRelationshipConfig(String releationshipName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("relationname", releationshipName);
			return getResultList("FROM RelationshipConfiguration RC where RC.relationname=:relationname ",
					RelationshipConfiguration.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	@Deprecated
	public List<RelationshipConfiguration> loadRelationshipsFromDatabase() {
		try  {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM RelationshipConfiguration RC ",
					RelationshipConfiguration.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	@Deprecated
	public void saveRelationshipConfig(CorrelationConfiguration config) {
		try {
			save(config);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}

	}

}
