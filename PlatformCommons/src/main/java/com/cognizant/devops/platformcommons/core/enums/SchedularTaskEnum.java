/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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

public class SchedularTaskEnum {
	
	public enum SchedularTaskAction {
		NOT_STARTED("NOT_STARTED"),
		START("START"),
		STOP("STOP"),
		RESCHEDULE("RESCHEDULE"),
		RESTART("RESTART");

		private String value;

		SchedularTaskAction(final String value) {
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
	
	public enum TaskDefinitionProperty {
		COMPONENTNAME("componentName"),
		COMPONENTCLASSDETAIL("componentClassDetail"),
		SCHEDULE("schedule"),
		ACTION("action");

		private String value;

		TaskDefinitionProperty(final String value) {
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

}
