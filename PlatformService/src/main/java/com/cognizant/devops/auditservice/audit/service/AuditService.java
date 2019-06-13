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
