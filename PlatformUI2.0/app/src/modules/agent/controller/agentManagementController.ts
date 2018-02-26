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
			self.homeController.templateName = 'agentManagement';
			
			self.agentService.getAgentversionTools()
			.then(function (data) {	
				self.response['versions'] = data.data.details;
				for(var key in self.response['versions']){					
					self.versionList.push(key);
				}				
			})			
			.catch(function (data) {												
				self.showMessage = "Problem with Platform Service, Please try again";	
			}); 			
			
			self.getAgenttoolConfig(self.editAgentDetails['toolname'], self.editAgentDetails['version']);
			
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
		selectedOS: string;		
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
            this.editAgentDetails = this.homeController.selectedAgentTooldetails;            
        }
		
		versionOnChange(key): void {				
			this.selectedTool = "";			
			this.toolsArr = [];
			for(var data in this.response['versions'][key]){				
				this.toolsArr[data] = this.response['versions'][key][data];
			}					
		} 
		
		getAgenttoolConfig(version, toolName): void{
			var self = this;			
			self.showConfig = false;
			self.showThrobber = true;	
			self.showMessage = "";
			self.agentService.getAgentToolConfig(version, toolName)
			.then(function (data) {		
				console.log(data);
				self.showConfig = true;
				self.showThrobber = false;
				self.defaultConfigdata  = data.data;
				self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamic'], undefined, 4);
			})			
			.catch(function (data) {		
				self.showThrobber = false;							
				self.showMessage = "Problem with Platform Service, Please try again";				
			}); 
			
		}
		
		getUpdatedConfigData(): void{			
			
			for(var key in this.defaultConfigdata) {
			
				if(key != "dynamic" && this.findDataType(key, this.defaultConfigdata) == "object"){
					
					this.item = {};
					
					for(var value in this.defaultConfigdata[key]){				
						this.item[value] = this.defaultConfigdata[key][value];						
					}					
					
					this.updatedConfigdata[key] = this.item;
					
					if(key == "communication") {					
						this.updatedConfigdata["dynamic"] = JSON.parse(this.dynamicData);
					}
					
				}else if(key != "dynamic") {	
						this.updatedConfigdata[key] = this.defaultConfigdata[key];						
				}				
			}	
			
			this.updatedConfigdata["OS"] = this.selectedOS;
			
			
			console.log(JSON.stringify(this.updatedConfigdata));
		}
		
	}
	
}
