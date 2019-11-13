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
package com.cognizant.devops.engines.platformengine.modules.users;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.platformengine.modules.EngineAbstractModule;

public class EngineUsersModule extends EngineAbstractModule {
	private static Logger log = LogManager.getLogger(EngineUsersModule.class.getName());
	
	public boolean onboardDefaultUsers(){
		/*List<UserData> users = ApplicationConfigProvider.getInstance().getUsers();
		if(users != null){
			for(UserData user : users){
				JsonArray rolesJson = new JsonArray();
				for(String role : user.getRoles()){
					rolesJson.add(role);
				}
				StringBuffer query = new StringBuffer();
				query.append("MERGE (n:USER { id: '").append(user.getId()).append("'}) SET  n.role='").append(rolesJson.toString()).append("' return n");
				try {
					graphDBHandler.executeCypherQuery(query.toString());
				} catch (GraphDBException e) {
					log.error(e);
				}
			}
		}*/
		return true;
	}
}
