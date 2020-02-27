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

package com.cognizant.devops.AzureInsightsWebhook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.simple.JSONObject;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.microsoft.azure.functions.ExecutionContext;

/**
 * @author 668284
 *
 */
public abstract class AzureFunctionHelper {

    final static String storageEndpoint = System.getenv("StorageEndpoint");
    final static String accountName = System.getenv("StorageAccountName");
    final static String accountKey = System.getenv("StorageAccountKey");
    final static String webhookEventContainer = System.getenv("WebhookEventContainer");
    final static String failedEventContainer = System.getenv("FailedEventContainer");
    final static StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
    
    /* Creating a new BlobServiceClient */
    final static BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint(storageEndpoint)
    		.credential(credential).buildClient();
    
    /* Fetching the container client */
    final static BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(webhookEventContainer);
    final static BlobContainerClient failedContainerClient = blobServiceClient.getBlobContainerClient(failedEventContainer);
    
    /**
     * This method will return BlockBlobClient for the container and the event.
     * @param container			BlobContainerClient where blobs resides.
     * @param payloadName		Name of the event
     * @return					BlockBlobClient
     */
    public BlockBlobClient getBlockBlobClient(BlobContainerClient container, String payloadName) {
    	BlockBlobClient blobClient = container.getBlobClient(payloadName).getBlockBlobClient();
		return blobClient;
    }
    
    /**
	 * This method will upload the payload to the azure storage container based on
	 * the eventType. If the eventType is 'Event' then it will be stored to
	 * permanent container. If the eventType is 'FailedEvent' then it will be stored
	 * to failed container.
	 * 
	 * @param context			ExecutionContext for logging
	 * @param payloadName		Payload Name
	 * @param payload			Payload Object
	 * @param eventType			Type of Event. Event/FailedEvent
	 */
    public boolean uploadPayload(ExecutionContext context, String payloadName, JSONObject payload, String eventType) {
    	boolean isUploadSuccess = false;
    	String bodyString = payload.toString();
        int bodyLength = bodyString.length();
        try(InputStream dataStream = new ByteArrayInputStream(bodyString.getBytes())) {
	    	if(eventType.equals("Event")) {
	    		BlockBlobClient webhookEventBlobClient = getBlockBlobClient(containerClient, payloadName);
	    		webhookEventBlobClient.upload(dataStream, bodyLength);
	    		
	    	} else if(eventType.equals("FailedEvent")) {
	    		BlockBlobClient failedEventBlobClient = getBlockBlobClient(failedContainerClient, payloadName);
	    		failedEventBlobClient.upload(dataStream, bodyLength);
	    		
	    	}
	    	isUploadSuccess = true;
		} catch (Exception e) {
			context.getLogger().info("Exception ouccured while uploading " + payloadName + 
					" to Azure Storage container " + e.toString());
			isUploadSuccess = false;
		}
        return isUploadSuccess;
    }
    
    /**
	 * This method will send the payload to webhook endpoint based on the endpoint url.
	 * @param url		URL of the webhook endpoint
	 * @param context 	ExecutionContext for logging
	 * @param body		Payload came from tool
	 * @return			Response code 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	public int sendPayloadToWebhookEndpoint(final String url, JSONObject payload, ExecutionContext context)
			throws MalformedURLException, IOException, ProtocolException {
		URL insightsUrl = new URL(url);
		HttpURLConnection con = (HttpURLConnection)insightsUrl.openConnection();
		OutputStream os = null;
		try {
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);
			con.setConnectTimeout(5000);
			os = con.getOutputStream();
			os.write(payload.toString().getBytes());
		} catch(Exception e) {
			context.getLogger().info("Exception ouccured while sending payload to webHook endpoint: " + e.toString());
		} finally {
			os.flush();
			os.close();
			con.disconnect();
		}
		return con.getResponseCode();
	}
}
