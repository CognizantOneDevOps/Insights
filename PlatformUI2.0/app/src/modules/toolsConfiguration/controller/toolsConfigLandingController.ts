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
    export class ToolsConfigurationController {
        static $inject = ['$location', '$window', 'toolConfigService'];
        constructor(private $location, private $window, private toolConfigService: IToolConfigService) {
            var self = this;
            self.toolsData = self.toolConfigService.readToolsDataList();
            self.toolConfigService
                .readToolsConfigurationGlobal().then(function (data) {
                    var dataArray = data.data;
                    if (dataArray !== undefined) {
                        for (var i = 0; i < dataArray.length; i++) {
                            self.configuredToolsList.push(dataArray[i].toolName);
                        }
                    }
                    self.showTemplateAfterLoad = true;
                });
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
        }
        /*start for common code */
        isTabSeleted: boolean = false;
        imageurl1: string = "dist/icons/svg/landingPage/Admin_icon_selected.svg";
        imageurl2: string = "dist/icons/svg/landingPage/Dashboard_icon_normal.svg";
        imageurl3: string = "dist/icons/svg/landingPage/Healthcheck_icon_normal.svg";
        homeController: HomePageController;
        showTemplateAfterLoad: boolean  = false;
        redirect(iconId: string): void {
            if (iconId == 'dashboard') {
                this.$location.path('/InSights/dashboard');
            } else if (iconId == 'settings') {
                this.$location.path('/InSights/toolsConfig');
            } else if (iconId == 'graphview') {
                this.$location.path('/InSights/explore');
            } else if (iconId == 'userview') {
                this.$location.path('/InSights/roles');
            } else if (iconId == 'prjtmapping') {
                this.$location.path('/InSights/onboardProject');
            } else if (iconId == 'healthcheck') {
                this.$location.path('/InSights/agent');
            }
        }
        selectAct(selVal: boolean): void {
            this.isTabSeleted = false;
            this.isTabSeleted = selVal;
        }
        addSelectedImage(selectedTab: string): void {
            var self = this;
            if (selectedTab == 'Admin') {
                self.imageurl1 = "dist/icons/svg/landingPage/Admin_icon_selected.svg";
            }
            else if (selectedTab == 'Dashboards') {
                self.imageurl2 = "dist/icons/svg/landingPage/Dashboard_icon_selected.svg";
            }
            else if (selectedTab == 'HealthCheck') {
                self.imageurl3 = "dist/icons/svg/landingPage/Healthcheck_icon_selected.svg";
            }
        }
        removeSelectedImage(selectedTab: string): void {
            var self = this;
            if (selectedTab == 'Admin') {
                self.imageurl1 = "dist/icons/svg/landingPage/Admin_icon_normal.svg";
            }
            else if (selectedTab == 'Dashboards') {
                self.imageurl2 = "dist/icons/svg/landingPage/Dashboard_icon_normal.svg";
            }
            else if (selectedTab == 'HealthCheck') {
                self.imageurl3 = "dist/icons/svg/landingPage/Healthcheck_icon_normal.svg";
            }
        }
        /*end for common code */
        /* code for tools configuration landing page*/
        configuredToolsList = [];
        openToolUrl(toolCategory: string, toolName: string): void {
            this.homeController.selectedToolName = toolName;
            this.homeController.selectedToolCategory = toolCategory;
            this.homeController.templateName = 'oneToolConfigured';
        }
        toToolsConfiguredpage(): void {
            this.homeController.templateName = 'configuredTools';
        }
        toolsData = [];
    }
}