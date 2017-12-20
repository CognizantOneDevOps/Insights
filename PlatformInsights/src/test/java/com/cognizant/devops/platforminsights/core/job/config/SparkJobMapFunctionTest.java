package com.cognizant.devops.platforminsights.core.job.config;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import scala.Tuple2;


public class SparkJobMapFunctionTest {

	@Mock
	SparkJobMapFunction sparkJobMap;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Test
	public void testCall() throws Exception {
				
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("status","success");
		
		Map<String, Object> tupleMap = new HashMap<String, Object>();
		tupleMap.put("Status", "Success");
		
		Tuple2<String, Map<String, Object>> v1 = new Tuple2<String, Map<String, Object>>(null, tupleMap);
		Map<String, Object> data = v1._2;
		Gson gson = new Gson();
		SparkJobConfiguration model = gson.fromJson(gson.toJson(data), SparkJobConfiguration.class);
		model.setId(v1._1);
		
		Mockito.when(sparkJobMap.call(v1)).thenReturn(model);
		System.out.println(model);
		assertEquals(model,sparkJobMap.call(v1));
		
		//When there are no Spark Jobs found
		Mockito.when(sparkJobMap.call(null)).thenReturn(null);
		assertNull(sparkJobMap.call(null));
	}

}
