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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformauditing.util.PdfTableUtil;
import com.cognizant.devops.platformcommons.core.enums.WorkflowTaskEnum;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.workflow.WorkflowDAL;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.cognizant.devops.platformreports.assessment.util.ReportEngineUtils;
import com.cognizant.devops.platformreports.exception.InsightsJobFailedException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LedgerPDFChartHandler implements BasePDFProcessor {
	
	private static Logger log = LogManager.getLogger(LedgerPDFChartHandler.class);
	
	private WorkflowDAL workflowDAL = new WorkflowDAL();
	
	String incomingTaskMessage;

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
			String exportedFilePath = assessmentReportDTO.getPdfReportDirPath() + File.separator
					+ assessmentReportDTO.getAsseementreportname() + "." + ReportEngineUtils.REPORT_TYPE;
			assessmentReportDTO.setPdfExportedFilePath(exportedFilePath);
			PdfTableUtil pdfTableUtil = new PdfTableUtil();
			byte[] pdfResponse = pdfTableUtil.generateLedgerReport(assessmentReportDTO.getLedgerRecords(), assessmentReportDTO.getAsseementreportname()+".pdf");
			File extractedPdfFile = new File(exportedFilePath);
			savePDFFile(extractedPdfFile, pdfResponse);
			saveToVisualizationContaner(assessmentReportDTO);
		} catch (Exception e) {
			log.error("Workflow Detail ==== error while processing pdf data {}", e);
			throw new InsightsJobFailedException(" unable to prepare pdf data " + e);
		}
		
	}
	
	private void saveToVisualizationContaner(InsightsAssessmentConfigurationDTO assessmentReportDTO) {
		try {
            InsightsReportVisualizationContainer emailHistoryConfig = new InsightsReportVisualizationContainer();
            emailHistoryConfig.setAttachmentPath(assessmentReportDTO.getPdfExportedFilePath());
            emailHistoryConfig.setExecutionId(assessmentReportDTO.getExecutionId());
            JsonObject incomingTaskMessageJson = new JsonParser().parse(assessmentReportDTO.getIncomingTaskMessageJson()).getAsJsonObject();
            if (incomingTaskMessageJson.get("nextTaskId").getAsInt() == -1) {
                emailHistoryConfig.setStatus(WorkflowTaskEnum.WorkflowStatus.COMPLETED.name());
                emailHistoryConfig.setExecutionTime(InsightsUtils.getCurrentTimeInEpochMilliSeconds());
            } else {
                emailHistoryConfig.setStatus(WorkflowTaskEnum.EmailStatus.NOT_STARTED.name());
            }
            emailHistoryConfig.setWorkflowConfig(assessmentReportDTO.getWorkflowId());
            workflowDAL.saveEmailExecutionHistory(emailHistoryConfig);
        } catch (Exception e) {
            log.error("Worlflow Detail ==== Error setting PDF details in Email History table");
            throw new InsightsJobFailedException(
                    "Worlflow Detail ==== Error setting PDF details in Email History table");
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
