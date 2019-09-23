package com.cognizant.devops.platformwebhookengine.test.parser;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformwebhookengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.platformwebhookengine.parser.InsightsGeneralParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class InsightsGeneralParserTest extends InsightsParserTestData {
	public static final InsightsGeneralParser generalparser = new InsightsGeneralParser();
	public static final InsightsParserTestData testdata = new InsightsParserTestData();
	EngineAggregatorModule testapp = new EngineAggregatorModule();

	@Test(priority = 1)
	public void testGetToolDetailJson() throws InsightsCustomException {

		List<JsonObject> response = generalparser.parseToolData(testdata.responseTemplate, toolData, testdata.toolName,
				testdata.labelName, testdata.webhookName);
		JsonParser parser = new JsonParser();
	    List<JsonObject> retrunJsonList = new ArrayList<JsonObject>(0);
	Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
	String prettyJson = prettyGson.toJson(expectedOutput);
	JsonElement element = parser.parse(prettyJson);
	retrunJsonList.add(element.getAsJsonObject());
		//Assert.assertSame(response,testdata.expectedOutput);
		//List<JsonObject> participantJsonList = testdata.expectedOutput.readValue(jsonString, new TypeReference<List<JsonObject>>(){})
		Assert.assertEquals(response,testdata.expectedOutput);
		

	}
	
	@Test(priority = 2, expectedExceptions = Exception.class)
	public void testGetToolDetailJsonWithExceptions() throws InsightsCustomException
	{
		List<JsonObject> response = generalparser.parseToolData(testdata.responseTemplate, incorrectToolData, testdata.toolName,
				testdata.labelName, "");
	}
	  @Test(priority = 3)
	  public void testforMain()  throws Exception
	  {
		  
		       
	  }

}
