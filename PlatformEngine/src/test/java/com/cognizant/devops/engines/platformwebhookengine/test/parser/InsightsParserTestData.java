/*******************************************************************************
 *  * Copyright 2017 Cognizant Technology Solutions
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  * use this file except in compliance with the License.  You may obtain a copy
 *  * of the License at
 *  * 
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  * License for the specific language governing permissions and limitations under
 *  * the License.
 *******************************************************************************/
package com.cognizant.devops.engines.platformwebhookengine.test.parser;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import com.cognizant.devops.platformdal.webhookConfig.WebhookDerivedConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsParserTestData {

	int defaultInterval = 600;
	String responseTemplate = "head_commit.id=commitId,head_commit.message=message,head_commit.timestamp=commitTime,head_commit.author.name=authorName";
	String toolName = "GIT";
	String labelName = "SCM:GIT76:DATA";
	String mqChannel = "IPW_git_webhook";
	String webhookName = "git_webhook";
	String incorrectToolData = "tooldata=incorrcet";
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	LocalDateTime now = LocalDateTime.now();
	String commitId = "\"86ef096bb924674a69cd2198e2964b76aa75d88b\"";
	String authorName = "\"Insights_test\"";
	String message = "\"Update Hello\"";
	String timestamp = "\"2019-09-16T04:52:25Z\"";
	String fieldNotFoundinToolData = "head_commit.newid=commitId,head_commit.newmessage=message";
	String toolData = "{\"head_commit\":{\"id\":\"86ef096bb924674a69cd2198e2964b76aa75d88b\",\"tree_id\":\"d1face44ae7151dfaf6387b5eaf3075419583dcc\",\"distinct\":true,\"message\":\"Update Hello\",\"timestamp\":\"2019-09-16T04:52:25Z\",\"author\":{\"name\":\"Insights_test\",\"email\":\"46189557+insights@users.noreply.github.com\",\"username\":\"insights546\"},\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"added\":[],\"removed\":[],\"modified\":[\"Hello\"]}}";
	String expectedOutput = "{\"commitTime\":\"2019-09-16T04:52:25Z\",\"authorName\":\"Insights_test\",\"insightsTime\":"
			+ ZonedDateTime.now().toInstant().toEpochMilli()
			+ ",\"webhookName\":\"git_demo\",\"commitId\":\"86ef096bb924674a69cd2198e2964b76aa75d88b\",\"source\":\"webhook\",\"message\":\"Update Hello\",\"inSightsTimeX\":\""
			+ dtf.format(now) + "\",\"labelName\":\"SCM:GIT76:DATA\",\"toollName\":\"GIT\"}";
	Set<WebhookDerivedConfig> setWebhookDerivedConfigs = new HashSet<WebhookDerivedConfig>();
	WebhookDerivedConfig wdcInsightsTime = new WebhookDerivedConfig();
	WebhookDerivedConfig wdcTimeSeriesMapping = new WebhookDerivedConfig();
	WebhookDerivedConfig wdcDataEnrichment = new WebhookDerivedConfig();
	String operationFieldsTimeX = "{\"timeField\":\"commitTime\",\"timeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\",\"epochTime\":\"false\"}";
	String operationFieldsTimeSeries = "{\"mappingTimeField\":\"commitTime\",\"mappingTimeFormat\":\"yyyy-MM-dd'T'HH:mm:ssXXX\",\"epochTime\":\"false\"}";
	String operationFieldsDataEnrich = "{\"sourceProperty\":\"message\",\"keyPattern\":\"-\",\"targetProperty\":\"jirakey\"}";
	//JsonObject jsonObject = new JsonParser().parse(operationFieldsDataEnrich).getAsJsonObject();
	public Set<WebhookDerivedConfig> getSetObject() {
		wdcInsightsTime.setOperationName("insightsTimex");
		wdcInsightsTime.setWebhookName("git_demo");
		wdcInsightsTime.setWid(123);
		wdcInsightsTime.setOperationFields(operationFieldsTimeX);
		setWebhookDerivedConfigs.add(wdcInsightsTime);
		wdcTimeSeriesMapping.setOperationFields(operationFieldsTimeSeries);
		wdcTimeSeriesMapping.setOperationName("timeFieldSeriesMapping");
		wdcTimeSeriesMapping.setWebhookName("git_demo");
		wdcTimeSeriesMapping.setWid(173);
		setWebhookDerivedConfigs.add(wdcTimeSeriesMapping);
		wdcDataEnrichment.setOperationFields(operationFieldsDataEnrich);
		wdcDataEnrichment.setOperationName("dataEnrichment");
		wdcDataEnrichment.setWebhookName("git_demo");
		wdcDataEnrichment.setWid(873);
		setWebhookDerivedConfigs.add(wdcDataEnrichment);

		return setWebhookDerivedConfigs;
	}

}