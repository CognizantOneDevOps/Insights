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
    export class OneToolConfigurationController {
        static $inject = ['$location', '$window', 'toolConfigService', '$mdDialog', '$routeParams', '$sce', '$timeout'];
        constructor(private $location, private $window, private toolConfigService: IToolConfigService, private $mdDialog, private $routeParams, private $sce, private $timeout) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            self.selectTool();
            self.toolConfigService
                .readToolsConfigurationGlobal().then(function (data) {
                    var dataArray = data.data;
                    if (dataArray !== undefined) {
                        for (var i = 0; i < dataArray.length; i++) {
                            self.configuredToolsList.push(dataArray[i].toolName);
                        }
                    }
                });
            self.toolsInfo();
        }

        homeController: HomePageController;
        configuredToolsList = [];
        selectedrows: number = 0;
        ToolRows = new ToolRows();
        toolName = '';
        toolCategory = '';
        isOpen = false;
        selectedMode = 'md-fling';
        selectedDirection = 'left';
        authModData = '';
        downloadURL: string;
        categoryIndex: number;
        toolIndex: number;
        dataArray = [];
        toolsLayout = {
            jsonLayout: {}
        };
        timezoneList: any;
        buttonDisableStatus: boolean = true;
        saveButtonUrl: string = "dist/icons/svg/oneToolsConfigIcons/Save_icon_Disabled.svg";
        removeButtonUrl: string = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_disabled.svg";
        downloadButtonUrl: string = "dist/icons/svg/oneToolsConfigIcons/Download_icon_Disabled.svg";
        addRowButtonUrl: string = "dist/icons/svg/oneToolsConfigIcons/Add_icon_MouseOver.svg";
        showTemplate: boolean = true;
        saveMsg: string = "";
        showTemplateAfterLoad: boolean = false;


        selectTool(): void {
            this.toolCategory = this.homeController.selectedToolCategory;
            this.toolName = this.homeController.selectedToolName;
            var self = this;
            self.toolConfigService
                .readToolsLayoutJson(self.toolName, self.toolCategory).then(function (data) {
                    if (data.data !== undefined) {
                        self.toolsLayout.jsonLayout = data.data.layoutSettings;

                        self.showTemplate = true;
                    }
                    else {
                        self.showTemplate = false;
                    }
                });
        }
        onAuthenticationChange(selectedMode: string): void {
            this.authModData = selectedMode;
        }
        toToolsConfigurationLandingPage(): void {
            this.homeController.templateName = 'toolsConfiguration';
        }
        toToolsConfiguredpage(): void {
            this.homeController.templateName = 'configuredTools';
            //this.$location.path('/InSights/configuredTools');
        }
        addResponseTemplate(params, agentId): void {
            var self = this;
            var agentData = null;
            for (let i = 0; i < self.ToolRows.toolsConfigRows.length; i++) {
                if (self.ToolRows.toolsConfigRows[i]['agentId'] === agentId) {
                    agentData = self.ToolRows.toolsConfigRows[i];
                }
            }
            self.$mdDialog.show({
                controller: ShowTemplateResponseDialogController,
                controllerAs: 'showTemplateDialogController',
                templateUrl: './dist/modules/oneToolConfig/view/oneToolshowTemplate.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                clickOutsideToClose: true,
                locals: {
                    agentData: agentData
                },
                bindToController: true
            })
        }
        disableActionButtons(): void {
            var self = this;
            self.buttonDisableStatus = false;
            self.saveButtonUrl = "dist/icons/svg/oneToolsConfigIcons/Save_icon_MouseOver.svg";
            self.removeButtonUrl = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_MouseOver.svg";
            self.downloadButtonUrl = "dist/icons/svg/oneToolsConfigIcons/Download_icon_MouseOver.svg";
        }
        toolsInfo(): void {
            var self = this;
            self.timezoneList = self.toolConfigService.readTimeZonelist();
            self.toolConfigService
                .readToolsConfiguration(self.toolName, self.toolCategory).then(function (data) {
                    self.dataArray = data.data;
                    if (self.dataArray.length != 0) {
                        for (let jsonObject of self.dataArray) {
                            if (jsonObject['category'] === 'ALM') {
                                jsonObject['dataUpdateSupported'] = false;
                            }
                            self.ToolRows.toolsConfigRows.push(jsonObject);
                        }
                        self.showTemplateAfterLoad = true;
                    }
                    else {
                        var rowLength = 1;
                        var insertObject = new DataModel();
                        insertObject['category'] = self.toolCategory;
                        insertObject['toolName'] = self.toolName;
                        insertObject['agentId'] = rowLength;
                        insertObject['selectedAuthMtd'] = 'Access Token';
                        insertObject['useResponseTemplate'] = false;
                        if (self.toolCategory === 'ALM') {
                            insertObject['dataUpdateSupported'] = false;
                        }
                        self.ToolRows.toolsConfigRows.push(insertObject);
                        self.showTemplateAfterLoad = true;
                    }
                });
        }
        toggleSelectRow(agentId): void {
            var self = this;
            self.saveMsg = "";
            self.selectedrows = agentId;
            self.disableActionButtons();
            self.downloadConfig();
        }
        addAction(): void {
            this.saveMsg = "";
            var agentIdArray = [];
            var self = this;
            for (var i = 0; i < self.ToolRows.toolsConfigRows.length; i++) {
                if (self.ToolRows.toolsConfigRows[i]['agentId'] !== undefined)
                    agentIdArray.push(self.ToolRows.toolsConfigRows[i]['agentId']);
            };
            agentIdArray.sort(self.agentIdSort);
            var rowLength = parseInt(agentIdArray[agentIdArray.length - 1]) + 1;
            var insertObject = new DataModel();
            insertObject['category'] = self.toolCategory;
            insertObject['toolName'] = self.toolName;
            insertObject['agentId'] = rowLength;
            insertObject['selectedAuthMtd'] = 'Access Token';
            insertObject['useResponseTemplate'] = false;
            if (self.toolCategory === 'ALM') {
                insertObject['dataUpdateSupported'] = false;
            }
            self.ToolRows.toolsConfigRows.push(insertObject);
        }
        agentIdSort(a: number, b: number): number {
            var d = a - b;
            return d;
        };
        downloadConfig(): void {
            var self = this;
            var agentIdArray = [];
            for (var i = 0; i < self.dataArray.length; i++) {
                agentIdArray.push(self.dataArray[i].agentId);
            };
            if (agentIdArray.indexOf(self.selectedrows) > -1) {
                self.toolConfigService
                    .downloadAgentConfig(self.toolName, self.toolCategory, self.selectedrows).then(function (data) {
                        var content = data;
                        var blob = new Blob([JSON.stringify(content, null, 2)], { type: "application/json;charset=utf-8;" });
                        var URL = window.URL.createObjectURL(blob);
                        self.downloadURL = self.$sce.trustAsResourceUrl(URL);
                    });
            }
        }
        removeAction(status: boolean): void {
            if (status === true) {
                var self = this;
                var ToolRows = self.ToolRows;
                for (let j = 0; j < ToolRows.toolsConfigRows.length; j++) {
                    if (ToolRows.toolsConfigRows[j]['agentId'] === self.selectedrows) {
                        self.toolConfigService
                            .deleteToolsConfig(ToolRows.toolsConfigRows[j]['toolName'], ToolRows.toolsConfigRows[j]['category'], ToolRows.toolsConfigRows[j]['agentId'])
                            .then(function (data) {
                                if (data.status === 'success') {
                                    self.saveMsg = "Data deleted sucessfully!";
                                    ToolRows.toolsConfigRows = [];
                                    self.toolConfigService
                                        .readToolsConfiguration(self.toolName, self.toolCategory).then(function (data) {
                                            self.dataArray = [];
                                            self.selectedrows = 0;
                                            self.dataArray = data.data;
                                            if (self.dataArray.length != 0) {
                                                for (let jsonObject of self.dataArray) {
                                                    if (jsonObject['category'] === 'ALM') {
                                                        jsonObject['dataUpdateSupported'] = false;
                                                    }
                                                    ToolRows.toolsConfigRows.push(jsonObject);
                                                }
                                            }
                                            else if (ToolRows.toolsConfigRows.length === 0) {
                                                var rowLength = 1;
                                                var insertObject = new DataModel();
                                                insertObject['category'] = self.toolCategory;
                                                insertObject['toolName'] = self.toolName;
                                                insertObject['agentId'] = rowLength;
                                                insertObject['selectedAuthMtd'] = 'Access Token';
                                                insertObject['useResponseTemplate'] = false;
                                                if (self.toolCategory === 'ALM') {
                                                    insertObject['dataUpdateSupported'] = false;
                                                }
                                                self.ToolRows.toolsConfigRows.push(insertObject);
                                            }
                                        });
                                }
                                else {
                                    self.saveMsg = "Failed to delete data!";
                                }
                            });
                        break;
                    }
                }
            }
        }
        formSubmit(status: boolean): void {
            if (status === true) {
                var self = this;
                for (let i = 0; i < self.ToolRows.toolsConfigRows.length; i++) {
                    if (self.ToolRows.toolsConfigRows[i]['agentId'] === self.selectedrows) {
                        if (self.ToolRows.toolsConfigRows[i]['responseTemplate'] !== undefined) {
                            self.ToolRows.toolsConfigRows[i]['useResponseTemplate'] = true;
                        }
                        else {
                            self.ToolRows.toolsConfigRows[i]['useResponseTemplate'] = false;
                        }
                        if (self.toolCategory === 'ALM') {
                            if (self.ToolRows.toolsConfigRows[i]['uniqueKey'] !== undefined) {
                                self.ToolRows.toolsConfigRows[i]['dataUpdateSupported'] = true;
                            }
                            else {
                                self.ToolRows.toolsConfigRows[i]['dataUpdateSupported'] = false;
                            }
                        }
                        self.toolConfigService
                            .saveToolsConfiguration(JSON.stringify(self.ToolRows.toolsConfigRows[i], null, 2));
                        self.saveMsg = "Data saved sucessfully!";
                        break;
                    }
                }
            }
        }
        openDialog(params, selectedOperation): void {
            var self = this;
            if (selectedOperation === 'delete' && Object.keys(self.ToolRows.toolsConfigRows[0]).length === 5) {
                self.saveMsg = "please save data first!";
            }
            else {
                var statusObject = {
                    'status': false
                }
                self.$mdDialog.show({
                    controller: ShowToolConfirmationDialogController,
                    controllerAs: 'showToolConfirmationDialogController',
                    templateUrl: './dist/modules/oneToolConfig/view/oneToolConfirmationDialog.tmp.html',
                    parent: angular.element(document.body),
                    targetEvent: params,
                    preserveScope: true,
                    clickOutsideToClose: true,
                    locals: {
                        statusObject: statusObject,
                        selectedOperation: selectedOperation,
                        operationName: 'configuration'
                    },
                    bindToController: true,
                    onRemoving: function () {
                        if (selectedOperation === 'save') {
                            self.formSubmit(statusObject.status);
                        }
                        else if (selectedOperation === 'delete') {
                            self.removeAction(statusObject.status);
                        }
                    }
                })
            }
        }
    }
}