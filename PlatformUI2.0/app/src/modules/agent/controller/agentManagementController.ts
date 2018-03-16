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
        static $inject = ['agentService', 'iconService', '$sce', '$mdDialog', '$cookies', 'toolConfigService', 'restEndpointService'];
        constructor(			
            private agentService: IAgentService,
            private iconService: IIconService,
			private $sce,
            private $mdDialog, private $cookies, private toolConfigService: IToolConfigService, private restEndpointService: IRestEndpointService) {
			var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;	
			self.getOsVersionTools("");
			self.getSelectedAgentDetails();
			self.showMessage = "Please select version & tools";
			self.showConfig = false;			
			
			if(self.editAgentDetails['type'] == "update") 
			{
				self.getDbAgentConfig(self.editAgentDetails['agentid']);
				self.btnValue = "Update";
				self.buttonDisableStatus = false;
			}else {
				self.btnValue = "Register";
			}
			
			self.Oslists = {
				"windows" : "Windows",
				"linux" : "Linux",
				"ubuntu" : "Ubuntu",
			};
			
		}
		
		Oslists = {};
		configDesc = {};
		configAbbr = [];
		buttonDisableStatus: boolean = true;
		btnValue: string;
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
		
		getOsVersionTools(Selversion): void{
			var self = this;
			self.toolsArr = [];			
			self.agentService.getDocRootAgentVersionTools()
			.then(function (data) {	
				
				self.response = data.data;			
				
				if(Selversion) {	
					self.toolsArr = self.response[Selversion];
				}else {
					for(var key in self.response){					
						self.versionList.push(key);
					}			
				}
				
			})			
			.catch(function (data) {												
				self.showMessage = "Problem with Platform Service, Please try again";	
			}); 
		}
				
		findDataType(key, arr) : string {			
			return typeof(arr[key]);
		}
		
		getSelectedAgentDetails(): void {
            this.editAgentDetails = this.homeController.selectedAgentID;    
        }
		
		versionOnChange(key): void {				
			this.selectedTool = "";			
			this.toolsArr = [];							
			this.toolsArr = this.response[key];
		} 
		
		getDocRootAgentConfig(version, toolName): void{
			var self = this;			
			self.showConfig = false;
			self.showThrobber = true;	
			self.showMessage = "";
			self.configDesc = self.restEndpointService.getConfigDesc();
			
			self.agentService.getDocrootAgentConfig(version, toolName)
			.then(function (data) {		
				self.buttonDisableStatus = false;
				self.showConfig = true;
				self.showThrobber = false;
				self.defaultConfigdata  = JSON.parse(data.data);				
				self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
				
				//if(Object.keys(self.configDesc).length != 0 ) {
					
					for(var key in self.defaultConfigdata){		
						if(self.configDesc.hasOwnProperty(key)) {
							self.configAbbr[key] = self.configDesc[key];			
						}else {
							self.configAbbr[key] = key;
						}
					}				
				//}
				
				
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
			self.configDesc = self.restEndpointService.getConfigDesc();
			self.agentService.getDbAgentConfig(agentId)
			.then(function (data) {		
				console.log(data);
				self.showConfig = true;
				self.showThrobber = false;
				self.defaultConfigdata  = JSON.parse(data.data.agentJson);
				self.selectedVersion = data.data.agentVersion;	
				self.selectedOS = data.data.osVersion;
				self.getOsVersionTools(self.selectedVersion);
				self.selectedTool = data.data.toolName;
				self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
				
				for(var key in self.defaultConfigdata){	
				
					if(self.configDesc.hasOwnProperty(key)) {
						self.configAbbr[key] = self.configDesc[key];							
					}else {
						self.configAbbr[key] = key;
					}
				}
			})			
			.catch(function (data) {		
				self.showThrobber = false;							
				self.showMessage = "Problem with Platform Service, Please try again";				
			}); 
			
		}
		
		sendSuccessStatusMsg(Msg): void{
			this.homeController.showConfirmMessage = Msg+" Successfully";
			this.homeController.templateName = 'agentList';		
		}
		
		sendFailureStatusMsg(Msg): void{
			this.homeController.showConfirmMessage = "Problem with "+Msg+", Please try again.";
			this.homeController.templateName = 'agentList';		
		}
		
		getUpdatedConfigData(actionType): void{		
			var self = this;	
			self.updatedConfigdata = {};
			
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
			
			if(self.updatedConfigdata){
				
				self.configData = "";				
				self.configData = encodeURIComponent(JSON.stringify(self.updatedConfigdata));					
				
				if(actionType == "Update") {
					
					self.agentService.updateAgent(self.editAgentDetails['agentid'], self.configData, self.selectedTool, self.selectedVersion, self.selectedOS)
					.then(function (data) {				
							
						if(data.status == "success"){							
							self.sendSuccessStatusMsg("Updated");
						}else {
							self.sendFailureStatusMsg("update");
						}
					})			
					.catch(function (data) {		
							self.sendFailureStatusMsg("update Platform Service");					
					}); 					
					
					
				}else {		
					
					self.agentService.registerAgent(self.selectedTool, self.selectedVersion, self.selectedOS, self.configData)
					.then(function (data) {		
						console.log(data);	
						if(data.status == "success"){						
							self.sendSuccessStatusMsg("Registered");
						}else {
							self.sendFailureStatusMsg("register");
						}
					})			
					.catch(function (data) {		
						self.sendFailureStatusMsg("register Platform Service");	
					}); 	
					
				}							
				
			}
		}
		
	}
	
}
