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
package com.cognizant.devops.platformdal.relationshipconfig;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;

public class RelationshipConfigDAL extends BaseDAL {
	
	private static Logger log = LogManager.getLogger(RelationshipConfigDAL.class);
	
	public List<RelationshipConfiguration> getRelationshipConfig(String releationshipName)
	{
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			Query<RelationshipConfiguration> createQuery = session.createQuery(
					"FROM RelationshipConfiguration RC where RC.relationname=:relationname ",
					RelationshipConfiguration.class);
			createQuery.setParameter("relationname", releationshipName);
			return createQuery.getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	
	}
	
	public List<RelationshipConfiguration> loadRelationshipsFromDatabase()
	{
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			CriteriaQuery<RelationshipConfiguration> cq = session.getCriteriaBuilder()
					.createQuery(RelationshipConfiguration.class);
			cq.from(RelationshipConfiguration.class);
			return session.createQuery(cq).getResultList();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
       
		
	}
	
    public void saveRelationshipConfig(CorrelationConfiguration config)
    {
		try (Session session = getSessionObj()) {
			session.beginTransaction();
			session.save(config);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
    
    }
    
}
