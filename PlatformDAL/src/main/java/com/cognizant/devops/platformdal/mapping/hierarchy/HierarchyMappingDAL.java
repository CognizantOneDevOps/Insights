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
package com.cognizant.devops.platformdal.mapping.hierarchy;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class HierarchyMappingDAL extends BaseDAL {

	public List<HierarchyMapping> fetchAllHierarchyMapping() {
		Query<HierarchyMapping> createQuery = getSession().createQuery("FROM HierarchyMapping HM",
				HierarchyMapping.class);
		List<HierarchyMapping> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public boolean saveHierarchyMapping(int rowId, String hierarchyName, String orgName, int orgId) {
		Query<HierarchyMapping> createQuery = getSession().createQuery(
				"FROM HierarchyMapping a WHERE a.hierarchyName = :hierarchyName AND a.orgName = :orgName AND a.rowId = :rowId AND a.orgId = :orgId",
				HierarchyMapping.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		createQuery.setParameter("orgName", orgName);
		createQuery.setParameter("rowId", rowId);
		createQuery.setParameter("orgId", orgId);
		List<HierarchyMapping> resultList = createQuery.getResultList();
		HierarchyMapping hierarchyMapping = null;
		if (resultList.size() > 0) {
			hierarchyMapping = resultList.get(0);
		}
		getSession().beginTransaction();
		if (hierarchyMapping == null) {
			hierarchyMapping = new HierarchyMapping();
			hierarchyMapping.setHierarchyName(hierarchyName);
			hierarchyMapping.setOrgName(orgName);
			hierarchyMapping.setRowId(rowId);
			hierarchyMapping.setOrgId(orgId);
			getSession().save(hierarchyMapping);
		}
		// in else need to write update logic
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public List<String> getHierarchyMapping(String hierarchyName) {
		/*Query<HierarchyMapping> createQuery = getSession().createQuery(
				"FROM HierarchyMapping a WHERE a.hierarchyName = :hierarchyName",
				HierarchyMapping.class);*/
		Query<String> createQuery = getSession().createQuery(
				"SELECT HM.orgName FROM HierarchyMapping HM WHERE HM.hierarchyName = :hierarchyName",String.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		List<String> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public boolean deleteHierarchyMapping(String hierarchyName, String orgName) {
		Query<HierarchyMapping> createQuery = getSession().createQuery(
				"FROM HierarchyMapping a WHERE a.hierarchyName = :hierarchyName AND a.orgName = :orgName",
				HierarchyMapping.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		createQuery.setParameter("orgName", orgName);
		HierarchyMapping hierarchyMapping = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(hierarchyMapping);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
}
