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

import java.awt.Color;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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
import com.cognizant.devops.platformreports.assessment.dal.ReportGraphDataHandler;
import com.cognizant.devops.platformreports.assessment.datamodel.GrafanaReportConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsReportPdfTableConfig;
import com.cognizant.devops.platformreports.assessment.util.PdfReportTableUtil;
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

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;

public class GrafanaPDFHandler implements BasePDFProcessor {

	private static Logger log = LogManager.getLogger(GrafanaPDFHandler.class);

	private static final String HEADER_HTML = "header.html";
	private static final String FOOTER_HTML = "footer.html";
	private static final String DASHBOARD = "Dashboard";

	private static final String PDF_TYPE = " PDFType: ";
	private static final String SCHEDULE = " Schedule: ";
	private static final String SOURCE = " Source: ";

	private PDFont font;
	private float yStart = 1f;

	private static final String ALL_VAR = "in [\"All\"]";
	private static final String ALL_VAR2 = "IN [\"$__all\"]";
	private static final String ALL_VAR3 = "in [\"__all\"]";
	private static final String REGEX = "=~ \".*\"";

	private static final String FILE_TRANSFER_PROTOCOL = "file:";

	private static final int TRANSITION_TIME = 1000;

	private static final String DASHBOARD_REPORT_DIR = "dashboardReportTemplate";
	private static final String DASHBOARD_ELEMENT_JSON_FILE = "dashboardReportElement.json";
	private static final String FRONT_PAGE_TEMPLATE = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ DASHBOARD_REPORT_DIR + File.separator + "frontPageTemplate.html";
	private static final String LOGO_IMAGE_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ DASHBOARD_REPORT_DIR + File.separator + "image.webp";
	private static final String MODIFIED_FRONT_PAGE_TEMPLATE = "frontPage.html";
	private static final String DASHBOARD_ELEMENT_JSON_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME)
			+ File.separator + DASHBOARD_REPORT_DIR + File.separator + DASHBOARD_ELEMENT_JSON_FILE;
	private static final String LOG_MESSAGE = "Type=TaskExecution  executionId={} workflowId={} ConfigId={} WorkflowType={} KpiId={} Category={} ProcessingTime={} message={}";

	private static final String ORGANISATION = "organisation";
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	private GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
	GrafanaReportConfigurationDTO pdfconfigDto = new GrafanaReportConfigurationDTO();
	PDFExecutionUtils pdfExecutionUtils = new PDFExecutionUtils();

	ReportGraphDataHandler reportGraphDataHandler = new ReportGraphDataHandler();

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
			String folderName = WorkflowType.GRAFANADASHBOARDPDFREPORT + "_" + assessmentReportDTO.getExecutionId();
			assessmentReportDTO.setPdfReportFolderName(folderName);
			String reportExecutionFile = AssessmentReportAndWorkflowConstants.REPORT_PDF_EXECUTION_RESOLVED_PATH
					+ folderName;
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

			if (grafanaDashboardPdfConfig != null) {

				JsonObject dashboardConfigJson = JsonUtils
						.parseStringAsJsonObject(grafanaDashboardPdfConfig.getDashboardJson());
				pdfconfigDto.setDashboardJson(grafanaDashboardPdfConfig.getDashboardJson());
				pdfconfigDto.setOrganisation(dashboardConfigJson.get(ORGANISATION).getAsString());
				pdfconfigDto.setPdfType(grafanaDashboardPdfConfig.getPdfType());
				pdfconfigDto.setScheduleType(grafanaDashboardPdfConfig.getScheduleType());
				pdfconfigDto.setSource(grafanaDashboardPdfConfig.getSource());
				pdfconfigDto.setTitle(grafanaDashboardPdfConfig.getTitle());
				pdfconfigDto.setVariables(grafanaDashboardPdfConfig.getVariables());
				pdfconfigDto.setWorkflowType(grafanaDashboardPdfConfig.getWorkflowConfig().getWorkflowType());
			} else {
				InsightsWorkflowConfiguration workflowConfig = workflowDAL
						.getWorkflowConfigByWorkflowId(assessmentReportDTO.getWorkflowId());
				InsightsAssessmentConfiguration assessmentConfig = workflowConfig.getAssessmentConfig();

				if (workflowConfig != null && assessmentConfig != null) {
					JsonObject dashboardConfigJson = JsonUtils
							.parseStringAsJsonObject(assessmentConfig.getAdditionalDetail());
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
			log.error(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), "-", "-", "-", 0,
					" exeception in preparePDFDTO  " + e.getMessage());
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
					+ (assessmentReportDTO.getWorkflowId() + "_" + assessmentReportDTO.getExecutionId()) + "."
					+ ReportEngineUtils.REPORT_TYPE;
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
			log.debug(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), pdfconfigDto.getWorkflowType(), "-", "-", processingTime,
					PDF_TYPE + pdfconfigDto.getPdfType() + SCHEDULE + pdfconfigDto.getScheduleType() + SOURCE
							+ pdfconfigDto.getSource() + " PDF generation completed");

			if (pdfconfigDto.getWorkflowType().equalsIgnoreCase("GRAFANADASHBOARDPDFREPORT")) {
				deletePDFDirectory(new File(assessmentReportDTO.getPdfReportDirPath()));
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data ", e);
			log.error(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), "-", "-", "-", 0,
					" unable to prepare pdf data " + e.getMessage());
			throw new InsightsJobFailedException(" unable to prepare pdf data " + e);
		}
	}

	/**
	 * Uses playwright to create current url of grafana dashboard as pdf
	 * 
	 * @param assessmentReportDTO
	 * @param grafanaDashboardConfig
	 * @param incomingTaskMessageJson
	 * @param exportedFilePath
	 */
	private void grafanaDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO,
			String exportedFilePath) {

		JsonElement config = JsonUtils.parseStringAsJsonElement(pdfconfigDto.getDashboardJson());
		String grafanaEndpoint = getGrafanaEndPoint();
		int loadTime = config.getAsJsonObject().get("loadTime").getAsInt() * 1000;

		log.debug("Worlflow Detail ==== LoadTIme configured for Grafana in milliseconds ===== {} ", loadTime);
		String grafanaUrl = config.getAsJsonObject().get("dashUrl").getAsString().replace("GRAFANA_URL",
				grafanaEndpoint);
		log.debug("Worlflow Detail ==== grafanadashboard Url ===== {} ", grafanaUrl);

		Playwright playwright = null;
		String pageContent = "";

		playwright = Playwright.create();
		long startTime = System.nanoTime();
		BrowserType browserType = playwright.chromium();
		LaunchOptions launchOptions = new LaunchOptions();
		launchOptions.setHeadless(Boolean.TRUE);
		launchOptions.setDevtools(Boolean.FALSE);
		PdfOptions pdfOptions = new PdfOptions();

		Browser browser = browserType.launch(launchOptions);

		try (BrowserContext context = browser.newContext(); Page page = context.newPage();) {

			//TablePanelList and TablePanelTitleList
			List<String> grafanaTablePanelList = processTablePanelPDFData(config).get(1);
			List<String> grafanaTablePanelTitleList = processTablePanelPDFData(config).get(0);
			
			InsightsReportPdfTableConfig insightsReportPdfTableConfig = new InsightsReportPdfTableConfig();
			
			
			// original dashboard view code starts from here
			Map<String, String> headers = getGrafanaHeaders(config);
			String theme = config.getAsJsonObject().get("theme").getAsString();
			

			page.setExtraHTTPHeaders(headers);
			page.onRequest(request -> log.debug("Request >> {} {} ", request.method(), request.url()));
			page.onResponse(response -> log.debug("Response << {} {} ", response.status(), response.url()));

			NavigateOptions navigateOptions = new NavigateOptions();
			navigateOptions.setWaitUntil(WaitUntilState.NETWORKIDLE);

			page.navigate(grafanaUrl, navigateOptions);
			pageContent = page.content();
			page.waitForLoadState();
			log.debug("Worlflow Detail ==== Waiting for dashboard to load == loadTime {} time {} ", loadTime,
					Instant.now());
			page.waitForTimeout(loadTime);
			log.debug("Worlflow Detail ====Waiting exit post configured time to load Dashboard == {} ", Instant.now());

			String dashboardElement = readDashboardElementJson();
			log.debug("Worlflow Detail ==== reading elements from dashboardReportElement.json");
			JsonObject dashboardElementJson = JsonUtils.parseStringAsJsonObject(dashboardElement).get("dashboard")
					.getAsJsonObject();
			JsonArray elementList = dashboardElementJson.get("elements").getAsJsonArray();
			for (JsonElement ele : elementList) {
				page.evaluate(ele.getAsString());
			}

			Object width = page.evaluate(dashboardElementJson.get("width").getAsString());
			log.debug("Worlflow Detail ==== Grafana grid Width ===== {} ", width);
			int dashboardwidth = width instanceof Integer ? (int) width + 40 : Integer.parseInt(width.toString());
			log.debug("Worlflow Detail ==== Grafana Dashboard width ===== {} ", dashboardwidth);
			Object height = page.evaluate(dashboardElementJson.get("height").getAsString());
			log.debug("Worlflow Detail ==== Grafana grid height ===== {} ", height);
			int dashboardlength = height instanceof Integer ? (int) height + 160 : Integer.parseInt(height.toString());
			log.debug("Worlflow Detail ==== Grafana Dashboard length ===== {} ", dashboardlength);
			page.evaluate(dashboardElementJson.get("screenshotElement").getAsString());
			log.debug("Waiting for dashboard to load completely before screenshot == {} ", Instant.now());
			page.waitForTimeout(loadTime);
			log.debug("Waiting time completed for dashboard == {} ", Instant.now());
			page.waitForLoadState(LoadState.NETWORKIDLE);

			pdfOptions.setPrintBackground(Boolean.TRUE);
			pdfOptions.setWidth(dashboardwidth + "px");
			pdfOptions.setHeight(dashboardlength + "px");
			pdfOptions.setScale(1);
			pdfOptions.setDisplayHeaderFooter(true);
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(false));
			pdfOptions.setFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin(new Margin().setTop("90").setRight("0").setBottom("50").setLeft("0"));
			page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
			byte[] dashboardPdf = page.pdf(pdfOptions);
			log.debug("Worlflow Detail ==== dashboard pdf generated ===");

			byte[] pdfbytes = null;
			if (!grafanaTablePanelList.isEmpty()) {
				Map<String,List<String>> variableMap = processGrafanaVariables(config);
				PDDocument tablePanelList = addTablePanels(grafanaTablePanelList, grafanaTablePanelTitleList,
						variableMap, assessmentReportDTO, theme, pdfconfigDto.getPdfType());
				// add footer
				tablePanelList = new PdfReportTableUtil().footer(assessmentReportDTO, tablePanelList, 0,
						insightsReportPdfTableConfig, pdfconfigDto.getPdfType());

				// add header
				tablePanelList = setImageHeader(tablePanelList, pdfconfigDto.getPdfType());

				pdfbytes = toByteArray(tablePanelList);
			}

			String frontPagePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ MODIFIED_FRONT_PAGE_TEMPLATE;

			modifyFrontPageTemplate(frontPagePath, theme);
			page.navigate(FILE_TRANSFER_PROTOCOL + new File(frontPagePath).getAbsolutePath());
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(true));
			pdfOptions.setFooterTemplate(fetchTemplate("frontPagefooter.html"));
			pdfOptions.setHeight("1600px");
			byte[] frontPagePdf = page.pdf(pdfOptions);

			browser.close();

			byte[] finalPdf = mergePDFFiles(frontPagePdf, dashboardPdf, exportedFilePath);
			

			if (Boolean.FALSE.equals(pdfconfigDto.getIsAssessmentReport())) {
				if (!grafanaTablePanelList.isEmpty()) {
					byte[] finalPdfWithTables = mergePDFFiles(finalPdf, pdfbytes, exportedFilePath);

					pdfExecutionUtils.saveToVisualizationContainer(assessmentReportDTO, finalPdfWithTables);
				} else {
					pdfExecutionUtils.saveToVisualizationContainer(assessmentReportDTO, finalPdf);
				}
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), pdfconfigDto.getWorkflowType(), "-", "-", processingTime,
					PDF_TYPE + pdfconfigDto.getPdfType() + SCHEDULE + pdfconfigDto.getScheduleType() + SOURCE
							+ pdfconfigDto.getSource());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as PDF Completed with error {} ", e.getMessage());
			log.error(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), "-", "-", "-", 0,
					" Grafana Dashboard export as PDF Completed with error " + e.getMessage());
			log.error("Dashboard page content ========== {} ", pageContent);
			log.error(e);
			throw new InsightsJobFailedException(e.getMessage());
		} finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
				log.error(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
						assessmentReportDTO.getConfigId(), "-", "-", "-", 0,
						" Unable to close Playwright " + e.getMessage());
			}
		}
	}

	/**
	 * This method use to prepare Grafana header object
	 * 
	 * @return map of headers
	 */
	private Map<String, String> getGrafanaHeaders(JsonElement config) {
		GrafanaOrgToken grafanaOrgToken = grafanaDashboardConfigDAL
				.getTokenByOrgId(config.getAsJsonObject().get(ORGANISATION).getAsInt());
		String token = "Bearer " + AES256Cryptor.decrypt(grafanaOrgToken.getApiKey(),
				AssessmentReportAndWorkflowConstants.GRAFANA_PDF_TOKEN_SIGNING_KEY);
		Map<String, String> headers = new HashMap<>();
		headers.put("Authorization", token);
		return headers;
	}

	/**
	 * Generates printable pdf from Grafana dashboard panels.
	 * 
	 * @param assessmentReportDTO
	 * @param grafanaDashboardConfig
	 * @param incomingTaskMessageJson
	 * @param exportedFilePath
	 */
	private synchronized void printableDashboardAsPdf(InsightsAssessmentConfigurationDTO assessmentReportDTO,
			String exportedFilePath) {
		Playwright playwright = null;

		playwright = Playwright.create();
		long startTime = System.nanoTime();
		BrowserType browserType = playwright.chromium();
		LaunchOptions launchOptions = new LaunchOptions();
		launchOptions.setHeadless(Boolean.TRUE);
		launchOptions.setDevtools(Boolean.FALSE);
		Browser browser = browserType.launch(launchOptions);
		PdfOptions pdfOptions = new PdfOptions();
		NavigateOptions navigateOptions = new NavigateOptions();
		navigateOptions.setWaitUntil(WaitUntilState.NETWORKIDLE);

		try (BrowserContext context = browser.newContext(); Page page = context.newPage();) {
			JsonElement config = JsonUtils.parseStringAsJsonElement(pdfconfigDto.getDashboardJson());
			int loadTime = config.getAsJsonObject().get("loadTime").getAsInt() * 1000;
			log.debug("Worlflow Detail ==== LoadTIme configured for Grafana in milliseconds ===== {} ", loadTime);
			String grafanaEndpoint = getGrafanaEndPoint();
			log.debug("Worlflow Detail ==== grafanaEndpoint from config ===== {} ", grafanaEndpoint);
			String theme = config.getAsJsonObject().get("theme").getAsString();
			
			//TablePanelList and TablePanelTitleList
			List<String> grafanaTablePanelList = processTablePanelPDFData(config).get(1);
			List<String> grafanaTablePanelTitleList = processTablePanelPDFData(config).get(0);
			
			InsightsReportPdfTableConfig insightsReportPdfTableConfig = new InsightsReportPdfTableConfig();
			
			Map<String,List<String>> variableMap = processGrafanaVariables(config);
			
			byte[] finalTablePanelPDF = null;
			if (!grafanaTablePanelList.isEmpty()) {

				PDDocument tablePanelList = addTablePanels(grafanaTablePanelList, grafanaTablePanelTitleList,
						variableMap, assessmentReportDTO, theme, pdfconfigDto.getPdfType());

				// add footer
				tablePanelList = new PdfReportTableUtil().footer(assessmentReportDTO, tablePanelList, 0,
						insightsReportPdfTableConfig, pdfconfigDto.getPdfType());

				// add header
				tablePanelList = setImageHeader(tablePanelList, pdfconfigDto.getPdfType());

				log.debug("Worlflow Detail ==== Added headers and footers in Table Panel PDF successfully!");
				tablePanelList.save(assessmentReportDTO.getPdfReportDirPath() + File.separator
						+ assessmentReportDTO.getAsseementreportname() + "_Open." + ReportEngineUtils.REPORT_TYPE);

				finalTablePanelPDF = toByteArray(tablePanelList);
			}

			List<String> grafanaPanelList = new ArrayList<>();
			JsonArray panelUrlArray = config.getAsJsonObject().get("panelUrlArray").getAsJsonArray();
			
			panelUrlArray.forEach(e -> {
				String panelUrl = e.getAsJsonObject().get("panelURL").getAsString().replace("GRAFANA_URL",
						grafanaEndpoint);
				log.debug("Worlflow Detail ==== Panel url ===== {} ", panelUrl);
				if (((e.getAsJsonObject().get("type").getAsString()).equals("table"))
						|| ((e.getAsJsonObject().get("type").getAsString()).equals("table-old"))) {
					return;
				}
				grafanaPanelList.add(panelUrl);

			});

			Map<String, String> headers = getGrafanaHeaders(config);

			String dashboardElement = readDashboardElementJson();
			log.debug("Worlflow Detail ==== dashboardReportElement.json fetched successfully");
			JsonObject dashboardElementJson = JsonUtils.parseStringAsJsonObject(dashboardElement).get("Printable")
					.getAsJsonObject();
			JsonArray elementList = dashboardElementJson.get("elements").getAsJsonArray();

			List<byte[]> imageList = new ArrayList<>();
			for (int idx = 0; idx < grafanaPanelList.size(); idx++) {

				page.setExtraHTTPHeaders(headers);
				page.waitForTimeout(TRANSITION_TIME);
				page.setViewportSize(1200, 800);
				page.route("**", route -> route.resume());
				page.onRequest(request -> log.debug("Request >> {} {}  ", request.method(), request.url()));
				page.onResponse(response -> log.debug("Response << {} {} ", response.status(), response.url()));

				page.navigate(grafanaPanelList.get(idx), navigateOptions);
				Page.WaitForSelectorOptions waitForSelectorOptions = new Page.WaitForSelectorOptions();
				waitForSelectorOptions.setState(WaitForSelectorState.ATTACHED);
				log.debug("Waiting for panel {} to load completely before screenshot == {} ", idx, Instant.now());
				page.waitForTimeout(loadTime);
				log.debug("Waiting time completed for dashboard == {} ", Instant.now());
				for (JsonElement ele : elementList) {
					page.evaluate(ele.getAsString());
				}
				ElementHandle elementHandle = page
						.querySelector(dashboardElementJson.get("screenshotElement").getAsString());
				page.waitForTimeout(TRANSITION_TIME);
				byte[] image = elementHandle.screenshot();
				imageList.add(image);
			}

			String dynamicTemplate = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.HTML_EXTENSION;
			log.debug("Worlflow Detail ==== Generated DynamicTemplate.html name  ===== {} ", dynamicTemplate);
			prepareHtml(imageList, dynamicTemplate);
			page.navigate(new File(dynamicTemplate).getAbsolutePath());
			pdfOptions.setPrintBackground(Boolean.TRUE);
			pdfOptions.setDisplayHeaderFooter(true);
			pdfOptions.setWidth("793.92px");
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(false));
			pdfOptions.setFooterTemplate(fetchTemplate(FOOTER_HTML));
			pdfOptions.setMargin(new Margin().setTop("85").setRight("10").setBottom("5").setLeft("10"));
			byte[] dashboardPdf = page.pdf(pdfOptions);

			String frontPagePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ MODIFIED_FRONT_PAGE_TEMPLATE;
			modifyFrontPageTemplate(frontPagePath, theme);

			page.navigate(FILE_TRANSFER_PROTOCOL + new File(frontPagePath).getAbsolutePath());
			pdfOptions.setHeaderTemplate(updateLogoImgInHeaderTemplate(true));
			pdfOptions.setWidth("793.92px");
			pdfOptions.setFooterTemplate(fetchTemplate("frontPagefooter.html"));
			byte[] frontPagePdf = page.pdf(pdfOptions);

			browser.close();

			byte[] finalPdf = mergePDFFiles(frontPagePdf, dashboardPdf, exportedFilePath);
			
			if (!grafanaTablePanelList.isEmpty()) {
				byte[] finalPdfWithTables = mergePDFFiles(finalPdf, finalTablePanelPDF, exportedFilePath);

				pdfExecutionUtils.saveToVisualizationContainer(assessmentReportDTO, finalPdfWithTables);
			} else {
				pdfExecutionUtils.saveToVisualizationContainer(assessmentReportDTO, finalPdf);
			}

			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), pdfconfigDto.getWorkflowType(), "-", "-", processingTime,
					PDF_TYPE + pdfconfigDto.getPdfType() + SCHEDULE + pdfconfigDto.getScheduleType() + SOURCE
							+ pdfconfigDto.getSource());
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Grafana Dashboard export as Printable PDF Completed with error {} ",
					e.getMessage());
			log.error(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
					assessmentReportDTO.getConfigId(), "-", "-", "-", 0,
					" Grafana Dashboard export as Printable PDF Completed with error " + e.getMessage());
			log.error(e);
			throw new InsightsJobFailedException(e.getMessage());
		} finally {
			try {
				playwright.close();
			} catch (Exception e) {
				log.error("Worlflow Detail ==== Unable to close Playwright {} ", e.getMessage());
				log.error(LOG_MESSAGE, assessmentReportDTO.getExecutionId(), assessmentReportDTO.getWorkflowId(),
						assessmentReportDTO.getConfigId(), "-", "-", "-", 0,
						" Unable to close Playwright " + e.getMessage());
			}
		}
	}
	
	private Map<String, List<String>> processGrafanaVariables(JsonElement config) {
		String grafanaVariables = config.getAsJsonObject().get("variables").getAsString();
		List<String> variableList = Arrays.asList(grafanaVariables.split(","));
		Map<String, List<String>> variableMap = new HashMap<>();
		

		// creating Variable map
		variableList.stream().forEach(var -> {
			String[] keyValuePair = var.split("=");
			if (variableMap.containsKey(keyValuePair[0])) {
				List<String> list = variableMap.get(keyValuePair[0]);
				list.add(keyValuePair[1]);
				variableMap.put(keyValuePair[0], list);
			} else {
				List<String> list = new ArrayList<>();
				list.add(keyValuePair[1]);
				variableMap.put(keyValuePair[0], list);
			}

		});
		return variableMap;
	}
	
	
	private List<List<String>> processTablePanelPDFData(JsonElement config) {
		// processing grafana variables // For panel Url
		List<String> grafanaTablePanelList = new ArrayList<>();
		List<String> grafanaTablePanelTitleList = new ArrayList<>();
		if (Boolean.FALSE.equals(pdfconfigDto.getIsAssessmentReport())) {
			JsonArray panelUrlArray = config.getAsJsonObject().get("panelUrlArray").getAsJsonArray();
			// filtering table panels
			for (JsonElement panel : panelUrlArray) {
				if ((panel.getAsJsonObject().get("type").getAsString()).equals("table")
						|| (panel.getAsJsonObject().get("type").getAsString()).equals("table-old")) {
					String panelLinks = panel.getAsJsonObject().get("query").getAsString();
					String panelTitle = panel.getAsJsonObject().get("title").getAsString();
					grafanaTablePanelList.add(panelLinks);
					grafanaTablePanelTitleList.add(panelTitle);
				}
			}
		}
		List<List<String>> listOfPanelAndTitles = new ArrayList<>();
		listOfPanelAndTitles.add(grafanaTablePanelTitleList);
		listOfPanelAndTitles.add(grafanaTablePanelList);
		return listOfPanelAndTitles;

	}
	private PDDocument setImageHeader(PDDocument doc, String pdfType) {
		try {
			PDPageTree pages = doc.getPages();
			byte[] image = null;
			Icon logoEntity = new IconDAL().fetchEntityData("logo");
			PDImageXObject pdImage = null;

			for (PDPage page : pages) {
				if (logoEntity.getImage() != null) {
					image = logoEntity.getImage();
					pdImage = PDImageXObject.createFromByteArray(doc, image, "LOGO");

				} else {
					InputStream imageStream = new FileInputStream(new File(LOGO_IMAGE_PATH).getCanonicalPath());
					image = IOUtils.toByteArray(imageStream);
					pdImage = PDImageXObject.createFromByteArray(doc, image, "LOGO");
				}
				PDPageContentStream contentStream = new PDPageContentStream(doc, page,
						PDPageContentStream.AppendMode.APPEND, true);

				if (pdfType.equalsIgnoreCase(DASHBOARD)) {
					contentStream.drawImage(pdImage, 20, 840, 135, 45);
				} else {
					contentStream.drawImage(pdImage, 10, 780, 135, 45);
				}

				contentStream.beginText();
				contentStream.setNonStrokingColor(0f, 0f, 0f);

				if (pdfType.equalsIgnoreCase(DASHBOARD)) {
					contentStream.newLineAtOffset(340, 860);
				} else {
					contentStream.newLineAtOffset(200, 800);
				}
				contentStream.setFont(PDType1Font.HELVETICA, 20);

				contentStream.showText(pdfconfigDto.getTitle());
				contentStream.endText();

				contentStream.close();
			}

		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while updating logo image in header template  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
		return doc;

	}

	public PDDocument addTablePanels(List<String> tablePanelQuery, List<String> tablePanelTitles,
			Map<String, List<String>> variableMap, InsightsAssessmentConfigurationDTO assessmentReportDTO, String theme,
			String pdfType) {

		int tableIndex = 0;

		PDDocument doc = null;
		try {

			InsightsReportPdfTableConfig insightsReportPdfTableConfig = new InsightsReportPdfTableConfig();
			String templateHtmlPath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.HTML_EXTENSION;
			File render = new File(templateHtmlPath);
			String reportDirPath = assessmentReportDTO.getPdfReportDirPath() + File.separator;
			PdfReportTableUtil pdfReportTableUtil = new PdfReportTableUtil();
			doc = new PDDocument();

			PDPage page = pdfReportTableUtil.addNewPage(doc, pdfconfigDto.getPdfType());
			pdfReportTableUtil.fetchPdfConfig(reportDirPath, insightsReportPdfTableConfig);

			int titleCount = 0;
			for (String query : tablePanelQuery) {

				String updatedQuery = query;
				List<JsonObject> tableData = null;
				updatedQuery = (updatedQuery.replace("\n", " "));
				List<String> queryStringList = Arrays.asList(updatedQuery.split(" "));
				Stream<String> s = queryStringList.stream().filter(name -> name.startsWith("$"));
				List<String> paramList = s.collect(Collectors.toList());
				if (!paramList.isEmpty()) {
					for (String filterQueryParam : queryStringList) {
						String grafanaQueryParam = filterQueryParam;
						String queryParamForMap = (filterQueryParam.replace("$", ""));
						if (variableMap.containsKey(queryParamForMap)) {
							List<String> arrayOfFilterString = variableMap.get(queryParamForMap);
							String arrayOfString = "[" + arrayOfFilterString.stream().map(str -> "\"" + str + "\"")
									.collect(Collectors.joining(", ")) + "]";
							updatedQuery = updatedQuery.replace(grafanaQueryParam, arrayOfString);
						}
					}

					if (updatedQuery.contains(ALL_VAR) || updatedQuery.contains(ALL_VAR2)
							|| updatedQuery.contains(ALL_VAR3)) {
						updatedQuery = updatedQuery.replace(ALL_VAR, REGEX);
						updatedQuery = updatedQuery.replace(ALL_VAR2, REGEX);
						updatedQuery = updatedQuery.replace(ALL_VAR3, REGEX);
					}

					tableData = reportGraphDataHandler.fetchData(updatedQuery);
				}

				else {
					tableData = reportGraphDataHandler.fetchData(updatedQuery);
				}

				JsonObject obh1 = tableData.get(0);
				JsonObject columnData = obh1.getAsJsonArray("results").get(0).getAsJsonObject();
				JsonArray columnsArray = columnData.get("columns").getAsJsonArray();
				// For rows
				JsonArray rowsElement = columnData.get("data").getAsJsonArray();

				prepareGrafanaTable(assessmentReportDTO, columnsArray, rowsElement, tableIndex, doc, page,
						insightsReportPdfTableConfig, tablePanelTitles.get(titleCount), theme, pdfType);
				tableIndex++;

				titleCount++;
			}
			try {
				if (tableIndex > 0) {
					log.debug("Worlflow Detail ==== Table Panel PDF Generated Successfully!");
				}
			} catch (Exception e) {
				log.error("Worlflow Detail ==== unable to save open pdf with tables, {}", e.getMessage());
			}
		} catch (Exception e) {
			log.error(" Worlflow Detail ==== unable to create table, {}", e.getMessage());
		}

		return doc;
	}

	private static byte[] toByteArray(PDDocument pdDoc) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			pdDoc.save(out);
			pdDoc.close();
		} catch (Exception ex) {
			log.error(" Worlflow Detail ====  Error while converting table panel PDF into byte array, {} ",
					ex.getMessage());
		}
		return out.toByteArray();
	}

	private void prepareGrafanaTable(InsightsAssessmentConfigurationDTO assessmentReportDTO, JsonArray columnData,
			JsonArray rowData, int tableIndex, PDDocument doc, PDPage page,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, String title, String theme, String pdfType) {
		PdfReportTableUtil pdfReportTableUtil = new PdfReportTableUtil();
		if (yStart > 750) {
			PDPage newPage = pdfReportTableUtil.addNewPage(doc, pdfconfigDto.getPdfType());
			yStart = createOpenPdfTableForPanel(doc, newPage, tableIndex, title, columnData, rowData,
					assessmentReportDTO, insightsReportPdfTableConfig, yStart, theme);
		} else {
			yStart = createOpenPdfTableForPanel(doc, page, tableIndex, title, columnData, rowData, assessmentReportDTO,
					insightsReportPdfTableConfig, yStart, theme);
		}

	}

	/**
	 * Create table based on number of rows with observations.
	 * 
	 * @param doc
	 * @param page
	 * @param tableIndex
	 * @param kpiId
	 * @param caption
	 * @param columnData
	 * @param rowData
	 * @param assessmentReportDTO
	 * @param insightsTableConfig
	 * @param yStart
	 * @return
	 */
	public float createOpenPdfTableForPanel(PDDocument doc, PDPage page, int tableIndex, String caption,
			JsonArray columnData, JsonArray rowData, InsightsAssessmentConfigurationDTO assessmentReportDTO,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, float yStart, String theme) {

		float margin = 0;
		float pageTopMargin = 70;
		float bottomMargin = 23;
		float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
		float tableWidth = page.getMediaBox().getWidth() - 10;
		float tw;

		if (pdfconfigDto.getPdfType().equalsIgnoreCase(DASHBOARD)) {
			tw = tableWidth + 10;
		} else {
			tw = tableWidth - 10;
			margin = margin + 10;
		}

		BaseTable dataTable;
		Set<String> headerList = new LinkedHashSet<>();
		try {
			getFont(doc, assessmentReportDTO, insightsReportPdfTableConfig);

			if (tableIndex == 0) {
				yStart = yStartNewPage - 60;
				dataTable = new BaseTable(yStart, yStartNewPage, pageTopMargin, bottomMargin, tw, margin, doc, page,
						true, true);
			} else {
				dataTable = new BaseTable(yStart, yStartNewPage, pageTopMargin, bottomMargin, tw, margin, doc,
						doc.getPage(doc.getPages().getCount() - 1), true, true);
			}

			dataTable.drawTitle(caption, font, Integer.parseInt(insightsReportPdfTableConfig.getTableCaptionFontSize()),
					tableWidth, 5, "center", 1, true);

			for (JsonElement jsonTableHeaderElement : columnData) {
				headerList.add(jsonTableHeaderElement.getAsString());
			}

			Row<PDPage> headerRow = generateDynamicHeader(doc, dataTable, headerList, insightsReportPdfTableConfig,
					theme);

			if (preparePdfRows(doc, rowData, dataTable, headerRow, insightsReportPdfTableConfig, font, theme)) {
				yStart = dataTable.draw() - 70;
				dataTable.drawTitle(" ", font, 6, tableWidth, 5, "center", 1, true);
				dataTable.drawTitle("No Data", font,
						Integer.parseInt(insightsReportPdfTableConfig.getTableCaptionFontSize()), tableWidth, 5,
						"center", 1, true);
			} else {
				yStart = dataTable.draw() - 70;
			}

		} catch (Exception e) {
			log.error(" Worlflow Detail ==== unable to generate table, {} ", e.getMessage());
		}
		return yStart;
	}

	private boolean preparePdfRows(PDDocument doc, JsonArray rowData, BaseTable table, Row<PDPage> headerRow,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, PDFont font, String theme) throws IOException {
		float columnsplit = headerRow.getColCount();
		for (JsonElement jsonTableRowElement : rowData) {
			List<String> rowList = new ArrayList<>(0);
			JsonArray rowValues = jsonTableRowElement.getAsJsonObject().get("row").getAsJsonArray();
			rowValues.forEach(val -> rowList.add(val.toString()));
			headerRow = table.createRow(10f);

			for (String rows : rowList) {
				rows = rows.replace("\"", "");
				rows = rows.replace("null", "-");
				Cell<PDPage> cell = headerRow.createCell((100 / columnsplit), rows);
				cell.setFont(font);
				cell.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableRowsFontSize()));
				cell.setAlign(HorizontalAlignment.LEFT);
				cell.setValign(VerticalAlignment.MIDDLE);
				cellPropsTableData(doc, cell, insightsReportPdfTableConfig, theme);

			}
		}
		if (rowData.size() == 0) {
			return true;
		}
		return false;
	}

	private Row<PDPage> generateDynamicHeader(PDDocument doc, BaseTable dataTable, Set<String> headerList,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, String theme) throws IOException {
		Row<PDPage> headerRow = dataTable.createRow(15f);
		float totalHeader = headerList.size();
		for (String header : headerList) {
			Cell<PDPage> cell = headerRow.createCell((100 / totalHeader), header);
			cellProps(doc, cell, insightsReportPdfTableConfig, theme);
		}

		dataTable.addHeaderRow(headerRow);
		return headerRow;
	}

	private void cellProps(PDDocument doc, Cell<PDPage> sno, InsightsReportPdfTableConfig insightsReportPdfTableConfig,
			String theme) throws IOException {

		// commented code is to add roboto font in table PDF, for future reference
		/*
		 * InputStream templateStream =
		 * getClass().getClassLoader().getResourceAsStream("Font-Family.ttf"); PDFont
		 * pdfont = PDType0Font.load(doc, templateStream);
		 * 
		 * sno.setFont(pdfont);
		 */
		sno.setFont(PDType1Font.HELVETICA);
		if (insightsReportPdfTableConfig.getTableHeaderFillColor().isEmpty()) {
			if (theme.equals("dark")) {
				Color fillColor = new Color(34, 37, 43);
				sno.setFillColor(fillColor);
			} else {
				Color fillColor = new Color(232, 232, 232);
				sno.setFillColor(fillColor);
			}

		} else {
			sno.setFillColor(parse(insightsReportPdfTableConfig.getTableHeaderFillColor()));
		}
		if (insightsReportPdfTableConfig.getTableHeaderTextColor().isEmpty()) {
			if (theme.equals("dark")) {
				Color fillColor = new Color(110, 159, 255);
				sno.setTextColor(fillColor);
			} else {
				Color fillColor = new Color(31, 98, 224);
				sno.setTextColor(fillColor);
			}

		} else {
			sno.setTextColor(parse(insightsReportPdfTableConfig.getTableHeaderTextColor()));
		}
		sno.setAlign(HorizontalAlignment.CENTER);
		sno.setValign(VerticalAlignment.MIDDLE);
		sno.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableHeaderFontSize()));
	}

	private void cellPropsTableData(PDDocument doc, Cell<PDPage> sno,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, String theme) throws IOException {

		sno.setFont(PDType1Font.HELVETICA);
		if (insightsReportPdfTableConfig.getTableHeaderFillColor().isEmpty()) {
			if (theme.equals("dark")) {

				Color fillColor = new Color(24, 27, 31);
				sno.setFillColor(fillColor);

			} else {
				sno.setFillColor(Color.WHITE);
			}
		} else {
			sno.setFillColor(parse(insightsReportPdfTableConfig.getTableHeaderFillColor()));
		}
		if (insightsReportPdfTableConfig.getTableHeaderTextColor().isEmpty()) {
			if (theme.equals("dark")) {
				sno.setTextColor(Color.WHITE);
			} else {
				sno.setTextColor(Color.BLACK);
			}
		} else {
			sno.setTextColor(parse(insightsReportPdfTableConfig.getTableHeaderTextColor()));
		}
		sno.setAlign(HorizontalAlignment.LEFT);
		sno.setValign(VerticalAlignment.MIDDLE);
		sno.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableRowsFontSize()));
	}

	/**
	 * @param doc
	 * @param assessmentReportDTO
	 * @param insightsTableConfig
	 * @throws IOException
	 */
	private void getFont(PDDocument doc, InsightsAssessmentConfigurationDTO assessmentReportDTO,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig) throws IOException {
		File fontFile = new File(assessmentReportDTO.getPdfReportDirPath() + File.separator
				+ insightsReportPdfTableConfig.getFont() + ".ttf");
		if (fontFile.exists()) {
			font = PDType0Font.load(doc, fontFile);
			log.debug("fetchPdfConfig ==== Custom font loaded successfully..");
		} else {
			log.info("fetchPdfConfig ==== No font ttf file present..using default.");
			font = PDType1Font.HELVETICA;
		}
	}

	/**
	 * Parser the rgb code to Color .
	 * 
	 * @param input
	 * @return Color
	 */
	public static Color parse(String input) {
		Pattern c = Pattern.compile(ReportEngineUtils.RGB_PATTERN);
		Matcher m = c.matcher(input);

		if (m.matches()) {
			return new Color(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)));
		}

		return null;
	}

	/**
	 * prepare dynamic HTML for PDF generation
	 * 
	 * @param imageList
	 * @param dynamicTemplate
	 * @param grafanaDashboardConfig
	 * @return
	 */
	private void prepareHtml(List<byte[]> imageList, String dynamicTemplate) {

		StringBuilder htmlStartTag = new StringBuilder();
		htmlStartTag.append("<!DOCTYPE html><html><head>").append("<link rel=\"stylesheet\" href=\"template.css\" />")
				.append("<title>").append(pdfconfigDto.getTitle()).append("</title>").append("</head><body>");
		String rowStart = "<div class=\"row\">";
		String columnStart = "<div class=\"column\">";
		String endDiv = "</div>";
		String htmlEnd = "</body></html>";
		StringBuilder baseImage = new StringBuilder();

		for (byte[] image : imageList) {
			String imgTag = "<img src=\"data:image/jpeg;base64,";
			String imgEndTag = "\" alt=\"Snow\" style=\"width:100%\"></img>";
			baseImage.append(rowStart).append(columnStart).append(imgTag)
					.append(Base64.getEncoder().encodeToString(image)).append(imgEndTag).append(endDiv).append(endDiv);

		}

		baseImage.append(htmlEnd);

		StringBuilder template = new StringBuilder();
		template.append(htmlStartTag).append(baseImage);
		log.debug("Worlflow Detail ==== Generated DynamicTemplate.html content  ===== {} ", template);
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
		try (FileOutputStream outStream = new FileOutputStream(extractedPdfFile)) {
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
	private void modifyFrontPageTemplate(String frontPagePath, String theme) {
		try {

			String templateFilePath = new File(FRONT_PAGE_TEMPLATE).getCanonicalPath();
			File file = new File(templateFilePath);
			StringBuilder render = new StringBuilder();
			if (file.exists()) {
				try (BufferedReader in = new BufferedReader(new FileReader(templateFilePath))) {
					String line;
					while ((line = in.readLine()) != null)
						render.append(line);
				} catch (Exception e) {
					e.getMessage();
				}

			} else {
				String frontPageTemplate = fetchTemplate("frontPageTemplate.html");
				render = new StringBuilder(frontPageTemplate);
				String renderString = null;
				if (theme.equals("dark")) {

					renderString = render.toString();
					renderString = renderString.replace("background-color: blue; color: white", "");
					renderString = renderString.replace("<thead>",
							"<thead style=\"background-color:  #303030; color: #6E9FFF; !imporatant\">");
					render = new StringBuilder(renderString);
				} else {

					renderString = render.toString().replace("background-color: blue; color: white", "");// background-color:
					renderString = render.toString().replace("<thead>",
							"<thead style=\"background-color:  #E8E8E8; color: #1F62E0; !imporatant\">");
					render = new StringBuilder(renderString);
				}

			}

			int titleIndex = render.indexOf("</h1>");
			render.insert(titleIndex, pdfconfigDto.getTitle());
			render.insert(render.indexOf("</span>"), new Date().toString());
			String fontsize = "";
			if (pdfconfigDto.getPdfType().equalsIgnoreCase(DASHBOARD)) {
				fontsize = "160%;";
			} else {
				fontsize = "110%;";
			}
			render.insert(render.indexOf("body {") + 6, "font-size:" + fontsize);
			JsonObject variableJson = formatVariables(pdfconfigDto.getVariables());
			if (!variableJson.keySet().isEmpty()) {
				StringBuilder tableRow = new StringBuilder();
				variableJson.keySet().forEach(key -> {
					String value = variableJson.get(key).getAsJsonArray().toString().replace("[", "").replace("\"", "")
							.replace("]", "");
					String row = "<tr>\r\n" + "            <td style=\"padding: 0.5%;width:20%;\">" + key + "</td>\r\n"
							+ "            <td style=\"padding: 0.5%;width:80%;\">" + value + "</td>\r\n"
							+ "        </tr>";
					tableRow.append(row);
				});
				render.insert(render.indexOf("</thead>"),
						"<tr>\r\n" + "<th>Filter Name</th>\r\n" + "<th>Values</th>\r\n" + "</tr>");
				int index = render.indexOf("</thead>");
				render.insert(index + 8, tableRow);
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
		try (FileWriter fileWriter = new FileWriter(fileName)) {
			printWriter = new PrintWriter(fileWriter);
			printWriter.print(fileContent);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while saving Html file  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}
	}

	/**
	 * Method to merge two PDF files, save in destination folder and returns merged
	 * pdf byte array.
	 * 
	 * @param pdf1
	 * @param pdf2
	 * @param destinationFilePath
	 * @return
	 */
	private byte[] mergePDFFiles(byte[] pdf1, byte[] pdf2, String destinationFilePath) {
		byte final_array[] = null;
		try {
			PDFMergerUtility merger = new PDFMergerUtility();
			merger.addSource(new ByteArrayInputStream(pdf1));
			merger.addSource(new ByteArrayInputStream(pdf2));
			merger.setDestinationFileName(destinationFilePath);
			merger.mergeDocuments(null);
			log.debug("Worlflow Detail ==== Pdf files merged successfully ===== ");

		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while merging PDF Files  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}
		try (FileInputStream inputStream = new FileInputStream(destinationFilePath)) {
			final_array = IOUtils.toByteArray(inputStream);
		} catch (Exception e) {
			log.error(e);
		}
		return final_array;

	}

	/**
	 * Method use to fetch Logo Image from database or filesystem and update in
	 * header template.
	 * 
	 * @return
	 */
	private String updateLogoImgInHeaderTemplate(Boolean isFrontPage) {
		try {
			String headerContent = fetchTemplate(HEADER_HTML);
			StringBuilder headerTemplate = new StringBuilder(headerContent);
			byte[] image = null;
			Icon logoEntity = new IconDAL().fetchEntityData("logo");
			if (logoEntity.getImage() != null) {
				image = logoEntity.getImage();

			} else {
				InputStream imageStream = new FileInputStream(new File(LOGO_IMAGE_PATH).getCanonicalPath());
				image = IOUtils.toByteArray(imageStream);
			}
			StringBuilder imageTag = new StringBuilder();
			imageTag.append("src=\"data:image/webp;base64,").append(Base64.getEncoder().encodeToString(image))
					.append("\" alt=\"OneDevOps\" ");
			int index = headerTemplate.indexOf("<img");
			headerTemplate.insert(index + 5, imageTag);
			if (Boolean.FALSE.equals(isFrontPage)) {
				String titleSpan = "<span style=\"padding-left: 15%;\" float: middle;>" + pdfconfigDto.getTitle()
						+ "</span>";
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
			String dashboardElementFilePath = new File(FilenameUtils.normalize(DASHBOARD_ELEMENT_JSON_PATH))
					.getCanonicalPath();
			File dashboardElementFile = new File(dashboardElementFilePath);
			if (dashboardElementFile.exists()) {
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
