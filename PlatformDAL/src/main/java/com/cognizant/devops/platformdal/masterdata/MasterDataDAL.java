/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.masterdata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class MasterDataDAL extends BaseDAL {

	private static final Logger log = LogManager.getLogger(MasterDataDAL.class);
	
	public static final List<String> MASTER_TABLE_LIST = Collections.unmodifiableList(Arrays.asList("agent_configuration","INSIGHTS_SCHEDULER_TASK_DEFINITION"
												,"INSIGHTS_WORKFLOW_TYPE","INSIGHTS_WORKFLOW_TASK","INSIGHTS_ROI_TOOLS","INSIGHTS_ASSESSMENT_REPORT_TEMPLATE"));

	public void processMasterDataQuery(String query) throws InsightsCustomException {
		try {
			String escapedSQL = ValidationUtils.cleanXSS(query);
			boolean validData = MASTER_TABLE_LIST.stream().anyMatch(tableName -> query.contains(tableName));
			if(validData) {
				int executedRecord = executeUpdateWithSQLQuery(escapedSQL);
				log.debug("processMasterDataQuery data updated {} for query  {} ",executedRecord, query);
			}else {
				log.error(query);
				throw new InsightsCustomException("Unable to process query ");
			}
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
}
