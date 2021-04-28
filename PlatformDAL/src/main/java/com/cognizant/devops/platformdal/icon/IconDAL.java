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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.core.BaseDAL;

public class IconDAL extends BaseDAL {
	private static final String ICON_QUERY = "FROM Icon IC WHERE IC.iconId = :iconId";
	private static final String ICONID = "iconId";

	private static Logger log = LogManager.getLogger(IconDAL.class);

	public boolean addEntityData(Icon icon) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ICONID, icon.getIconId());
			List<Icon> resultList = getResultList(ICON_QUERY, Icon.class, parameters);

			Icon iconImg = null;
			if (!resultList.isEmpty()) {
				iconImg = resultList.get(0);
				iconImg.setIconId(icon.getIconId());
				iconImg.setFileName(icon.getFileName());
				iconImg.setImage(icon.getImage());
				iconImg.setImageType(icon.getImageType());
				update(iconImg);
			} else {
				save(icon);
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public Icon fetchEntityData(String iconId) {
		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ICONID, iconId);
			List<Icon> resultList = getResultList(ICON_QUERY, Icon.class, parameters);
			Icon result = new Icon();
			if (!resultList.isEmpty()) {
				result = resultList.get(0);
			}
			return result;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	public boolean deleteEntityData(String iconId) {

		try {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(ICONID, iconId);
			Icon result = getSingleResult(ICON_QUERY, Icon.class, parameters);
			delete(result);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

}
