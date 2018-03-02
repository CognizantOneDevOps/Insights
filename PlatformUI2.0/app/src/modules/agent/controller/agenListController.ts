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
		
		showConfirmMessage: string;
		showList: boolean = false;
		showThrobber: boolean;
		showMessage: string;
		data = [];
		tableParams = [];
		homeController: HomePageController;
		buttonDisableStatus: boolean = true;
	    editIconSrc: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
		startIconSrc: string = "dist/icons/svg/actionIcons/Start_icon_Active.svg";
		stopIconSrc: string = "dist/icons/svg/actionIcons/Stop_icon_Active.svg";    	
		
		editAgentConfig(params): void {							
			this.homeController.selectedAgentID = params;
			this.homeController.templateName = 'agentManagement';			
		}
		
		enableActions(): void{		
            this.buttonDisableStatus = false;
			this.editIconSrc = "dist/icons/svg/userOnboarding/Edit_icon_MouseOver.svg";
		}
		
		getRegisteredAgents(): void{
			
			var self = this;						
			self.showList = false;
			self.showThrobber = true;				
			self.homeController.showConfirmMessage = "";
			self.agentService.loadAgentServices("DB_AGENTS_LIST")			
			.then(function (response) {						
				self.showThrobber = false;	
				self.data = response.data;
				console.log(self.data);
				
				if(self.data.length == 0){
					self.showMessage = "No Records found";							
				}else{
					self.showList = true;
					self.tableParams = new self.NgTableParams({				
						page: 1,
						count: 5				
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
			
			
		}
		
		newAgentRegister(): void {	
			this.homeController.selectedAgentID = {'type' : 'new'};  	
			this.homeController.templateName = 'agentManagement';
		}
		
	}
	
}
