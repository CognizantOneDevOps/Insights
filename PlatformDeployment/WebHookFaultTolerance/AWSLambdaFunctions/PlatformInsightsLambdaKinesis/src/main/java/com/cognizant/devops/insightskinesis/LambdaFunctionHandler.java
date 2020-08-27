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

/**
 * @author 513585
 */
package com.cognizant.devops.insightskinesis;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Record;
import com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.Result;
import com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class LambdaFunctionHandler implements RequestHandler<KinesisFirehoseEvent, KinesisAnalyticsInputPreprocessingResponse> {
	private static Logger log = LogManager.getLogger(LambdaFunctionHandler.class);
	private static final int CONNECTION_TIMEOUT = Integer.valueOf(System.getenv("connectionTimeout"));
	private static final boolean MAINTENANCE_MODE = Boolean.parseBoolean(System.getenv("maintenanceMode"));
	/**
	 * Get the Kinesis event, De-serialize the event and get event Body and URL
	 * @param KinesisFirehoseEvent
	 * @return transformedEvent
	 */
	@Override
	public KinesisAnalyticsInputPreprocessingResponse handleRequest(KinesisFirehoseEvent event, Context context) {

		KinesisAnalyticsInputPreprocessingResponse transformedEvent = new KinesisAnalyticsInputPreprocessingResponse();
		List<Record> transRecords = new ArrayList<Record>();
		int responseCode = 200;
		for (com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent.Record  record : event.getRecords()) {

			ByteBuffer bufferData = record.getData();
			InputStream data = new ByteArrayInputStream(bufferData.array());
			JsonElement element = new JsonParser().parse(new InputStreamReader(data));
			JsonObject json = element.getAsJsonObject();
			String body = json.get("body").toString();
			String tool = json.get("tool").getAsString();
			String url = System.getenv(tool);
			try {
				if (responseCode == 200 && url != null && !MAINTENANCE_MODE) {
					responseCode = 0;
					URL insightsUrl = new URL(url);
					HttpURLConnection con = (HttpURLConnection)insightsUrl.openConnection();
					con.setRequestMethod("POST");
					con.setRequestProperty("Content-Type", "application/json; utf-8");
					con.setRequestProperty("Accept", "application/json");
					con.setDoOutput(true);
					con.setConnectTimeout(CONNECTION_TIMEOUT);
					OutputStream os = con.getOutputStream();
					os.write(body.getBytes());
					os.flush();
					os.close();
					responseCode = con.getResponseCode();
					if (responseCode == 200) {
						Record transRecord = new Record(record.getRecordId(), Result.Dropped, bufferData);
						transRecords.add(transRecord);
					} else {
						log.error("Error while conecting URL " + url + " return response code " + responseCode);
					}
				}
			} catch (MalformedURLException e) {
				log.error("Invalid URL fot the tool " + tool + " " + e.toString());
			} catch (Exception e) {
				log.error("Error connecting to URL " + url + " " + e.toString());
			} finally {
				if (responseCode != 200 || MAINTENANCE_MODE || url == null) {
					if (url == null) {
						log.error("Url Entry is null for the tool "+ tool);
					}
					Record transRecord = new Record(record.getRecordId(), Result.Ok, bufferData);
					transRecords.add(transRecord);
				}
			}
		}
		transformedEvent.setRecords(transRecords);
		return transformedEvent;
	}
}