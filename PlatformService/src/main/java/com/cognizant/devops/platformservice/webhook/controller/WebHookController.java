package com.cognizant.devops.platformservice.webhook.controller;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.settingsconfig.SettingsConfiguration;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentConfigTO;
import com.cognizant.devops.platformservice.rest.neo4j.GraphDBService;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.webhook.service.WebHookConfigTO;
import com.cognizant.devops.platformservice.webhook.service.WebHookService;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Stream;

@RestController
@RequestMapping("/admin/webhook")
public class WebHookController {
	 static Logger log = LogManager.getLogger(WebHookController.class.getName());
	@Autowired	
	WebHookService webhookConfigurationService;
	
	
	@RequestMapping(value = "/webhookConfiguration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject saveSettingsConfiguration(@RequestParam String webhookname, @RequestParam String toolName,
													@RequestParam String eventname,@RequestParam String dataformat,@RequestParam String mqchannel, @RequestParam Boolean subscribestatus) {
	  log.debug(webhookname,toolName);
		 Boolean result = webhookConfigurationService.saveWebHookConfiguration(webhookname,toolName,eventname,dataformat,mqchannel,subscribestatus); 
		
		if (result) {
			log.error(result);
			return PlatformServiceUtil.buildSuccessResponse();
		} else {
			return PlatformServiceUtil.buildFailureResponse("Unable to save or update Setting Configuration for the request");
		} 
	  	}
	
	
	@RequestMapping(value = "/loadwebhookConfiguration", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getRegisteredAgents() {
		List<WebHookConfigTO> webhookList;
		try {
			webhookList = webhookConfigurationService.getRegisteredWebHooks();
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(webhookList);
	}
	
	@RequestMapping(value = "/uninstallWebHook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject uninstallAgent(@RequestParam String webhookname) {
		String message = null;
		try {
			message = webhookConfigurationService.uninstallWebhook(webhookname);
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
	
	@RequestMapping(value = "/updateWebhook", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject updateAgent(@RequestParam String webhookname, @RequestParam String toolName,
			@RequestParam String eventname,@RequestParam String dataformat,@RequestParam String mqchannel, @RequestParam Boolean subscribestatus) {
		String message = null;
		try {
			message = webhookConfigurationService.updateWebHook(webhookname,toolName,eventname,dataformat,mqchannel,subscribestatus); 
		} catch (InsightsCustomException e) {
			return PlatformServiceUtil.buildFailureResponse(e.toString());
		}
		return PlatformServiceUtil.buildSuccessResponseWithData(message);
	}
}
