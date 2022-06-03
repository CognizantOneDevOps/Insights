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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Validator;
import org.owasp.esapi.reference.DefaultSecurityConfiguration;
import java.util.Properties;

public class DataArchivalConfigDal extends BaseDAL {
	private static final String INSIGHTSDATAARCHIVALCONFIG_QUERY = "FROM InsightsDataArchivalConfig DA WHERE ";
	private static final String ARCHIVALNAME = "archivalName";
	private static final String DA_ARCHIVALNAME = "DA.archivalName = :archivalName";
	private static Logger log = LogManager.getLogger(DataArchivalConfigDal.class);

	/**
	 * Method to save Data archival record
	 * 
	 * @param dataArchivalConfig
	 * @return Boolean
	 * @throws InsightsCustomException
	 */
	public Boolean saveDataArchivalConfiguration(InsightsDataArchivalConfig dataArchivalConfig)
			throws InsightsCustomException {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ARCHIVALNAME, dataArchivalConfig.getArchivalName());
			List<InsightsDataArchivalConfig> resultList = getResultList(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME, InsightsDataArchivalConfig.class, parameters);
			if (!resultList.isEmpty()) {
				throw new InsightsCustomException("Archival Name already exists.");
			} else {
				save(dataArchivalConfig);
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
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ARCHIVALNAME, archivalName);
			return getUniqueResult(INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME, InsightsDataArchivalConfig.class,
					parameters);
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
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsDataArchivalConfig DA ORDER BY DA.createdOn DESC",
					InsightsDataArchivalConfig.class, parameters);
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
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList(INSIGHTSDATAARCHIVALCONFIG_QUERY + " DA.status = 'ACTIVE' ORDER BY DA.createdOn DESC",
					InsightsDataArchivalConfig.class, parameters);
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
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig dataArchivalConfig = (InsightsDataArchivalConfig) getSingleResult(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME, InsightsDataArchivalConfig.class, parameters);
			delete(dataArchivalConfig);
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
	 * @throws Exception 
	 */
	public Boolean updateArchivalStatus(String archivalName, String status) throws Exception  {
		try {
			Validator validate = getESAPIValidator() ;
			String validatedArchivalName = validate.getValidInput("DAL_parameter_checking", archivalName, "SafeString",
					600, false);
			String validatedStatus = validate.getValidInput("DAL_parameter_checking", status, "SafeString", 600, false);
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ARCHIVALNAME, validatedArchivalName);
			InsightsDataArchivalConfig updateStatus = getSingleResult(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME, InsightsDataArchivalConfig.class, parameters);
			updateStatus.setStatus(validatedStatus);
			update(updateStatus);
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e.getMessage());
//			return Boolean.FALSE;
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
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig updateSourceUrl = getUniqueResult(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME, InsightsDataArchivalConfig.class, parameters);

			if (updateSourceUrl != null) {
				updateSourceUrl.setSourceUrl(sourceUrl);
				updateSourceUrl.setStatus(DataArchivalStatus.ACTIVE.name());
				update(updateSourceUrl);
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
	public Boolean updateContainerDetails(String archivalName, String sourceUrl, String containerID, Long expiryDate,
			int boltPort) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ARCHIVALNAME, archivalName);
			InsightsDataArchivalConfig updateSourceUrl = getUniqueResult(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + DA_ARCHIVALNAME, InsightsDataArchivalConfig.class, parameters);

			if (updateSourceUrl != null) {
				updateSourceUrl.setSourceUrl(sourceUrl);
				updateSourceUrl.setContainerID(containerID);
				updateSourceUrl.setExpiryDate(expiryDate);
				updateSourceUrl.setStatus(DataArchivalStatus.ACTIVE.name());
				updateSourceUrl.setBoltPort(boltPort);
				update(updateSourceUrl);
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

		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("containerID", containerID);
			return getUniqueResult(INSIGHTSDATAARCHIVALCONFIG_QUERY + "DA.containerID = :containerID",
					InsightsDataArchivalConfig.class, parameters);
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
		try {
			long now = InsightsUtils.getCurrentTimeInSeconds();
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("now", now);
			return getResultList(
					INSIGHTSDATAARCHIVALCONFIG_QUERY + "DA.expiryDate <= :now AND DA.status != 'TERMINATED'",
					InsightsDataArchivalConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}

	}

}
