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
package com.cognizant.devops.platformcommons.constants;

public final class CommonsAndDALConstants {

	public static final String RABBIT_MQ_EXCEPTION = "Exception while initializing RabbitMQ connection.";
	public static final String COLUMN_DEFINITION = "columnDefinition";
	public static final String MAX_RESULTS = "MaxResults";
	public static final String ISACTIVE = "isActive";
	public static final String MATCH_PATTERN_STRING = "{\"size\": 2,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } } ]}}}";
	public static final String MATCH_PATTERN_STRING_1 = " {\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}";
	public static final String MATCH_B_PATTERN="Match (b:";

}
