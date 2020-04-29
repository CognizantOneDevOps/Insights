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
package com.cognizant.devops.platformdal.correlationConfig;

import java.util.List;
import org.hibernate.query.Query;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class CorrelationConfigDAL extends BaseDAL {

	public Boolean saveCorrelationConfig(CorrelationConfiguration saveCorrelationJson) throws InsightsCustomException {
		Query<CorrelationConfiguration> createQuery = getSession().createQuery(
				"FROM CorrelationConfiguration CC WHERE CC.relationName = :relationName",
				CorrelationConfiguration.class);
		createQuery.setParameter("relationName", saveCorrelationJson.getRelationName());
		List<CorrelationConfiguration> resultList = createQuery.getResultList();
		if (!resultList.isEmpty()) {
			throw new InsightsCustomException("Relation Name already exists.");
		} else {
			getSession().beginTransaction();
			getSession().save(saveCorrelationJson);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
			return Boolean.TRUE;
		}
	}

	public Boolean updateCorrelationConfig(String relationName, Boolean flag) throws InsightsCustomException {
		Query<CorrelationConfiguration> createQuery = getSession().createQuery(
				"FROM CorrelationConfiguration CC WHERE CC.relationName = :relationName",
				CorrelationConfiguration.class);
		createQuery.setParameter("relationName", relationName);
		List<CorrelationConfiguration> resultList = createQuery.getResultList();
		if (resultList.isEmpty()) {
			throw new InsightsCustomException("Unable to update correlation.");
		} else {
			CorrelationConfiguration correlationConfig = resultList.get(0);
			getSession().beginTransaction();
			correlationConfig.setEnableCorrelation(flag);
			getSession().update(correlationConfig);
			getSession().getTransaction().commit();
			terminateSession();
			terminateSessionFactory();
			return Boolean.TRUE;
		}
	}

	public Boolean deleteCorrelationConfig(String relationName) throws InsightsCustomException{
		Query<CorrelationConfiguration> createQuery = getSession().createQuery(
				"FROM CorrelationConfiguration CC WHERE CC.relationName = :relationName",
				CorrelationConfiguration.class);
		createQuery.setParameter("relationName", relationName);
		List<CorrelationConfiguration> resultList = createQuery.getResultList();
		if (resultList.isEmpty()) {
			throw new InsightsCustomException("Unable to update correlation.");
		}
		else {
		CorrelationConfiguration correlationConfig = resultList.get(0);
		getSession().beginTransaction();
		getSession().delete(correlationConfig);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return Boolean.TRUE;
		}
	}

	public List<CorrelationConfiguration> getActiveCorrelations() {
		Query<CorrelationConfiguration> createQuery = getSession().createQuery(
				"FROM CorrelationConfiguration CC WHERE CC.enableCorrelation = true ", CorrelationConfiguration.class);
		List<CorrelationConfiguration> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

	public List<CorrelationConfiguration> getAllCorrelations() {
		Query<CorrelationConfiguration> createQuery = getSession().createQuery("FROM CorrelationConfiguration CC ",
				CorrelationConfiguration.class);
		List<CorrelationConfiguration> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}

}
