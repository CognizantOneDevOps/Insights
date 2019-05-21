package com.cognizant.devops.platformauditing.blockchaindatacollection.modules.blockchainprocessing;

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphDBException;
import com.cognizant.devops.platformcommons.dal.neo4j.GraphResponse;
import com.cognizant.devops.platformcommons.dal.neo4j.Neo4jDBHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.util.Map;

public class util {

    private static InputStream is = null;
    private static JsonObject tracking = new JsonObject();
    private static File f = new File("tracking.json");
    private static Logger LOG = LogManager.getLogger(PlatformAuditProcessingExecutor.class);
    private static InsightsAuditImpl insightAuditImpl = null;

    public  static InsightsAuditImpl getAuditObject() {
        if (insightAuditImpl == null)
            insightAuditImpl = new InsightsAuditImpl();
        return insightAuditImpl;
    }

    public JsonObject readTracking() throws IOException {
        JsonParser parser = new JsonParser();
        if(f.exists()) {
            is = new FileInputStream(f);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            tracking = (JsonObject) parser.parse(jsonTxt);
        }
        else{
            f.createNewFile();
            tracking.addProperty("insightsTime", 0);
            tracking.addProperty("jiraTimestamp", 0);
            FileUtils.writeStringToFile(f,String.valueOf(tracking));
        }
        return tracking;
    }

    public void writeTracking(JsonObject input) throws IOException {

        JsonObject tracking = readTracking();
        for(Map.Entry<String, JsonElement> element:input.entrySet()){
            tracking.add(element.getKey(), element.getValue());
        }
        FileUtils.writeStringToFile(f, String.valueOf(tracking));
    }

    public boolean updateFlagToNeo4j(boolean flag, JsonObject data) throws GraphDBException {
        String blockchainProcessedFlag = "blockchainProcessedFlag";
        Neo4jDBHandler dbHandler = new Neo4jDBHandler();
        if (flag == true ) {
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
