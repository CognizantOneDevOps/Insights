/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.dataArchivalConfig;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;


public class DataArchivalConfigDal extends BaseDAL {
	
	/**
	 * Method to save Data archival record
	 * 
	 * @param dataArchivalConfig
	 * @return Boolean 
	 * @throws InsightsCustomException
	 */
	public Boolean saveDataArchivalConfiguration(InsightsDataArchivalConfig dataArchivalConfig) throws InsightsCustomException {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA WHERE "
				+ "DA.archivalName = :archivalName ",InsightsDataArchivalConfig.class);
		createQuery.setParameter("archivalName", dataArchivalConfig.getArchivalName());
		List<InsightsDataArchivalConfig> resultList = createQuery.getResultList();
		if (!resultList.isEmpty()) {
			throw new InsightsCustomException("Archival Name already exists.");
		} else {
			getSession().beginTransaction();
			getSession().save(dataArchivalConfig);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
			return Boolean.TRUE;
		}
	}
	
	/**
	 * Method to get a specific Archival record using archivalName
	 * 
	 * @param archivalName
	 * @return InsightsDataArchivalConfig object
	 */
	public InsightsDataArchivalConfig getSpecificArchivalRecord(String archivalName) {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA WHERE "
				+ "DA.archivalName = :archivalName ",InsightsDataArchivalConfig.class);
		createQuery.setParameter("archivalName", archivalName);
		InsightsDataArchivalConfig result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
		
	}
	
	/**
	 * Method to get all Archival records
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 */
	public List<InsightsDataArchivalConfig> getAllArchivalRecord() {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA ORDER BY DA.createdOn DESC",InsightsDataArchivalConfig.class);
		List<InsightsDataArchivalConfig> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
		
	}
	
	/**
	 * Method to get all active Archival records
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 */
	public List<InsightsDataArchivalConfig> getActiveList() {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA WHERE DA.status = 'ACTIVE' ORDER BY DA.createdOn DESC",InsightsDataArchivalConfig.class);
		List<InsightsDataArchivalConfig> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
		
	}
	
	/**
	 * Method to delete an archival record
	 * 
	 * @param archivalName
	 * @return Boolean
	 */
	public Boolean deleteArchivalRecord(String archivalName) {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA WHERE "
				+ "DA.archivalName = :archivalName",InsightsDataArchivalConfig.class);
		createQuery.setParameter("archivalName", archivalName);
		InsightsDataArchivalConfig dataArchivalConfig = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(dataArchivalConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}
	
	/**
	 * Method to update status of archival record
	 * 
	 * @param archivalName
	 * @param status
	 * @return Boolean
	 */
	public Boolean updateArchivalStatus(String archivalName, String status) {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA WHERE "
				+ "DA.archivalName = :archivalName",InsightsDataArchivalConfig.class);
		createQuery.setParameter("archivalName", archivalName);
		InsightsDataArchivalConfig updateStatus = createQuery.getSingleResult();
		updateStatus.setStatus(status);
		getSession().beginTransaction();
		getSession().update(updateStatus);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
	}
	
	/**
	 * Method to update Archival source URL
	 * 
	 * @param archivalName
	 * @param sourceUrl
	 * @return Boolean 
	 */
	public Boolean updateArchivalSourceUrl(String archivalName, String sourceUrl) {
		Query<InsightsDataArchivalConfig> createQuery = getSession().createQuery("FROM InsightsDataArchivalConfig DA WHERE "
				+ "DA.archivalName = :archivalName",InsightsDataArchivalConfig.class);
		createQuery.setParameter("archivalName", archivalName);
		InsightsDataArchivalConfig updateSourceUrl = createQuery.uniqueResult();
		if (updateSourceUrl != null) {
			updateSourceUrl.setSourceUrl(sourceUrl);
			updateSourceUrl.setStatus(DataArchivalStatus.ACTIVE.name());
			getSession().beginTransaction();
			getSession().update(updateSourceUrl);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
		} else {
			throw new NoResultException("No entity result found for query");
		}
		return Boolean.TRUE;
	}

}
