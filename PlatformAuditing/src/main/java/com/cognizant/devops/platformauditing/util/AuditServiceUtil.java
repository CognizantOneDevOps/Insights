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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import com.cognizant.devops.platformcommons.constants.InsightsAuditConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public class AuditServiceUtil {

private AuditServiceUtil() {
}
    //GetAssetDetails and QueryByDate: Convert the output from ledger into format suitable for UI
    public static JsonObject parseOutput(String message) {

        JsonObject inputMessage = JsonUtils.parseStringAsJsonObject(message);
        JsonObject formattedresponse = new JsonObject();
        try {
            formattedresponse.addProperty(InsightsAuditConstants.STATUS_CODE, inputMessage.getAsJsonPrimitive(InsightsAuditConstants.STATUS_CODE).getAsString());
            if (!inputMessage.getAsJsonPrimitive(InsightsAuditConstants.STATUS_CODE).getAsString().equals("200")) {
                formattedresponse.addProperty(InsightsAuditConstants.DATA, inputMessage.getAsJsonPrimitive(InsightsAuditConstants.MSG).getAsString());
            } else {
                if (inputMessage.get(InsightsAuditConstants.MSG).isJsonArray()) {
                    JsonArray array = new JsonArray();
                    JsonArray mainMsg = inputMessage.getAsJsonArray(InsightsAuditConstants.MSG);
                    for (JsonElement messageEntry : mainMsg) {
                        JsonObject msg = messageEntry.getAsJsonObject();
                        msg.remove(InsightsAuditConstants.UPLINK);
                        msg.remove(InsightsAuditConstants.DOWNLINK);
                        String formattedDate = epochToHumanDate(msg.get(InsightsAuditConstants.TIMESTAMP).getAsString());
                        msg.remove(InsightsAuditConstants.TIMESTAMP);
                        msg.addProperty(InsightsAuditConstants.TIMESTAMP, formattedDate);
                            array.add(msg);
                    }
                    if (array.size() > 0)
                        formattedresponse.add(InsightsAuditConstants.DATA, array);
                    else {
                        formattedresponse.addProperty(InsightsAuditConstants.STATUS_CODE, "104");
                        formattedresponse.add(InsightsAuditConstants.DATA, new JsonPrimitive("No assets found within the selected date range"));
                    }
                } else {
                    JsonArray array = new JsonArray();
                    JsonObject msg = inputMessage.getAsJsonObject(InsightsAuditConstants.MSG);
                    msg.remove(InsightsAuditConstants.UPLINK);
                    msg.remove(InsightsAuditConstants.DOWNLINK);
                    String formattedDate = epochToHumanDate(msg.get(InsightsAuditConstants.TIMESTAMP).getAsString());
                    msg.remove(InsightsAuditConstants.TIMESTAMP);
                    msg.addProperty(InsightsAuditConstants.TIMESTAMP, formattedDate);
                        array.add(msg);
                    if (array.size() > 0)
                        formattedresponse.add(InsightsAuditConstants.DATA, array);
                    else {
                        formattedresponse.addProperty(InsightsAuditConstants.STATUS_CODE, "104");
                        formattedresponse.add(InsightsAuditConstants.DATA, new JsonPrimitive("No assets found."));
                    }
                }
            }
        } catch (Exception e) {
        	//No code
        }

        return formattedresponse;
    }

    //GetAssetHistory: Convert the output from ledger into format suitable for UI
    public static JsonObject parseHistory(String message) {
        JsonObject inputMessage = JsonUtils.parseStringAsJsonObject(message);
        JsonObject formattedresponse = new JsonObject();
        JsonArray array = new JsonArray();
        try {
            formattedresponse.addProperty(InsightsAuditConstants.STATUS_CODE, inputMessage.getAsJsonPrimitive(InsightsAuditConstants.STATUS_CODE).getAsString());
            if (inputMessage.get(InsightsAuditConstants.MSG).isJsonArray()) {
                for (JsonElement entry : inputMessage.getAsJsonArray(InsightsAuditConstants.MSG)) {
                    JsonObject newMessage = new JsonObject();
                    newMessage.addProperty("TxID", entry.getAsJsonObject().getAsJsonPrimitive("TxId").getAsString());
                    for (Map.Entry<String, JsonElement> entrySet : entry.getAsJsonObject().getAsJsonObject("Value").entrySet()) {
                        if (entrySet.getKey().toLowerCase().contains("time")) {
                            String formattedDate = epochToHumanDate(entrySet.getValue().getAsString());
                            newMessage.addProperty(entrySet.getKey(), formattedDate);
                        } else if (entrySet.getKey().equals(InsightsAuditConstants.UPLINK) || entrySet.getKey().equals(InsightsAuditConstants.DOWNLINK))
                            continue;
                        else
                            newMessage.add(entrySet.getKey(), entrySet.getValue());
                    }
                            if (newMessage != null) {
                        array.add(newMessage);
                            }
                }

                if (array.size() > 0) {
                    ArrayList<JsonObject> aL = new ArrayList<>();
                    for (JsonElement a : array) {
                        aL.add(a.getAsJsonObject());
                    }
                    aL.sort(new Comparator<JsonObject>() {
                        @Override
                        public int compare(JsonObject o1, JsonObject o2) {
                            final String KEY_NAME = InsightsAuditConstants.TIMESTAMP;
                            String valA="";
                            String valB ="";

                            try {
                                valA = o1.getAsJsonPrimitive(KEY_NAME).getAsString();
                                valB = o2.getAsJsonPrimitive(KEY_NAME).getAsString();
                            } catch (Exception e) {
                            	//No code
                            }

                            return -valA.compareTo(valB);
                        }
                    });
                    JsonArray sortedArray = new JsonArray();
                    for (Object entry : aL) {
                        sortedArray.add((JsonObject) entry);
                    }

                    formattedresponse.add(InsightsAuditConstants.DATA, sortedArray);
                } else {
                    formattedresponse.addProperty(InsightsAuditConstants.STATUS_CODE, "104");
                    formattedresponse.add(InsightsAuditConstants.DATA, new JsonPrimitive("No assets found.."));
                }

            } else {
                formattedresponse.add(InsightsAuditConstants.DATA, new JsonPrimitive("No assets found..."));
            }
        } catch (Exception e) {
        	//No code
        
        }
        return formattedresponse;
    }

    private static String epochToHumanDate(String epochtime) {
        Long epoch = Long.valueOf(epochtime);
        Date date = new Date(epoch * 1000L);
        DateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return format.format(date);
    }

}
