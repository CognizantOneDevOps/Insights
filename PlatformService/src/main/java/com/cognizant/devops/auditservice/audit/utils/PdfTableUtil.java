package com.cognizant.devops.auditservice.audit.utils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.VerticalAlignment;
import be.quodlibet.boxable.utils.FontUtils;
import be.quodlibet.boxable.utils.ImageUtils;
import be.quodlibet.boxable.utils.PDStreamUtils;
import com.cognizant.devops.platformcommons.constants.ConfigOptions;
/**
 * Pdf Table creation with Boxable API.
 * Function consists to write , protect and digitally sign the Doc.
 *
 */
public class PdfTableUtil {

	private static final Logger log = LogManager.getLogger(PdfTableUtil.class.getName());
	
	// Owner password (to open the file with all permissions) is "12345"
	private static final String OWNER_PWD = "12345";
	// User password (to open the file but with restricted permissions, is empty here)
	private static final String USER_PWD = "12345";
	private static final boolean ALLOW_PRINTING = false;
	private static final String PDF_PATH = System.getenv().get("INSIGHTS_HOME") + File.separator + ConfigOptions.CONFIG_DIR + File.separator + "Pdf" + File.separator;

	/**
	 * Define Table props and write its content in dynamic fashion.
	 * @param pdfName 
	 * @param assetsResults
	 * @param doc 
	 * @return 
	 * @throws IOException
	 * @throws JAXBException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws UnrecoverableKeyException 
	 * @throws URISyntaxException 
	 */
	public byte[] generateTableContent(List<Map> assetList, String pdfName) throws IOException, JAXBException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException{
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

		PdfTableUtil pdfTableUtil = new PdfTableUtil();
		PDDocument doc = pdfTableUtil.generateStaticContent();

		List<String[]> assetResults = formatAsset(assetList);

		// Set margins
		float margin = 10;
		// Initialize Document
		//PDDocument doc = new PDDocument();
		PDPage page = addNewPage(doc);
		float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
		log.info("Start of new Page - "+yStartNewPage);
		// Initialize table
		float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
		boolean drawContent = true;
		float ystaticStart = yStartNewPage-30;
		float yStart = yStartNewPage-120;
		float bottomMargin = 70;

		generatePageStaticContent(doc, margin, page, yStartNewPage, tableWidth, drawContent, ystaticStart,
				bottomMargin);


		BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
				drawContent);

		//Header
		Row<PDPage> headerRow = generateHeader(table);


		//Table rows
		generateDynamicRows(assetResults, headerRow, table, page, null);
		doc.addPage(new PDPage());//For Signature purpose
		footer(doc, pdfName);
		protectPdf(doc, pdfName);
		
		PdfSignUtil pdfSignUtil = new PdfSignUtil();
		return pdfSignUtil.digitalSign(doc, pdfName);

		//doc.save(file);
		//doc.close();


	}

	private void generatePageStaticContent(PDDocument doc, float margin, PDPage page, float yStartNewPage,
			float tableWidth, boolean drawContent, float ystaticStart, float bottomMargin) throws IOException, JAXBException {
		try{
		//ClassLoader classLoader = getClass().getClassLoader();
		//System.out.println("classLoader"+classLoader);
		//File staticFile = new File(classLoader.getResource("static/static.xml").getFile());
		File staticFile = new File(PDF_PATH+"static/static.xml").getAbsoluteFile();
		JAXBContext jaxbContext = JAXBContext.newInstance(PdfStaticContent.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		PdfStaticContent pdfStaticContent = (PdfStaticContent) jaxbUnmarshaller.unmarshal(staticFile);
		
		Calendar cal = Calendar.getInstance();
		Date caldate=cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		String formattedDate=dateFormat.format(caldate);
		
		PDPageContentStream cos = new PDPageContentStream(doc, page);
		PDStreamUtils.rect(cos, 10, ystaticStart-65, page.getMediaBox().getWidth()-20, 1, Color.blue);
		cos.close();
		
		BaseTable statictable = new BaseTable(ystaticStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true,
				drawContent);
		Row<PDPage> row = statictable.createRow(10f);
		Cell<PDPage> cell = row.createImageCell(25f, ImageUtils.readImage(new File(PDF_PATH+"sign/Insights.png").getAbsoluteFile()));
		cell = row.createCell(10f, "");
		cell = row.createCell(50f, pdfStaticContent.getTitle());
		cell.setFont(PDType1Font.HELVETICA);
		cell.setFontSize(20);
		cell.setWidth(ystaticStart);
		cell.setAlign(HorizontalAlignment.LEFT);
		cell.setValign(VerticalAlignment.MIDDLE);
		statictable.addHeaderRow(row);
		
		Row<PDPage> additionArow = statictable.createRow(15f);
		cell = additionArow.createCell(40f, "Insights "+pdfStaticContent.getVersion());
		cell = additionArow.createCell(20f, "");
		cell = additionArow.createCell(40f, "Generated on : "+formattedDate);
		cell.setAlign(HorizontalAlignment.CENTER);
		cell.setValign(VerticalAlignment.MIDDLE);
		statictable.removeAllBorders(true);
		statictable.draw();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Format and sort asset details based on timestamp.
	 * @param assetList
	 * @return
	 */
	private List<String[]> formatAsset(List<Map> assetList) {
		List<String[]> assetResults = new ArrayList<String[]>();
		//for(Map asset:assetList){
		for(int i=0;i<assetList.size();i++){
			String[] assetArray = new String[10];
			//assetArray[0] = String.valueOf(i+1);
			assetArray[0] = checkEmpty(String.valueOf(assetList.get(i).get("assetID")));
			assetArray[1] = checkEmpty(String.valueOf(assetList.get(i).get("toolName")));
			assetArray[2] = checkEmpty(String.valueOf(assetList.get(i).get("author")));
			assetArray[3] = checkEmpty(String.valueOf(assetList.get(i).get("phase")));
			assetArray[4] = checkEmpty(String.valueOf(assetList.get(i).get("toolstatus")));
			assetArray[5] = checkEmpty(String.valueOf(assetList.get(i).get("timestamp")));
			assetArray[6] = checkEmpty(String.valueOf(assetList.get(i).get("environment")));
			assetArray[7] = checkEmpty(String.valueOf(assetList.get(i).get("author")));
			assetArray[8] = checkEmpty(String.valueOf(assetList.get(i).get("lastUpdatedTime")));
			assetArray[9] = checkEmpty(String.valueOf(assetList.get(i).get("evidence")));
			assetResults.add(assetArray);
		}

		Collections.sort(assetResults, new Comparator<String[]>() {
			@Override
			public int compare(String[] item1, String[] item2) {

				String dateString1 = item1[5];
				String dateString2 = item2[5];

				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				Date date1 = null;
				Date date2 = null;

				if(dateString1 == "NA")
					dateString1 = "00/00/0000 00:00:00";
				if(dateString2 =="NA")
					dateString2 = "00/00/0000 00:00:00";
				try {
					date1 = format.parse(dateString1);
					date2 = format.parse(dateString2);
				} catch (ParseException e) {
					e.printStackTrace();
					return 0;
				}

				// dateString1 is an earlier date than dateString2
				return date2.compareTo(date1);
			}
		});
		//assetResults.sort(Comparator.comparing((String[] o) -> o[5]).reversed());
		return assetResults;
	}


	/**
	 * Generate Header with defined width considering total width as 100.
	 * @param table
	 * @return header row
	 */
	private Row<PDPage> generateHeader(BaseTable table) {
		Row<PDPage> headerRow = table.createRow(15f);
		Cell<PDPage> cell = headerRow.createCell((100 / 11f), "SNo");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Asset");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Tool");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Actor");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Phase");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Status");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "TimeStamp");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Env");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Updated By");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Last Updated Time");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Evidence");
		cellProps(cell);
		/*cell = headerRow.createCell((100 / 11f), "Ledger Transaction Id");
		cellProps(cell);
		cell = headerRow.createCell((100 / 11f), "Blocks");
		cellProps(cell);*/

		table.addHeaderRow(headerRow);

		return headerRow;
	}

	private static void cellProps(Cell<PDPage> sno) {
		sno.setFont(PDType1Font.HELVETICA_BOLD);
		sno.setFillColor(Color.BLUE);
		sno.setTextColor(Color.WHITE);
		sno.setAlign(HorizontalAlignment.LEFT);
		sno.setValign(VerticalAlignment.MIDDLE);
	}

	private static PDPage addNewPage(PDDocument doc) throws IOException {
		PDPage page = new PDPage();
		doc.addPage(page);
		return page;
	}

	private String checkEmpty(String value) {
		if(value == null || "".equalsIgnoreCase(value) || value == "null"){
			value = "NA";
		}
		return value;
	}

	/**
	 * Add multiple rows with random assests.
	 * @param assetsResults
	 * @param headerRow 
	 * @param table
	 * @param page 
	 * @param parentAsset 
	 * @throws IOException
	 */
	private void generateDynamicRows(List<String[]> assetResults, Row<PDPage> headerRow, BaseTable table, PDPage page, String parentAsset) throws IOException {
		Cell<PDPage> cell;
		int count = assetResults.size();
		float columnsplit = headerRow.getColCount();
		int localcount = 1;
		for (String[] asset : assetResults) {
			headerRow = table.createRow(10f);
			for (int i = 0; i <= asset.length; i++) {
				if(i==0 && localcount<=count){
					cell = headerRow.createCell((100 / columnsplit), String.valueOf(localcount));
					cell.setFont(PDType1Font.HELVETICA);
					cell.setFontSize(7);
					cell.setAlign(HorizontalAlignment.LEFT);
					cell.setValign(VerticalAlignment.MIDDLE);
					localcount++;
				}else{
					if(parentAsset!=null && asset[i-1].equals(parentAsset)){
						cell = headerRow.createCell((100 / columnsplit), asset[i-1]);
						cell.setFont(PDType1Font.HELVETICA);
						cell.setFontSize(7);
						cell.setAlign(HorizontalAlignment.LEFT);
						cell.setValign(VerticalAlignment.MIDDLE);
						cell.setFillColor(Color.GREEN);
						cell.setTextColor(Color.BLACK);
					}else{
						cell = headerRow.createCell((100 / columnsplit), asset[i-1]);
						cell.setFont(PDType1Font.HELVETICA);
						cell.setFontSize(7);
						cell.setAlign(HorizontalAlignment.LEFT);
						cell.setValign(VerticalAlignment.MIDDLE);
					}
				}
			}
		}
		table.draw();
	}

	/**
	 * Defines the length of the encryption key.
	 * Possible values are 40, 128 or 256.
	 * 256-bit AES encryption requires a JDK with â€œunlimited strengthâ€� cryptography, which requires extra files to be installed.
	 * @param doc 
	 * @param doc
	 * @return 
	 * @throws IOException
	 */
	public void protectPdf(PDDocument doc, String pdfName) throws IOException {
		//PDDocument doc = PDDocument.load(new File(pdfName==null ? TARGET_PDF : pdfName));
		int keyLength = 128;
		AccessPermission ap = new AccessPermission();
		// disable printing, everything else is allowed
		ap.setCanPrint(ALLOW_PRINTING);
		StandardProtectionPolicy spp = new StandardProtectionPolicy(PdfTableUtil.OWNER_PWD, PdfTableUtil.USER_PWD, ap);
		spp.setEncryptionKeyLength(keyLength);
		spp.setPermissions(ap);
		doc.protect(spp);
		//doc.save(new File(pdfName == null ? TARGET_PDF : pdfName));
		//doc.close();
	}

	/**
	 * Add a footer text to all pages.
	 * @param doc
	 * @param pdfName 
	 * @return
	 * @throws IOException
	 */
	private PDDocument footer(PDDocument doc, String pdfName) throws IOException {
		//File file = null;
		try{
			//file = new File(pdfName == null ? TARGET_PDF : pdfName);
			//System.out.println("Sample file saved at : " + file.getAbsolutePath());
			//Files.createParentDirs(file);
			PDPageTree pages = doc.getPages();
			//int i=1;
			for(PDPage p : pages){
				PDPageContentStream contentStream = new PDPageContentStream(doc, p, AppendMode.APPEND, false);
				//String text = FOOTER_TEXT;
				contentStream.beginText();
				contentStream.newLineAtOffset(200, 780);
				contentStream.setFont(PDType1Font.HELVETICA, 7);
				contentStream.showText("OneDevOps Insights – Software Traceability Report");
				contentStream.endText();
				contentStream.beginText();
				contentStream.newLineAtOffset(120, 15);
				contentStream.setFont(PDType1Font.HELVETICA, 7);
				contentStream.showText("This is an electronically generated report. The report has been digitally signed by the generator.");
				contentStream.endText();
				contentStream.beginText();
				contentStream.newLineAtOffset(230, 5);
				contentStream.setFont(PDType1Font.HELVETICA, 7);
				contentStream.showText("PROPRIETARY & CONFIDENTIAL");
				contentStream.endText();
				contentStream.close();
				//i++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * Generate Seperate Pdf Doc for static content.
	 * @param staticContent
	 * @return 
	 * @throws IOException 
	 * @throws JAXBException 
	 * @throws URISyntaxException 
	 */
	public PDDocument generateStaticContent() throws IOException, JAXBException {
		// Set margins
		float margin = 10;

		// Initialize Document
		PDDocument doc = new PDDocument();
		//Creating the PDDocumentInformation object 
		PDDocumentInformation pdd = doc.getDocumentInformation();
		//Setting the author of the document
		pdd.setAuthor("Insights");
		// Setting the title of the document
		pdd.setTitle("Traceability Audit Report"); 
		//Setting the creator of the document 
		pdd.setCreator("Insights"); 
		//Setting the subject of the document 
		pdd.setSubject("Audit records"); 
		//Setting the created date of the document 
		Calendar date = new GregorianCalendar();
		//date.set(2015, 11, 5); 
		pdd.setCreationDate(date);
		//Setting the modified date of the document 
		//date.set(2016, 6, 5); 
		//pdd.setModificationDate(date); 

		//Setting keywords for the document 
		pdd.setKeywords("Audit, Report, HL");
		PDPage page = addNewPage(doc);

		// Initialize table
		float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
		float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
		boolean drawContent = true;
		boolean drawLines = true;
		float yStart = yStartNewPage;
		float bottomMargin = 70;

		//ClassLoader classLoader = getClass().getClassLoader();
		//File staticFile = new File(classLoader.getResource("static/static.xml").getFile());
		File staticFile = new File(PDF_PATH+"static/static.xml").getAbsoluteFile();
		JAXBContext jaxbContext = JAXBContext.newInstance(PdfStaticContent.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		PdfStaticContent pdfStaticContent = (PdfStaticContent) jaxbUnmarshaller.unmarshal(staticFile);
		log.info("PdfStaticContent - "+pdfStaticContent);
		yStart = yStart -100;
		// draw page title
		PDPageContentStream cos = new PDPageContentStream(doc, page);
		PDStreamUtils.write(cos, pdfStaticContent.getTitle(), PDType1Font.HELVETICA, 30, 80, yStart,
				Color.BLACK);
		PDStreamUtils.rect(cos, 80, yStart-35, page.getMediaBox().getWidth()-230, 1, Color.blue);

		cos.close();

		yStart -= FontUtils.getHeight(PDType1Font.HELVETICA, 14) + 15;

		BaseTable table = new BaseTable(yStart, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, drawLines,
				drawContent);

		Calendar cal = Calendar.getInstance();
		Date caldate=cal.getTime();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		String formattedDate=dateFormat.format(caldate);
		log.info("Current time of the day using Calendar - 24 hour format: "+ formattedDate);


		Row<PDPage> titlerow = table.createRow(50f);
		Cell<PDPage> cell = titlerow.createCell(20f,"");
		cell = titlerow.createCell(60f, "");
		cell = titlerow.createCell(20f,"");
		cell.setFont(PDType1Font.HELVETICA_BOLD);
		cell.setFontSize(50);
		cell.setTextColor(Color.BLACK);
		table.addHeaderRow(titlerow);
		table.createRow(25f);

		Row<PDPage> versionrow = table.createRow(20f);
		Cell<PDPage> versioncell = versionrow.createCell(40f,"");
		versioncell = versionrow.createCell(20f, pdfStaticContent.getVersion());
		versioncell = versionrow.createCell(40f,"");
		versioncell.setFont(PDType1Font.HELVETICA_BOLD);
		versioncell.setFontSize(80);
		//table.createRow(15f);

		Row<PDPage> daterow = table.createRow(20f);
		Cell<PDPage> datecell = daterow.createCell(35f,"");
		datecell = daterow.createCell(35f, "Date: "+new Date().toString());
		datecell = daterow.createCell(30f,"");
		datecell.setFont(PDType1Font.HELVETICA_BOLD);
		datecell.setFontSize(80);
		//table.createRow(15f);

		Row<PDPage> timerow = table.createRow(20f);
		Cell<PDPage> timecell = timerow.createCell(40f,"");
		timecell = timerow.createCell(20f, "Time: "+formattedDate);
		timecell = timerow.createCell(40f,"");
		timecell.setFont(PDType1Font.HELVETICA_BOLD);
		timecell.setFontSize(80);
		table.createRow(380f);

		Row<PDPage> contentrow = table.createRow(35f);
		Cell<PDPage> contentcell = contentrow.createCell(100f, pdfStaticContent.getBody());
		contentcell.setFont(PDType1Font.HELVETICA);
		contentcell.setFontSize(10);
		table.removeAllBorders(true);
		table.draw();

		// Save the document
		//File file = new File(STATIC_PDF);
		//System.out.println("Sample file saved at : " + file.getAbsolutePath());
		//Files.createParentDirs(file);
		//doc.save(file);
		//doc.close();
		return doc;
	}

	/**
	 * 
	 * @param headerList
	 * @param rowValueList
	 * @return 
	 */
	public byte[] generateCypherReport(Set<String> headerList, List<String> rowValueList,String pdfName) {
		log.info("--generateCypherReport for -- " + pdfName);
		byte[] response = null;
		try{
			PdfTableUtil pdfTableUtil = new PdfTableUtil();
			PDDocument doc = pdfTableUtil.generateStaticContent();
			float margin = 10;
			PDPage page = addNewPage(doc);
			float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
			log.info("Start of new Page - "+yStartNewPage);
			float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
			boolean drawContent = true;
			float yStart = yStartNewPage-120;
			float ystaticStart = yStartNewPage-30;
			float bottomMargin = 70;
			
			pdfTableUtil.generatePageStaticContent(doc, margin, page, yStartNewPage, tableWidth, 
					drawContent, ystaticStart, bottomMargin);
			
			/*List<List> data = new ArrayList();
			data.add(new ArrayList<>(headerList));
			for (int i = 0; i < rowValueList.size(); i++) {
				data.add(new ArrayList<>(
						Arrays.asList(rowValueList.get(i).split(","))));
			}*/
			
			BaseTable dataTable = new BaseTable(yStart,
					yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, drawContent);
			
			//Header
			Row<PDPage> headerRow = generateDynamicHeader(dataTable, headerList);
			List<String[]> rowList = new ArrayList<String[]>();
			for(String value: rowValueList){
				String[] row = value.split(",");
				log.info(value);
				rowList.add(row);
			}
			log.info("Total Rows -- "+rowList.size());

			//Table rows
			generateDynamicRows(rowList, headerRow, dataTable, page, null);
			/*dataTable.draw();
			DataTable t = new DataTable(dataTable, page);
			t.addListToTable(data, DataTable.HASHEADER);
			dataTable.draw();*/
			//doc.addPage(new PDPage());//For Signature purpose
			log.info("Rows creation successfull--");
			addNewPage(doc);
			footer(doc, pdfName);
			protectPdf(doc, pdfName);
			log.info("Footer and protection is done!!--");
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//doc.save(baos);
			PdfSignUtil pdfSignUtil = new PdfSignUtil();
			response = pdfSignUtil.digitalSign(doc, pdfName);
			log.info("Signed successfully!!--");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return response;
		
	}

	private Row<PDPage> generateDynamicHeader(BaseTable dataTable, Set<String> headerList) {
		Row<PDPage> headerRow = dataTable.createRow(15f);
		float totalHeader = headerList.size();
		for(String header : headerList){
			Cell<PDPage> cell = headerRow.createCell((100 / totalHeader), header);
			cellProps(cell);
		}
		dataTable.addHeaderRow(headerRow);
		return headerRow;
	}

	public byte[] generateLedgerReport(List<Map> ledgerMap, String pdfName) {
		log.info("--generateLedgerReport for -- " + pdfName);
		byte[] response = null;
		try{
			PdfTableUtil pdfTableUtil = new PdfTableUtil();
			PDDocument doc = pdfTableUtil.generateStaticContent();
			float margin = 10;
			PDPage page = addNewPage(doc);
			float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
			log.info("Start of new page - "+yStartNewPage);
			float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
			boolean drawContent = true;
			float ystaticStart = yStartNewPage-30;
			float bottomMargin = 70;
			
			pdfTableUtil.generatePageStaticContent(doc, margin, page, yStartNewPage, tableWidth, 
					drawContent, ystaticStart, bottomMargin);
			
			log.info("ledgerMap ---"+ledgerMap);
			log.info("size of map ---"+ledgerMap.size());
			String parentAsset = "NA";
			for(int i=0;i<ledgerMap.size();i++){
				
				if(i>0){
					PDPage page1 = addNewPage(doc);
					BaseTable dataTable = new BaseTable(page1.getMediaBox().getHeight() - (4 * margin),
									yStartNewPage, bottomMargin, tableWidth, margin, doc, page1, true, drawContent);
					List<Map> assetList = (List<Map>) ledgerMap.get(i).get("data");
					if(assetList.size()>0){
						parentAsset = checkEmpty(String.valueOf(assetList.get(0).get("parentAsset")));
						log.info("parentAsset--"+parentAsset);
					}
					addText(ystaticStart, parentAsset, dataTable);
					List<String[]> assetResults = formatAsset(assetList);
					Row<PDPage> headerRow = generateHeader(dataTable);
					generateDynamicRows(assetResults, headerRow, dataTable, page1, parentAsset);
				}else{
					BaseTable dataTable = new BaseTable(page.getMediaBox().getHeight() - (4 * margin)-120,
									yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, drawContent);
					List<Map> assetList = (List<Map>) ledgerMap.get(i).get("data");
					if(assetList.size()>0){
						parentAsset = checkEmpty(String.valueOf(assetList.get(0).get("parentAsset")));
						log.info("parentAsset--"+parentAsset);
					}
					addText(ystaticStart, parentAsset, dataTable);
					List<String[]> assetResults = formatAsset(assetList);
					Row<PDPage> headerRow = generateHeader(dataTable);
					generateDynamicRows(assetResults, headerRow, dataTable, page, parentAsset);
				}
				
			}
			log.info("Rows creation successfull--");
			addNewPage(doc);
			footer(doc, pdfName);
			protectPdf(doc, pdfName);
			log.info("Footer and protection is done!!--");
			PdfSignUtil pdfSignUtil = new PdfSignUtil();
			response = pdfSignUtil.digitalSign(doc, pdfName);
			log.info("Signed successfully!!--");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return response;
	}

	/**
	 * Helps adding a row with a designated text.
	 * @param ystaticStart
	 * @param text
	 * @param dataTable
	 */
	private void addText(float ystaticStart, String text, BaseTable dataTable) {
		Row<PDPage> row = dataTable.createRow(10f);
		Cell<PDPage> cell = row.createCell(100f, "History for the Asset - "+text);
		cell.setFont(PDType1Font.HELVETICA);
		cell.setFontSize(10);
		cell.setAlign(HorizontalAlignment.LEFT);
		cell.setValign(VerticalAlignment.MIDDLE);
		cell.setFillColor(Color.BLACK);
		cell.setTextColor(Color.WHITE);
		dataTable.addHeaderRow(row);
	}


}
