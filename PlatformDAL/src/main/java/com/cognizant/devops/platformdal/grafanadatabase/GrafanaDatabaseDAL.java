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
package com.cognizant.devops.platformdal.grafanadatabase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.cognizant.devops.platformdal.core.BaseDAL;


public class GrafanaDatabaseDAL extends BaseDAL {

	public List<Object[]> fetchDashboardDetails() {
		Map<String,Object> parameters = new HashMap<>();
		Map<String,Type> scalarList = new LinkedHashMap<>();
		scalarList.put("id", StandardBasicTypes.INTEGER);
		scalarList.put("data", StandardBasicTypes.STRING);
		return executeGrafanaSQLQueryAndRetunList("SELECT id,data FROM dashboard",scalarList, parameters);
	}

	public List<Object[]> fetchOrgDetails() {
		Map<String,Type> scalarList = new LinkedHashMap<>();
		scalarList.put("id", StandardBasicTypes.INTEGER);
		scalarList.put("name", StandardBasicTypes.STRING);
		Map<String,Object> parameters = new HashMap<>();
		return executeGrafanaSQLQueryAndRetunList("SELECT id,name FROM org",scalarList, parameters);
	}
	
	public List<Object[]> fetchDashboardDetailsByOrgId(int orgid) {
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("OrgId", orgid);
		Map<String,Type> scalarList = new LinkedHashMap<>();
		scalarList.put("uid", StandardBasicTypes.STRING);
		scalarList.put("title", StandardBasicTypes.STRING);
		return executeGrafanaSQLQueryAndRetunList("SELECT uid,title FROM dashboard where org_id=:OrgId ",scalarList, parameters);
	}
	
	public List<Object[]> fetchDashboardDetailsByUUId(String uid,int orgid) {
		Map<String,Object> parameters = new HashMap<>();
		parameters.put("uid", uid);
		parameters.put("orgid", orgid);
		Map<String,Type> scalarList = new LinkedHashMap<>();
		scalarList.put("uid", StandardBasicTypes.STRING);
		scalarList.put("data", StandardBasicTypes.STRING);
		return executeGrafanaSQLQueryAndRetunList("SELECT uid,data FROM dashboard where uid=:uid and org_id=:orgid ",scalarList, parameters);
	}
	
}