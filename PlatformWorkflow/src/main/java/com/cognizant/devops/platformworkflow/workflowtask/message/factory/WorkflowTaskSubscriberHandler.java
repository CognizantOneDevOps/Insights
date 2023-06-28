/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformworkflow.workflowtask.message.factory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformcommons.mq.core.AWSSQSProvider;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowDataHandler;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;

import jakarta.ws.rs.ProcessingException;

public abstract class WorkflowTaskSubscriberHandler {
	private static final Logger log = LogManager.getLogger(WorkflowTaskSubscriberHandler.class);
	private Channel channel;
	private WorkflowTaskSubscriberHandler responseHandler;
	protected String statusLog = "{}";
	WorkflowDataHandler workflowStateProcess;

	public WorkflowTaskSubscriberHandler(String routingKey)
			throws IOException, InsightsCustomException, TimeoutException, InterruptedException, JMSException {
		responseHandler = this;
		this.workflowStateProcess = new WorkflowDataHandler();
		MessageFactory msgFactory;
		String providerName = ApplicationConfigProvider.getInstance().getMessageQueue().getProviderName();
		if (providerName.equalsIgnoreCase("AWSSQS"))
			msgFactory = new MessageAWSSubsbriberFactory();
		else
			msgFactory = new MessageRabbitMQSubsbriberFactory();
		msgFactory.registerSubscriber(routingKey, responseHandler);
	}

	public abstract void handleTaskExecution(String message) throws IOException;

	/**
	 * Once task subscribe message from RabbitMq, Add record in workflow Execution
	 * history table and update workflow configuration to IN_PROGRESS.
	 * 
	 * @param body
	 * @return
	 */
	private synchronized int workflowTaskPreProcesser(String message) {

		int exectionHistoryId = -1;
		try {
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);

			boolean isWorkflowTaskRetry = (boolean) requestMessage.get(WorkflowUtils.RETRY_JSON_PROPERTY);
			// Add entry in INSIGHTS_WORKFLOW_EXECUTION_ENTITY
			if (isWorkflowTaskRetry) {
				String workflowId = String.valueOf(requestMessage.get("workflowId"));
				exectionHistoryId = Integer.parseInt(String.valueOf(requestMessage.get("exectionHistoryId")));
				workflowStateProcess.updateRetryWorkflowExecutionHistory(exectionHistoryId, workflowId,
						WorkflowTaskEnum.WorkflowStatus.IN_PROGRESS.toString(), "");

			} else {
				exectionHistoryId = workflowStateProcess.saveWorkflowExecutionHistory(requestMessage);
			}
			log.debug("Worlflow Detail ==== workflowTaskPreProcess  message handleDelivery ===== {} ", message);
		} catch (Exception e) {
			log.error("Unable to run workflowTaskPreProcess ", e);
		}
		return exectionHistoryId;
	}

	/**
	 * Once task execution completed then it will update end time and task status in
	 * workflow Execution history and publish messege to next task. If task is last
	 * task then It will also change status in workflow configuration and update
	 * next run time.
	 * 
	 * @param body
	 * @param exectionHistoryId
	 * @param status
	 */
	private synchronized void workflowTaskPostProcesser(String message, int exectionHistoryId, String status) {

		String workflowId = "";
		try {
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);

			workflowId = (String) requestMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID);
			// update complete time in INSIGHTS_WORKFLOW_EXECUTION_ENTITY
			workflowStateProcess.updateWorkflowExecutionHistory(exectionHistoryId, status, statusLog);
			// send message to next task
			if (status.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString())) {
				workflowStateProcess.publishMessageToNextInMQ(requestMessage);
			} else {
				log.error(
						"Worlflow Detail ==== workflowId {} Current execution status not completed Status is {} message is {} ",
						workflowId, status, message);
			}
			// Mq ack message
		} catch (WorkflowTaskInitializationException wtie) {
			log.error("Worlflow Detail ==== failed in workflow task post processer due to ", wtie);
			workflowStateProcess.updateWorkflowDetails(workflowId,
					WorkflowTaskEnum.WorkflowStatus.TASK_INITIALIZE_ERROR.toString(), false);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== failed in workflow task post processer due to exception  ", e);
		}

	}

	/**
	 * If task throws an error then method will update status in workflow Execution
	 * history and workflow configuration
	 * 
	 * @param body
	 * @param exectionHistoryId
	 * @throws Exception
	 */
	private synchronized void workflowTaskErrorHandler(String message, int exectionHistoryId) throws Exception {

		try {
			log.debug("Worlflow Detail ==== Error Handler  ===== {} ", message);
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);
			String workflowId = String.valueOf(requestMessage.get(AssessmentReportAndWorkflowConstants.WORKFLOW_ID));
			workflowStateProcess.updateWorkflowExecutionHistory(exectionHistoryId,
					WorkflowTaskEnum.WorkflowStatus.ERROR.toString(), statusLog);
			workflowStateProcess.updateWorkflowDetails(workflowId, WorkflowTaskEnum.WorkflowTaskStatus.ERROR.toString(),
					false);
		} catch (Exception e) {
			log.error("Worlflow Detail ====  unable to update history and workflow config ", e);
			throw new Exception(e.getMessage());
		}

	}

	public String getStatusLog() {
		return statusLog;
	}

	public void setStatusLog(String statusLog) {
		this.statusLog = statusLog;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] body)
			throws Exception {
		String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
		workflowTaskSubscriberCore(message);
		log.debug("Worlflow Detail ==== handle delivery finally method ");
		getChannel().basicAck(envelope.getDeliveryTag(), false);

	}

	public void onMessage(String routingKey, Message message) throws InsightsCustomException, JMSException {
		String msgBody = ((TextMessage) message).getText();

		try {
			log.debug("Received: {} ", msgBody);
			workflowTaskSubscriberCore(msgBody);
			message.acknowledge();
		}  catch (ProcessingException e) {
			log.error(e);
		} catch (JMSException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
			if (ApplicationConfigProvider.getInstance().getMessageQueue().isEnableDeadLetterExchange()) {
				AWSSQSProvider.publishInDLQ(routingKey, msgBody);
				message.acknowledge();
			}
		}
	}

	private void workflowTaskSubscriberCore(String message) throws Exception {
		int exectionHistoryId = -1;
		setStatusLog("{}");
		try {
			log.debug("Worlflow Detail ==== before  workflowTaskPreProcesser ");
			exectionHistoryId = workflowTaskPreProcesser(message);
			log.debug("Worlflow Detail ==== before  handleTaskExecution ");
			/* return */
			responseHandler.handleTaskExecution(message);
			log.debug("Worlflow Detail ==== after handleTaskExecution");

			workflowTaskPostProcesser(message, exectionHistoryId, WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error in handle delivery  ", e);
			workflowTaskErrorHandler(message, exectionHistoryId);

		}
	}

}