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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;

public class AutoMLConfigDAL extends BaseDAL {

    private static Logger log = LogManager.getLogger(AutoMLConfigDAL.class.getName());

    /**
     * Method to check whether the usecase name is existing in the Database
     *
     * @param usecase
     * @return
     */
    private WorkflowDAL workflowDal = new WorkflowDAL(); 

    public boolean isUsecaseExisting(String usecase) {
        Query<AutoMLConfig> createQuery = getSession().createQuery("FROM AutoMLConfig a WHERE a.useCaseName = :usecase ",
                AutoMLConfig.class);
        createQuery.setParameter("usecase", usecase);
        log.debug("QUERY: {}", createQuery);
        if (createQuery.getResultList().isEmpty()) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
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
        Query<AutoMLConfig> createQuery = getSession().createQuery(
                "FROM AutoMLConfig a WHERE a.usecase = :usecase",
                AutoMLConfig.class);
        createQuery.setParameter("usecase", usecase);
        List<AutoMLConfig> resultList = createQuery.getResultList();
        AutoMLConfig autoMLConfig = null;
        Long updatedDate =System.currentTimeMillis();
        if (!resultList.isEmpty()) {
            autoMLConfig = resultList.get(0);
        }
        getSession().beginTransaction();
        if (autoMLConfig != null) {
            if (Mojo != null)
                autoMLConfig.setMojoDeployed(Mojo);
            if (prediction != null)
                autoMLConfig.setPredictionColumn(prediction);
            autoMLConfig.setUpdatedDate(updatedDate);
            getSession().update(autoMLConfig);
        } else {
            autoMLConfig = new AutoMLConfig();
			autoMLConfig.setUseCaseName(usecase);
            autoMLConfig.setConfigJson(config);
            autoMLConfig.setCreatedDate(updatedDate);
            autoMLConfig.setUpdatedDate(updatedDate);
            getSession().save(autoMLConfig);
        }
        getSession().getTransaction().commit();
        terminateSession();
        terminateSessionFactory();
        return true;
    }
    
    
    public int updateMLConfig(AutoMLConfig config)
    {
    	int id=-1;
    	try {
    	getSession().beginTransaction();
		getSession().update(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return 1;
    	}
    	catch(Exception e) {return id;}
    }
    
    public int saveMLConfig(AutoMLConfig config)
    { 
    	int id=-1;
    	try {
    	getSession().beginTransaction();
		id = (int) getSession().save(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return id;
    	}
    	catch(Exception e) {return id;}
    }

    /**
     * Method to get the Prediction Column name for given usecase
     *
     * @param usecase
     * @return
     */
    public String getPredictionColumn(String usecase) {
        Query<AutoMLConfig> createQuery = getSession().createQuery("FROM AutoMLConfig AC WHERE AC.usecase = :usecase",
                AutoMLConfig.class);
        createQuery.setParameter("usecase", usecase);
        AutoMLConfig result = createQuery.getSingleResult();
        terminateSession();
        terminateSessionFactory();
        return result.getPredictionColumn();
    }
    
    public AutoMLConfig getMLConfigByUsecase(String usecase) {
        Query<AutoMLConfig> createQuery = getSession().createQuery("FROM AutoMLConfig AC WHERE AC.useCaseName = :usecase",
                AutoMLConfig.class);
        createQuery.setParameter("usecase", usecase);
        AutoMLConfig result = createQuery.getSingleResult();
        terminateSession();
        terminateSessionFactory();
        return result;
    }

    /**
     * Delete the record for given usecase
     *
     * @param usecase
     * @return
     */
    public boolean deleteUsecase(String usecase) {

		Query<AutoMLConfig> createQuery = getSession().createQuery(
				"FROM AutoMLConfig a WHERE a.useCaseName = :usecase",
				AutoMLConfig.class);
		createQuery.setParameter("usecase", usecase);
		AutoMLConfig autoMLConfig = createQuery.uniqueResult();
		if (autoMLConfig != null) {
			List<InsightsWorkflowExecutionHistory> executionHistory = workflowDal
					.getWorkflowExecutionHistoryByWorkflowId(autoMLConfig.getWorkflowConfig().getWorkflowId());
			if (!executionHistory.isEmpty()) {
				executionHistory.forEach(eachExecution -> {
					workflowDal.deleteExecutionHistory(eachExecution);
				});
			}
			getSession().beginTransaction();
			getSession().delete(autoMLConfig);
			getSession().getTransaction().commit();
		} else {
			return false;
		}
		terminateSession();
		terminateSessionFactory();
		return true;
    }

    /**
     * Get the list of all usecases
     *
     * @return
     */
    public List<AutoMLConfig> fetchUsecases() {
        Query<AutoMLConfig> createQuery = getSession().createQuery("FROM AutoMLConfig",
                AutoMLConfig.class);
        List<AutoMLConfig> result = createQuery.getResultList();
        terminateSession();
        terminateSessionFactory();
        return result;
    }
    
    

	/**
	 * Get the usecases based on workflow id
	 *
	 * @return
	 */
	public AutoMLConfig fetchUseCasesByWorkflowId(String workflowId) {
		Query<AutoMLConfig> createQuery = getSession().createQuery(
				"FROM AutoMLConfig AMLC where AMLC.workflowConfig.workflowId = :workflowId", AutoMLConfig.class);
		createQuery.setParameter("workflowId", workflowId);
		AutoMLConfig result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
	/**
     * Get all active and mojo_deployed usecases list
     *
     * @return
     */
	public List<AutoMLConfig> getActiveUsecaseList() {
        Query<AutoMLConfig> createQuery = getSession().createQuery(
        		"FROM AutoMLConfig AMLC WHERE AMLC.isActive = true AND AMLC.status = 'MOJO_DEPLOYED'",
        		AutoMLConfig.class);
        List<AutoMLConfig> result = createQuery.getResultList();
        terminateSession();
        terminateSessionFactory();
        return result;
    }

}
