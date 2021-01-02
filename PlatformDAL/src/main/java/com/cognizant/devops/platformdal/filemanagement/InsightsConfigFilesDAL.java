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
package com.cognizant.devops.platformdal.filemanagement;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class InsightsConfigFilesDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(InsightsConfigFilesDAL.class);

	/**
	 * Method to get all Configuration files
	 * 
	 * @return List<InsightsConfigFiles>
	 */
	public List<InsightsConfigFiles> getAllConfigurationFiles() {
		try (Session session = getSessionObj()) {
			Query<InsightsConfigFiles> createQuery = session.createQuery("FROM InsightsConfigFiles ICF",
					InsightsConfigFiles.class);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Configuration files for a particular module
	 * 
	 * @param fileModule
	 * @return List<InsightsConfigFiles>
	 */
	public List<InsightsConfigFiles> getAllConfigurationFilesForModule(String fileModule) {
		try (Session session = getSessionObj()) {
			Query<InsightsConfigFiles> createQuery = session.createQuery(
					"FROM InsightsConfigFiles ICF where ICF.fileModule = :fileModule", InsightsConfigFiles.class);
			createQuery.setParameter("fileModule", fileModule);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * Method to fetch a Configuration file object for a given filename
	 * 
	 * @param fileName
	 * @return InsightsConfigFiles
	 */
	public InsightsConfigFiles getConfigurationFile(String fileName) {
		try (Session session = getSessionObj()) {
			Query<InsightsConfigFiles> createQuery = session.createQuery(
					"FROM InsightsConfigFiles ICF where ICF.fileName = :fileName", InsightsConfigFiles.class);
			createQuery.setParameter("fileName", fileName);
			return createQuery.uniqueResult();
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Configuration file
	 * 
	 * @param config
	 * @return int
	 */
	public int saveConfigurationFile(InsightsConfigFiles config) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int recordId = (int) session.save(config);
			session.getTransaction().commit();
			return recordId;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Configuration file
	 * 
	 * @param fileName
	 * @return String
	 */
	public String deleteConfigurationFile(String fileName) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsConfigFiles> createQuery = session.createQuery(
					"FROM InsightsConfigFiles ICF where ICF.fileName = :fileName", InsightsConfigFiles.class);
			createQuery.setParameter("fileName", fileName);
			InsightsConfigFiles record = createQuery.getSingleResult();
			session.delete(record);
			session.getTransaction().commit();
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * Method to update Configuration file
	 * 
	 * @param config
	 * @return int
	 */
	public int updateConfigurationFile(InsightsConfigFiles config) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.update(config);
			session.getTransaction().commit();
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
