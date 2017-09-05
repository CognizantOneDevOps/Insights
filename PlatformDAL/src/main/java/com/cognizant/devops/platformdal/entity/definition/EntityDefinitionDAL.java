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
package com.cognizant.devops.platformdal.entity.definition;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class EntityDefinitionDAL extends BaseDAL {

	public List<EntityDefinition> fetchAllEntityDefination() {
		Query<EntityDefinition> createQuery = getSession().createQuery("FROM EntityDefinition PM ORDER BY PM.levelName",
				EntityDefinition.class);
		List<EntityDefinition> resultList = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return resultList;
	}

	public boolean saveEntityDefinition(int rowId, String levelName, String entityName) {
		Query<EntityDefinition> createQuery = getSession().createQuery(
				"FROM EntityDefinition a WHERE a.levelName = :levelName AND a.entityName = :entityName AND a.rowId = :rowId",
				EntityDefinition.class);
		createQuery.setParameter("levelName", levelName);
		createQuery.setParameter("entityName", entityName);
		createQuery.setParameter("rowId", rowId);
		List<EntityDefinition> resultList = createQuery.getResultList();
		EntityDefinition entityDefinition = null;
		if (resultList.size() > 0) {
			entityDefinition = resultList.get(0);
		}
		getSession().beginTransaction();
		if (entityDefinition == null) {
			entityDefinition = new EntityDefinition();
			entityDefinition.setLevelName(levelName);
			entityDefinition.setEntityName(entityName);
			entityDefinition.setRowId(rowId);
			getSession().save(entityDefinition);
		}
		// in else need to write update logic
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public EntityDefinition getEntityDefinition(String levelName, String entityName) {
		Query<EntityDefinition> createQuery = getSession().createQuery(
				"FROM EntityDefinition a WHERE a.levelName = :levelName AND a.entityName = :entityName",
				EntityDefinition.class);
		createQuery.setParameter("levelName", levelName);
		createQuery.setParameter("entityName", entityName);
		EntityDefinition entityDefinition = null;
		try {
			entityDefinition = createQuery.getSingleResult();
		} catch (Exception e) {
			throw new RuntimeException("Exception while retrieving data"+e);
		}
		terminateSession();
		terminateSessionFactory();
		return entityDefinition;
	}

	public boolean deleteEntityDefinition(String levelName, String entityName) {
		Query<EntityDefinition> createQuery = getSession().createQuery(
				"FROM EntityDefinition a WHERE a.levelName = :levelName AND a.entityName = :entityName",
				EntityDefinition.class);
		createQuery.setParameter("levelName", levelName);
		createQuery.setParameter("entityName", entityName);
		EntityDefinition entityDefinition = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(entityDefinition);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
}
