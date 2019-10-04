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
package com.cognizant.devops.platformdal.hierarchy.details;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class HierarchyDetailsDAL extends BaseDAL {

	public boolean addHierarchyDetails(HierarchyDetails hierarchyDetails) {
		getSession().beginTransaction();
		getSession().save(hierarchyDetails);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public List<String> fetchDistinctHierarchyName() {
		Query<String> createQuery = getSession().createQuery("SELECT DISTINCT HD.hierarchyName FROM HierarchyDetails HD",String.class);
		List<String> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public List<HierarchyDetails> fetchAllEntityData() {
		Query<HierarchyDetails> createQuery = getSession().createQuery("FROM HierarchyDetails HD", HierarchyDetails.class);
		List<HierarchyDetails> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public boolean deleteHierarchyDetails(String hierarchyName) {
		Query<HierarchyDetails> createQuery = getSession().createQuery(
				"FROM HierarchyDetails HD WHERE HD.hierarchyName = :hierarchyName",
				HierarchyDetails.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		HierarchyDetails hierarchyDetails = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(hierarchyDetails);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
	
	public boolean addHierarchyDetailsList(List<HierarchyDetails> hiearchyList) {
		getSession().beginTransaction();
		for(HierarchyDetails details:hiearchyList){
			getSession().save(details);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
}
