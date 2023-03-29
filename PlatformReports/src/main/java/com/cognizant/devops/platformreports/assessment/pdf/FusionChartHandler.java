/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformreports.assessment.pdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ReportChartCollection;
import com.cognizant.devops.platformcommons.constants.ReportStatusConstants;
import com.cognizant.devops.platformcommons.constants.StringExpressionConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.multipart.MultipartDataHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportTemplateConfigFiles;
import com.cognizant.devops.platformdal.assessmentreport.ReportConfigDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsReportPdfTableConfig;
import com.cognizant.devops.platformreports.assessment.util.PdfReportTableUtil;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FusionChartHandler implements BasePDFProcessor {
	 
	private static Logger log = LogManager.getLogger(FusionChartHandler.class);
	MultipartDataHandler multiPartHandler = new MultipartDataHandler();
	private float yStart = 1f;
	ReportConfigDAL reportConfigDAL = new ReportConfigDAL();
	
	@Override
	public void generatePDF(InsightsAssessmentConfigurationDTO assessmentReportDTO) {

		try {
			long startTime = System.nanoTime();
			createPDFDirectory(assessmentReportDTO);

			JsonArray finalTemplateJson = generateChartsConfig(assessmentReportDTO);

			modifyHtmlTemplate(assessmentReportDTO);
			
			InsightsReportPdfTableConfig insightsReportPdfTableConfig = processHtmlTableWithOpenPDF(
					assessmentReportDTO);
			
			exportPDFFile(finalTemplateJson, assessmentReportDTO, insightsReportPdfTableConfig);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",processingTime,
					ReportStatusConstants.REPORT_ID +assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil());

		} catch (Exception e) {
			log.error(e);
			log.error(StringExpressionConstants.STR_EXP_TASK,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
					ReportStatusConstants.REPORT_ID +assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
					assessmentReportDTO.getVisualizationutil() + e.getMessage());
			throw new InsightsJobFailedException(e.getMessage());
		}

	}

	private void createPDFDirectory(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			long startTime = System.nanoTime();
			String folderName = assessmentReportDTO.getAsseementreportname() + "_"
					+ assessmentReportDTO.getExecutionId();
			assessmentReportDTO.setPdfReportFolderName(folderName);
			String reportExecutionFile = AssessmentReportAndWorkflowConstants.REPORT_PDF_EXECUTION_RESOLVED_PATH
					+ folderName;
			assessmentReportDTO.setPdfReportDirPath(reportExecutionFile);
			setReportExecutionFolder(assessmentReportDTO);
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",processingTime,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + "Foldername" +folderName
					);
		} catch (Exception e) {
			log.error(e);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
					ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() +"Unable to create pdf execution directory"
					+ e.getMessage());
			throw new InsightsJobFailedException(
					"Unable to create pdf execution directory, message == " + e.getMessage());			
		}
	}

	public void setReportExecutionFolder(InsightsAssessmentConfigurationDTO assessmentReportDTO) throws IOException {

		List<InsightsReportTemplateConfigFiles> records = reportConfigDAL
				.getReportTemplateConfigFileByReportId(assessmentReportDTO.getReportId());
		for (InsightsReportTemplateConfigFiles record : records) {
			String filePath = assessmentReportDTO.getPdfReportDirPath() + File.separator + record.getFileName();
			File file = new File(filePath);
			if (!file.exists()) {
				FileUtils.writeByteArrayToFile(file, record.getFileData());
			}
		}
	}

	private JsonArray generateChartsConfig(InsightsAssessmentConfigurationDTO assessmentReportDTO) throws IOException {
		JsonArray finalTemplateJson = new JsonArray();
		String templateJsonPath = assessmentReportDTO.getPdfReportDirPath() + File.separator
				+ assessmentReportDTO.getReportFilePath() + ".json";
		try {
			String json = new String(Files.readAllBytes(Paths.get(new File(templateJsonPath).getCanonicalPath()).toAbsolutePath()));
			JsonArray pdfTemplateJsonArray =JsonUtils.parseStringAsJsonArray(json);
			Map<String, JsonObject> templateJsonObjMap = loadTemplateJson(pdfTemplateJsonArray);
			Map<String, String> contentMap = new HashMap<>();
			Map<String, List<String>> kpiContentMap = new HashMap<>();
			Map<String, JsonArray> tableJsonObjMap = new HashMap<>();
			JsonArray kpiResultArray = assessmentReportDTO.getVisualizationResult();
			for (JsonElement element : kpiResultArray) {
				boolean isTable = false;
				JsonObject eachKpiResult = element.getAsJsonObject();
				String kpiId = eachKpiResult.get(AssessmentReportAndWorkflowConstants.KPIID).getAsString();
				JsonArray kpiVisualizationArray = eachKpiResult.get("visualizationresult").getAsJsonArray();
				log.debug("Worlflow Detail ====  generateChartsConfig for kpi {} kpiVisualizationArray  {} ", kpiId,
						kpiVisualizationArray);			
				for (JsonElement eachElement : kpiVisualizationArray) {
					JsonObject eachVisualizationResult = eachElement.getAsJsonObject();
					String vType = eachVisualizationResult.get("vType").getAsString();
					JsonArray fromkpiResultArray = eachVisualizationResult.get("KpiResult").getAsJsonArray();
					JsonObject visualizationObjectFromTemplateJson = templateJsonObjMap.get(vType);
					// add KPi Data
					if (visualizationObjectFromTemplateJson != null && !vType.startsWith("table")) {
						JsonObject toObject = getChartJsonFromVisualizationJson(fromkpiResultArray,
								visualizationObjectFromTemplateJson);
						finalTemplateJson.add(toObject);
					} else if (vType.startsWith("table") && fromkpiResultArray.size() > 0) {
						isTable=true;
						String caption = "";
						if (templateJsonObjMap.containsKey(vType)) {
							caption = templateJsonObjMap.get(vType).getAsJsonObject().get(AssessmentReportAndWorkflowConstants.CAPTION).getAsString();
						}
						fromkpiResultArray.get(0).getAsJsonObject().addProperty(AssessmentReportAndWorkflowConstants.CAPTION, caption);
						fromkpiResultArray.get(0).getAsJsonObject().addProperty(AssessmentReportAndWorkflowConstants.KPIID, kpiId);
						fromkpiResultArray.get(0).getAsJsonObject().addProperty(AssessmentReportAndWorkflowConstants.KPIID, kpiId);
						tableJsonObjMap.put(vType, fromkpiResultArray);
					} else {
						log.debug(
								"Worlflow Detail ==== For KPI id {} , No template found for vType {}  or Kpi Result is empty {} ",
								kpiId, vType, fromkpiResultArray.size());
					}
				}
				extractContentDataFromVisualizationResponse(contentMap, eachKpiResult, kpiId, kpiContentMap,isTable);
			}
			assessmentReportDTO.setTableJsonObjMap(tableJsonObjMap);
			assessmentReportDTO.setContentMap(contentMap);
			assessmentReportDTO.setKpiContentMap(kpiContentMap);
		} catch (NoSuchFileException e) {
			log.error("Worlflow Detail ==== unable to create charts config report template json file not found", e);
			throw new InsightsJobFailedException(" Unable to generate PDF, report template json file not found "
					+ assessmentReportDTO.getReportFilePath() + ".json");
		} catch (Exception e) {
			log.error("Worlflow Detail ==== unable to create charts config ", e);
			throw new InsightsJobFailedException(" unable to create charts config " + e);
		}
		return finalTemplateJson;
	}

	/**
	 * Used to fetch all content information from common response
	 * 
	 * @param contentMap
	 * @param eachKpiResult
	 * @param kpiId
	 */
	private void extractContentDataFromVisualizationResponse(Map<String, String> contentMap, JsonObject eachKpiResult,
			String kpiId, Map<String, List<String>> kpiContentMap,boolean isTable) {
		if (eachKpiResult.has("contentResult")) {
			JsonArray kpiContentArray = eachKpiResult.get("contentResult").getAsJsonArray();
			log.debug("Worlflow Detail ==== generateChartsConfig for kpi {} kpiContentArray  {} ", kpiId,
					kpiContentArray);			
			
			prepareKpiContentMap(kpiContentArray,isTable,contentMap,kpiId,kpiContentMap);
			
		}
	}

	private void prepareKpiContentMap(JsonArray kpiContentArray, boolean isTable, Map<String, String> contentMap,
			            String kpiId, Map<String, List<String>> kpiContentMap) {
		
		if (kpiContentArray.size() > 0) {
			JsonObject content = kpiContentArray.get(0).getAsJsonObject();
			JsonArray dataArray = content.get("data").getAsJsonArray();
			List<String> observationList = new ArrayList<>();
			
			for (JsonElement eachData : dataArray) {
				JsonObject eachRowObject = eachData.getAsJsonObject();
				JsonArray eachRowArray = eachRowObject.get("row").getAsJsonArray();
				// Assuming First Array Item as content Text
				if (!eachRowArray.get(0).isJsonNull()) {
					String contentText = eachRowArray.get(0).getAsString();
					// Assuming Second Array Item as content Id
					String contentId = eachRowArray.get(1).getAsString();
					if (!isTable) {
						contentMap.put(contentId, contentText);
					}
					observationList.add(contentText);
				} else {
					log.debug(
							"Worlflow Detail ==== generateChartsConfig for Content {} value is null in kpiContentArray  {} ",
							eachRowArray.get(1).getAsString(), eachData);
				}
			}
			kpiContentMap.put(kpiId, observationList);
		}
		
	}
	
	private void modifyHtmlTemplate(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		String templateHtmlPath = assessmentReportDTO.getPdfReportDirPath() + File.separator
				+ assessmentReportDTO.getReportFilePath() + AssessmentReportAndWorkflowConstants.HTMLEXTENSION;
		log.debug("Worlflow Detail ==== templateHtmlPath {} ", templateHtmlPath);
		try {	
			long startTime = System.nanoTime();
			File render = new File(templateHtmlPath);
			Document document = Jsoup.parse(render, "UTF-8");
			String originalHTml = document.toString();
			processHtmlContent(assessmentReportDTO, document);
			String modifiedHtml = document.toString();
			if (!originalHTml.equalsIgnoreCase(modifiedHtml)) {
				try(PrintWriter printWriter = new PrintWriter(new FileWriter(templateHtmlPath))) {
					printWriter.print(modifiedHtml);
				}
			}
			long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
			log.debug(StringExpressionConstants.STR_EXP_TASK,
					assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",processingTime,
					"reportId: "+assessmentReportDTO.getReportId() + "reportName: " +assessmentReportDTO.getReportName()
					);
		} catch (FileNotFoundException e) {
			log.error("Worlflow Detail ==== Unable to update html template , report template html file not found", e);
			throw new InsightsJobFailedException("Unable to update html template, report template html file not found "
					+ assessmentReportDTO.getReportFilePath() + AssessmentReportAndWorkflowConstants.HTMLEXTENSION);
		} catch (Exception e) {
			log.error("Worlflow Detail ==== unable to modify html and content data  ", e);
			throw new InsightsJobFailedException("unable to modify html and content data " + e);
		} 
	}

	private InsightsReportPdfTableConfig processHtmlTableWithOpenPDF(
			InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		InsightsReportPdfTableConfig insightsReportPdfTableConfig = new InsightsReportPdfTableConfig();
		try {
			String templateHtmlPath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getReportFilePath() + AssessmentReportAndWorkflowConstants.HTMLEXTENSION;
			File render = new File(templateHtmlPath);
			Document document;
			document = Jsoup.parse(render, "UTF-8");
			String reportDirPath = assessmentReportDTO.getPdfReportDirPath() + File.separator;
			PdfReportTableUtil pdfReportTableUtil = new PdfReportTableUtil();
			PDDocument doc = new PDDocument();
			PDPage page = pdfReportTableUtil.addNewPage(doc, "tablePDF");
			pdfReportTableUtil.fetchPdfConfig(reportDirPath, insightsReportPdfTableConfig);
			Elements allTableDiv = document.getElementsByAttributeValueMatching("id", "table_*");
			if (!allTableDiv.isEmpty()) {
				Map<String, JsonArray> tableJsonObjMap = assessmentReportDTO.getTableJsonObjMap();
				int tableIndex = 0;
				for (Element elmTableDiv : allTableDiv) {
					String divId = elmTableDiv.attr("id");
					if (tableJsonObjMap.containsKey(divId)) {
						commonTableResponse(elmTableDiv, tableJsonObjMap.get(divId), assessmentReportDTO, tableIndex,
								doc, page, insightsReportPdfTableConfig);
						tableIndex++;
					}
				}
				try {
					if (tableIndex > 0) {
						doc.save(assessmentReportDTO.getPdfReportDirPath() + File.separator
								+ assessmentReportDTO.getAsseementreportname() + "_Open."
								+ ReportEngineUtils.REPORT_TYPE);
					}
				} catch (IOException e) {
					log.error("Worlflow Detail ==== unable to save open pdf with tables ", e);
				} finally {
					try {
						doc.close();
					} catch (IOException e) {
						log.error("Worlflow Detail ==== unable to close open pdf with tables ", e);
					}
				}
			}
		} catch (IOException e1) {
			log.error(e1);
		}

		return insightsReportPdfTableConfig;

	}
	

	private void processHtmlContent(InsightsAssessmentConfigurationDTO assessmentReportDTO, Document document) {
		Elements allContentDiv = document.getElementsByAttributeValueMatching("id", "content_*");
		if (!allContentDiv.isEmpty()) {
			Map<String, String> contentMap = assessmentReportDTO.getContentMap();
			for (Element elmDiv : allContentDiv) {
				String divId = elmDiv.attr("id");
				String contentId = divId.split("_")[1];
				if (contentMap.containsKey(contentId)) {
					addContentInHtmlTag(elmDiv, contentMap.get(contentId));
				}
			}
		}
	}

	private void exportPDFFile(JsonArray finalTemplateJson, InsightsAssessmentConfigurationDTO assessmentReportDTO, InsightsReportPdfTableConfig insightsReportPdfTableConfig) {
		Path zipPathVeriable = null;
		try {
			long startTime = System.nanoTime();
			String url = ApplicationConfigProvider.getInstance().getAssessmentReport().getFusionExportAPIUrl();
			String zipPath = assessmentReportDTO.getPdfReportDirPath() + ".zip";
			Path sourceFolderPath = Paths.get(assessmentReportDTO.getPdfReportDirPath());
			zipPathVeriable = Paths.get(zipPath);
			Path zipData = getPDFZipFolder(sourceFolderPath, zipPathVeriable);
			if (zipData.toFile().exists()) {
				log.debug("Worlflow Detail ====  Zip file created  {} ", zipPath);
			} else {
				throw new InsightsCustomException("Worlflow Detail ==== Zip file not created ");
			}
			String exportedFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.REPORT_TYPE;
			assessmentReportDTO.setPdfExportedFilePath(exportedFilePath);
			String fusionFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "_Fusion." + ReportEngineUtils.REPORT_TYPE;

			String templateFilePathInZip = assessmentReportDTO.getReportFilePath() + AssessmentReportAndWorkflowConstants.HTMLEXTENSION;
			String dashboardLogoPathInzip = "image.webp";

			Map<String, String> multipartFiles = new HashMap<>();
			multipartFiles.put("payload", zipPath);
			String chartConfigJson = new Gson().toJson(finalTemplateJson);
			Map<String, String> formDataMultiPartMap = new HashMap<>();
			formDataMultiPartMap.put("chartConfig", chartConfigJson);
			formDataMultiPartMap.put("templateFilePath", templateFilePathInZip);
			formDataMultiPartMap.put("dashboardLogo", dashboardLogoPathInzip);
			formDataMultiPartMap.put("dashboardHeading", assessmentReportDTO.getAsseementreportdisplayname());

			formDataMultiPartMap.put("type", ReportEngineUtils.REPORT_TYPE);
			formDataMultiPartMap.put("templateFormat", "A3");
			formDataMultiPartMap.put("footerEnabled", "true");
			formDataMultiPartMap.put("footerComponents",
					"{\"pageNumber\":{\"style\":\"margin-left:105px;\",\"format\": \"{{current}}\"},\"date\":{\"style\":\"float:right;margin-right:105px;\"}}");

			Map<String, String> headers = new HashMap<>();

			boolean fileUploadStatus = multiPartHandler.uploadMultipartFile(url, multipartFiles, formDataMultiPartMap,
					headers, ReportEngineUtils.REPORT_MEDIA_TYPE, fusionFilePath);
			if (fileUploadStatus) {
				log.debug("Worlflow Detail ==== pdf File Saved {} ", fusionFilePath);
				long processingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
				log.debug(StringExpressionConstants.STR_EXP_TASK,
						assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",processingTime,
						ReportStatusConstants.REPORT_ID+assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
						assessmentReportDTO.getVisualizationutil() + "FusionFilePath :" +fusionFilePath);
				String fusion = assessmentReportDTO.getPdfReportDirPath() + File.separator
						+ assessmentReportDTO.getAsseementreportname() + "_Fusion." + ReportEngineUtils.REPORT_TYPE;
				String open = assessmentReportDTO.getPdfReportDirPath() + File.separator
						+ assessmentReportDTO.getAsseementreportname() + "_Open." + ReportEngineUtils.REPORT_TYPE;
				File openFile = new File(open);
				if(openFile.exists()) {
					PDDocument fusionPages = PDDocument.load(new File(fusion));
					PDDocument openPages = PDDocument.load(new File(open));
					PDDocument f = new PdfReportTableUtil().footer(assessmentReportDTO,openPages,fusionPages.getPages().getCount(),insightsReportPdfTableConfig, "tablePDF");
					f.save(new File(open));
					PDFMergerUtility merger = new PDFMergerUtility();
					merger.setDestinationFileName(exportedFilePath);
					merger.addSource(new File(fusion));
					merger.addSource(open);
					merger.mergeDocuments(null);
				}else {
					PDFMergerUtility merger = new PDFMergerUtility();
					merger.setDestinationFileName(exportedFilePath);
					merger.addSource(new File(fusion));
					merger.mergeDocuments(null);
				}
			} else {
				log.error("Worlflow Detail ==== Error while created pdf file {}", exportedFilePath);
				log.error(StringExpressionConstants.STR_EXP_TASK,
						assessmentReportDTO.getExecutionId(),assessmentReportDTO.getWorkflowId(),assessmentReportDTO.getConfigId(),"-","-","-",0,
						ReportStatusConstants.REPORT_ID + assessmentReportDTO.getReportId() + ReportStatusConstants.REPORT_NAME +assessmentReportDTO.getReportName() + ReportStatusConstants.VISUALIZATION_UTIL +
						assessmentReportDTO.getVisualizationutil() + "FusionFilePath :" +fusionFilePath +"Error while created pdf file");
				throw new InsightsJobFailedException(
						"Unable to generate pdf, Please check pdf export server connectivity  ");
			}
		} catch (Exception e) {
			log.error("Worlflow Detail ==== Error while created pdf ", e);
			throw new InsightsJobFailedException(e.getMessage());
		} finally {
			try {
				if (zipPathVeriable != null) {
					Files.delete(zipPathVeriable);
				}
			} catch (Exception e) {
				log.error(" Worlflow Detail ==== unable to delete file, It might not created ");
			}
		}

	}

	public Path getPDFZipFolder(Path sourceFolderPath, Path zipPath) throws IOException {
		Path normalizedZipPath = zipPath.normalize();
		try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(normalizedZipPath.toFile()))) {
			Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString().replace("\\", "/")));
					Files.copy(file, zos);
					zos.closeEntry();
					return FileVisitResult.CONTINUE;
				}
			});
		}
		return zipPath;
	}

	private JsonObject getChartJsonFromVisualizationJson(JsonArray fromkpiResultArray, JsonObject toTemplateObject) {

		if (fromkpiResultArray.size() > 0) {
			JsonArray columnData = fromkpiResultArray.get(0).getAsJsonObject().get("columns").getAsJsonArray();
			JsonArray rowData = fromkpiResultArray.get(0).getAsJsonObject().get("data").getAsJsonArray();

			String chartType = toTemplateObject.get("type").getAsString();

			/* Single Series Response */

			if (ReportChartCollection.SINGLE_SERIES_CHARTS.contains(chartType)) {
				singleSeriesResonse(rowData, toTemplateObject);
			}

			else if (ReportChartCollection.SINGLE_VALUE_CHARTS.contains(chartType)) {
				singleValueResponse(rowData, toTemplateObject);
			}
			/* Common Response */
			else if (ReportChartCollection.COMMON_CHARTS.contains(chartType)) {
				commonChartResponse(columnData, rowData, toTemplateObject);
			}

			else {
				log.debug("Worlflow Detail ==== PDFExecutionSubscriber ====="
						+ " No Supported Chart Type {}  for KPI {} for report ", chartType);
			}
		} else {
			log.debug("Worlflow Detail ==== No KPI data found for kpi  ");
			toTemplateObject.get(AssessmentReportAndWorkflowConstants.FUSION_DATASOURCE).getAsJsonObject().add("categories", new JsonArray());
			toTemplateObject.get(AssessmentReportAndWorkflowConstants.FUSION_DATASOURCE).getAsJsonObject().add("dataset", new JsonArray());
		}
		return toTemplateObject;
	}

	private Map<String, JsonObject> loadTemplateJson(JsonArray templateJsonArray) {
		Map<String, JsonObject> map = new HashMap<>();
		for (JsonElement object : templateJsonArray) {
			JsonObject templateObject = object.getAsJsonObject();
			String renderAt = templateObject.get("renderAt").getAsString();
			map.put(renderAt, templateObject);
		}
		return map;
	}

	private JsonObject singleSeriesResonse(JsonArray rowData, JsonObject toTemplateObject) {

		JsonArray data = new JsonArray();

		for (JsonElement row : rowData) {

			String labelValue = row.getAsJsonObject().get("row").getAsJsonArray().get(0).getAsString();
			int value = row.getAsJsonObject().get("row").getAsJsonArray().get(1).getAsInt();
			JsonObject dataObject = new JsonObject();
			dataObject.addProperty("label", labelValue);
			dataObject.addProperty(AssessmentReportAndWorkflowConstants.VALUE, value);
			data.add(dataObject);

		}
		toTemplateObject.get(AssessmentReportAndWorkflowConstants.FUSION_DATASOURCE).getAsJsonObject().add("data", data);

		return toTemplateObject;
	}

	private JsonObject singleValueResponse(JsonArray rowData, JsonObject toTemplateObject) {
		JsonArray dial = new JsonArray();
		for (JsonElement row : rowData) {
			int value = row.getAsJsonObject().get("row").getAsJsonArray().get(1).getAsInt();
			JsonObject dialObject = new JsonObject();
			dialObject.addProperty(AssessmentReportAndWorkflowConstants.VALUE, value);
			dial.add(dialObject);
		}
		JsonObject dialArray = new JsonObject();
		dialArray.add("dial", dial);
		toTemplateObject.get(AssessmentReportAndWorkflowConstants.FUSION_DATASOURCE).getAsJsonObject().add("dials", dialArray);

		return toTemplateObject;
	}

	private JsonObject commonChartResponse(JsonArray columnData, JsonArray rowData, JsonObject toTemplateObject) {

		JsonArray categoriesArray = new JsonArray();
		JsonArray datasetArray = new JsonArray();
		JsonArray categoryArray = new JsonArray();
		for (int i = 0; i < columnData.size(); i++) {
			String seriesName = columnData.get(i).getAsString();
			JsonArray dataArray = new JsonArray();
			JsonObject datasetObject = new JsonObject();
			for (JsonElement data : rowData) {

				if (i == 0) // Assuming 1st element as label always
				{
					String rowValue = data.getAsJsonObject().get("row").getAsJsonArray().get(i).getAsString();
					JsonObject labelObject = new JsonObject();
					labelObject.addProperty("label", rowValue);
					categoryArray.add(labelObject);
				} else {
					int rowValue = data.getAsJsonObject().get("row").getAsJsonArray().get(i).getAsInt();
					JsonObject dataObject = new JsonObject();
					dataObject.addProperty(AssessmentReportAndWorkflowConstants.VALUE, rowValue);
					dataArray.add(dataObject);
				}

			}
			if (i != 0) {
				datasetObject.addProperty("seriesname", seriesName);
				datasetObject.add("data", dataArray);
				datasetArray.add(datasetObject);
			}
		}
		JsonObject categoryObject = new JsonObject();
		categoryObject.add(AssessmentReportAndWorkflowConstants.CATEGORY, categoryArray);
		categoriesArray.add(categoryObject);
		toTemplateObject.get(AssessmentReportAndWorkflowConstants.FUSION_DATASOURCE).getAsJsonObject().add("categories", categoriesArray);
		toTemplateObject.get(AssessmentReportAndWorkflowConstants.FUSION_DATASOURCE).getAsJsonObject().add("dataset", datasetArray);

		return toTemplateObject;
	}

	private void addContentInHtmlTag(Element elmDiv, String contentText) {
		StringBuffer contentList = new StringBuffer();
		contentList.append("<ul>");

		contentList.append("<li>" + contentText + "</li>");

		contentList.append("</ul>");

		elmDiv.append(contentList.toString());
	}

	private void commonTableResponse(Element elmDiv, JsonArray fromTableResultArray,
			InsightsAssessmentConfigurationDTO assessmentReportDTO, int tableIndex, PDDocument doc, PDPage page,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig) {
		JsonArray columnData = fromTableResultArray.get(0).getAsJsonObject().get("columns").getAsJsonArray();
		JsonArray rowData = fromTableResultArray.get(0).getAsJsonObject().get("data").getAsJsonArray();
		String caption = fromTableResultArray.get(0).getAsJsonObject().get("caption").getAsString();
		String kpiId = fromTableResultArray.get(0).getAsJsonObject().get(AssessmentReportAndWorkflowConstants.KPIID).getAsString();
		
		PdfReportTableUtil tableUtil = new PdfReportTableUtil();
		yStart = tableUtil.createTable(doc, page, tableIndex, kpiId, caption, columnData, rowData, assessmentReportDTO,
				insightsReportPdfTableConfig, yStart);

	}
	
	
	
}
