/*******************************************************************************
 * * Copyright 2017 Cognizant Technology Solutions
 * *
 * * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * * use this file except in compliance with the License. You may obtain a copy
 * * of the License at
 * *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * *
 * * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * * License for the specific language governing permissions and limitations
 * under
 * * the License.
 *******************************************************************************/
package com.cognizant.devops.engines.platformwebhookengine.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cognizant.devops.engines.util.EngineUtils;
import com.cognizant.devops.platformcommons.core.enums.ResultOutputType;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class InsightsDynamicJsonParser {
	private static Logger LOG = LogManager.getLogger(InsightsDynamicJsonParser.class);
	static String nodeCreationType = ResultOutputType.COMBINED_NODE.getValue();

	/*public static void main(String[] args) throws Exception {
			InsightsDynamicJsonParser mainclass = new InsightsDynamicJsonParser();
			ObjectMapper mapper = new ObjectMapper();
			ApplicationConfigCache.loadConfigCache();
	
			String genreJson_pivotal = "{\"kind\":\"story_update_activity\",\"guid\":\"2182342_9400\",\"project_version\":9400,\"message\":\"gaurav edited this feature\",\"highlight\":\"edited\",\"changes\":[{\"kind\":\"story\",\"change_type\":\"update\",\"id\":171077620,\"original_values\":{\"owner_ids\":[3133265,3086337],\"updated_at\":1584418981000},\"new_values\":{\"owner_ids\":[3133265],\"updated_at\":1584419499000},\"name\":\"Nested JSON Support for Webhooks\",\"story_type\":\"feature\"}],\"primary_resources\":[{\"kind\":\"story\",\"id\":171077620,\"name\":\"Nested JSON Support for Webhooks\",\"story_type\":\"feature\",\"url\":\"https://www.pivotaltracker.com/story/show/171077620\"}],\"secondary_resources\":[],\"project\":{\"kind\":\"project\",\"id\":2182342,\"name\":\"OneDevOps\"},\"performed_by\":{\"kind\":\"person\",\"id\":3086337,\"name\":\"gaurav\",\"initials\":\"GA\"},\"occurred_at\":1584419499000,\"webHookName\":\"PIVOTALTRACKER_65_WEBHOOK\",\"iswebhookdata\":true}";
			String git_json = "{  \"action\": \"created\",  \"content_reference\": {    \"id\": 17,    \"node_id\": \"MDE2OkNvbnRlbnRSZWZlcmVuY2UxNjA5\",    \"reference\": \"https://errors.ai/\"  },  \"repository\": {    \"id\": 145551601,    \"node_id\": \"MDEwOlJlcG9zaXRvcnkxNDU1NTE2MDE=\",    \"name\": \"hello-world\",    \"full_name\": \"octocoders/hello-world\",    \"private\": true,    \"owner\": {      \"login\": \"Codertocat\",      \"id\": 7718702,      \"node_id\": \"MDQ6VXNlcjc3MTg3MDI=\",      \"avatar_url\": \"https://avatars1.githubusercontent.com/u/7718702?v=4\",      \"gravatar_id\": \"\",      \"url\": \"https://api.github.com/users/Codertocat\",      \"html_url\": \"https://github.com/Codertocat\",      \"followers_url\": \"https://api.github.com/users/Codertocat/followers\",      \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\",      \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\",      \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\",      \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\",      \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\",      \"repos_url\": \"https://api.github.com/users/Codertocat/repos\",      \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\",      \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\",      \"type\": \"User\",      \"site_admin\": true    },    \"html_url\": \"https://github.com/Codertocat/hello-world\",    \"description\": null,    \"fork\": false,    \"url\": \"https://api.github.com/repos/Codertocat/hello-world\",    \"forks_url\": \"https://api.github.com/repos/Codertocat/hello-world/forks\",    \"keys_url\": \"https://api.github.com/repos/Codertocat/hello-world/keys{/key_id}\",    \"collaborators_url\": \"https://api.github.com/repos/Codertocat/hello-world/collaborators{/collaborator}\",    \"teams_url\": \"https://api.github.com/repos/Codertocat/hello-world/teams\",    \"hooks_url\": \"https://api.github.com/repos/Codertocat/hello-world/hooks\",    \"issue_events_url\": \"https://api.github.com/repos/Codertocat/hello-world/issues/events{/number}\",    \"events_url\": \"https://api.github.com/repos/Codertocat/hello-world/events\",    \"assignees_url\": \"https://api.github.com/repos/Codertocat/hello-world/assignees{/user}\",    \"branches_url\": \"https://api.github.com/repos/Codertocat/hello-world/branches{/branch}\",    \"tags_url\": \"https://api.github.com/repos/Codertocat/hello-world/tags\",    \"blobs_url\": \"https://api.github.com/repos/Codertocat/hello-world/git/blobs{/sha}\",    \"git_tags_url\": \"https://api.github.com/repos/Codertocat/hello-world/git/tags{/sha}\",    \"git_refs_url\": \"https://api.github.com/repos/Codertocat/hello-world/git/refs{/sha}\",    \"trees_url\": \"https://api.github.com/repos/Codertocat/hello-world/git/trees{/sha}\",    \"statuses_url\": \"https://api.github.com/repos/Codertocat/hello-world/statuses/{sha}\",    \"languages_url\": \"https://api.github.com/repos/Codertocat/hello-world/languages\",    \"stargazers_url\": \"https://api.github.com/repos/Codertocat/hello-world/stargazers\",    \"contributors_url\": \"https://api.github.com/repos/Codertocat/hello-world/contributors\",    \"subscribers_url\": \"https://api.github.com/repos/Codertocat/hello-world/subscribers\",    \"subscription_url\": \"https://api.github.com/repos/Codertocat/hello-world/subscription\",    \"commits_url\": \"https://api.github.com/repos/Codertocat/hello-world/commits{/sha}\",    \"git_commits_url\": \"https://api.github.com/repos/Codertocat/hello-world/git/commits{/sha}\",    \"comments_url\": \"https://api.github.com/repos/Codertocat/hello-world/comments{/number}\",    \"issue_comment_url\": \"https://api.github.com/repos/Codertocat/hello-world/issues/comments{/number}\",    \"contents_url\": \"https://api.github.com/repos/Codertocat/hello-world/contents/{+path}\",    \"compare_url\": \"https://api.github.com/repos/Codertocat/hello-world/compare/{base}...{head}\",    \"merges_url\": \"https://api.github.com/repos/Codertocat/hello-world/merges\",    \"archive_url\": \"https://api.github.com/repos/Codertocat/hello-world/{archive_format}{/ref}\",    \"downloads_url\": \"https://api.github.com/repos/Codertocat/hello-world/downloads\",    \"issues_url\": \"https://api.github.com/repos/Codertocat/hello-world/issues{/number}\",    \"pulls_url\": \"https://api.github.com/repos/Codertocat/hello-world/pulls{/number}\",    \"milestones_url\": \"https://api.github.com/repos/Codertocat/hello-world/milestones{/number}\",    \"notifications_url\": \"https://api.github.com/repos/Codertocat/hello-world/notifications{?since,all,participating}\",    \"labels_url\": \"https://api.github.com/repos/Codertocat/hello-world/labels{/name}\",    \"releases_url\": \"https://api.github.com/repos/Codertocat/hello-world/releases{/id}\",    \"deployments_url\": \"https://api.github.com/repos/Codertocat/hello-world/deployments\",    \"created_at\": \"2018-08-21T10:58:58Z\",    \"updated_at\": \"2018-08-21T10:59:01Z\",    \"pushed_at\": \"2018-08-21T10:59:00Z\",    \"git_url\": \"git://github.com/Codertocat/hello-world.git\",    \"ssh_url\": \"git@github.com:Codertocat/hello-world.git\",    \"clone_url\": \"https://github.com/Codertocat/hello-world.git\",    \"svn_url\": \"https://github.com/Codertocat/hello-world\",    \"homepage\": null,    \"size\": 0,    \"stargazers_count\": 0,    \"watchers_count\": 0,    \"language\": null,    \"has_issues\": true,    \"has_projects\": true,    \"has_downloads\": true,    \"has_wiki\": true,    \"has_pages\": false,    \"forks_count\": 0,    \"mirror_url\": null,    \"archived\": false,    \"open_issues_count\": 2,    \"license\": null,    \"forks\": 0,    \"open_issues\": 2,    \"watchers\": 0,    \"default_branch\": \"master\"  },  \"sender\": {    \"login\": \"Codertocat\",    \"id\": 7718702,    \"node_id\": \"MDQ6VXNlcjc3MTg3MDI=\",    \"avatar_url\": \"https://avatars1.githubusercontent.com/u/7718702?v=4\",    \"gravatar_id\": \"\",    \"url\": \"https://api.github.com/users/Codertocat\",    \"html_url\": \"https://github.com/Codertocat\",    \"followers_url\": \"https://api.github.com/users/Codertocat/followers\",    \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\",    \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\",    \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\",    \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\",    \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\",    \"repos_url\": \"https://api.github.com/users/Codertocat/repos\",    \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\",    \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\",    \"type\": \"User\",    \"site_admin\": true  },  \"installation\": {    \"id\": 371641,    \"node_id\": \"MDIzOkludGVncmF0aW9uSW5zdGFsbGF0aW9uMzcxNjQx\"  }}";
			String gitLab_json = "{    \"object_kind\":\"pipeline\",   \"object_attributes\":{       \"id\":176881,      \"ref\":\"release-oct\",      \"tag\":false,      \"sha\":\"f8c07daacb96e4cb07c50ee9f64884ca80ed8d52\",      \"before_sha\":\"f4aaf1b0d70e36765313e8c48fd0d4c11a766c09\",      \"status\":\"success\",      \"detailed_status\":\"passed with warnings\",      \"stages\":[          \"Build\",         \"Scan\",         \"Package\",         \"Publish\",         \"Dev-Deploy\",         \"Scan-Results-Check\"      ],      \"created_at\":\"2019-11-13 14:35:17 UTC\",      \"finished_at\":\"2019-11-13 14:45:15 UTC\",      \"duration\":589,      \"variables\":[       ]   },   \"user\":{       \"name\":\"Kathirvel Shanmugam\",      \"username\":\"Kathirvel.Shanmugam\",      \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"   },   \"project\":{       \"id\":1622,      \"name\":\"impact-web-app\",      \"description\":\"Impact Web App Code\",      \"web_url\":\"https://gitlab.lfg.com/TDM/DevOpsServices/impact-web-app\",      \"avatar_url\":null,      \"git_ssh_url\":\"git@gitlab.lfg.com:TDM/DevOpsServices/impact-web-app.git\",      \"git_http_url\":\"https://gitlab.lfg.com/TDM/DevOpsServices/impact-web-app.git\",      \"namespace\":\"DevOpsServices\",      \"visibility_level\":0,      \"path_with_namespace\":\"TDM/DevOpsServices/impact-web-app\",      \"default_branch\":\"master\",      \"ci_config_path\":\"\"   },   \"commit\":{       \"id\":\"f8c07daacb96e4cb07c50ee9f64884ca80ed8d52\",      \"message\":\"excel export sheet name change\",      \"timestamp\":\"2019-11-13T09:35:09-05:00\",      \"url\":\"https://gitlab.lfg.com/TDM/DevOpsServices/impact-web-app/commit/f8c07daacb96e4cb07c50ee9f64884ca80ed8d52\",      \"author\":{          \"name\":\"kajsh4\",         \"email\":\"Kathirvel.Shanmugam@lfg.com\"      }   },   \"builds\":[       {          \"id\":791981,         \"stage\":\"Scan-Results-Check\",         \"name\":\"Get-DTRScanResults\",         \"status\":\"failed\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:44:46 UTC\",         \"finished_at\":\"2019-11-13 14:45:15 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":4,            \"description\":\"awsdlgrunner01-docker\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791982,         \"stage\":\"Scan-Results-Check\",         \"name\":\"Sonar-Quality-Gate\",         \"status\":\"failed\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:44:47 UTC\",         \"finished_at\":\"2019-11-13 14:45:15 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":4,            \"description\":\"awsdlgrunner01-docker\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":\"artifacts.zip\",            \"size\":494         }      },      {          \"id\":791980,         \"stage\":\"Dev-Deploy\",         \"name\":\"Deploy\",         \"status\":\"success\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:44:35 UTC\",         \"finished_at\":\"2019-11-13 14:44:46 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":52,            \"description\":\"awsdlgrunner01-group-tdm-rhel-bash\",            \"active\":true,            \"is_shared\":false         },         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791979,         \"stage\":\"Publish\",         \"name\":\"Docker-Build\",         \"status\":\"success\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:44:11 UTC\",         \"finished_at\":\"2019-11-13 14:44:34 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":52,            \"description\":\"awsdlgrunner01-group-tdm-rhel-bash\",            \"active\":true,            \"is_shared\":false         },         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791978,         \"stage\":\"Package\",         \"name\":\"Zip\",         \"status\":\"success\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:43:18 UTC\",         \"finished_at\":\"2019-11-13 14:44:09 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":10,            \"description\":\"awsdlgrunner02-shared-docker-aws2\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":\"Zip.zip\",            \"size\":2632522         }      },      {          \"id\":791977,         \"stage\":\"Package\",         \"name\":\"FortifyScanUpload\",         \"status\":\"manual\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":null,         \"finished_at\":null,         \"when\":\"manual\",         \"manual\":true,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":null,         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791975,         \"stage\":\"Scan\",         \"name\":\"SonarScan\",         \"status\":\"success\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:38:27 UTC\",         \"finished_at\":\"2019-11-13 14:43:16 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":4,            \"description\":\"awsdlgrunner01-docker\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791976,         \"stage\":\"Scan\",         \"name\":\"Fortify\",         \"status\":\"manual\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":null,         \"finished_at\":null,         \"when\":\"manual\",         \"manual\":true,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":null,         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791973,         \"stage\":\"Build\",         \"name\":\"Build\",         \"status\":\"success\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:35:20 UTC\",         \"finished_at\":\"2019-11-13 14:38:24 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":4,            \"description\":\"awsdlgrunner01-docker\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":\"artifacts.zip\",            \"size\":2902109         }      },      {          \"id\":791974,         \"stage\":\"Build\",         \"name\":\"JasmineCoverage\",         \"status\":\"failed\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:35:20 UTC\",         \"finished_at\":\"2019-11-13 14:36:29 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":10,            \"description\":\"awsdlgrunner02-shared-docker-aws2\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      },      {          \"id\":791972,         \"stage\":\"Build\",         \"name\":\"Lint\",         \"status\":\"failed\",         \"created_at\":\"2019-11-13 14:35:17 UTC\",         \"started_at\":\"2019-11-13 14:35:20 UTC\",         \"finished_at\":\"2019-11-13 14:36:12 UTC\",         \"when\":\"on_success\",         \"manual\":false,         \"user\":{             \"name\":\"Kathirvel Shanmugam\",            \"username\":\"Kathirvel.Shanmugam\",            \"avatar_url\":\"https://secure.gravatar.com/avatar/fd8de7c39afb34ff2c4141f49c8876ba?s=80&d=identicon\"         },         \"runner\":{             \"id\":4,            \"description\":\"awsdlgrunner01-docker\",            \"active\":true,            \"is_shared\":true         },         \"artifacts_file\":{             \"filename\":null,            \"size\":null         }      }   ]}";
			String responseTemplate_pivotal = "{\"message\":\"storyDetail\",\"performed_by\":{\"name\":\"performed_by_name\",\"initials\":\"performed_by_initials\"},\"changes\":[{\"kind\":\"newKind\",\"change_type\":\"newchange_type\",\"original_values\":{\"updated_at\":\"originalupdated_at\",\"owner_ids\":\"originalowner_ids\"},\"new_values\":{\"updated_at\":\"newupdated_at\",\"owner_ids\":\"newowner_ids\"}}],\"primary_resources\":[{\"story_type\":\"story_type\",\"id\":\"pivotalId\",\"name\":\"name\"}]}";
			String gitLab_json_responseTemplate = "{\"object_kind\":\"webhookDataType\",\"object_attributes\":{\"status\":\"objectStatus\",\"detailed_status\":\"detailed_status\",\"stages\":\"new_stages\",\"created_at\":\"created_at\"},\"user\":{\"name\":\"username\",\"username\":\"userDetail\"},\"project\":{\"id\":\"projectId\",\"description\":\"description\",\"web_url\":\"web_url\"},\"commit\":{\"id\":\"commitId\",\"message\":\"message\"},\"builds\":[{\"id\":\"buildId\",\"stage\":\"buildStage\",\"when\":\"buildWhenStatus\",\"status\":\"buildStatus\",\"user\":{\"username\":\"build_username\"},\"runner\":{\"description\":\"build_description\"},\"artifacts_file\":{\"size\":\"artifacts_file_size\"}}]}";
			String pivotalResponseTemplate = "{\"message\":\"storyDetail\",\"performed_by\":{\"name\":\"performed_by_name\",\"initials\":\"performed_by_initials\"},\"changes\":[{\"kind\":\"newKind\",\"change_type\":\"newchange_type\",\"eqwew\":\"wwqewe\",\"original_values\":{\"updated_at\":\"originalupdated_at\",\"owner_ids\":\"originalowner_ids\"},\"new_values\":{\"updated_at\":\"newupdated_at\",\"owner_ids\":\"newowner_ids\"}}],\"primary_resources\":[{\"story_type\":\"story_type\",\"id\":\"pivotalId\",\"name\":\"name\"}]}";
	
			LOG.debug("================================================================");
		nodeCreationType = ResultOutputType.COMBINED_NODE.getValue();
			List<JsonObject> responceFinalDataListPivotal = new ArrayList(0);
			JsonNode nodePivotal = mapper.readTree(genreJson_pivotal);
			JsonNode nodeResponseTemplatePivotal3 = mapper.readTree(responseTemplate_pivotal);
			LOG.debug("getNodeType {} isContainerNode == {} ", nodePivotal.getNodeType(), nodePivotal.isContainerNode());
			responceFinalDataListPivotal = mainclass.parserResponseTemplate(nodeResponseTemplatePivotal3, nodePivotal);
			LOG.debug("  responceFinalDataList {} ", responceFinalDataListPivotal.toString());
		//mainclass.addDataInNeo4j(responceFinalDataListPivotal, "WEBHOOK:TESTDATAPIVOTAL");
	
			LOG.debug("================================================================");
			List<JsonObject> responceFinalDataList2GitNodeIndividualNode = new ArrayList(0);
		nodeCreationType = ResultOutputType.INDIVIDUAL_NODE.getValue();
			JsonNode node_gitLab_json = mapper.readTree(gitLab_json);
			JsonNode nodeGitLabResponseTemplate3 = mapper.readTree(gitLab_json_responseTemplate);
			responceFinalDataList2GitNodeIndividualNode = mainclass.parserResponseTemplate(nodeGitLabResponseTemplate3,
					node_gitLab_json);
			LOG.debug(" responceFinalDataList2GitNodeIndividualNode {} ", responceFinalDataList2GitNodeIndividualNode);
		//mainclass.addDataInNeo4j(responceFinalDataList2GitNodeIndividualNode, "WEBHOOK:TESTDATAGITLAB");
	
			LOG.debug("================================================================");
	
			List<JsonObject> responceFinalDataList3GitNodeCombinedNodeWithSubNode = new ArrayList(0);
		nodeCreationType = ResultOutputType.COMBINED_WITH_SUB_NODE.getValue();
			JsonNode nodegitLabjson2 = mapper.readTree(gitLab_json);
			JsonNode nodeGitLabResponseTemplate4 = mapper.readTree(gitLab_json_responseTemplate);
			responceFinalDataList3GitNodeCombinedNodeWithSubNode = mainclass
					.parserResponseTemplate(nodeGitLabResponseTemplate4, nodegitLabjson2);
			LOG.debug(" responceFinalDataList3GitNodeCombinedNodeWithSubNode {} ",
					responceFinalDataList3GitNodeCombinedNodeWithSubNode);
		//mainclass.addDataInNeo4j(responceFinalDataList3GitNodeCombinedNodeWithSubNode,
		//		"WEBHOOK:TESTDATAGITLAB_SUBNODE");
	}*/

	/***
	 * @param nodeResponseTemplate3
	 * @param nodeOriginalData
	 * @param recordHirarchy
	 */
	public List<JsonObject> parserResponseTemplate(JsonNode nodeResponseTemplate, JsonNode nodeOriginalData) {
		LOG.debug("  nodeCreationType {} getNodeType {} isContainerNode {} ", nodeCreationType,
				nodeOriginalData.getNodeType(), nodeOriginalData.isContainerNode());
		List<JsonObject> responceDataList = new ArrayList(0);
		List<JsonObject> responceFinalDataList = new ArrayList(0);
		JsonObject responceData = new JsonObject();
		try {
			//LOG.debug(" inside parser Respons eTemplate Object {} ", nodeOriginalData);
			for (Iterator<Map.Entry<String, JsonNode>> it = nodeResponseTemplate.fields(); it.hasNext();) {
				Map.Entry<String, JsonNode> field = it.next();
				String key = field.getKey();
				JsonNode valueResponse = field.getValue();
				//LOG.debug("key:  {}  value: {}  ", key, valueResponse);
				JsonNode jsonChildNode = nodeOriginalData.get(key);
				/*LOG.debug(
						" Response Template Field Name ===== key {} responseTemplatevalue {}  is {} data value type {}  ",
						key, valueResponse.asText(), jsonChildNode.asText(), jsonChildNode.getNodeType());*/
				if (jsonChildNode != null) {
					if (jsonChildNode.isArray()) {
						if (valueResponse.isArray()) {
							List<JsonObject> returnArray = parseResponseChildArray(valueResponse.get(0), jsonChildNode,
									responceData, key);
							responceDataList.addAll(returnArray);
						} else {
							parseResponseChildArray(valueResponse, jsonChildNode, responceData, key);
						}
					} else if (jsonChildNode.isObject()) {
						parseResponseChildObject(valueResponse, jsonChildNode, responceData, key);
					} else if (!jsonChildNode.isContainerNode()) {
						getNodeValue(jsonChildNode.toString(), jsonChildNode, valueResponse, responceData);
					}
				}
			}
			if (nodeCreationType.equals(ResultOutputType.INDIVIDUAL_NODE.getValue()) && !responceDataList.isEmpty()) {//createIndividualNode
				for (JsonObject jsonObject : responceDataList) {
					JsonObject mergeJson = EngineUtils.mergeTwoJson(jsonObject, responceData);
					responceFinalDataList.add(mergeJson);
				}
			} else {
				responceFinalDataList.add(responceData);
			}
		} catch (Exception e) {
			LOG.error(" Error while parsing dynamic response template {} ", e);
		}
		//LOG.debug(" responceFinalDataList {}  ", responceFinalDataList);
		return responceFinalDataList;
	}

	public List<JsonObject> parseResponseChildArray(JsonNode jsonChildResponseNode, JsonNode jsonArrayNode,
			JsonObject responceData, String jsonKey) {
		List<JsonObject> responceDataList = new ArrayList(0);
		Iterator<JsonNode> datasetElements = jsonArrayNode.iterator();
		int nodeCount = 0;
		boolean isArrayContainOnlyValueNode = isArrayContainOnlyValueNode(jsonArrayNode);
		/*LOG.debug(" inside parseResponseChildArray Array check isArrayContainOnlyValueNode {}  ",
				isArrayContainOnlyValueNode);*/
		if (!isArrayContainOnlyValueNode) {
			while (datasetElements.hasNext()) {
				JsonObject childJsonObject;
				nodeCount = nodeCount + 1;
				if (nodeCreationType.equals(ResultOutputType.INDIVIDUAL_NODE.getValue())) {
					childJsonObject = new JsonObject();//merge(childJsonObject, responceData)
				} else if (nodeCreationType.equals(ResultOutputType.COMBINED_NODE.getValue())) {
					childJsonObject = responceData;
				} else if (nodeCreationType.equals(ResultOutputType.COMBINED_WITH_SUB_NODE.getValue())) {
					childJsonObject = new JsonObject();
				} else {
					childJsonObject = new JsonObject();
				}
				JsonNode datasetElement = datasetElements.next();
				Iterator<String> datasetElementFields = datasetElement.fieldNames();
				while (datasetElementFields.hasNext()) {
					String datasetElementField = datasetElementFields.next();
					JsonNode jsonChildNode = datasetElement.get(datasetElementField);
					JsonNode jsonChildNodeResponse = jsonChildResponseNode.get(datasetElementField);
					List<JsonNode> jsonChildNodeResponseList = jsonChildResponseNode.findValues(datasetElementField);
					if (!jsonChildNodeResponseList.isEmpty() && jsonChildNode != null) {
						//LOG.debug("datasetElementField  {} ", datasetElementField);
						if (jsonChildNode.isArray()) {
							parseResponseChildArray(jsonChildNodeResponse, jsonChildNode, childJsonObject, jsonKey);

						} else if (jsonChildNode.isObject()) {
							parseResponseChildObject(jsonChildNodeResponse, jsonChildNode, childJsonObject, jsonKey);
						} else if (!jsonChildNode.isContainerNode()) {

							getNodeValue(datasetElementField, jsonChildNode, jsonChildNodeResponse, childJsonObject);
						}
					} else {
						LOG.debug("Field Not in response template or Field Not present in original data {}  ",
								datasetElementField);
					}
				}
				responceDataList.add(childJsonObject);
				if (nodeCreationType.equals(ResultOutputType.COMBINED_WITH_SUB_NODE.getValue())) {
					responceData.add(jsonKey + nodeCount, childJsonObject);
				}
			}
		} else {
			//LOG.debug("Array contain only values {} ", jsonArrayNode);
			getNodeValue(jsonChildResponseNode.asText(), jsonArrayNode, jsonChildResponseNode, responceData);
		}
		//LOG.debug("Array for build   " + responceDataList.toString());
		return responceDataList;
	}

	private void parseResponseChildObject(JsonNode jsonChildResponseNode, JsonNode jsonArrayNode,
			JsonObject responceData, String jsonKey) {
		getResposeNodeDetail(jsonChildResponseNode, jsonArrayNode, responceData, jsonKey);
	}

	public void getResposeNodeDetail(JsonNode jsonChildResponseNode, JsonNode nodeOriginalData, JsonObject responceData,
			String jsonKey) {

		for (Iterator<Map.Entry<String, JsonNode>> it = jsonChildResponseNode.fields(); it.hasNext();) {
			Map.Entry<String, JsonNode> field = it.next();
			String key = field.getKey();
			JsonNode valueResponse = field.getValue();
			//LOG.debug("key: {}  value:  {}", key, valueResponse);
			JsonNode jsonChildNode = nodeOriginalData.get(key);
			/*LOG.debug(
					"getResposeNodeDetail key: {}  value: {} parseChildArray for field name for NodeValue {}  value is {} ",
					key, valueResponse, key, jsonChildNode);*/
			if (jsonChildNode != null) {
				if (jsonChildNode.isArray()) {
					if (valueResponse.isArray()) {
						parseResponseChildArray(valueResponse.get(0), jsonChildNode, responceData, jsonKey);
					} else {
						parseResponseChildArray(valueResponse, jsonChildNode, responceData, jsonKey);
					}
				} else if (jsonChildNode.isObject()) {
					//LOG.debug(" parseChildObject for field name for NodeValue {} value is {}  ", key, valueResponse);
					parseResponseChildObject(valueResponse, jsonChildNode, responceData, jsonKey);
				} else if (!jsonChildNode.isContainerNode()) {
					getNodeValue(jsonChildNode.toString(), jsonChildNode, valueResponse, responceData);

				}
			} else {
				LOG.error(" Field Not present in original data +  {}  ", key);
			}
		}
	}

	private Object getNodeValue(String key, JsonNode valueNode, JsonNode jsonChildResponseNode,
			JsonObject responceData) {
		Object value = null;
		if (!valueNode.getNodeType().toString().equalsIgnoreCase("NULL")) {
			if (valueNode.isBoolean()) {
				value = valueNode.asBoolean();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asBoolean());
			} else if (valueNode.isLong()) {
				value = valueNode.asLong();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asLong());
			} else if (valueNode.isDouble()) {
				value = valueNode.asDouble();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asDouble());
			} else if (valueNode.isTextual()) {
				value = valueNode.asText();
				responceData.addProperty(jsonChildResponseNode.asText(), valueNode.asText());
			} else {
				if (valueNode.isArray()) {
					JsonParser parser = new JsonParser();
					JsonElement tradeElement = parser.parse(valueNode.toString());
					JsonArray trade = tradeElement.getAsJsonArray();
					responceData.add(jsonChildResponseNode.asText(), trade);
				} else {
					value = valueNode.toString();
					responceData.addProperty(jsonChildResponseNode.asText(), valueNode.toString());
				}
			}
			LOG.debug(
					" getNodeValue .. only isValueNode  {}  value is {}  data type value {} response template filed {} ",
					key, value, valueNode.getNodeType(), jsonChildResponseNode.asText());
		} else {
			LOG.debug(" getNodeValue Node is null so skipping that node {}   value is {} ",
					jsonChildResponseNode.asText(), valueNode);
		}
		return value;
	}

	private boolean isArrayContainOnlyValueNode(JsonNode jsonArrayNode) {
		boolean returnvalue = Boolean.FALSE;
		Iterator<JsonNode> datasetElements = jsonArrayNode.iterator();
		while (datasetElements.hasNext()) {
			JsonNode datasetElement = datasetElements.next();
			if (datasetElement.isValueNode()) {
				returnvalue = Boolean.TRUE;
				break;
			}
		}
		return returnvalue;
	}

	/*private void addDataInNeo4j(List<JsonObject> webhookToolData, String labelName) {
		try {
			LOG.debug(" in addDataInNeo4j for lable {} ", labelName);
			Neo4jDBHandler dbHandler = new Neo4jDBHandler();
			String cypherQuery = "UNWIND {props} AS properties CREATE (n:" + labelName.toUpperCase()
					+ ") set n=properties ";//return count(n)
			JsonObject graphResponse = dbHandler.executeQueryWithData(cypherQuery, webhookToolData);
			if (graphResponse.get("response").getAsJsonObject().get("errors").getAsJsonArray().size() > 0) {
				LOG.error("Unable to insert nodes for routing key: {} , error occured: {} ", labelName, graphResponse);
			}
		} catch (Exception e) {
			LOG.error(" Error while saving data in Neo4j {} ", e.getMessage());
		}
	}*/

	/*private void processJson(JsonElement jsonElement) {
		List<JsonElement> list = new ArrayList<JsonElement>();
		if (jsonElement.isJsonNull()) {
			LOG.debug("Null value " + jsonElement);
		} else if (jsonElement.isJsonArray()) {
	
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			// log.error("Json Array found " + jsonArray.size());
			if (jsonArray.size() > 0) {
				for (JsonElement jsonArrayElement : jsonArray) {
					if (jsonArrayElement.isJsonObject()) {
						list.addAll(Arrays.asList(jsonArrayElement));
						processJson(jsonArrayElement);
					} else {
						list.addAll(Arrays.asList(jsonArrayElement));
	
					}
				}
			} else {
				// log.debug("Null value" + jsonArray);
			}
		} else {
			LOG.debug(jsonElement);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			LOG.debug(jsonObject);
			if (!jsonObject.isJsonNull()) {
	
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
	
					if (entry.getValue().isJsonNull()) {
	
					} else if (entry.getValue().isJsonArray()) {
						processJson(entry.getValue());
	
					} else if (!entry.getValue().isJsonPrimitive()) {
						parseJsonPrimitive(entry);
					} else {
	
					}
	
				}
			}
		}
		LOG.debug(list);
	}*/

	/*private void parseJsonPrimitive(Map.Entry<String, JsonElement> entry) {
	
		if (entry.getValue().isJsonArray()) {
			processJson(entry.getValue());
		} else if (!entry.getValue().isJsonNull()) {
	
			JsonObject jsonObjectInternal = entry.getValue().getAsJsonObject();
			for (Map.Entry<String, JsonElement> entryAgain : jsonObjectInternal.entrySet()) {
				if (entry.getValue().isJsonArray()) {
					processJson(entry.getValue());
				} else if (!entryAgain.getValue().isJsonNull() && !entryAgain.getValue().isJsonPrimitive()) {
					parseJsonPrimitive(entryAgain);
				} else {
	
				}
			}
		} else {
			LOG.debug(entry.getKey());
		}
	}*/

}
