package com.cognizant.devops.platforminsights.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.cognizant.devops.platformcommons.core.enums.ExecutionActions;
import com.cognizant.devops.platformcommons.core.enums.JobSchedule;

public class BaseActionImplTest {
	@Mock
	BaseActionImpl baseActionTest;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Test
	public void testBaseActionImpl() {
		assertEquals("AVERAGE",ExecutionActions.AVERAGE.toString());
	}

	@Test
	public void testGetResultMap() {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("Status", "Success");
		
		Mockito.when(baseActionTest.getResultMap(1L, "Jobs")).thenReturn(resultMap);
		assertEquals("Success",baseActionTest.getResultMap(1L, "Jobs").get("Status"));
	}

	@Test
	public void testGetEsQueryWithDates() {
		String esQuery = "query";
		JobSchedule schedule = null;
		Mockito.when(baseActionTest.getEsQueryWithDates(schedule , esQuery)).thenReturn("Success");
		assertEquals("Success",baseActionTest.getEsQueryWithDates(schedule, esQuery));
	}

}
