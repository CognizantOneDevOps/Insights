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
package com.cognizant.devops.platformreports.assessment.util;

public class ReportEngineEnum {

	public enum ContentCategory {
		STANDARD, COMPARISON, THRESHOLD, TREND,THRESHOLD_RANGE , MINMAX
	}
	
	public enum KPISentiment {
		POSITIVE("positive"), NEGATIVE("negative"), NEUTRAL("neutral");

		private String value;

		KPISentiment(final String value) {
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

	public enum KPITrends {

		UPWARDS("UPWARDS"), DOWNWARDS("DOWNWARDS"), NOCHANGE("NOCHANGE"), NORESULT("NORESULT");

		private String value;

		KPITrends(final String value) {
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
	
	public enum ExecutionActions {
		AVERAGE, COUNT, MIN,MAX, SUM, PERCENTAGE
	}

	public enum DirectionOfThreshold {
		BELOW, ABOVE
	}

	public enum StatusCode {
		SUCCESS(200), NO_DATA(404), ERROR(500);

		private int value;

		StatusCode(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

	
	}
	
}
