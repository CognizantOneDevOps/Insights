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
package com.cognizant.devops.platformworkflow.workflowthread.core;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.config.WorkflowDetails;
import com.google.common.collect.Lists;

public class WorkflowThreadPool extends ThreadPoolExecutor {

	private static WorkflowThreadPool workflowThreadPool = null;

	private static final Logger log = LogManager.getLogger(WorkflowThreadPool.class);

	private WorkflowThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);

	}

	public static WorkflowThreadPool getInstance() {
		if (workflowThreadPool == null) {
			WorkflowDetails threadDetails = ApplicationConfigProvider.getInstance().getWorkflowDetails();
			workflowThreadPool = new WorkflowThreadPool(threadDetails.getCorePoolSize(), 
									 threadDetails.getMaximumPoolSize(),threadDetails.getKeepAliveTime(), 
									 TimeUnit.MINUTES,
									 new LinkedBlockingQueue<Runnable>(threadDetails.getWaitingQueueSize()), 
									 new ThreadPoolExecutor.CallerRunsPolicy());	
			log.debug("Worlflow Thread Details ==== Custom ThreadPoolExecutor created successfully");
			}
		return workflowThreadPool;
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		super.afterExecute(r, t);
		if (r instanceof Future<?>) {
			try {
				Object result = ((Future<?>) r).get();
				log.debug("Worlflow Thread Details ==== Task {} Completed ", result);

			} catch (Exception e) {
				log.debug("Worlflow Thread Details ==== Task not completed and end up with exception {} ",
						e.getMessage());
			}
		}
	}

	public static synchronized <T> List<List<Callable<T>>> getChunk(List<Callable<T>> list, int size) {
		return Lists.partition(list, size);
	}

}
