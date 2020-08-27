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

/*
 * import com.fusioncharts.fusionexport.client.ExportConfig;
 * import com.fusioncharts.fusionexport.client.ExportManager;
 */

public class ExportPDFWithAPI {
//	private static Logger log = LogManager.getLogger(RelationshipMain.class);
//	public static final String INSIGHTS_HOME = "INSIGHTS_HOME";
//	public static final String CONFIG_DIR = "assessmentReportPdfTemplate";
//	public static final String INSIGHTS_CONFIG_DIR = ".InSights";
//	public static final String WORKFLOW_PDF_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + File.separator
//			+ CONFIG_DIR + File.separator;
//	public static final String REPORT_PDF_EXECUTION_RESOLVED_PATH = System.getenv().get(INSIGHTS_HOME) + File.separator
//			+ CONFIG_DIR + File.separator + "executionsDetail" + File.separator;
//	  public static void main(String[] args) throws Exception {
//		/*   String configPath = "resources/Executive.json";
//		   String template = "resources/Executive.html";
//		
//		   ExportConfig config = new ExportConfig();
//		   config.set("chartConfig", configPath);
//		   config.set("templateFilePath", template);
//		   config.set("dashboardLogo", "resources/image.webp");
//		   config.set("dashboardHeading", "Executive Dashboard");
//		   config.set("type", "pdf");
//		ExportManager exportManager = new ExportManager();
//		exportManager.setHostAndPort("fusionscripts.cogdevops.com", 1337);
//		exportManager.export(config, ".", true);*/
//
//		//long epochtime = InsightsUtils.getEpochTime("2020-03-20 04:14:51 UTC", "YYYY-MM-DD HH:MM:SS Z"); //timeFieldValue dateFormat
//		//log.debug("message epochtime  {} ", epochtime);
//		callDashboardPost();
//	}
//
//	private static void callDashboardPost() {
//		try {
//			String url = "http://localhost:1337/api/v2.0/export";
//			JsonObject requestconfigJson = new JsonObject();
//			Map<String, String> headers = new HashMap<>();
//
//			String configPath = "Executive.json";
//			String template = "Executive.html";
//
//
//
//			//File payload_file = new File(payload_zip_path); 
//
//			//requestconfigJson.addProperty("payload", payload_zip_path);
//			requestconfigJson.addProperty("chartConfig", configPath);
//			requestconfigJson.addProperty("templateFilePath", template);
//			requestconfigJson.addProperty("dashboardLogo", "resources/image.webp");
//			requestconfigJson.addProperty("dashboardHeading", "Executive Dashboard");
//			requestconfigJson.addProperty("type", "pdf");
//
//			log.debug(" requestconfigJson {} ", requestconfigJson);
//
//			//RestApiHandler.doPost(url, requestconfigJson, headers);
//			sendfileWithData();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void sendfileWithData() {
//		try {
//			String url = "http://localhost:1337/api/v2.0/export";
//			String payload_zip_path = WORKFLOW_PDF_RESOLVED_PATH + "payload.zip";
//			String type = "html"; // pdf
//
//			String template = WORKFLOW_PDF_RESOLVED_PATH + "/payload/" + "Executive.html";
//
//			String chatrsData = "[{\"type\": \"mscolumn3d\",\"renderAt\": \"column_chart1\",\"width\": \"700\",\"height\": \"350\",\"dataFormat\": \"json\",\"dataSource\": {\"chart\": { 		\"bgColor\": \"black\",    \"caption\": \"BTG PortFolios\",    \"subCaption\": \"Story points committed VS completed\",    \"theme\": \"fusion\"},\"categories\": [    {   \"category\": [  { \"label\": \"BTG\"  },  { \"label\": \"Consumer\"  },  { \"label\": \"FEIT\"  },  { \"label\": \"NewsDigital\"  }   ]    }],\"dataset\": [    {   \"seriesname\": \"CommittedStoryPoints\",   \"data\": [  { \"value\": \"10000\"  },  { \"value\": \"11500\"  },  { \"value\": \"12500\"  },  { \"value\": \"15000\"  }   ]    },    {   \"seriesname\": \"CompletedStoryPoints\",   \"data\": [  { \"value\": \"25400\"  },  { \"value\": \"29800\"  },  { \"value\": \"21800\"  },  { \"value\": \"26800\"  }   ]    }] } }, { \"type\": \"mscolumn3d\",\"renderAt\": \"column_chart2\",\"width\": \"700\",\"height\": \"350\",\"dataFormat\": \"json\",\"dataSource\": {\"chart\": { 		\"bgColor\": \"black\",    \"caption\": \"BTG Product/Applications\",    \"subCaption\": \"Story points committed VS completed\",    \"theme\": \"fusion\"},\"categories\": [    {   \"category\": [  { \"label\": \"CMS\"  },  { \"label\": \"Counter\"  },  { \"label\": \"MAM\"  },  { \"label\": \"NBCNews\"  },  { \"label\": \"Score\"  },  { \"label\": \"Today\"  } 				   ]    }],\"dataset\": [    {   \"seriesname\": \"CommittedStoryPoints\",   \"data\": [  { \"value\": \"10000\"  },  { \"value\": \"11500\"  },  { \"value\": \"12500\"  },  { \"value\": \"15000\"  },  { \"value\": \"12000\"  },  { \"value\": \"1000\"  }   ]    },    {   \"seriesname\": \"CompletedStoryPoints\",   \"data\": [  { \"value\": \"25400\"  },  { \"value\": \"29800\"  },  { \"value\": \"21800\"  },  { \"value\": \"26800\"  },  { \"value\": \"21800\"  },  { \"value\": \"20800\"  }   ]    }] } }, { \"type\": \"mscolumn3d\",\"renderAt\": \"column_chart3\",\"width\": \"700\",\"height\": \"350\",\"dataFormat\": \"json\",\"dataSource\": {\"chart\": { 	\"bgColor\": \"black\",    \"caption\": \"BTG- TV System's Sprint\",    \"subCaption\": \"Story points committed VS completed\",    \"theme\": \"fusion\"},\"categories\": [    {   \"category\": [  { \"label\": \"S100\"  },  { \"label\": \"S101\"  },  { \"label\": \"S102\"  },  { \"label\": \"S103\"  },  { \"label\": \"S104\"  },  { \"label\": \"S105\"  } 				   ]    }],\"dataset\": [    {   \"seriesname\": \"CommittedStoryPoints\",   \"data\": [  { \"value\": \"1000\"  },  { \"value\": \"1150\"  },  { \"value\": \"200\"  },  { \"value\": \"100\"  },  { \"value\": \"120\"  },  { \"value\": \"1000\"  }   ]    },    {   \"seriesname\": \"CompletedStoryPoints\",   \"data\": [  { \"value\": \"540\"  },  { \"value\": \"980\"  },  { \"value\": \"210\"  },  { \"value\": \"260\"  },  { \"value\": \"180\"  },  { \"value\": \"1080\"  }   ]    }] } }, { \"type\": \"mscolumn3d\",\"renderAt\": \"column_chart4\",\"width\": \"700\",\"height\": \"350\",\"dataFormat\": \"json\",\"dataSource\": {\"chart\": { 	\"bgColor\": \"black\",    \"caption\": \"BTG TV\",    \"subCaption\": \"Story points committed VS completed\",    \"theme\": \"fusion\"},\"categories\": [    {   \"category\": [  { \"label\": \"BackendTeam\"  },  { \"label\": \"MobileTeam\"  },  { \"label\": \"OnlineTeam\"  },  { \"label\": \"WebTeam\"  } 				   ]    }],\"dataset\": [    {   \"seriesname\": \"CommittedStoryPoints\",   \"data\": [  { \"value\": \"10000\"  },  { \"value\": \"11500\"  },  { \"value\": \"12500\"  },  { \"value\": \"15000\"  }   ]    },    {   \"seriesname\": \"CompletedStoryPoints\",   \"data\": [  { \"value\": \"25400\"  },  { \"value\": \"29800\"  },  { \"value\": \"21800\"  },  { \"value\": \"26800\"  }   ]    }] } } ]";
//
//			log.debug("payload_zip_path {} ", payload_zip_path);
//
//			//final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
//
//			File input = new File(template);//"resources/Executive.html"
//			Document doc = Jsoup.parse(input, "UTF-8", template); //, "http://example.com/"
//
//			Element content = doc.getElementById("content");
//			/*Elements links = content.getElementsByTag("a");
//			for (Element link : links) {
//				String linkHref = link.attr("href");
//				String linkText = link.text();
//			}*/
//
//			Elements contentClass = doc.getElementsByClass("content");
//
//			for (Element contenttag : contentClass) {
//				System.out.println(" contenttag  " + contenttag);
//				contenttag.text(" ADDED content here To persist the config every time you start the server or "
//						+ "for getting access to more special options or you can pass a config file through the --config-file or -C "
//						+ "option of the CLI. It should be a JSON file having the following properties."); //five > four
//			}
//
//			log.debug("doc  {}", doc);
//
//			Map<String, String> multipartFiles = new HashMap<>();
//			multipartFiles.put("payload", payload_zip_path);
//
//			Map<String, String> formDataMultiPartMap = new HashMap<>();
//			formDataMultiPartMap.put("chartConfig", chatrsData);
//			formDataMultiPartMap.put("templateFilePath", "payload/Executive.html");
//			formDataMultiPartMap.put("dashboardLogo", "payload/image.webp");
//			formDataMultiPartMap.put("dashboardHeading", "Executive Dashboard");
//			formDataMultiPartMap.put("type", type);
//			formDataMultiPartMap.put("templateFormat", "A3");
//
//			Map<String, String> headers = new HashMap<>();
//
//			/*final FileDataBodyPart filePart = new FileDataBodyPart("payload", new File(payload_zip_path));
//			FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
//			final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("chartConfig", chatrsData)
//					.field("templateFilePath", "payload/Executive.html").field("dashboardLogo", "payload/image.webp")
//					.field("dashboardHeading", "Executive Dashboard").field("type", type)
//					.bodyPart(filePart);
//			
//			final WebTarget target = client.target(url);
//			final Response response = target.request().accept("application/pdf")
//					.post(Entity.entity(multipart, multipart.getMediaType()));
//			
//			log.debug("response.getStatus() {} ", response.getStatus());
//			//log.debug(response.readEntity(String.class));
//			//String output = response.readEntity(String.class);
//			response.bufferEntity();
//			InputStream initialStream = response.readEntity(InputStream.class);*/
//			
//			InputStream initialStream = RestApiHandler.uploadMultipartFile(url, multipartFiles, formDataMultiPartMap,
//					headers, "application/pdf");
//			
//			/*ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
//			int nRead;
//			byte[] data = new byte[1024];
//			while ((nRead = initialStream.read(data, 0, data.length)) != -1) {
//				buffer2.write(data, 0, nRead);
//			}
//			
//			buffer2.flush();
//			byte[] byteArray = buffer2.toByteArray();
//			
//			PDDocument document = new PDDocument();
//			PDPage page = new PDPage();
//			//page.
//			document.addPage(page);
//			
//			PDPageContentStream contentStream = new PDPageContentStream(document, page);
//			
//			contentStream.setFont(PDType1Font.COURIER, 12);
//			contentStream.beginText();
//			contentStream.showText("Hello World");
//			contentStream.endText();
//			contentStream.close();*/
//			
//			//PDPageContentStream cos = new PDPageContentStream(document, staticPage);
//			/*PDStreamUtils.write(contentStream, "OneDevOps | Insights", PDType1Font.HELVETICA, 40, 120,
//					staticPage.getMediaBox().getHeight() - (2 * margin) - 250, Color.BLACK);
//			PDStreamUtils.rect(cos, 100, staticPage.getMediaBox().getHeight() - (2 * margin) - 300,
//					staticPage.getMediaBox().getWidth() - 190, 1, Color.blue);
//			cos.close();*/
//			
//			//PDDocument doc2 = document.load(byteArray);
//			//document.saveIncremental(buffer2);
//			
//
//
//			byte[] buffer = new byte[initialStream.available()];
//			initialStream.read(buffer);
//			String exportedFile = WORKFLOW_PDF_RESOLVED_PATH + "export" + System.currentTimeMillis() + ".pdf";
//			log.debug("exportedFile  {} ", exportedFile);
//			log.debug("buffer  {} ", buffer.toString());
//			File extractedPdfFile = new File(exportedFile);
//			OutputStream outStream = new FileOutputStream(extractedPdfFile);
//			outStream.write(buffer);// output.getBytes()
//			log.debug("extractedPdfFile path   {} ", extractedPdfFile.getAbsoluteFile());
//
//			//document.save(exportedFile);
//			//document.close();
//
//			//ByteArrayOutputStream actual = new ByteArrayOutputStream();
//			
//			/*BuilderConfig config =new BuilderConfig() {
//			    @Override
//			    public void configure(PdfRendererBuilder builder) {
//			    }
//			};
//			*/
//			// html to pdf using pdf box 
//			/*PdfRendererBuilder builder = new PdfRendererBuilder();
//			builder.withHtmlContent(extractedPdfFile.getAbsolutePath(), WORKFLOW_PDF_RESOLVED_PATH);
//			builder.toStream(actual);
//			builder.useFastMode();
//			builder.testMode(true);
//			//config.configure(builder);
//			
//			try {
//				builder.run();
//			} catch (Exception e) {
//				System.err.println("Failed to render resource ");
//			}
//			
//			FileUtils.writeByteArrayToFile(new File(WORKFLOW_PDF_RESOLVED_PATH, "export_html.pdf"),
//					actual.toByteArray());*/
//
//			//Use response object to verify upload success
//
//			//formDataMultiPart.close();
//			//multipart.close();
//			outStream.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
}
