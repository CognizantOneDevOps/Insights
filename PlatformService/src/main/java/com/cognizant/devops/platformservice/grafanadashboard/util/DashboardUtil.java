/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
package com.cognizant.devops.platformservice.grafanadashboard.util;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;

import be.quodlibet.boxable.utils.PDStreamUtils;

/**
 * 
 * @author
 *
 */
public class DashboardUtil {

	private static Logger Log = LogManager.getLogger(DashboardUtil.class);

	public byte[] getPanels(String dashUrl, String title, String variables) {
		List<BufferedImage> imageList = null;
		PDDocument document = new PDDocument();
		WebDriver driver = DriverFactory.getInstance().getDriver();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			loginGrafana(driver,dashUrl);
			generateStaticContent(document, title);
			imageList = captureScreenShot(driver);
			generateImage(imageList, document);
			footer(document, title, variables);
			//document.save(title); - Saving a physical for debug purpose 
			document.save(baos);
		}catch (InterruptedException | IOException | AWTException e) {
			Log.error("Error, Failed to download Dashboard .. ", e.getMessage());
		}finally {
			try {
				document.close();
			} catch (IOException e) {
				Log.error("Error, Document failed to load .. ", e.getMessage());
			}
			DriverFactory.getInstance().removeDriver();
		}
		return baos.toByteArray();
	}


	/**
	 * Login to Grafana and fetch current {dashUrl} dashboard 
	 * Css Applicable only to Grafana v6.1.6 and firefox browser.
	 * 
	 * @param driver
	 * @param dashUrl
	 */
	private void loginGrafana(WebDriver driver, String dashUrl) {
		driver.get(ApplicationConfigProvider.getInstance().getGrafana().getGrafanaEndpoint());
		WebElement username = driver.findElement(By.name("username"));
		username.sendKeys(ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName());
		WebElement pwd = driver.findElement(By.name("password"));
		pwd.sendKeys(ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword());
		driver.findElement(By.cssSelector("button.btn:nth-child(1)")).click();
		driver.get(dashUrl);
	}


	/**
	 * Get previous page in the document.
	 * 
	 * @param document
	 * @return {pageNum}
	 */
	private static int getPages(PDDocument document) {
		PDPageTree pages = document.getPages();
		return pages.getCount()-1;
	}


	/**
	 * Generate Static content based on the Title configured .
	 * 
	 * @param document
	 * @param title
	 * @throws IOException
	 */
	private void generateStaticContent(PDDocument document, String title) throws IOException {
		float margin = 10f;
		PDPage staticPage = new PDPage();
		document.addPage(staticPage); 
		PDPageContentStream cos = new PDPageContentStream(document, staticPage);
		PDStreamUtils.write(cos, "OneDevOps | Insights", PDType1Font.HELVETICA, 40, 120, staticPage.getMediaBox().getHeight() - (2 * margin)-250,
				Color.BLACK);
		PDStreamUtils.rect(cos, 100, staticPage.getMediaBox().getHeight() - (2 * margin)-300, staticPage.getMediaBox().getWidth()-190, 1, Color.blue);
		cos.close();
	}

	/**
	 * Fetches all the panels based on the Grafana shortuct keys and 
	 * returns list of panel screenshots.
	 * 
	 * 
	 * @param driver
	 * @return {imageList}
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws AWTException
	 */
	private List<BufferedImage> captureScreenShot(WebDriver driver) throws InterruptedException, IOException, AWTException {
		List<BufferedImage> imageList = new ArrayList<BufferedImage>();
		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
				.withTimeout(Duration.ofSeconds(30)).pollingEvery(Duration.ofSeconds(5))
				.ignoring(NoSuchElementException.class).ignoring(TimeoutException.class); 
		wait.until(new Function<WebDriver, WebElement>(){ 
			public WebElement apply(WebDriver driver )
			{ 
				return driver.findElement(By.cssSelector(".panel-title")); 
			} 
		});
		List<WebElement> eleq = driver.findElements(By.cssSelector(".react-grid-item.react-draggable.react-resizable"));
		Log.info("Total panels - "+eleq.size());
		JavascriptExecutor js = ((JavascriptExecutor) driver);
		for(WebElement e: eleq){
			Log.info("Panel --"+e);
			js.executeScript("arguments[0].scrollIntoView(true);", e);
			Actions builder = new Actions(driver);
			Action seriesOfActions = builder
					.moveToElement(e).sendKeys("\u0056")
					.build();
			seriesOfActions.perform();
			Date d =new Date();
			Thread.sleep(1000);
			File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
			BufferedImage fullImg;
			fullImg = ImageIO.read(screenshot);
			WebElement container = wait.until(new
					Function<WebDriver, WebElement>(){ 
				public WebElement apply(WebDriver driver )
				{ 
					return driver.findElement(By.cssSelector(".react-grid-item.react-grid-item--fullscreen.react-draggable.react-resizable")); 
				} 
			});
			js.executeScript("arguments[0].scrollIntoView(true);", container);
			Point point = container.getLocation();
			Log.info("point--"+point);
			Log.info("point.getX()--"+point.getX());
			Log.info("point.getY()--"+point.getY());
			int eleWidth = container.getSize().getWidth();
			Log.info("eleWidth--"+eleWidth);
			int eleHeight = container.getSize().getHeight();
			Log.info("eleHeight--"+eleHeight);
			/** Allows to save panel screenshot - enable it to debug **/
			//FileUtils.copyFile(screenshot, new File(d.toString().replace(":", "_")+".png"));
			BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), point.getY(),
					eleWidth, eleHeight);
			imageList.add(eleScreenshot);
			Action seriesOfActions1 = builder
					.sendKeys("\u0056")
					.build();
			seriesOfActions1.perform();
		}
		return imageList;
	}

	/**
	 * Footer is filled with varaibles selected in Grafana by user
	 * 
	 * @param doc
	 * @param title
	 * @param variables
	 * @return doc
	 * @throws IOException
	 */
	private PDDocument footer(PDDocument doc, String title, String variables) throws IOException {
		try{
			PDPageTree pages = doc.getPages();
			for(PDPage p : pages){
				PDPageContentStream contentStream = new PDPageContentStream(doc, p, AppendMode.APPEND, false);
				contentStream.beginText();
				contentStream.newLineAtOffset(220, 780);
				contentStream.setFont(PDType1Font.HELVETICA, 11);
				contentStream.showText("OneDevOps Insights – "+title);
				contentStream.endText();
				if(!variables.equals("") && variables != null){
					contentStream.beginText();
					contentStream.newLineAtOffset(2, 17);
					contentStream.setFont(PDType1Font.HELVETICA, 9);
					contentStream.showText("This Report is generated based on the user selected values as below.");
					contentStream.endText();
					contentStream.beginText();
					contentStream.newLineAtOffset(2, 5);
					contentStream.setFont(PDType1Font.HELVETICA, 7);
					contentStream.showText(variables);
					contentStream.endText();
				}
				contentStream.close();
			}
		}catch(Exception e){
			Log.error("Error, Failed in Footer.. ", e.getMessage());
		}
		return doc;
	}

	private void generateImage(List<BufferedImage> imageList, PDDocument document) throws IOException {
		if(imageList != null) {
			Log.info("size---"+imageList.size());
			PDPageContentStream contentStream = null;
			for(int i=0;i<imageList.size();i++){
				if(i%2 ==0){
					PDPage page = new PDPage();
					document.addPage(page); 
					contentStream = new PDPageContentStream(document, page, AppendMode.APPEND,true, true);
					PDImageXObject img = LosslessFactory.createFromImage(document, imageList.get(i));
					contentStream.drawImage( img, 10, 400, 600, 300 );
					contentStream.close();
				}else{
					int previousPage = getPages(document);
					Log.info(previousPage);
					contentStream = new PDPageContentStream(document, document.getPage(previousPage), AppendMode.APPEND,true, true);
					PDImageXObject img1 = LosslessFactory.createFromImage(document, imageList.get(i));
					contentStream.drawImage( img1, 10, 30, 600, 300 );
					contentStream.close();
				}
			}
		}
	}

}
