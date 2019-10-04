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

import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestructureDataUtil {
    private static final Logger LOG = LogManager.getLogger(InsightsAuditImpl.class.getName());

    //convert data from neo4j to the format for ledger based on datamodel.json
    public JsonObject masssageData(JsonObject input) {
        JsonObject output = new JsonObject();
        //Read datamodel
        JsonObject datamodel = LoadFile.getDataModel();
        datamodel = datamodel.getAsJsonObject(input.getAsJsonPrimitive("toolName").getAsString().toUpperCase());
        for (Map.Entry<String, JsonElement> property : datamodel.entrySet()) {
            if (!property.getKey().equals("uplink") && !property.getKey().equals("downlink")) {
                if (property.getKey().equals("jiraKeys")) {
                    //regex matching particularly for extracting jira Keys from git commit message
                    JsonArray ExtractedData = extractJiraKeysFromCommitMessage(input, property.getValue().getAsString());
                    output.add("jiraKeys", ExtractedData);
                } else if (property.getValue().isJsonObject()) {
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
        output.add("date", humanDateFromTimestamp(output.get("timestamp").getAsLong()));
        output = addLinks(output, datamodel);
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
            JsonObject processModel = LoadFile.getProcessModel();
            JsonArray process = processModel.getAsJsonArray("Steps");
            for (JsonElement tool : process) {
                if (input.getAsJsonPrimitive("toolName").getAsString().equals(tool.getAsJsonObject().getAsJsonPrimitive("Tool").getAsString().toUpperCase())) {
                    if (tool.getAsJsonObject().get("Types").isJsonArray()) {
                        for (JsonElement type : tool.getAsJsonObject().get("Types").getAsJsonArray()) {
                            for (Map.Entry<String, JsonElement> property : type.getAsJsonObject().getAsJsonObject("Entity").entrySet()) {
                                if (input.getAsJsonPrimitive(property.getKey()).equals(property.getValue())) {
                                    if (type.getAsJsonObject().getAsJsonPrimitive("SubworkflowFlag").getAsBoolean()) {
                                        String inputEntity = input.getAsJsonPrimitive(type.getAsJsonObject().getAsJsonObject("Subworkflow").getAsJsonPrimitive("Entity").getAsString()).getAsString();
                                        for (JsonElement step : type.getAsJsonObject().getAsJsonObject("Subworkflow").getAsJsonArray("Steps")) {
                                            if (inputEntity.equals(step.getAsJsonObject().getAsJsonPrimitive("Entity").getAsString())) {
                                                return step.getAsJsonObject().getAsJsonPrimitive("insertIntoLedger").getAsBoolean();
                                            }
                                        }
                                    } else {
                                        return type.getAsJsonObject().getAsJsonPrimitive("insertIntoLedger").getAsBoolean();
                                    }
                                }
                            }
                        }
                    } else if (tool.getAsJsonObject().getAsJsonPrimitive("SubworkflowFlag").getAsBoolean()) {
                        String inputEntity = input.getAsJsonPrimitive(tool.getAsJsonObject().getAsJsonObject("Subworkflow").getAsJsonPrimitive("Entity").getAsString()).getAsString();
                        for (JsonElement step : tool.getAsJsonObject().getAsJsonObject("Subworkflow").getAsJsonArray("Steps")) {
                            if (inputEntity.equals(step.getAsJsonObject().getAsJsonPrimitive("Entity").getAsString())) {
                                return step.getAsJsonObject().getAsJsonPrimitive("insertIntoLedger").getAsBoolean();
                            }
                        }
                    } else {
                        return tool.getAsJsonObject().getAsJsonPrimitive("insertIntoLedger").getAsBoolean();
                    }
                }
            }

        } catch (Exception e) {
            LOG.error(e);
        }
        LOG.error("No process rules defined for the given input:\n" + input);
        return false;
    }

    //extract hyphenated words in commitmessage as jiraKeys
    private JsonArray extractJiraKeysFromCommitMessage(JsonObject input, String property) {
        final String regex = "([a-zA-Z0-9]*[\\-][a-zA-Z0-9]*)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        JsonObject extractedData = new JsonObject();
        String inputString = input.getAsJsonPrimitive(property).getAsString();
        final Matcher matcher = pattern.matcher(inputString);
        JsonArray jiraKeys = new JsonArray();
        while (matcher.find()) {
            jiraKeys.add(matcher.group(0));
        }
        extractedData.add("Value", jiraKeys);
        return jiraKeys;
    }

    //add the uplink and downlink fields
    public JsonObject addLinks(JsonObject input, JsonObject datamodel) {
        if (!datamodel.get("uplink").isJsonObject()) {
            input.addProperty("uplink", "null");
        } else {
            JsonObject uplinkObject = new JsonObject();
            for (Map.Entry<String, JsonElement> uplink : datamodel.getAsJsonObject("uplink").entrySet()) {
                JsonElement value = input.get(uplink.getValue().getAsString());
                uplinkObject.add(uplink.getKey(), value);
            }
            input.add("uplink", uplinkObject);
        }
        if (!datamodel.get("downlink").isJsonObject()) {
            input.addProperty("downlink", "null");
        } else {
            JsonObject downlinkObject = new JsonObject();
            for (Map.Entry<String, JsonElement> downlink : datamodel.getAsJsonObject("downlink").entrySet()) {
                JsonElement value = input.get(downlink.getValue().getAsString());
                downlinkObject.add(downlink.getKey(), value);
            }
            input.add("downlink", downlinkObject);
        }

        return input;
    }

    //convert changelog nodes into the fields specified in datamodel.json
    public JsonObject massageChangeLog(JsonObject input) {
        JsonObject output = new JsonObject();
        boolean fieldnamesetFlag = false;
        //Read datamodel
        JsonObject datamodel = LoadFile.getDataModel().getAsJsonObject("CHANGELOG");
        for (Map.Entry<String, JsonElement> property : datamodel.entrySet()) {
            if (input.has(property.getValue().getAsString())) {
                if (property.getKey().equals("fieldName")) {
                    fieldnamesetFlag = false;
                    JsonObject jiraDataModel = LoadFile.getDataModel().getAsJsonObject("JIRA");
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
        jiraNode.remove("timestamp");
        jiraNode.add("timestamp", changelog.getAsJsonPrimitive("timestamp"));
        if (changelog.has("fieldName")) {
            if (changelog.get("fieldName").getAsString().equals("attachments") && !changelog.get("toString").getAsString().equals("N/A")) {
                String baseUrl = jiraNode.get("issueAPI").getAsString().split("atlassian.net\\/")[0];
                StringBuffer attachmentUrl = new StringBuffer();
                attachmentUrl.append(baseUrl).append("atlassian.net/").append("secure/attachment/").append(changelog.get("to").getAsString()).append("/").append(changelog.get("toString").getAsString());
                jiraNode.remove(changelog.get("fieldName").getAsString());
                jiraNode.addProperty(changelog.get("fieldName").getAsString(), attachmentUrl.toString());
            } else if (changelog.get("fieldName").getAsString().equals("attachments") && changelog.get("toString").getAsString().equals("N/A")) {
                StringBuffer attachment = new StringBuffer();
                attachment.append(changelog.get("fromString")).append(" has been deleted");
                jiraNode.remove(changelog.get("fieldName").getAsString());
                jiraNode.addProperty(changelog.get("fieldName").getAsString(), attachment.toString());
            } else {
                jiraNode.remove(changelog.get("fieldName").getAsString());
                jiraNode.add(changelog.get("fieldName").getAsString(), changelog.get("toString"));
            }
            jiraNode.add("date", humanDateFromTimestamp(jiraNode.get("createdTime").getAsLong()));
            return jiraNode;
        } else {
            return null;
        }
    }

    //check if the changelog and jira nodes are in same context or not
    public boolean validateChangelog(JsonObject jiraNode, JsonObject changelog) {
        if (jiraNode.get("timestamp").getAsString().equals(changelog.get("timestamp").getAsString())) {
            if (jiraNode.get(changelog.get("fieldName").getAsString()).getAsString().equals(changelog.get("toString").getAsString())) {
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
                    if (massagedChangelog.has("fieldName")) {
                        if (massagedChangelog.get("fieldName").getAsString().equals("attachments") && !massagedChangelog.get("fromString").getAsString().equals("N/A")) {
                            String baseUrl = jiraNode.get("issueAPI").getAsString().split("atlassian.net\\/")[0];
                            StringBuffer attachmentUrl = new StringBuffer();
                            attachmentUrl.append(baseUrl).append("atlassian.net/").append("secure/attachment/").append(massagedChangelog.get("from").getAsString()).append("/").append(massagedChangelog.get("fromString").getAsString());
                            jiraNode.remove(massagedChangelog.get("fieldName").getAsString());
                            jiraNode.addProperty(massagedChangelog.get("fieldName").getAsString(), attachmentUrl.toString());
                        } else {
                            jiraNode.remove(massagedChangelog.get("fieldName").getAsString());
                            jiraNode.add(massagedChangelog.get("fieldName").getAsString(), massagedChangelog.get("fromString"));
                        }
                    } else
                        continue;
            }
        }
        jiraNode.remove("timestamp");
        jiraNode.add("timestamp", jiraNode.get("createdTime"));
        return jiraNode;
    }

}
