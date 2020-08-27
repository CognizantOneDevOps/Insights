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
import java.time.format.DateTimeFormatter;

public class InsightsParserTestData {

	int defaultInterval = 600;
	String toolName = "PIVOTAL";
	String labelName = "ALM:PIVOTAL76:DATA";
	String mqChannel = "IPW_pivotal_webhook_parser_test";
	String webhookName = "pivotal_webhook_parser_test";
	String incorrectToolData = "tooldata=incorrcet";
	String emptyDynamicTemplate = null;
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	LocalDateTime now = LocalDateTime.now();
	String fieldNotFoundinToolData = "head_commit.newid=commitId,head_commit.newmessage=message";
	String toolDataWithArray = "{\"kind\":\"blocker_create_activity\",\"guid\":\"2182342_9453\",\"created_at\":\"2020-03-11T18:17:37+05:30\",\"project_version\":9453,\"message\":\"surbhigupta1 added a blocker: \\\"https://www.pivotaltracker.com/story/show/171787663\\\"\",\"highlight\":\"added a blocker\",\"changes\":[{\"kind\":\"blocker\",\"change_type\":\"create\",\"id\":2197991,\"original_values\":{\"owner_ids\":[3133265,3086337,899032323],\"updated_at\":1584418981000},\"new_values\":{\"id\":2197991,\"story_id\":171874214,\"person_id\":3133265,\"description\":\"https://www.pivotaltracker.com/story/show/171787663\",\"resolved\":false,\"owner_ids\":[3133265,89343233],\"created_at\":1584610689000,\"updated_at\":1584610689000,\"value\":341332.3223}},{\"kind\":\"story\",\"change_type\":\"update\",\"id\":171874214,\"original_values\":{\"updated_at\":1584337896000,\"blocked_story_ids\":[]},\"new_values\":{\"updated_at\":1584610689000,\"blocked_story_ids\":[171874214]},\"name\":\"Webhook Testing Story\",\"story_type\":\"bug\"},{\"kind\":\"story\",\"change_type\":\"update\",\"id\":171874214,\"original_values\":{\"updated_at\":1584610516000},\"new_values\":{\"updated_at\":1584610689000},\"name\":\"Webhook Testing Story\",\"story_type\":\"bug\"}],\"primary_resources\":[{\"kind\":\"story\",\"id\":171874214,\"name\":\"Webhook Testing Story\",\"story_type\":\"bug\",\"url\":\"https://www.pivotaltracker.com/story/show/171874214\"}],\"secondary_resources\":[{\"kind\":\"secondary_resource\",\"message\":\"This story is blocking #171874214: \\\"https://www.pivotaltracker.com/story/show/171787663\\\"\",\"highlight\":\"is blocking\",\"resource\":{\"kind\":\"story\",\"id\":171787663,\"name\":\"WebHookFaultTolerance Artifacts not create with correct name in slack\",\"story_type\":\"bug\"}}],\"project\":{\"kind\":\"project\",\"id\":2182342,\"name\":\"OneDevOps\"},\"performed_by\":{\"kind\":\"person\",\"id\":3133265,\"name\":\"surbhigupta1\",\"initials\":\"su\"},\"occurred_at\":1584610689000,\"webHookName\":\"PIVOTALTRACKER_65_WEBHOOK\",\"iswebhookdata\":true}";
	String responseTemplateForArray = "project.id=projectId,kind=story_state,occurred_at=pivotalTime,message=message";
	String dynamicTemplateWithArray = "{\"message\":\"storyDetail\",\"performed_by\":{\"name\":\"performed_by_name\",\"initials\":\"performed_by_initials\"},\"changes\":[{\"kind\":\"newKind\",\"change_type\":\"newchange_type\",\"accepted_at\":\"story_accepted_at\",\"original_values\":{\"updated_at\":\"originalupdated_at\",\"owner_ids\":\"originalowner_ids\"},\"new_values\":{\"updated_at\":\"newupdated_at\",\"owner_ids\":\"newowner_ids\",\"person_id\":\"person_id\",\"review_type_id\":\"review_type_id\",\"resolved\":\"newresolved\",\"created_at\":\"newcreated_at\",\"value\":\"newvalue\",\"label_ids\":\"label_ids\"}}],\"primary_resources\":[{\"story_type\":\"story_type\",\"id\":\"pivotalId\",\"name\":\"name\"}],\"secondary_resources\":[{\"resource\":{\"id\":\"blockerStoryId\"}}]}";
	String fieldUsedForUpdate = "pivotalId";
}