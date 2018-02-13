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

module ISightApp {
    export class AgentManagementController {
        static $inject = ['agentService', 'iconService', '$sce', '$mdDialog', '$cookies', 'toolConfigService'];
        constructor(			
            private agentService: IAgentService,
            private iconService: IIconService,
			private $sce,
            private $mdDialog, private $cookies, private toolConfigService: IToolConfigService) {
            var self = this;	

			
			self.defaultConfigdata = {
			 "mqConfig": [{
				"user": "iSight",
				"password": "iSight",
				"host": "127.0.0.1",
				"exchange": "iSight"
			  }],
			   "subscribe": [{
				"config": "ALM.HP.config"
			  }],
			  "publish": [{
				"data": "ALM.HP.DATA",
				"health": "ALM.HP.HEALTH"
			  }],
			  "communication": [{
				"type": "REST"
			  }],			  
			  "runSchedule": 30,
			  "toolsTimeZone" : "GMT",
			  "insightsTimeZone" : "GMT",
			  "StartFrom" : "2017-07-01 00:00:00",
			  "useResponseTemplate" : true,
			  "scanAllBranches" : false,
			  "scanPullRequests": true,
			  "auth": "base64",
			  "UserID": "",
			  "Passwd": "",
			  "timeStampField":"createdTime",
			  "isEpochTimeFormat" : true,
			  "BaseEndPoint": "http://127.0.0.1:7990/rest/api/1.0/projects/",
			  "isDebugAllowed" : false,			  
			  "loggingSetting" : [{
					"logLevel" : "WARN"
				}] 
			};	
			
		} 
		
		headerData = [];
		defaultConfigdata = {};
		addButtIcon: string = "dist/icons/svg/actionIcons/Add_icon_disabled.svg";
        deleteButtIcon: string = "dist/icons/svg/actionIcons/Delete_icon_disabled.svg";
        editButtIcon: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
        saveButtonIcon: string = "dist/icons/svg/actionIcons/Save_icon_Disabled.svg";

		
		findDataType(key, arr) : string {				
			return typeof(arr[key]);
		}
		
		
	}
	
}
