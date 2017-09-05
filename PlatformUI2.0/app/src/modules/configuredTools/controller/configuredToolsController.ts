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

/// <reference path="../../../_all.ts" />

module ISightApp {
    export class ConfiguredToolsController {
        static $inject = ['$location', '$window', 'toolConfigService'];
        constructor(private $location, private $window, private toolConfigService: IToolConfigService) {
            var self = this;
            self.toolsData = self.toolConfigService.readToolsDataList();
            self.toolConfigService
                .readToolsConfigurationGlobal().then(function (data) {
                    var dataArray = data.data;
                    if (dataArray !== undefined) {
                        for (var i = 0; i < dataArray.length; i++) {
                            self.configuredToolsList.push(dataArray[i]);
                            self.configuredToolsListName.push(dataArray[i].toolName);
                            if (self.configuredToolsCategoryList.indexOf(dataArray[i].category) == -1) {
                                self.configuredToolsCategoryList.push(dataArray[i].category);
                            }
                        }
                    }
                    self.calculateConfiguredToolsCount();
                    self.totalConfiguredTools = Object.keys(self.configuredToolsCount).length;
                    self.showTemplateAfterLoad = true;
                });
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
        }
        /* start for configured tools code*/
        configuredToolsList = [];
        configuredToolsListName = [];
        configuredToolsCount = {};
        totalConfiguredTools: number = 0;
        configuredToolsCategoryList = [];
        homeController: HomePageController;
        showTemplateAfterLoad = false;
        toToolsConfigurationLandingPage(): void {
            var self = this;
            this.homeController.templateName = 'toolsConfiguration';
        }
        calculateConfiguredToolsCount(): void {
            var self = this;
            var toolsList = self.configuredToolsListName;
            for (var i = 0, j = toolsList.length; i < j; i++) {
                if (self.configuredToolsCount[toolsList[i]]) {
                    self.configuredToolsCount[toolsList[i]]++;
                }
                else {
                    self.configuredToolsCount[toolsList[i]] = 1;
                }
            }
        }
        openToolUrl(toolCategory: string, toolName: string): void {
            this.homeController.selectedToolName = toolName;
            this.homeController.selectedToolCategory = toolCategory;
            this.homeController.templateName = 'oneToolConfigured';
        }
        getCategory(toolName): string {
            var self = this;
            for (var i = 0; i < self.configuredToolsList.length; i++) {
                if (self.configuredToolsList[i].toolName === toolName) {
                    return self.configuredToolsList[i].category;
                }
            }
        }
        toolsData = [];
        toolsInfDataArray = [];
        /* end for configured tools code*/
       /* getCategoryName(category): void {
            let categoryName = category;
            for (let tool in this.toolsData) {
                if (this.toolsData[tool].category === categoryName) {
                    return this.toolsData[tool].categoryDisplayName;
                }
            }
        }
         getCategoryAbbreviatedName(category): void {
            let categoryName = category;
            for (let tool in this.toolsData) {
                if (this.toolsData[tool].category === categoryName) {
                    return this.toolsData[tool].toolTipName;
                }
            }
        }
        getToolName(category, tool): void {
            let categoryName = category;
            let toolActualName = tool;
            for (let tool in this.toolsData) {
                if (this.toolsData[tool].category === categoryName) {
                    var toolsValueArray = this.toolsData[tool].tools;
                    for (let toolValue in toolsValueArray) {
                        if (toolsValueArray[toolValue].toolName === toolActualName) {
                            return toolsValueArray[toolValue].toolDisplayName;
                        }
                    }
                }
            }
        }*/
    }
}