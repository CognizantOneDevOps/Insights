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
import com.cognizant.devops.platforminsights.core.function.Neo4jDBImp;
import com.cognizant.devops.platforminsights.core.minmax.MinMaxActionImpl;
import com.cognizant.devops.platforminsights.core.sum.SumActionImpl;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
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
			Neo4jDBImp neo4jImpl = new Neo4jDBImp();
			List<Neo4jKPIDefinition> jobsFromNeo4j = neo4jImpl.readKPIJobsFromNeo4j();
			List<Neo4jKPIDefinition> updatedJobs = new ArrayList<Neo4jKPIDefinition>();
			//log.debug("  jobsFromNeo4j " + jobsFromNeo4j);
			for (Neo4jKPIDefinition neo4jJob : jobsFromNeo4j) {
				try {
					if (!(neo4jJob.isActive()
							&& isJobScheduledToRun(neo4jJob.getLastRunTime(), neo4jJob.getSchedule().toString()))) {
						log.debug(" Job not run because last run time is less than scheduled " + neo4jJob.getName()
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

	private void executeJob(Neo4jKPIDefinition neo4jKpiDefinition) throws InsightsJobFailedException {
		log.debug(" KPI action found as ==== " + neo4jKpiDefinition.getAction() + " KPI Name is ==== "
				+ neo4jKpiDefinition.getName());
		if (!neo4jKpiDefinition.getNeo4jQuery().equalsIgnoreCase("")) {
			if (ExecutionActions.AVERAGE == neo4jKpiDefinition.getAction()) {
				BaseActionImpl impl = new AverageActionImpl(neo4jKpiDefinition);
				impl.executeNeo4jGraphQuery();
			} else if (ExecutionActions.COUNT == neo4jKpiDefinition.getAction()) {
				BaseActionImpl impl = new CountActionImpl(neo4jKpiDefinition);
				impl.executeNeo4jGraphQuery();
			} else if (ExecutionActions.MINMAX == neo4jKpiDefinition.getAction()) {
				BaseActionImpl impl = new MinMaxActionImpl(neo4jKpiDefinition);
				impl.executeNeo4jGraphQuery();
			} else if (ExecutionActions.SUM == neo4jKpiDefinition.getAction()) {
				BaseActionImpl impl = new SumActionImpl(neo4jKpiDefinition);
				impl.executeNeo4jGraphQuery();
			} else {
				log.error(" No calculation methon defined for KIP " + neo4jKpiDefinition.getName() + " With Id "
						+ +neo4jKpiDefinition.getKpiID());
			}
		} else {
			log.error(" No neo4j query defined for KPI " + neo4jKpiDefinition.getName() + " With Id "
					+ neo4jKpiDefinition.getKpiID());
		}
	}
	private boolean isJobScheduledToRun(Long lastRun, String jobSchedule) {
		Long lastRunSinceDays = InsightsUtils.getDurationBetweenDatesInDays(lastRun);
		log.debug(" lastRunSinceDays  " + lastRunSinceDays + " jobSchedule  " + jobSchedule);
		return InsightsUtils.isAfterRange(jobSchedule, lastRunSinceDays);
	}
}
