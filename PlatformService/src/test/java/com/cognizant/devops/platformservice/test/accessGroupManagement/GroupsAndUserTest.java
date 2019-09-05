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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.cognizant.devops.platformcommons.exception.InsightsCustomException;
import com.cognizant.devops.platformservice.rest.AccessGroupManagement.AccessGroupManagement;
import com.cognizant.devops.platformservice.rest.grafana.UserManagementService;

@Test
@ContextConfiguration(locations = { "classpath:spring-test-config.xml" })
public class GroupsAndUserTest {

	AccessGroupManagement accessGroupManagement = new AccessGroupManagement();
	UserManagementService userManagementService = new UserManagementService();
	GroupsAndUserTestData groupsAndUserTestData = new GroupsAndUserTestData();
	MockHttpServletRequest httpRequest = new MockHttpServletRequest();

	@Test(priority = 1)
	public void testGetOrgUsers() throws InsightsCustomException {

		httpRequest.addHeader("Authorization", groupsAndUserTestData.authorization);

		Object Actualrespone = userManagementService.getOrgUsers(groupsAndUserTestData.orgId);

	}

	@Test(priority = 2)
	public void testCreateOrg() throws InsightsCustomException {

		httpRequest.addHeader("Authorization", groupsAndUserTestData.authorization);

		Object Actualrespone = userManagementService.createOrg(groupsAndUserTestData.orgName);

	}

	@Test(priority = 3)
	public void testAddUser() throws InsightsCustomException {

		Object Actualrespone = accessGroupManagement.addUser(groupsAndUserTestData.userPropertyListAdmin);

	}

	@Test(priority = 4)
	public void testSearchUser() throws InsightsCustomException {

		String Actualrespone = accessGroupManagement.searchUser(groupsAndUserTestData.userName).toString();

		Assert.assertEquals(Actualrespone, groupsAndUserTestData.expectedSearchData);

	}

	@Test(priority = 5)
	public void testassignUser() throws InsightsCustomException {

		Object Actualrespone = accessGroupManagement.assignUser(groupsAndUserTestData.assignUserData);

	}

	@Test(priority = 6)
	public void testAddUserEditor() throws InsightsCustomException {

		Object Actualrespone = accessGroupManagement.addUser(groupsAndUserTestData.userPropertyListEditor);

	}

	@Test(priority = 7)
	public void testAddUserViewer() throws InsightsCustomException {

		Object Actualrespone = accessGroupManagement.addUser(groupsAndUserTestData.userPropertyListViewer);
	}

	@Test(priority = 8)
	public void testEditOrganizationUser() throws InsightsCustomException {

		String Actualrespone = userManagementService.editOrganizationUser(groupsAndUserTestData.orgId,
				groupsAndUserTestData.userId, groupsAndUserTestData.role);

	}

	@Test(priority = 9)
	public void testDeleteOrganizationUser() throws InsightsCustomException {

		String Actualrespone = userManagementService.deleteOrganizationUser(groupsAndUserTestData.orgId,
				groupsAndUserTestData.userIdDelete, groupsAndUserTestData.roleDelete);

	}

}
