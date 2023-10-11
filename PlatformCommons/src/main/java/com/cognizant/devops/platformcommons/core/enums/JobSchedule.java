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
package com.cognizant.devops.platformcommons.core.enums;

public enum JobSchedule {
	HOURLY("3600"), EVERY_6_HOUR("21600"), EVERY_12_HOUR("43200"), DAILY("86400"), WEEKLY("604800"), BI_WEEKLY_SPRINT("bi_weekly_sprint"), TRI_WEEKLY_SPRINT("tri_weekly_sprint"),MONTHLY("2628000"), QUARTERLY("7884000"), YEARLY("yearly"),ONETIME("onetime");

	 private String value;

	 JobSchedule(final String value) {
	        this.value = value;
	    }

	    public String getValue() {
	        return value;
	    }

	    @Override
	    public String toString() {
	        return this.getValue();
	    }

	void setValue(String value) {
		this.value = value;
	}
}
