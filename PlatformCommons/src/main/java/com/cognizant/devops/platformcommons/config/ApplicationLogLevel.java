/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformcommons.config;

import java.io.Serializable;

import com.google.gson.JsonObject;

public class ApplicationLogLevel implements Serializable {
	private static final long serialVersionUID = 2405172041950251807L;
	
	private boolean updateLevelTransitiveDependency = Boolean.FALSE;
   
	private transient JsonObject serviceLogLevel = new JsonObject();	 

	public boolean isUpdateLevelTransitiveDependency() {
		return updateLevelTransitiveDependency;
	}

	public void setUpdateLevelTransitiveDependency(boolean updateLevelTransitiveDependency) {
		this.updateLevelTransitiveDependency = updateLevelTransitiveDependency;
	}

	public JsonObject getServiceLogLevel() {
		return serviceLogLevel;
	}

	public void setServiceLogLevel(JsonObject serviceLogLevel) {
		this.serviceLogLevel = serviceLogLevel;
	}

}
