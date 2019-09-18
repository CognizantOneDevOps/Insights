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
package com.cognizant.devops.platformdal.mapping.projects;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class ProjectMappingDAL extends BaseDAL{
	
	public List<ProjectMapping> fetchProjectMapping(String projectName, String projectId){
		Query<ProjectMapping> createQuery = getSession().createQuery(
				"FROM ProjectMapping PM WHERE PM.projectName = :projectName AND PM.projectId = :projectId", ProjectMapping.class);
		createQuery.setParameter("projectName", projectName);
		createQuery.setParameter("projectId", projectId);
		List<ProjectMapping> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}
	
	public List<ProjectMapping> fetchProjectMappingByOrgId(int orgId){
		Query<ProjectMapping> createQuery = getSession().createQuery(
				"FROM ProjectMapping PM WHERE PM.orgId = :orgId", ProjectMapping.class);
		createQuery.setParameter("orgId", orgId);
		List<ProjectMapping> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}
	
	
	public List<ProjectMapping> fetchAllProjectMapping(){
		Query<ProjectMapping> createQuery = getSession().createQuery(
				"FROM ProjectMapping PM", ProjectMapping.class);
		List<ProjectMapping> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}
	
	public boolean addProjectMapping(ProjectMapping projectMapping){
		getSession().beginTransaction();
		getSession().save(projectMapping);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

}
