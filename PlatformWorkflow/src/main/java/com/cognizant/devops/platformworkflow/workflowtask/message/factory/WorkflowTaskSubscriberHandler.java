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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowDataHandler;
import com.cognizant.devops.platformworkflow.workflowtask.exception.WorkflowTaskInitializationException;
import com.cognizant.devops.platformworkflow.workflowtask.utils.MQMessageConstants;
import com.cognizant.devops.platformworkflow.workflowtask.utils.WorkflowUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public abstract class WorkflowTaskSubscriberHandler {
	private static final Logger log = LogManager.getLogger(WorkflowTaskSubscriberHandler.class);
	private Channel channel;
	private WorkflowTaskSubscriberHandler engineSubscriberResponseHandler;
	protected String statusLog = "{}";
	WorkflowDataHandler workflowStateProcess;

	public WorkflowTaskSubscriberHandler(String routingKey) throws Exception {
		engineSubscriberResponseHandler = this;
		this.workflowStateProcess = new WorkflowDataHandler();
		registerSubscriber(routingKey);
	}

	public abstract void handleTaskExecution(byte[] body) throws IOException;

	/**
	 * Register workflow task as rabbit mq subscriber
	 * 
	 * @param routingKey
	 * @throws Exception
	 */
	public void registerSubscriber(String routingKey) throws Exception {
		try {
			Channel channel = WorkflowTaskSubscriberFactory.getInstance().getConnection().createChannel();
			String queueName = routingKey.replace(".", "_");
			channel.queueDeclare(queueName, true, false, false, null);
			channel.queueBind(queueName, MQMessageConstants.EXCHANGE_NAME, routingKey);
			channel.basicQos(ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount());
			setChannel(channel);

			log.debug("prefetchCount {} ",
					ApplicationConfigProvider.getInstance().getMessageQueue().getPrefetchCount());
			// Executor shutdown
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					int exectionHistoryId = -1;
					setStatusLog("{}");
					try {

						log.debug("Worlflow Detail ==== before  workflowTaskPreProcesser ");
						exectionHistoryId = workflowTaskPreProcesser(body);
						log.debug("Worlflow Detail ==== before  handleTaskExecution ");
						/* return */
						engineSubscriberResponseHandler.handleTaskExecution(body);
						log.debug("Worlflow Detail ==== after handleTaskExecution");

						workflowTaskPostProcesser(body, exectionHistoryId,
								WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
					} catch (Exception e) {
						log.error("Worlflow Detail ==== Error in handle delivery :", e);
						workflowTaskErrorHandler(body, exectionHistoryId);

					} finally {
						log.debug("Worlflow Detail ==== handle delivery finally method ");
						getChannel().basicAck(envelope.getDeliveryTag(), false);

					}
				}
			};

			channel.basicConsume(queueName, false, routingKey, consumer);
		} catch (IOException e) {
			log.error("Unable to registerSubscriber for routingKey {} error {} ", routingKey, e);
		}
	}

	/**
	 * Unregister workflow task subscriber
	 * 
	 * @param routingKey
	 * @param responseHandler
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public void unregisterSubscriber(String routingKey, final WorkflowTaskSubscriberHandler responseHandler)
			throws IOException, TimeoutException {
		responseHandler.getChannel().basicCancel(routingKey);
		responseHandler.getChannel().close();
	}

	/**
	 * Once task subscribe message from RabbitMq, Add record in workflow Execution
	 * history table and update workflow configuration to IN_PROGRESS.
	 * 
	 * @param body
	 * @return
	 */
	private synchronized int workflowTaskPreProcesser(byte[] body) {
		int exectionHistoryId = -1;
		try {
			String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
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
	private synchronized void workflowTaskPostProcesser(byte[] body, int exectionHistoryId, String status) {
		String workflowId = "";
		try {
			String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);

			log.debug("Worlflow Detail ==== workflowTaskPostProcesser message handleDelivery ===== {} ", message);
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);

			workflowId = (String) requestMessage.get("workflowId");
			// update complete time in INSIGHTS_WORKFLOW_EXECUTION_ENTITY
			workflowStateProcess.updateWorkflowExecutionHistory(exectionHistoryId, status, statusLog);
			// send message to next task
			if (status.equalsIgnoreCase(WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString())) {
				workflowStateProcess.publishMessageToNextInMQ(requestMessage);
			} else {
				log.error("Worlflow Detail ==== Current execution status not completed Status is {} message is {} ",
						status, message);
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
	 */
	private synchronized void workflowTaskErrorHandler(byte[] body, int exectionHistoryId) {
		try {
			String message = new String(body, MQMessageConstants.MESSAGE_ENCODING);
			log.debug("Worlflow Detail ==== Error Handler  ===== {} ", message);
			Map<String, Object> requestMessage = WorkflowUtils.convertJsonObjectToMap(message);
			String workflowId = String.valueOf(requestMessage.get("workflowId"));
			workflowStateProcess.updateWorkflowExecutionHistory(exectionHistoryId,
					WorkflowTaskEnum.WorkflowStatus.ERROR.toString(), statusLog);
			workflowStateProcess.updateWorkflowDetails(workflowId, WorkflowTaskEnum.WorkflowTaskStatus.ERROR.toString(),
					false);
		} catch (Exception e) {
			log.error("Worlflow Detail ====  unable to update history and workflow config", e);
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

}
