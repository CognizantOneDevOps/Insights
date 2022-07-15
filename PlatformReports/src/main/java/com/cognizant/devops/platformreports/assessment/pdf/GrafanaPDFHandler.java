/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
package com.cognizant.devops.platformreports.assessment.pdf;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowType;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentConfiguration;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.icon.IconDAL;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.GrafanaReportConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Page.PdfOptions;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.Media;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

public class GrafanaPDFHandler implements BasePDFProcessor {

	private static Logger log = LogManager.getLogger(GrafanaPDFHandler.class);
	
	private static final String HEADER_HTML = "header.html";
	private static final String FOOTER_HTML = "footer.html";
	private static final String DASHBOARD = "Dashboard";
	
	private static final String PDF_TYPE = " PDFType: ";
	private static final String SCHEDULE = " Schedule: ";
	private static final String SOURCE = " Source: ";
		
	private static final int TRANSITION_TIME = 1000;

	private static final String DASHBOARD_REPORT_DIR = "dashboardReportTemplate";
	private static final String DASHBOARD_ELEMENT_JSON_FILE = "dashboardReportElement.json";
	private static final String FRONT_PAGE_TEMPLATE = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + DASHBOARD_REPORT_DIR + File.separator + "frontPageTemplate.html";
	private static final String LOGO_IMAGE_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + DASHBOARD_REPORT_DIR + File.separator + "image.webp";
	private static final String MODIFIED_FRONT_PAGE_TEMPLATE = "frontPage.html";
	private static final String DASHBOARD_ELEMENT_JSON_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + DASHBOARD_REPORT_DIR + File.separator + DASHBOARD_ELEMENT_JSON_FILE;
	private static final String LOG_MESSAGE = "Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";

	private static final String ORGANISATION = "organisation";
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	GrafanaReportConfigurationDTO pdfconfigDto = new GrafanaReportConfigurationDTO();
	PDFExecutionUtils pdfExecutionUtils = new PDFExecutionUtils();


	@Override
	public void generatePDF(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			
			createPDFDirectory(assessmentReportDTO);
			
			preparePDFDTO(assessmentReportDTO);

			prepareAndExportPDFFile(assessmentReportDTO);


		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException(e.getMessage());
		}

	}


	
	/**
	 * Method to create PDF execution directory.
	 * 
	 * @param assessmentReportDTO
	 */
	private void createPDFDirectory(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			String folderName = WorkflowType.GRAFANADASHBOARDPDFREPORT + "_"
					+ assessmentReportDTO.getExecutionId();
			assessmentReportDTO.setPdfReportFolderName(folderName);
			String reportExecutionFile = AssessmentReportAndWorkflowConstants.REPORT_PDF_EXECUTION_RESOLVED_PATH + folderName;
			assessmentReportDTO.setPdfReportDirPath(reportExecutionFile);
			File reportExecutionFolder = new File(reportExecutionFile);
			reportExecutionFolder.mkdir();			
		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException(
					"Unable to create pdf execution directory, message == {} " + e.getMessage());
		}
	}
	
	private void preparePDFDTO(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL
					.fetchGrafanaDashboardDetailsByWorkflowId(assessmentReportDTO.getWorkflowId());
			
			if(grafanaDashboardPdfConfig !=null) {
				
				JsonObject dashboardConfigJson = JsonUtils.parseStringAsJsonObject(grafanaDashboardPdfConfig.getDashboardJson());
				pdfconfigDto.setDashboardJson(grafanaDashboardPdfConfig.getDashboardJson());
				pdfconfigDto.setOrganisation(dashboardConfigJson.get(ORGANISATION).getAsString());
				pdfconfigDto.setPdfType(grafanaDashboardPdfConfig.getPdfType());
				pdfconfigDto.setScheduleType(grafanaDashboardPdfConfig.getScheduleType());
				pdfconfigDto.setSource(grafanaDashboardPdfConfig.getSource());
				pdfconfigDto.setTitle(grafanaDashboardPdfConfig.getTitle());
				pdfconfigDto.setVariables(grafanaDashboardPdfConfig.getVariables());
				pdfconfigDto.setWorkflowType(grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowType());
			}else {
				InsightsWorkflowConfiguration workflowConfig = workflowDAL.getWorkflowConfigByWorkflowId(assessmentReportDTO.getWorkflowId());
				InsightsAssessmentConfiguration assessmentConfig = workflowConfig.getAssessmentConfig();
				
				if(workflowConfig != null && assessmentConfig != null) {
					JsonObject dashboardConfigJson = JsonUtils.parseStringAsJsonObject(assessmentConfig.getAdditionalDetail());
					pdfconfigDto.setDashboardJson(assessmentConfig.getAdditionalDetail());
					pdfconfigDto.setOrganisation(dashboardConfigJson.get(ORGANISATION).getAsString());
					pdfconfigDto.setPdfType(dashboardConfigJson.get("pdfType").getAsString());
					pdfconfigDto.setScheduleType(workflowConfig.getScheduleType());
					pdfconfigDto.setSource(dashboardConfigJson.get("source").getAsString());
					pdfconfigDto.setTitle(assessmentConfig.getAsseementReportDisplayName());
					pdfconfigDto.setVariables(dashboardConfigJson.get("variables").getAsString());
					pdfconfigDto.setWorkflowType(workflowConfig.getWorkflowType());
					pdfconfigDto.setIsAssessmentReport(true);
				}
			}
			
		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data ", e);
			log.error(LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					"-","-","-",0," exeception in preparePDFDTO  " + e.getMessage());
			throw new InsightsJobFailedException(" exeception in preparePDFDTO " + e);
		}
	}


	/**
	 * Method to generate grafana dashboard as PDF and save in database.
	 * 
	 * @param assessmentReportDTO
	 */
	private void prepareAndExportPDFFile(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			long startTime = System.nanoTime();
						
			log.debug("Worlflow Detail ==== GrafanaDashboardPdfConfig from UI ===== ");
			assessmentReportDTO.setAsseementreportname(pdfconfigDto.getTitle());
			String exportedFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ (assessmentReportDTO.getWorkflowId()+"_"+assessmentReportDTO.getExecutionId()) + "." + ReportEngineUtils.REPORT_TYPE;
			assessmentReportDTO.setPdfExportedFilePath(exportedFilePath);
			String pdfType = pdfconfigDto.getPdfType();
			if (pdfType.equals(DASHBOARD)) {
				grafanaDashboardAsPdf(assessmentReportDTO, exportedFilePath);
			} else {
				printableDashboardAsPdf(assessmentReportDTO, exportedFilePath);
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Worlflow Detail ==== PDF generation completed for Type : ===== {} ", pdfType);
			log.debug(
					LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), pdfconfigDto.getWorkflowType(),
					"-", "-", processingTime,
					PDF_TYPE + pdfconfigDto.getPdfType() + SCHEDULE
							+ pdfconfigDto.getScheduleType() + SOURCE
							+ pdfconfigDto.getSource() 
							+ " PDF generation completed");
			
			deletePDFDirectory(new File(assessmentReportDTO.getPdfReportDirPath()));
		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data ", e);
			log.error(LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					"-","-","-",0," unable to prepare pdf data " + e.getMessage());
			throw new InsightsJobFailedException(" unable to prepare pdf data " + e);
		}
	}


	/**
	 * Uses playwright to create current url of grafana dashboard as pdf
	 * @param assessmentReportDTO 
	 * @param grafanaDashboardConfig 
	 * @param incomingTaskMessageJson
	 * @param exportedFilePath 
	 */
	private void grafanaDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO, String exportedFilePath) {

		JsonElement config = JsonUtils.parseStringAsJsonElement(pdfconfigDto.getDashboardJson());
		String grafanaEndpoint = getGrafanaEndPoint();
		int loadTime = config.getAsJsonObject().get("loadTime").getAsInt() * 1000;
		log.debug("Worlflow Detail ==== LoadTIme configured for Grafana in milliseconds ===== {} ",loadTime);
		String grafanaUrl = config.getAsJsonObject().get("dashUrl").getAsString().replace("<GRAFANA_URL>", grafanaEndpoint);
		log.debug("Worlflow Detail ==== grafanadashboard Url ===== {} ",grafanaUrl);
		Playwright playwright = null;
		String pageContent = "";
		
		
    	playwright = Playwright.create();
		long startTime = System.nanoTime();
		BrowserType browserType = 	playwright.chromium();
		LaunchOptions launchOptions = new LaunchOptions();
		launchOptions.setHeadless(Boolean.TRUE);
		launchOptions.setDevtools(Boolean.FALSE);
		
		Browser browser = browserType.launch(launchOptions);
		
		try(    BrowserContext context = browser.newContext();
				Page page = context.newPage();) {			

			Map<String, String> headers = getGrafanaHeaders(config);

			page.setExtraHTTPHeaders(headers);
			page.onRequest(request -> log.debug("Request >> {} {} ", request.method(), request.url()));
		    page.onResponse(response -> log.debug("Response << {} {} ",response.status(), response.url()));
		    
			NavigateOptions navigateOptions = new NavigateOptions();
			navigateOptions.setWaitUntil(WaitUntilState.NETWORKIDLE);
			
			page.navigate(grafanaUrl,navigateOptions);
			pageContent = page.content();
			page.waitForLoadState();
			log.debug("Worlflow Detail ==== Waiting for dashboard to load == loadTime {} time {} ",loadTime,Instant.now());
			page.waitForTimeout(loadTime);
			log.debug("Worlflow Detail ====Waiting exit post configured time to load Dashboard == {} ", Instant.now());
			
			String dashboardElement = readDashboardElementJson();
			log.debug("Worlflow Detail ==== reading elements from dashboardReportElement.json");
			JsonObject dashboardElementJson = JsonUtils.parseStringAsJsonObject(dashboardElement).get("dashboard").getAsJsonObject();
			JsonArray elementList = dashboardElementJson.get("elements").getAsJsonArray();
			for(JsonElement ele: elementList) {
				page.evaluate(ele.getAsString());
			}

			Object width = page.evaluate(dashboardElementJson.get("width").getAsString());
			log.debug("Worlflow Detail ==== Grafana grid Width ===== {} ",width);
			int dashboardwidth = width instanceof Integer ? (int) width + 40 : Integer.parseInt(width.toString());
			log.debug("Worlflow Detail ==== Grafana Dashboard width ===== {} ",dashboardwidth);
			Object height = page.evaluate(dashboardElementJson.get("height").getAsString());
			log.debug("Worlflow Detail ==== Grafana grid height ===== {} ",height);
			int dashboardlength = height instanceof Integer ? (int) height + 160 : Integer.parseInt(height.toString());
			log.debug("Worlflow Detail ==== Grafana Dashboard length ===== {} ",dashboardlength);
			page.evaluate(dashboardElementJson.get("screenshotElement").getAsString());
			log.debug("Waiting for dashboard to load completely before screenshot == {} ",Instant.now());
			page.waitForTimeout(loadTime);
			log.debug("Waiting time completed for dashboard == {} ",Instant.now());
			page.waitForLoadState(LoadState.NETWORKIDLE);
			
			PdfOptions pdfOptions =  new PdfOptions();
			pdfOptions.setPrintBackground(Boolean.TRUE);
			pdfOptions.setWidth(dashboardwidth+"px");
			pdfOptions.setHeight(dashboardlength+"px");
			pdfOptions.setScale(1);
			pdfOptions.setDisplayHeaderFooter(true);
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(false));
			pdfOptions.setFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin(new Margin().setTop("90").setRight("0").setBottom("50").setLeft("0"));
			page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
			byte[] dashboardPdf = page.pdf(pdfOptions);
			log.debug("Worlflow Detail ==== dashboard pdf generated ===");
			
			String frontPagePath = assessmentReportDTO.getPdfReportDirPath() + File.separator + MODIFIED_FRONT_PAGE_TEMPLATE;
			modifyFrontPageTemplate(frontPagePath);
			page.navigate("file:" +new File(frontPagePath).getAbsolutePath());
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(true));
			pdfOptions.setFooterTemplate(fetchTemplate("frontPagefooter.html"));
			pdfOptions.setHeight("1600px");
			byte[] frontPagePdf = page.pdf(pdfOptions);
		
			browser.close();
			
			byte[] finalPdf = mergePDFFiles(frontPagePdf, dashboardPdf, exportedFilePath);
			
			if(Boolean.FALSE.equals(pdfconfigDto.getIsAssessmentReport())) {
				pdfExecutionUtils.saveToVisualizationContainer(assessmentReportDTO, finalPdf);
			}			
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					pdfconfigDto.getWorkflowType(),"-","-",processingTime,
					PDF_TYPE + pdfconfigDto.getPdfType() +
					SCHEDULE + pdfconfigDto.getScheduleType() +
					SOURCE + pdfconfigDto.getSource());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as PDF Completed with error {} ", e.getMessage());
			log.error(LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					"-","-","-",0," Grafana Dashboard export as PDF Completed with error " + e.getMessage());
			log.error("Dashboard page content ========== {} ",pageContent);
			log.error(e);
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
				log.error(LOG_MESSAGE,
						assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
						"-","-","-",0," Unable to close Playwright " + e.getMessage());
			}
		}
	}

	/** This method use to prepare Grafana header object 
	 * @return map of headers 
	 */
	private Map<String, String> getGrafanaHeaders(JsonElement config) {
		GrafanaOrgToken grafanaOrgToken = grafanaDashboardConfigDAL.getTokenByOrgId(config.getAsJsonObject().get(ORGANISATION).getAsInt());
		String token = "Bearer "+AES256Cryptor.decrypt(grafanaOrgToken.getApiKey(), AssessmentReportAndWorkflowConstants.GRAFANA_PDF_TOKEN_SIGNING_KEY);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", token);
		return headers;
	}

	/**
	 * Generates printable pdf from Grafana dashboard panels.
	 * @param assessmentReportDTO 
	 * @param grafanaDashboardConfig
	 * @param incomingTaskMessageJson
	 * @param exportedFilePath 
	 */
	private synchronized void printableDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO, String exportedFilePath) {
		Playwright playwright = null;
		
		playwright = Playwright.create();
		long startTime = System.nanoTime();
		BrowserType browserType = 	playwright.chromium();
		LaunchOptions launchOptions = new LaunchOptions();
		launchOptions.setHeadless(Boolean.TRUE);
		launchOptions.setDevtools(Boolean.FALSE);
		Browser browser = browserType.launch(launchOptions);
		
		try(BrowserContext context = browser.newContext();
			Page page = context.newPage();) {
			JsonElement config = JsonUtils.parseStringAsJsonElement(pdfconfigDto.getDashboardJson());
			int loadTime = config.getAsJsonObject().get("loadTime").getAsInt() * 1000;
			log.debug("Worlflow Detail ==== LoadTIme configured for Grafana in milliseconds ===== {} ",loadTime);
			String grafanaEndpoint = getGrafanaEndPoint();
			log.debug("Worlflow Detail ==== grafanaEndpoint from config ===== {} ",grafanaEndpoint);
			JsonArray panelArray = config.getAsJsonObject().get("panelUrls").getAsJsonArray();
			List<String> grafanaPanelList = new ArrayList<>(0);
			panelArray.forEach(e -> {
				String panelUrl = e.getAsString().replace("<GRAFANA_URL>", grafanaEndpoint);
				log.debug("Worlflow Detail ==== Panel url ===== {} ",panelUrl);
				grafanaPanelList.add(panelUrl);
			});

			Map<String, String> headers = getGrafanaHeaders(config);

			NavigateOptions navigateOptions = new NavigateOptions();
			navigateOptions.setWaitUntil(WaitUntilState.NETWORKIDLE);
			
			String dashboardElement = readDashboardElementJson();
			log.debug("Worlflow Detail ==== dashboardReportElement.json fetched successfully");
			JsonObject dashboardElementJson = JsonUtils.parseStringAsJsonObject(dashboardElement).get("Printable").getAsJsonObject();
			JsonArray elementList = dashboardElementJson.get("elements").getAsJsonArray();
			
			List<byte[]> imageList = new ArrayList<>();
			for(int idx=0;idx<grafanaPanelList.size();idx++) {

				page.setExtraHTTPHeaders(headers);
				page.waitForTimeout(TRANSITION_TIME);
				page.setViewportSize(1200, 800);
				page.route("**", route -> route.resume());
				page.onRequest(request -> log.debug("Request >> {} {}  ", request.method(), request.url()));
			    page.onResponse(response -> log.debug("Response << {} {} ",response.status(), response.url()));
				page.navigate(grafanaPanelList.get(idx),navigateOptions);
				Page.WaitForSelectorOptions waitForSelectorOptions = new Page.WaitForSelectorOptions();
				waitForSelectorOptions.setState(WaitForSelectorState.ATTACHED);
				log.debug("Waiting for panel {} to load completely before screenshot == {} ",idx,Instant.now());
				page.waitForTimeout(loadTime);
				log.debug("Waiting time completed for dashboard == {} ",Instant.now());
				for(JsonElement ele: elementList) {
					page.evaluate(ele.getAsString());
				}
				ElementHandle elementHandle = page.querySelector(dashboardElementJson.get("screenshotElement").getAsString());
				page.waitForTimeout(TRANSITION_TIME);
				byte[] image = elementHandle.screenshot();
				imageList.add(image);
			}
			String dynamicTemplate = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.HTML_EXTENSION;
			log.debug("Worlflow Detail ==== Generated DynamicTemplate.html name  ===== {} ",dynamicTemplate);
			prepareHtml(imageList,dynamicTemplate);
			page.navigate(new File(dynamicTemplate).getAbsolutePath());
			PdfOptions pdfOptions =  new PdfOptions();
			pdfOptions.setPrintBackground(Boolean.TRUE);
			pdfOptions.setDisplayHeaderFooter(true);
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(false));
			pdfOptions.setFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin(new Margin().setTop("85").setRight("10").setBottom("5").setLeft("10"));
			byte[] dashboardPdf = page.pdf(pdfOptions);
			
			String frontPagePath = assessmentReportDTO.getPdfReportDirPath() + File.separator + MODIFIED_FRONT_PAGE_TEMPLATE;
			modifyFrontPageTemplate(frontPagePath);
			page.navigate("file:" +new File(frontPagePath).getAbsolutePath());
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(true));
			pdfOptions.setFooterTemplate(fetchTemplate("frontPagefooter.html"));
			byte[] frontPagePdf = page.pdf(pdfOptions);
			
			browser.close();
			
			byte[] finalPdf = mergePDFFiles(frontPagePdf, dashboardPdf, exportedFilePath);
			
			pdfExecutionUtils.saveToVisualizationContainer(assessmentReportDTO, finalPdf);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					pdfconfigDto.getWorkflowType(),"-","-",processingTime,
					PDF_TYPE + pdfconfigDto.getPdfType() +
					SCHEDULE +pdfconfigDto.getScheduleType() +
					SOURCE +pdfconfigDto.getSource());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as Printable PDF Completed with error {} ", e.getMessage());
			log.error(LOG_MESSAGE,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					"-","-","-",0," Grafana Dashboard export as Printable PDF Completed with error " + e.getMessage());
			log.error(e);
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
				log.error(LOG_MESSAGE,
						assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
						"-","-","-",0," Unable to close Playwright " + e.getMessage());
			}
		}

	}
	

	/**
	 * prepare dynamic HTML for PDF generation
	 * 
	 * @param imageList
	 * @param dynamicTemplate
	 * @param grafanaDashboardConfig
	 */
	private void prepareHtml(List<byte[]> imageList, String dynamicTemplate ) {

		StringBuilder htmlStartTag  = new StringBuilder();
		htmlStartTag.append("<!DOCTYPE html><html><head>")
					.append("<link rel=\"stylesheet\" href=\"template.css\" />")
					.append("<title>")
					.append(pdfconfigDto.getTitle())
					.append("</title>")
					.append("</head><body>");
		String rowStart = "<div class=\"row\">";
		String columnStart = "<div class=\"column\">";
		String endDiv = "</div>";
		String htmlEnd = "</body></html>";
		StringBuilder baseImage = new StringBuilder();
		for(byte[] image: imageList){
			String imgTag = "<img src=\"data:image/jpeg;base64,";
			String imgEndTag = "\" alt=\"Snow\" style=\"width:100%\"></img>";
			baseImage.append(rowStart).append(columnStart).append(imgTag)
					 .append(Base64.getEncoder().encodeToString(image))
					 .append(imgEndTag).append(endDiv).append(endDiv);
		}

		baseImage.append(htmlEnd);

		StringBuilder template = new StringBuilder();
		template.append(htmlStartTag).append(baseImage);

		log.debug("Worlflow Detail ==== Generated DynamicTemplate.html content  ===== {} ",template);

		saveHtmlFile(dynamicTemplate, template);
		
	}
	
	
	/**
	 * Method use to fetch template file from resource.
	 * 
	 * @param templateFileName
	 * @return
	 */
	private String fetchTemplate(String templateFileName) {
		InputStream templateStream = getClass().getClassLoader().getResourceAsStream(templateFileName);
		BufferedReader r = new BufferedReader(new InputStreamReader(templateStream));
		String line;
		StringBuilder render = new StringBuilder();
		try {
			while ((line = r.readLine()) != null) {
				render.append(line);
			}
			r.close();
		} catch (IOException e) {
			log.error("Workflow Detail ==== Error fetching template ", e);
		}
		return render.toString();
	}


	/**
	 * Method use to fetch grafana end point details.
	 * 
	 * @return
	 */
	private String getGrafanaEndPoint() {
		return ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	}

	
	/**
	 * Method use to save PDF file in PDF execution directory.
	 * 
	 * @param extractedPdfFile
	 * @param pdfResponse
	 */
	public void savePDFFile(File extractedPdfFile, byte[] pdfResponse) {
		try(FileOutputStream outStream = new FileOutputStream(extractedPdfFile)) {
			outStream.write(pdfResponse);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while saving pdf ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
	}
	
	/**
	 * Method use to segregate dashboard filter details in JsonObject.
	 * 
	 * @param variables
	 * @return JsonObject
	 */
	public JsonObject formatVariables(String variables) {
		JsonObject variableJson = new JsonObject();
		if (!variables.isEmpty()) {
			List<String> varList = Arrays.asList(variables.split(","));
			varList.forEach(var -> {
				JsonArray array = new JsonArray();
				String[] items = var.split("=");
				if (variableJson.has(items[0])) {
					JsonArray value = variableJson.get(items[0]).getAsJsonArray();
					value.add(items[1]);
				} else {
					array.add(items[1]);
					variableJson.add(items[0], array);
				}
			});
			variableJson.remove("from");
			variableJson.remove("to");
			log.debug(variableJson);
		}
		return variableJson;

	}
	
	/**
	 * Method use to update filter details in front page template.
	 * 
	 * @param frontPagePath
	 * @param grafanaDashboardConfig
	 */
	private void modifyFrontPageTemplate(String frontPagePath ) {
		try {
			String templateFilePath = new File(FRONT_PAGE_TEMPLATE).getCanonicalPath();
			File file = new File(templateFilePath);
			StringBuilder render = new StringBuilder();
			if (file.exists()) {
				try(BufferedReader in = new BufferedReader(new FileReader(templateFilePath))) {				
				String line;
				while((line = in.readLine())!=null)
					render.append(line);
				}catch(Exception e){
					e.getMessage();					
				}
								
			} else {
				String frontPageTemplate = fetchTemplate("frontPageTemplate.html");
				render = new StringBuilder(frontPageTemplate);
			}

			int titleIndex = render.indexOf("</h1>");
			render.insert(titleIndex, pdfconfigDto.getTitle());
			render.insert(render.indexOf("</span>"), new Date().toString());
			String fontsize = "";
			if(pdfconfigDto.getPdfType().equalsIgnoreCase(DASHBOARD)) {
				fontsize = "160%;";
			} else {
				fontsize = "110%;";
			}
			render.insert(render.indexOf("body {")+6, "font-size:"+fontsize);
			JsonObject variableJson = formatVariables(pdfconfigDto.getVariables());
			if(!variableJson.keySet().isEmpty()) {		
				StringBuilder tableRow = new StringBuilder();
				variableJson.keySet().forEach(key -> {
					String value = variableJson.get(key).getAsJsonArray().toString().replace("[", "").replace("\"", "").replace("]", "");
					String row = "<tr>\r\n" + 
							"            <td style=\"padding: 0.5%;width:20%;\">"+key+"</td>\r\n" + 
							"            <td style=\"padding: 0.5%;width:80%;\">"+value+"</td>\r\n" + 
							"        </tr>";
					tableRow.append(row);
				});
				render.insert(render.indexOf("</thead>"), "<tr>\r\n" + 
						"<th>Filter Name</th>\r\n" + 
						"<th>Values</th>\r\n" + 
						"</tr>");
				int index = render.indexOf("</thead>");
				render.insert(index+8, tableRow);
			}
			saveHtmlFile(frontPagePath, render);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while generating front page ", e);
		}
	}
	
	/**
	 * Method to delete PDF execution directory.
	 * 
	 * @param file
	 */
	private void deletePDFDirectory(File file) {
		try {
			FileUtils.deleteDirectory(file);	
			log.debug("Worlflow Detail ==== Pdf execution directory deleted successfully ===== ");
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Unable to delete pdf execution directory, message == {} ", e.getMessage());
			
		}
	}
	
	/**
	 * Method to save Html File.
	 * 
	 * @param fileName
	 * @param fileContent
	 */
	private void saveHtmlFile(String fileName, StringBuilder fileContent) {
		PrintWriter printWriter = null;
		try(FileWriter fileWriter = new FileWriter(fileName)) {
			printWriter = new PrintWriter(fileWriter);
			printWriter.print(fileContent);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while saving Html file  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			if(printWriter != null) {
				printWriter.close();
			}
		}
	}
	
	
	/**
	 * Method to merge two PDF files, save in destination folder and returns merged pdf byte array.
	 * 
	 * @param pdf1
	 * @param pdf2
	 * @param destinationFilePath
	 * @return 
	 */
	private byte[] mergePDFFiles(byte[] pdf1, byte[] pdf2, String destinationFilePath) {
		try {
			PDFMergerUtility merger = new PDFMergerUtility();
			merger.addSource(new ByteArrayInputStream(pdf1));
			merger.addSource(new ByteArrayInputStream(pdf2));
			merger.setDestinationFileName(destinationFilePath);
			merger.mergeDocuments(null);
			log.debug("Worlflow Detail ==== Pdf files merged successfully ===== ");
			FileInputStream inputStream = new FileInputStream(destinationFilePath);
			return IOUtils.toByteArray(inputStream);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while merging PDF Files  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		} 
		
	}
	
	/**
	 * Method use to fetch Logo Image from database or filesystem and update in header template.
	 * 
	 * @return
	 */
	private String updateLogoImgInHeaderTemplate(Boolean isFrontPage) {
		try {
			String headerContent = fetchTemplate(HEADER_HTML);
			StringBuilder headerTemplate = new StringBuilder(headerContent);
			byte[] image = null;
			Icon logoEntity = new IconDAL().fetchEntityData("logo");
			if(logoEntity.getImage() != null) {
				image = logoEntity.getImage();
			} else {
				InputStream imageStream = new FileInputStream(new File(LOGO_IMAGE_PATH).getCanonicalPath());
				image = IOUtils.toByteArray(imageStream);
			}
			StringBuilder imageTag = new StringBuilder();
			imageTag.append("src=\"data:image/webp;base64,")
			.append(Base64.getEncoder().encodeToString(image)).append("\" alt=\"OneDevOps\" ");
			int index = headerTemplate.indexOf("<img");
			headerTemplate.insert(index+5, imageTag);
			if(Boolean.FALSE.equals(isFrontPage)) {
				String titleSpan = "<span style=\"padding-left: 15%;\" float: middle;>"+pdfconfigDto.getTitle()+"</span>";
				int titleIndex = headerTemplate.indexOf("</div>");
				headerTemplate.insert(titleIndex, titleSpan);
			}
			return headerTemplate.toString();
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while updating logo image in header template  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}		
	}
	
	private String readDashboardElementJson() {
		String dashboardElementContent = null;
		try {
			String dashboardElementFilePath = new File(FilenameUtils.normalize(DASHBOARD_ELEMENT_JSON_PATH)).getCanonicalPath();
			File dashboardElementFile = new File(dashboardElementFilePath);
			if(dashboardElementFile.exists()) { 
				dashboardElementContent = new String(Files.readAllBytes(Paths.get(dashboardElementFilePath)));
			} else {
				dashboardElementContent = fetchTemplate(DASHBOARD_ELEMENT_JSON_FILE);
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while reading dashboard report element Json ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
		return dashboardElementContent;	
	}
	
}