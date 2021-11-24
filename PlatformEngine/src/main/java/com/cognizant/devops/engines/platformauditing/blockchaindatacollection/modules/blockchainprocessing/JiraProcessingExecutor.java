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
package com.cognizant.devops.engines.platformauditing.blockchaindatacollection.modules.blockchainprocessing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Cipher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cognizant.devops.engines.platformengine.message.core.EngineStatusLogger;
import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformauditing.util.LoadFile;
import com.cognizant.devops.platformcommons.config.ApplicationConfigInterface;
import com.cognizant.devops.platformcommons.constants.InsightsAuditConstants;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class JiraProcessingExecutor implements Job, ApplicationConfigInterface {
	
	private static Logger LOG = LogManager.getLogger(JiraProcessingExecutor.class);
	private String blockchainProcessedFlag = "blockchainProcessedFlag";
	private long lastTimestamp;
	private JsonObject config = LoadFile.getInstance().getConfig();
	private int dataBatchSize = config.get("dataBatchSize").getAsInt();
	private int nextBatchSize = 0;
	private final InsightsAuditImpl insightAuditImpl = Util.getAuditObject();
	private static Util utilObj = new Util();
    private String decryptionAlgorithm = config.get("decryptionAlgorithm").getAsString();
    private String keyAlgorithm = config.get("keyAlgorithm").getAsString();
    String jobName="";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOG.info("Blockchain Processing Executer jira module is getting executed");
		long startTime =System.currentTimeMillis();
		try {
			jobName=context.getJobDetail().getKey().getName();
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("JiraProcessingExecutor execution Start ",
					PlatformServiceConstants.SUCCESS,jobName);
			ApplicationConfigInterface.loadConfiguration();
			JiraNodeExtraction();
		} catch (InsightsCustomException e) {
			LOG.error(e);
			EngineStatusLogger.getInstance().createSchedularTaskStatusNode("JiraProcessingExecutor execution has some issue  ",
					PlatformServiceConstants.FAILURE,jobName);
		}
		
		long processingTime = System.currentTimeMillis() - startTime ;
		EngineStatusLogger.getInstance().createSchedularTaskStatusNode("JiraProcessingExecutor execution Completed",
				PlatformServiceConstants.SUCCESS,jobName,processingTime);
		
	}

	private void JiraNodeExtraction() {
		LOG.debug("into jira node extract");		
		GraphDBHandler dbHandler = new GraphDBHandler();
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (n:DATA) WHERE ");
		cypher.append("\"JIRA\" IN labels(n) AND ");
		cypher.append("NOT \"SPRINT\" IN labels(n) AND ");
		cypher.append("(NOT EXISTS (n.").append(blockchainProcessedFlag).append(") ");
		cypher.append("OR n.").append(blockchainProcessedFlag).append(" = false) ");

		try {
			LOG.debug("into try block");
			boolean nextBatchQuery = true;
			while (nextBatchQuery) {
				Boolean successfulWriteFlag = true;
				StringBuffer cypherPickUpTime = new StringBuffer();

				cypherPickUpTime.append("RETURN distinct(n) ORDER BY n.inSightsTime,n.changeDateEpoch");
				StringBuffer cypherSkip = new StringBuffer();
				cypherSkip.append(" skip ").append(nextBatchSize);
				cypherSkip.append(" limit ").append(dataBatchSize);
				GraphResponse response = dbHandler
						.executeCypherQuery(cypher.toString() + cypherPickUpTime.toString() + cypherSkip.toString());
				JsonArray rows = response.getJson().get(InsightsAuditConstants.RESULTS).getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray();
				LOG.debug(rows);
				for (JsonElement dataElem : rows) {
					if(dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has(InsightsAuditConstants.DIGITALSIGNATURE) && (dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("key") || dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("issueKey"))) {
						String digitalSign = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.DIGITALSIGNATURE).getAsString();
						LOG.debug("--------------------------------jira executer-----------------");
						String hc = getHash(dataElem.getAsJsonObject().get("row").getAsJsonArray());
						LOG.debug(hc);
						//decrypt digitalSign
						byte[] byteArray = Base64.getDecoder().decode(digitalSign.getBytes());						
						String keyStr = new String(Files.readAllBytes(Paths.get(config.get("ENGINE_PRIVATE_KEY").getAsString())));
						keyStr = keyStr.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
						KeyFactory key = KeyFactory.getInstance(keyAlgorithm);
						PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyStr));
						PrivateKey k = key.generatePrivate(spec);
						Cipher cipher = Cipher.getInstance(decryptionAlgorithm);
						cipher.init(Cipher.DECRYPT_MODE, k);

						byte[] decryptedBytes = cipher.doFinal(byteArray);
						LOG.debug("decryptedBytes");
						LOG.debug(new String(decryptedBytes));
						if(hc.equals(new String(decryptedBytes)))
							successfulWriteFlag = insertJiraNodes(dataElem, successfulWriteFlag);
						else
							LOG.debug("Hash values do not match.. skipping uuid: " +
									dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uuid"));
					}else
						LOG.debug("INVALID ASSET OR DigitalSignature not found for uuid: "+
							dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uuid")+"\nNode skipped...");
                    //successfulWriteFlag = insertJiraNodes(dataElem, successfulWriteFlag);
				}

				// check for success for updating tracking

				if (successfulWriteFlag && lastTimestamp != 0) {
					JsonObject tracking = new JsonObject();
					tracking.addProperty("jiraTimestamp", lastTimestamp);
					
					utilObj.writeTracking(tracking);
				}
				int processedRecords = rows.size();
				nextBatchSize += dataBatchSize;
				if (processedRecords == 0) {
					nextBatchSize = 0;
					nextBatchQuery = false;

				}

			}
		} catch (InsightsCustomException | IOException e) {
			LOG.error("Error occured while loading the destination data ", e);
		} catch (Exception e) {			
			LOG.error(e);
		}

	}
	
    private String getHash(JsonArray row) {
        String dataString = "";
        HashCode sb = null;        
        if(row.get(0).getAsJsonObject().has("changeId")){
            if(!row.get(0).getAsJsonObject().has("from"))
                row.get(0).getAsJsonObject().addProperty("from","None");
            if(!row.get(0).getAsJsonObject().has("fromString"))
                row.get(0).getAsJsonObject().addProperty("fromString","None");
        }
        for (JsonElement dt : row) {
            //put dataObject into treemap to get sorted
        	//LOG.debug(dt);
            TreeMap<String, String> t = new TreeMap<String, String>();
            dt.getAsJsonObject().entrySet().parallelStream().forEach(entry -> {
                if (!entry.getKey().equals("uuid") && !entry.getKey().equals(InsightsAuditConstants.DIGITALSIGNATURE)) {
                    if (entry.getKey().equals("inSightsTime") || entry.getKey().endsWith("Epoch")) {
                    	LOG.debug("has epoch time");
                        t.put(entry.getKey(), String.valueOf(entry.getValue().getAsLong()) + ".0");
                    }
					else {
                    	LOG.debug("jsonarray*****"+Boolean.toString(entry.getValue().isJsonArray()));
                        if(entry.getValue().isJsonArray()) {
                            for (JsonElement e: entry.getValue().getAsJsonArray()) {
                                if(t.containsKey(entry.getKey())) {
                                    LOG.debug("t.get(entry.getKey()).toString()");
                                    LOG.debug(t.get(entry.getKey()).toString());
                                    t.put(entry.getKey(), t.get(entry.getKey()).toString().concat(e.getAsString()));
                                }else
                                    t.put(entry.getKey(), e.getAsString());
                            }
                        }
                        else {
                        	//LOG.debug(entry.getValue().getClass().getName());
                        	LOG.debug(entry.getKey()+"-->"+entry.getValue());
                            t.put(entry.getKey(), entry.getValue().getAsString());
                        }
                        //LOG.debug(entry.getValue().getAsString());
                    }
                    /*else {
                    	//LOG.debug(t);
                    	LOG.debug(entry.getValue().getClass().getName());
                    	LOG.debug(entry.getKey()+"-->"+entry.getValue().getClass().getName());
                        t.put(entry.getKey(), entry.getValue().getAsString());
                    }*/
                }
            });
            Set data = t.entrySet();
            Iterator i = data.iterator();
            while (i.hasNext()) {
                Map.Entry m = (Map.Entry) i.next();
                dataString += m.getValue();
            }
            LOG.debug(dataString);
            sb = Hashing.sha256().hashString(dataString, StandardCharsets.UTF_8);
            LOG.debug(sb.toString());            
        }
        return sb.toString();
    }

	private boolean insertJiraNodes(JsonElement dataElem, boolean successfulWriteFlag) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			if (dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has(InsightsAuditConstants.DIGITALSIGNATURE))
                dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().remove(InsightsAuditConstants.DIGITALSIGNATURE);
			if (dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("changeId")) {
				lastTimestamp = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject()
						.getAsJsonPrimitive("changeDateEpoch").getAsLong();
				boolean result;
				synchronized (insightAuditImpl) {
					result = insightAuditImpl.insertChangeLogData(
							dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject());
				}
				boolean tempFlag = utilObj.updateFlagToNeo4j(result, dataElem.getAsJsonObject());
				if (!tempFlag || !result)
					successfulWriteFlag = false;
			} else {
				lastTimestamp = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject()
						.getAsJsonPrimitive("inSightsTime").getAsLong();
				JsonArray changelogArray = new JsonArray();
				String jiraKey = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject()
						.getAsJsonPrimitive("key").getAsString();
				StringBuffer changelogCypher = new StringBuffer();
				changelogCypher.append("MATCH (c:DATA:JIRA:CHANGE_LOG) where c.issueKey = \"").append(jiraKey)
						.append("\"");
				changelogCypher.append(" RETURN distinct(c) order by c.changeDateEpoch DESC");
				GraphResponse response = dbHandler.executeCypherQuery(changelogCypher.toString());
				if (response.getJson().getAsJsonArray(InsightsAuditConstants.RESULTS).size() > 0) {
					JsonArray rows = response.getJson().get(InsightsAuditConstants.RESULTS).getAsJsonArray().get(0).getAsJsonObject()
							.get("data").getAsJsonArray();

					for (JsonElement changeLog : rows) {
						changelogArray
								.add(changeLog.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject());
					}
				}
				boolean result;
				synchronized (insightAuditImpl) {
					result = insightAuditImpl.insertJiraNode(
							dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject(),
							changelogArray);
				}
				boolean tempFlag = utilObj.updateFlagToNeo4j(result, dataElem.getAsJsonObject());
				if (!tempFlag || !result)
					successfulWriteFlag = false;
			}
		} catch (Exception e) {
			LOG.error("Error occured while inserting changed node ", e);
		}
		return successfulWriteFlag;
	}

}