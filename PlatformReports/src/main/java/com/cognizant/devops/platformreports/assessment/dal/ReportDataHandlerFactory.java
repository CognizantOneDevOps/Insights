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
package com.cognizant.devops.platformreports.assessment.dal;

public class ReportDataHandlerFactory {

	public static ReportDataHandler getDataSource(String datasource) {
		if (datasource == null) {
			return null;
		}
		if (datasource.equalsIgnoreCase("NEO4J")) {
			return new ReportGraphDataHandler();

		} else if (datasource.equalsIgnoreCase("ElasticSearch")) {
			return new ReportElasticSearchDataHandler();

		}
		return null;
	}

}
