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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class DataTaggingDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(DataTaggingDAL.class);
	
	
	public boolean addEntityData(DataTagging dataTagging) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.save(dataTagging);
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<DataTagging> fetchEntityData(String hierarchyName) {
		try (Session session = getSessionObj()) {
			Query<DataTagging> createQuery = session
					.createQuery("FROM DataTagging DT WHERE DT.hierarchyName = :hierarchyName", DataTagging.class);
			createQuery.setParameter("hierarchyName", hierarchyName);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
		
	}
	
	public List<DataTagging> fetchEntityDataByLevelName(String levelName) {

		try (Session session = getSessionObj()) {
		Query<DataTagging> createQuery = session.createQuery("SELECT DT.levelName = :levelName from DataTagging DT",DataTagging.class);
		return createQuery.getResultList();	
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public List<String> fetchEntityHierarchyName() {
		try (Session session = getSessionObj()) {
			Query<String> createQuery = session.createQuery("SELECT DISTINCT DT.hierarchyName FROM DataTagging DT",
					String.class);
			List<String> resultList = createQuery.getResultList();
			return resultList;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<DataTagging> fetchAllEntityData() {
		try (Session session = getSessionObj()) {
		Query<DataTagging> createQuery = session.createQuery("FROM DataTagging DT", DataTagging.class);
		return createQuery.getResultList();	
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteEntityData(String hierarchyName) {
		try (Session session = getSessionObj()) {
		Query<DataTagging> createQuery = session.createQuery(
				"FROM DataTagging DT WHERE DT.hierarchyName = :hierarchyName",
				DataTagging.class);
		createQuery.setParameter("hierarchyName", hierarchyName);
		DataTagging dataTagging = createQuery.getSingleResult();
		session.beginTransaction();
		session.delete(dataTagging);
		session.getTransaction().commit();
		return true;
		}catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
}
