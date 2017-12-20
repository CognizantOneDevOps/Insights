/*package com.cognizant.devops.platforminsights.core.util;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.gson.JsonObject;

public class FileHandlerUtilTest {
	@Mock
	static FileHandlerUtil fileHandler;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Test
	public void testLoadJsonFile() {
		JsonObject jsonObj = new JsonObject();
		jsonObj.addProperty("Load Status", "Success");
		
		String path = "./json-file";
		
		Mockito.when(fileHandler.loadJsonFile(path)).thenReturn(jsonObj);
		assertEquals(jsonObj.get("Load Status"), fileHandler.loadJsonFile(path));aas
	}

}
*/