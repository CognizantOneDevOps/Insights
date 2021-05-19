/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.test.grafanadashboard;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfig;
import com.cognizant.devops.platformdal.grafana.pdf.GrafanaDashboardPdfConfigDAL;
import com.cognizant.devops.platformservice.grafanadashboard.service.GrafanaPdfServiceImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Test
@ContextConfiguration(locations = {"classpath:spring-test-config.xml"})
public class GrafanaDashboardReportTest extends GrafanaDashboardReportData{

	private static final Logger log = LogManager.getLogger(GrafanaDashboardReportTest.class);

	GrafanaPdfServiceImpl grafanaPdfServiceImpl = new GrafanaPdfServiceImpl();
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		try {
			prepareDashboardData();
		} catch (Exception e) {
			log.error("message", e);
		}

	}
	
	@Test(priority = 1)
    public void publishGrafanaDashboardDetails() throws InsightsCustomException {
        try {
        	JsonObject detailsJson = new JsonParser().parse(dashboardJson).getAsJsonObject();
        	grafanaPdfServiceImpl.saveGrafanaDashboardConfig(detailsJson);
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }
	
	@Test(priority = 2)
    public void getAllGrafanaDashboardConfigs() throws InsightsCustomException {
        try {
        	List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
        	Assert.assertEquals(list.size(), 1);
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }
	
	@Test(priority = 3)
    public void updateGrafanaDashboardDetails() throws InsightsCustomException {
        try {
            int id =0;
            List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
            for(GrafanaDashboardPdfConfig g: list){
                id= g.getId();
            }
        	JsonObject detailsJson = new JsonParser().parse(updateJson).getAsJsonObject();
            detailsJson.addProperty("id", id);
        	grafanaPdfServiceImpl.updateGrafanaDashboardDetails(detailsJson);
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }
	
	@Test(priority = 4)
    public void fetchGrafanaDashboardDetailsByWorkflowId() throws InsightsCustomException {
		GrafanaDashboardPdfConfigDAL grafanaDashboardConfigDAL = new GrafanaDashboardPdfConfigDAL();
        try {
        	grafanaDashboardConfigDAL.fetchGrafanaDashboardDetailsByWorkflowId("GRAFANADASHBOARDPDFREPORT_1620379924");
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }
	
	@Test(priority = 5)
    public void deleteGrafanaDashboardDetails() throws InsightsCustomException {
        try {
            int id =0;
            List<GrafanaDashboardPdfConfig> list = grafanaPdfServiceImpl.getAllGrafanaDashboardConfigs();
            for(GrafanaDashboardPdfConfig g: list){
                id= g.getId();
            }
        	grafanaPdfServiceImpl.deleteGrafanaDashboardDetails(id);;
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }
    }

}
