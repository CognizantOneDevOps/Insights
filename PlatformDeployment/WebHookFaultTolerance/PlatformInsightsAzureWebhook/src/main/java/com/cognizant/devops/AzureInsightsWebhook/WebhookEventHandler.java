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

import java.io.IOException;
import java.util.Optional;

import org.json.simple.JSONObject;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with Http Trigger.
 */
public class WebhookEventHandler extends AzureFunctionHelper {

	@FunctionName("InsightsWebhookHandler")
    public HttpResponseMessage  run(
    		@HttpTrigger(name = "request", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) 
    		HttpRequestMessage<Optional<JSONObject>> request,
            final ExecutionContext context) throws IOException {
        
		final String endpoint = request.getQueryParameters().get("endpoint");
		final String url = System.getenv(endpoint);
        final long currentTimeInMillis = System.currentTimeMillis();
        boolean isWebhookSent = false;
        boolean isUploadSuccess = false;
        int responseCode = 0;
        
        JSONObject payload = request.getBody().get();
        payload.put("endpoint", endpoint);
        String payloadName = endpoint + "_" + currentTimeInMillis;
        context.getLogger().info(payload.toString());
        
        isUploadSuccess = uploadPayload(context, payloadName, payload, "Event");
        
    	try {
    		if(url != null) {
	    		responseCode = sendPayloadToWebhookEndpoint(url, payload, context);
	    		
	    		if(responseCode == 200) {
	    			isWebhookSent = true;
	    			context.getLogger().info("Payload saved to storage container with name " + payloadName + 
	    					" and sent to " + endpoint + " webhook endpoint");
	    		} else {
	    			context.getLogger().info("Error ouccured with response code "+ responseCode + 
	    					" while sending " + payloadName + " to " +  endpoint + " endpoint");
	    		}
    		} else {
    			context.getLogger().info(endpoint + " endpoint url not found for the " + payloadName);
    		}
    	} catch (Exception e) {
    		context.getLogger().info("Exception ouccured while sending payload to webHook endpoint: " + e.toString());
    	} finally {
			if(isWebhookSent == false) {
				isUploadSuccess = uploadPayload(context, payloadName, payload, "FailedEvent");
			}
    	}
    	
		if(isUploadSuccess){
			return request.createResponseBuilder(HttpStatus.OK).body("payload Successfully sent to " + endpoint + " endpoint").build();
		} else {
			return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Request failed to sent payload to " + endpoint + " endpoint").build();
		}
	}
}
