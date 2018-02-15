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
			self.showMessage = "Please select version & tools";
			
			self.agentService.getAgentversionTools()
			.then(function (data) {			
			
				self.response['versions'] = data.data.details;

				for(var key in self.response['versions']){					
					self.versionList.push(key);
				}
				
			})			
			.catch(function (data) {												
				console.log(data);
			}); 

		
		
		
		/* self.defaultConfigdata = {
			  "mqConfig": {
				"user": "iSight",
				"password": "iSight",
				"host": "127.0.0.1",
				"exchange": "iSight"
			  },
			  "subscribe": {
				"config": "ALM.RALLY.config"
			  },
			  "publish": {
				"data": "ALM.RALLY.DATA",
				"health": "ALM.RALLY.HEALTH"
			  },
			  "communication": {
				"type": "REST"
			  },
			  "responseTemplate": {
					"LastUpdateDate": "LastUpdateDate",
					"Name": "StoryName"
			  },
			  "proxy": "proxy.companyname.com:8080/",
			  "accesstoken":"***REMOVED***",
			  "runSchedule": 30,
			  "userid": "userid",
			  "passwd": "passwd",
			  "baseUrl": "https://rally1.rallydev.com/slm/webservice/v2.0/",
			  "startFrom": "2016-10-10 00:01",
			  "toolsTimeZone" : "Asia/Kolkata",
			  "insightsTimeZone" : "Asia/Kolkata",
			  "useResponseTemplate" : true,
			  "timeStampField":"LastUpdated",
			  "timeStampFormat":"%Y-%m-%dT%H:%M:%S",
			  "isDebugAllowed" : false,
			  "loggingSetting" : {
					"logLevel" : "WARN"
				}
			}; */
			
			
		}
		
		showMessage:string;
		showConfig: boolean = false;
		showThrobber: boolean;
		versionList = [];
		toolsArr = [];
		response = {};
		headerData = [];
		defaultConfigdata = {};
		addButtIcon: string = "dist/icons/svg/actionIcons/Add_icon_disabled.svg";
        deleteButtIcon: string = "dist/icons/svg/actionIcons/Delete_icon_disabled.svg";
        editButtIcon: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
        saveButtonIcon: string = "dist/icons/svg/actionIcons/Save_icon_Disabled.svg";
				
		findDataType(key, arr) : string {			
			return typeof(arr[key]);
		}
		
		versionOnChange(key): void {	
			this.toolsArr = [];
			for(var data in this.response['versions'][key]){				
				this.toolsArr[data] = this.response['versions'][key][data];
			}		
			console.log(this.toolsArr);
		} 
		
		getAgenttoolConfig(version, toolName): void{
			var self = this;			
			self.showConfig = false;
			self.showThrobber = true;	
			self.showMessage = "";
			self.agentService.getAgentToolConfig(version, toolName)
			.then(function (data) {		
				self.showConfig = true;
				self.showThrobber = false;
				self.defaultConfigdata  = data.data;
				console.log(data);				
			})			
			.catch(function (data) {		
				self.showThrobber = false;							
				self.showMessage = "Problem with Platform Service, Please try again";
				console.log(data);
			}); 
			
		}
		
		
	}
	
}
