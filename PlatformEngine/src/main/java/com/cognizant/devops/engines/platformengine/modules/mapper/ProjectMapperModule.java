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
package com.cognizant.devops.engines.platformengine.modules.mapper;

import java.util.List;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.mapping.projects.ProjectMapping;
import com.cognizant.devops.platformdal.mapping.projects.ProjectMappingDAL;

public class ProjectMapperModule extends TimerTask {
	private static Logger log = LogManager.getLogger(ProjectMapperModule.class.getName());

	@Override
	public void run() {
		executeProjectMapping();
	}

	private void executeProjectMapping() {
		ProjectMappingDAL projectMappingDAL = new ProjectMappingDAL();
		List<ProjectMapping> projectMappingList = projectMappingDAL.fetchAllProjectMapping();
		if(projectMappingList != null){
			GraphDBHandler graphDBHandler = new GraphDBHandler();
			for(ProjectMapping projectMapping : projectMappingList){
				try {
					graphDBHandler.executeCypherQuery(projectMapping.getCypherQuery());
				} catch (InsightsCustomException e) {
					log.error(e);
				}
			}
		}
		EngineStatusLogger.getInstance().createEngineStatusNode("Project Mapper Module run successfully",PlatformServiceConstants.SUCCESS);
	}
}
