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
package com.cognizant.devops.platformcommons.util;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;

public class TraceabilityUtils {

	private static Logger log = LogManager.getLogger(TraceabilityUtils.class);
	private PDFont font;
	public static final String RGB_PATTERN = "rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)";

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
	public float createOpenPdfTableForPanel(PDDocument doc, PDPage page, int tableIndex, JsonArray columnData,
			JsonArray rowData, InsightsReportPdfTableConfig insightsReportPdfTableConfig, float yStart) {

		String theme = "light";
		final String alignment = "center";
		float margin = 0;
		float pageTopMargin = 70;
		float bottomMargin = 23;
		float yStartNewPage = page.getMediaBox().getHeight() - 210;
		float tableWidth = page.getMediaBox().getWidth() - 10;
		float tw;

		tw = tableWidth - 10;
		margin = margin + 10;

		BaseTable dataTable;
		Set<String> headerList = new LinkedHashSet<>();
		try {
			getFont();

			if (tableIndex == 0) {
				yStart = yStartNewPage - 60;
				dataTable = new BaseTable(yStart, yStartNewPage, pageTopMargin, bottomMargin, tw, margin, doc, page,
						true, true);
			} else {
				dataTable = new BaseTable(yStart, yStartNewPage, pageTopMargin, bottomMargin, tw, margin, doc,
						doc.getPage(doc.getPages().getCount() - 1), true, true);
			}

			for (JsonElement jsonTableHeaderElement : columnData) {
				headerList.add(jsonTableHeaderElement.getAsString());
			}

			Row<PDPage> headerRow = generateDynamicHeader(dataTable, headerList, insightsReportPdfTableConfig, theme);

			if (preparePdfRows(rowData, dataTable, headerRow, insightsReportPdfTableConfig, font, theme)) {
				yStart = dataTable.draw() - 70;
				dataTable.drawTitle(" ", font, 6, tableWidth, 5, alignment, 1, true);
				dataTable.drawTitle("No Data", font,
						Integer.parseInt(insightsReportPdfTableConfig.getTableCaptionFontSize()), tableWidth, 5,
						alignment, 1, true);
			} else {
				yStart = dataTable.draw() - 70;
			}

		} catch (Exception e) {
			log.error(" Worlflow Detail ==== unable to generate table, {} ", e.getMessage());
		}

		return yStart;
	}

	/**
	 * @param assessmentReportDTO
	 * @param insightsTableConfig
	 * @throws IOException
	 */
	private void getFont() {

		log.info("fetchPdfConfig ==== No font ttf file present..using default.");
		font = PDType1Font.HELVETICA;
	}

	/**
	 * Generate rows
	 * 
	 * @param rowData
	 * @param table
	 * @param headerRow
	 * @param insightsReportPdfTableConfig
	 * @param font
	 * @param theme
	 * @return
	 */
	private boolean preparePdfRows(JsonArray rowData, BaseTable table, Row<PDPage> headerRow,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, PDFont font, String theme) {
		float columnsplit = headerRow.getColCount();

		for (int i = 0; i < rowData.size(); i += 2) {
			JsonArray rowValues = new JsonArray();
			List<String> rowList = new ArrayList<>();
			rowValues.add(rowData.get(i));
			rowValues.add(rowData.get(i + 1));
			headerRow = table.createRow(10f);
			rowValues.forEach(val -> rowList.add(val.toString()));
			for (String rows : rowList) {
				rows = rows.replace("\"", "");
				rows = rows.replace("null", "-");
				rows = rows.replace("[", "");
				rows = rows.replace("]", "");

				Cell<PDPage> cell = headerRow.createCell((100 / columnsplit), rows);
				cell.setFont(font);
				cell.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableRowsFontSize()));
				cell.setAlign(HorizontalAlignment.LEFT);
				cell.setValign(VerticalAlignment.MIDDLE);
				cellPropsTableData(cell, insightsReportPdfTableConfig, theme);

			}
		}

		return rowData.size() == 0;
	}

	/**
	 * generate header
	 * 
	 * @param dataTable
	 * @param headerList
	 * @param insightsReportPdfTableConfig
	 * @param theme
	 * @return
	 */
	private Row<PDPage> generateDynamicHeader(BaseTable dataTable, Set<String> headerList,
			InsightsReportPdfTableConfig insightsReportPdfTableConfig, String theme) {
		Row<PDPage> headerRow = dataTable.createRow(15f);
		float totalHeader = headerList.size();
		for (String header : headerList) {
			Cell<PDPage> cell = headerRow.createCell((100 / totalHeader), header);
			cellProps(cell, insightsReportPdfTableConfig, theme);
		}

		dataTable.addHeaderRow(headerRow);
		return headerRow;
	}

	/**
	 * color fill in header
	 * 
	 * @param sno
	 * @param insightsReportPdfTableConfig
	 * @param theme
	 */
	private void cellProps(Cell<PDPage> sno, InsightsReportPdfTableConfig insightsReportPdfTableConfig, String theme) {

		sno.setFont(PDType1Font.HELVETICA);
		if (insightsReportPdfTableConfig.getTableHeaderFillColor().isEmpty()) {
			if (theme.equals("dark")) {
				Color fillColor = new Color(34, 37, 43);
				sno.setFillColor(fillColor);
			} else {
				Color fillColor = new Color(0, 0, 255);
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
				Color fillColor = new Color(255, 255, 255);
				sno.setTextColor(fillColor);
			}

		} else {
			sno.setTextColor(parse(insightsReportPdfTableConfig.getTableHeaderTextColor()));
		}
		sno.setAlign(HorizontalAlignment.CENTER);
		sno.setValign(VerticalAlignment.MIDDLE);
		sno.setFontSize(Integer.parseInt(insightsReportPdfTableConfig.getTableHeaderFontSize()));
	}

	/**
	 * Parser the rgb code to Color .
	 * 
	 * @param input
	 * @return Color
	 */
	public static Color parse(String input) {
		Pattern c = Pattern.compile(RGB_PATTERN);
		Matcher m = c.matcher(input);

		if (m.matches()) {
			return new Color(Integer.valueOf(m.group(1)), Integer.valueOf(m.group(2)), Integer.valueOf(m.group(3)));
		}

		return null;
	}

	/**
	 * color fill in rows
	 * 
	 * @param sno
	 * @param insightsReportPdfTableConfig
	 * @param theme
	 */
	private void cellPropsTableData(Cell<PDPage> sno, InsightsReportPdfTableConfig insightsReportPdfTableConfig,
			String theme) {

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
	 * Add page with A3 size
	 * 
	 * @param doc
	 * @return
	 */
	public PDPage addNewPage(PDDocument doc) {
		PDPage page = new PDPage(PDRectangle.A3);
		doc.addPage(page);
		return page;
	}

	/**
	 * Add customized page
	 * 
	 * @param doc
	 * @param w
	 * @param h
	 * @return
	 */
	public PDPage addNewPage2(PDDocument doc, float width, float height) {
		PDPage page = null;
		page = new PDPage(new PDRectangle(width, height));
		doc.addPage(page);
		return page;
	}

	/**
	 * Adding footer
	 * 
	 * @param doc
	 */
	public PDDocument setFooter(PDDocument doc) {
		try {
			PDPageTree pages = doc.getPages();

			for (PDPage page : pages) {
				PDPageContentStream contentStream = new PDPageContentStream(doc, page,
						PDPageContentStream.AppendMode.APPEND, true);
				contentStream.setStrokingColor(Color.LIGHT_GRAY);
				contentStream.setLineWidth(0.9f);
				contentStream.setFont(PDType1Font.HELVETICA, 12);
				contentStream.moveTo(0, 25);
				contentStream.lineTo(page.getMediaBox().getWidth(), 25);
				contentStream.stroke();
				contentStream.beginText();
				contentStream.setNonStrokingColor(Color.GRAY);
				contentStream.newLineAtOffset(775, 5);
				contentStream.showText(InsightsUtils.getLocalDateTime("MM/dd/yyyy"));
				contentStream.endText();
				contentStream.close();
			}
		} catch (Exception e) {
			log.error("Workflow Detail ==== Error while updating footer template  ", e);
		}
		return doc;
	}
}
