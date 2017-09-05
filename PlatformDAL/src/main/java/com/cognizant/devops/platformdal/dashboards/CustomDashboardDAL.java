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
package com.cognizant.devops.platformdal.dashboards;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.core.BaseDAL;
import com.cognizant.devops.platformdal.user.UserPortfolioEnum;

public class CustomDashboardDAL extends BaseDAL{
	
	public List<CustomDashboard> getCustomDashboard(UserPortfolioEnum portfolio){
		Query<CustomDashboard> createQuery = getSession().createQuery("FROM CustomDashboard C WHERE C.portfolio = :portfolio", CustomDashboard.class);
		createQuery.setParameter("portfolio", portfolio);
		List<CustomDashboard> result = createQuery.getResultList();
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
	public boolean addCustomDashboard(String dashboardName, String dashboardJson, UserPortfolioEnum portfolio){
		CustomDashboard customDashboard = new CustomDashboard();
		customDashboard.setDashboardName(dashboardName);
		customDashboard.setDashboardJson(dashboardJson);
		customDashboard.setPortfolio(portfolio);
		getSession().beginTransaction();
		getSession().save(customDashboard);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
}
