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

package com.cognizant.devops.platformservice.test.accessGroupManagement;

import javax.servlet.http.Cookie;

public class GroupsAndUserTestData {

	String accept = "application/json, text/plain, */*";
	String authorization = "token";

	String orgName = "Insights285";
	int orgId = 14;
	String userName = "user";
	String role = "Admin";
	int userId = 25;

	// Delete
	String roleDelete = "Editor";
	int userIdDelete = 26;

	// AddUser
	String userPropertyListAdmin = "{\"name\":\"userAdmin\",\"email\":\"useradmin@cognizant.com\",\"userName\":\"userAdmin\",\"password\":\"userTest\",\"role\":\"Admin\",\"orgName\":\"Insights285\",\"orgId\":14}";
	String userPropertyListEditor = "{\"name\":\"userEditor\",\"email\":\"userEditor@cognizant.com\",\"userName\":\"userEditor\",\"password\":\"userTest1\",\"role\":\"Editor\",\"orgName\":\"Insights285\",\"orgId\":14}";
	String userPropertyListViewer = "{\"name\":\"userViewer\",\"email\":\"userViewer@cognizant.com\",\"userName\":\"userViewer\",\"password\":\"userTest2\",\"role\":\"Viewer\",\"orgName\":\"Insights285\",\"orgId\":14}";

	// AssignUser
	String assignUserData = "[{\"orgName\":\"Insight_Org\",\"orgId\":2,\"roleName\":\"Viewer\",\"userName\":\"userTest\"}]";
	String expectedSearchData = "{\"status\":\"success\",\"data\":[{\"orgId\":2,\"name\":\"Insight_Org\",\"role\":\"Editor\"},{\"orgId\":14,\"name\":\"Insights285\",\"role\":\"Editor\"},{\"orgId\":1,\"name\":\"Main Org.\",\"role\":\"Viewer\"},{\"orgId\":4,\"name\":\"T-Demo\",\"role\":\"Viewer\"}]}";

}