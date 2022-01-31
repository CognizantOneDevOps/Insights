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

package com.cognizant.devops.auditservice.audit.controller;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.auditservice.audit.service.AuditService;
import com.cognizant.devops.auditservice.audit.service.AuditServiceImpl;
import com.cognizant.devops.auditservice.audit.service.PdfWriter;
import com.cognizant.devops.auditservice.audit.service.PdfWriterImpl;
import com.cognizant.devops.platformcommons.constants.InsightsAuditConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.core.util.ValidationUtils;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
//@Conditional(InsightsAuditInitializationCondition.class)
@RequestMapping("traceability")
public class AuditController {
	private static Logger Log = LogManager.getLogger(AuditController.class);

    
    AuditService auditServiceImpl = new AuditServiceImpl();
    
    
	PdfWriter pdfWriterImpl = new PdfWriterImpl();

    @GetMapping(value = "/getAssetInfo",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonObject searchAuditLogByAsset(@RequestParam String assetId) {
        JsonObject response;
        try {
        	Log.debug(" Inside getAssetInfo searchAuditLogByAsset ====== ");
            response = auditServiceImpl.searchAuditLogByAsset(assetId);
            if (response.getAsJsonPrimitive(InsightsAuditConstants.STATUS_CODE).getAsString().equals("200"))
                return PlatformServiceUtil.buildSuccessResponseWithData(response.getAsJsonArray("data"));
            else
                return PlatformServiceUtil.buildFailureResponse(response.getAsJsonPrimitive("data").getAsString());
        } catch (Exception e) {
            return PlatformServiceUtil.buildFailureResponse(e.toString());
        }
    }

    @GetMapping(value = "/getAllAssets",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonObject searchAuditLogByDate(@RequestParam String startDate, @RequestParam String endDate, @RequestParam String toolName) {
        JsonObject response;
        try {
        	Log.debug(" Inside getAllAssets searchAuditLogByDate ====== ");
            response = auditServiceImpl.searchAuditLogByDate(startDate, endDate, toolName);
            if (response.getAsJsonPrimitive(InsightsAuditConstants.STATUS_CODE).getAsString().equals("200"))
                return PlatformServiceUtil.buildSuccessResponseWithData(response.getAsJsonArray("data"));
            else
                return PlatformServiceUtil.buildFailureResponse(response.getAsJsonPrimitive("data").getAsString());
        } catch (Exception e) {
            return PlatformServiceUtil.buildFailureResponse(e.toString());
        }
    }

    @GetMapping(value = "/getAssetHistory",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonObject getAssetHistory(@RequestParam String assetId) {
        JsonObject response = new JsonObject();
        try {
        	Log.debug(" Inside getAssetHistory  ====== ");
            response = auditServiceImpl.getAssetHistory(assetId);
            if (response.getAsJsonPrimitive(InsightsAuditConstants.STATUS_CODE).getAsString().equals("200"))
                return PlatformServiceUtil.buildSuccessResponseWithData(response.getAsJsonArray("data"));
            else
                return PlatformServiceUtil.buildFailureResponse(response.getAsJsonPrimitive("data").getAsString());
        } catch (Exception e) {
            return PlatformServiceUtil.buildFailureResponse(e.toString());
        }
    }
    
    /**
	 * Get Audit Report for Traceability from HL
	 * @param assetsResults - Dynamic table content
	 * 
	 */
	@PostMapping(value = "/getAuditReport")
	@ResponseBody
	public ResponseEntity<byte[]> getAuditReport(@RequestBody JsonObject assetsResults,@RequestParam String pdfName) {
		ResponseEntity<byte[]>  response = null;
		try {
			Log.debug(" Inside getAuditReport  ====== ");
			String validatedResponse = ValidationUtils.validateRequestBody(assetsResults.toString());
			String validatedPdfName = StringEscapeUtils.escapeHtml(ValidationUtils.cleanXSS(pdfName));
			boolean validInput = false;
			JsonObject assetResults = JsonUtils.parseStringAsJsonObject(validatedResponse);
			JsonArray jsonarray = assetResults.get("data").getAsJsonArray();
			for(JsonElement jsonelement: jsonarray) {
				JsonObject element = jsonelement.getAsJsonObject();
				if(element.has(InsightsAuditConstants.TIMESTAMP) && element.get(InsightsAuditConstants.TIMESTAMP)!=null &&
						!element.get(InsightsAuditConstants.TIMESTAMP).getAsString().isEmpty()) {
					validInput = true;
				}
			}
			if(validInput) {
				byte[] fileContent = pdfWriterImpl.generatePdf(jsonarray, validatedPdfName);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("application/pdf"));
				headers.add("Access-Control-Allow-Methods", "POST");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "attachment; filename="+ validatedPdfName);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
			}else {
				throw new Exception("Invalid Response from Ledger , No timestamp found!");
			}
		} catch (Exception e) {
			Log.error("Error, Failed to download pdf -- {} {}" , pdfName, e.getMessage());
		}
		return response;
	}

	//Get the process json for UI pipeline
    @GetMapping(value = "/getProcessFlow",produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonObject getProcessFlow() {
        JsonObject response = new JsonObject();
        try {
        	Log.debug(" Inside getProcessFlow  ====== ");
            response = auditServiceImpl.getProcessFlow();
        } catch (Exception e) {
            return PlatformServiceUtil.buildFailureResponse(e.toString());
        }
        return response;
    }
    
    /**
     * Downloads report log file based on report name.
     * @param logFileName
     * @return
     */
	/*@GetMapping(value = "/getReportLog")
	@ResponseBody
	public ResponseEntity<String> getReportLog(@RequestParam("logFileName") String logFileName) {
		ResponseEntity<String>  response = null;
		try {
			String path = System.getenv().get("INSIGHTS_HOME") + File.separator + "logs" + File.separator + "PlatformAuditService" + File.separator + "Reports"+ File.separator + logFileName;
			String validatedResponse = ValidationUtils.validateRequestBody(logFileName);
			JsonObject fileDetailsJson = JsonUtils.parseStringAsJsonObject(validatedResponse);
			boolean checkValidFile = PlatformServiceUtil.validateFile(validatedResponse);
			if(checkValidFile) {
				byte[] fileContent = Files.readAllBytes(Paths.get(new File(path).getAbsolutePath()));
				String fileData = ValidationUtils.cleanXSS(new String(fileContent, StandardCharsets.UTF_8).replace("\n", "").replace("\r", "").replace("\t", ""));
				boolean isValid = PlatformServiceUtil.checkFileForHTML(new String(fileContent));
				if(isValid) {
					String mediaType = "application/" + fileDetailsJson.get("fileType").getAsString().toLowerCase();
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.parseMediaType(mediaType));
					headers.add("Access-Control-Allow-Methods", "POST");
					headers.add("Content-Disposition", "attachment; filename="+logFileName);
					headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
					headers.add("Pragma", "no-cache");
					headers.add("Expires", "0");
					headers.add("Accept-Encoding", "UTF-8");
					response = new ResponseEntity<>(fileData, headers, HttpStatus.OK);
				}else {
					throw new Exception("Invalid file content -- "+new String(fileContent));
				}
			}
		} catch (Exception e) {
			Log.error("Error, Failed to download Log file --%s %s", logFileName, e.getMessage());
		}
		return response;
	}
	*/
}
