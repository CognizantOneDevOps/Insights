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
package com.cognizant.devops.platformauditing.util;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.constants.InsightsAuditConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RestructureDataUtil {
    private static final Logger LOG = LogManager.getLogger(RestructureDataUtil.class.getName());

    //convert data from neo4j to the format for ledger based on datamodel.json
    public JsonObject massageData(JsonObject input) {
    	boolean isUnique = Boolean.FALSE;
    	JsonObject output = new JsonObject();
    	JsonObject datamodel = LoadFile.getInstance().getDataModel();
    	Set<Entry<String, JsonElement>> e = datamodel.entrySet();
    	List<String> uniqueKeyList = new ArrayList<>();
    	//Below line to handle primary key assetID should not be null before insert into ledger
    	e.forEach(x-> {
    		//Fetch Set 
    		Set<Entry<String, JsonElement>> tool = x.getValue().getAsJsonObject().entrySet();

    		uniqueKeyList.add(tool.iterator().next().getValue().getAsString());
    	});
    	for (String toolUniqueKey : uniqueKeyList) {
    		if(input.has(toolUniqueKey)) {
    			isUnique = Boolean.TRUE;
    			//Below line to handle primary key assetID should not be null before insert into ledger    	

    			//Read datamodel        
    			datamodel = datamodel.getAsJsonObject(input.getAsJsonPrimitive("toolName").getAsString().toUpperCase());
    			for (Map.Entry<String, JsonElement> property : datamodel.entrySet()) {
    				if (!property.getKey().equals(InsightsAuditConstants.UPLINK) && !property.getKey().equals(InsightsAuditConstants.DOWNLINK)) {
    					if (property.getValue().isJsonObject()) {
    						output.addProperty(property.getKey(), property.getValue().getAsJsonObject().getAsJsonPrimitive(property.getKey()).getAsString());
    					} else if (property.getValue().isJsonArray()) {
    						if (input.has(property.getValue().getAsJsonArray().get(0).getAsString())) {
    							JsonArray inputValue = input.getAsJsonArray(property.getValue().getAsJsonArray().get(0).getAsString());
    							output.add(property.getKey(), inputValue);
    						} else                     	
    							output.addProperty(property.getKey(), "N/A");
    					} else {
    						if (input.has(property.getValue().getAsString()))
    							output.add(property.getKey(), input.getAsJsonPrimitive(property.getValue().getAsString()));
    						else
    							output.addProperty(property.getKey(), "N/A");
    					}
    				}
    			}
    			output.add("date", humanDateFromTimestamp(output.get(InsightsAuditConstants.TIMESTAMP).getAsLong()));
    			output = addLinks(output, datamodel);        
    		}
    	}
    	if(!isUnique) {
    		LOG.info("Neo4j input fields has NO assetId for the tool before insert into ledger:--{}", input);
    	}
    	return output;
    }

    //convert the epoch timestamp to human date for consumption by chaincode
    private JsonPrimitive humanDateFromTimestamp(Long longdate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return new JsonPrimitive(format.format(new Date(longdate * 1000)));
    }


    //get the insertion flag from process.json
    public boolean getInsertionFlag(JsonObject input) {
        try {        
            JsonObject processModel = LoadFile.getInstance().getProcessModel();
            JsonArray process = processModel.getAsJsonArray(InsightsAuditConstants.STEPS);
            for (JsonElement tool : process) {
                if (input.getAsJsonPrimitive(InsightsAuditConstants.TOOLNAME).getAsString().equalsIgnoreCase(tool.getAsJsonObject().getAsJsonPrimitive("Tool").getAsString())) {
                    if (tool.getAsJsonObject().get(InsightsAuditConstants.TYPES).isJsonArray()) {
                        for (JsonElement type : tool.getAsJsonObject().get(InsightsAuditConstants.TYPES).getAsJsonArray()) {
                            for (Map.Entry<String, JsonElement> property : type.getAsJsonObject().getAsJsonObject(InsightsAuditConstants.ENTITY).entrySet()) {
                                if (input.getAsJsonPrimitive(property.getKey()).equals(property.getValue())) {
                                    if (type.getAsJsonObject().getAsJsonPrimitive("SubworkflowFlag").getAsBoolean()) {
                                        String inputEntity = input.getAsJsonPrimitive(type.getAsJsonObject().getAsJsonObject(InsightsAuditConstants.SUBWORKFLOW).getAsJsonPrimitive(InsightsAuditConstants.ENTITY).getAsString()).getAsString();
                                        for (JsonElement step : type.getAsJsonObject().getAsJsonObject(InsightsAuditConstants.SUBWORKFLOW).getAsJsonArray("Steps")) {
                                            if (inputEntity.equals(step.getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.ENTITY).getAsString())) {
                                                return step.getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.INSERTINTOLEDGER).getAsBoolean();
                                            }
                                        }
                                    } else {
                                        return type.getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.INSERTINTOLEDGER).getAsBoolean();
                                    }
                                }
                            }
                        }
                    } else if (tool.getAsJsonObject().getAsJsonPrimitive("SubworkflowFlag").getAsBoolean()) {
                        String inputEntity = input.getAsJsonPrimitive(tool.getAsJsonObject().getAsJsonObject(InsightsAuditConstants.SUBWORKFLOW).getAsJsonPrimitive(InsightsAuditConstants.ENTITY).getAsString()).getAsString();
                        for (JsonElement step : tool.getAsJsonObject().getAsJsonObject(InsightsAuditConstants.SUBWORKFLOW).getAsJsonArray("Steps")) {
                            if (inputEntity.equals(step.getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.ENTITY).getAsString())) {
                                return step.getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.INSERTINTOLEDGER).getAsBoolean();
                            }
                        }
                    } else {
                        return tool.getAsJsonObject().getAsJsonPrimitive(InsightsAuditConstants.INSERTINTOLEDGER).getAsBoolean();
                    }
                }
            }       	        	

        } catch (Exception e) {
            LOG.error(e);            
        }
        LOG.error("No process rules defined for the given input:-%n", input);
        return false;
    }


    //add the uplink and downlink fields
    public JsonObject addLinks(JsonObject input, JsonObject datamodel) {
        if (!datamodel.get(InsightsAuditConstants.UPLINK).isJsonObject()) {
            input.addProperty(InsightsAuditConstants.UPLINK, "null");
        } else {
            JsonObject uplinkObject = new JsonObject();
            for (Map.Entry<String, JsonElement> uplink : datamodel.getAsJsonObject(InsightsAuditConstants.UPLINK).entrySet()) {
                JsonElement value = input.get(uplink.getValue().getAsString());
                uplinkObject.add(uplink.getKey(), value);
            }
            input.add(InsightsAuditConstants.UPLINK, uplinkObject);
        }
        if (!datamodel.get(InsightsAuditConstants.DOWNLINK).isJsonObject()) {
            input.addProperty(InsightsAuditConstants.DOWNLINK, "null");
        } else {
            JsonObject downlinkObject = new JsonObject();
            for (Map.Entry<String, JsonElement> downlink : datamodel.getAsJsonObject(InsightsAuditConstants.DOWNLINK).entrySet()) {
            	JsonElement value = input.get(downlink.getValue().getAsString());
                downlinkObject.add(downlink.getKey(), value);
            }
            input.add(InsightsAuditConstants.DOWNLINK, downlinkObject);
        }

        return input;
    }

    //convert changelog nodes into the fields specified in datamodel.json
    public JsonObject massageChangeLog(JsonObject input) {
        JsonObject output = new JsonObject();
        boolean fieldnamesetFlag = false;
        //Read datamodel
        JsonObject datamodel = LoadFile.getInstance().getDataModel().getAsJsonObject("CHANGELOG");
        for (Map.Entry<String, JsonElement> property : datamodel.entrySet()) {
            if (input.has(property.getValue().getAsString())) {
                if (property.getKey().equals(InsightsAuditConstants.FIELDNAME)) {
                    fieldnamesetFlag = false;
                    JsonObject jiraDataModel = LoadFile.getInstance().getDataModel().getAsJsonObject("JIRA");
                    for (Map.Entry<String, JsonElement> jiraProperty : jiraDataModel.entrySet()) {
                        if (jiraProperty.getValue().isJsonArray() ? jiraProperty.getValue().getAsJsonArray().get(0).equals(input.getAsJsonPrimitive(property.getValue().getAsString())) : jiraProperty.getValue().equals(input.getAsJsonPrimitive(property.getValue().getAsString()))) {
                            output.addProperty(property.getKey(), jiraProperty.getKey());
                            fieldnamesetFlag = true;
                        }
                    }
                } else
                    output.add(property.getKey(), input.getAsJsonPrimitive(property.getValue().getAsString()));
            } else
                output.addProperty(property.getKey(), "N/A");
        }
        if (fieldnamesetFlag)
            return output;
        else
            return null;
    }

    //Construct a new jira node using the old jira node read from ledger and the changes obtained from neo4j
    public JsonObject constructJiraFromChangelog(JsonObject jiraNode, JsonObject changelog) {
        jiraNode.remove(InsightsAuditConstants.TIMESTAMP);
        jiraNode.add(InsightsAuditConstants.TIMESTAMP, changelog.getAsJsonPrimitive(InsightsAuditConstants.TIMESTAMP));
        if (changelog.has(InsightsAuditConstants.FIELDNAME)) {
            if (changelog.get(InsightsAuditConstants.FIELDNAME).getAsString().equals(InsightsAuditConstants.ATTACHMENTS) && !changelog.get(InsightsAuditConstants.TO_STRING).getAsString().equals("N/A")) {
                String baseUrl = jiraNode.get("issueAPI").getAsString().split("atlassian.net\\/")[0];
                StringBuilder attachmentUrl = new StringBuilder();
                attachmentUrl.append(baseUrl).append("atlassian.net/").append("secure/attachment/").append(changelog.get("to").getAsString()).append("/").append(changelog.get(InsightsAuditConstants.TO_STRING).getAsString());
                jiraNode.remove(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString());
                jiraNode.addProperty(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString(), attachmentUrl.toString());
            } else if (changelog.get(InsightsAuditConstants.FIELDNAME).getAsString().equals(InsightsAuditConstants.ATTACHMENTS) && changelog.get(InsightsAuditConstants.TO_STRING).getAsString().equals("N/A")) {
            	StringBuilder attachment = new StringBuilder();
                attachment.append(changelog.get(InsightsAuditConstants.FROM_STRING)).append(" has been deleted");
                jiraNode.remove(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString());
                jiraNode.addProperty(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString(), attachment.toString());
            } else {
                jiraNode.remove(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString());
                jiraNode.add(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString(), changelog.get(InsightsAuditConstants.TO_STRING));
            }
            jiraNode.add("date", humanDateFromTimestamp(jiraNode.get("createdTime").getAsLong()));
            return jiraNode;
        } else {
            return null;
        }
    }

    //check if the changelog and jira nodes are in same context or not
    public boolean validateChangelog(JsonObject jiraNode, JsonObject changelog) {
        if (jiraNode.get(InsightsAuditConstants.TIMESTAMP).getAsString().equals(changelog.get(InsightsAuditConstants.TIMESTAMP).getAsString())) {
            if (jiraNode.get(changelog.get(InsightsAuditConstants.FIELDNAME).getAsString()).getAsString().equals(changelog.get("toString").getAsString())) {
                return false;
            } else
                return true;
        }
        return true;
    }

    //Construct the initial jira node from the current jira node using the changelog
    public JsonObject traceBackJiraNode(JsonObject jiraNode, JsonArray changelogArray) {
        for (int i = 0; i < changelogArray.size(); i++) {
            JsonObject massagedChangelog = massageChangeLog(changelogArray.get(i).getAsJsonObject());
            if (massagedChangelog != null) {
                    if (massagedChangelog.has(InsightsAuditConstants.FIELDNAME)) {
                        if (massagedChangelog.get(InsightsAuditConstants.FIELDNAME).getAsString().equals(InsightsAuditConstants.ATTACHMENTS) && !massagedChangelog.get(InsightsAuditConstants.FROM_STRING).getAsString().equals("N/A")) {
                            String baseUrl = jiraNode.get("issueAPI").getAsString().split("atlassian.net\\/")[0];
                            StringBuilder attachmentUrl = new StringBuilder();
                            attachmentUrl.append(baseUrl).append("atlassian.net/").append("secure/attachment/").append(massagedChangelog.get("from").getAsString()).append("/").append(massagedChangelog.get(InsightsAuditConstants.FROM_STRING).getAsString());
                            jiraNode.remove(massagedChangelog.get(InsightsAuditConstants.FIELDNAME).getAsString());
                            jiraNode.addProperty(massagedChangelog.get(InsightsAuditConstants.FIELDNAME).getAsString(), attachmentUrl.toString());
                        } else {
                            jiraNode.remove(massagedChangelog.get(InsightsAuditConstants.FIELDNAME).getAsString());
                            jiraNode.add(massagedChangelog.get(InsightsAuditConstants.FIELDNAME).getAsString(), massagedChangelog.get(InsightsAuditConstants.FROM_STRING));
                        }
                    } else
                        continue;
            }
        }
        jiraNode.remove(InsightsAuditConstants.TIMESTAMP);
        jiraNode.add(InsightsAuditConstants.TIMESTAMP, jiraNode.get("createdTime"));
        return jiraNode;
    }

}