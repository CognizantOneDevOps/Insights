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
package com.cognizant.devops.platformdal.queryBuilder;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformdal.core.BaseDAL;


public class QueryBuilderConfigDAL extends BaseDAL {


	public boolean saveOrUpdateQuery(String reportName, String frequency, String subscribers, String fileName, String queryType, String user) {

		Query<QueryBuilderConfig> createQuery = getSession().createQuery(
				"FROM QueryBuilderConfig a WHERE a.reportName = :reportName",
				QueryBuilderConfig.class);
		createQuery.setParameter("reportName", reportName);
		List<QueryBuilderConfig> resultList = createQuery.getResultList();
		QueryBuilderConfig queryBuilderConfig = null;
		if(!resultList.isEmpty()){
			queryBuilderConfig = resultList.get(0);
		}
		getSession().beginTransaction();
		if (queryBuilderConfig != null) {
			queryBuilderConfig.setReportName(reportName);
			queryBuilderConfig.setFrequency(frequency);
			queryBuilderConfig.setSubscribers(subscribers);
			queryBuilderConfig.setQuerypath(ConfigOptions.QUERY_DATA_PROCESSING_RESOLVED_PATH+File.separator+fileName);
			queryBuilderConfig.setQuerytype(queryType);
			queryBuilderConfig.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
			queryBuilderConfig.setLastUpdatedByUser(user);
			getSession().update(queryBuilderConfig);
		} else {
			queryBuilderConfig = new QueryBuilderConfig();
			queryBuilderConfig.setReportName(reportName);
			queryBuilderConfig.setFrequency(frequency);
			queryBuilderConfig.setSubscribers(subscribers);
			queryBuilderConfig.setQuerypath(ConfigOptions.QUERY_DATA_PROCESSING_RESOLVED_PATH+File.separator+fileName);
			queryBuilderConfig.setQuerytype(queryType);
			queryBuilderConfig.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
			queryBuilderConfig.setLastUpdatedByUser(user);
			getSession().save(queryBuilderConfig);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
	
	public boolean deleteQuery(String reportName) {
		Query<QueryBuilderConfig> createQuery = getSession().createQuery(
				"FROM QueryBuilderConfig a WHERE a.reportName = :reportName",
				QueryBuilderConfig.class);
		createQuery.setParameter("reportName", reportName);
		QueryBuilderConfig queryBuilderConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(queryBuilderConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}


	public List<QueryBuilderConfig> fetchQueries() {
		Query<QueryBuilderConfig> createQuery = getSession().createQuery("FROM QueryBuilderConfig",
				QueryBuilderConfig.class);
		List<QueryBuilderConfig> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

}