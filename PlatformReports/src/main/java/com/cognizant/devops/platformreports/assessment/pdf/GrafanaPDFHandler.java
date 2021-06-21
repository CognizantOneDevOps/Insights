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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
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
import com.microsoft.playwright.Deferred;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame.LoadState;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.EventType;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Page.PdfOptions;
import com.microsoft.playwright.Page.WaitForSelectorOptions.State;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route.FulfillResponse;

public class GrafanaPDFHandler implements BasePDFProcessor {

	private static Logger log = LogManager.getLogger(GrafanaPDFHandler.class);
	
	private static final String HEADER_HTML = "header.html";
	private static final String FOOTER_HTML = "footer.html";

	private static final String TRACK_HORIZONTAL = ".track-horizontal";
	private static final String PANEL_CONTENT = ".panel-content";
	private static final String REACT_GRID_ITEM = ".react-grid-item";
	private static final String DASHBOARD_CONTENT = ".dashboard-content";
	private static final String REACT_GRID_LAYOUT = ".react-grid-layout";
	private static final String VIEW = ".view";
	private static final String PROXY_URL_PATTERN = "**/proxy/**";
	private static final String DASHBOARD = "Dashboard";
	
	private static final int DASHBOARD_LOAD_TIME = 30000;

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
			String folderName = assessmentReportDTO.getAsseementreportname() + "_"
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
			
			GrafanaDashboardPdfConfig grafanaDashboardPdfConfig = grafanaDashboardConfigDAL.fetchGrafanaDashboardDetailsByWorkflowId(assessmentReportDTO.getWorkflowId());
			log.debug("Worlflow Detail ==== GrafanaDashboardPdfConfig from UI ===== {} ",grafanaDashboardPdfConfig);
			assessmentReportDTO.setAsseementreportname(grafanaDashboardPdfConfig.getTitle());
			String exportedFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.REPORT_TYPE;
			assessmentReportDTO.setPdfExportedFilePath(exportedFilePath);
			if(grafanaDashboardPdfConfig!= null) {
				String pdfType = grafanaDashboardPdfConfig.getPdfType();
				if(pdfType.equals(DASHBOARD)) {
					grafanaDashboardAsPdf(assessmentReportDTO,grafanaDashboardPdfConfig, exportedFilePath);
				}else {
					printableDashboardAsPdf(assessmentReportDTO,grafanaDashboardPdfConfig, exportedFilePath);
				}

				log.debug("Worlflow Detail ==== PDF generation completed for Type : ===== {} ",pdfType);
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data ", e);
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
		log.debug("Worlflow Detail ==== grafanaEndpoint from config ===== {} ",grafanaEndpoint);
		String grafanaUrl = config.getAsJsonObject().get("dashUrl").getAsString().replace("<GRAFANA_URL>", grafanaEndpoint);
		Playwright playwright = Playwright.create();
		try {
			BrowserType browserType = 	playwright.chromium();
			LaunchOptions launchOptions = new LaunchOptions();
			launchOptions.withHeadless(true);
			launchOptions.withDevtools(false);

			String auth = getGrafanaAuth();
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
			String authHeader = "Basic " + new String(encodedAuth);
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", authHeader);

			Browser browser = browserType.launch(launchOptions);
			BrowserContext context = browser.newContext();
			Page page = context.newPage();

			page.setExtraHTTPHeaders(headers);
			page.setDefaultNavigationTimeout(120000);//Basic time to load any dashboard in Grafana.

			NavigateOptions navigateOptions = new NavigateOptions();
			navigateOptions.withWaitUntil(LoadState.NETWORKIDLE);
			page.route("**", route -> route.continue_());

			page.addListener(EventType.REQUEST, e -> log.debug("Request == {} ",((Request)e.data()).url()));
			page.addListener(EventType.RESPONSE, e -> log.debug("Response == {} ",((Response)e.data()).url()));

			/**To make sure all neo4j queries are returned**/
			page.route(PROXY_URL_PATTERN, r -> r.fulfill(new FulfillResponse().withStatus(200)));
			page.waitForResponse(grafanaUrl);
			page.waitForNavigation();
			page.navigate(grafanaUrl,navigateOptions);
			log.debug("Starting Waiting for dashboard to load == in ms {} ",DASHBOARD_LOAD_TIME);
			Thread.sleep(DASHBOARD_LOAD_TIME);
			log.debug("Waiting exit post configured time to load Dashboard ");
			Page.WaitForSelectorOptions waitForSelectorOptions = new Page.WaitForSelectorOptions();
			waitForSelectorOptions.withState(State.ATTACHED);
			Deferred<ElementHandle> event = page.waitForSelector(VIEW, new Page.WaitForSelectorOptions()
					.withState(Page.WaitForSelectorOptions.State.ATTACHED));
			event.get();
			Deferred<ElementHandle> revent = page.waitForSelector(REACT_GRID_LAYOUT, new Page.WaitForSelectorOptions()
					.withState(Page.WaitForSelectorOptions.State.ATTACHED));
			revent.get();
			Deferred<ElementHandle> devent = page.waitForSelector(DASHBOARD_CONTENT, new Page.WaitForSelectorOptions()
					.withState(Page.WaitForSelectorOptions.State.ATTACHED));
			devent.get();
			Deferred<ElementHandle> ievent = page.waitForSelector(REACT_GRID_ITEM, new Page.WaitForSelectorOptions()
					.withState(Page.WaitForSelectorOptions.State.ATTACHED));
			ievent.get();
			Deferred<ElementHandle> cevent = page.waitForSelector(PANEL_CONTENT, new Page.WaitForSelectorOptions()
					.withState(Page.WaitForSelectorOptions.State.ATTACHED));
			cevent.get();
			Deferred<ElementHandle> sevent = page.waitForSelector(TRACK_HORIZONTAL, new Page.WaitForSelectorOptions()
					.withState(Page.WaitForSelectorOptions.State.ATTACHED));
			sevent.get();

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
			page.waitForResponse(PROXY_URL_PATTERN);
			PdfOptions pdfOptions =  new PdfOptions();
			//pdfOptions.withPath(Paths.get(URLEncoder.encode(grafanaDashboardConfig.getTitle(),"UTF-8")+".pdf"));
			pdfOptions.withPrintBackground(true);
			pdfOptions.withWidth(dashboardwidth+"px");
			pdfOptions.withHeight(dashboardlength+"px");
			pdfOptions.withScale(1);
			pdfOptions.withDisplayHeaderFooter(true);
			pdfOptions.withHeaderTemplate(fetchTemplate(HEADER_HTML));
			pdfOptions.withFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin().withTop("90").withRight("0").withBottom("50").withLeft("0");
			byte[] pdf = page.pdf(pdfOptions);
			page.close();
			context.close();
			browser.close();
			
			File extractedPdfFile = new File(exportedFilePath);
			savePDFFile(extractedPdfFile, pdf);

			saveToVisualizationContaner(assessmentReportDTO, pdf);
			updateReportStatus(grafanaDashboardPdfConfig);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as PDF Completed with error {} ", e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
			}
		}

	}

	/**
	 * Generates printable pdf from Grafana dashboard panels.
	 * @param assessmentReportDTO 
	 * @param grafanaDashboardConfig
	 * @param incomingTaskMessageJson
	 * @param exportedFilePath 
	 */
	private synchronized void printableDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO, GrafanaDashboardPdfConfig grafanaDashboardPdfConfig, String exportedFilePath) {
		Playwright playwright = Playwright.create();
		try {
			JsonParser jsonParser = new JsonParser();
			JsonElement config = jsonParser.parse(grafanaDashboardPdfConfig.getDashboardJson());
			String grafanaEndpoint = getGrafanaEndPoint();
			log.debug("Worlflow Detail ==== grafanaEndpoint from config ===== {} ",grafanaEndpoint);
			JsonArray panelArray = config.getAsJsonObject().get("panelUrls").getAsJsonArray();
			List<String> grafanaPanelList = new ArrayList<>(0);
			panelArray.forEach(e -> {
				String panelUrl = e.getAsString().replace("<GRAFANA_URL>", grafanaEndpoint);
				log.debug("Worlflow Detail ==== Panel url ===== {} ",panelUrl);
				grafanaPanelList.add(panelUrl);
			});

			BrowserType browserType = 	playwright.chromium();
			LaunchOptions launchOptions = new LaunchOptions();
			launchOptions.withHeadless(true);
			launchOptions.withDevtools(false);

			String auth = getGrafanaAuth();
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.ISO_8859_1));
			String authHeader = "Basic " + new String(encodedAuth);
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", authHeader);

			Browser browser = browserType.launch(launchOptions);
			BrowserContext context = browser.newContext(
					new Browser.NewContextOptions().withViewport(1200, 800));

			NavigateOptions navigateOptions = new NavigateOptions();
			navigateOptions.withWaitUntil(LoadState.NETWORKIDLE);
			Page page = context.newPage();
			List<byte[]> imageList = new ArrayList<>();
			for(int idx=0;idx<grafanaPanelList.size();idx++) {

				page.setExtraHTTPHeaders(headers);
				page.setDefaultNavigationTimeout(120000);
				page.setViewportSize(1200, 800);
				page.route("**", route -> route.continue_());
				page.navigate(grafanaPanelList.get(idx),navigateOptions);
				Page.WaitForSelectorOptions waitForSelectorOptions = new Page.WaitForSelectorOptions();
				waitForSelectorOptions.withState(State.ATTACHED);
				Deferred<ElementHandle> event = page.waitForSelector("body > img", new Page.WaitForSelectorOptions()
						.withState(Page.WaitForSelectorOptions.State.ATTACHED));
				event.get();
				page.route(PROXY_URL_PATTERN, r -> {
					r.fulfill(new FulfillResponse().withStatus(200));
				});
				page.waitForResponse(grafanaPanelList.get(idx));
				page.waitForResponse(PROXY_URL_PATTERN);

				byte[] image = page.waitForSelector("body > img").get().asElement().screenshot();
				//log.debug("Worlflow Detail ==== Image  ===== {} ",image);
				imageList.add(image);
				//page.screenshot(new Page.ScreenshotOptions().withPath(Paths.get("screenshot-"+idx + ".png")));

			}
			String dynamicTemplate = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.HTML_EXTENSION;
			log.debug("Worlflow Detail ==== Generated DynamicTemplate.html name  ===== {} ",dynamicTemplate);
			File extractedPdfFile = new File(exportedFilePath);
			prepareHtml(imageList,dynamicTemplate, grafanaDashboardPdfConfig);

			page.navigate(new File(dynamicTemplate).getAbsolutePath());
			PdfOptions pdfOptions =  new PdfOptions();
			//pdfOptions.withPath(Paths.get(URLEncoder.encode(grafanaDashboardConfig.getTitle(),"UTF-8")+".pdf"));
			pdfOptions.withPrintBackground(false);
			pdfOptions.withDisplayHeaderFooter(true);
			pdfOptions.withHeaderTemplate(fetchTemplate(HEADER_HTML));
			pdfOptions.withFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin().withTop("90").withRight("10").withBottom("50").withLeft("10");
			byte[] pdf = page.pdf(pdfOptions);
			page.close();
			context.close();
			browser.close();
			
			savePDFFile(extractedPdfFile, pdf);
			saveToVisualizationContaner(assessmentReportDTO, pdf);
			updateReportStatus(grafanaDashboardPdfConfig);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as Printable PDF Completed with error {} ", e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
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

	private String getGrafanaAuth() {
		return ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName()+":"+
				ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
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
