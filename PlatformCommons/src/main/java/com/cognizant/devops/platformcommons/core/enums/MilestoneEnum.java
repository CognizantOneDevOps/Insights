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

public class MilestoneEnum {

	/**
	 * Enums stating the list of Milestone status supported.
	 */
	public enum MilestoneStatus {
		NOT_STARTED, COMPLETED, NODE_CREATED, ERROR, IN_PROGRESS, RESTART
	}
	
	public enum OutcomeStatus {
		
		NOT_STARTED ("Outcome will be picked up soon"),
		IN_PROGRESS ("Outcome data collection is on progress "),
		COMPLETED ("Outcome data collection completed "), 
		SUCCESS ("Outcome data collection completed Successfully "),
		OUTCOME_SENT_TO_AGENT ("Outcome already sent to Agent for data collection"), 
		ERROR ("Outcome data collection in error state"),
		NODE_CREATED ("Outcome data collection node creates"),
		RESTART ("Outcome restarted");
		
		private String value;

		OutcomeStatus(final String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return super.toString();
		}

		void setValue(String value) {
			this.value = value;
		}
		
	}
}