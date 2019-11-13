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
package com.cognizant.devops.engines.platformengine.modules.aggregator;

import java.util.HashMap;
import java.util.Map;

public class BusinessMappingData {
	public String toolName; 
	public String businessMappingLabel;
	Map<String,String> propertyMap=new HashMap<String,String>(0);
	
	public String getToolName() {
		return toolName;
	}
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
	public String getBusinessMappingLabel() {
		return businessMappingLabel;
	}
	public void setBusinessMappingLabel(String businessMappingLabel) {
		this.businessMappingLabel = businessMappingLabel;
	}
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}
	
	@Override
	public String toString() {
		return "BusinessMappingData [toolName=" + toolName + ", businessMappingLabel=" + businessMappingLabel
				+ ", propertyMap=" + propertyMap + "]";
	}
	
	

}
