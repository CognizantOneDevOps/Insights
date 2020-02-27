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
import org.hibernate.query.Query;
import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;

public class RelationshipConfigDAL extends BaseDAL {
	
	
	public List<RelationshipConfiguration> getRelationshipConfig(String releationshipName)
	{
		getSession().beginTransaction();		
		Query<RelationshipConfiguration> createQuery = getSession().createQuery("FROM RelationshipConfiguration RC where RC.relationname=:relationname ",
				RelationshipConfiguration.class);
		createQuery.setParameter("relationname", releationshipName);		
		List<RelationshipConfiguration> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	
	}
	
	public List<RelationshipConfiguration> loadRelationshipsFromDatabase()
	{
		getSession().beginTransaction();
		CriteriaQuery<RelationshipConfiguration> cq=getSession().getCriteriaBuilder().createQuery(RelationshipConfiguration.class);
		cq.from(RelationshipConfiguration.class);
		List<RelationshipConfiguration> configList =getSession().createQuery(cq).getResultList();     
       	terminateSession();
		terminateSessionFactory();
		return configList;
	}
	
    public void saveRelationshipConfig(CorrelationConfiguration config)
    {
    	getSession().beginTransaction();
    	getSession().save(config);
    	getSession().getTransaction().commit();
    	terminateSession();
		terminateSessionFactory();
    }
    
}
