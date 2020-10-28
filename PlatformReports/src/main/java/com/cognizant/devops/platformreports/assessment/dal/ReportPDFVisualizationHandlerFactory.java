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
package com.cognizant.devops.platformreports.assessment.dal;

import com.cognizant.devops.platformreports.assessment.pdf.BasePDFProcessor;
import com.cognizant.devops.platformreports.assessment.pdf.D3ChartHandler;
import com.cognizant.devops.platformreports.assessment.pdf.FusionChartHandler;
import com.cognizant.devops.platformreports.assessment.pdf.LedgerPDFChartHandler;
import com.cognizant.devops.platformreports.assessment.pdf.OpenPDFChartHandler;

public class ReportPDFVisualizationHandlerFactory {
	
	private ReportPDFVisualizationHandlerFactory() {

	}

	public static BasePDFProcessor getChartHandler(String vender) {
		if (vender == null) {
			return null;
		}
		if (vender.equalsIgnoreCase("Fusion")) {
			return new FusionChartHandler();

		} else if (vender.equalsIgnoreCase("D3")) {
			return new D3ChartHandler();

		} else if (vender.equalsIgnoreCase("OpenPDF")) {
			return new OpenPDFChartHandler();
		} else if (vender.equalsIgnoreCase("LedgerPDF")) {
			return new LedgerPDFChartHandler();
		}
		return null;
	}

}
