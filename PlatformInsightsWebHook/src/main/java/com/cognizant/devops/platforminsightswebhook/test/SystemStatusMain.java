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
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

// import sun.misc.BASE64Encoder;

public class SystemStatusMain {
	private static Logger LOG = LogManager.getLogger(SystemStatusMain.class);


	public static void main(String[] args) {
		LOG.debug(" Run Event Subscriber .....");
		SystemStatusMain ssm = new SystemStatusMain();
		String data = ssm.createMessage();
		String[] arr = { "git_test", "GIT_push" };
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
				authStringEnc = "bnVsbDpudWxs";//new BASE64Encoder().encode(authString.getBytes())
			} else {
				authStringEnc = authtoken;
			}
			//LOG.debug("authStringEnc ====" + authStringEnc);
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
		String message = "{  \"action\": \"created\",  \"comment\": {  \"url\": \"https://api.github.com/repos/Codertocat/Hello-World/comments/33548674\",  \"html_url\": \"https://github.com/Codertocat/Hello-World/commit/6113728f27ae82c7b1a177c8d03f9e96e0adf246#commitcomment-33548674\",  \"id\": 33548674,  \"node_id\": \"MDEzOkNvbW1pdENvbW1lbnQzMzU0ODY3NA==\",  \"user\": { \"login\": \"Codertocat\", \"id\": 21031067, \"node_id\": \"MDQ6VXNlcjIxMDMxMDY3\", \"avatar_url\": \"https://avatars1.githubusercontent.com/u/21031067?v=4\", \"gravatar_id\": \"\", \"url\": \"https://api.github.com/users/Codertocat\", \"html_url\": \"https://github.com/Codertocat\", \"followers_url\": \"https://api.github.com/users/Codertocat/followers\", \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\", \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\", \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\", \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\", \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\", \"repos_url\": \"https://api.github.com/users/Codertocat/repos\", \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\", \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\", \"type\": \"User\", \"site_admin\": false  },  \"position\": null,  \"line\": null,  \"path\": null,  \"commit_id\": \"6113728f27ae82c7b1a177c8d03f9e96e0adf246\",  \"created_at\": \"2019-05-15T15:20:39Z\",  \"updated_at\": \"2019-05-15T15:20:39Z\",  \"author_association\": \"OWNER\",  \"body\": \"This is a really good change! :+1:\"  },  \"repository\": {  \"id\": 186853002,  \"node_id\": \"MDEwOlJlcG9zaXRvcnkxODY4NTMwMDI=\",  \"name\": \"Hello-World\",  \"full_name\": \"Codertocat/Hello-World\",  \"private\": false,  \"owner\": { \"login\": \"Codertocat\", \"id\": 21031067, \"node_id\": \"MDQ6VXNlcjIxMDMxMDY3\", \"avatar_url\": \"https://avatars1.githubusercontent.com/u/21031067?v=4\", \"gravatar_id\": \"\", \"url\": \"https://api.github.com/users/Codertocat\", \"html_url\": \"https://github.com/Codertocat\", \"followers_url\": \"https://api.github.com/users/Codertocat/followers\", \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\", \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\", \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\", \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\", \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\", \"repos_url\": \"https://api.github.com/users/Codertocat/repos\", \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\", \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\", \"type\": \"User\", \"site_admin\": false  },  \"html_url\": \"https://github.com/Codertocat/Hello-World\",  \"description\": null,  \"fork\": false,  \"url\": \"https://api.github.com/repos/Codertocat/Hello-World\",  \"forks_url\": \"https://api.github.com/repos/Codertocat/Hello-World/forks\",  \"keys_url\": \"https://api.github.com/repos/Codertocat/Hello-World/keys{/key_id}\",  \"collaborators_url\": \"https://api.github.com/repos/Codertocat/Hello-World/collaborators{/collaborator}\",  \"teams_url\": \"https://api.github.com/repos/Codertocat/Hello-World/teams\",  \"hooks_url\": \"https://api.github.com/repos/Codertocat/Hello-World/hooks\",  \"issue_events_url\": \"https://api.github.com/repos/Codertocat/Hello-World/issues/events{/number}\",  \"events_url\": \"https://api.github.com/repos/Codertocat/Hello-World/events\",  \"assignees_url\": \"https://api.github.com/repos/Codertocat/Hello-World/assignees{/user}\",  \"branches_url\": \"https://api.github.com/repos/Codertocat/Hello-World/branches{/branch}\",  \"tags_url\": \"https://api.github.com/repos/Codertocat/Hello-World/tags\",  \"blobs_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/blobs{/sha}\",  \"git_tags_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/tags{/sha}\",  \"git_refs_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/refs{/sha}\",  \"trees_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/trees{/sha}\",  \"statuses_url\": \"https://api.github.com/repos/Codertocat/Hello-World/statuses/{sha}\",  \"languages_url\": \"https://api.github.com/repos/Codertocat/Hello-World/languages\",  \"stargazers_url\": \"https://api.github.com/repos/Codertocat/Hello-World/stargazers\",  \"contributors_url\": \"https://api.github.com/repos/Codertocat/Hello-World/contributors\",  \"subscribers_url\": \"https://api.github.com/repos/Codertocat/Hello-World/subscribers\",  \"subscription_url\": \"https://api.github.com/repos/Codertocat/Hello-World/subscription\",  \"commits_url\": \"https://api.github.com/repos/Codertocat/Hello-World/commits{/sha}\",  \"git_commits_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/commits{/sha}\",  \"comments_url\": \"https://api.github.com/repos/Codertocat/Hello-World/comments{/number}\",  \"issue_comment_url\": \"https://api.github.com/repos/Codertocat/Hello-World/issues/comments{/number}\",  \"contents_url\": \"https://api.github.com/repos/Codertocat/Hello-World/contents/{+path}\",  \"compare_url\": \"https://api.github.com/repos/Codertocat/Hello-World/compare/{base}...{head}\",  \"merges_url\": \"https://api.github.com/repos/Codertocat/Hello-World/merges\",  \"archive_url\": \"https://api.github.com/repos/Codertocat/Hello-World/{archive_format}{/ref}\",  \"downloads_url\": \"https://api.github.com/repos/Codertocat/Hello-World/downloads\",  \"issues_url\": \"https://api.github.com/repos/Codertocat/Hello-World/issues{/number}\",  \"pulls_url\": \"https://api.github.com/repos/Codertocat/Hello-World/pulls{/number}\",  \"milestones_url\": \"https://api.github.com/repos/Codertocat/Hello-World/milestones{/number}\",  \"notifications_url\": \"https://api.github.com/repos/Codertocat/Hello-World/notifications{?since,all,participating}\",  \"labels_url\": \"https://api.github.com/repos/Codertocat/Hello-World/labels{/name}\",  \"releases_url\": \"https://api.github.com/repos/Codertocat/Hello-World/releases{/id}\",  \"deployments_url\": \"https://api.github.com/repos/Codertocat/Hello-World/deployments\",  \"created_at\": \"2019-05-15T15:19:25Z\",  \"updated_at\": \"2019-05-15T15:20:34Z\",  \"pushed_at\": \"2019-05-15T15:20:33Z\",  \"git_url\": \"git://github.com/Codertocat/Hello-World.git\",  \"ssh_url\": \"git@github.com:Codertocat/Hello-World.git\",  \"clone_url\": \"https://github.com/Codertocat/Hello-World.git\",  \"svn_url\": \"https://github.com/Codertocat/Hello-World\",  \"homepage\": null,  \"size\": 0,  \"stargazers_count\": 0,  \"watchers_count\": 0,  \"language\": \"Ruby\",  \"has_issues\": true,  \"has_projects\": true,  \"has_downloads\": true,  \"has_wiki\": true,  \"has_pages\": true,  \"forks_count\": 0,  \"mirror_url\": null,  \"archived\": false,  \"disabled\": false,  \"open_issues_count\": 2,  \"license\": null,  \"forks\": 0,  \"open_issues\": 2,  \"watchers\": 0,  \"default_branch\": \"master\"  },  \"sender\": {  \"login\": \"Codertocat\",  \"id\": 21031067,  \"node_id\": \"MDQ6VXNlcjIxMDMxMDY3\",  \"avatar_url\": \"https://avatars1.githubusercontent.com/u/21031067?v=4\",  \"gravatar_id\": \"\",  \"url\": \"https://api.github.com/users/Codertocat\",  \"html_url\": \"https://github.com/Codertocat\",  \"followers_url\": \"https://api.github.com/users/Codertocat/followers\",  \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\",  \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\",  \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\",  \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\",  \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\",  \"repos_url\": \"https://api.github.com/users/Codertocat/repos\",  \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\",  \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\",  \"type\": \"User\",  \"site_admin\": false  }  }";
		String maessage2 = "{  \"ref\": \"refs/tags/simple-tag\",  \"before\": \"6113728f27ae82c7b1a177c8d03f9e96e0adf246\",  \"after\": \"0000000000000000000000000000000000000000\",  \"created\": false,  \"deleted\": true,  \"forced\": false,  \"base_ref\": null,  \"compare\": \"https://github.com/Codertocat/Hello-World/compare/6113728f27ae...000000000000\",  \"commits\": [  ],  \"head_commit\": null,  \"repository\": {  \"id\": 186853002,  \"node_id\": \"MDEwOlJlcG9zaXRvcnkxODY4NTMwMDI=\",  \"name\": \"Hello-World\",  \"full_name\": \"Codertocat/Hello-World\",  \"private\": false,  \"owner\": {  \"name\": \"Codertocat\",  \"email\": \"21031067+Codertocat@users.noreply.github.com\",  \"login\": \"Codertocat\",  \"id\": 21031067,  \"node_id\": \"MDQ6VXNlcjIxMDMxMDY3\",  \"avatar_url\": \"https://avatars1.githubusercontent.com/u/21031067?v=4\",  \"gravatar_id\": \"\",  \"url\": \"https://api.github.com/users/Codertocat\",  \"html_url\": \"https://github.com/Codertocat\",  \"followers_url\": \"https://api.github.com/users/Codertocat/followers\",  \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\",  \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\",  \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\",  \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\",  \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\",  \"repos_url\": \"https://api.github.com/users/Codertocat/repos\",  \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\",  \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\",  \"type\": \"User\",  \"site_admin\": false  },  \"html_url\": \"https://github.com/Codertocat/Hello-World\",  \"description\": null,  \"fork\": false,  \"url\": \"https://github.com/Codertocat/Hello-World\",  \"forks_url\": \"https://api.github.com/repos/Codertocat/Hello-World/forks\",  \"keys_url\": \"https://api.github.com/repos/Codertocat/Hello-World/keys{/key_id}\",  \"collaborators_url\": \"https://api.github.com/repos/Codertocat/Hello-World/collaborators{/collaborator}\",  \"teams_url\": \"https://api.github.com/repos/Codertocat/Hello-World/teams\",  \"hooks_url\": \"https://api.github.com/repos/Codertocat/Hello-World/hooks\",  \"issue_events_url\": \"https://api.github.com/repos/Codertocat/Hello-World/issues/events{/number}\",  \"events_url\": \"https://api.github.com/repos/Codertocat/Hello-World/events\",  \"assignees_url\": \"https://api.github.com/repos/Codertocat/Hello-World/assignees{/user}\",  \"branches_url\": \"https://api.github.com/repos/Codertocat/Hello-World/branches{/branch}\",  \"tags_url\": \"https://api.github.com/repos/Codertocat/Hello-World/tags\",  \"blobs_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/blobs{/sha}\",  \"git_tags_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/tags{/sha}\",  \"git_refs_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/refs{/sha}\",  \"trees_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/trees{/sha}\",  \"statuses_url\": \"https://api.github.com/repos/Codertocat/Hello-World/statuses/{sha}\",  \"languages_url\": \"https://api.github.com/repos/Codertocat/Hello-World/languages\",  \"stargazers_url\": \"https://api.github.com/repos/Codertocat/Hello-World/stargazers\",  \"contributors_url\": \"https://api.github.com/repos/Codertocat/Hello-World/contributors\",  \"subscribers_url\": \"https://api.github.com/repos/Codertocat/Hello-World/subscribers\",  \"subscription_url\": \"https://api.github.com/repos/Codertocat/Hello-World/subscription\",  \"commits_url\": \"https://api.github.com/repos/Codertocat/Hello-World/commits{/sha}\",  \"git_commits_url\": \"https://api.github.com/repos/Codertocat/Hello-World/git/commits{/sha}\",  \"comments_url\": \"https://api.github.com/repos/Codertocat/Hello-World/comments{/number}\",  \"issue_comment_url\": \"https://api.github.com/repos/Codertocat/Hello-World/issues/comments{/number}\",  \"contents_url\": \"https://api.github.com/repos/Codertocat/Hello-World/contents/{+path}\",  \"compare_url\": \"https://api.github.com/repos/Codertocat/Hello-World/compare/{base}...{head}\",  \"merges_url\": \"https://api.github.com/repos/Codertocat/Hello-World/merges\",  \"archive_url\": \"https://api.github.com/repos/Codertocat/Hello-World/{archive_format}{/ref}\",  \"downloads_url\": \"https://api.github.com/repos/Codertocat/Hello-World/downloads\",  \"issues_url\": \"https://api.github.com/repos/Codertocat/Hello-World/issues{/number}\",  \"pulls_url\": \"https://api.github.com/repos/Codertocat/Hello-World/pulls{/number}\",  \"milestones_url\": \"https://api.github.com/repos/Codertocat/Hello-World/milestones{/number}\",  \"notifications_url\": \"https://api.github.com/repos/Codertocat/Hello-World/notifications{?since,all,participating}\",  \"labels_url\": \"https://api.github.com/repos/Codertocat/Hello-World/labels{/name}\",  \"releases_url\": \"https://api.github.com/repos/Codertocat/Hello-World/releases{/id}\",  \"deployments_url\": \"https://api.github.com/repos/Codertocat/Hello-World/deployments\",  \"created_at\": 1557933565,  \"updated_at\": \"2019-05-15T15:20:41Z\",  \"pushed_at\": 1557933657,  \"git_url\": \"git://github.com/Codertocat/Hello-World.git\",  \"ssh_url\": \"git@github.com:Codertocat/Hello-World.git\",  \"clone_url\": \"https://github.com/Codertocat/Hello-World.git\",  \"svn_url\": \"https://github.com/Codertocat/Hello-World\",  \"homepage\": null,  \"size\": 0,  \"stargazers_count\": 0,  \"watchers_count\": 0,  \"language\": \"Ruby\",  \"has_issues\": true,  \"has_projects\": true,  \"has_downloads\": true,  \"has_wiki\": true,  \"has_pages\": true,  \"forks_count\": 1,  \"mirror_url\": null,  \"archived\": false,  \"disabled\": false,  \"open_issues_count\": 2,  \"license\": null,  \"forks\": 1,  \"open_issues\": 2,  \"watchers\": 0,  \"default_branch\": \"master\",  \"stargazers\": 0,  \"master_branch\": \"master\"  },  \"pusher\": {  \"name\": \"Codertocat\",  \"email\": \"21031067+Codertocat@users.noreply.github.com\"  },  \"sender\": {  \"login\": \"Codertocat\",  \"id\": 21031067,  \"node_id\": \"MDQ6VXNlcjIxMDMxMDY3\",  \"avatar_url\": \"https://avatars1.githubusercontent.com/u/21031067?v=4\",  \"gravatar_id\": \"\",  \"url\": \"https://api.github.com/users/Codertocat\",  \"html_url\": \"https://github.com/Codertocat\",  \"followers_url\": \"https://api.github.com/users/Codertocat/followers\",  \"following_url\": \"https://api.github.com/users/Codertocat/following{/other_user}\",  \"gists_url\": \"https://api.github.com/users/Codertocat/gists{/gist_id}\",  \"starred_url\": \"https://api.github.com/users/Codertocat/starred{/owner}{/repo}\",  \"subscriptions_url\": \"https://api.github.com/users/Codertocat/subscriptions\",  \"organizations_url\": \"https://api.github.com/users/Codertocat/orgs\",  \"repos_url\": \"https://api.github.com/users/Codertocat/repos\",  \"events_url\": \"https://api.github.com/users/Codertocat/events{/privacy}\",  \"received_events_url\": \"https://api.github.com/users/Codertocat/received_events\",  \"type\": \"User\",  \"site_admin\": false  }  }";
		return message;
	}

}
