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
package com.cognizant.devops.insightsretrieval;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonStreamParser;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {


	private static final String S3_BUCKET_NAME =  System.getenv("s3BucketName");
	private static final String REGION = System.getenv("s3BucketRegion");
	private static final S3Client s3 = S3Client.builder().region(Region.of(REGION)).build();
	private static final String FUNCTION_FAILED = "function-failed";
	private static final String CONNECTION_FAILED = "connection-failed";
	private static final int CONNECTION_TIMEOUT = Integer.valueOf(System.getenv("connectionTimeout"));
	private static final Integer MAX_FILE_READ_COUNT = 2;
	private static Logger log = LogManager.getLogger(LambdaFunctionHandler.class);

	private StringBuffer failedData;
	private int responseCode;

	/**
	 * Retrieval Lambda Handler to retrieve data form S3 Bucket and send the data
	 * @param S3 Object
	 */

	@Override
	public String handleRequest(Object event, Context context) {
		processObject(CONNECTION_FAILED);

		processObject(FUNCTION_FAILED);

		return null;
	}

	/**
	 * Get file names from S3 and proces the data
	 * @param prefix (S3 Folder Name)
	 */

	public void processObject(String prefix){

		try {
			ListObjectsRequest listObjects = ListObjectsRequest.builder().bucket(S3_BUCKET_NAME).prefix(prefix).maxKeys(MAX_FILE_READ_COUNT).build();
			ListObjectsResponse response = s3.listObjects(listObjects);
			List<S3Object> objects = response.contents();

			for (ListIterator<S3Object> iterVals = objects.listIterator(); iterVals.hasNext(); ) {
				S3Object s3Keys = (S3Object) iterVals.next();
				if (s3Keys != null) {
					processData(s3Keys, prefix);
					if (failedData.length() == 0) {
						deleteObject(s3Keys);
					}
					else {
						deleteObject(s3Keys);
						updateObject(s3Keys, failedData);
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not able to list object form S3 Bucket " + e.toString());
		}
	}

	/**
	 * Read the S3 file and deserialize the data
	 * @param parseKey (S3 object or File Name)
	 * @param prefix (S3 key or Folder Name)
	 */

	public void processData(S3Object parseKey, String prefix){
		JsonObject finalJson;
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(S3_BUCKET_NAME).key(parseKey.key()).build();
		InputStream s3Object = s3.getObject(getObjectRequest);
		try {
			Reader reader = new InputStreamReader(s3Object, "UTF-8");
			JsonStreamParser parser = new JsonStreamParser(reader);
			failedData = new StringBuffer();
			Gson gson = new GsonBuilder().create();
			responseCode = 200;
			while (parser.hasNext()) {
				try {
					JsonElement element = parser.next();
					String data = gson.toJson(element);
					JsonElement parsedElement = new JsonParser().parse(data);
					JsonObject encodedJson = parsedElement.getAsJsonObject();
					if (prefix == FUNCTION_FAILED) {
						String jsonString= encodedJson.get("rawData").toString();
						String decodedString = new String(Base64.decodeBase64(jsonString.getBytes()));
						JsonElement rawData = new JsonParser().parse(decodedString);
						finalJson = rawData.getAsJsonObject();
					}else {
						finalJson = encodedJson;
					}
					String body = finalJson.get("body").toString();
					String tool = finalJson.get("tool").getAsString();
					String url = System.getenv(tool);
					sendData(element, body, url, tool);
				} catch (Exception e) {
					log.error("Could able to process the data" + e.toString());
				} 
			}
		} catch (UnsupportedEncodingException e) {
			log.error("Could able to parse the data UnsupportedEncodingException" + e.toString());
		}

	} 

	/**
	 * Send the events to Webhook endpoint
	 * @param data
	 * @param body
	 * @param url
	 * @return responseCode
	 */

	public int sendData(JsonElement data, String body, String url, String tool){

		try {
			if (responseCode == 200 && url != null) {
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
				if  (responseCode != 200) {
					log.error("Could not connect URL " + url + " return response code " + responseCode);
				}
			}
		} catch (MalformedURLException e) {
			log.error("Invalid URL fot the tool " + e.toString());
		} catch (Exception e) {
			log.error("Error connecting to URL " + url + " " + e.toString());
		} finally {
			if (responseCode != 200 || url == null) {
				if (url == null) {
					log.error("Url Entry is null for the tool " + tool);
				}
				failedData.append(data);
			}
		}
		return responseCode;
	}

	/**
	 * Delete the S3 Object (S3 file)
	 * @param deleteKey
	 */

	public void deleteObject(S3Object deleteKey) {

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(S3_BUCKET_NAME).key(deleteKey.key()).build();
			s3.deleteObject(deleteObjectRequest);
		} catch (Exception e) {
			log.error("Error while deleting the object form S3" + e.toString());
		}
	}	

	/**
	 * Update the Failure events to S3Bucket
	 * @param updateKey
	 * @param updateDsta
	 */

	public void updateObject(S3Object updateKey, StringBuffer updateDsta) {
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(S3_BUCKET_NAME).key(updateKey.key()).build();
			s3.putObject(putObjectRequest, RequestBody.fromString(updateDsta.toString()));
		} catch (Exception e) {
			log.error("Error while updating the object to S3" + e.toString());
		}
	}
}