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

import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;

public enum QueryEnum {
	
	
	
	
	NEO4J_STANDARD(
			"Match (b:"
			+ ReportEngineUtils.NEO4J_RESULT_LABEL + ") Where b.kpiId= :kpiId "
					+ " and b.executionId=:executionId and b.assessmentId =:assessmentId RETURN b "),
	NEO4J_COMPARISON(
			"MATCH (n:" + ReportEngineUtils.NEO4J_RESULT_LABEL + ") "
			+ "with max(n.executionId) as latestexecutionId " + "MATCH (s:"
			+ ReportEngineUtils.NEO4J_RESULT_LABEL
			+ ") where s.executionId <> latestexecutionId with max(s.executionId) as secondlastexecutionId,latestexecutionId "
			+ "Match (b:" + ReportEngineUtils.NEO4J_RESULT_LABEL
					+ ") where b.executionId in [latestexecutionId,secondlastexecutionId] and b.kpiId =:kpiId "
			+ " return b order by b.executionId desc"),
	NEO4J_THRESHOLD(
			"Match (b:" + ReportEngineUtils.NEO4J_RESULT_LABEL
					+ ") Where b.kpiId=:kpiId and b.executionId = :executionId and b.assessmentId =:assessmentId RETURN b order by b.executionId desc "),
	NEO4J_THRESHOLD_RANGE(
			"Match (b:" + ReportEngineUtils.NEO4J_RESULT_LABEL
					+ ") Where b.kpiId= :kpiId and b.executionId = :executionId and b.assessmentId =:assessmentId  RETURN b order by b.executionId desc  "),
	NEO4J_MINMAX(
			"Match (b:" + ReportEngineUtils.NEO4J_RESULT_LABEL
					+ ") Where b.kpiId= :kpiId and b.executionId = :executionId and b.assessmentId =:assessmentId  RETURN b order by b.executionId desc "),
	
	NEO4J_TREND(
			"Match (b:" + ReportEngineUtils.NEO4J_RESULT_LABEL
					+ ") Where b.kpiId=:kpiId and b.executionId = :executionId and b.assessmentId =:assessmentId RETURN b order by b.executionId desc "),

	NEO4J_VCONTENTQUERY("Match (b:" + ReportEngineUtils.NEO4J_CONTENT_RESULT_LABEL
					+ ") Where b.kpiId= :kpiId and b.executionId = :executionId and b.assessmentId =:assessmentId RETURN b.inferenceText as Text ,b.contentId as contentId"),

	ES_STANDARD(
			" {\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}"),

	ES_COMPARISON(
			"{\"size\": 2,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } } ]}}}"),

	ES_THRESHOLD(
			" {\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}"),

	ES_THRESHOLD_RANGE(
			" {\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}"),

	ES_MINMAX(
			" {\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}"),
	
	ES_VCONTENTQUERY(
			"{\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}"),

	ES_TREND(
			" {\"size\": 400,\"sort\": [{ \"executionId\": \"desc\" }],\"query\": {\"bool\":{ \"must\":[{ \"match\":{ \"kpiId\":%kpiId% } },{ \"match\":{ \"executionId\":%executionId% } } ]}}}");
	
	private String value;

	QueryEnum(String value)
	{
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
