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
package com.cognizant.devops.platformengine.modules.mapper;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.cognizant.devops.platformdal.mapping.projects.ProjectMapping;
import com.cognizant.devops.platformdal.mapping.projects.ProjectMappingDAL;

public class ProjectMapperModule implements Job {
	private static Logger log = Logger.getLogger(ProjectMapperModule.class.getName());

	public void execute(JobExecutionContext context) throws JobExecutionException {
		executeProjectMapping();
	}

	private void executeProjectMapping() {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		List<ProjectMapping> projectMappingList = projectMappingDAL.fetchAllProjectMapping();
		if(projectMappingList != null){
			Neo4jDBHandler graphDBHandler = new Neo4jDBHandler();
			for(ProjectMapping projectMapping : projectMappingList){
				try {
					graphDBHandler.executeCypherQuery(projectMapping.getCypherQuery());
				} catch (GraphDBException e) {
					log.error(e);
				}
			}
		}
	}
}
