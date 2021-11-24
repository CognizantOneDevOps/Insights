/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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

package com.cognizant.devops.platformdal.timertasks;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class InsightsSchedulerTaskDAL extends BaseDAL {
    private static Logger log = LogManager.getLogger(InsightsSchedulerTaskDAL.class);
    
    /** Get all task definition
     * @return
     */
    public List<InsightsSchedulerTaskDefinition> getAllSchedulerTaskConfigurations() {

		try {
			Map<String, Object> parameters = new HashMap<>();
			return getResultList("FROM InsightsSchedulerTaskDefinition STD order by timerTaskId ASC", InsightsSchedulerTaskDefinition.class, parameters);
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
    
    
    /** use to save Task Definition 
     * @param schedulatTaskDefination
     */
    public void saveOrUpdateSchedulerTaskConfiguration(InsightsSchedulerTaskDefinition schedulatTaskDefination) {
        try {
            saveOrUpdate(schedulatTaskDefination);
        } catch (Exception e) {
        	log.error(e);
        	throw e;
        }
    }
    
    /** delete task defination 
     * @param schedulatTaskDefination
     */
    public void deleteTaskConfiguration(InsightsSchedulerTaskDefinition schedulatTaskDefination) {
        try {
            delete(schedulatTaskDefination);
        } catch (Exception e) {
        	log.error(e);
        	throw e;
        }
    }
    
    /** save task history 
     * @param schedulatTaskStatus
     */
    public void saveOrUpdateSchedulerTaskStatus(InsightsSchedulerTaskStatus schedulatTaskStatus) {
        try {
            saveOrUpdate(schedulatTaskStatus);
        } catch (Exception e) {
        	log.error(e);
        	throw e;
        }
    }
    
   
    /** Get latest 50 days History for component by component Name 
     * @param componentName
     * @return
     */
    public List<InsightsSchedulerTaskStatus> getSchedulerTaskHistoryConfigurations(String componentName) {

  		try {
  			Map<String, Object> parameters = new HashMap<>();
  			parameters.put("componentName", componentName);
  			Map<String, Object> extraParameters = new HashMap<>();
  			extraParameters.put("MaxResults", 50);
  			return  executeQueryWithExtraParameter(
					"FROM InsightsSchedulerTaskStatus STS where STS.timerTaskMapping = :componentName order by STS.recordtimestamp DESC",
					InsightsSchedulerTaskStatus.class, parameters, extraParameters);
  		} catch (Exception e) {
  			log.error(e.getMessage());
  			throw e;
  		}
  	}
    
    /** use to get InsightsSchedulerTaskDefinition by component Name
     * @param componentName
     * @return
     */
    public InsightsSchedulerTaskDefinition getSchedulerTaskDefinitionBytaskId(String componentName) {

  		try {
  			Map<String, Object> parameters = new HashMap<>();
  			parameters.put("componentName", componentName);
  			return getUniqueResult("FROM InsightsSchedulerTaskDefinition STD where STD.componentName = :componentName ", InsightsSchedulerTaskDefinition.class, parameters);
  		} catch (Exception e) {
  			log.error(e.getMessage());
  			throw e;
  		}
  	}
    
    /**
	 * Method to get Task last run time based on  Execution History records using taskname
	 * from the database
	 * 
	 * @param assessmentConfigId
	 * @return List<Object[]>
	 */
	public List<Object[]> getTaskLastRunTime() {
		try {
			Map<String,Type> scalarList = new LinkedHashMap<>();
			Map<String,Object> parameters = new HashMap<>();
			String query = "	select max(STS.recordtimestamp) as maxrecordtimestamp ,STS.timerTaskMapping as taskname " + 
					"	from \"INSIGHTS_SCHEDULER_TASK_STATUS\" STS " + 
					"	Group By STS.timerTaskMapping ";
			scalarList.put("maxrecordtimestamp", StandardBasicTypes.LONG);
			scalarList.put("taskname", StandardBasicTypes.STRING);
			return executeSQLQueryAndRetunList(query,scalarList,parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	 /** use to delete Scheduler Task history by using component name 
	 * @param componentName
	 * @return
	 */
	public int deleteSchedulerTaskHistoryConfigurations(String componentName) {
		 try {
	  			Map<String,Object> parameters = new HashMap<>();
				parameters.put("timerTaskMapping", componentName);
	  			String deleteTaskSatus = "delete from InsightsSchedulerTaskStatus STS WHERE STS.timerTaskMapping = :timerTaskMapping ";
	  			return executeUpdate(deleteTaskSatus,parameters);
	  		} catch (Exception e) {
	  			log.error(e.getMessage());
	  			throw e;
	  		}
	  	}
	
}
