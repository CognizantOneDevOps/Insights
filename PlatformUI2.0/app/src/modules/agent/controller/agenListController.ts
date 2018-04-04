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
    export class AgentListController {
        static $inject = ['agentService', 'iconService', '$sce', 'NgTableParams', '$mdDialog', '$cookies', 'toolConfigService'];
        constructor(			
            private agentService: IAgentService,
            private iconService: IIconService,
			private $sce, private NgTableParams,
            private $mdDialog, private $cookies, private toolConfigService: IToolConfigService) {
			
			var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
           
		    var self = this;				
			self.showConfirmMessage = "";
			self.homeController.templateName = 'agentList';				
			if(self.homeController.showConfirmMessage) {
				self.showConfirmMessage = self.homeController.showConfirmMessage;				
			}			
			self.getRegisteredAgents();
		}
		
		validationArr = {};
		showConfirmMessage: string;
		showList: boolean = false;
		showThrobber: boolean;
		showMessage: string;
		data = [];
		tableParams = [];
		homeController: HomePageController;
		buttonDisableStatus: boolean = false;
		runDisableStatus: string;
	    editIconSrc: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
		startIconSrc: string = "dist/icons/svg/actionIcons/Start_icon_Disabled.svg";
		stopIconSrc: string = "dist/icons/svg/actionIcons/Stop_icon_Disabled.svg";

		agentStartStopAction(actDetails, actType): void{
			var self = this;
			
			self.agentService.agentStartStop(actDetails.agentid, actType)
			.then(function (data) {		
				console.log(data);			
				self.showConfirmMessage = data.status;
				self.getRegisteredAgents();						
			})			
			.catch(function (data) {		
				console.log(data);
				self.showConfirmMessage = "Error";
				self.getRegisteredAgents();	
			});		
			
		}
		
		editAgentConfig(params): void {							
			this.homeController.selectedAgentID = params;
			this.homeController.templateName = 'agentManagement';			
		}
		
		enableActions(agntStatus): void{		
            this.buttonDisableStatus = true;
			this.runDisableStatus = agntStatus;
			this.editIconSrc = "dist/icons/svg/userOnboarding/Edit_icon_MouseOver.svg";			
			if(agntStatus == "STOP"){				
				this.startIconSrc = "dist/icons/svg/actionIcons/Start_icon_Active.svg";	
			}else {
			this.stopIconSrc = "dist/icons/svg/actionIcons/Stop_icon_Active.svg";}
		}
		
		getRegisteredAgents(): void{
			
			var self = this;						
			self.showList = false;
			self.showThrobber = true;				
			self.buttonDisableStatus = false;
			self.runDisableStatus = "";
			self.editIconSrc = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
			self.startIconSrc = "dist/icons/svg/actionIcons/Start_icon_Disabled.svg";
			self.stopIconSrc = "dist/icons/svg/actionIcons/Stop_icon_Disabled.svg";
			self.homeController.showConfirmMessage = "";
			self.agentService.loadAgentServices("DB_AGENTS_LIST")			
			.then(function (response) {						
				self.showThrobber = false;	
				self.data = response.data;
				
				if(self.data.length == 0){
					self.showMessage = "No Records found";							
				}else{
					self.consolidatedArr(self.data);
					self.showList = true;
					self.tableParams = new self.NgTableParams({				
						page: 1,
						count: 10				
					}, 
					{ 
						counts: [], // hide page counts control
						total: 1,  // value less than count hide pagination				
						dataset: self.data
					});				
				}
				
			})			
			.catch(function (response) {		
				self.showThrobber = false;		
				self.showList = false;				
				self.showMessage = "Problem with Platform Service, Please try again";				
			}); 			
			
			setTimeout(function() {
                  self.showConfirmMessage = "";
				  document.getElementById('confrmMsg').innerHTML = "";
				}, 2000); 	
			
		}
		
		consolidatedArr(detailArr): void {
			var self = this;
			for (var i = 0; i < detailArr.length; i++) {
				 this.validationArr[i] = { "os": detailArr[i].osVersion, "version" : detailArr[i].agentVersion , "tool": detailArr[i].toolName }
			}
		}
		
		newAgentRegister(dataArr): void {	
			this.homeController.selectedAgentID = {'type' : 'new', 'detailedArr' : dataArr};  	
			this.homeController.templateName = 'agentManagement';
		}
		
	}
	
}
