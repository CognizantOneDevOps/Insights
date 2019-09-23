package com.cognizant.devops.platformwebhookengine.test.parser;

import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformwebhookengine.modules.aggregator.EngineAggregatorModule;
import com.cognizant.devops.platformwebhookengine.parser.InsightsGeneralParser;
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
		/*
		 * List<JsonObject> retrunJsonList = new ArrayList<JsonObject>(0); Gson
		 * prettyGson = new GsonBuilder().setPrettyPrinting().create(); String
		 * prettyJson = prettyGson.toJson(expectedOutput); JsonElement element =
		 * parser.parse(prettyJson); retrunJsonList.add(element.getAsJsonObject());
		 */
		// Assert.assertSame(response,testdata.expectedOutput);
		// List<JsonObject> participantJsonList =
		// testdata.expectedOutput.readValue(jsonString, new
		// TypeReference<List<JsonObject>>(){})
		String commitId = response.get(0).get("commitId").toString();
		String authorName = response.get(0).get("authorName").toString();
		String message = response.get(0).get("message").toString();

		Assert.assertEquals(commitId, testdata.commitId);
		Assert.assertEquals(authorName, testdata.authorName);
		Assert.assertEquals(message, testdata.message);

		/*
		 * StringBuilder builder = new StringBuilder(response.toString());
		 * builder.deleteCharAt(response.toString().length() - 1);
		 * builder.deleteCharAt(0);
		 * 
		 * Assert.assertEquals(builder,testdata.expectedOutput);
		 */

	}

	@Test(priority = 2, expectedExceptions = Exception.class)
	public void testGetToolDetailJsonWithExceptions() throws InsightsCustomException {
		List<JsonObject> response = generalparser.parseToolData(testdata.responseTemplate, incorrectToolData,
				testdata.toolName, testdata.labelName, "");
	}

}
