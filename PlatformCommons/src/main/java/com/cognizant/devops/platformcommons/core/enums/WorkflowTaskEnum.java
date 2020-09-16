/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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

public class WorkflowTaskEnum {

	/**
	 * Enums stating the list of WORKFLOW status supported.
	 */
	public enum WorkflowStatus {
		NOT_STARTED, IN_PROGRESS, COMPLETED, ERROR, RESTART, ABORTED, TASK_INITIALIZE_ERROR
	}

	public enum WorkflowType {
		REPORT("Report");

		private String value;

		WorkflowType(final String value) {
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

	public enum WorkflowTaskStatus {
		IN_PROGRESS, COMPLETED, ERROR, RETRY_EXCEEDED, ABORTED
	}

	/**
	 * List of the schedules supported by Workflow
	 *
	 */
	public enum WorkflowSchedule {

		ONETIME(0), DAILY(1), WEEKLY(7), BI_WEEKLY_SPRINT(14), TRI_WEEKLY_SPRINT(21), MONTHLY(30), QUARTERLY(90),
		YEARLY(365);

		private int value;

		WorkflowSchedule(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		@Override
		public String toString() {
			return super.toString();
		}

		void setValue(int value) {
			this.value = value;
		}
	}

	public enum EmailStatus {
		NOT_STARTED, IN_PROGRESS, COMPLETED, ERROR
	}

}