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

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

//@Service("auditService")
public interface AuditService {
    public JsonObject searchAuditLogByAsset(String assetId) throws InsightsCustomException;
    public JsonObject searchAuditLogByDate(String startDate, String endDate, String toolName) throws InsightsCustomException;
    public JsonObject getAssetHistory(String assetId) throws InsightsCustomException;
    public JsonObject getProcessFlow() throws InsightsCustomException;
}
