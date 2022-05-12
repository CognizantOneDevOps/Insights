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
import com.cognizant.devops.platformcommons.constants.CommonsAndDALConstants;

public enum QueryEnum {

      NEO4J_STANDARD(
    		  CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_RESULT_LABEL + ") Where b.kpiId= :kpiId "
                              + " and b.executionId=:executionId and b.assessmentReportName =:assessmentReportName RETURN b "),
      NEO4J_COMPARISON(
                  "MATCH (n:" + ReportEngineUtils.NEO4J_RESULT_LABEL + ") "
                  + "Where n.kpiId=:kpiId and n.assessmentReportName =:assessmentReportName with max(n.executionId) as latestexecutionId " + "MATCH (s:"
                  + ReportEngineUtils.NEO4J_RESULT_LABEL
                  + ") where s.kpiId=:kpiId and s.assessmentReportName =:assessmentReportName and s.executionId <> latestexecutionId with max(s.executionId) as secondlastexecutionId,latestexecutionId "
                  + CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_RESULT_LABEL
                              + ") where b.executionId in [latestexecutionId,secondlastexecutionId] and b.kpiId =:kpiId "
                  + " return b order by b.executionId desc"),
      NEO4J_THRESHOLD(
    		  CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_RESULT_LABEL
                              + ") Where b.kpiId=:kpiId and b.executionId = :executionId and b.assessmentReportName =:assessmentReportName RETURN b order by b.executionId desc "),
      NEO4J_THRESHOLD_RANGE(
    		  CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_RESULT_LABEL
                              + ") Where b.kpiId= :kpiId and b.executionId = :executionId and b.assessmentReportName =:assessmentReportName  RETURN b order by b.executionId desc  "),
      NEO4J_MINMAX(
    		  CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_RESULT_LABEL
                              + ") Where b.kpiId= :kpiId and b.executionId = :executionId and b.assessmentReportName =:assessmentReportName  RETURN b order by b.executionId desc "),
      NEO4J_TREND(
    		  CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_RESULT_LABEL
                              + ") Where b.kpiId=:kpiId and b.executionId = :executionId and b.assessmentReportName =:assessmentReportName RETURN b order by b.executionId desc "),
      NEO4J_VCONTENTQUERY(CommonsAndDALConstants.MATCH_B_PATTERN + ReportEngineUtils.NEO4J_CONTENT_RESULT_LABEL
                              + ") Where b.kpiId= :kpiId and b.executionId = :executionId and b.assessmentReportName =:assessmentReportName RETURN b.inferenceText as Text ,b.contentId as contentId"),
      
      ES_STANDARD(CommonsAndDALConstants.MATCH_PATTERN_STRING_1),
      
      ES_COMPARISON(CommonsAndDALConstants.MATCH_PATTERN_STRING),
      
      ES_THRESHOLD(CommonsAndDALConstants.MATCH_PATTERN_STRING_1),
      
      ES_THRESHOLD_RANGE(CommonsAndDALConstants.MATCH_PATTERN_STRING_1),
      
      ES_MINMAX(CommonsAndDALConstants.MATCH_PATTERN_STRING_1),
      
      ES_VCONTENTQUERY(CommonsAndDALConstants.MATCH_PATTERN_STRING_1),
      
      ES_TREND(CommonsAndDALConstants.MATCH_PATTERN_STRING_1);

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