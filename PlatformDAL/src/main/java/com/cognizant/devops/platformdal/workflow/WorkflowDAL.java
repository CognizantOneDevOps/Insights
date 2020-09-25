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
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
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

		Query<InsightsWorkflowTaskSequence> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowTaskSequence a WHERE a.workflowConfig.workflowId= :workflowId",
				InsightsWorkflowTaskSequence.class);
		createQuery.setParameter("workflowId", workflowId);
		List<InsightsWorkflowTaskSequence> asssessmentList = createQuery.getResultList();
		List<Integer> listofPrimaryKey = new ArrayList<>();
		for (InsightsWorkflowTaskSequence asssessment : asssessmentList) {
			getSession().beginTransaction();
			asssessment.setWorkflowConfig(null);
			asssessment.setWorkflowTaskEntity(null);
			listofPrimaryKey.add(asssessment.getId());
			getSession().save(asssessment);
			getSession().getTransaction().commit();
		}

		terminateSession();
		terminateSessionFactory();
		for (InsightsWorkflowTaskSequence asssessment : asssessmentList) {
			getSession().beginTransaction();
			getSession().delete(asssessment);
			getSession().getTransaction().commit();
		}

		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to save record to InsightsWorkflowExecutionHistory table
	 * 
	 * @param historyConfig
	 * @return historyID
	 */
	public int saveTaskworkflowExecutionHistory(InsightsWorkflowExecutionHistory historyConfig) {
		getSession().beginTransaction();
		int historyId = (int) getSession().save(historyConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return historyId;
	}

	/**
	 * Method to update record in InsightsWorkflowExecutionHistory table
	 * 
	 * @param config
	 * @return int
	 */
	public int updateTaskworkflowExecutionHistory(InsightsWorkflowExecutionHistory config) {

		getSession().beginTransaction();
		getSession().update(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return 0;
	}

	/**
	 * Method to update record in InsightsWorkflowConfiguration table
	 * 
	 * @param workflowConfig
	 * @return int
	 */
	public int updateWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		getSession().beginTransaction();
		getSession().update(workflowConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return 0;
	}

	/**
	 * Method to get workflow config by workflowId
	 * 
	 * @param workflowId
	 * @return InsightsWorkflowConfiguration object
	 */
	public InsightsWorkflowConfiguration getWorkflowByWorkflowId(String workflowId) {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId ",
				InsightsWorkflowConfiguration.class);
		createQuery.setParameter("workflowId", workflowId);
		InsightsWorkflowConfiguration result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Workflow Execution History records
	 * 
	 * @param workflowId
	 * @return List<InsightsWorkflowExecutionHistory>
	 */
	public List<InsightsWorkflowExecutionHistory> getWorkflowExecutionHistoryRecordsByWorkflowId(String workflowId) {
		Query<Long> executionIDscreateQuery = getSession().createQuery(
				"select distinct executionId FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.workflowId = :workflowId ORDER BY executionId DESC",
				Long.class);
		executionIDscreateQuery.setParameter("workflowId", workflowId);
		executionIDscreateQuery.setMaxResults(5);
		List<Long> executionIds = executionIDscreateQuery.getResultList();
		Query<InsightsWorkflowExecutionHistory> resultQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory EH WHERE EH.executionId IN (:executionIDs) ORDER BY executionId DESC",
				InsightsWorkflowExecutionHistory.class);
		resultQuery.setParameterList("executionIDs", executionIds);
		List<InsightsWorkflowExecutionHistory> result = resultQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Workflow Execution History using HistoryId
	 * 
	 * @param historyId
	 * @return InsightsWorkflowExecutionHistory object
	 */
	public InsightsWorkflowExecutionHistory getWorkflowExecutionHistoryByHistoryId(int historyId) {
		Query<InsightsWorkflowExecutionHistory> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory EH WHERE EH.id = :historyId ",
				InsightsWorkflowExecutionHistory.class);
		createQuery.setParameter("historyId", historyId);
		InsightsWorkflowExecutionHistory result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Workflow Execution History using workflowId
	 * 
	 * @param workflowId
	 * @return List<InsightsWorkflowExecutionHistory>
	 */
	public List<InsightsWorkflowExecutionHistory> getWorkflowExecutionHistoryByWorkflowId(String workflowId) {
		Query<InsightsWorkflowExecutionHistory> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.workflowId = :workflowId ",
				InsightsWorkflowExecutionHistory.class);
		createQuery.setParameter("workflowId", workflowId);
		List<InsightsWorkflowExecutionHistory> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get task using TaskId
	 * 
	 * @param taskId
	 * @return InsightsWorkflowTask object
	 */
	public InsightsWorkflowTask getTaskByTaskId(int taskId) {
		Query<InsightsWorkflowTask> createQuery = getSession()
				.createQuery("FROM InsightsWorkflowTask TE WHERE TE.taskId = :taskId ", InsightsWorkflowTask.class);
		createQuery.setParameter("taskId", taskId);
		InsightsWorkflowTask result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Task by TaskID
	 * 
	 * @param taskId
	 * @return InsightsWorkflowTask object
	 */
	public InsightsWorkflowTask getTaskByTaskIdList(Integer taskId) {
		Query<InsightsWorkflowTask> createQuery = getSession()
				.createQuery("FROM InsightsWorkflowTask TE WHERE TE.taskId = :taskId ", InsightsWorkflowTask.class);
		createQuery.setParameter("taskId", taskId);
		InsightsWorkflowTask result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get all Workflow Tasks
	 * 
	 * @return List<InsightsWorkflowTask>
	 */
	public List<InsightsWorkflowTask> getAllWorkflowTask() {
		Query<InsightsWorkflowTask> createQuery = getSession().createQuery("FROM InsightsWorkflowTask TE ",
				InsightsWorkflowTask.class);
		List<InsightsWorkflowTask> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get all Active Workflow Configurations
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getAllActiveWorkflowConfiguration() {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true ", InsightsWorkflowConfiguration.class);
		List<InsightsWorkflowConfiguration> configList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return configList;
	}

	/**
	 * Method to get all Active and scheduled (not immediate workflow) Workflow
	 * Configurations
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getAllScheduledAndActiveWorkflowConfiguration() {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.runImmediate = false  ",
				InsightsWorkflowConfiguration.class);
		List<InsightsWorkflowConfiguration> configList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return configList;
	}

	/**
	 * Method to get all Active Workflow Configurations
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getImmediateWorkflowConfiguration() {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.runImmediate = true ",
				InsightsWorkflowConfiguration.class);
		List<InsightsWorkflowConfiguration> configList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return configList;
	}

	/**
	 * Method to get Workflow Task Sequence by WorkflowId
	 * 
	 * @param workflowId
	 * @return InsightsWorkflowTaskSequence object
	 */
	public InsightsWorkflowTaskSequence getWorkflowTaskSequenceByWorkflowId(String workflowId) {
		Query<InsightsWorkflowTaskSequence> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId and WTS.sequence=1 ",
				InsightsWorkflowTaskSequence.class);
		createQuery.setParameter("workflowId", workflowId);
		InsightsWorkflowTaskSequence result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get all Workflow Task Sequence by WorkflowId
	 * 
	 * @param workflowId
	 * @return List<InsightsWorkflowTaskSequence>
	 */
	public List<InsightsWorkflowTaskSequence> getAllWorkflowTaskSequenceByWorkflowId(String workflowId) {
		Query<InsightsWorkflowTaskSequence> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId ORDER BY sequence ASC ",
				InsightsWorkflowTaskSequence.class);
		createQuery.setParameter("workflowId", workflowId);
		List<InsightsWorkflowTaskSequence> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * Method to get Workflow Task Sequence using WorkflowId and TaskId
	 * 
	 * @param workflowId
	 * @param taskId
	 * @return InsightsWorkflowTaskSequence object
	 */
	public InsightsWorkflowTaskSequence getWorkflowTaskSequenceByWorkflowAndTaskId(String workflowId, int taskId) {
		Query<InsightsWorkflowTaskSequence> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId and WTS.workflowTaskEntity.taskId=:taskId ",
				InsightsWorkflowTaskSequence.class);
		createQuery.setParameter("workflowId", workflowId);
		createQuery.setParameter("taskId", taskId);
		InsightsWorkflowTaskSequence result = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	/**
	 * method to save record in INSIGHTS_WORKFLOW_TASK table
	 * 
	 * @param config
	 * @return taskId
	 */
	public int saveInsightsWorkflowTaskConfig(InsightsWorkflowTask config) {
		getSession().beginTransaction();
		int taskId = (int) getSession().save(config);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return taskId;

	}

	/**
	 * Method to get all Tasks
	 * 
	 * @return List<InsightsWorkflowTask>
	 */
	public List<InsightsWorkflowTask> getTaskLists() {
		List<InsightsWorkflowTask> tasks = new ArrayList<>();
		Query<InsightsWorkflowTask> createQuery = getSession().createQuery("FROM InsightsWorkflowTask RE",
				InsightsWorkflowTask.class);
		tasks = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return tasks;
	}

	/**
	 * Method to get Workflow Configuration using workflowId
	 * 
	 * @param workflowId
	 * @return InsightsWorkflowConfiguration object
	 */
	public InsightsWorkflowConfiguration getWorkflowConfigByWorkflowId(String workflowId) {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId",
				InsightsWorkflowConfiguration.class);
		createQuery.setParameter("workflowId", workflowId);
		InsightsWorkflowConfiguration wconfig = createQuery.getSingleResult();
		terminateSession();
		terminateSessionFactory();
		return wconfig;
	}

	/**
	 * Method to get Workflow Execution History records using assessmentConfigId
	 * from the database
	 * 
	 * @param assessmentConfigId
	 * @return List<Object[]>
	 */
	public List<Object[]> getWorkflowExecutionRecordsbyAssessmentConfigID(int assessmentConfigId) {
		String query = "SELECT IWHU.executionid as executionid,IWHU.starttime as startTime,IWHU.endtime as endTime,IWHU.retrycount as retryCount,IWHU.statuslog as statusLog,"
				+ "IWHU.taskstatus as taskStatus,IWT.description as currentTask FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWHU inner join \"INSIGHTS_WORKFLOW_TASK\" IWT ON IWHU.currenttask=IWT.taskid WHERE executionid IN "
				+ "(SELECT DISTINCT(executionid) FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWH where IWH.workflowid IN"
				+ "(SELECT IWG.workflowid FROM \"INSIGHTS_ASSESSMENT_CONFIGURATION\" ISC "
				+ "inner join \"INSIGHTS_WORKFLOW_CONFIG\" IWG ON ISC.workflowid = IWG.workflowid "
				+ "where ISC.configid=:configId ) ORDER BY executionid DESC limit 5 )"
				+ "order by IWHU.executionid desc,IWHU.starttime";
		Query createQuery = getSession().createSQLQuery(query).addScalar("executionid", StandardBasicTypes.LONG)
				.addScalar("startTime", StandardBasicTypes.LONG).addScalar("endTime", StandardBasicTypes.LONG)
				.addScalar("retryCount", StandardBasicTypes.INTEGER).addScalar("statusLog", StandardBasicTypes.STRING)
				.addScalar("taskStatus", StandardBasicTypes.STRING).addScalar("currentTask", StandardBasicTypes.STRING)
				.setParameter("configId", assessmentConfigId);
		List<Object[]> records = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return records;
	}

	/**
	 * Method to get records with Error state in Workflow Execution History table
	 * 
	 * @return List<InsightsWorkflowExecutionHistory>
	 */
	public List<InsightsWorkflowExecutionHistory> getErrorExecutionHistoryBasedOnWorflow() {
		Query<InsightsWorkflowExecutionHistory> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory EH WHERE EH.taskStatus='ERROR' and EH.workflowConfig.isActive = true order by EH.executionId desc",
				InsightsWorkflowExecutionHistory.class);
		List<InsightsWorkflowExecutionHistory> failedTasks = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return failedTasks;
	}

	/**
	 * Method to get Error Workflow records
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getCompletedTaskRetryWorkflows() {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.status='TASK_INITIALIZE_ERROR' and WC.isActive = true",
				InsightsWorkflowConfiguration.class);
		List<InsightsWorkflowConfiguration> failedWorkflows = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return failedWorkflows;
	}

	/**
	 * Method to get latest Workflow Task by End time
	 * 
	 * @param workflowId
	 * @param latestExecutionId
	 * @return InsightsWorkflowExecutionHistory object
	 */
	public InsightsWorkflowExecutionHistory getLastestTaskByEndTime(String workflowId, long latestExecutionId) {
		Query<InsightsWorkflowExecutionHistory> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.isActive = true and EH.workflowConfig.workflowId=:workflowId and EH.executionId=:latestExecutionId order by EH.endTime desc",
				InsightsWorkflowExecutionHistory.class);
		createQuery.setParameter("workflowId", workflowId);
		createQuery.setParameter("latestExecutionId", latestExecutionId);
		List<InsightsWorkflowExecutionHistory> nextTasks = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		List<String> errorStatusList = Arrays.asList("ERROR", "ABORTED", "RETRY_EXCEEDED", "IN_PROGRESS");
		if (nextTasks.stream().noneMatch(anyFailedTask -> errorStatusList.contains(anyFailedTask.getTaskStatus()))) {
			return nextTasks.get(0);
		}

		return null;
	}

	/**
	 * Method to get latest Execution Id for failed workflow
	 * 
	 * @param workflowId
	 * @return long
	 */
	public long getLastestExecutionIdForFailedWorkflow(String workflowId) {
		Query<InsightsWorkflowExecutionHistory> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.isActive = true and EH.workflowConfig.workflowId=:workflowId order by EH.executionId desc",
				InsightsWorkflowExecutionHistory.class);
		createQuery.setParameter("workflowId", workflowId);
		createQuery.setMaxResults(1);
		try {
			InsightsWorkflowExecutionHistory nextTasks = createQuery.getSingleResult();
			return nextTasks.getExecutionId();
		} catch (NoResultException ne) {
			return 0;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}

	}

	/**
	 * Method to get workflow configs with status as RESTART
	 * 
	 * @return List<InsightsWorkflowConfiguration>
	 */
	public List<InsightsWorkflowConfiguration> getAllRestartWorkflows() {
		Query<InsightsWorkflowConfiguration> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.status='TASK_INITIALIZE_ERROR' ",
				InsightsWorkflowConfiguration.class);
		List<InsightsWorkflowConfiguration> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	/**
	 * Method to get Task using MqChannel
	 * 
	 * @param mqChannel
	 * @return int
	 */
	public int getTaskId(String mqChannel) {
		Query createQuery = getSession().createQuery("FROM InsightsWorkflowTask IWT WHERE IWT.mqChannel = :mqChannel",
				InsightsWorkflowTask.class);
		createQuery.setParameter("mqChannel", mqChannel);
		InsightsWorkflowTask workflowTask = (InsightsWorkflowTask) createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return workflowTask.getTaskId();
	}

	/**
	 * Method to delete Execution History using Id
	 * 
	 * @param id
	 * @return String
	 */
	public String deleteExecutionHistory(int id) {
		getSession().beginTransaction();
		Query<InsightsWorkflowExecutionHistory> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowExecutionHistory a WHERE a.id= :id", InsightsWorkflowExecutionHistory.class);
		createQuery.setParameter("id", id);
		InsightsWorkflowExecutionHistory executionRecord = createQuery.getSingleResult();
		executionRecord.setWorkflowConfig(null);
		getSession().delete(executionRecord);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	/**
	 * Method to get Task using Task description
	 * 
	 * @param description
	 * @return InsightsWorkflowTask object
	 */
	public InsightsWorkflowTask getTaskbyTaskDescription(String description) {
		Query<InsightsWorkflowTask> createQuery = getSession().createQuery(
				"FROM InsightsWorkflowTask RE WHERE RE.description = :description", InsightsWorkflowTask.class);
		createQuery.setParameter("description", description);
		InsightsWorkflowTask task = createQuery.uniqueResult();
		terminateSession();
		terminateSessionFactory();
		return task;
	}

	/**
	 * Method to delete Task using taskId
	 * 
	 * @param taskId
	 * @return String
	 */
	public String deleteTask(int taskId) {
		Query<InsightsWorkflowTask> createQuery = getSession()
				.createQuery("FROM InsightsWorkflowTask a WHERE a.taskId= :id", InsightsWorkflowTask.class);
		createQuery.setParameter("id", taskId);
		InsightsWorkflowTask executionRecord = createQuery.getSingleResult();
		executionRecord.setWorkflowType(null);
		getSession().beginTransaction();
		getSession().delete(executionRecord);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return PlatformServiceConstants.SUCCESS;
	}

	public void saveEmailExecutionHistory(InsightsReportVisualizationContainer emailHistoryConfig) {
		getSession().beginTransaction();
		int historyId = (int) getSession().save(emailHistoryConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
	}

	public InsightsEmailTemplates getEmailTemplateByWorkflowId(String workflowId) {
		try {
			Query<InsightsEmailTemplates> createQuery = getSession().createQuery(
					"FROM InsightsEmailTemplates EH WHERE EH.workflowConfig.workflowId = :workflowId ",
					InsightsEmailTemplates.class);
			createQuery.setParameter("workflowId", workflowId);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	public void updateEmailExecutionHistory(InsightsReportVisualizationContainer emailHistoryConfig) {
		getSession().beginTransaction();
		getSession().update(emailHistoryConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
	}

	public InsightsReportVisualizationContainer getEmailExecutionHistoryByExecutionId(long executionId) {
		try {
			Query<InsightsReportVisualizationContainer> createQuery = getSession().createQuery(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.executionId = :executionId ",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter("executionId", executionId);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	public InsightsReportVisualizationContainer getEmailExecutionHistoryByWorkflowId(String workflowId) {
		try {
			Query<InsightsReportVisualizationContainer> createQuery = getSession().createQuery(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId order by EH.executionId desc",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter("workflowId", workflowId);
			createQuery.setMaxResults(1);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	public InsightsReportVisualizationContainer getReportVisualizationContainerByWorkflowAndExecutionId(
			String workflowId, long executionId) {
		try {
			Query<InsightsReportVisualizationContainer> createQuery = getSession().createQuery(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId AND EH.executionId=:executionId",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter("workflowId", workflowId);
			createQuery.setParameter("executionId", executionId);
			createQuery.setMaxResults(1);
			return createQuery.getSingleResult();
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	public String deleteEmailExecutionHistoryByWorkflowId(String workflowId) {
		try {
			getSession().beginTransaction();
			Query<InsightsReportVisualizationContainer> createQuery = getSession().createQuery(
					"FROM InsightsReportVisualizationContainer a WHERE a.workflowId= :workflowId",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter("workflowId", workflowId);
			List<InsightsReportVisualizationContainer> executionRecords = createQuery.getResultList();
			for (InsightsReportVisualizationContainer insightsReportVisualizationContainer : executionRecords) {
				getSession().delete(insightsReportVisualizationContainer);

			}
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {		
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	public String deleteEmailTemplateByWorkflowId(String workflowId) {
		try {
			getSession().beginTransaction();
			Query<InsightsEmailTemplates> createQuery = getSession().createQuery(
					"FROM InsightsEmailTemplates a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsEmailTemplates.class);
			createQuery.setParameter("workflowId", workflowId);
			List<InsightsEmailTemplates> executionRecords = createQuery.getResultList();
			for (InsightsEmailTemplates insightsEmailTemplates : executionRecords) {
				getSession().delete(insightsEmailTemplates);
				getSession().getTransaction().commit();
			}
			terminateSession();
			terminateSessionFactory();
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			return null;
		} finally {
			terminateSession();
			terminateSessionFactory();
		}
	}

	public List<Object[]> getMaxExecutionIDsFromWorkflowExecutionAndReportVisualization(String workflowId) {
		List<Object[]> records = null;
		try {
			String query = "SELECT max(IRCV.executionid) as emailexecutionid,max(IWEH.executionid) as workflowexecutionid"
					+ "  FROM \"INSIGHTS_REPORT_VISUALIZATION_CONTAINER\" IRCV inner join \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWEH on IRCV.workflowid=IWEH.workflowid where IWEH.workflowid=:workflowId";
			Query createQuery = getSession().createSQLQuery(query)
					.addScalar("emailexecutionid", StandardBasicTypes.LONG)
					.addScalar("workflowexecutionid", StandardBasicTypes.LONG).setParameter("workflowId", workflowId);
			records = createQuery.getResultList();
			terminateSession();
			terminateSessionFactory();
		} catch (Exception e) {
			log.error(e);
		}
		return records;
	}

}
