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

package com.cognizant.devops.platformservice.querybuilder.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.queryBuilder.QueryBuilderConfig;
import com.cognizant.devops.platformdal.queryBuilder.QueryBuilderConfigDAL;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;

@Service("queryBuilderService")
public class QueryBuilderServiceImpl implements QueryBuilderService{

	private static Logger Log = LogManager.getLogger(AgentManagementServiceImpl.class);
	private static final String SUCCESS = "SUCCESS";

	@Override
	public String saveOrUpdateQuery(String reportName, String frequency, String subscribers, String fileName, String queryType, String user) throws InsightsCustomException {
		String result = "";
		try{
			QueryBuilderConfigDAL queryBuilderConfigDAL = new QueryBuilderConfigDAL();
			boolean status = queryBuilderConfigDAL.saveOrUpdateQuery(reportName, frequency, subscribers, fileName, queryType, user);
			if(status){
				Log.info("Successfully inserted/updated the query for "+ reportName);
				result = SUCCESS;
			}
		}catch(Exception e){
			Log.error("Error while inserting new query " + reportName, e);
			throw new InsightsCustomException(e.toString());
		}
		return result;
	}

	@Override
	public String deleteQuery(String reportName) throws InsightsCustomException {
		String result = "";
		try{
			QueryBuilderConfigDAL queryBuilderConfigDAL = new QueryBuilderConfigDAL();
			boolean status = queryBuilderConfigDAL.deleteQuery(reportName);
			if(status){
				Log.info("Successfully deleted the query for "+ reportName);
				result = SUCCESS;
			}
		}catch(Exception e){
			Log.error("Error while deleting query " + reportName, e);
			throw new InsightsCustomException(e.toString());
		}
		return result;
	}

	@Override
	public List<QueryBuilderConfig> fetchQueries() throws InsightsCustomException {
		List<QueryBuilderConfig> queryList = null;
		try{
			QueryBuilderConfigDAL queryBuilderConfigDAL = new QueryBuilderConfigDAL();
			queryList = queryBuilderConfigDAL.fetchQueries();
		}catch(Exception e){
			Log.error("Error while Fetching queries " , e);
			throw new InsightsCustomException(e.toString());
		}
		return queryList;
	}

}
