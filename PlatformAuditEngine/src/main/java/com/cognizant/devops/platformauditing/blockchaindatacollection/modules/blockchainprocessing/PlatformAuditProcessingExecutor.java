/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformauditing.blockchaindatacollection.modules.blockchainprocessing;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.IOException;


public class PlatformAuditProcessingExecutor implements Job {
    private static Logger LOG = LogManager.getLogger(PlatformAuditProcessingExecutor.class);


    private long lastTimestamp;
    private String blockchainProcessedFlag = "blockchainProcessedFlag";
    private int dataBatchSize = 100;
    private int nextBatchSize = 0;
    private final InsightsAuditImpl insightAuditImpl = util.getAuditObject();
	private static util utilObj = new util();


    @Override
    public void execute(JobExecutionContext context) {
        LOG.info("Blockchain Processing Executer module is getting executed");
        orphanNodeExtraction();
    }

    private void orphanNodeExtraction() {
        Neo4jDBHandler dbHandler = new Neo4jDBHandler();
        StringBuffer cypher = new StringBuffer();
        cypher.append("MATCH (n:DATA) WHERE ");
        cypher.append("NOT \"JIRA\" IN labels(n) AND ");
        cypher.append("(NOT EXISTS (n.").append(blockchainProcessedFlag).append(") ");
        cypher.append("OR n.").append(blockchainProcessedFlag).append("=false) ");

        try {
            boolean nextBatchQuery = true;
            while (nextBatchQuery) {
                Boolean successfulWriteFlag = true;
                StringBuffer cypherPickUpTime = new StringBuffer();
                cypherPickUpTime.append("RETURN distinct(n) ORDER BY n.inSightsTime ");
                StringBuffer cypherSkip = new StringBuffer();
                cypherSkip.append(" skip ").append(nextBatchSize);
                cypherSkip.append(" limit ").append(dataBatchSize);
                GraphResponse response = dbHandler.executeCypherQuery(cypher.toString() + cypherPickUpTime.toString() + cypherSkip.toString());
                JsonArray rows = response.getJson()
                        .get("results").getAsJsonArray().get(0).getAsJsonObject()
                        .get("data").getAsJsonArray();
                for (JsonElement dataElem : rows) {
                    successfulWriteFlag = insertNode(dataElem, successfulWriteFlag);
                }

                //check for success for updating tracking

                if (successfulWriteFlag && lastTimestamp != 0) {
                    JsonObject tracking = new JsonObject();
                    tracking.addProperty("insightsTime", lastTimestamp);
                    utilObj.writeTracking(tracking);
                }
                int processedRecords = rows.size();
                nextBatchSize += dataBatchSize;
                if (processedRecords == 0) {
                    nextBatchSize = 0;
                    nextBatchQuery = false;

                }

            }
        } catch (GraphDBException | IOException e) {
            LOG.error("Error occured while loading the destination data ", e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean insertNode(JsonElement dataElem, boolean successfulWriteFlag) {
        try {
            lastTimestamp = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("inSightsTime").getAsLong();
			boolean result;
			synchronized(insightAuditImpl){
				result = insightAuditImpl.insertToolData(dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject());
			}
            boolean tempFlag = utilObj.updateFlagToNeo4j(result, dataElem.getAsJsonObject());
            if (!tempFlag)
                successfulWriteFlag = false;
        } catch (Exception e) {
            LOG.error("Error occured while inserting changed node ", e);
        }
        return successfulWriteFlag;
    }
}