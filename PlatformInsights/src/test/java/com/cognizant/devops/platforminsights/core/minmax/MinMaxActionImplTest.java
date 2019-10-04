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
package com.cognizant.devops.platforminsights.core.minmax;

import static org.junit.Assert.*;

import java.util.HashMap;
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
import com.cognizant.devops.platforminsights.exception.InsightsJobFailedException;

public class MinMaxActionImplTest {
	@Mock
	MinMaxActionImpl minMax;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Before
	public void load(){
	ApplicationConfigCache.loadConfigCache();
	}
	
	@Test
	public void testExecute() throws InsightsJobFailedException {
		load();
		//assertEquals("9200", ConfigConstants.SPARK_ES_PORT);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("Jobs", "Success");
		
		//When there are some jobs
		//Mockito.when(minMax.execute()).thenReturn(resultMap);
		//assertEquals(resultMap.get("Jobs"),minMax.execute().get("Jobs"));
				
				//When no jobs are present
		//Mockito.when(minMax.execute()).thenReturn(null);
		//assertNull(minMax.execute());
	}

}
