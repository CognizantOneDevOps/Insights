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

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {


	private static final String S3_BUCKET_NAME =  System.getenv("s3BucketName");
	private static final String REGION = System.getenv("s3BucketRegion");
	private S3Client s3 = S3Client.builder().region(Region.of(REGION)).build();
	private static final String FUNCTION_FAILED = "function-failed";
	private static final String CONNECTION_FAILED = "connection-failed";
	private static final int LAMBDA_CONNECTION_TIMEOUT = 5000;
	private static final Integer MAX_FILE_READ_COUNT = 2;
	private static Logger logger = LoggerFactory.getLogger(LambdaFunctionHandler.class);

	private int responseCode;
	private boolean deleteObject;

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

			responseCode = 200;
			for (ListIterator<S3Object> iterVals = objects.listIterator(); iterVals.hasNext(); ) {
				S3Object s3Keys = (S3Object) iterVals.next();
				if (s3Keys != null) {
					getData(s3Keys, prefix);
					if (responseCode == 200 && deleteObject == true) {
						deleteData(s3Keys);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not able to list object form S3 Bucket " + e.toString());
		}
	}

	/**
	 * Read the S3 file and deserialize the data
	 * @param parseKey (S3 object or File Name)
	 * @param prefix (S3 key or Folder Name)
	 */

	public void getData(S3Object parseKey, String prefix){
		JsonObject finalJson;
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(S3_BUCKET_NAME).key(parseKey.key()).build();
		InputStream s3Object = s3.getObject(getObjectRequest);
		try {
			Reader reader = new InputStreamReader(s3Object, "UTF-8");
			JsonStreamParser parser = new JsonStreamParser(reader);
			Gson gson = new GsonBuilder().create();
			while (parser.hasNext()) {
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
				if (responseCode == 200 && url != null) {
					sendData(body, url);
					deleteObject = true;
				}else {
					deleteObject = false;
					logger.error("Could not connect to WebHook endpoint tool : " + tool + "url : " + url );
					break;
				}
			}

		} catch (UnsupportedEncodingException e) {
			logger.error("Could able to parse the data UnsupportedEncodingException" + e.toString());
		}
	}

	/**
	 * Send the events to Webhook endpoint
	 * @param inputData
	 * @param url
	 * @return responseCode
	 */

	public int sendData(String inputData, String url){

		try {
			URL insightsUrl = new URL(url);
			HttpURLConnection con;
			con = (HttpURLConnection)insightsUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setConnectTimeout(LAMBDA_CONNECTION_TIMEOUT);
			OutputStream os = con.getOutputStream();
			os.write(inputData.getBytes());
			os.flush();
			os.close();
			responseCode = con.getResponseCode();
		} catch (IOException e) {
			logger.error("Error ouccured while send message to WebHook endpoint" + e.toString());
			deleteObject = false;
		}
		return responseCode;

	}

	/**
	 * Delete the S3 Object (S3 file)
	 * @param deleteKey
	 */

	public void deleteData(S3Object deleteKey) {

		try {
			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(S3_BUCKET_NAME).key(deleteKey.key()).build();
			s3.deleteObject(deleteObjectRequest);
		} catch (Exception e) {
			logger.error("Error while deleting the object form S3" + e.toString());
		}
	}
}
