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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.dal.multipart.MultipartDataHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportChartCollection;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FusionChartHandler implements BasePDFProcessor {

	private static Logger log = LogManager.getLogger(FusionChartHandler.class);
	MultipartDataHandler multiPartHandler = new MultipartDataHandler();

	@Override
	public void generatePDF(InsightsAssessmentConfigurationDTO assessmentReportDTO) {

		try {

			createPDFDirectory(assessmentReportDTO);

			JsonArray finalTemplateJson = generateChartsConfig(assessmentReportDTO);

			modifyHtmlTemplate(assessmentReportDTO);

			exportPDFFile(finalTemplateJson, assessmentReportDTO);

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
			String reportExecutionFile = ReportEngineUtils.REPORT_PDF_EXECUTION_RESOLVED_PATH + folderName;
			assessmentReportDTO.setPdfReportDirPath(reportExecutionFile);
			String templatePath = ReportEngineUtils.REPORT_PDF_RESOLVED_PATH
					+ ReportEngineUtils.REPORT_CONFIG_TEMPLATE_DIR + File.separator
					+ assessmentReportDTO.getReportFilePath();
			log.debug("Worlflow Detail ==== path sourceTemplateFolderPath {} reportExecutionFile {}  ", templatePath,
					reportExecutionFile);

			File sourceTemplateFolderPath = new File(templatePath);
			File reportExecutionFolder = new File(reportExecutionFile);
			if (sourceTemplateFolderPath.exists()) {
				getReportExecutionFolder(sourceTemplateFolderPath, reportExecutionFolder);
			} else {
				throw new InsightsJobFailedException("PDF report template directory not exists ");
			}
		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException(
					"Unable to create pdf execution directory, message == " + e.getMessage());
		}
	}

	public String getReportExecutionFolder(File sourceFolderPath, File reportExecutionFolder) throws IOException {

		for (File srcFile : sourceFolderPath.listFiles()) {
			if (!srcFile.isDirectory()) {
				FileUtils.copyFileToDirectory(srcFile, reportExecutionFolder);
			}
		}
		return reportExecutionFolder.getAbsolutePath();
	}

	private JsonArray generateChartsConfig(InsightsAssessmentConfigurationDTO assessmentReportDTO) throws IOException {
		JsonArray finalTemplateJson = new JsonArray();
		String templateJsonPath = assessmentReportDTO.getPdfReportDirPath() + File.separator
				+ assessmentReportDTO.getReportFilePath() + ".json";
		try {
			String json = new String(Files.readAllBytes(Paths.get(templateJsonPath)));
			JsonArray pdfTemplateJsonArray = new JsonParser().parse(json).getAsJsonArray();
			Map<String, JsonObject> templateJsonObjMap = loadTemplateJson(pdfTemplateJsonArray);
			Map<String, String> contentMap = new HashMap<>();
			Map<String, JsonArray> tableJsonObjMap = new HashMap<>();
			JsonArray kpiResultArray = assessmentReportDTO.getVisualizationResult();
			for (JsonElement element : kpiResultArray) {
				JsonObject eachKpiResult = element.getAsJsonObject();
				String kpiId = eachKpiResult.get("kpiId").getAsString();
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
						String caption = "";
						if (templateJsonObjMap.containsKey(vType)) {
							caption = templateJsonObjMap.get(vType).getAsJsonObject().get("caption").getAsString();
						}
						fromkpiResultArray.get(0).getAsJsonObject().addProperty("caption", caption);
						tableJsonObjMap.put(vType, fromkpiResultArray);
					} else {
						log.debug(
								"Worlflow Detail ==== For KPI id {} , No template found for vType {}  or Kpi Result is empty {} ",
								kpiId, vType, fromkpiResultArray.size());
					}
				}
				extractContentDataFromVisualizationResponse(contentMap, eachKpiResult, kpiId);
			}
			assessmentReportDTO.setTableJsonObjMap(tableJsonObjMap);
			assessmentReportDTO.setContentMap(contentMap);
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
			String kpiId) {
		if (eachKpiResult.has("contentResult")) {
			JsonArray kpiContentArray = eachKpiResult.get("contentResult").getAsJsonArray();
			log.debug("Worlflow Detail ==== generateChartsConfig for kpi {} kpiContentArray  {} ", kpiId,
					kpiContentArray);
			if (kpiContentArray.size() > 0) {
				JsonObject content = kpiContentArray.get(0).getAsJsonObject();
				JsonArray dataArray = content.get("data").getAsJsonArray();
				for (JsonElement eachData : dataArray) {
					JsonObject eachRowObject = eachData.getAsJsonObject();
					JsonArray eachRowArray = eachRowObject.get("row").getAsJsonArray();
					// Assuming First Array Item as content Text
					if (!eachRowArray.get(0).isJsonNull()) {
						String contentText = eachRowArray.get(0).getAsString();
						// Assuming Second Array Item as content Id
						String contentId = eachRowArray.get(1).getAsString();
						contentMap.put(contentId, contentText);
					} else {
						log.debug(
								"Worlflow Detail ==== generateChartsConfig for Content {} value is null in kpiContentArray  {} ",
								eachRowArray.get(1).getAsString(), eachData);
					}
				}
			}

		}
	}

	private void modifyHtmlTemplate(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		String templateHtmlPath = assessmentReportDTO.getPdfReportDirPath() + File.separator
				+ assessmentReportDTO.getReportFilePath() + ".html";
		PrintWriter printWriter = null;
		log.debug("Worlflow Detail ==== templateHtmlPath {} ", templateHtmlPath);
		try {
			File render = new File(templateHtmlPath);
			Document document = Jsoup.parse(render, "UTF-8");
			String originalHTml = document.toString();
			processHtmlContent(assessmentReportDTO, document);
			processHtmlTable(assessmentReportDTO, document);

			String modifiedHtml = document.toString();
			if (!originalHTml.equalsIgnoreCase(modifiedHtml)) {
				printWriter = new PrintWriter(new FileWriter(templateHtmlPath));
				printWriter.print(modifiedHtml);
			}

		} catch (FileNotFoundException e) {
			log.error("Worlflow Detail ==== Unable to update html template , report template html file not found", e);
			throw new InsightsJobFailedException("Unable to update html template, report template html file not found "
					+ assessmentReportDTO.getReportFilePath() + ".html");
		} catch (Exception e) {
			log.error("Worlflow Detail ==== unable to modify html and content data  ", e);
			throw new InsightsJobFailedException("unable to modify html and content data " + e);
		} finally {

			if (printWriter != null) {
				printWriter.close();
			}
		}

	}

	private void processHtmlTable(InsightsAssessmentConfigurationDTO assessmentReportDTO, Document document) {
		Elements allTableDiv = document.getElementsByAttributeValueMatching("id", "table_*");
		if (!allTableDiv.isEmpty()) {
			Map<String, JsonArray> tableJsonObjMap = assessmentReportDTO.getTableJsonObjMap();
			for (Element elmTableDiv : allTableDiv) {
				String divId = elmTableDiv.attr("id");
				if (tableJsonObjMap.containsKey(divId)) {
					commonTableResponse(elmTableDiv, tableJsonObjMap.get(divId));
				}
			}
		}
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

	private void exportPDFFile(JsonArray finalTemplateJson, InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		Path zipPathVeriable = null;
		try {
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

			String templateFilePathInZip = assessmentReportDTO.getReportFilePath() + ".html";
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
					"{\"pageNumber\":{\"style\":\"margin-left:105px;\"},\"date\":{\"style\":\"float:right;margin-right:105px;\"}}");

			Map<String, String> headers = new HashMap<>();

			boolean fileUploadStatus = multiPartHandler.uploadMultipartFile(url, multipartFiles, formDataMultiPartMap,
					headers, ReportEngineUtils.REPORT_MEDIA_TYPE, exportedFilePath);
			if (fileUploadStatus) {
				log.debug("Worlflow Detail ==== pdf File Saved {} ", exportedFilePath);
			} else {
				log.error("Worlflow Detail ==== Error while created pdf file {} ", exportedFilePath);
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
		try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
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
			toTemplateObject.get("dataSource").getAsJsonObject().add("categories", new JsonArray());
			toTemplateObject.get("dataSource").getAsJsonObject().add("dataset", new JsonArray());
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
			dataObject.addProperty("value", value);
			data.add(dataObject);

		}
		toTemplateObject.get("dataSource").getAsJsonObject().add("data", data);

		return toTemplateObject;
	}

	private JsonObject singleValueResponse(JsonArray rowData, JsonObject toTemplateObject) {
		JsonArray dial = new JsonArray();
		for (JsonElement row : rowData) {
			int value = row.getAsJsonObject().get("row").getAsJsonArray().get(1).getAsInt();
			JsonObject dialObject = new JsonObject();
			dialObject.addProperty("value", value);
			dial.add(dialObject);
		}
		JsonObject dialArray = new JsonObject();
		dialArray.add("dial", dial);
		toTemplateObject.get("dataSource").getAsJsonObject().add("dials", dialArray);

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
					dataObject.addProperty("value", rowValue);
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
		categoryObject.add("category", categoryArray);
		categoriesArray.add(categoryObject);
		toTemplateObject.get("dataSource").getAsJsonObject().add("categories", categoriesArray);
		toTemplateObject.get("dataSource").getAsJsonObject().add("dataset", datasetArray);

		return toTemplateObject;
	}

	private void addContentInHtmlTag(Element elmDiv, String contentText) {
		StringBuffer contentList = new StringBuffer();
		contentList.append("<ul>");

		contentList.append("<li>" + contentText + "</li>");

		contentList.append("</ul>");

		elmDiv.append(contentList.toString());
	}

	private void commonTableResponse(Element elmDiv, JsonArray fromTableResultArray) {
		JsonArray columnData = fromTableResultArray.get(0).getAsJsonObject().get("columns").getAsJsonArray();
		JsonArray rowData = fromTableResultArray.get(0).getAsJsonObject().get("data").getAsJsonArray();
		String caption = fromTableResultArray.get(0).getAsJsonObject().get("caption").getAsString();
		StringBuffer tableList = new StringBuffer();
		tableList.append("<h2 class='tablecaption'>" + caption + "</h2>");
		tableList.append("<table class='table table-bordered'>");
		tableList.append("<thead>");
		tableList.append("<tr>");
		for (JsonElement jsonTableHeaderElement : columnData) {
			tableList.append("<th>" + jsonTableHeaderElement.getAsString() + "</th>");
		}
		tableList.append("</tr>");
		tableList.append("</thead>");
		tableList.append("<tbody>");
		for (JsonElement jsonTableRowElement : rowData) {
			tableList.append("<tr>");
			JsonArray rowValues = jsonTableRowElement.getAsJsonObject().get("row").getAsJsonArray();
			for (JsonElement rowValue : rowValues) {
				tableList.append("<td>" + rowValue.getAsString() + "</td>");
			}
			tableList.append("</tr>");
		}
		tableList.append("</tbody>");
		tableList.append("</table>");

		elmDiv.append(tableList.toString());
	}
}
