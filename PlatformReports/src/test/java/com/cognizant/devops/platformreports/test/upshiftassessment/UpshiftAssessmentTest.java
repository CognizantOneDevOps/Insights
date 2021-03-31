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

package com.cognizant.devops.platformreports.test.upshiftassessment;

import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowExecutionHistory;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowType;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformworkflow.workflowtask.core.WorkflowImmediateJobExecutor;
import com.google.gson.JsonArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@Test

public class UpshiftAssessmentTest extends UpshiftAssessmentTestData {
    private static Logger log = LogManager.getLogger(UpshiftAssessmentTest.class);
    WorkflowDAL workflowDAL = new WorkflowDAL();
    int reportTypeId = 0;

    @BeforeClass
    public void onInit() throws Exception {

        reportTypeId = saveWorkflowType("UPSHIFTASSESSMENT");


        saveWorkflowTask(createNodeWorkflow);
        saveWorkflowTask(createRelationshipWorkflow);

        readUpshiftAssessmentFileAndSave("UpshiftAssessment.json", "Test01");

        initializeTask();

        //run immediate workflow executor
        WorkflowImmediateJobExecutor immediateJobExecutor = new WorkflowImmediateJobExecutor();
        immediateJobExecutor.executeImmediateWorkflow();
        Thread.sleep(50000);
    }

    @Test(priority = 1)
    public void testExecutionHistoryUpdateProd() {
        try {
            List<InsightsWorkflowExecutionHistory> executionHistory = workflowDAL
                    .getWorkflowExecutionHistoryRecordsByWorkflowId(workflowId);
            if (executionHistory.size() > 0) {
                for (InsightsWorkflowExecutionHistory eachExecutionRecord : executionHistory) {
                    Assert.assertEquals(eachExecutionRecord.getTaskStatus(),
                            WorkflowTaskEnum.WorkflowStatus.COMPLETED.toString());
                }
            }
        } catch (AssertionError e) {
            log.error(e);
            Assert.fail("testExecutionHistoryUpdateProd ", e);
        }
    }

    @Test(priority = 2)
    public void testReportResult() {
        try {
            String query = "MATCH (n:UPSHIFT) where n.upshiftUuid=\"Test01\" return n";
            Assert.assertNotNull(readNeo4jData(query));
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(priority = 3)
    public void testRelationResult() {
        try {
            String query = "MATCH (n:UPSHIFT:DATA)-[:UPSHIFT_CHILD]->() where n.upshiftUuid=\"Test01\" return n";
            Assert.assertNotNull(readNeo4jData(query));
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }

    public int saveWorkflowType(String workflowtype) {
        int typeId = 0;
        try {
            InsightsWorkflowType workflowTypeObj = workflowDAL
                    .getWorkflowType(workflowtype);
            if (workflowTypeObj == null) {
                InsightsWorkflowType type = new InsightsWorkflowType();
                type.setWorkflowType(workflowtype);
                typeId = workflowDAL.saveWorkflowType(type);
            } else {
                typeId = workflowTypeObj.getId();
            }
        } catch (Exception e) {
            log.error(e);
        }
        return typeId;
    }

    public String readNeo4jData(String query) {
        log.debug(" query executed for Upshift assessment {} ", query);
        GraphDBHandler dbHandler = new GraphDBHandler();
        GraphResponse neo4jResponse;
        String finalJson = null;
        try {
            neo4jResponse = dbHandler.executeCypherQuery(query);
            log.debug(" Upshift assessment  neo4jResponse  {} ", neo4jResponse.getJson());
            JsonArray data = neo4jResponse.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
                    .get("data").getAsJsonArray();
            if (data.size() > 0) {
                finalJson = data.get(0).getAsJsonObject().get("row").toString().replace("[", "").replace("]", "");
            }

        } catch (Exception e) {
            log.error(e);
            return finalJson;
        }
        return finalJson;

    }
}

