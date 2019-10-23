package com.cognizant.devops.platformservice.grafanadashboard.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformservice.grafanadashboard.service.DashboardService;

@RestController
@RequestMapping("/traceability")
public class DashboardController {

	private static Logger Log = LogManager.getLogger(DashboardController.class);

	@Autowired
	DashboardService dashboardServiceImpl;

	@RequestMapping(value = "/getGrafanaReport", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<byte[]> generatePdf(@RequestParam String dashUrl, @RequestParam String title, @RequestParam String variables) {
		ResponseEntity<byte[]>  response = null;
		Log.debug("DashUrl -- " + dashUrl);
		Log.debug("Title -- " + title);
		Log.debug("Variables -- " + variables);
		try {
			byte[] fileContent = dashboardServiceImpl.downloadPanel(dashUrl, title, variables);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("application/pdf"));
			headers.add("Access-Control-Allow-Methods", "POST");
			headers.add("Access-Control-Allow-Headers", "Content-Type");
			headers.add("Content-Disposition", "attachment; filename=");
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate,post-check=0, pre-check=0");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			response = new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
			Log.info("PDf generation completed !!");
		} catch (Exception e) {
			Log.error("Error, Failed to download Dashboard .. ", e.getMessage());
		}
		return response;
	}
}
