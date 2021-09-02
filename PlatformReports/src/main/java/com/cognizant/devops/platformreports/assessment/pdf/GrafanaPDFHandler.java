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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum.WorkflowType;
import com.cognizant.devops.platformcommons.core.util.AES256Cryptor;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaOrgToken;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	
	private static final String STATUS = " Status: ";
	
	private static final int TRANSITION_TIME = 1000;

	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();


	@Override
	public void generatePDF(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {

			createPDFDirectory(assessmentReportDTO);

			prepareAndExportPDFFile(assessmentReportDTO);


		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException(e.getMessage());
		}

	}


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


	private void prepareAndExportPDFFile(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			long startTime = System.nanoTime();
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL
					.fetchGrafanaDashboardDetailsByWorkflowId(assessmentReportDTO.getWorkflowId());
			log.debug("Worlflow Detail ==== GrafanaDashboardPdfConfig from UI ===== {} ", grafanaDashboardPdfConfig);
			assessmentReportDTO.setAsseementreportname(grafanaDashboardPdfConfig.getTitle());
			String exportedFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.REPORT_TYPE;
			assessmentReportDTO.setPdfExportedFilePath(exportedFilePath);
			String pdfType = grafanaDashboardPdfConfig.getPdfType();
			if (pdfType.equals(DASHBOARD)) {
				grafanaDashboardAsPdf(assessmentReportDTO, grafanaDashboardPdfConfig, exportedFilePath);
			} else {
				printableDashboardAsPdf(assessmentReportDTO, grafanaDashboardPdfConfig, exportedFilePath);
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Worlflow Detail ==== PDF generation completed for Type : ===== {} ", pdfType);
			log.debug(
					"Type=TaskExecution executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowType(),
					"-", "-", processingTime,
					PDF_TYPE + grafanaDashboardPdfConfig.getPdfType() + SCHEDULE
							+ grafanaDashboardPdfConfig.getScheduleType() + SOURCE
							+ grafanaDashboardPdfConfig.getSource() + STATUS + grafanaDashboardPdfConfig.getStatus()
							+ " PDF generation completed");
		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data ", e);
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
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
	private void grafanaDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO, GrafanaDashboardPdfConfig grafanaDashboardPdfConfig, String exportedFilePath) {

		JsonParser jsonParser = new JsonParser();
		JsonElement config = jsonParser.parse(grafanaDashboardPdfConfig.getDashboardJson());
		String grafanaEndpoint = getGrafanaEndPoint();
		int loadTime = config.getAsJsonObject().get("loadTime").getAsInt() * 1000;
		log.debug("Worlflow Detail ==== LoadTIme configured for Grafana in milliseconds ===== {} ",loadTime);
		String grafanaUrl = config.getAsJsonObject().get("dashUrl").getAsString().replace("<GRAFANA_URL>", grafanaEndpoint);
		Playwright playwright = null;
		try {
			playwright = Playwright.create();
			long startTime = System.nanoTime();
			BrowserType browserType = 	playwright.chromium();
			LaunchOptions launchOptions = new LaunchOptions();
			launchOptions.setHeadless(Boolean.TRUE);
			launchOptions.setDevtools(Boolean.FALSE);
			
			Browser browser = browserType.launch(launchOptions);
			BrowserContext context = browser.newContext();
			Page page = context.newPage();

			Map<String, String> headers = getGrafanaHeaders(config);

			page.setExtraHTTPHeaders(headers);
			page.onRequest(request -> log.debug(">> {} {} ", request.method(), request.url()));
		    page.onResponse(response -> log.debug("<< {} {} ",response.status(), response.url()));
		    
			NavigateOptions navigateOptions = new NavigateOptions();
			navigateOptions.setWaitUntil(WaitUntilState.NETWORKIDLE);

			page.navigate(grafanaUrl,navigateOptions);
			page.waitForLoadState();
			log.debug("Dashboard load time configured in milliseconds == {} ",loadTime);
			log.debug("Waiting for dashboard to load == {} ",Instant.now());
			page.waitForTimeout(loadTime);
			log.debug("Waiting exit post configured time to load Dashboard == {} ", Instant.now());

			page.evaluate("() => {for (el of document.getElementsByClassName('sidemenu')) {return el.hidden = true; };  }");
			page.evaluate("() => {for (el of document.getElementsByClassName('navbar')) {return el.hidden = true; };  }");
			page.evaluate("() => {for (el of document.getElementsByClassName('react-resizable-handle')) {return el.hidden = true; };  }");
			page.evaluate("() => {for (el of document.getElementsByClassName('panel-info-corner')) {return el.hidden = true; };  }");
			page.evaluate("() => {for (el of document.getElementsByClassName('submenu-controls')) {return el.hidden = true; };  }");

			Object width = page.evaluate("() => { return document.getElementsByClassName('react-grid-layout')[0].getBoundingClientRect().width; }");
			log.debug("Worlflow Detail ==== Grafana grid Width ===== {} ",width);
			int dashboardwidth = width instanceof Integer ? (int) width + 40 : Integer.parseInt(width.toString());
			log.debug("Worlflow Detail ==== Grafana Dashboard width ===== {} ",dashboardwidth);
			Object height = page.evaluate("() => { return document.getElementsByClassName('react-grid-layout')[0].getBoundingClientRect().bottom; }");
			log.debug("Worlflow Detail ==== Grafana grid height ===== {} ",height);
			int dashboardlength = height instanceof Integer ? (int) height + 160 : Integer.parseInt(height.toString());
			log.debug("Worlflow Detail ==== Grafana Dashboard dashboard ===== {} ",dashboardlength);
			page.evaluate("async () => { await new Promise((resolve, reject) => {"
					+ "let totalHeight=0;"
					+ "let distance =100;"
					+ "let height_px= document.getElementsByClassName('react-grid-layout')[0].getBoundingClientRect().bottom; "
					+ "let timer =setInterval(()=>{"
							+ "var scrollHeight = height_px+160;"
							+ "var element = document.querySelector('.view');"
							+ "element.scrollBy({"
									+ " top: distance,"
									+ "left: 0,"
									+ "behavior: 'smooth'"
							+ "});"
							+ "totalHeight += distance;"
							+ "console.log('totalHeight', totalHeight); "
							+ "if (totalHeight >= scrollHeight) {"
								+ "clearInterval(timer);resolve();}},300) "
							+ "});"
					+ "}");
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
			pdfOptions.setHeaderTemplate(fetchTemplate(HEADER_HTML));
			pdfOptions.setFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin(new Margin().setTop("90").setRight("0").setBottom("50").setLeft("0"));
			page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
			byte[] pdf = page.pdf(pdfOptions);
			page.close();
			context.close();
			browser.close();
			
			File extractedPdfFile = new File(exportedFilePath);
			savePDFFile(extractedPdfFile, pdf);

			saveToVisualizationContaner(assessmentReportDTO, pdf);
			updateReportStatus(grafanaDashboardPdfConfig);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowType(),"-","-",processingTime,
					PDF_TYPE + grafanaDashboardPdfConfig.getPdfType() +
					SCHEDULE + grafanaDashboardPdfConfig.getScheduleType() +
					SOURCE + grafanaDashboardPdfConfig.getSource() +
					STATUS + grafanaDashboardPdfConfig.getStatus());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as PDF Completed with error {} ", e.getMessage());
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					"-","-","-",0," Grafana Dashboard export as PDF Completed with error " + e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
				log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
						assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
						"-","-","-",0," Unable to close Playwright " + e.getMessage());
			}
		}
	}

	/** This method use to prepare Grafana header object 
	 * @return map of headers 
	 */
	private Map<String, String> getGrafanaHeaders(JsonElement config) {
		GrafanaOrgToken grafanaOrgToken = grafanaDashboardConfigDAL.getTokenByOrgId(config.getAsJsonObject().get("organisation").getAsInt());
		String token = "Bearer "+AES256Cryptor.decrypt(grafanaOrgToken.getApiKey(),ApplicationConfigProvider.getInstance().getSingleSignOnConfig().getTokenSigningKey());
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
	private synchronized void printableDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO, GrafanaDashboardPdfConfig grafanaDashboardPdfConfig, String exportedFilePath) {
		Playwright playwright = null;
		try {
			playwright = Playwright.create();
			long startTime = System.nanoTime();
			BrowserType browserType = 	playwright.chromium();
			LaunchOptions launchOptions = new LaunchOptions();
			launchOptions.setHeadless(Boolean.TRUE);
			launchOptions.setDevtools(Boolean.FALSE);
			
			Browser browser = browserType.launch(launchOptions);
			BrowserContext context = browser.newContext();
			Page page = context.newPage();
			
			JsonParser jsonParser = new JsonParser();
			JsonElement config = jsonParser.parse(grafanaDashboardPdfConfig.getDashboardJson());
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
			
			List<byte[]> imageList = new ArrayList<>();
			for(int idx=0;idx<grafanaPanelList.size();idx++) {

				page.setExtraHTTPHeaders(headers);
				page.waitForTimeout(TRANSITION_TIME);
				page.setViewportSize(1200, 800);
				page.route("**", route -> route.resume());
				page.onRequest(request -> log.debug(">> {} {}  ", request.method(), request.url()));
			    page.onResponse(response -> log.debug("<< {} {} ",response.status(), response.url()));
				page.navigate(grafanaPanelList.get(idx),navigateOptions);
				Page.WaitForSelectorOptions waitForSelectorOptions = new Page.WaitForSelectorOptions();
				waitForSelectorOptions.setState(WaitForSelectorState.ATTACHED);
				log.debug("Waiting for panel {} to load completely before screenshot == {} ",idx,Instant.now());
				page.waitForTimeout(loadTime);
				log.debug("Waiting time completed for dashboard == {} ",Instant.now());
				page.evaluate("() => {for (el of document.getElementsByClassName('navbar')) {return el.hidden = true; };  }");
				page.evaluate("() => {for (el of document.getElementsByClassName('submenu-controls')) {return el.hidden = true; };  }");
				ElementHandle elementHandle = page.querySelector(".panel-wrapper.panel-wrapper--view");
				page.waitForTimeout(TRANSITION_TIME);
				byte[] image = elementHandle.screenshot();
				imageList.add(image);
			}
			String dynamicTemplate = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.HTML_EXTENSION;
			log.debug("Worlflow Detail ==== Generated DynamicTemplate.html name  ===== {} ",dynamicTemplate);
			File extractedPdfFile = new File(exportedFilePath);
			prepareHtml(imageList,dynamicTemplate, grafanaDashboardPdfConfig);
			page = context.newPage();
			page.navigate(new File(dynamicTemplate).getAbsolutePath());
			PdfOptions pdfOptions =  new PdfOptions();
			pdfOptions.setPrintBackground(Boolean.TRUE);
			pdfOptions.setDisplayHeaderFooter(true);
			pdfOptions.setHeaderTemplate(fetchTemplate(HEADER_HTML));
			pdfOptions.setFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin(new Margin().setTop("90").setRight("10").setBottom("50").setLeft("10"));
			byte[] pdf = page.pdf(pdfOptions);
			page.close();
			context.close();
			browser.close();
			
			savePDFFile(extractedPdfFile, pdf);
			saveToVisualizationContaner(assessmentReportDTO, pdf);
			updateReportStatus(grafanaDashboardPdfConfig);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowType(),"-","-",processingTime,
					PDF_TYPE + grafanaDashboardPdfConfig.getPdfType() +
					SCHEDULE +grafanaDashboardPdfConfig.getScheduleType() +
					SOURCE +grafanaDashboardPdfConfig.getSource() +
					STATUS +grafanaDashboardPdfConfig.getStatus());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as Printable PDF Completed with error {} ", e.getMessage());
			log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
					"-","-","-",0," Grafana Dashboard export as Printable PDF Completed with error " + e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
				log.error("Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}",
						assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),
						"-","-","-",0," Unable to close Playwright " + e.getMessage());
			}
		}

	}
	
	/**
	 * Update the status back to Grafana config
	 * @param grafanaDashboardConfig
	 */
	private void updateReportStatus(GrafanaDashboardPdfConfig grafanaDashboardConfig) {
		grafanaDashboardConfig.setStatus(WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
		grafanaDashboardConfig.setWorkflowConfig(grafanaDashboardConfig.getWorkflowConfig());
		grafanaDashboardConfig.setUpdatedDate(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
		grafanaDashboardConfigDAL.updateGrafanaDashboardConfig(grafanaDashboardConfig);
	}

	/**
	 * prepare dynamic HTML for PDF generation
	 * 
	 * @param imageList
	 * @param dynamicTemplate
	 * @param grafanaDashboardConfig
	 */
	private static void prepareHtml(List<byte[]> imageList, String dynamicTemplate, GrafanaDashboardPdfConfig grafanaDashboardConfig) {

		StringBuilder htmlStartTag  = new StringBuilder();
		htmlStartTag.append("<!DOCTYPE html><html><head>")
					.append("<link rel=\"stylesheet\" href=\"template.css\" />")
					.append("<title>")
					.append(grafanaDashboardConfig.getTitle())
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


		PrintWriter printWriter = null;
		try(FileWriter fileWriter = new FileWriter(dynamicTemplate)) {
			printWriter = new PrintWriter(fileWriter);
			printWriter.print(template);
		} catch (IOException e) {
			log.error("Worlflow Detail ==== Error creating dynamic template {} ", e.getMessage());
		} finally {
			if(printWriter != null) {
				printWriter.close();
			}
		}
	}
	
	private void saveToVisualizationContaner(InsightsAssessmentConfigurationDTO assessmentReportDTO, byte[] pdf) {
		try {
			InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
			emailHistoryConfig.setExecutionId(assessmentReportDTO.getExecutionId());
			emailHistoryConfig.setAttachmentData(pdf);
			JsonObject incomingTaskMessageJson = new JsonParser().parse(assessmentReportDTO.getIncomingTaskMessageJson()).getAsJsonObject();
			if (incomingTaskMessageJson.get("nextTaskId").getAsInt() == -1) {
				emailHistoryConfig.setStatus(WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
				emailHistoryConfig.setExecutionTime(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
			} else {
				emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.NOT_STARTED.name());
			}
			emailHistoryConfig.setMailAttachmentName(assessmentReportDTO.getAsseementreportname());
			emailHistoryConfig.setWorkflowConfig(assessmentReportDTO.getWorkflowId());
			workflowDAL.saveEmailExecutionHistory(emailHistoryConfig);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error setting PDF details in Email History table");
			throw new InsightsJobFailedException(
					"Worlflow Detail ==== Error setting PDF details in Email History table");
		}
	}
	
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


	private String getGrafanaEndPoint() {
		return ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint();
	}

	public void savePDFFile(File extractedPdfFile, byte[] pdfResponse) {
		try(FileOutputStream outStream = new FileOutputStream(extractedPdfFile)) {
			outStream.write(pdfResponse);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while saving pdf ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
	}

}
