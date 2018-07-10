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
    export class DataDictionaryController {
        static $inject = ['dataDictionaryService']
        constructor(private dataDictionaryService: IDataDictionaryService) {
            // var elem = document.querySelector('#homePageTemplateContainer');
            //var homePageControllerScope = angular.element(elem).scope();
            //var homePageController = homePageControllerScope['homePageController'];
            //this.homeController = homePageController;

            var self = this;
            self.showConfirmMessage = "";
            //self.homeController.templateName = 'dataDictionary';
            self.getToolsList();
        }

        // homeController: HomePageController;
        showConfirmMessage: string;
        columnContentHeight: string;
        startTool: any;
        endTool: any;
        toolsList = [];
        endToolsList = [];
        data = [];
        showMessage = "";
        startToolProperties = [];
        endToolProperties = [];
        selectedToolProperties = [];
        correlationProperties: any = {};
        selectedToolName: any;
        showNoToolsSelectedMessage: boolean = true;
        showNoToolsSelectedForCorrelation: boolean = true;
        showCorrelationsLoadThrobber: string = "";
        showSelectEndToolMessage: boolean = true;
        showSelectStartToolThrobber: string = "";
        showSelectEndToolThrobber: string = "";
        logoSrc = "dist/icons/svg/landingPage/Cognizant_Insights.svg";

        getColumnHeight() {
            var height = (<HTMLIFrameElement>document.getElementById('selectColumn1'));
            this.columnContentHeight = height.height;
            console.log(this.columnContentHeight);
        }

        getToolsList() {
            var self = this;
            self.dataDictionaryService.getToolsAndCategories()
                .then(function (response) {
                    self.data = response.data;
                    self.toolsList = self.data;
                })
                .catch(function (response) {
                    self.showMessage = "Something wrong with Service, Please try again.";
                });
        }

        getStartToolProperties(toolName, categoryName) {
            var self = this;
            self.showSelectStartToolThrobber = "true";
            self.showNoToolsSelectedMessage = false;
            self.dataDictionaryService.getToolProperties(toolName, categoryName)
                .then(function (response) {
                    self.data = response.data;
                    self.showSelectStartToolThrobber = "false";
                    self.startToolProperties = self.data;
                    self.selectedToolProperties = self.data;
                    self.selectedToolName = self.startTool.toolName;
                    self.getEndToolsList(toolName);
                })
                .catch(function (response) {
                    self.showMessage = "Something wrong with Service, Please try again.";
                });
        }

        getEndToolsList(toolName) {
            var self = this;
            self.endToolsList = [];
            for (var index in self.toolsList) {
                if (self.toolsList[index].toolName !== toolName) {
                    self.endToolsList.push(self.toolsList[index]);
                }
            }
        }

        getEndToolProperties(toolName, categoryName) {
            var self = this;
            self.showSelectEndToolMessage = false;
            self.showSelectEndToolThrobber = "true";
            self.dataDictionaryService.getToolProperties(toolName, categoryName)
                .then(function (response) {
                    self.data = response.data;
                    self.showSelectEndToolThrobber = "false";
                    self.endToolProperties = self.data;
                })
                .catch(function (response) {
                    self.showMessage = "Something wrong with Service, Please try again.";
                });
        }

        showStartToolProperties() {
            var self = this;
            self.selectedToolProperties = self.startToolProperties;
            self.selectedToolName = self.startTool.toolName;
        }

        showEndToolProperties() {
            var self = this;
            self.selectedToolProperties = self.endToolProperties;
            self.selectedToolName = self.endTool.toolName;
        }

        getCorrelatedToolsData() {
            var self = this
            self.showNoToolsSelectedForCorrelation = false;
            self.showCorrelationsLoadThrobber = "true";
            self.dataDictionaryService.getToolsRelationshipAndProperties(self.startTool.toolName, self.startTool.categoryName, self.endTool.toolName, self.endTool.categoryName)
                .then(function (response) {
                    self.data = response.data;
                    self.showCorrelationsLoadThrobber = "false";
                    self.correlationProperties = self.data;
                })
                .catch(function (response) {
                    self.showMessage = "Something wrong with Service, Please try again.";
                });
        }

    }
}