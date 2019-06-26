package com.cognizant.devops.platforminsights.dal;

import java.util.List;
import java.util.Map;

import com.cognizant.devops.platforminsights.datamodel.InferenceConfigDefinition;

public interface DatabaseService {
	public List<Map<String, Object>> getResult();

	public void saveResult(List<Map<String, Object>> resultList);

	public List<InferenceConfigDefinition> readKPIJobs();

	public void updateJobLastRun(List<InferenceConfigDefinition> jobUpdateList);
}
