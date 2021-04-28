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
package com.cognizant.devops.platformdal.correlationConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class CorrelationConfigDAL extends BaseDAL {
	 public static final String CORRELATION_CONFIGURATION_QUERY="FROM CorrelationConfiguration CC WHERE CC.relationName = :relationName";
	public static final String RELATIONNAME="relationName";
	private static Logger log = LogManager.getLogger(CorrelationConfigDAL.class);
	
	public Boolean saveCorrelationConfig(CorrelationConfiguration saveCorrelationJson) throws InsightsCustomException {
		
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(RELATIONNAME, saveCorrelationJson.getRelationName());
			List<CorrelationConfiguration> resultList = getResultList(
					CORRELATION_CONFIGURATION_QUERY,
					CorrelationConfiguration.class,
					parameters);
			
			if (!resultList.isEmpty()) {
				throw new InsightsCustomException("Relation Name already exists.");
			} else {
				save(saveCorrelationJson);
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public Boolean updateCorrelationConfig(String relationName, Boolean flag) throws InsightsCustomException {
		
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(RELATIONNAME, relationName);
			List<CorrelationConfiguration> resultList = getResultList(
					CORRELATION_CONFIGURATION_QUERY,
					CorrelationConfiguration.class,
					parameters);

			if (resultList.isEmpty()) {
				throw new InsightsCustomException("Unable to update correlation.");
			} else {
				CorrelationConfiguration correlationConfig = resultList.get(0);
				correlationConfig.setEnableCorrelation(flag);
				update(correlationConfig);				
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public Boolean deleteCorrelationConfig(String relationName) throws InsightsCustomException{
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(RELATIONNAME, relationName);
			List<CorrelationConfiguration> resultList = getResultList(
					CORRELATION_CONFIGURATION_QUERY,
					CorrelationConfiguration.class,
					parameters);
			
			if (resultList.isEmpty()) {
				throw new InsightsCustomException("Unable to update correlation.");
			} else {
				CorrelationConfiguration correlationConfig = resultList.get(0);
				delete(correlationConfig);
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<CorrelationConfiguration> getActiveCorrelations() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM CorrelationConfiguration CC WHERE CC.enableCorrelation = true ",
					CorrelationConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<CorrelationConfiguration> getAllCorrelations() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM CorrelationConfiguration CC ",
					CorrelationConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}
