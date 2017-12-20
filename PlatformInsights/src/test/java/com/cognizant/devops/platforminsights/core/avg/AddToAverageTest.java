package com.cognizant.devops.platforminsights.core.avg;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class AddToAverageTest {
	@Mock
	AddToAverage addAvg;
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Test
	public void testCall() {
		Average a = new Average(0L, 0L);
		Mockito.when(addAvg.call(a, 0L)).thenReturn(a);
				
		assertEquals(a,addAvg.call(a, 0L));
	}

}
