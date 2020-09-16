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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformauditing.util.PdfTableUtil;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class OpenPDFChartHandler implements BasePDFProcessor {
	
	private static Logger log = LogManager.getLogger(OpenPDFChartHandler.class);

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
			String reportExecutionFile = ReportEngineUtils.REPORT_PDF_EXECUTION_RESOLVED_PATH + folderName;
			assessmentReportDTO.setPdfReportDirPath(reportExecutionFile);
			File reportExecutionFolder = new File(reportExecutionFile);
			reportExecutionFolder.mkdir();
		} catch (Exception e) {
			log.error(e);
			throw new InsightsJobFailedException(
					"Unable to create pdf execution directory, message == " + e.getMessage());
		}
	}

	
	private void prepareAndExportPDFFile(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			Set<String> headerList = new LinkedHashSet<>();
			List<String> rowValueList = new ArrayList<>();
			JsonArray kpiResultArray = assessmentReportDTO.getVisualizationResult();
			log.debug("Workflow Detail ====  kpiResultArray  {} ", kpiResultArray);
			JsonArray kpiVisualizationArray = kpiResultArray.get(0).getAsJsonObject().get("visualizationresult").getAsJsonArray();
			log.debug("Workflow Detail ====  kpiVisualizationArray  {} ", kpiVisualizationArray);
			JsonArray fromkpiResultArray = kpiVisualizationArray.get(0).getAsJsonObject().get("KpiResult").getAsJsonArray();
			JsonArray dataArray = fromkpiResultArray.get(0).getAsJsonObject().get("data").getAsJsonArray();
			JsonArray columnArray = fromkpiResultArray.get(0).getAsJsonObject().get("columns").getAsJsonArray();
			
			for (JsonElement elements : dataArray) {
				JsonArray rowArray = elements.getAsJsonObject().get("row").getAsJsonArray();
				String rowValue = StringUtils.join(rowArray, ',');
				rowValueList.add(rowValue.replaceAll("\"", ""));
			}
			
			headerList.add("SNo");
			for (JsonElement elements : columnArray) {
				headerList.add(elements.getAsString().replace("\"", ""));
			}
			
			exportPDFFile(headerList, rowValueList, assessmentReportDTO);
			

		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data {}", e);
			throw new InsightsJobFailedException(" unable to prepare pdf data " + e);
		}
		
	}
	
	private void exportPDFFile(Set<String> headerList, List<String> rowValueList, InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
			String exportedFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.REPORT_TYPE;
			assessmentReportDTO.setPdfExportedFilePath(exportedFilePath);
			PdfTableUtil pdfTableUtil = new PdfTableUtil();
			byte[] pdfResponse = pdfTableUtil.generateCypherReport(headerList, rowValueList, URLEncoder.encode(assessmentReportDTO.getAsseementreportname()+".pdf","UTF-8"));
			File extractedPdfFile = new File(exportedFilePath);
			
			savePDFFile(extractedPdfFile, pdfResponse);

            log.debug("Workflow Detail ==== pdf File Saved {} ", exportedFilePath);
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while exporting pdf ", e);
			throw new InsightsJobFailedException(e.getMessage());
		} 
	
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
