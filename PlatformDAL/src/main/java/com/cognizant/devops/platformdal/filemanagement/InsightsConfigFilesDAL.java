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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList( "FROM InsightsConfigFiles ICF",
					InsightsConfigFiles.class,
					parameters);
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
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("fileModule", fileModule);
			return getResultList( "FROM InsightsConfigFiles ICF where ICF.fileModule = :fileModule",
					InsightsConfigFiles.class,
					parameters);
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
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("fileName", fileName);
			return getUniqueResult( "FROM InsightsConfigFiles ICF where ICF.fileName = :fileName",
					InsightsConfigFiles.class,
					parameters);
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
		try  {
			return (int) save(config);
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
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("fileName", fileName);
			InsightsConfigFiles record = getSingleResult(
					"FROM InsightsConfigFiles ICF where ICF.fileName = :fileName",
							InsightsConfigFiles.class,
					parameters);
			delete(record);
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
		try {
			update(config);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
