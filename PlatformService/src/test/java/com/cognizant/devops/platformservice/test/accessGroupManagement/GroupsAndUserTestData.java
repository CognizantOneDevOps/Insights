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

import java.util.Base64;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import com.cognizant.devops.platformcommons.config.ApplicationConfigProvider;
import com.cognizant.devops.platformcommons.constants.PlatformServiceConstants;
import com.cognizant.devops.platformcommons.core.util.JsonUtils;
import com.cognizant.devops.platformcommons.dal.grafana.GrafanaHandler;
import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.cognizant.devops.platformservice.security.config.AuthenticationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GroupsAndUserTestData extends AbstractTestNGSpringContextTests{
	
	@Autowired
	HttpServletRequest httpRequest;
	
	String BASICAUTH = "Basic ";
	String NAME = "name";
	String orgName = "InsightsTestOrg123";
	String orgName1 = "";
	String userLoginName1="TestUser1";
	String userLoginName2="TestUser2";
	int orgId=0;
	String tempOrgName="TestInsightsOrg123";
	int tempOrgId=0;
	int userId=0;
	String uid="";
	String accept = "application/json, text/plain, */*";
	String authorization = "token";
	String outcomeResponse="User added to organization";
	String outcomeResponseExistingUserDifferentRole="User exists in currrent org with different role";
	String themePreference = "dark";
	String userName = "user";
	String role = "Admin";

	// Delete
	String roleDelete = "Editor";
	int userIdDelete = 26;

	// AddUser
	String NewUserRoleAdmin = "";
	String NewUserRoleViewer = "";
	String AssignUserRoleViewerToEditor="";
	String AssignNewUserRoleViewerToEditor="";
	String DuplicateUserRoleAdmin="";
	String DuplicateUserRoleViewer = "";
	String NewUserAdminEmailExist = "";
	String NewUserAdminUserNameExist = "";
	String assignNonExistingUserData="";
	String assignExistingUserDataToNewOrg="";
	String assignExistingUserDatatoSameOrg="";
	int mainId=0;
	String mainOrg="";
	String mainEmailAdmin="";
	String mainEmailViewer="";
	String mainUserNameAdmin="";
	String mainUserNameViewer="";
	String NewUserRoleAdminMainOrg = "";
	String NewUserRoleAdminViewerOrg = "";
	String ExisitngUserRoleOrg = "";
	
	JsonObject NewUserRoleViewerJson = null;
	JsonArray assignNonExistingUserDataJson = null;

	GrafanaHandler grafanaHandler = new GrafanaHandler();
	
	public int getGrafanaOrgId(String orgName) throws InsightsCustomException{
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, BASICAUTH + encodedString);
		
		// check if organization exists
		String orgResponse = grafanaHandler.grafanaGet(PlatformServiceConstants.API_ORGS + "name/" + orgName, headers);
		JsonObject orgResponseJson = JsonUtils.parseStringAsJsonObject(orgResponse);
		if (orgResponse.contains("id"))
			orgId = orgResponseJson.get("id").getAsInt();
		else
			orgId = -1;
		return orgId;
	}
	
	public void deleteGrafanaOrgId(String id) throws InsightsCustomException{
		Map<String, String> headers = PlatformServiceUtil.prepareGrafanaHeader(httpRequest);
		String authString = ApplicationConfigProvider.getInstance().getGrafana().getAdminUserName() + ":"
				+ ApplicationConfigProvider.getInstance().getGrafana().getAdminUserPassword();
		String encodedString = Base64.getEncoder().encodeToString(authString.getBytes());
		headers.put(AuthenticationUtils.AUTH_HEADER_KEY, BASICAUTH + encodedString);
		grafanaHandler.grafanaDelete(PlatformServiceConstants.API_ORGS + id, headers);
	 }
	public void setOrdId(int id, String orgName) {
		this.orgId = id;
		String email="demo123"+String.valueOf(id)+"@gmail.com";
		String userName = "User"+String.valueOf(id);
		//forNewUserAdditionWithNewEmail
		NewUserRoleAdmin = "{\"name\":\"userAdminTest\",\"email\":\""+email+"\",\"userName\":\""+userName+"\",\"password\":\"userTest\",\"role\":\"Admin\",\"orgName\":\""+orgName+"\",\"orgId\":"+id+"}";
		DuplicateUserRoleAdmin = NewUserRoleAdmin;

		//forDuplicateUserAdditionWithDifferentRole 4th attempt
		DuplicateUserRoleViewer = "{\"name\":\"userAdminTest\",\"email\":\""+email+"\",\"userName\":\""+userName+"\",\"password\":\"userTest\",\"role\":\"Viewer\",\"orgName\":\""+orgName+"\",\"orgId\":"+id+"}";
				
		//forNewUserAdditionWithExistingEmail 2nd attempt
		String newUserName=userName+"test"+String.valueOf(id);
		NewUserAdminEmailExist = "{\"name\":\"userAdminTest\",\"email\":\""+email+"\",\"userName\":\""+newUserName+"\",\"password\":\"userTest\",\"role\":\"Admin\",\"orgName\":\""+orgName+"\",\"orgId\":"+id+"}";

		email="test"+String.valueOf(id)+email;
		NewUserRoleViewer = "{\"name\":\"userAdminTest\",\"email\":\""+email+"\",\"userName\":\""+newUserName+"\",\"password\":\"userTest\",\"role\":\"Viewer\",\"orgName\":\""+orgName+"\",\"orgId\":"+id+"}";
		NewUserRoleViewerJson = JsonUtils.parseStringAsJsonObject(NewUserRoleViewer);
		
		NewUserAdminUserNameExist = "{\"name\":\"userAdminTest\",\"email\":\""+email+"\",\"userName\":\""+userName+"\",\"password\":\"userTest\",\"role\":\"Admin\",\"orgName\":\""+orgName+"\",\"orgId\":"+id+"}";
	
		assignNonExistingUserData = "[{\"orgName\":\"Insight_Org\",\"orgId\":2,\"roleName\":\"Viewer\",\"userName\":\"userAdminTest\"}]";
		assignNonExistingUserDataJson = JsonUtils.parseStringAsJsonArray(assignNonExistingUserData);
		assignExistingUserDatatoSameOrg = "[{\"orgName\":\""+orgName+"\",\"orgId\":"+id+",\"roleName\":\"Editor\",\"userName\":\""+newUserName+"\"}]";
	
		mainId=1;
		mainOrg="Main Org.";
		mainEmailAdmin="qwerty"+id+"@gmail.com";
		mainEmailViewer="qwertytest"+id+"@gmail.com";
		mainUserNameAdmin="MainUserTest"+id;
		mainUserNameViewer="UserMainTest"+id;
		
		NewUserRoleAdminMainOrg = "{\"name\":\"userAdminTest\",\"email\":\""+mainEmailAdmin+"\",\"userName\":\""+mainUserNameAdmin+"\",\"password\":\"userTest\",\"role\":\"Admin\",\"orgName\":\""+mainOrg+"\",\"orgId\":"+mainId+"}";
		NewUserRoleAdminViewerOrg ="{\"name\":\"userAdminTest\",\"email\":\""+mainEmailViewer+"\",\"userName\":\""+mainUserNameViewer+"\",\"password\":\"userTest\",\"role\":\"Viewer\",\"orgName\":\""+mainOrg+"\",\"orgId\":"+mainId+"}";
	}
	
	public void setId(int id, String orgName)
	{
		ExisitngUserRoleOrg	= "{\"name\":\"userAdminTest\",\"email\":\""+mainEmailViewer+"\",\"userName\":\""+mainUserNameViewer+"\",\"password\":\"userTest\",\"role\":\"Viewer\",\"orgName\":\""+orgName+"\",\"orgId\":"+id+"}";
	}
	
	public void setOrgName(String orgName1) {
		this.orgName1 = orgName1;
	}
	
	public void setNewOrdId(int id, String orgName) {
		String user = NewUserRoleViewerJson.get("userName").getAsString();
		assignExistingUserDataToNewOrg = "[{\"orgName\":\""+orgName+"\",\"orgId\":"+id+",\"roleName\":\"Editor\",\"userName\":\""+user+"\"}]";
	}
	
	public void SetUid(String uid) {
		this.uid = uid;
	}
}