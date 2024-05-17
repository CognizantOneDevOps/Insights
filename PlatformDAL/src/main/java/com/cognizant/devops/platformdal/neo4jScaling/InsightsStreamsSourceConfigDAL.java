/*******************************************************************************
* Copyright 2024 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.neo4jScaling;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class InsightsStreamsSourceConfigDAL extends BaseDAL {
	private static Logger log = LogManager.getLogger(InsightsStreamsSourceConfigDAL.class);

	/**
	 * Method to get source neo4j config by configID
	 * 
	 * @return InsightsStreamsSourceConfig
	 */
	public InsightsStreamsSourceConfig getStreamsSourceConfig(String configID) {
		InsightsStreamsSourceConfig streamsSourceConfig = null;
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("configID", configID);
			streamsSourceConfig = getUniqueResult("FROM InsightsStreamsSourceConfig ISSC where ISSC.configID = :configID",
					InsightsStreamsSourceConfig.class, parameters);
			return streamsSourceConfig;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
}
