/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
package com.cognizant.devops.platformdal.core;

import java.util.List;
import java.util.Map;

import org.hibernate.type.Type;

public interface IBaseDAL {


	void delete(Object entityObj);

	void saveOrUpdate(Object entityObj);

	Object save(Object entityObj);

	void update(Object entityObj);

	<T> List<T> getResultList(String query, Class<T> type, Map<String,Object> parameters);
		
	<T> T getUniqueResult(String query, Class<T> type, Map<String,Object> parameters);

	<T> T getSingleResult(String query, Class<T> type, Map<String, Object> parameters);

	int executeUpdateWithSQLQuery(String createQuery);

	<T> List<T> executeSQLQueryAndRetunList(String query, Map<String, Type> sclarValues, Map<String, Object> parameters);

	<T> List<T> executeQueryWithExtraParameter(String query, Class<T> type, Map<String, Object> parameters,
			Map<String, Object> extraParameters);

	<T> Object executeUniqueResultQueryWithExtraParameter(String query, Class<T> type, Map<String, Object> parameters,
			Map<String, Object> extraParameters);
	
	<T> List<T> executeGrafanaSQLQueryAndRetunList(String query, Map<String, Type> scalarValues,
			Map<String, Object> parameters);

	int executeUpdate(String query, Map<String, Object> parameters);
	

}
