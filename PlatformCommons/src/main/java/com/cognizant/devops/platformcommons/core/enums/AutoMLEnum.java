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
package com.cognizant.devops.platformcommons.core.enums;

public class AutoMLEnum {

	public enum Status {
		
		LEADERBOARD_READY("Leaderboard_Ready"), MOJO_DEPLOYED("Mojo_Deployed") ,IN_PROGRESS("In_Progress"),
		ERROR("ERROR");
		private String value;

		Status(final String value) {
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
	
public enum PredictionType {
		
		REGRESSION("Regression"), CLASSIFICATION("Classification");
		private String value;

	    PredictionType(final String value) {
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
