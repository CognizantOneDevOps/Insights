/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions,. 
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
 * License for the specific language governing permissions and l                                                                                                                                                                         imitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.outcome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class OutComeConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(OutComeConfigDAL.class);

	public int saveOutcomeConfig(InsightsOutcomeTools config) {
		int id = -1;
		try {
			id = (int) save(config);
			return id;
		} catch (Exception e) {
			log.error(e);
			return id;
		}
	}
	
	public int saveInsightsTools(InsightsTools config) {
		int id = -1;
		try {
			id = (int) save(config);
			return id;
		} catch (Exception e) {
			log.error(e);
			return id;
		}
	}

	public List<InsightsTools> getMileStoneTools() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsTools mc", InsightsTools.class, parameters);
		} catch (Exception e) {
			 log.error(e);
			 throw e;
		}
	}

	public void updateOutcomeConfig(InsightsOutcomeTools config) {
		try {
			update(config);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public String deleteOutcomeConfig(int id) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("id", id);
			InsightsOutcomeTools mileStoneRecord = getSingleResult("FROM InsightsOutcomeTools mc WHERE mc.id = :id",
					InsightsOutcomeTools.class, parameters);
			delete(mileStoneRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public InsightsOutcomeTools getOutcomeConfig(int id) {
		InsightsOutcomeTools mileStoneRecord = null;
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("id", id);
			mileStoneRecord = getSingleResult("FROM InsightsOutcomeTools mc WHERE mc.id = :id",
					InsightsOutcomeTools.class, parameters);
			return mileStoneRecord;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<InsightsOutcomeTools> getAllActiveOutcome() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsOutcomeTools", InsightsOutcomeTools.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public InsightsOutcomeTools getOutComeConfigById(int id) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("id", id);
			return getUniqueResult("FROM InsightsOutcomeTools OC WHERE OC.id = :id ", InsightsOutcomeTools.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/*
	 * public OutCome getOutComeById(int outcomeId) { try { Map<String,Object>
	 * parameters = new HashMap<>(); parameters.put("id", outcomeId); return
	 * getUniqueResult( "FROM OutCome OC WHERE OC.id = :id ", OutCome.class,
	 * parameters); } catch (Exception e) { log.error(e); throw e; } }
	 */

	public InsightsTools getOutComeByToolId(int id) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("id", id);
			return getUniqueResult("FROM InsightsTools OC WHERE OC.id = :id ", InsightsTools.class, parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	public InsightsTools getOutComeByToolName(String toolName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("toolName", toolName);
			return getUniqueResult("FROM InsightsTools OC WHERE OC.toolName = :toolName ", InsightsTools.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	public InsightsOutcomeTools getOutComeConfigByName(String outcomeName) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("outcomeName", outcomeName);
			return getUniqueResult("FROM InsightsOutcomeTools OC WHERE OC.outcomeName = :outcomeName ", InsightsOutcomeTools.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}