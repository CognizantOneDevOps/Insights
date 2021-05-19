/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsEmailTemplates;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class WorkflowDAL extends BaseDAL {
	private static final Logger log = LogManager.getLogger(WorkflowDAL.class);

	/**
	 * Method to delete record from InsightsWorkflowTaskSequence table
	 * 
	 * @param workflowId
	 * @return String
	 */
	public String deleteWorkflowTaskSequence(String workflowId) {
		 
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			List<InsightsWorkflowTaskSequence> asssessmentList = getResultList(
					"FROM InsightsWorkflowTaskSequence a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsWorkflowTaskSequence.class,
					parameters);
			List<Integer> listofPrimaryKey = new ArrayList<>();
			for (InsightsWorkflowTaskSequence asssessment : asssessmentList) {
				asssessment.setWorkflowConfig(null);
				asssessment.setWorkflowTaskEntity(null);
				listofPrimaryKey.add(asssessment.getId());
				update(asssessment);
			}
			for (InsightsWorkflowTaskSequence asssessment : asssessmentList) {
				delete(asssessment);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save record to InsightsWorkflowExecutionHistory table
	 * 
	 * @param historyConfig
	 * @return historyID
	 */
	public int saveTaskworkflowExecutionHistory(InsightsWorkflowExecutionHistory historyConfig) {
		try  {
			return (int) save(historyConfig);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update record in InsightsWorkflowExecutionHistory table
	 * 
	 * @param config
	 * @return int
	 */
	public int updateTaskworkflowExecutionHistory(InsightsWorkflowExecutionHistory config) {
		try {
			update(config);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update record in InsightsWorkflowConfiguration table
	 * 
	 * @param workflowConfig
	 * @return int
	 */
	public int updateWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		try {
			update(workflowConfig);
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get workflow config by workflowId
	 * 
	 * @param workflowId
	 * @return InsightsWorkflowConfiguration object
	 */
	public InsightsWorkflowConfiguration getWorkflowByWorkflowId(String workflowId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getUniqueResult(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId ",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Execution History records
	 * 
	 * @param workflowId
	 * @return List<InsightsWorkflowExecutionHistory>
	 */
	public List<InsightsWorkflowExecutionHistory> getWorkflowExecutionHistoryRecordsByWorkflowId(String workflowId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			extraParameters.put("MaxResults", 5);
			List<Long> executionIds =  executeQueryWithExtraParameter(
					"select distinct executionId FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.workflowId = :workflowId ORDER BY executionId DESC",
					Long.class, parameters, extraParameters);
			
			parameters.clear();
			extraParameters.clear();
			
			extraParameters.put("executionIDs", executionIds);
			
			return executeQueryWithExtraParameter(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.executionId IN (:executionIDs) ORDER BY executionId DESC",
					InsightsWorkflowExecutionHistory.class, parameters, extraParameters);
			
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Execution History using HistoryId
	 * 
	 * @param historyId
	 * @return InsightsWorkflowExecutionHistory object
	 */
	public InsightsWorkflowExecutionHistory getWorkflowExecutionHistoryByHistoryId(int historyId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("historyId", historyId);
			return getUniqueResult(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.id = :historyId ",
					InsightsWorkflowExecutionHistory.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Execution History using workflowId
	 * 
	 * @param workflowId
	 * @return List<InsightsWorkflowExecutionHistory>
	 */
	public List<InsightsWorkflowExecutionHistory> getWorkflowExecutionHistoryByWorkflowId(String workflowId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getResultList(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.workflowId = :workflowId ",
					InsightsWorkflowExecutionHistory.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get task using TaskId
	 * 
	 * @param taskId
	 * @return InsightsWorkflowTask object
	 */
	public InsightsWorkflowTask getTaskByTaskId(int taskId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
			return getUniqueResult(
					"FROM InsightsWorkflowTask TE WHERE TE.taskId = :taskId ",
					InsightsWorkflowTask.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Task by TaskID
	 * 
	 * @param taskId
	 * @return InsightsWorkflowTask object
	 */
	@Deprecated
	public InsightsWorkflowTask getTaskByTaskIdList(Integer taskId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
			return getUniqueResult(
					"FROM InsightsWorkflowTask TE WHERE TE.taskId = :taskId ",
					InsightsWorkflowTask.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Workflow Tasks
	 * 
	 * @return List<InsightsWorkflowTask>
	 */
	public List<InsightsWorkflowTask> getAllWorkflowTask() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowTask TE ",
					InsightsWorkflowTask.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Active Workflow Configurations
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getAllActiveWorkflowConfiguration() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.reoccurence = true",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Active and scheduled (not immediate workflow) Workflow
	 * Configurations
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getAllScheduledAndActiveWorkflowConfiguration() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.runImmediate = false  ",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Active Workflow Configurations
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getImmediateWorkflowConfiguration() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.runImmediate = true ",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Task Sequence by WorkflowId
	 * 
	 * @param workflowId
	 * @return InsightsWorkflowTaskSequence object
	 */
	public InsightsWorkflowTaskSequence getWorkflowTaskSequenceByWorkflowId(String workflowId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getUniqueResult(
					"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId and WTS.sequence=1 ",
					InsightsWorkflowTaskSequence.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get all Workflow Task Sequence by WorkflowId
	 * 
	 * @param workflowId
	 * @return List<InsightsWorkflowTaskSequence>
	 */
	public List<InsightsWorkflowTaskSequence> getAllWorkflowTaskSequenceByWorkflowId(String workflowId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getResultList(
					"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId ORDER BY sequence ASC ",
					InsightsWorkflowTaskSequence.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Task Sequence using WorkflowId and TaskId
	 * 
	 * @param workflowId
	 * @param taskId
	 * @return InsightsWorkflowTaskSequence object
	 */
	public InsightsWorkflowTaskSequence getWorkflowTaskSequenceByWorkflowAndTaskId(String workflowId, int taskId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			parameters.put(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
			return getUniqueResult(
					"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId and WTS.workflowTaskEntity.taskId=:taskId ",
					InsightsWorkflowTaskSequence.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * method to save record in INSIGHTS_WORKFLOW_TASK table
	 * 
	 * @param config
	 * @return taskId
	 */
	public int saveInsightsWorkflowTaskConfig(InsightsWorkflowTask config) {
		try  {
			return(int) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get all Tasks
	 * 
	 * @return List<InsightsWorkflowTask>
	 */
	public List<InsightsWorkflowTask> getTaskLists(String workflowType) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowType", workflowType);
			return getResultList(
					"FROM InsightsWorkflowTask RE WHERE RE.workflowType.workflowType = :workflowType",
					InsightsWorkflowTask.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Configuration using workflowId
	 * 
	 * @param workflowId
	 * @return InsightsWorkflowConfiguration object
	 */
	public InsightsWorkflowConfiguration getWorkflowConfigByWorkflowId(String workflowId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getUniqueResult(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Execution History records using assessmentConfigId
	 * from the database
	 * 
	 * @param assessmentConfigId
	 * @return List<Object[]>
	 */
	public List<Object[]> getWorkflowExecutionRecordsbyAssessmentConfigID(int assessmentConfigId) {
		try {
			Map<String,Type> scalarList = new LinkedHashMap<>();
			Map<String,Object> parameters = new HashMap<>();
			String query = "SELECT IWHU.executionid as executionid,IWHU.starttime as startTime,IWHU.endtime as endTime,IWHU.retrycount as retryCount,IWHU.statuslog as statusLog,"
					+ "IWHU.taskstatus as taskStatus,IWT.description as currentTask FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWHU inner join \"INSIGHTS_WORKFLOW_TASK\" IWT ON IWHU.currenttask=IWT.taskid WHERE executionid IN "
					+ "(SELECT DISTINCT(executionid) FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWH where IWH.workflowid IN"
					+ "(SELECT IWG.workflowid FROM \"INSIGHTS_ASSESSMENT_CONFIGURATION\" ISC "
					+ "inner join \"INSIGHTS_WORKFLOW_CONFIG\" IWG ON ISC.workflowid = IWG.workflowid "
					+ "where ISC.configid=:configId ) ORDER BY executionid DESC limit 5 )"
					+ "order by IWHU.executionid desc,IWHU.starttime";
			scalarList.put("executionid", StandardBasicTypes.LONG);
			scalarList.put("startTime", StandardBasicTypes.LONG);
			scalarList.put("endTime", StandardBasicTypes.LONG);
			scalarList.put("retryCount", StandardBasicTypes.INTEGER);
			scalarList.put("statusLog", StandardBasicTypes.STRING);
			scalarList.put("taskStatus", StandardBasicTypes.STRING);
			scalarList.put("currentTask", StandardBasicTypes.STRING);
			parameters.put("configId", assessmentConfigId);
			return executeSQLQueryAndRetunList(query,scalarList,parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Workflow Execution History records using workflowID from the
	 * database
	 * 
	 * @param assessmentConfigId
	 * @return List<Object[]>
	 */
	public List<Object[]> getWorkflowExecutionRecordsByworkflowID(String workflowId) {
		try  {
			Map<String,Type> scalarList = new LinkedHashMap<>();
			Map<String,Object> parameters = new HashMap<>();
			String query = "SELECT IWHU.executionid as executionid,IWHU.starttime as startTime,IWHU.endtime as endTime,IWHU.retrycount as retryCount,IWHU.statuslog as statusLog,"
					+ "IWHU.taskstatus as taskStatus,IWT.description as currentTask "
					+ "FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWHU "
					+ "inner join \"INSIGHTS_WORKFLOW_TASK\" IWT ON IWHU.currenttask=IWT.taskid WHERE executionid IN "
					+ "(SELECT DISTINCT(executionid) "
					+ "FROM  \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWH where IWH.workflowid = :workflowID ORDER BY executionid DESC limit 5 ) "
					+ "order by IWHU.executionid desc,IWHU.starttime";
			scalarList.put("executionid", StandardBasicTypes.LONG);
			scalarList.put("startTime", StandardBasicTypes.LONG);
			scalarList.put("endTime", StandardBasicTypes.LONG);
			scalarList.put("retryCount", StandardBasicTypes.INTEGER);
			scalarList.put("statusLog", StandardBasicTypes.STRING);
			scalarList.put("taskStatus", StandardBasicTypes.STRING);
			scalarList.put("currentTask", StandardBasicTypes.STRING);
			parameters.put("workflowID", workflowId);
			return executeSQLQueryAndRetunList(query,scalarList,parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get records with Error state in Workflow Execution History table
	 * 
	 * @return List<InsightsWorkflowExecutionHistory>
	 */
	public List<InsightsWorkflowExecutionHistory> getErrorExecutionHistoryBasedOnWorflow() {
		try {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.taskStatus='ERROR' and EH.workflowConfig.isActive = true order by EH.executionId desc",
					InsightsWorkflowExecutionHistory.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Error Workflow records
	 * 
	 * @return List<InsightsWorkflowConfiguration> @
	 */
	public List<InsightsWorkflowConfiguration> getCompletedTaskRetryWorkflows() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.status='TASK_INITIALIZE_ERROR' and WC.isActive = true",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get latest Workflow Task by End time
	 * 
	 * @param workflowId
	 * @param latestExecutionId
	 * @return InsightsWorkflowExecutionHistory object
	 */
	public InsightsWorkflowExecutionHistory getLastestTaskByEndTime(String workflowId, long latestExecutionId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			parameters.put("latestExecutionId", latestExecutionId);
			List<InsightsWorkflowExecutionHistory> nextTasks =  getResultList(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.isActive = true and EH.workflowConfig.workflowId=:workflowId and EH.executionId=:latestExecutionId order by EH.endTime desc",
					InsightsWorkflowExecutionHistory.class,
					parameters);
			List<String> errorStatusList = Arrays.asList("ERROR", "ABORTED", "RETRY_EXCEEDED", "IN_PROGRESS");
			if (nextTasks.stream()
					.noneMatch(anyFailedTask -> errorStatusList.contains(anyFailedTask.getTaskStatus()))) {
				return nextTasks.get(0);
			}
			return null;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get latest Execution Id for failed workflow
	 * 
	 * @param workflowId
	 * @return long
	 */
	public long getLastestExecutionIdForFailedWorkflow(String workflowId) {
		try {
			
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			extraParameters.put("MaxResults", 1);
			InsightsWorkflowExecutionHistory nextTasks = (InsightsWorkflowExecutionHistory) executeUniqueResultQueryWithExtraParameter(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.isActive = true and EH.workflowConfig.workflowId=:workflowId order by EH.executionId desc",
					InsightsWorkflowExecutionHistory.class, parameters, extraParameters);
			return nextTasks.getExecutionId();
		} catch (NoResultException ne) {
			return 0;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get workflow configs with status as RESTART
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getAllRestartWorkflows() {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			return getResultList(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.status='TASK_INITIALIZE_ERROR' ",
					InsightsWorkflowConfiguration.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Task using MqChannel
	 * 
	 * @param mqChannel
	 * @return int
	 */
	public int getTaskId(String mqChannel) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("mqChannel", mqChannel);
			InsightsWorkflowTask workflowTask =  getUniqueResult(
					"FROM InsightsWorkflowTask IWT WHERE IWT.mqChannel = :mqChannel",
					InsightsWorkflowTask.class,
					parameters);
			return workflowTask.getTaskId();
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to get Task using MqChannel
	 * 
	 * @param mqChannel
	 * @return InsightsWorkflowTask
	 */
	public InsightsWorkflowTask getTaskByChannel(String mqChannel) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("mqChannel", mqChannel);
			return getUniqueResult(
					"FROM InsightsWorkflowTask IWT WHERE IWT.mqChannel = :mqChannel",
					InsightsWorkflowTask.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Workflow Execution History record
	 * 
	 * @param history
	 * @return String
	 */
	public String deleteExecutionHistory(InsightsWorkflowExecutionHistory history) {
		try {
			delete(history);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Execution History using Id
	 * 
	 * @param id
	 * @return String
	 */
	public String deleteExecutionHistory(int id) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", id);
			InsightsWorkflowExecutionHistory executionRecord = getSingleResult(
					"FROM InsightsWorkflowExecutionHistory a WHERE a.id= :id",
					InsightsWorkflowExecutionHistory.class,
					parameters);
			executionRecord.setWorkflowConfig(null);
			delete(executionRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}

	}

	/**
	 * Method to get Task using Task description
	 * 
	 * @param description
	 * @return InsightsWorkflowTask object
	 */
	public InsightsWorkflowTask getTaskbyTaskDescription(String description) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("description", description);
			return getUniqueResult(
					"FROM InsightsWorkflowTask RE WHERE RE.description = :description",
					InsightsWorkflowTask.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Task using taskId
	 * 
	 * @param taskId
	 * @return String
	 */
	public String deleteTask(int taskId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", taskId);
			InsightsWorkflowTask deleteTask = getSingleResult(
					"FROM InsightsWorkflowTask a WHERE a.taskId= :id",
					InsightsWorkflowTask.class,
					parameters);
			deleteTask.setWorkflowType(null);
			delete(deleteTask);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Email Execution History
	 * 
	 * @param emailHistoryConfig
	 */
	public void saveEmailExecutionHistory(InsightsReportVisualizationContainer emailHistoryConfig) {
		try  {
			 save(emailHistoryConfig);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to fetch Email Template using workflowId
	 * 
	 * @param workflowId
	 * @return InsightsEmailTemplates
	 */
	public InsightsEmailTemplates getEmailTemplateByWorkflowId(String workflowId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return getUniqueResult(
					"FROM InsightsEmailTemplates EH WHERE EH.workflowConfig.workflowId = :workflowId ",
					InsightsEmailTemplates.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update Email Execution History
	 * 
	 * @param emailHistoryConfig
	 */
	public void updateEmailExecutionHistory(InsightsReportVisualizationContainer emailHistoryConfig) {
		update(emailHistoryConfig);
	}

	

	/**
	 * Method to fetch Email Execution History using ExecutionId
	 * 
	 * @param executionId
	 * @return InsightsReportVisualizationContainer
	 */
	public InsightsReportVisualizationContainer getEmailExecutionHistoryByExecutionId(long executionId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("executionId", executionId);
			return getUniqueResult(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.executionId = :executionId ",
					InsightsReportVisualizationContainer.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to fetch Email Execution History using WorkflowId
	 * 
	 * @param workflowId
	 * @return InsightsReportVisualizationContainer
	 */
	public InsightsReportVisualizationContainer getEmailExecutionHistoryByWorkflowId(String workflowId) {
		try  {
			
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			extraParameters.put("MaxResults", 1);
			return (InsightsReportVisualizationContainer) executeUniqueResultQueryWithExtraParameter(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId order by EH.executionId desc",
					InsightsReportVisualizationContainer.class, parameters, extraParameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to fetch Email Execution History using WorkflowId and ExecutionId
	 * 
	 * @param workflowId
	 * @param executionId
	 * @return InsightsReportVisualizationContainer
	 */
	public InsightsReportVisualizationContainer getReportVisualizationContainerByWorkflowAndExecutionId(
			String workflowId, long executionId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			parameters.put("executionId", executionId);
			extraParameters.put("MaxResults", 1);

			return (InsightsReportVisualizationContainer) executeUniqueResultQueryWithExtraParameter(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId AND EH.executionId=:executionId",
					InsightsReportVisualizationContainer.class, parameters, extraParameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Email Execution History using WorkflowId
	 * 
	 * @param workflowId
	 * @return String
	 */
	public String deleteEmailExecutionHistoryByWorkflowId(String workflowId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			List<InsightsReportVisualizationContainer> executionRecords = getResultList(
					"FROM InsightsReportVisualizationContainer a WHERE a.workflowId= :workflowId",
					InsightsReportVisualizationContainer.class,
					parameters);
			
			for (InsightsReportVisualizationContainer insightsReportVisualizationContainer : executionRecords) {
				delete(insightsReportVisualizationContainer);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete Email template record using WorkflowId
	 * 
	 * @param workflowId
	 * @return String
	 * @throws InsightsCustomException
	 */
	public String deleteEmailTemplateByWorkflowId(String workflowId) throws InsightsCustomException {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			List<InsightsEmailTemplates> executionRecords = getResultList(
					"FROM InsightsEmailTemplates a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsEmailTemplates.class,
					parameters);
			
			for (InsightsEmailTemplates insightsEmailTemplates : executionRecords) {
				insightsEmailTemplates.setWorkflowConfig(null);
				save(insightsEmailTemplates);
			}
			for (InsightsEmailTemplates insightsEmailTemplates : executionRecords) {
				delete(insightsEmailTemplates);
			}
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw new InsightsCustomException("Error while deleting email template, Please check log for detail  ");
		}
	}

	/**
	 * Method to fetch Largest ExecutionId from Workflow Execution and Email History
	 * tables
	 * 
	 * @param workflowId
	 * @return List<Object[]>
	 */
	public List<Object[]> getMaxExecutionIDsFromWorkflowExecutionAndReportVisualization(String workflowId) {
		try  {
			Map<String,Type> scalarList = new LinkedHashMap<>();
			Map<String,Object> parameters = new HashMap<>();
			scalarList.put("emailexecutionid", StandardBasicTypes.LONG);
			scalarList.put("workflowexecutionid", StandardBasicTypes.LONG);
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			String query = "SELECT max(IRCV.executionid) as emailexecutionid,max(IWEH.executionid) as workflowexecutionid"
					+ "  FROM \"INSIGHTS_REPORT_VISUALIZATION_CONTAINER\" IRCV inner join \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWEH on IRCV.workflowid=IWEH.workflowid where IWEH.workflowid=:workflowId";
			return executeSQLQueryAndRetunList(query,scalarList,parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Workflow Config record
	 * 
	 * @param config
	 * @return String
	 */
	public String saveInsightsWorkflowConfig(InsightsWorkflowConfiguration config) {
		try  {
			return (String) save(config);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to save Workflow type record
	 * 
	 * @param type
	 * @return int
	 */
	public int saveWorkflowType(InsightsWorkflowType type) {
		try  {
			return(int) save(type);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to fetch WorkflowType object
	 * 
	 * @param workflowType
	 * @return InsightsWorkflowType
	 */
	public InsightsWorkflowType getWorkflowType(String workflowType) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowType", workflowType);
			return getUniqueResult(
					"FROM InsightsWorkflowType IWT WHERE IWT.workflowType = :workflowType",
					InsightsWorkflowType.class,
					parameters);
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to update WorkflowConfig Active state
	 * 
	 * @param workflowId
	 * @param status
	 * @return Boolean
	 */
	public Boolean updateWorkflowConfigActive(String workflowId, Boolean status) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			InsightsWorkflowConfiguration updateStatus =  getUniqueResult(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId ",
					InsightsWorkflowConfiguration.class,
					parameters);
			updateStatus.setActive(status);
			update(updateStatus);
			return Boolean.TRUE;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete WorkflowType
	 * 
	 * @param typeId
	 * @return String
	 */
	public String deleteWorkflowType(int typeId) {
		try {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("id", typeId);
			InsightsWorkflowType executionRecord = getSingleResult(
					"FROM InsightsWorkflowType a WHERE a.id= :id",
					InsightsWorkflowType.class,
					parameters);
			delete(executionRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to delete WorkflowConfiguration record
	 * 
	 * @param workflowId
	 * @return String
	 */
	public String deleteWorkflowConfig(String workflowId) {
		try  {
			Map<String,Object> parameters = new HashMap<>();
			parameters.put("workflowId", workflowId);
			InsightsWorkflowConfiguration executionRecord = getSingleResult(
					"FROM InsightsWorkflowConfiguration a WHERE a.workflowId= :workflowId",
					InsightsWorkflowConfiguration.class,
					parameters);

			executionRecord.setEmailConfig(null);
			executionRecord.setTaskSequenceEntity(null);
			executionRecord.setAssessmentConfig(null);
			delete(executionRecord);
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * Method to fetch Email Execution History using WorkflowId
	 * 
	 * @param workflowId
	 * @return InsightsReportVisualizationContainer
	 */
	public InsightsReportVisualizationContainer getReportVisualizationContainerByWorkflowId(
			String workflowId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			Map<String, Object> extraParameters = new HashMap<>();
			parameters.put(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			extraParameters.put("MaxResults", 1);

			return (InsightsReportVisualizationContainer) executeUniqueResultQueryWithExtraParameter(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId",
					InsightsReportVisualizationContainer.class, parameters, extraParameters);

		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
