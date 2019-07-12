package com.congnizant.eventsubscriber.test;

// import com.google.gson.JsonElement;
// import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import sun.misc.BASE64Encoder;

public class SystemStatusMain {

	public static void main(String[] args) {
		System.out.println(" Run Event Subscriber .....");
		SystemStatusMain ssm = new SystemStatusMain();
		String data = ssm.createMessage();
		for (int i = 0; i < 4000; i++) {
			System.out.println(" request number " + i);
			jerseyPostClientWithAuthentication(
					"http://localhost:8981/PlatfromEventSubscriber/webhookEvent?Toolname=GIT", null, null, null, data);//Git 34.236.204.95 GitEvent /EventSubscriber
		}
		System.out.println(" Run Event Subscriber complete .....");

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
			//JsonParser parser = new JsonParser();
			//JsonElement dataJson = parser.parse(data);//new Gson().fromJson(data, JsonElement.class)
			Client restClient = Client.create();
			WebResource webResource = restClient.resource(url);
			response = webResource.accept("application/json")
					//.header("Authorization", "Basic " + authStringEnc)
					.post(ClientResponse.class, data);//"{aa}"dataJson.toString()
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			} else {
				output = response.getEntity(String.class);
			}
			System.out.print(" response code " + response.getStatus() + "  output  " + output);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" error while getGetting  jerseyPostClientWithAuthentication " + e.getMessage());
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
		String message = "\"{\r\n" + "  \\\"zen\\\": \\\"Design for failure.\\\",\r\n"
				+ "  \\\"hook_id\\\": 118548122,\r\n" + "  \\\"hook\\\": {\r\n"
				+ "    \\\"type\\\": \\\"Repository\\\",\r\n" + "    \\\"id\\\": 118548122,\r\n"
				+ "    \\\"name\\\": \\\"web\\\",\r\n" + "    \\\"active\\\": true,\r\n" + "    \\\"events\\\": [\r\n"
				+ "      \\\"commit_comment\\\",\r\n" + "      \\\"push\\\"\r\n" + "    ],\r\n"
				+ "    \\\"config\\\": {\r\n" + "      \\\"content_type\\\": \\\"form\\\",\r\n"
				+ "      \\\"insecure_ssl\\\": \\\"0\\\",\r\n"
				+ "      \\\"url\\\": \\\"http://34.236.204.95:8080/EventSubscriber/GitEvent\\\"\r\n" + "    },\r\n"
				+ "    \\\"updated_at\\\": \\\"2019-06-21T05:55:52Z\\\",\r\n"
				+ "    \\\"created_at\\\": \\\"2019-06-21T05:55:52Z\\\",\r\n"
				+ "    \\\"url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/hooks/118548122\\\",\r\n"
				+ "    \\\"test_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/hooks/118548122/test\\\",\r\n"
				+ "    \\\"ping_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/hooks/118548122/pings\\\",\r\n"
				+ "    \\\"last_response\\\": {\r\n" + "      \\\"code\\\": null,\r\n"
				+ "      \\\"status\\\": \\\"unused\\\",\r\n" + "      \\\"message\\\": null\r\n" + "    }\r\n"
				+ "  },\r\n" + "  \\\"repository\\\": {\r\n" + "    \\\"id\\\": 141991164,\r\n"
				+ "    \\\"node_id\\\": \\\"MDEwOlJlcG9zaXRvcnkxNDE5OTExNjQ=\\\",\r\n"
				+ "    \\\"name\\\": \\\"insightTest\\\",\r\n"
				+ "    \\\"full_name\\\": \\\"gauravdeshmukh612/insightTest\\\",\r\n"
				+ "    \\\"private\\\": false,\r\n" + "    \\\"owner\\\": {\r\n"
				+ "      \\\"login\\\": \\\"gauravdeshmukh612\\\",\r\n" + "      \\\"id\\\": 41290302,\r\n"
				+ "      \\\"node_id\\\": \\\"MDQ6VXNlcjQxMjkwMzAy\\\",\r\n"
				+ "      \\\"avatar_url\\\": \\\"https://avatars0.githubusercontent.com/u/41290302?v=4\\\",\r\n"
				+ "      \\\"gravatar_id\\\": \\\"\\\",\r\n"
				+ "      \\\"url\\\": \\\"https://api.github.com/users/gauravdeshmukh612\\\",\r\n"
				+ "      \\\"html_url\\\": \\\"https://github.com/gauravdeshmukh612\\\",\r\n"
				+ "      \\\"followers_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/followers\\\",\r\n"
				+ "      \\\"following_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/following{/other_user}\\\",\r\n"
				+ "      \\\"gists_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/gists{/gist_id}\\\",\r\n"
				+ "      \\\"starred_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/starred{/owner}{/repo}\\\",\r\n"
				+ "      \\\"subscriptions_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/subscriptions\\\",\r\n"
				+ "      \\\"organizations_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/orgs\\\",\r\n"
				+ "      \\\"repos_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/repos\\\",\r\n"
				+ "      \\\"events_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/events{/privacy}\\\",\r\n"
				+ "      \\\"received_events_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/received_events\\\",\r\n"
				+ "      \\\"type\\\": \\\"User\\\",\r\n" + "      \\\"site_admin\\\": false\r\n" + "    },\r\n"
				+ "    \\\"html_url\\\": \\\"https://github.com/gauravdeshmukh612/insightTest\\\",\r\n"
				+ "    \\\"description\\\": \\\"insightTest\\\",\r\n" + "    \\\"fork\\\": false,\r\n"
				+ "    \\\"url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest\\\",\r\n"
				+ "    \\\"forks_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/forks\\\",\r\n"
				+ "    \\\"keys_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/keys{/key_id}\\\",\r\n"
				+ "    \\\"collaborators_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/collaborators{/collaborator}\\\",\r\n"
				+ "    \\\"teams_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/teams\\\",\r\n"
				+ "    \\\"hooks_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/hooks\\\",\r\n"
				+ "    \\\"issue_events_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/issues/events{/number}\\\",\r\n"
				+ "    \\\"events_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/events\\\",\r\n"
				+ "    \\\"assignees_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/assignees{/user}\\\",\r\n"
				+ "    \\\"branches_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/branches{/branch}\\\",\r\n"
				+ "    \\\"tags_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/tags\\\",\r\n"
				+ "    \\\"blobs_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/blobs{/sha}\\\",\r\n"
				+ "    \\\"git_tags_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/tags{/sha}\\\",\r\n"
				+ "    \\\"git_refs_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/refs{/sha}\\\",\r\n"
				+ "    \\\"trees_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/trees{/sha}\\\",\r\n"
				+ "    \\\"statuses_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/statuses/{sha}\\\",\r\n"
				+ "    \\\"languages_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/languages\\\",\r\n"
				+ "    \\\"stargazers_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/stargazers\\\",\r\n"
				+ "    \\\"contributors_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/contributors\\\",\r\n"
				+ "    \\\"subscribers_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/subscribers\\\",\r\n"
				+ "    \\\"subscription_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/subscription\\\",\r\n"
				+ "    \\\"commits_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/commits{/sha}\\\",\r\n"
				+ "    \\\"git_commits_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/git/commits{/sha}\\\",\r\n"
				+ "    \\\"comments_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/comments{/number}\\\",\r\n"
				+ "    \\\"issue_comment_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/issues/comments{/number}\\\",\r\n"
				+ "    \\\"contents_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/contents/{+path}\\\",\r\n"
				+ "    \\\"compare_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/compare/{base}...{head}\\\",\r\n"
				+ "    \\\"merges_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/merges\\\",\r\n"
				+ "    \\\"archive_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/{archive_format}{/ref}\\\",\r\n"
				+ "    \\\"downloads_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/downloads\\\",\r\n"
				+ "    \\\"issues_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/issues{/number}\\\",\r\n"
				+ "    \\\"pulls_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/pulls{/number}\\\",\r\n"
				+ "    \\\"milestones_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/milestones{/number}\\\",\r\n"
				+ "    \\\"notifications_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/notifications{?since,all,participating}\\\",\r\n"
				+ "    \\\"labels_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/labels{/name}\\\",\r\n"
				+ "    \\\"releases_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/releases{/id}\\\",\r\n"
				+ "    \\\"deployments_url\\\": \\\"https://api.github.com/repos/gauravdeshmukh612/insightTest/deployments\\\",\r\n"
				+ "    \\\"created_at\\\": \\\"2018-07-23T09:19:46Z\\\",\r\n"
				+ "    \\\"updated_at\\\": \\\"2019-03-07T11:14:29Z\\\",\r\n"
				+ "    \\\"pushed_at\\\": \\\"2019-03-11T11:56:42Z\\\",\r\n"
				+ "    \\\"git_url\\\": \\\"git://github.com/gauravdeshmukh612/insightTest.git\\\",\r\n"
				+ "    \\\"ssh_url\\\": \\\"git@github.com:gauravdeshmukh612/insightTest.git\\\",\r\n"
				+ "    \\\"clone_url\\\": \\\"https://github.com/gauravdeshmukh612/insightTest.git\\\",\r\n"
				+ "    \\\"svn_url\\\": \\\"https://github.com/gauravdeshmukh612/insightTest\\\",\r\n"
				+ "    \\\"homepage\\\": null,\r\n" + "    \\\"size\\\": 40,\r\n"
				+ "    \\\"stargazers_count\\\": 0,\r\n" + "    \\\"watchers_count\\\": 0,\r\n"
				+ "    \\\"language\\\": \\\"Python\\\",\r\n" + "    \\\"has_issues\\\": true,\r\n"
				+ "    \\\"has_projects\\\": true,\r\n" + "    \\\"has_downloads\\\": true,\r\n"
				+ "    \\\"has_wiki\\\": true,\r\n" + "    \\\"has_pages\\\": false,\r\n"
				+ "    \\\"forks_count\\\": 0,\r\n" + "    \\\"mirror_url\\\": null,\r\n"
				+ "    \\\"archived\\\": false,\r\n" + "    \\\"disabled\\\": false,\r\n"
				+ "    \\\"open_issues_count\\\": 0,\r\n" + "    \\\"license\\\": null,\r\n"
				+ "    \\\"forks\\\": 0,\r\n" + "    \\\"open_issues\\\": 0,\r\n" + "    \\\"watchers\\\": 0,\r\n"
				+ "    \\\"default_branch\\\": \\\"master\\\"\r\n" + "  },\r\n" + "  \\\"sender\\\": {\r\n"
				+ "    \\\"login\\\": \\\"gauravdeshmukh612\\\",\r\n" + "    \\\"id\\\": 41290302,\r\n"
				+ "    \\\"node_id\\\": \\\"MDQ6VXNlcjQxMjkwMzAy\\\",\r\n"
				+ "    \\\"avatar_url\\\": \\\"https://avatars0.githubusercontent.com/u/41290302?v=4\\\",\r\n"
				+ "    \\\"gravatar_id\\\": \\\"\\\",\r\n"
				+ "    \\\"url\\\": \\\"https://api.github.com/users/gauravdeshmukh612\\\",\r\n"
				+ "    \\\"html_url\\\": \\\"https://github.com/gauravdeshmukh612\\\",\r\n"
				+ "    \\\"followers_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/followers\\\",\r\n"
				+ "    \\\"following_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/following{/other_user}\\\",\r\n"
				+ "    \\\"gists_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/gists{/gist_id}\\\",\r\n"
				+ "    \\\"starred_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/starred{/owner}{/repo}\\\",\r\n"
				+ "    \\\"subscriptions_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/subscriptions\\\",\r\n"
				+ "    \\\"organizations_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/orgs\\\",\r\n"
				+ "    \\\"repos_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/repos\\\",\r\n"
				+ "    \\\"events_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/events{/privacy}\\\",\r\n"
				+ "    \\\"received_events_url\\\": \\\"https://api.github.com/users/gauravdeshmukh612/received_events\\\",\r\n"
				+ "    \\\"type\\\": \\\"User\\\",\r\n" + "    \\\"site_admin\\\": false\r\n" + "  }\r\n" + "}\"";
		return message;
	}

}
