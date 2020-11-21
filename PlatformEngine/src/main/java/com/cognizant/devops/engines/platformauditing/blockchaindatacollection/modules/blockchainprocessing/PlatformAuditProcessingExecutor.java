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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.crypto.Cipher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformauditing.util.LoadFile;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class PlatformAuditProcessingExecutor extends TimerTask {
    private static Logger LOG = LogManager.getLogger(PlatformAuditProcessingExecutor.class);
    private final InsightsAuditImpl insightAuditImpl = Util.getAuditObject();
	private static Util utilObj = new Util();
	private long lastTimestamp;

    @Override
	public void run() {
        LOG.info("Blockchain Processing Executer module is getting executed");
        orphanNodeExtraction();
    }

    private void orphanNodeExtraction() {    	
    	String blockchainProcessedFlag = "blockchainProcessedFlag";
    	JsonObject config = LoadFile.getInstance().getConfig();
    	int dataBatchSize = config.get("dataBatchSize").getAsInt();
    	int nextBatchSize = 0;
    	String decryptionAlgorithm = config.get("decryptionAlgorithm").getAsString();
        String keyAlgorithm = config.get("keyAlgorithm").getAsString();
        
        GraphDBHandler dbHandler = new GraphDBHandler();
        StringBuffer cypher = new StringBuffer();
        cypher.append("MATCH (n:DATA) WHERE ");
        cypher.append("NOT \"JIRA\" IN labels(n) AND ");
        cypher.append("(NOT EXISTS (n.").append(blockchainProcessedFlag).append(") ");
        cypher.append("OR n.").append(blockchainProcessedFlag).append("=false) ");

        try {
        	JsonObject datamodel = LoadFile.getInstance().getDataModel();
    		Set<Entry<String, JsonElement>> e = datamodel.entrySet();
    		List<String> uniqueKeyList = new ArrayList<>();
    		e.forEach(x-> {
    			Set<Entry<String, JsonElement>> tool = x.getValue().getAsJsonObject().entrySet();
    			uniqueKeyList.add(tool.iterator().next().getValue().getAsString());
    		});
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
                	boolean isUniquePresent = checkUniquekeys(uniqueKeyList,dataElem);
                	if(dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("digitalSignature") && isUniquePresent) {
                        String digitalSign = dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("digitalSignature").getAsString();
                        LOG.debug("--------------------------process executor------------------------");
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
                        if (hc.equals(new String(decryptedBytes)))                        	
                            successfulWriteFlag = insertNode(dataElem, successfulWriteFlag);
                        else
                            LOG.debug("Hash values do not match.. skipping uuid: " +
								dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uuid"));
                    }else
                        LOG.debug("INVALID ASSET OR DigitalSignature not found for uuid: "+
							dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("uuid")+"\nNode skipped...");
                    //successfulWriteFlag = insertNode(dataElem, successfulWriteFlag);
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
        } catch (InsightsCustomException | IOException e) {
            LOG.error("Error occured while loading the destination data ", e);
        } catch (Exception e) {
			LOG.error(e);
        }

    }
    
    private String getHash(JsonArray row) {
        String dataString = "";
        HashCode sb = null;
        for (JsonElement dt : row) {
            //put dataObject into treemap to get sorted
            TreeMap<String, String> t = new TreeMap<String, String>();
            dt.getAsJsonObject().entrySet().parallelStream().forEach(entry -> {
                if (!entry.getKey().equals("uuid") && !entry.getKey().equals("digitalSignature") &&  !entry.getKey().equals("correlationTime") && !entry.getKey().equals("maxCorrelationTime") ) {
                    if (entry.getKey().equals("inSightsTime") ||entry.getKey().endsWith("Epoch"))
                        t.put(entry.getKey(), String.valueOf(entry.getValue().getAsLong()) + ".0");
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
                }
            });
            Set<?> data = t.entrySet();
            Iterator<?> i = data.iterator();
            while (i.hasNext()) {
                Map.Entry m = (Map.Entry) i.next();
                dataString += m.getValue();
            }
            LOG.debug(dataString);
            sb = Hashing.sha256().hashString(dataString, StandardCharsets.UTF_8);
        }
        return sb.toString();
    }

    private boolean insertNode(JsonElement dataElem, boolean successfulWriteFlag) {
        try {
        	if (dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has("digitalSignature"))
                dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().remove("digitalSignature");
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
    
    /**
     * Check if the response json contains the unique key required for each tool
     * @param uniqueKeyList
     * @param dataElem
     * @return isUniquePresent
     */
    private Boolean checkUniquekeys(List<String> uniqueKeyList, JsonElement dataElem) {
    	for (String uniqueKey : uniqueKeyList) {
    		if(dataElem.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsJsonObject().has(uniqueKey)) {
    			return Boolean.TRUE;
    		}
		}
		return Boolean.FALSE;
	}
}