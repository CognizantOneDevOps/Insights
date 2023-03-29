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
package com.cognizant.devops.platformservice.test.dataArchival;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.constants.ConfigOptions;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;import com.cognizant.devops.platformcommons.core.enums.DataArchivalStatus;
import com.cognizant.devops.platformcommons.core.util.InsightsUtils;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.agentConfig.AgentConfigDAL;
import com.cognizant.devops.platformdal.dataArchivalConfig.DataArchivalConfigDal;
import com.cognizant.devops.platformdal.dataArchivalConfig.InsightsDataArchivalConfig;
import com.cognizant.devops.platformservice.agentmanagement.service.AgentManagementServiceImpl;
import com.cognizant.devops.platformservice.dataarchival.service.DataArchivalServiceImpl;
import com.cognizant.devops.platformservice.test.testngInitializer.TestngInitializerTest;
import com.google.gson.JsonObject;
import com.cognizant.devops.platformservice.dataarchival.controller.DataArchivalController;

@Test
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class DataArchivalServiceTest extends DataArchivalServiceData {

	private static final Logger log = LogManager.getLogger(DataArchivalServiceTest.class);

	@Autowired
	DataArchivalController DataArchivalController;// = new DataArchivalController();
	@Autowired
	DataArchivalServiceImpl dataArchivalServiceImpl;// = new DataArchivalServiceImpl();
	DataArchivalConfigDal dataArchivalConfigDal = new DataArchivalConfigDal();
	@Autowired
	AgentManagementServiceImpl agentManagementServiceImpl;// = new AgentManagementServiceImpl();
	
	AgentConfigDAL agentConfigDAL = new AgentConfigDAL();
	String host = null;

	JsonObject testData = new JsonObject();
	
	@BeforeClass
	public void prepareData() throws InsightsCustomException {
		// register agent in DB
		try {
			   String path = System.getenv().get(ConfigOptions.INSIGHTS_HOME) + File.separator + TestngInitializerTest.TESTNG_TESTDATA + File.separator
	                    + TestngInitializerTest.TESTNG_PLATFORMSERVICE + File.separator + "DataArchivalService.json";
			   testData = JsonUtils.getJsonData(path).getAsJsonObject();
				
			JsonObject agentJson=testData.get("agentJson").getAsJsonObject();
			Boolean status = agentConfigDAL.saveAgentConfigFromUI(agentJson.get("agentId").getAsString(),
					agentJson.get("toolCategory").getAsString(), agentJson.get("labelName").getAsString(),
					agentJson.get("toolName").getAsString(), agentJson, agentJson.get("agentVersion").getAsString(),
					agentJson.get("osversion").getAsString(), updateDate, vault,false);
		} catch (Exception e) {
			log.error("message", e);
		}

	}
	
	@Test(priority = 1)
	public void testSaveArchivalRecord() throws InsightsCustomException {
		try {
			Long expectedCreatedOn = InsightsUtils.getTodayTime() / 1000;
			Long expectedExpiredDate = getExpiryDate(expectedCreatedOn, expectedDaysToRetain);
			JsonObject status = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecords").toString());
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString());
			
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, status.get("status").getAsString().replace("\"", ""));
			
			Assert.assertEquals("Start Date assertion failed", expectedStartDate, dataArchivalConfig.getStartDate());
			Assert.assertEquals("End Date assertion failed", expectedEndDate, dataArchivalConfig.getEndDate());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 2)
	public void testSaveArchivalRecordForDeleting() throws InsightsCustomException {
		try {
			Long expectedCreatedOnForDeleteCase = InsightsUtils.getTodayTime() / 1000;
			Long expectedExpiryDateForDeleteCase = getExpiryDate(expectedCreatedOnForDeleteCase,
					expectedDaysToRetainForDeleteCase);
			JsonObject status = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsForDeleteCase").toString());
			
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(testData.get("saveArchivalRecordsForDeleteCase").getAsJsonObject().get("archivalName").getAsString());
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, status.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 3)
	public void testSaveDuplicateArchivalRecord() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecords").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	}
  }

	@Test(priority = 4)
	public void testSaveArchivalRecordWithoutArchivalName() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsWithoutName").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE,response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	      }
	}

	@Test(priority = 5)
	public void testSaveArchivalRecordWithIncompleteData() throws InsightsCustomException {
		try {
			DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsIncompleteData").toString());
			}
		catch (Exception e) {
			Assert.assertEquals(true, e.toString().contains("NullPointerException"));
		}
	}

	@Test(priority = 6)
	public void testSaveArchivalRecordWithIncorrectNameData() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsIncorrectNameData").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 7)
	public void testSaveArchivalRecordWithIncorrectDateFormat() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsIncorrectDateData").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 8)
	public void testSaveArchivalRecordWithIncorrectDaysToRetain() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsIncorrectDaysToRetainData").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 9)
	public void testSaveArchivalRecordWithLargeDaysToRetain() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsLargeDaysToRetainData").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 10)
	public void testsaveArchivalRecordsWithStartDateGreaterThanEndDate() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecordsLargeDaysToRetainData").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 11)
	public void testsaveArchivalRecordsWithNoDataArchivalAgent() throws InsightsCustomException {
		try {
			List<AgentConfig> agentConfigs = agentConfigDAL.deleteAgentConfigurations(testData.get("agentJson").getAsJsonObject().get("agentId").getAsString());
			JsonObject response = DataArchivalController.saveDataArchivalDetails(testData.get("saveArchivalRecords").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 12)
	public void testUpdateArchivalSourceURL() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.updateArchivalSourceUrl(testData.get("updateSourceURL").toString());
			Assert.assertEquals(PlatformServiceConstants.SUCCESS, response.get("status").getAsString().replace("\"", ""));
		    InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal.getSpecificArchivalRecord(testData.get("updateSourceURL").getAsJsonObject().get("archivalName").getAsString());
			Assert.assertEquals(dataArchivalConfig.getSourceUrl(), testData.get("updateSourceURL").getAsJsonObject().get("sourceUrl").getAsString());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 13)
	public void testUpdateArchivalSourceURLWithEmptyArchivalName() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.updateArchivalSourceUrl(testData.get("updateSourceURLWithEmptyName").toString());
			Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}

	@Test(priority = 14)
	public void testInActivateArchivalRecord() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.inactivateArchivalRecord(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString());
		    Assert.assertEquals(PlatformServiceConstants.SUCCESS, response.get("status").getAsString().replace("\"", ""));
	
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(testData.get("updateSourceURL").getAsJsonObject().get("archivalName").getAsString());
			Assert.assertEquals("INACTIVE", dataArchivalConfig.getStatus());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test(priority = 15)
	public void testInActivateArchivalRecordWithWrongName() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.inactivateArchivalRecord(wrongRecordName);
		    Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
	}
	
	@Test(priority = 16)
	public void testActivateArchivalRecord() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.activateArchivalRecord(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString());
		    Assert.assertEquals(PlatformServiceConstants.SUCCESS, response.get("status").getAsString().replace("\"", ""));
	
			InsightsDataArchivalConfig dataArchivalConfig = dataArchivalConfigDal
					.getSpecificArchivalRecord(testData.get("updateSourceURL").getAsJsonObject().get("archivalName").getAsString());
			Assert.assertEquals("ACTIVE", dataArchivalConfig.getStatus());
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 17)
	public void testActivateArchivalRecordWithWrongName() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.activateArchivalRecord(wrongRecordName);
		    Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
		} catch (AssertionError e) {
			Assert.fail(e.getMessage());
	    }
    }

	@Test(priority = 18)
	public void testGetActivateArchivalRecords() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.getRegisteredWebHooks();
	        Assert.assertEquals(PlatformServiceConstants.SUCCESS, response.get("status").getAsString().replace("\"", ""));
	} catch (AssertionError e) {
			Assert.fail(e.getMessage());
		} 
	}

	@Test(priority = 19)
	public void testGetAllArchivalRecords() throws InsightsCustomException {
		try {
		JsonObject response = DataArchivalController.getAllArchivalRecord();
        Assert.assertEquals(PlatformServiceConstants.SUCCESS, response.get("status").getAsString().replace("\"", ""));
		}
		catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 20)
	public void testDeleteArchivalRecords() throws Exception {
		try {
			dataArchivalConfigDal.updateArchivalStatus(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString(), DataArchivalStatus.TERMINATED.name());
		    JsonObject response = DataArchivalController.deleteArchivalRecord(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString());
            Assert.assertEquals(PlatformServiceConstants.SUCCESS, response.get("status").getAsString().replace("\"", ""));
		}
		catch (AssertionError e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test(priority = 21)
	public void testDeleteArchivalRecordsWithWrongName() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.deleteArchivalRecord(wrongRecordName);
	        Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
			}
			catch (AssertionError e) {
				Assert.fail(e.getMessage());
			}
		}
	
	@Test(priority = 22)
	public void testDeleteArchivalRecordsError() throws InsightsCustomException {
		try {
			JsonObject response = DataArchivalController.deleteArchivalRecord(testData.get("saveArchivalRecordsForDeleteCase").getAsJsonObject().get("archivalName").getAsString());
	        Assert.assertEquals(PlatformServiceConstants.FAILURE, response.get("status").getAsString().replace("\"", ""));
			}
			catch (AssertionError e) {
				Assert.fail(e.getMessage());
			}
		}

	@AfterClass
	public void cleanUp() throws InsightsCustomException {
		try {
			Boolean statusAfterInActivating = dataArchivalServiceImpl
					.inactivateArchivalRecord(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString());
		} catch (Exception e) {
			log.error("Error cleaning data in  DataArchivalServiceTest statusInactive record ", e);
		}
		try {
			Boolean statusAfterDeleting = dataArchivalServiceImpl
					.deleteArchivalRecord(testData.get("saveArchivalRecords").getAsJsonObject().get("archivalName").getAsString());
		} catch (Exception e) {
			log.error("Error cleaning data in DataArchivalServiceTest archival record ", e);
		}
	}
}
