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
package com.cognizant.devops.platforminsights.core.avg;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AverageTest {
	

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	/*@Test
	public void testAverage() {
		Mockito.when(avg.total_).thenReturn(0L);
		Mockito.when(avg.num_).thenReturn(0L);
		
		assertEquals(0L,avg.total_.longValue());
		assertEquals(0L,avg.num_.longValue());
	}*/
	
	@Test
	public void testAvg(){
		//assertEquals(0L,avg.avg().longValue());
	}

}
