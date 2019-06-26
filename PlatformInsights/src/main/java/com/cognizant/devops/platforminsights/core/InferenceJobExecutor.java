package com.cognizant.devops.platforminsights.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platforminsights.core.avg.AverageActionImpl;
import com.cognizant.devops.platforminsights.core.count.CountActionImpl;
import com.cognizant.devops.platforminsights.core.minmax.MinMaxActionImpl;
import com.cognizant.devops.platforminsights.core.sum.SumActionImpl;
import com.cognizant.devops.platforminsights.dal.DatabaseService;
import com.cognizant.devops.platforminsights.dal.Neo4jDBImpl;
import com.cognizant.devops.platforminsights.datamodel.InferenceConfigDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;

public class InferenceJobExecutor implements Job, Serializable {
	private static final Logger log = LogManager.getLogger(InferenceJobExecutor.class);
	private static final long serialVersionUID = -4343203101560318074L;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			startExecution();
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"Platform Insights Inference Application started Successfully", PlatformServiceConstants.SUCCESS);
		} catch (JobExecutionException e) {
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"message Exception occur while Job Execution " + e.getMessage(), PlatformServiceConstants.FAILURE);
		}
	}

	private void startExecution() throws JobExecutionException {
		log.debug("Starting Spark Jobs Execution");
		try {
			DatabaseService neo4jImpl = new Neo4jDBImpl();
			List<InferenceConfigDefinition> jobsFromNeo4j = neo4jImpl.readKPIJobs();
			List<InferenceConfigDefinition> updatedJobs = new ArrayList<InferenceConfigDefinition>();
			//log.debug("  jobsFromNeo4j " + jobsFromNeo4j);
			for (InferenceConfigDefinition neo4jJob : jobsFromNeo4j) {
				try {
					if (!(neo4jJob.isActive()
							&& isJobScheduledToRun(neo4jJob.getLastRunTime(), neo4jJob.getSchedule().toString()))) {
						log.debug(" Job not run because last run time is greater than scheduled Time"
								+ neo4jJob.getName()
								+ "  kpiId " + neo4jJob.getKpiID());
						continue;
					} else {
						log.debug(" Run Job Detail " + neo4jJob);
						executeJob(neo4jJob);
						updatedJobs.add(neo4jJob);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			if (updatedJobs.size() > 0) {
				neo4jImpl.updateJobLastRun(updatedJobs);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"Platform Insights Inference  not started job " + e.getMessage(), PlatformServiceConstants.FAILURE);
			throw new JobExecutionException("Platform Insights Inference Application not started " + e.getMessage());
		}
	}

	private void executeJob(InferenceConfigDefinition kpiDefinition) throws InsightsJobFailedException {
		log.debug(" KPI action found as ==== " + kpiDefinition.getAction() + " KPI Name is ==== "
				+ kpiDefinition.getName());
		if (!kpiDefinition.getNeo4jQuery().equalsIgnoreCase("")) {
			if (ExecutionActions.AVERAGE == kpiDefinition.getAction()) {
				BaseActionImpl impl = new AverageActionImpl(kpiDefinition);
				impl.execute();
			} else if (ExecutionActions.COUNT == kpiDefinition.getAction()) {
				BaseActionImpl impl = new CountActionImpl(kpiDefinition);
				impl.execute();
			} else if (ExecutionActions.MINMAX == kpiDefinition.getAction()) {
				BaseActionImpl impl = new MinMaxActionImpl(kpiDefinition);
				impl.execute();
			} else if (ExecutionActions.SUM == kpiDefinition.getAction()) {
				BaseActionImpl impl = new SumActionImpl(kpiDefinition);
				impl.execute();
			} else {
				log.error(" No calculation methon defined for KIP " + kpiDefinition.getName() + " With Id "
						+ +kpiDefinition.getKpiID());
			}
		} else {
			log.error(" No neo4j query defined for KPI " + kpiDefinition.getName() + " With Id "
					+ kpiDefinition.getKpiID());
		}
	}
	private boolean isJobScheduledToRun(Long lastRun, String jobSchedule) {
		Long lastRunSinceDays = InsightsUtils.getDurationBetweenDatesInDays(lastRun);
		log.debug(" lastRunSinceDays  " + lastRunSinceDays + " jobSchedule  " + jobSchedule);
		return InsightsUtils.isAfterRange(jobSchedule, lastRunSinceDays);
	}
}
