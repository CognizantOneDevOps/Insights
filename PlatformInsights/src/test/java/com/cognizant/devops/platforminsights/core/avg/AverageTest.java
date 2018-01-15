package com.cognizant.devops.platforminsights.core.avg;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AverageTest {
	@Mock
	Average avg;
	
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
		assertEquals(0L,avg.avg().longValue());
	}

}
