/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformdal.icon;

import java.util.List;

import org.hibernate.query.Query;

import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.core.BaseDAL;

public class IconDAL extends BaseDAL {

	public boolean addEntityData(Icon icon) {
		Query<Icon> createQuery = getSession().createQuery(
				"FROM Icon IC WHERE IC.iconId = :iconId",
				Icon.class);
		createQuery.setParameter("iconId", icon.getIconId());
		List<Icon> resultList = createQuery.getResultList();
		Icon iconImg = null;
		if(resultList.size()>0){
			iconImg = resultList.get(0);
		}
		getSession().beginTransaction();
		if (iconImg != null) {
			iconImg.setIconId(icon.getIconId());
			iconImg.setFileName(icon.getFileName());
			iconImg.setImage(icon.getImage());
			iconImg.setImageType(icon.getImageType());
			getSession().update(iconImg);
		} else {
			getSession().save(icon);
		}
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}

	public Icon fetchEntityData(String iconId) {
		Query<Icon> createQuery = getSession().createQuery("FROM Icon IC WHERE IC.iconId = :iconId", Icon.class);
		createQuery.setParameter("iconId", iconId);
		Icon result = new Icon();
		try {
			List<Icon> resultList = createQuery.getResultList();
			if(resultList.size()>0){
				result = resultList.get(0);
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Exception while retrieving data" + e);
		}
		terminateSession();
		terminateSessionFactory();
		return result;
	}
	
	public boolean deleteEntityData(String iconId) {
		Query<Icon> createQuery = getSession().createQuery(
				"FROM Icon IC WHERE IC.iconId = :iconId",
				Icon.class);
		createQuery.setParameter("iconId", iconId);
		Icon result = createQuery.getSingleResult();
		getSession().beginTransaction();
		getSession().delete(result);
		getSession().getTransaction().commit();
		terminateSession();
		terminateSessionFactory();
		return true;
	}
	
}
