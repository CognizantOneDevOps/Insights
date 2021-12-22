/*********************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
 *******************************************************************************/
package com.cognizant.devops.platformdal.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.platformdal.agentConfig.AgentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsAssessmentReportTemplate;
import com.cognizant.devops.platformdal.assessmentreport.InsightsContentConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsKPIConfig;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportTemplateConfigFiles;
import com.cognizant.devops.platformdal.assessmentreport.InsightsReportVisualizationContainer;
import com.cognizant.devops.platformdal.autoML.AutoMLConfig;
import com.cognizant.devops.platformdal.correlationConfig.CorrelationConfiguration;
import com.cognizant.devops.platformdal.filemanagement.InsightsConfigFiles;
import com.cognizant.devops.platformdal.webhookConfig.WebHookConfig;
import com.cognizant.devops.platformdal.workflow.InsightsWorkflowConfiguration;

public class CommonDALUtils {
	private static final Logger log = LogManager.getLogger(CommonDALUtils.class);
	
	private CommonDALUtils() {
		
	}
	
	/** This method use for changing annotation value/base data type of some table so that It is compilable with other database
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws ClassNotFoundException
	 */
	public static void changeAnnotationValue() throws NoSuchFieldException, SecurityException, ClassNotFoundException {
		Map<Object, String> classMap = new HashMap<>();
		classMap.put(new InsightsConfigFiles(), "fileData");
		classMap.put(new InsightsReportTemplateConfigFiles(), "fileData");
		classMap.put(new InsightsReportVisualizationContainer(), "attachmentData");
		classMap.put(new AutoMLConfig(), "file");
		classMap.put(new AutoMLConfig(), "mojoDeployedZip");
		
		for (Map.Entry<Object, String> entry : classMap.entrySet()) {
			Field field = entry.getKey().getClass().getDeclaredField(entry.getValue());
			Annotation annotations = field.getAnnotations()[0];
			Object handler = Proxy.getInvocationHandler(annotations);
			log.debug("Field : {}", field.getName());
			field.setAccessible(true);
			Field f;
			try {
				f = handler.getClass().getDeclaredField("memberValues");
			} catch (NoSuchFieldException | SecurityException e) {
				throw new IllegalStateException(e);
			}
			f.setAccessible(true);
			Map<String, Object> memberValues;
			try {
				memberValues = (Map<String, Object>) f.get(handler);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			Object oldValue = memberValues.get("columnDefinition");
			memberValues.put("columnDefinition", "BLOB");
		}
		
		Map<Object, String> classBooleanMap = new HashMap<>();
		classBooleanMap.put(new AgentConfig(), "vault");
		classBooleanMap.put(new AgentConfig(), "iswebhook");
		classBooleanMap.put(new CorrelationConfiguration(), "enableCorrelation");
		classBooleanMap.put(new CorrelationConfiguration(), "isSelfRelation");
		classBooleanMap.put(new InsightsAssessmentReportTemplate() ,"isActive");
		classBooleanMap.put(new InsightsContentConfig() ,"isActive");
		classBooleanMap.put(new InsightsKPIConfig() ,"isActive");
		classBooleanMap.put(new AutoMLConfig(), "isActive");
		classBooleanMap.put(new WebHookConfig(), "subscribeStatus");
		classBooleanMap.put(new WebHookConfig(), "isUpdateRequired");
		classBooleanMap.put(new WebHookConfig(), "isEventProcessing");
		classBooleanMap.put(new InsightsWorkflowConfiguration(), "isActive");
		classBooleanMap.put(new InsightsWorkflowConfiguration(), "runImmediate");
		classBooleanMap.put(new InsightsWorkflowConfiguration(), "reoccurence");
		
		for (Map.Entry<Object, String> entry : classBooleanMap.entrySet()) {
			Field field = entry.getKey().getClass().getDeclaredField(entry.getValue());
			Annotation annotations = field.getAnnotations()[0];
			Object handler = Proxy.getInvocationHandler(annotations);
			log.debug("Field :{} ", field.getName());
			field.setAccessible(true);
			Field f;
			try {
				f = handler.getClass().getDeclaredField("memberValues");
			} catch (NoSuchFieldException | SecurityException e) {
				throw new IllegalStateException(e);
			}
			f.setAccessible(true);
			Map<String, Object> memberValues;
			try {
				memberValues = (Map<String, Object>) f.get(handler);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
			Object oldValue = memberValues.get("columnDefinition");
			memberValues.put("columnDefinition", "INT");
		}
	}
}
