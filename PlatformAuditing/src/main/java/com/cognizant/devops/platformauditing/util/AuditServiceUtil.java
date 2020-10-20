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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


public class AuditServiceUtil {


    //GetAssetDetails and QueryByDate: Convert the output from ledger into format suitable for UI
    public static JsonObject parseOutput(String message) {

        JsonParser parser = new JsonParser();
        JsonObject inputMessage = (JsonObject) parser.parse(message);
        JsonObject formattedresponse = new JsonObject();
        try {
            formattedresponse.addProperty("statusCode", inputMessage.getAsJsonPrimitive("statusCode").getAsString());
            if (!inputMessage.getAsJsonPrimitive("statusCode").getAsString().equals("200")) {
                formattedresponse.addProperty("data", inputMessage.getAsJsonPrimitive("msg").getAsString());
            } else {
                if (inputMessage.get("msg").isJsonArray()) {
                    JsonArray array = new JsonArray();
                    JsonArray mainMsg = inputMessage.getAsJsonArray("msg");
                    for (JsonElement messageEntry : mainMsg) {
                        JsonObject msg = messageEntry.getAsJsonObject();
                        msg.remove("uplink");
                        msg.remove("downlink");
                        String formattedDate = epochToHumanDate(msg.get("timestamp").getAsString());
                        msg.remove("timestamp");
                        msg.addProperty("timestamp", formattedDate);
                        if (msg != null)
                            array.add(msg);
                    }
                    if (array.size() > 0)
                        formattedresponse.add("data", array);
                    else {
                        formattedresponse.addProperty("statusCode", "104");
                        formattedresponse.add("data", new JsonPrimitive("No assets found within the selected date range"));
                    }
                } else {
                    JsonArray array = new JsonArray();
                    JsonObject msg = inputMessage.getAsJsonObject("msg");
                    msg.remove("uplink");
                    msg.remove("downlink");
                    String formattedDate = epochToHumanDate(msg.get("timestamp").getAsString());
                    msg.remove("timestamp");
                    msg.addProperty("timestamp", formattedDate);
                    if (msg != null)
                        array.add(msg);
                    if (array.size() > 0)
                        formattedresponse.add("data", array);
                    else {
                        formattedresponse.addProperty("statusCode", "104");
                        formattedresponse.add("data", new JsonPrimitive("No assets found"));
                    }
                }
            }
        } catch (Exception e) {
        }

        return formattedresponse;
    }

    //GetAssetHistory: Convert the output from ledger into format suitable for UI
    public static JsonObject parseHistory(String message) {
        JsonParser parser = new JsonParser();
        JsonObject inputMessage = (JsonObject) parser.parse(message);
        JsonObject formattedresponse = new JsonObject();
        JsonArray array = new JsonArray();
        try {
            formattedresponse.addProperty("statusCode", inputMessage.getAsJsonPrimitive("statusCode").getAsString());
            if (inputMessage.get("msg").isJsonArray()) {
                for (JsonElement entry : inputMessage.getAsJsonArray("msg")) {
                    JsonObject newMessage = new JsonObject();
                    newMessage.addProperty("TxID", entry.getAsJsonObject().getAsJsonPrimitive("TxId").getAsString());
                    for (Map.Entry<String, JsonElement> entrySet : entry.getAsJsonObject().getAsJsonObject("Value").entrySet()) {
                        if (entrySet.getKey().toLowerCase().contains("time")) {
                            String formattedDate = epochToHumanDate(entrySet.getValue().getAsString());
                            newMessage.addProperty(entrySet.getKey(), formattedDate);
                        } else if (entrySet.getKey().equals("uplink") || entrySet.getKey().equals("downlink"))
                            continue;
                        else
                            newMessage.add(entrySet.getKey(), entrySet.getValue());
                    }
                    if (newMessage != null)
                        array.add(newMessage);
                }

                if (array.size() > 0) {
                    ArrayList<JsonObject> aL = new ArrayList<>();
                    for (JsonElement a : array) {
                        aL.add(a.getAsJsonObject());
                    }
                    aL.sort(new Comparator<JsonObject>() {
                        @Override
                        public int compare(JsonObject o1, JsonObject o2) {
                            final String KEY_NAME = "timestamp";
                            String valA = new String();
                            String valB = new String();

                            try {
                                valA = o1.getAsJsonPrimitive(KEY_NAME).getAsString();
                                valB = o2.getAsJsonPrimitive(KEY_NAME).getAsString();
                            } catch (Exception e) {
                            }

                            return -valA.compareTo(valB);
                        }
                    });
                    JsonArray sortedArray = new JsonArray();
                    for (Object entry : aL) {
                        sortedArray.add((JsonObject) entry);
                    }

                    formattedresponse.add("data", sortedArray);
                } else {
                    formattedresponse.addProperty("statusCode", "104");
                    formattedresponse.add("data", new JsonPrimitive("No assets found"));
                }

            } else {
                formattedresponse.add("data", new JsonPrimitive("No assets found"));
            }
        } catch (Exception e) {
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
