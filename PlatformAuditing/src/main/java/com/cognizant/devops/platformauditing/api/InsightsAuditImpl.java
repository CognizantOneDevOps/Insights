package com.cognizant.devops.platformauditing.api;

import com.cognizant.devops.platformauditing.hyperledger.accesslayer.BCNetworkClient;
import com.cognizant.devops.platformauditing.util.LoadFile;
import com.cognizant.devops.platformauditing.util.RestructureDataUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InsightsAuditImpl implements InsightsAudit {

    private static final Logger LOG = LogManager.getLogger(InsightsAuditImpl.class.getName());

    RestructureDataUtil utilMethods = new RestructureDataUtil();

    @Override
    public String getAssetHistory(String assetID) {

        LOG.debug("searching for asset history" + assetID);

        String[] queryArgs = {assetID};
        try {

            BCNetworkClient bcClient = BCNetworkClient.getInstance();
            return bcClient.getAssetHistory(queryArgs);

        } catch (Exception e) {
            LOG.error("Error while quering blockchain", e);
            return null;
        }
    }

    @Override
    public String getAllAssets(String startDate, String endDate, String toolName) {

        LOG.debug("searching for asset for a date range");

        try {
            String[] queryArgs = {startDate, endDate, toolName};

            BCNetworkClient bcClient = BCNetworkClient.getInstance();
            return bcClient.getAllAssetsByDates(queryArgs);

        } catch (Exception e) {
            LOG.error("Error while quering blockchain", e);
            return null;
        }
    }


    @Override
    public String getAssetInfo(String assetId) {

        LOG.debug("searching for asset for an asset id");

        try {
            String[] queryArgs = {assetId};

            BCNetworkClient bcClient = BCNetworkClient.getInstance();
            return bcClient.getAssetDetails(queryArgs);

        } catch (Exception e) {
            LOG.error("Error while quering blockchain", e);
            return null;
        }
    }

    @Override
    public boolean insertToolData(JsonObject input) {
        try {
            JsonObject data = utilMethods.masssageData(input);
            Boolean insertFlag = utilMethods.getInsertionFlag(data);
            if (insertFlag) {
                BCNetworkClient bcNetworkClient = BCNetworkClient.getInstance();
                LOG.info("Inserting Data:\n" + data);
                return insertNode(data, bcNetworkClient);
                //return true;
            } else {
                LOG.info("Insertion is not required according to process rules:\n" + data);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertChangeLogData(JsonObject input) {
        try {
            JsonObject changelogData = utilMethods.massageChangeLog(input);
            JsonParser parser = new JsonParser();
            JsonObject ledgerCopy = parser.parse(getAssetInfo(changelogData.get("almAssetID").getAsString())).getAsJsonObject();
            if (ledgerCopy.getAsJsonPrimitive("statusCode").getAsString().equals("200")) {
                if(utilMethods.validateChangelog(ledgerCopy.getAsJsonObject("msg"), changelogData)) {
                    changelogData = utilMethods.constructJiraFromChangelog(ledgerCopy.getAsJsonObject("msg"), changelogData);
                    Boolean insertFlag = utilMethods.getInsertionFlag(changelogData);
                    if (insertFlag) {
                        BCNetworkClient bcNetworkClient = BCNetworkClient.getInstance();
                        LOG.info("Inserting Data:\n" + changelogData);
                        return insertNode(changelogData, bcNetworkClient);
                        //return true;
                    } else {
                        LOG.info("Insertion is not required according to process rules:\n" + changelogData);
                        return true;
                    }
                }else{
                    LOG.info("Changelog already updated in jira :"+ changelogData);
                    return true;
                }
            } else {
                LOG.info("Changelog node was read before the actual jira node. No changes made: \n" + input);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean insertJiraNode(JsonObject input, JsonArray changelogArray) {
        try {
            JsonObject data = utilMethods.masssageData(input);
            LOG.info("POST MASSAGING:\n" + data);
            Boolean insertFlag = utilMethods.getInsertionFlag(data);
            if (insertFlag) {
                BCNetworkClient bcNetworkClient = BCNetworkClient.getInstance();
                if(changelogArray.size() == 0){
                    return insertNode(data, bcNetworkClient);
                }
                else{
                    data = utilMethods.traceBackJiraNode(data, changelogArray);
					LOG.info("Traced back Jira node:\n"+ data);
                    return insertNode(data, bcNetworkClient);
                }
            } else {
                LOG.info("Insertion is not required according to process rules:\n" + data);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    //common method for insertion
    private boolean insertNode(JsonObject arg, BCNetworkClient bcNetworkClient) throws Exception {
        String[] nodeData = {arg.toString()};
        JsonObject insertionResult = bcNetworkClient.createBCNode(nodeData);
        LOG.debug(insertionResult);
        if (insertionResult.getAsJsonPrimitive("statusCode").getAsString().equals("201")) {
            return true;
        } else if (insertionResult.getAsJsonPrimitive("statusCode").getAsString().equals("104")) {
            LOG.error(insertionResult.getAsJsonPrimitive("msg"));
            return false;
        } else {
            LOG.warn(insertionResult.getAsJsonPrimitive("msg"));
            return true;
        }
    }

    public JsonObject getProcessFlow(){
        return LoadFile.getProcessModel();
    }
}