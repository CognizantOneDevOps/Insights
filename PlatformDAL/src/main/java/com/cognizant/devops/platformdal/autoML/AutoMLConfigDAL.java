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
package com.cognizant.devops.platformdal.autoML;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;

public class AutoMLConfigDAL extends BaseDAL {
	public static final String USECASE = "usecase";
	private static Logger log = LogManager.getLogger(AutoMLConfigDAL.class.getName());

	/**
	 * Method to check whether the usecase name is existing in the Database
	 *
	 * @param usecase
	 * @return
	 */
	private WorkflowDAL workflowDal = new WorkflowDAL();

	public boolean isUsecaseExisting(String usecase) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(USECASE, usecase);
			List<AutoMLConfig> resultList =  getResultList("FROM AutoMLConfig a WHERE a.useCaseName = :usecase ",
					AutoMLConfig.class, parameters);
			if (resultList.isEmpty()) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Create or update the usecase details
	 *
	 * @param usecase
	 * @param config
	 * @param prediction
	 * @param Mojo
	 * @return
	 */
	public boolean createOrUpdate(String usecase, String config, String prediction, String Mojo) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(USECASE, usecase);
			List<AutoMLConfig> resultList =  getResultList("FROM AutoMLConfig a WHERE a.usecase = :usecase",
					AutoMLConfig.class, parameters);

			AutoMLConfig autoMLConfig = null;
			Long updatedDate = System.currentTimeMillis();
			if (!resultList.isEmpty()) {
				autoMLConfig = resultList.get(0);
			}
			if (autoMLConfig != null) {
				if (Mojo != null)
					autoMLConfig.setMojoDeployed(Mojo);
				if (prediction != null)
					autoMLConfig.setPredictionColumn(prediction);
				autoMLConfig.setUpdatedDate(updatedDate);
				update(autoMLConfig);
			} else {
				autoMLConfig = new AutoMLConfig();
				autoMLConfig.setUseCaseName(usecase);
				autoMLConfig.setConfigJson(config);
				autoMLConfig.setCreatedDate(updatedDate);
				autoMLConfig.setUpdatedDate(updatedDate);
				save(autoMLConfig);
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	

	public int updateMLConfig(AutoMLConfig config) {

		int id = -1;
		try {
			update(config);
			return 1;
		} catch (Exception e) {
			return id;
		}
	}

	public int saveMLConfig(AutoMLConfig config) {
		int id = -1;
		try {
			return (int) save(config);
		} catch (Exception e) {
			return id;
		}
	}

	/**
	 * Method to get the Prediction Column name for given usecase
	 *
	 * @param usecase
	 * @return
	 */
	public String getPredictionColumn(String usecase) {

		try {
			
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(USECASE, usecase);
			AutoMLConfig result = getSingleResult(
					"FROM AutoMLConfig AC WHERE AC.usecase = :usecase",
					AutoMLConfig.class,
					parameters);
			return result.getPredictionColumn();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}

	}

	public AutoMLConfig getMLConfigByUsecase(String usecase) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(USECASE, usecase);
			return getSingleResult(
					"FROM AutoMLConfig AC WHERE AC.useCaseName = :usecase",
					AutoMLConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Delete the record for given usecase
	 *
	 * @param usecase
	 * @return
	 */
	public boolean deleteUsecase(String usecase) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(USECASE, usecase);
			AutoMLConfig autoMLConfig =  getUniqueResult(
					"FROM AutoMLConfig a WHERE a.useCaseName = :usecase",
					AutoMLConfig.class,
					parameters);
			
			if (autoMLConfig != null) {
				List<InsightsWorkflowExecutionHistory> executionHistory = workflowDal
						.getWorkflowExecutionHistoryByWorkflowId(autoMLConfig.getWorkflowConfig().getWorkflowId());
				if (!executionHistory.isEmpty()) {
					executionHistory.forEach(eachExecution -> {
						workflowDal.deleteExecutionHistory(eachExecution);
					});
				}
				delete(autoMLConfig);
			} else {
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Get the list of all usecases
	 *
	 * @return
	 */
	public List<AutoMLConfig> fetchUsecases() {
		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM AutoMLConfig",
					AutoMLConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Get the usecases based on workflow id
	 *
	 * @return
	 */
	public AutoMLConfig fetchUseCasesByWorkflowId(String workflowId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowId", workflowId);
			return  getUniqueResult(
					"FROM AutoMLConfig AMLC where AMLC.workflowConfig.workflowId = :workflowId",
					AutoMLConfig.class,
					parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * Get all active and mojo_deployed usecases list
	 *
	 * @return
	 */
	public List<AutoMLConfig> getActiveUsecaseList() {

		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM AutoMLConfig AMLC WHERE AMLC.isActive = true AND AMLC.status = 'MOJO_DEPLOYED'",
					AutoMLConfig.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}
