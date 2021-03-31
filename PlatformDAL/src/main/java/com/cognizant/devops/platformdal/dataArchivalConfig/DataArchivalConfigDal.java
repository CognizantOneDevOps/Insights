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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;


public class DataArchivalConfigDal extends BaseDAL {
	private static final String INSIGHTSDATAARCHIVALCONFIG_QUERY ="FROM InsightsDataArchivalConfig DA WHERE ";
		private static final String ARCHIVALNAME ="archivalName";
		private static final String DA_ARCHIVALNAME="DA.archivalName = :archivalName";
	private static Logger log = LogManager.getLogger(DataArchivalConfigDal.class);
	
	
	/**
	 * Method to save Data archival record
	 * 
	 * @param dataArchivalConfig
	 * @return Boolean 
	 * @throws InsightsCustomException
	 */
	public Boolean saveDataArchivalConfiguration(InsightsDataArchivalConfig dataArchivalConfig) throws InsightsCustomException {
		
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME,
					InsightsDataArchivalConfig.class);
			createQuery.setParameter(ARCHIVALNAME, dataArchivalConfig.getArchivalName());
			List<InsightsDataArchivalConfig> resultList = createQuery.getResultList();
			if (!resultList.isEmpty()) {
				throw new InsightsCustomException("Archival Name already exists.");
			} else {
				session.beginTransaction();
				session.save(dataArchivalConfig);
				session.getTransaction().commit();
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Method to get a specific Archival record using archivalName
	 * 
	 * @param archivalName
	 * @return InsightsDataArchivalConfig object
	 */
	public InsightsDataArchivalConfig getSpecificArchivalRecord(String archivalName) {
		
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME,
					InsightsDataArchivalConfig.class);
			createQuery.setParameter(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig result = createQuery.uniqueResult();
			return result;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}
	
	/**
	 * Method to get all Archival records
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 */
	public List<InsightsDataArchivalConfig> getAllArchivalRecord() {
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					"FROM InsightsDataArchivalConfig DA ORDER BY DA.createdOn DESC", InsightsDataArchivalConfig.class);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}
	
	/**
	 * Method to get all active Archival records
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 */
	public List<InsightsDataArchivalConfig> getActiveList() {
		
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					"FROM InsightsDataArchivalConfig DA WHERE DA.status = 'ACTIVE' ORDER BY DA.createdOn DESC",
					InsightsDataArchivalConfig.class);
			List<InsightsDataArchivalConfig> resultList = createQuery.getResultList();
			return resultList;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}
	
	/**
	 * Method to delete an archival record
	 * 
	 * @param archivalName
	 * @return Boolean
	 */
	public Boolean deleteArchivalRecord(String archivalName) {
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME,
					InsightsDataArchivalConfig.class);
			createQuery.setParameter(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig dataArchivalConfig = createQuery.getSingleResult();
			session.beginTransaction();
			session.delete(dataArchivalConfig);
			session.getTransaction().commit();
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Method to update status of archival record
	 * 
	 * @param archivalName
	 * @param status
	 * @return Boolean
	 */
	public Boolean updateArchivalStatus(String archivalName, String status) {
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME,
					InsightsDataArchivalConfig.class);
			createQuery.setParameter(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig updateStatus = createQuery.getSingleResult();
			updateStatus.setStatus(status);
			session.beginTransaction();
			session.update(updateStatus);
			session.getTransaction().commit();
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Method to update Archival source URL
	 * 
	 * @param archivalName
	 * @param sourceUrl
	 * @return Boolean 
	 */
	public Boolean updateArchivalSourceUrl(String archivalName, String sourceUrl) {
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME,
					InsightsDataArchivalConfig.class);
			createQuery.setParameter(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig updateSourceUrl = createQuery.uniqueResult();
			if (updateSourceUrl != null) {
				updateSourceUrl.setSourceUrl(sourceUrl);
				updateSourceUrl.setStatus(DataArchivalStatus.ACTIVE.name());
				session.beginTransaction();
				session.update(updateSourceUrl);
				session.getTransaction().commit();

			} else {
				throw new NoResultException("No entity result found for query");
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Method to update source URL, containerID and expiry date.
	 * 
	 * @param archivalName
	 * @param sourceUrl
	 * @param containerID
	 * @param expiryDate
	 * @return Boolean
	 */
	public Boolean updateContainerDetails(String archivalName, String sourceUrl, String containerID, Long expiryDate) {
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME,
					InsightsDataArchivalConfig.class);
			createQuery.setParameter(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig updateSourceUrl = createQuery.uniqueResult();
			if (updateSourceUrl != null) {
				updateSourceUrl.setSourceUrl(sourceUrl);
				updateSourceUrl.setContainerID(containerID);
				updateSourceUrl.setExpiryDate(expiryDate);
				updateSourceUrl.setStatus(DataArchivalStatus.ACTIVE.name());
				session.beginTransaction();
				session.update(updateSourceUrl);
				session.getTransaction().commit();

			} else {
				throw new NoResultException("No entity result found for query");
			}
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Method to get specific archival record using containerID.
	 * 
	 * @param containerID
	 * @return InsightsDataArchivalConfig object
	 */
	public InsightsDataArchivalConfig getArchivalRecordByContainerId(String containerID) {
		
		try (Session session = getSessionObj()) {
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + "DA.containerID = :containerID",
					InsightsDataArchivalConfig.class);
			createQuery.setParameter("containerID", containerID);
			return createQuery.uniqueResult();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}
	
	/**
	 * Method to get expired records which have status other than TERMINATED.
	 * 
	 * @return List<InsightsDataArchivalConfig>
	 */
	public List<InsightsDataArchivalConfig> getExpiredArchivalrecords() {
		try (Session session = getSessionObj()) {
			long now = InsightsUtils.getCurrentTimeInSeconds();
			Query<InsightsDataArchivalConfig> createQuery = session.createQuery(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + "DA.expiryDate <= :now AND DA.status != 'TERMINATED'", InsightsDataArchivalConfig.class);
			createQuery.setParameter("now", now);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}

}
