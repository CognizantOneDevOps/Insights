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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class HierarchyDetailsDAL extends BaseDAL {

	private static Logger log = LogManager.getLogger(HierarchyDetailsDAL.class);
	public boolean addHierarchyDetails(HierarchyDetails hierarchyDetails) {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.save(hierarchyDetails);
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<String> fetchDistinctHierarchyName() {
		try (Session session = getSessionObj()) {
			Query<String> createQuery = session.createQuery("SELECT DISTINCT HD.hierarchyName FROM HierarchyDetails HD",
					String.class);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public List<HierarchyDetails> fetchAllEntityData() {
		try (Session session = getSessionObj()) {
		Query<HierarchyDetails> createQuery = session.createQuery("FROM HierarchyDetails HD", HierarchyDetails.class);
		return createQuery.getResultList();	
		}
		catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteHierarchyDetails(String hierarchyName) {
		try (Session session = getSessionObj()) {
			Query<HierarchyDetails> createQuery = session.createQuery(
					"FROM HierarchyDetails HD WHERE HD.hierarchyName = :hierarchyName", HierarchyDetails.class);
			createQuery.setParameter("hierarchyName", hierarchyName);
			HierarchyDetails hierarchyDetails = createQuery.getSingleResult();
			session.beginTransaction();
			session.delete(hierarchyDetails);
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	public boolean addHierarchyDetailsList(List<HierarchyDetails> hiearchyList) {
	
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			for (HierarchyDetails details : hiearchyList) {
				session.save(details);
			}
			session.getTransaction().commit();
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
}
