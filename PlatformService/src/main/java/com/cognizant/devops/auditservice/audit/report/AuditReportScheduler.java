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
package com.cognizant.devops.auditservice.audit.report;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.SystemStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Runs everyday Midnight to Fetch Query_builder table records .
 * 
 */
@Deprecated
@Component
@Configuration
@EnableScheduling
public class AuditReportScheduler {

	private static final Logger log = LoggerFactory.getLogger(AuditReportScheduler.class.getName());
	
	private static final String QUERY_BUILDER_FETCH_QUERIES = "/PlatformService/blockchain/queryBuilder/fetchQueries";
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	private static final String CYPHER = "CYPHER";
	private static final String LEDGER = "LEDGER";

	/**
	 * <second> <minute> <hour> <day-of-month> <month> <day-of-week> <year>
	 */
	@Scheduled(cron = "0 0 0 * * ?")//At 00:00:00am every day
	void dailyReport(){
		fetchReports("Daily");
	}
	 
	@Scheduled(cron = "0 0 0 */7 * ?")// At 00:00:00am, every 7 days starting on the 1st, every month
	void weeklyReport(){
		fetchReports("Weekly");
	}
	
	@Scheduled(cron = "0 0 0 15 * ?")// At 00:00:00am every 15th day of month 
	void fornightlyReport(){
		fetchReports("Fortnightly");
	}
	
	@Scheduled(cron = "0 0 0 28-31 * ?")// At 00:00:00am, on the last day of the month, every month
	void monthlyReport(){
		final Calendar c = Calendar.getInstance();
	    if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
	    	fetchReports("Monthly");
	    }
	}

	/**
	 * Fetch Reports based on frequency.
	 * @param string
	 */
	private void fetchReports(String frequency) {
		try{

			log.info("@Scheduler started at --   -"+dateFormat.format(new Date()));
			JsonObject jsonObject = getReportList();
			log.info("API response status  -"+jsonObject.get("status").getAsString());
			if("success".equalsIgnoreCase(jsonObject.get("status").getAsString())){
				JsonArray process = jsonObject.getAsJsonArray("data");
				processReports(process, frequency);
			}else{
				log.info("Unable to Fetch queries from DB!");
			}
		}catch(Exception e){
			log.error("Error  - {} ", e.getMessage());
		}
	}
	
	/**
	 * For testing purpose only. will be removed once done.
	 * @param string
	 */
	public void testReports(String reportName, String frequency) {
		try{

			log.info("@Scheduler started at --   -"+dateFormat.format(new Date()));
			JsonObject jsonObject = getReportList();
			log.info("API response status  -"+jsonObject.get("status").getAsString());
			if("success".equalsIgnoreCase(jsonObject.get("status").getAsString())){
				JsonArray process = jsonObject.getAsJsonArray("data");
				testProcessReports(process, reportName, frequency);
			}else{
				log.info("Unable to Fetch queries from DB!");
			}
		}catch(Exception e){
			log.error("Error  -"+e.getMessage());
		}
	}



	/**
	 * Process each query fetched from DB and creates log file each Report.
	 * @param process
	 * @param frequency 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void processReports(JsonArray process, String frequency) throws IOException, UnsupportedEncodingException {
		boolean result = false;
		for(JsonElement element : process) {
			if(element.getAsJsonObject().get("frequency").getAsString().equals(frequency)){

				String reportname = element.getAsJsonObject().get("reportName").getAsString();
				MDC.put("logFileName", reportname);

				log.info("-----------------------------------------------------------");
				log.info("ReportData -- "+element);
				String querypath = element.getAsJsonObject().get("querypath").getAsString();
				String querytype = element.getAsJsonObject().get("querytype").getAsString();
				String subscribers = element.getAsJsonObject().get("subscribers").getAsString();
				File file = new File(querypath);
				log.info("File exists -- "+file.exists());
				if(file.exists() && file.isFile()){
					log.info("Query File is present!");
					String fileContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
					if(LEDGER.equals(querytype)){

						AuditReportStrategy ledger = AuditLedgerReport.getInstance();
						result = ledger.executeQuery(fileContent,URLEncoder.encode(reportname+".pdf","UTF-8"),subscribers);
						reportStatus(result,LEDGER);

					}else if(CYPHER.equals(querytype)){

						AuditReportStrategy cypher = AuditCypherReport.getInstance();
						result = cypher.executeQuery(fileContent,URLEncoder.encode(reportname+".pdf","UTF-8"),subscribers);
						reportStatus(result,CYPHER);

					}else{
						log.error("No Such query type exists");
					}
				}else{
					log.error("File not found in Audit-report folder for report  -",reportname);
				}


				MDC.remove("logFileName");
			}
		}
	}

	private void reportStatus(boolean result, String type) {
		if(result){
			log.info("---"+type+" Report generated successfully---");
		}else{
			log.error("---"+type+" Report generation not successfull--");
		}
	}

	/**
	 * Fetches report list from QUERY_BUILDER table.
	 * @return JsonObject
	 */
	private JsonObject getReportList() {
		JsonObject jsonObject = null;
		try{
			String username = ApplicationConfigProvider.getInstance().getUserId();
			final char[] password = ApplicationConfigProvider.getInstance().getPassword().toCharArray();
			String pwd = new String(password);
			Arrays.fill(password, ' ');
			String url = ApplicationConfigProvider.getInstance().getInsightsServiceURL()
					+QUERY_BUILDER_FETCH_QUERIES;
			log.info("Report API - "+url);
			String reports = SystemStatus.jerseyGetClientWithAuthentication(url, 
					username == null ? "admin" : username, password== null ? "admin" : pwd, null);
			//String reports = "{\"status\":\"success\",\"data\":[{\"id\":89,\"reportName\":\"JIRA\",\"frequency\":\"Daily\",\"subscribers\":\"757943@cognizant.com\",\"querypath\":\"C:\\InSights_Windows_v4.3\\Server2\\INSIGHTS_HOME\\.InSights\\Audit-report\\ProcessJiraChangeHistoryRecords.json\"}]}";
			log.debug("Report List from QUERY_BUILDER  -"+reports);
			JsonParser jsonParser = new JsonParser(); 
			JsonElement jsonElements = jsonParser.parse(reports);
			jsonObject = jsonElements.getAsJsonObject();
		}catch(Exception e){
			log.error("Error fetching records --"+e.getMessage());
		}
		return jsonObject;
	}
	
	
	/**
	 * Testing purpose only will be removed later.
	 * @param process
	 * @param frequency 
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private void testProcessReports(JsonArray process, String reportName, String frequency) throws IOException, UnsupportedEncodingException {
		boolean result = false;
		for(JsonElement element : process) {
			if(element.getAsJsonObject().get("frequency").getAsString().equals(frequency)
					&& element.getAsJsonObject().get("reportName").getAsString().equals(reportName)){

				String reportname = element.getAsJsonObject().get("reportName").getAsString();
				MDC.put("logFileName", reportname);

				log.info("-----------------------------------------------------------");
				log.info("ReportData -- "+element);
				String querypath = element.getAsJsonObject().get("querypath").getAsString();
				String querytype = element.getAsJsonObject().get("querytype").getAsString();
				String subscribers = element.getAsJsonObject().get("subscribers").getAsString();
				File file = new File(querypath);
				log.info("File exists -- "+file.exists());
				if(file.exists() && file.isFile()){
					log.info("FIle Exists - "+file.exists());
					String fileContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
					if(LEDGER.equals(querytype)){

						AuditReportStrategy ledger = AuditLedgerReport.getInstance();
						result = ledger.executeQuery(fileContent,URLEncoder.encode(reportname+".pdf","UTF-8"),subscribers);
						reportStatus(result,LEDGER);

					}else if(CYPHER.equals(querytype)){

						AuditReportStrategy cypher = AuditCypherReport.getInstance();
						result = cypher.executeQuery(fileContent,URLEncoder.encode(reportname+".pdf","UTF-8"),subscribers);
						reportStatus(result,CYPHER);

					}else{
						log.error("No Such query type exists");
					}
				}else{
					log.error("File not found in Audit-report folder for report  -"+reportname);
				}


				MDC.remove("logFileName");
			}
		}
	}
	

}
