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
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

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
		try (Session session = getSessionObj(); Session deleteSession = getSessionObj()) {
			Query<InsightsWorkflowTaskSequence> createQuery = session.createQuery(
					"FROM InsightsWorkflowTaskSequence a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsWorkflowTaskSequence.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			List<InsightsWorkflowTaskSequence> asssessmentList = createQuery.getResultList();
			List<Integer> listofPrimaryKey = new ArrayList<>();
			for (InsightsWorkflowTaskSequence asssessment : asssessmentList) {
				session.beginTransaction();
				asssessment.setWorkflowConfig(null);
				asssessment.setWorkflowTaskEntity(null);
				listofPrimaryKey.add(asssessment.getId());
				session.save(asssessment);
				session.getTransaction().commit();
			}
			for (InsightsWorkflowTaskSequence asssessment : asssessmentList) {
				deleteSession.beginTransaction();
				deleteSession.delete(asssessment);
				deleteSession.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int historyId = (int) session.save(historyConfig);
			session.getTransaction().commit();
			return historyId;
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

	/**
	 * Method to update record in InsightsWorkflowConfiguration table
	 * 
	 * @param workflowConfig
	 * @return int
	 */
	public int updateWorkflowConfig(InsightsWorkflowConfiguration workflowConfig) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.update(workflowConfig);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId ",
					InsightsWorkflowConfiguration.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<Long> executionIDscreateQuery = session.createQuery(
					"select distinct executionId FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.workflowId = :workflowId ORDER BY executionId DESC",
					Long.class);
			executionIDscreateQuery.setParameter("workflowId", workflowId);
			executionIDscreateQuery.setMaxResults(5);
			List<Long> executionIds = executionIDscreateQuery.getResultList();
			Query<InsightsWorkflowExecutionHistory> resultQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.executionId IN (:executionIDs) ORDER BY executionId DESC",
					InsightsWorkflowExecutionHistory.class);
			resultQuery.setParameterList("executionIDs", executionIds);
			return resultQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowExecutionHistory> createQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.id = :historyId ",
					InsightsWorkflowExecutionHistory.class);
			createQuery.setParameter("historyId", historyId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowExecutionHistory> createQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.workflowId = :workflowId ",
					InsightsWorkflowExecutionHistory.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTask> createQuery = session
					.createQuery("FROM InsightsWorkflowTask TE WHERE TE.taskId = :taskId ", InsightsWorkflowTask.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
			return createQuery.uniqueResult();
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
	public InsightsWorkflowTask getTaskByTaskIdList(Integer taskId) {
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTask> createQuery = session
					.createQuery("FROM InsightsWorkflowTask TE WHERE TE.taskId = :taskId ", InsightsWorkflowTask.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTask> createQuery = session.createQuery("FROM InsightsWorkflowTask TE ",
					InsightsWorkflowTask.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.reoccurence = true",
					InsightsWorkflowConfiguration.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.runImmediate = false  ",
					InsightsWorkflowConfiguration.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.runImmediate = true ",
					InsightsWorkflowConfiguration.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTaskSequence> createQuery = session.createQuery(
					"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId and WTS.sequence=1 ",
					InsightsWorkflowTaskSequence.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTaskSequence> createQuery = session.createQuery(
					"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId ORDER BY sequence ASC ",
					InsightsWorkflowTaskSequence.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTaskSequence> createQuery = session.createQuery(
					"FROM InsightsWorkflowTaskSequence WTS WHERE WTS.workflowConfig.workflowId = :workflowId and WTS.workflowTaskEntity.taskId=:taskId ",
					InsightsWorkflowTaskSequence.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.TASK_ID, taskId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int taskId = (int) session.save(config);
			session.getTransaction().commit();
			return taskId;
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
		try (Session session = getSessionObj()) {
			List<InsightsWorkflowTask> tasks = new ArrayList<>();
			Query<InsightsWorkflowTask> createQuery = session.createQuery(
					"FROM InsightsWorkflowTask RE WHERE RE.workflowType.workflowType = :workflowType",
					InsightsWorkflowTask.class);
			createQuery.setParameter("workflowType", workflowType);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId",
					InsightsWorkflowConfiguration.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			String query = "SELECT IWHU.executionid as executionid,IWHU.starttime as startTime,IWHU.endtime as endTime,IWHU.retrycount as retryCount,IWHU.statuslog as statusLog,"
					+ "IWHU.taskstatus as taskStatus,IWT.description as currentTask FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWHU inner join \"INSIGHTS_WORKFLOW_TASK\" IWT ON IWHU.currenttask=IWT.taskid WHERE executionid IN "
					+ "(SELECT DISTINCT(executionid) FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWH where IWH.workflowid IN"
					+ "(SELECT IWG.workflowid FROM \"INSIGHTS_ASSESSMENT_CONFIGURATION\" ISC "
					+ "inner join \"INSIGHTS_WORKFLOW_CONFIG\" IWG ON ISC.workflowid = IWG.workflowid "
					+ "where ISC.configid=:configId ) ORDER BY executionid DESC limit 5 )"
					+ "order by IWHU.executionid desc,IWHU.starttime";
			Query createQuery = session.createSQLQuery(query).addScalar("executionid", StandardBasicTypes.LONG)
					.addScalar("startTime", StandardBasicTypes.LONG).addScalar("endTime", StandardBasicTypes.LONG)
					.addScalar("retryCount", StandardBasicTypes.INTEGER)
					.addScalar("statusLog", StandardBasicTypes.STRING)
					.addScalar("taskStatus", StandardBasicTypes.STRING)
					.addScalar("currentTask", StandardBasicTypes.STRING).setParameter("configId", assessmentConfigId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			String query = "SELECT IWHU.executionid as executionid,IWHU.starttime as startTime,IWHU.endtime as endTime,IWHU.retrycount as retryCount,IWHU.statuslog as statusLog,"
					+ "IWHU.taskstatus as taskStatus,IWT.description as currentTask "
					+ "FROM \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWHU "
					+ "inner join \"INSIGHTS_WORKFLOW_TASK\" IWT ON IWHU.currenttask=IWT.taskid WHERE executionid IN "
					+ "(SELECT DISTINCT(executionid) "
					+ "FROM  \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWH where IWH.workflowid = :workflowID ORDER BY executionid DESC limit 5 ) "
					+ "order by IWHU.executionid desc,IWHU.starttime";
			Query createQuery = session.createSQLQuery(query).addScalar("executionid", StandardBasicTypes.LONG)
					.addScalar("startTime", StandardBasicTypes.LONG).addScalar("endTime", StandardBasicTypes.LONG)
					.addScalar("retryCount", StandardBasicTypes.INTEGER)
					.addScalar("statusLog", StandardBasicTypes.STRING)
					.addScalar("taskStatus", StandardBasicTypes.STRING)
					.addScalar("currentTask", StandardBasicTypes.STRING).setParameter("workflowID", workflowId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowExecutionHistory> createQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.taskStatus='ERROR' and EH.workflowConfig.isActive = true order by EH.executionId desc",
					InsightsWorkflowExecutionHistory.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.status='TASK_INITIALIZE_ERROR' and WC.isActive = true",
					InsightsWorkflowConfiguration.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowExecutionHistory> createQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.isActive = true and EH.workflowConfig.workflowId=:workflowId and EH.executionId=:latestExecutionId order by EH.endTime desc",
					InsightsWorkflowExecutionHistory.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			createQuery.setParameter("latestExecutionId", latestExecutionId);
			List<InsightsWorkflowExecutionHistory> nextTasks = createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowExecutionHistory> createQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory EH WHERE EH.workflowConfig.isActive = true and EH.workflowConfig.workflowId=:workflowId order by EH.executionId desc",
					InsightsWorkflowExecutionHistory.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			createQuery.setMaxResults(1);
			InsightsWorkflowExecutionHistory nextTasks = createQuery.getSingleResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.isActive = true and WC.status='TASK_INITIALIZE_ERROR' ",
					InsightsWorkflowConfiguration.class);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			Query createQuery = session.createQuery("FROM InsightsWorkflowTask IWT WHERE IWT.mqChannel = :mqChannel",
					InsightsWorkflowTask.class);
			createQuery.setParameter("mqChannel", mqChannel);
			InsightsWorkflowTask workflowTask = (InsightsWorkflowTask) createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTask> createQuery = session.createQuery(
					"FROM InsightsWorkflowTask IWT WHERE IWT.mqChannel = :mqChannel", InsightsWorkflowTask.class);
			createQuery.setParameter("mqChannel", mqChannel);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.delete(history);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsWorkflowExecutionHistory> createQuery = session.createQuery(
					"FROM InsightsWorkflowExecutionHistory a WHERE a.id= :id", InsightsWorkflowExecutionHistory.class);
			createQuery.setParameter("id", id);
			InsightsWorkflowExecutionHistory executionRecord = createQuery.getSingleResult();
			executionRecord.setWorkflowConfig(null);
			session.delete(executionRecord);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTask> createQuery = session.createQuery(
					"FROM InsightsWorkflowTask RE WHERE RE.description = :description", InsightsWorkflowTask.class);
			createQuery.setParameter("description", description);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowTask> createQuery = session
					.createQuery("FROM InsightsWorkflowTask a WHERE a.taskId= :id", InsightsWorkflowTask.class);
			createQuery.setParameter("id", taskId);
			InsightsWorkflowTask executionRecord = createQuery.getSingleResult();
			executionRecord.setWorkflowType(null);
			session.beginTransaction();
			session.delete(executionRecord);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int historyId = (int) session.save(emailHistoryConfig);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsEmailTemplates> createQuery = session.createQuery(
					"FROM InsightsEmailTemplates EH WHERE EH.workflowConfig.workflowId = :workflowId ",
					InsightsEmailTemplates.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.update(emailHistoryConfig);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

	/**
	 * Method to fetch Email Execution History using ExecutionId
	 * 
	 * @param executionId
	 * @return InsightsReportVisualizationContainer
	 */
	public InsightsReportVisualizationContainer getEmailExecutionHistoryByExecutionId(long executionId) {
		try (Session session = getSessionObj()) {
			Query<InsightsReportVisualizationContainer> createQuery = session.createQuery(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.executionId = :executionId ",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter("executionId", executionId);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsReportVisualizationContainer> createQuery = session.createQuery(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId order by EH.executionId desc",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			createQuery.setMaxResults(1);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsReportVisualizationContainer> createQuery = session.createQuery(
					"FROM InsightsReportVisualizationContainer EH WHERE EH.workflowId = :workflowId AND EH.executionId=:executionId",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			createQuery.setParameter("executionId", executionId);
			createQuery.setMaxResults(1);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<InsightsReportVisualizationContainer> createQuery = session.createQuery(
					"FROM InsightsReportVisualizationContainer a WHERE a.workflowId= :workflowId",
					InsightsReportVisualizationContainer.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			List<InsightsReportVisualizationContainer> executionRecords = createQuery.getResultList();
			for (InsightsReportVisualizationContainer insightsReportVisualizationContainer : executionRecords) {
				session.delete(insightsReportVisualizationContainer);

			}
			session.getTransaction().commit();
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
		try (Session session = getSessionObj(); Session deleteSession = getSessionObj()) {
			Query<InsightsEmailTemplates> createQuery = session.createQuery(
					"FROM InsightsEmailTemplates a WHERE a.workflowConfig.workflowId= :workflowId",
					InsightsEmailTemplates.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			List<InsightsEmailTemplates> executionRecords = createQuery.getResultList();
			for (InsightsEmailTemplates insightsEmailTemplates : executionRecords) {
				session.beginTransaction();
				insightsEmailTemplates.setWorkflowConfig(null);
				session.save(insightsEmailTemplates);
				session.getTransaction().commit();
			}
			for (InsightsEmailTemplates insightsEmailTemplates : executionRecords) {
				deleteSession.beginTransaction();
				deleteSession.delete(insightsEmailTemplates);
				deleteSession.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			String query = "SELECT max(IRCV.executionid) as emailexecutionid,max(IWEH.executionid) as workflowexecutionid"
					+ "  FROM \"INSIGHTS_REPORT_VISUALIZATION_CONTAINER\" IRCV inner join \"INSIGHTS_WORKFLOW_EXECUTION_HISTORY\" IWEH on IRCV.workflowid=IWEH.workflowid where IWEH.workflowid=:workflowId";
			Query createQuery = session.createSQLQuery(query).addScalar("emailexecutionid", StandardBasicTypes.LONG)
					.addScalar("workflowexecutionid", StandardBasicTypes.LONG).setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			return createQuery.getResultList();
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			String workflowId = (String) session.save(config);
			session.getTransaction().commit();
			return workflowId;
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
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			int workflowTypeId = (int) session.save(type);
			session.getTransaction().commit();
			return workflowTypeId;
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowType> createQuery = session.createQuery(
					"FROM InsightsWorkflowType IWT WHERE IWT.workflowType = :workflowType", InsightsWorkflowType.class);
			createQuery.setParameter("workflowType", workflowType);
			return createQuery.uniqueResult();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration WC WHERE WC.workflowId = :workflowId ",
					InsightsWorkflowConfiguration.class);
			createQuery.setParameter(AssessmentReportAndWorkflowConstants.WORKFLOW_ID, workflowId);
			InsightsWorkflowConfiguration updateStatus = createQuery.uniqueResult();
			updateStatus.setActive(status);
			session.beginTransaction();
			session.update(updateStatus);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowType> createQuery = session.createQuery("FROM InsightsWorkflowType a WHERE a.id= :id",
					InsightsWorkflowType.class);
			createQuery.setParameter("id", typeId);
			InsightsWorkflowType executionRecord = createQuery.getSingleResult();
			session.beginTransaction();
			session.delete(executionRecord);
			session.getTransaction().commit();
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
		try (Session session = getSessionObj()) {
			Query<InsightsWorkflowConfiguration> createQuery = session.createQuery(
					"FROM InsightsWorkflowConfiguration a WHERE a.workflowId= :workflowId",
					InsightsWorkflowConfiguration.class);
			createQuery.setParameter("workflowId", workflowId);
			InsightsWorkflowConfiguration executionRecord = createQuery.getSingleResult();
			executionRecord.setEmailConfig(null);
			executionRecord.setTaskSequenceEntity(null);
			executionRecord.setAssessmentConfig(null);
			session.beginTransaction();
			session.delete(executionRecord);
			session.getTransaction().commit();
			return PlatformServiceConstants.SUCCESS;
		} catch (Exception e) {
			log.error(e);
			throw e;
		}
	}

}
