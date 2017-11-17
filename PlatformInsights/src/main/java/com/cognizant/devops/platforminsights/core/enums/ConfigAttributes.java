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
package com.cognizant.devops.platforminsights.core.enums;

/**
 * 
 * @author 146414
 * All the high level JSON attribute name will be listed here.
 */
public enum ConfigAttributes {
	ACTION("action"),
	ES_QUERY("esQuery"),
	DURATION("duration"),
	DURATION_FIELD("durationField"),
	START_TIME_FIELD("startTimeField"),
	END_TIME_FIELD("endTimeField"),
	TIME_FORMAT("timeFormat"),
	INCLUDE("include"),
	JOB_CONFIGURATION("jobConfiguration"),
	SCHEDULE("schedule");
	
	
	private ConfigAttributes(String name){
		this.name = name;
	}
	private String name;
	
	public String getAttrName(){
		return name;
	}
}
