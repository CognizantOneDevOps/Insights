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

import java.io.ByteArrayOutputStream;

import org.json.simple.JSONObject;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.google.gson.Gson;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

/**
 * Azure Functions with Timer Trigger.
 */
public class WebhookFailureHandler extends AzureFunctionHelper {
   
    @FunctionName("InsightsEventsRetrieval")
    public void run(
    		@TimerTrigger(name = "timer", schedule = "%TimerInterval%") String timerInfo,
            final ExecutionContext context) {
        
        /* Fetching the container client */
        BlobContainerClient failedContainerClient = AzureFunctionHelper.failedContainerClient;
        
        /* Fetching the list of events from container client */
        failedContainerClient.listBlobs().forEach(blobItem -> {
        	
        	try {
	        	BlockBlobClient blobClient = getBlockBlobClient(failedContainerClient, blobItem.getName());
	        	int blobSize = (int) blobClient.getProperties().getBlobSize();
	        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream(blobSize);
	        	blobClient.download(outputStream);
	        	
	        	/*Converting payload from output stream to String*/
	        	String stringBody = new String(outputStream.toByteArray());
				outputStream.close();
				
				/*Converting payload from String to JSONObject*/
				Gson g = new Gson();
	        	JSONObject payload = g.fromJson(stringBody, JSONObject.class);
	        	String endpoint = payload.get("endpoint").toString();
	        	String url = System.getenv(endpoint);
	        	
	        	if(url != null) {
	        		int responseCode = sendPayloadToWebhookEndpoint(url, payload, context);
	        		if(responseCode == 200) {
	        			
	        			/* Deleting blob if response code is 200 */ 
	        			blobClient.delete();
	        			context.getLogger().info(blobClient.getBlobName() + " sent to " + endpoint + 
	        					" endpoint and deleted from failedeventcontainer");
	        		} else {
	        			context.getLogger().info("Error ouccured with response code "+ responseCode + 
	        					" while sending " + blobClient.getBlobName() + " to " +  endpoint + " endpoint");
	        		}
	        	} else {
	        		context.getLogger().info(endpoint + " endpoint url not found for the " + blobClient.getBlobName());
	        	}
        	} catch (Exception e) {
        		context.getLogger().info("Exception ouccured while sending payload to webHook endpoint: " + e.toString());        	
        	}
        });
    }
}
