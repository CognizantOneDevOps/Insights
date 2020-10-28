/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformreports.assessment.util;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReportChartCollection {
	
	private ReportChartCollection() {}

	
	public static final List<String> SINGLE_SERIES_CHARTS = Collections.unmodifiableList(
			     Arrays.asList("pie2d","pie3d","doughnut2d","doughnut3d","pareto2d","pareto3d"));
	
	
	public static final List<String> COMMON_CHARTS = Collections.unmodifiableList(
		     Arrays.asList("mscolumn2d","mscolumn3d","msline","msbar2d","msbar3d","overlappedcolumn2d",
		    		       "overlappedbar2d","msarea","stackedcolumn2d","stackedcolumn3d","stackedbar2d","stackedbar3d",
		    		       "stackedarea2d"));
	public static final List<String> SINGLE_VALUE_CHARTS = Collections.unmodifiableList(
		     Arrays.asList("angulargauge"));
	
	
}
