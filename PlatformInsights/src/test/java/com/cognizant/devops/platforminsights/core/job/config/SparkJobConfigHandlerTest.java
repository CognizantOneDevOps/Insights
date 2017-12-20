package com.cognizant.devops.platforminsights.core.job.config;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.cognizant.devops.platformcommons.config.ApplicationConfigCache;
import com.cognizant.devops.platforminsights.configs.ConfigConstants;
import com.google.gson.JsonObject;

public class SparkJobConfigHandlerTest {
	
	@Mock
	SparkJobConfigHandler configs;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Before
	public void load(){
	ApplicationConfigCache.loadConfigCache();
	}
	
	@Test
	public void testLoadJobsFromES() {
		Map<String, String> jobConf = new HashMap<String, String>();		
		load();
		assertEquals("spark-jobs-conf/configs", ConfigConstants.SPARK_ES_CONFIGINDEX);
		
		Object jsonResponse = new Object();
		jsonResponse = "abc";
		
		List<Map<String, Object>> resultList = new ArrayList<>();
		//resultList.add("JobsTest",jsonResponse);
		//Mockito.when(configs.loadJobsFromES()).thenReturn();
	}
	
	@Test
	public void testUpdateJobsInES(){
		load();
		assertEquals("spark-jobs-conf/configs", ConfigConstants.SPARK_ES_CONFIGINDEX);
		assertEquals("9200", ConfigConstants.SPARK_ES_PORT);
	}
	
	@Test
	public void testSaveJobResultInES(){
		load();
		assertEquals("spark-jobs-conf/kpiresults", ConfigConstants.SPARK_ES_RESULTINDEX);
		assertEquals("9200", ConfigConstants.SPARK_ES_PORT);
	}

}
