/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * 	of the License at
 *   
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
		
		//Mockito.when(baseActionTest.getResultMap(1L, "Jobs")).thenReturn(resultMap);
		//assertEquals("Success",baseActionTest.getResultMap(1L, "Jobs").get("Status"));
	}

	@Test
	public void testGetEsQueryWithDates() {
		String esQuery = "query";
		JobSchedule schedule = null;
		//Mockito.when(baseActionTest.getEsQueryWithDates(schedule , esQuery)).thenReturn("Success");
		//assertEquals("Success",baseActionTest.getEsQueryWithDates(schedule, esQuery));
	}

}
