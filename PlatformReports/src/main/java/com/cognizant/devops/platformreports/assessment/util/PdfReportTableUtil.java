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
package com.cognizant.devops.platformreports.assessment.util;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.util.InsightsReportPdfTableConfig;
import com.cognizant.devops.platformreports.assessment.datamodel.InsightsAssessmentConfigurationDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.steadystate.css.parser.CSSOMParser;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;

public class PdfReportTableUtil {

	private static final Logger log = LogManager.getLogger(PdfReportTableUtil.class);

	private PDFont font;
	private static final String DASHBOARD = "Dashboard";
	/**
	 * Create table based on number of rows with observations.
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
	public float createTable(PDDocument doc, PDPage page, int tableIndex,String kpiId, String caption, JsonArray columnData, 
			JsonArray rowData, InsightsAssessmentConfigurationDTO assessmentReportDTO, InsightsReportPdfTableConfig insightsReportPdfTableConfig, float yStart) {
		float margin = 10;
		float bottomMargin = 40;
		float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
		float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
		float tw = tableWidth-150;
		BaseTable dataTable;
		Set<String> headerList = new LinkedHashSet<>();
		try {
			getFont(doc, assessmentReportDTO, insightsReportPdfTableConfig);

			if(tableIndex==0) {
				yStart = yStartNewPage-30;
				dataTable = new BaseTable(yStart,
						yStartNewPage, bottomMargin, tw, margin+60, doc, page, true, true);
			}else {
				dataTable = new BaseTable(yStart,
						yStartNewPage, bottomMargin, tw, margin+60, doc,doc.getPage(doc.getPages().getCount()-1), true, true);
			}
			dataTable.drawTitle(caption, font, Integer.parseInt(insightsReportPdfTableConfig.getTableCaptionFontSize()),
					tableWidth, 15, "left", 1, true);
			dataTable.drawTitle("", font, 15, tableWidth, 10, "left", 1, true);
			dataTable.drawTitle("Observations", font,Integer.parseInt(insightsReportPdfTableConfig.getObservationFontSize()), 
					tableWidth, 10, "left", 1, true);
			Map<String, List<String>> kpiContentMap = assessmentReportDTO.getKpiContentMap();
			log.debug("Worlflow Detail ====  observations  {} ", kpiContentMap);
			if(kpiContentMap.containsKey(kpiId)) {
				List<String> observations = kpiContentMap.get(kpiId);
				for(String observation: observations) {
					dataTable.drawTitle("- "+observation, font, Integer.parseInt(insightsReportPdfTableConfig.getObservationListFontSize()), 
							tableWidth-130, 10, "left", 1, true);
				}
			}
			
			for (JsonElement jsonTableHeaderElement : columnData) {
				headerList.add(jsonTableHeaderElement.getAsString());
			}

			List<String[]> rowList = preparePdfRows(rowData);

			Row<PDPage> headerRow = generateDynamicHeader(dataTable, headerList, insightsReportPdfTableConfig);
			generateDynamicRows(rowList, headerRow, dataTable, insightsReportPdfTableConfig, font);
			
			yStart = dataTable.draw()-100;

		} catch (Exception e) {
			log.error("Worlflow Detail ==== unable to create table ", e);
		}
		return yStart;
	}

	/**
	 * @param doc
	 * @param assessmentReportDTO
	 * @param insightsTableConfig
	 * @throws IOException
	 */
	private void getFont(PDDocument doc, InsightsAssessmentConfigurationDTO assessmentReportDTO,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig) throws IOException {
		File fontFile = new File(assessmentReportDTO.getPdfReportDirPath()+File.separator+insightsReportPdfTableConfig.getFont()+".ttf");
		if(fontFile.exists()) {
			font = PDType0Font.load(doc,fontFile);
			log.debug("fetchPdfConfig ==== Custom font loaded successfully..");
		}else {
			log.info("fetchPdfConfig ==== No font ttf file present..using default.");
			font = PDType1Font.HELVETICA;
		}
	}

	/**
	 * Parser the rgb code to Color .
	 * @param input
	 * @return Color
	 */
	public static Color parse(String input) 
	{
		Pattern c = Pattern.compile(ReportEngineUtils.RGB_PATTERN);
		Matcher m = c.matcher(input);

		if (m.matches()) 
		{
			return new Color(Integer.valueOf(m.group(1)),
					Integer.valueOf(m.group(2)),
					Integer.valueOf(m.group(3)));
		}

		return null;  
	}

	private List<String[]> preparePdfRows(JsonArray rowData) {
		List<String> rowValueList = new ArrayList<>(0);
		List<String[]> rowList = new ArrayList<>(0);
		for (JsonElement jsonTableRowElement : rowData) {
			JsonArray rowValues = jsonTableRowElement.getAsJsonObject().get("row").getAsJsonArray();
			String rowValue = StringUtils.join(rowValues, ',');
			rowValueList.add(rowValue.replace("\"", ""));
		}
		rowValueList.forEach(row -> rowList.add(row.split(",")));

		return rowList;
	}

	/**
	 * Parses style.css file and uses css for table in pdf.
	 * @param cssPath
	 * @param insightsTableConfig
	 */
	public void fetchPdfConfig(String cssPath, InsightsReportPdfTableConfig insightsReportPdfTableConfig) {
		File tableJsonFile = new File(cssPath+File.separator+ReportEngineUtils.STYLE_CSS);
		if(tableJsonFile.exists()) {
			try {
				CSSOMParser cssParser = new CSSOMParser();
				CSSStyleSheet css = cssParser.parseStyleSheet(new InputSource(new FileReader(cssPath+File.separator+ReportEngineUtils.STYLE_CSS)), null, null);
				CSSRuleList cssRules = css.getCssRules();
				for (int i = 0; i < cssRules.getLength(); i++) {
					CSSRule rule = cssRules.item(i);
					if (rule instanceof CSSStyleRule) {
						CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
						getCssProperties(insightsReportPdfTableConfig, rule, style);
					}
				}
			}catch(IOException e) {
				log.error("Worlflow Detail ==== unable to create table config using default properties", e);
			}
		}else {
			log.info("fetchPdfConfig ==== No style.css found..using default properties.");
		}
	}

	private void getCssProperties(InsightsReportPdfTableConfig insightsReportPdfTableConfig, CSSRule rule, CSSStyleDeclaration style) {
		if(".table th".equals(((CSSStyleRule) rule).getSelectorText())) {
			insightsReportPdfTableConfig.setTableHeaderFillColor(style.getPropertyValue(ReportEngineUtils.BACKGROUND_COLOR));
			insightsReportPdfTableConfig.setTableHeaderTextColor(style.getPropertyValue(ReportEngineUtils.COLOR));
			insightsReportPdfTableConfig.setTableHeaderFontSize(style.getPropertyValue(ReportEngineUtils.FONT_SIZE));
			insightsReportPdfTableConfig.setFont(style.getPropertyValue(ReportEngineUtils.FONT_FAMILY));
		}else if(".table td".equals(((CSSStyleRule) rule).getSelectorText())) {
			insightsReportPdfTableConfig.setTableRowsFontSize(style.getPropertyValue(ReportEngineUtils.FONT_SIZE));
		}else if(".table .caption".equals(((CSSStyleRule) rule).getSelectorText())) {
			insightsReportPdfTableConfig.setTableCaptionFontSize(style.getPropertyValue(ReportEngineUtils.FONT_SIZE));
		}else if(".table .observations".equals(((CSSStyleRule) rule).getSelectorText())) {
			insightsReportPdfTableConfig.setObservationFontSize(((CSSStyleRule) rule).getStyle().getPropertyValue(ReportEngineUtils.FONT_SIZE));
		}else if(".table .observations .li".equals(((CSSStyleRule) rule).getSelectorText())) {
			insightsReportPdfTableConfig.setObservationListFontSize(((CSSStyleRule) rule).getStyle().getPropertyValue(ReportEngineUtils.FONT_SIZE));
		}
	}


	private static void generateDynamicRows(List<String[]> assetResults, Row<PDPage> headerRow, BaseTable table, InsightsReportPdfTableConfig insightsReportPdfTableConfig, PDFont font) {
		float columnsplit = headerRow.getColCount();
		for (String[] asset : assetResults) {
			headerRow = table.createRow(10f);
			for (int i = 0; i < asset.length; i++) {
				Cell<PDPage> cell = headerRow.createCell((100 / columnsplit), asset[i]);
				cell.setFont(font);
				cell.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableRowsFontSize()));
				cell.setAlign(HorizontalAlignment.LEFT);
				cell.setValign(VerticalAlignment.MIDDLE);
			}
		}
	}

	private static void cellProps(Cell<PDPage> sno, InsightsReportPdfTableConfig insightsReportPdfTableConfig) {
		sno.setFont(PDType1Font.HELVETICA_BOLD);
		if(insightsReportPdfTableConfig.getTableHeaderFillColor().isEmpty()) {
			sno.setFillColor(Color.GREEN);
		}else {
			sno.setFillColor(parse(insightsReportPdfTableConfig.getTableHeaderFillColor()));
		}
		if(insightsReportPdfTableConfig.getTableHeaderTextColor().isEmpty()) {
			sno.setTextColor(Color.BLACK);
		}else {
			sno.setTextColor(parse(insightsReportPdfTableConfig.getTableHeaderTextColor()));
		}
		sno.setAlign(HorizontalAlignment.LEFT);
		sno.setValign(VerticalAlignment.MIDDLE);
		sno.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableHeaderFontSize()));
	}

	private static Row<PDPage> generateDynamicHeader(BaseTable dataTable, Set<String> headerList, InsightsReportPdfTableConfig insightsReportPdfTableConfig) {
		Row<PDPage> headerRow = dataTable.createRow(15f);
		float totalHeader = headerList.size();
		for(String header : headerList){
			Cell<PDPage> cell = headerRow.createCell((100 / totalHeader), header);
			cellProps(cell, insightsReportPdfTableConfig);
		}
		dataTable.addHeaderRow(headerRow);
		return headerRow;
	}

	public PDPage addNewPage(PDDocument doc, String pdfType) {
		PDPage page = null;
		if(pdfType.equalsIgnoreCase(DASHBOARD)) {
			page = new PDPage(new PDRectangle(968, 900));
		}
		else {
			page = new PDPage(PDRectangle.A4);
		}
		doc.addPage(page);
		return page;
	}
	
	
	

	
	/**
	 * Add page number and date .
	 * @param assessmentReportDTO
	 * @param doc
	 * @param fusionPages
	 * @param insightsTableConfig
	 * @return
	 * @throws IOException
	 */
	public PDDocument footer(InsightsAssessmentConfigurationDTO assessmentReportDTO, PDDocument doc, int fusionPages, InsightsReportPdfTableConfig insightsReportPdfTableConfig, String pdfType) throws IOException {
		try{
			getFont(doc, assessmentReportDTO, insightsReportPdfTableConfig);

			PDPageTree pages = doc.getPages();
			int page = fusionPages + 1;
			for(PDPage p : pages){
				

				PDPageContentStream contentStream = new PDPageContentStream(doc, p, AppendMode.APPEND, false);
				contentStream.beginText();
				contentStream.setNonStrokingColor(0f,0f,0f); 
				contentStream.newLineAtOffset(13, 10);
				contentStream.setFont(font, 10);
				contentStream.endText();
				contentStream.beginText();
				
				if(pdfType.equalsIgnoreCase(DASHBOARD)) {
				contentStream.newLineAtOffset(910, 10);
				}
				else {
					contentStream.newLineAtOffset(530, 10);
				}
				contentStream.setFont(font, 10);
				contentStream.showText(InsightsUtils.getLocalDateTime("MM/dd/yyyy"));
				contentStream.endText();
				
				contentStream.close();
				page++;
			}
		}catch(Exception e){
			log.error("Worlflow Detail ==== error creating footer using openpdf ", e);
		}
		return doc;
	}


}
