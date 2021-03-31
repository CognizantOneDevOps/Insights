/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.cognizant.devops.platformservice.test.upshiftassessment;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfig;
import com.cognizant.devops.platformdal.upshiftassessment.UpshiftAssessmentConfigDAL;
import com.cognizant.devops.platformservice.upshiftassessment.service.UpshiftAssessmentServiceImpl;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Test
@ContextConfiguration(locations = {"classpath:spring-test-config.xml"})
public class UpshiftAssessmentServiceTest extends UpshiftAssessmentServiceData {
    @Autowired
    public static final UpshiftAssessmentServiceImpl upshiftAssessmentService = new UpshiftAssessmentServiceImpl();
    private static final Logger log = LogManager.getLogger(UpshiftAssessmentServiceTest.class);
    UpshiftAssessmentConfigDAL upshiftAssessmentConfigDAL = new UpshiftAssessmentConfigDAL();

    @BeforeClass
    public void prepareData() throws InsightsCustomException {
        try {
            prepareAssessmentData();
        } catch (Exception e) {
            log.error("message", e);
        }

    }

    @Test(priority = 1)
    public void testsaveReport() throws InsightsCustomException {
        try {
            upshiftAssessmentService.saveUpshiftAssessment("Test01", testFile);
            UpshiftAssessmentConfig upshiftAssessmentConfig = upshiftAssessmentConfigDAL.fetchUpshiftAssessmentByUuid("Test01");
            Assert.assertNotNull(upshiftAssessmentConfig);
            Assert.assertNotNull(upshiftAssessmentConfig.getUpshiftUuid());
            Assert.assertEquals(upshiftAssessmentConfig.getUpshiftUuid(), "Test01");
        } catch (AssertionError e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test(priority = 2, expectedExceptions = InsightsCustomException.class)
    public void testUploadContentInDatabaseWithWrongFileFormat() throws InsightsCustomException, IOException {
        File configFileTxt = new File(classLoader.getResource("ContentsConfiguration.txt").getFile());
        FileInputStream input = new FileInputStream(configFileTxt);
        MultipartFile multipartFile = new MockMultipartFile("file", configFileTxt.getName(), "text/plain",
                IOUtils.toByteArray(input));
        upshiftAssessmentService.saveUpshiftAssessment("Test02", multipartFile);
    }

}
