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
package com.cognizant.devops.platformdal.data.tagging;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class DataTaggingDAL extends BaseDAL {

	public boolean addEntityData(DataTagging dataTagging) {
		getSession().beginTransaction();
		getSession().save(dataTagging);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public List<DataTagging> fetchEntityData(String hierarchyName) {
		Query<DataTagging> createQuery = getSession().createQuery(
				"FROM DataTagging DT WHERE DT.hierarchyName = :hierarchyName",
				DataTagging.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		List<DataTagging> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}
	
	public List<DataTagging> fetchEntityDataByLevelName(String levelName) {
		/*Query<DataTagging> createQuery = getSession().createQuery(
				"FROM DataTagging DT WHERE DT.levelName = :levelName",
				DataTagging.class);*/
		Query<DataTagging> createQuery = getSession().createQuery("SELECT DT.levelName = :levelName from DataTagging DT",DataTagging.class);
		List<DataTagging> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}
	
	public List<String> fetchEntityHierarchyName() {
		Query<String> createQuery = getSession().createQuery("SELECT DISTINCT DT.hierarchyName FROM DataTagging DT",String.class);
		List<String> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public List<DataTagging> fetchAllEntityData() {
		Query<DataTagging> createQuery = getSession().createQuery("FROM DataTagging DT", DataTagging.class);
		List<DataTagging> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public boolean deleteEntityData(String hierarchyName) {
		Query<DataTagging> createQuery = getSession().createQuery(
				"FROM DataTagging DT WHERE DT.hierarchyName = :hierarchyName",
				DataTagging.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		DataTagging dataTagging = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(dataTagging);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
}
