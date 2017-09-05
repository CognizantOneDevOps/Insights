/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.cognizant.devops.platformservice.rest.dashboard;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.devops.platformdal.dashboards.CustomDashboardDAL;
import com.cognizant.devops.platformdal.user.UserPortfolioEnum;
import com.cognizant.devops.platformservice.rest.util.PlatformServiceUtil;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/dashboard")
public class CustomDashboardService {
	
	@RequestMapping(value = "/getCustomDashboard", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject getCustomDashboard(@RequestParam String portfolio){
		CustomDashboardDAL customDashboardDAL = new CustomDashboardDAL();
		return PlatformServiceUtil.buildSuccessResponseWithData(customDashboardDAL.getCustomDashboard(UserPortfolioEnum.valueOf(portfolio)));
	}
	
	@RequestMapping(value = "/addCustomDashboard", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public @ResponseBody JsonObject addCustomDashboard(@RequestParam String dashboardName, @RequestParam String dashboardJson, @RequestParam String portfolio){
		CustomDashboardDAL customDashboardDAL = new CustomDashboardDAL();
		boolean result = customDashboardDAL.addCustomDashboard(dashboardName, dashboardJson, UserPortfolioEnum.valueOf(portfolio));
		if(result){
			return PlatformServiceUtil.buildSuccessResponse();
		}else{
			return PlatformServiceUtil.buildFailureResponse("Unable to add custom Dashboard");
		}
	}
}
