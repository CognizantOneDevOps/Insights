/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.traceabilitydashboard.service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;
import com.cognizant.devops.platformcommons.util.InsightsReportPdfTableConfig;
import com.cognizant.devops.platformcommons.util.TraceabilityUtils;
import com.cognizant.devops.platformcommons.constants.AssessmentReportAndWorkflowConstants;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformdal.icon.Icon;
import com.cognizant.devops.platformdal.icon.IconDAL;
import com.cognizant.devops.platformcommons.exception.InsightsJobFailedException;
import com.google.gson.JsonArray;

@Service
public class TraceabilityDashboardPDFServiceImpl {
	// code for traceabilityPDF
	// first set assessmentReportDTO
	private float yStart = 1f;
	private static Logger log = LogManager.getLogger(TraceabilityDashboardPDFServiceImpl.class);

	private static final String DASHBOARD_REPORT_DIR = "dashboardReportTemplate";
	private static final String LOGO_IMAGE_PATH = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator
			+ DASHBOARD_REPORT_DIR + File.separator + "image.webp";
	TraceabilityUtils traceabilityUtils = new TraceabilityUtils();

	public byte[] getTraceabilityPDF(String toolName, String fieldName, List<String> fieldValue, String type,
			String imageString) throws IOException {

		byte[] finalTracePDF = null;

		try {
			InsightsReportPdfTableConfig insightsReportPdfTableConfig = new InsightsReportPdfTableConfig();
			PDDocument doc = new PDDocument();
			String exportedFilePath = AssessmentReportAndWorkflowConstants.REPORT_PDF_RESOLVED_PATH
					+ "Traceability.pdf";
			// Front Page doc
			PDPage page = traceabilityUtils.addNewPage(doc);
			setImageHeader(doc, 1134, true);
			traceabilityUtils.setFooter(doc);
			// Table Content for frontPage
			// Column header
			JsonArray columnDataFrontPage = new JsonArray();
			columnDataFrontPage.add("Filter Name");
			columnDataFrontPage.add("Value");
			// row values
			JsonArray rowDataFrontPage = new JsonArray();
			rowDataFrontPage.add("Tool Name");
			rowDataFrontPage.add(toolName);
			rowDataFrontPage.add("Field Name");
			rowDataFrontPage.add(fieldName);
			rowDataFrontPage.add("Issue");
			rowDataFrontPage.add(fieldValue.get(0));
			rowDataFrontPage.add("Type");
			rowDataFrontPage.add(type);
			// Prepare Table
			byte[] frontPageArray = prepareGrafanaTable(columnDataFrontPage, rowDataFrontPage, 0, doc, page,
					insightsReportPdfTableConfig);
			// Traceability PDF Section
			PDDocument traceDoc = new PDDocument();
			byte[] tracePDF = processTraceabilityImage(traceDoc, imageString);

			// merge pdf's
			finalTracePDF = mergePDFFiles(frontPageArray, tracePDF, exportedFilePath);

		} catch (Exception e) {
			log.error("Traceability PDF === PDF Generation completed with an exception {}", e.getMessage());
		}
		return finalTracePDF;
	}

	/**
	 * Method to merge two PDF files, save in destination folder and returns merged
	 * pdf byte array.
	 * 
	 * @param pdf1
	 * @param pdf2
	 * @param destinationFilePath
	 * @return
	 * @throws Exception
	 */
	private byte[] mergePDFFiles(byte[] pdf1, byte[] pdf2, String destinationFilePath) {
		byte[] finalArray = null;
		try {
			PDFMergerUtility merger = new PDFMergerUtility();
			merger.addSource(new ByteArrayInputStream(pdf1));
			merger.addSource(new ByteArrayInputStream(pdf2));
			merger.setDestinationFileName(destinationFilePath);
			merger.mergeDocuments(null);
			log.debug("Worlflow Detail ==== Pdf files merged successfully ===== ");

			try (FileInputStream inputStream = new FileInputStream(destinationFilePath)) {
				finalArray = IOUtils.toByteArray(inputStream);
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while merging PDF Files  ", e);
			throw new InsightsJobFailedException(e.getMessage());
		}

		return finalArray;
	}

	private byte[] prepareGrafanaTable(JsonArray columnData, JsonArray rowData, int tableIndex, PDDocument doc,
			PDPage page, InsightsReportPdfTableConfig insightsReportPdfTableConfig) {

		yStart = traceabilityUtils.createOpenPdfTableForPanel(doc, page, tableIndex, columnData, rowData,
				insightsReportPdfTableConfig, yStart);
		byte[] frontPageArray = null;

		try {
			frontPageArray = toByteArray(doc);
			doc.close();

		} catch (IOException e) {

			e.getMessage();
		}
		return frontPageArray;
	}

	private byte[] processTraceabilityImage(PDDocument traceDoc, String imageString) throws IOException {

		String[] strings = imageString.split(",");
		// convert base64 string to binary data
		byte[] data = DatatypeConverter.parseBase64Binary(strings[1]);
		// create a PDImageXObject from the binary data
		PDImageXObject image = PDImageXObject.createFromByteArray(traceDoc, data, "traceability");
		int height = image.getHeight();
		int width = image.getWidth();
		PDPageContentStream contentStream;
		PDPage page;
		float x1;
		float y1;
		float totalHeight;
		float aspectRatio = (float) height / width;
		float desiredWidth = 826;
		float desiredHeight = aspectRatio * desiredWidth;
		if (desiredHeight > 1099) {
			// Customized Page
			page = traceabilityUtils.addNewPage2(traceDoc, (float) 2.8346457 * 297, desiredHeight + 91);
			contentStream = new PDPageContentStream(traceDoc, page);
			x1 = 10;
			y1 = 35;
			totalHeight = desiredHeight + 35;
			contentStream.drawImage(image, x1, y1, desiredWidth, desiredHeight);
		} else {
			// A3 size Page
			page = traceabilityUtils.addNewPage(traceDoc);
			contentStream = new PDPageContentStream(traceDoc, page);
			x1 = 10;
			y1 = 1134 - desiredHeight;
			totalHeight = 1134;
			contentStream.drawImage(image, x1, y1, desiredWidth, desiredHeight);
		}
		// close the content stream and save the document
		contentStream.close();
		setImageHeader(traceDoc, totalHeight, false);
		traceabilityUtils.setFooter(traceDoc);
		byte[] pdfArray = toByteArray(traceDoc);
		traceDoc.close();

		return pdfArray;
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

	private PDDocument setImageHeader(PDDocument doc, float height, boolean isFrontPage) {

		long now = Instant.now().toEpochMilli();
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
				contentStream.drawImage(pdImage, 10, height + 5, 135, 40);
				contentStream.beginText();
				contentStream.setNonStrokingColor(0f, 0f, 0f);
				if (isFrontPage) {
					contentStream.newLineAtOffset(310, 990);
					contentStream.setFont(PDType1Font.HELVETICA_BOLD, 25);
					contentStream.showText("TRACEABILITY REPORT");
					contentStream.newLineAtOffset(5, -45);
					contentStream.setFont(PDType1Font.HELVETICA, 15);
					contentStream.showText("Generated on: " + InsightsUtils.specficTimeFormat(now, "MMMM dd yyyy"));
					contentStream.endText();
					contentStream.setStrokingColor(Color.BLUE);
					contentStream.setLineWidth(1.8f);
					contentStream.moveTo(310, 980);
					contentStream.lineTo(
							25 * PDType1Font.HELVETICA_BOLD.getStringWidth("TRACEABILITY REPORT") / 1000 + 310, 980);
					contentStream.stroke();
				} else {
					contentStream.newLineAtOffset(685, height + 20);
					contentStream.setFont(PDType1Font.HELVETICA, 15);
					contentStream.showText("Traceability Report");
					contentStream.endText();
				}
				contentStream.close();
			}

		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while updating logo image in header template  ", e);

		}
		return doc;
	}
}