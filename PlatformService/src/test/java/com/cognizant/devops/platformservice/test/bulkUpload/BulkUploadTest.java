package com.cognizant.devops.platformservice.test.bulkUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.bulkupload.service.BulkUploadService;




@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class BulkUploadTest extends BulkUploadTestData {
	
	public static final BulkUploadService bulkUploadService = new BulkUploadService();
	public static final BulkUploadTestData bulkUploadTestData = new BulkUploadTestData();

	

	
	@Test(priority = 2, expectedExceptions = InsightsCustomException.class)
	public void testUploadDataInDatabase() throws InsightsCustomException, IOException {
		
		Path path = Paths.get("D:\\sonar.csv");
		String contentType = "text/plain";
		byte[] content = null;
		content = Files.readAllBytes(path);
		MultipartFile multipartFile = new MockMultipartFile("sonar.csv","sonar.csv",contentType,content );
					
	boolean expectedOutCome = bulkUploadService.uploadDataInDatabase(multipartFile, toolName, label);	
	
	Assert.assertNotNull(expectedOutCome);
	Assert.assertEquals(expectedOutCome, true);
	Assert.assertFalse(multipartFile.isEmpty());
	Assert.assertTrue(multipartFile.getSize()>2097152);


	


		
		 
	
	}
	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {
					
		String  response = bulkUploadService.getToolDetailJson().toString();
		
		Assert.assertNotNull(response);
		Assert.assertTrue(response.length()>0);
		Assert.assertNotNull(bulkUploadTestData.toolJson);
		Assert.assertNotNull(bulkUploadTestData.toolName, "GIT");	
		Assert.assertNotNull(bulkUploadTestData.label, "SCM:GIT:DATA");
		
	
	
		
	}
	
			
	
	

}
