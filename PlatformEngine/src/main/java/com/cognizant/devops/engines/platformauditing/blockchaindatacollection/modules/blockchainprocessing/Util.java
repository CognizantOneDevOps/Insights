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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Util {

    
    private static File f = new File("tracking.json");
    private static Logger log = LogManager.getLogger(Util.class);
    private static InsightsAuditImpl insightAuditImpl = null;

    public  static InsightsAuditImpl getAuditObject() {
        if (insightAuditImpl == null)
            insightAuditImpl = new InsightsAuditImpl();
        return insightAuditImpl;
    }

    public JsonObject readTracking() throws IOException, InsightsCustomException {
    	JsonObject tracking = new JsonObject();
    	InputStream is = null;
        JsonParser parser = new JsonParser();
        if(f.exists()) {
            is = new FileInputStream(f);
            String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8.name());
            tracking = (JsonObject) parser.parse(jsonTxt);
        }
        else{
            if(!f.createNewFile()){
            	throw new InsightsCustomException("File to be created already exists");
            }
            tracking.addProperty("insightsTime", 0);
            tracking.addProperty("jiraTimestamp", 0);
            FileUtils.writeStringToFile(f,String.valueOf(tracking));
        }
        return tracking;
    }

    public void writeTracking(JsonObject input) throws IOException, InsightsCustomException {

        JsonObject tracking = readTracking();
        for(Map.Entry<String, JsonElement> element:input.entrySet()){
            tracking.add(element.getKey(), element.getValue());
        }
        FileUtils.writeStringToFile(f, String.valueOf(tracking));
    }

    public boolean updateFlagToNeo4j(boolean flag, JsonObject data) throws InsightsCustomException {
        String blockchainProcessedFlag = "blockchainProcessedFlag";
        GraphDBHandler dbHandler = new GraphDBHandler();
        if (flag) {
            StringBuffer writeBackCypher = new StringBuffer();
            writeBackCypher.append("MATCH (n:DATA) WHERE ID(n) = ").append(data.get("meta").getAsJsonArray().get(0).getAsJsonObject().getAsJsonPrimitive("id")).append(" ");
            writeBackCypher.append("SET n.").append(blockchainProcessedFlag).append(" = true");
            GraphResponse writeBackResponse = dbHandler.executeCypherQuery(writeBackCypher.toString());
            //check failure of writeBack
            if (writeBackResponse.getJson().getAsJsonArray("errors").size() != 0)
                return false;
            else
                return true;
        }
        return false;
    }
}
