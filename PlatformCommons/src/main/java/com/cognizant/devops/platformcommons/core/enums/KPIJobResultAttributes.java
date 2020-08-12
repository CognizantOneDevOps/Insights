/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformcommons.core.enums;

public enum KPIJobResultAttributes {

	KPIID("kpiId"),
	NAME("name"),
	RESULTS("results"),
	RESULTTIME("resultTime"),
	RESULTTIMEX("resultTimeX"),
	EXPECTEDTREND("expectedTrend"),
	ACTION("action"),
	SCHEDULE("schedule"),
	VECTOR("vector"),
	TOOLNAME("toolName"),
	ISGROUPBY("isGroupBy"),
	GROUPBYFIELDNAME("groupByFieldName"),
	GROUPBYFIELDID("groupByField"),
	GROUPBYFIELDVAL("groupByFieldVal"),
	RESULTOUTPUTTYPE("resultOutPutType"),
	ISCOMPARISIONKPI("isComparisionKpi"),
	COMPARISIONFIELD("comparisionField"),
	ISACTIVE("isActive"),
	AVERAGEFIELD("averageField"),
	ENDTIMEFIELD("endTimeField"),
	NEO4JLABEL("neo4jLabel"),
	NEXTRUNTIME("nextRunTime"),
	STARTTIMEFIELD("startTimeField"),
	TIMEFORMAT("timeFormat"),
	LASTRUNTIME("lastRunTime"),
	NEO4JQUERY("neo4jQuery"),
	RESULTPROPERTYCOUNT("resultPropertyCount"),
	EXCUTEIONID("executionId"),
	RESULTFIELD("resultField");

	private String value;

	KPIJobResultAttributes(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.getValue();
	}
}