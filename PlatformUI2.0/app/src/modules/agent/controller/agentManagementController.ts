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
			var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;	
			
			self.getSelectedAgentDetails();
			self.showMessage = "Please select version & tools";
			self.showConfig = false;			
			
			self.agentService.getDocRootAgentVersionTools()
			.then(function (data) {	
				self.response = data.data;
				for(var key in self.response){					
					self.versionList.push(key);
				}				
			})			
			.catch(function (data) {												
				self.showMessage = "Problem with Platform Service, Please try again";	
			}); 			
			
			if(self.editAgentDetails['type'] == "update") 
			{
				self.getDbAgentConfig(self.editAgentDetails['agentid']);
			}
			
		}
		
		dynamicData: string;
		homeController: HomePageController;
		selectedTool: string;
		showMessage:string;
		showConfig: boolean = false;
		showThrobber: boolean;
		versionList = [];
		toolsArr = [];
		response = {};
		editAgentDetails = {};
		headerData = [];
		updatedConfigdata = {};
		configData:string;
		selectedOS: string;		
		selectedVersion: string;
		item = {};
		datait = {};
		defaultConfigdata = {};
		addButtIcon: string = "dist/icons/svg/actionIcons/Add_icon_disabled.svg";
        deleteButtIcon: string = "dist/icons/svg/actionIcons/Delete_icon_disabled.svg";
        editButtIcon: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
        saveButtonIcon: string = "dist/icons/svg/actionIcons/Save_icon_Disabled.svg";
				
		findDataType(key, arr) : string {			
			return typeof(arr[key]);
		}
		
		getSelectedAgentDetails(): void {
            this.editAgentDetails = this.homeController.selectedAgentID;    
        }
		
		versionOnChange(key): void {				
			this.selectedTool = "";			
			this.toolsArr = [];
			/* for(var data in this.response[key]){				
				this.toolsArr[data] = this.response[key];
			} */					
			this.toolsArr = this.response[key];
		} 
		
		getDocRootAgentConfig(version, toolName): void{
			var self = this;			
			self.showConfig = false;
			self.showThrobber = true;	
			self.showMessage = "";
			self.agentService.getDocrootAgentConfig(version, toolName)
			.then(function (data) {		
				console.log(data);
				self.showConfig = true;
				self.showThrobber = false;
				self.defaultConfigdata  = JSON.parse(data.data);
				self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
			})			
			.catch(function (data) {		
				self.showThrobber = false;							
				self.showMessage = "Problem with Platform Service, Please try again";				
			}); 
			
		}
		
		getDbAgentConfig(agentId): void{
			var self = this;			
			self.showConfig = false;
			self.showThrobber = true;	
			self.showMessage = "";
			self.agentService.getDbAgentConfig(agentId)
			.then(function (data) {		
				console.log(data);
				self.showConfig = true;
				self.showThrobber = false;
				self.defaultConfigdata  = data.data;
				self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
			})			
			.catch(function (data) {		
				self.showThrobber = false;							
				self.showMessage = "Problem with Platform Service, Please try again";				
			}); 
			
		}
		
		getUpdatedConfigData(): void{		
			var self = this;	
			
			for(var key in self.defaultConfigdata) {
			
				if(key != "dynamicTemplate" && self.findDataType(key, self.defaultConfigdata) == "object"){
					
					self.item = {};
					
					for(var value in self.defaultConfigdata[key]){				
						self.item[value] = self.defaultConfigdata[key][value];						
					}					
					
					self.updatedConfigdata[key] = self.item;
					
					if(key == "communication") {					
						self.updatedConfigdata["dynamicTemplate"] = JSON.parse(self.dynamicData);
					}
					
				}else if(key != "dynamicTemplate") {	
						self.updatedConfigdata[key] = self.defaultConfigdata[key];						
				}				
			}	
			
			//self.updatedConfigdata["OS"] = self.selectedOS;
			
			if(self.updatedConfigdata){
				
				self.configData = JSON.stringify(self.updatedConfigdata);
				
				self.agentService.registerAgent(self.selectedTool, self.selectedVersion, self.selectedOS, self.configData)
				.then(function (data) {		
					console.log(data);					
				})			
				.catch(function (data) {		
					console.log(data);			
				}); 					
				
			}
		}
		
	}
	
}
