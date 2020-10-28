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
package com.cognizant.devops.auditservice.audit.service;

import com.cognizant.devops.platformauditing.api.InsightsAudit;
import com.cognizant.devops.platformauditing.api.InsightsAuditImpl;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;

import static com.cognizant.devops.platformauditing.util.AuditServiceUtil.parseHistory;
import static com.cognizant.devops.platformauditing.util.AuditServiceUtil.parseOutput;

import org.springframework.stereotype.Service;

@Service("auditService")
public class AuditServiceImpl implements  AuditService{

    InsightsAudit insightAudit = new InsightsAuditImpl();

    public JsonObject searchAuditLogByAsset(String assetId) throws InsightsCustomException {
        String response = insightAudit.getAssetInfo(assetId);
		if(response==null) {
			throw new InsightsCustomException("Error while quering searchAuditLogByAsset");
		}
        return parseOutput(response);
    }

    public JsonObject searchAuditLogByDate(String startDate, String endDate, String toolName) throws InsightsCustomException {

        String response = insightAudit.getAllAssets(startDate, endDate, toolName);
		if(response==null) {
			throw new InsightsCustomException("Error while quering blockchain");
		}
        return parseOutput(response);
    }

    public JsonObject getAssetHistory(String assetId) throws InsightsCustomException {
        String response = insightAudit.getAssetHistory(assetId);
        if (response == null) {
            throw new InsightsCustomException("Error while quering blockchain");
        }
        return parseHistory(response);
    }

    public JsonObject getProcessFlow() throws InsightsCustomException{
        JsonObject processjson = insightAudit.getProcessFlow();
        if(processjson == null){
            throw new InsightsCustomException("Error while reading Process.json");
        }
        return processjson;
    }

}
