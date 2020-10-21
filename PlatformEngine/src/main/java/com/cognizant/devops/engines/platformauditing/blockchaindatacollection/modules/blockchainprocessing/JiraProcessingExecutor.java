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
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformauditing.util.LoadFile;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

public class JiraProcessingExecutor extends TimerTask {
	private static Logger LOG = LogManager.getLogger(JiraProcessingExecutor.class);

	private long lastTimestamp;
	private String blockchainProcessedFlag = "blockchainProcessedFlag";

	private int nextBatchSize = 0;
	private final InsightsAuditImpl insightAuditImpl = Util.getAuditObject();
	private static Util utilObj = new Util();

	@Override
	public void run() {
		LOG.info("Blockchain Processing Executer jira module is getting executed");

		JiraNodeExtraction();
	}

	private void JiraNodeExtraction() {
		GraphDBHandler dbHandler = new GraphDBHandler();
		StringBuffer cypher = new StringBuffer();
		cypher.append("MATCH (n:DATA) WHERE ");
		cypher.append("\"JIRA\" IN labels(n) AND ");
		cypher.append("NOT \"SPRINT\" IN labels(n) AND ");
		cypher.append("(NOT EXISTS (n.").append(blockchainProcessedFlag).append(") ");
		cypher.append("OR n.").append(blockchainProcessedFlag).append(" = false) ");

		try {
			JsonObject config = LoadFile.getConfig();
			boolean nextBatchQuery = true;
			while (nextBatchQuery) {
				Boolean successfulWriteFlag = true;
				StringBuffer cypherPickUpTime = new StringBuffer();

				cypherPickUpTime.append("RETURN distinct(n) ORDER BY n.inSightsTime,n.changeDateEpoch");
				StringBuffer cypherSkip = new StringBuffer();
				cypherSkip.append(" skip ").append(nextBatchSize);
				cypherSkip.append(" limit ").append(config.get("dataBatchSize").getAsInt());
				GraphResponse response = dbHandler
						.executeCypherQuery(cypher.toString() + cypherPickUpTime.toString() + cypherSkip.toString());
				JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject().get("data")
						.getAsJsonArray();
				for (JsonElement dataElem : rows) {
					
					if(dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("digitalSignature")) {
						String digitalSign = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("digitalSignature").getAsString();
						LOG.debug("--------------------------------jira executer-----------------");
						String hc = getHash(dataElem.getAsJsonObject().get("row").getAsJsonArray());
						LOG.debug(hc);
						//decrypt digitalSign
						byte[] byteArray = Base64.getDecoder().decode(digitalSign.getBytes());
						JsonObject bcConfig = LoadFile.getConfig();
						String keyStr = new String(Files.readAllBytes(Paths.get(bcConfig.get("ENGINE_PRIVATE_KEY").getAsString())));
						keyStr = keyStr.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
						KeyFactory key = KeyFactory.getInstance(config.get("keyAlgorithm").getAsString());
						PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyStr));
						PrivateKey k = key.generatePrivate(spec);
						Cipher cipher = Cipher.getInstance(config.get("decryptionAlgorithm").getAsString());
						cipher.init(Cipher.DECRYPT_MODE, k);

						byte[] decryptedBytes = cipher.doFinal(byteArray);
						if(hc.equals(new String(decryptedBytes)))
							successfulWriteFlag = insertJiraNodes(dataElem, successfulWriteFlag);
						else
							LOG.debug("Hash values do not match.. skipping uuid: {}" ,
									dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uuid"));
					}else
						LOG.debug("DigitalSignature not found for uuid: {}",
							dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uuid")+"\nNode skipped...");
				}

				// check for success for updating tracking

				if (successfulWriteFlag && lastTimestamp != 0) {
					JsonObject tracking = new JsonObject();
					tracking.addProperty("jiraTimestamp", lastTimestamp);
					utilObj.writeTracking(tracking);
				}
				int processedRecords = rows.size();
				nextBatchSize += config.get("dataBatchSize").getAsInt();
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
            TreeMap t = new TreeMap<String, String>();
            dt.getAsJsonObject().entrySet().parallelStream().forEach(entry -> {
                if (!entry.getKey().equals("uuid") && !entry.getKey().equals("digitalSignature")) {
                    if (entry.getKey().equals("inSightsTime") || entry.getKey().equals("createdTimeEpoch") || entry.getKey().equals("changeDateEpoch") || entry.getKey().equals("creationDateEpoch")) {
                    	LOG.debug("has epoch time");
                        t.put(entry.getKey(), String.valueOf(entry.getValue().getAsLong()) + ".0");
                    }
                    else {
                    	LOG.debug(t);
                        t.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            });
            Set data = t.entrySet();
            Iterator i = data.iterator();
            while (i.hasNext()) {
                Map.Entry m = (Map.Entry) i.next();
                dataString += m.getValue();
            }
            //LOG.debug(dataString);
            
            sb = Hashing.sha256().hashString(dataString, StandardCharsets.UTF_8);
            //LOG.debug(sb_encode.toString());
            //LOG.debug(sb.toString());
        }
        return sb.toString();
    }

	private boolean insertJiraNodes(JsonElement dataElem, boolean successfulWriteFlag) {
		GraphDBHandler dbHandler = new GraphDBHandler();
		try {
			if (dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("digitalSignature"))
                dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().remove("digitalSignature");
			
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
				if (response.getJson().getAsJsonArray("results").size() > 0) {
					JsonArray rows = response.getJson().get("results").getAsJsonArray().get(0).getAsJsonObject()
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
