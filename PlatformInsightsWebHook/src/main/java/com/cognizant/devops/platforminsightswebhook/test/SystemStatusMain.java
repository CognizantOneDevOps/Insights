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
package com.cognizant.devops.platforminsightswebhook.test;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.cognizant.devops.platforminsightswebhook.config.WebHookMessagePublisher;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

public class SystemStatusMain {
	private static Logger LOG = LogManager.getLogger(SystemStatusMain.class);
	@Autowired
	private Gson gson;

	public static void main(String[] args) {
		LOG.debug(" Run Event Subscriber .....");
		SystemStatusMain ssm = new SystemStatusMain();
		String data = ssm.createMessage();
		String[] arr = { "GIT_commit_comment", "GIT_push_event", "GIT_event", "GIT_Watches", "GIT_Watches2" };
		Random r = new Random();
		for (int i = 0; i < 4000; i++) {
			int randomNumber = r.nextInt(arr.length);
			String webHookName = arr[randomNumber];
			LOG.debug(" webHookName ==== " + webHookName + "==== request number ====" + i);
			jerseyPostClientWithAuthentication(
					"http://localhost:8981/PlatformInsightsWebHook/insightsDevOpsWebHook?webHookName=" + webHookName,
					null, null, null, data);
		}
		LOG.debug(" Run Event Subscriber complete .....");

	}

	public static String jerseyPostClientWithAuthentication(String url, String name, String password, String authtoken,
			String data) {
		String output;
		String authStringEnc;
		ClientResponse response = null;
		try {
			if (authtoken == null) {
				String authString = name + ":" + password;
				authStringEnc = new BASE64Encoder().encode(authString.getBytes());
			} else {
				authStringEnc = authtoken;
			}
			JsonParser parser = new JsonParser();
			JsonElement dataJson = parser.parse(data);//new Gson().fromJson(data, JsonElement.class)
			Client restClient = Client.create();
			WebResource webResource = restClient.resource(url);
			response = webResource.type("application/json")
					//.header("Authorization", "Basic " + authStringEnc) .accept("application/json")
					.post(ClientResponse.class, data);//"{aa}"  dataJson.toString()
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			} else {
				output = response.getEntity(String.class);
			}
			System.out.print(" response code " + response.getStatus() + "  output  " + output);
		} catch (Exception e) {
			//e.printStackTrace();
			LOG.debug(" error while getGetting  jerseyPostClientWithAuthentication " + e.getMessage());
			throw new RuntimeException(
					"Failed : error while getGetting jerseyPostClientWithAuthentication : " + e.getMessage());
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return output;
	}

	public String createMessage() {
		String message = "{      \"ref\": \"refs/heads/master\",      \"before\": \"91f85cd59e01a5cbd269cdeb675e73698d4e6feb\",      \"after\": \"192552bd7088ce5317ec1dd0488f0b49cf052f27\",      \"created\": false,      \"deleted\": false,      \"forced\": false,      \"base_ref\": null,      \"compare\": \"https://github.com/gauravdeshmukh612/insightTest/compare/91f85cd59e01...192552bd7088\",      \"commits\": [        {          \"id\": \"192552bd7088ce5317ec1dd0488f0b49cf052f27\",          \"tree_id\": \"657c54815e68737242ea219aae4db0905e542dee\",          \"distinct\": true,          \"message\": \"trtrttrttt\\n\\nfgsfdgfgsfggfgg\",          \"timestamp\": \"2019-06-27T14:19:55+05:30\",          \"url\": \"https://github.com/gauravdeshmukh612/insightTest/commit/192552bd7088ce5317ec1dd0488f0b49cf052f27\",          \"author\": {            \"name\": \"gauravdeshmukh612\",            \"email\": \"41290302+gauravdeshmukh612@users.noreply.github.com\",            \"username\": \"gauravdeshmukh612\"          },          \"committer\": {            \"name\": \"GitHub\",            \"email\": \"noreply@github.com\",            \"username\": \"web-flow\"          },          \"added\": [            ],          \"removed\": [            ],          \"modified\": [            \"config.json\"          ]        }      ],      \"head_commit\": {        \"id\": \"192552bd7088ce5317ec1dd0488f0b49cf052f27\",        \"tree_id\": \"657c54815e68737242ea219aae4db0905e542dee\",        \"distinct\": true,        \"message\": \"trtrttrttt\\n\\nfgsfdgfgsfggfgg\",        \"timestamp\": \"2019-06-27T14:19:55+05:30\",        \"url\": \"https://github.com/gauravdeshmukh612/insightTest/commit/192552bd7088ce5317ec1dd0488f0b49cf052f27\",        \"author\": {          \"name\": \"gauravdeshmukh612\",          \"email\": \"41290302+gauravdeshmukh612@users.noreply.github.com\",          \"username\": \"gauravdeshmukh612\"        },        \"committer\": {          \"name\": \"GitHub\",          \"email\": \"noreply@github.com\",          \"username\": \"web-flow\"        },        \"added\": [          ],        \"removed\": [          ],        \"modified\": [          \"config.json\"        ]      },      \"repository\": {        \"id\": 141991164,        \"node_id\": \"MDEwOlJlcG9zaXRvcnkxNDE5OTExNjQ=\",        \"name\": \"insightTest\",        \"full_name\": \"gauravdeshmukh612/insightTest\",        \"private\": false,        \"owner\": {          \"name\": \"gauravdeshmukh612\",          \"email\": \"41290302+gauravdeshmukh612@users.noreply.github.com\",          \"login\": \"gauravdeshmukh612\",          \"id\": 41290302,          \"node_id\": \"MDQ6VXNlcjQxMjkwMzAy\",          \"avatar_url\": \"https://avatars0.githubusercontent.com/u/41290302?v=4\",          \"gravatar_id\": \"\",          \"url\": \"https://api.github.com/users/gauravdeshmukh612\",          \"html_url\": \"https://github.com/gauravdeshmukh612\",          \"followers_url\": \"https://api.github.com/users/gauravdeshmukh612/followers\",          \"following_url\": \"https://api.github.com/users/gauravdeshmukh612/following{/other_user}\",          \"gists_url\": \"https://api.github.com/users/gauravdeshmukh612/gists{/gist_id}\",          \"starred_url\": \"https://api.github.com/users/gauravdeshmukh612/starred{/owner}{/repo}\",          \"subscriptions_url\": \"https://api.github.com/users/gauravdeshmukh612/subscriptions\",          \"organizations_url\": \"https://api.github.com/users/gauravdeshmukh612/orgs\",          \"repos_url\": \"https://api.github.com/users/gauravdeshmukh612/repos\",          \"events_url\": \"https://api.github.com/users/gauravdeshmukh612/events{/privacy}\",          \"received_events_url\": \"https://api.github.com/users/gauravdeshmukh612/received_events\",          \"type\": \"User\",          \"site_admin\": false        },        \"html_url\": \"https://github.com/gauravdeshmukh612/insightTest\",        \"description\": \"insightTest\",        \"fork\": false,        \"url\": \"https://github.com/gauravdeshmukh612/insightTest\",        \"forks_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/forks\",        \"keys_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/keys{/key_id}\",        \"collaborators_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/collaborators{/collaborator}\",        \"teams_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/teams\",        \"hooks_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/hooks\",        \"issue_events_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/issues/events{/number}\",        \"events_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/events\",        \"assignees_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/assignees{/user}\",        \"branches_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/branches{/branch}\",        \"tags_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/tags\",        \"blobs_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/blobs{/sha}\",        \"git_tags_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/tags{/sha}\",        \"git_refs_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/refs{/sha}\",        \"trees_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/trees{/sha}\",        \"statuses_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/statuses/{sha}\",        \"languages_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/languages\",        \"stargazers_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/stargazers\",        \"contributors_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/contributors\",        \"subscribers_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/subscribers\",        \"subscription_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/subscription\",        \"commits_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/commits{/sha}\",        \"git_commits_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/commits{/sha}\",        \"comments_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/comments{/number}\",        \"issue_comment_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/issues/comments{/number}\",        \"contents_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/contents/{+path}\",        \"compare_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/compare/{base}...{head}\",        \"merges_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/merges\",        \"archive_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/{archive_format}{/ref}\",        \"downloads_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/downloads\",        \"issues_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/issues{/number}\",        \"pulls_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/pulls{/number}\",        \"milestones_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/milestones{/number}\",        \"notifications_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/notifications{?since,all,participating}\",        \"labels_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/labels{/name}\",        \"releases_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/releases{/id}\",        \"deployments_url\": \"https://api.github.com/repos/gauravdeshmukh612/insightTest/deployments\",        \"created_at\": 1532337586,        \"updated_at\": \"2019-06-21T05:58:48Z\",        \"pushed_at\": 1561625396,        \"git_url\": \"git://github.com/gauravdeshmukh612/insightTest.git\",        \"ssh_url\": \"git@github.com:gauravdeshmukh612/insightTest.git\",        \"clone_url\": \"https://github.com/gauravdeshmukh612/insightTest.git\",        \"svn_url\": \"https://github.com/gauravdeshmukh612/insightTest\",        \"homepage\": null,        \"size\": 43,        \"stargazers_count\": 0,        \"watchers_count\": 0,        \"language\": \"Python\",        \"has_issues\": true,        \"has_projects\": true,        \"has_downloads\": true,        \"has_wiki\": true,        \"has_pages\": false,        \"forks_count\": 0,        \"mirror_url\": null,        \"archived\": false,        \"disabled\": false,        \"open_issues_count\": 0,        \"license\": null,        \"forks\": 0,        \"open_issues\": 0,        \"watchers\": 0,        \"default_branch\": \"master\",        \"stargazers\": 0,        \"master_branch\": \"master\"      },      \"pusher\": {        \"name\": \"gauravdeshmukh612\",        \"email\": \"41290302+gauravdeshmukh612@users.noreply.github.com\"      },      \"sender\": {        \"login\": \"gauravdeshmukh612\",        \"id\": 41290302,        \"node_id\": \"MDQ6VXNlcjQxMjkwMzAy\",        \"avatar_url\": \"https://avatars0.githubusercontent.com/u/41290302?v=4\",        \"gravatar_id\": \"\",        \"url\": \"https://api.github.com/users/gauravdeshmukh612\",        \"html_url\": \"https://github.com/gauravdeshmukh612\",        \"followers_url\": \"https://api.github.com/users/gauravdeshmukh612/followers\",        \"following_url\": \"https://api.github.com/users/gauravdeshmukh612/following{/other_user}\",        \"gists_url\": \"https://api.github.com/users/gauravdeshmukh612/gists{/gist_id}\",        \"starred_url\": \"https://api.github.com/users/gauravdeshmukh612/starred{/owner}{/repo}\",        \"subscriptions_url\": \"https://api.github.com/users/gauravdeshmukh612/subscriptions\",        \"organizations_url\": \"https://api.github.com/users/gauravdeshmukh612/orgs\",        \"repos_url\": \"https://api.github.com/users/gauravdeshmukh612/repos\",        \"events_url\": \"https://api.github.com/users/gauravdeshmukh612/events{/privacy}\",        \"received_events_url\": \"https://api.github.com/users/gauravdeshmukh612/received_events\",        \"type\": \"User\",        \"site_admin\": false      }    }";
		return message;
	}

}
