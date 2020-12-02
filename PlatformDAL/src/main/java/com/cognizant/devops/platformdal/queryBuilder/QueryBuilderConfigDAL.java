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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformdal.core.BaseDAL;


public class QueryBuilderConfigDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(QueryBuilderConfigDAL.class);
	
	public boolean saveOrUpdateQuery(String reportName, String frequency, String subscribers, String fileName, String queryType, String user) {

		try (Session session = getSessionObj()) {
			Query<QueryBuilderConfig> createQuery = session.createQuery(
					"FROM QueryBuilderConfig a WHERE a.reportName = :reportName", QueryBuilderConfig.class);
			createQuery.setParameter("reportName", reportName);
			List<QueryBuilderConfig> resultList = createQuery.getResultList();
			QueryBuilderConfig queryBuilderConfig = null;
			if (!resultList.isEmpty()) {
				queryBuilderConfig = resultList.get(0);
			}
			session.beginTransaction();
			if (queryBuilderConfig != null) {
				queryBuilderConfig.setReportName(reportName);
				queryBuilderConfig.setFrequency(frequency);
				queryBuilderConfig.setSubscribers(subscribers);
				queryBuilderConfig
						.setQuerypath(ConfigOptions.QUERY_DATA_PROCESSING_RESOLVED_PATH + File.separator + fileName);
				queryBuilderConfig.setQuerytype(queryType);
				queryBuilderConfig.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
				queryBuilderConfig.setLastUpdatedByUser(user);
				session.update(queryBuilderConfig);
			} else {
				queryBuilderConfig = new QueryBuilderConfig();
				queryBuilderConfig.setReportName(reportName);
				queryBuilderConfig.setFrequency(frequency);
				queryBuilderConfig.setSubscribers(subscribers);
				queryBuilderConfig
						.setQuerypath(ConfigOptions.QUERY_DATA_PROCESSING_RESOLVED_PATH + File.separator + fileName);
				queryBuilderConfig.setQuerytype(queryType);
				queryBuilderConfig.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
				queryBuilderConfig.setLastUpdatedByUser(user);
				session.save(queryBuilderConfig);
			}
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public boolean deleteQuery(String reportName) {
		
		try (Session session = getSessionObj()) {
		Query<QueryBuilderConfig> createQuery = session.createQuery(
				"FROM QueryBuilderConfig a WHERE a.reportName = :reportName",
				QueryBuilderConfig.class);
		createQuery.setParameter("reportName", reportName);
		QueryBuilderConfig queryBuilderConfig = createQuery.getSingleResult();
		session.beginTransaction();
		session.delete(queryBuilderConfig);
		session.getTransaction().commit();	
		return true;
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}


	public List<QueryBuilderConfig> fetchQueries() {
		try (Session session = getSessionObj()) {
		Query<QueryBuilderConfig> createQuery = session.createQuery("FROM QueryBuilderConfig",
				QueryBuilderConfig.class);
		return createQuery.getResultList();
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	
	}

}