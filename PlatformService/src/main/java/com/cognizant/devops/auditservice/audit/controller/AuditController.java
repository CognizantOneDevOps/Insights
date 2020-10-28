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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.auditservice.audit.report.AuditReportScheduler;
import com.cognizant.devops.auditservice.audit.service.AuditService;
import com.cognizant.devops.auditservice.audit.service.PdfWriter;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("traceability")
public class AuditController {
	
	private static Logger Log = LogManager.getLogger(AuditController.class);

    @Autowired
    AuditService auditServiceImpl;
    
    @Autowired
	PdfWriter pdfWriterImpl;

    @RequestMapping(value = "/getAssetInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public JsonObject searchAuditLogByAsset(@RequestParam String assetId) {
        JsonObject response;
        try {
            response = auditServiceImpl.searchAuditLogByAsset(assetId);
            if (response.getAsJsonPrimitive("statusCode").getAsString().equals("200"))
                return PlatformServiceUtil.buildSuccessResponseWithData(response.getAsJsonArray("data"));
            else
                return PlatformServiceUtil.buildFailureResponse(response.getAsJsonPrimitive("data").getAsString());
        } catch (Exception e) {
            return PlatformServiceUtil.buildFailureResponse(e.toString());
        }
    }

    @RequestMapping(value = "/getAllAssets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public JsonObject searchAuditLogByDate(@RequestParam String startDate, @RequestParam String endDate, @RequestParam String toolName) {
        JsonObject response;
        try {
            response = auditServiceImpl.searchAuditLogByDate(startDate, endDate, toolName);
            if (response.getAsJsonPrimitive("statusCode").getAsString().equals("200"))
                return PlatformServiceUtil.buildSuccessResponseWithData(response.getAsJsonArray("data"));
            else
                return PlatformServiceUtil.buildFailureResponse(response.getAsJsonPrimitive("data").getAsString());
        } catch (Exception e) {
            return PlatformServiceUtil.buildFailureResponse(e.toString());
        }
    }

    @RequestMapping(value = "/getAssetHistory", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public JsonObject getAssetHistory(@RequestParam String assetId) {
        JsonObject response = new JsonObject();
        try {
            response = auditServiceImpl.getAssetHistory(assetId);
            if (response.getAsJsonPrimitive("statusCode").getAsString().equals("200"))
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
	@RequestMapping(value = "/getAuditReport", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<byte[]> getAuditReport(@RequestBody JsonObject assetsResults,@RequestParam String pdfName) {
		ResponseEntity<byte[]>  response = null;
		try {
			boolean validInput = false;
			JsonArray jsonarray = assetsResults.get("data").getAsJsonArray();
			for(JsonElement jsonelement: jsonarray) {
				JsonObject element = jsonelement.getAsJsonObject();
				if(element.has("timestamp") && element.get("timestamp")!=null &&
						element.get("timestamp").getAsString()!="") {
					validInput = true;
				}
			}
			if(validInput) {
				byte[] fileContent = pdfWriterImpl.generatePdf(jsonarray, pdfName);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.parseMediaType("application/pdf"));
				headers.add("Access-Control-Allow-Methods", "POST");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				headers.add("Content-Disposition", "attachment; filename="+pdfName);
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
			}else {
				throw new Exception("Invalid Response from Ledger , No timestamp found!");
			}
		} catch (Exception e) {
			Log.error("Error, Failed to download pdf -- {} " , pdfName, e.getMessage());
		}
		return response;
	}

	//Get the process json for UI pipeline
    @RequestMapping(value = "/getProcessFlow", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public JsonObject getProcessFlow() {
        JsonObject response = new JsonObject();
        try {
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
	@RequestMapping(value = "/getReportLog", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> getReportLog(@RequestParam("logFileName") String logFileName) {
		ResponseEntity<byte[]>  response = null;
		try {
			String path = System.getenv().get("INSIGHTS_HOME") + File.separator + "logs" + File.separator + "PlatformAuditService" + File.separator + "Reports"+ File.separator + logFileName;
			//String path = ConfigOptions.REPORT_LOGS_PATH + ConfigOptions.FILE_SEPERATOR + logFileName;
			//System.out.println("Report log path -- "+path);
			//System.out.println("Log File Name -- "+Paths.get(new File(path).getAbsolutePath()).getFileName());
			boolean checkValidFile = PlatformServiceUtil.validateFile(logFileName);
			if(checkValidFile) {
				byte[] fileContent = Files.readAllBytes(Paths.get(new File(path).getAbsolutePath()));
				boolean isValid = PlatformServiceUtil.checkFileForHTML(new String(fileContent));
				if(isValid) {
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.TEXT_PLAIN);
					headers.add("Access-Control-Allow-Methods", "POST");
					headers.add("Content-Disposition", "attachment; filename="+logFileName);
					headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
					headers.add("Pragma", "no-cache");
					headers.add("Expires", "0");
					response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
				}else {
					throw new Exception("Invalid file content -- "+new String(fileContent));
				}
			}
		} catch (Exception e) {
			Log.error("Error, Failed to download Log file -- " + logFileName, e.getMessage());
		}
		return response;
	}
	
	/**
	 * Only for testing purpose . Will be removed later.
	 */
	@RequestMapping(value = "/testQuery", method = RequestMethod.GET)
	@ResponseBody
	public void createReport(@RequestParam String reportName, @RequestParam String frequency) {
		try {
			AuditReportScheduler auditReportScheduler = new AuditReportScheduler();
			auditReportScheduler.testReports(reportName, frequency);
		} catch (Exception e) {
			Log.error(e);
		}
	}

}
