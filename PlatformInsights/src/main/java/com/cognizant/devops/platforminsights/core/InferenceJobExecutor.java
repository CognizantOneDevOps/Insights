package com.cognizant.devops.platforminsights.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platforminsights.core.avg.AverageActionImpl;
import com.cognizant.devops.platforminsights.core.count.CountActionImpl;
import com.cognizant.devops.platforminsights.core.minmax.MinMaxActionImpl;
import com.cognizant.devops.platforminsights.core.sum.SumActionImpl;
// import
// com.cognizant.devops.platforminsights.core.job.config.Neo4jJobConfiguration;
import com.cognizant.devops.platforminsights.datamodel.Neo4jKPIDefinition;
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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

			List<Neo4jKPIDefinition> jobs = readKPIJobsFromFile();

			for (Neo4jKPIDefinition neo4jJob : jobs) {
				log.debug(" Job Detail " + neo4jJob);
				executeJob(neo4jJob);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			InsightsStatusProvider.getInstance().createInsightStatusNode(
					"Platform Insights Inference  not started job " + e.getMessage(), PlatformServiceConstants.FAILURE);
			throw new JobExecutionException("Platform Insights Inference Application not started " + e.getMessage());
		}
	}

	private List<Neo4jKPIDefinition> readKPIJobsFromFile() {
		JsonElement objObject = null;
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		Type type = new TypeToken<List<Neo4jKPIDefinition>>() {
		}.getType();
		List<Neo4jKPIDefinition> jobs = new ArrayList<Neo4jKPIDefinition>(0);
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			File fileName = new File(classLoader.getResource("kpi_jobs_neo4j.json").getFile());
			Reader jsonFileReader = new FileReader(fileName);
			objObject = parser.parse(jsonFileReader);
			jobs = gson.fromJson(objObject, type);
			log.debug(" jobs  " + jobs.size());
		} catch (IOException e) {
			log.error("Error while reading KPI for Neo4j from file ");
		}
		return jobs;
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

}
