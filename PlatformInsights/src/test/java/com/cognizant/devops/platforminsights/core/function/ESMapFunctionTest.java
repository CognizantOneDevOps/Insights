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
package com.cognizant.devops.platforminsights.core.function;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import scala.Tuple2;

public class ESMapFunctionTest {
	@Mock
	ESMapFunction esMapFunc;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Test
	public void testCall() throws Exception {
		Map<String, Object> tupleMap = new HashMap<String, Object>();
		tupleMap.put("Status", "Success");
		Tuple2<String, Map<String, Object>> v1 = new Tuple2<String, Map<String, Object>>(null, tupleMap);
		
		Mockito.when(esMapFunc.call(v1)).thenReturn(1L);
		assertEquals(1L,esMapFunc.call(v1).longValue());
	}

}
