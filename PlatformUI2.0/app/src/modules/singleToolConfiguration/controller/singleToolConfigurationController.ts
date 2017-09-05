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
    export class SingleToolConfigurationController {
        static $inject = ['$location', '$window', 'singleToolConfigService', '$mdDialog', '$routeParams', '$sce', '$timeout'];
        constructor(private $location, private $window, private singleToolConfigService: ISingleToolConfigService, private $mdDialog, private $routeParams, private $sce, private $timeout) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            self.selectTool();
            self.singleToolConfigService
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
        ToolConfigurationPageModel = new ToolConfigurationPageModel();
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
        editIconSrc: string = "dist/icons/svg/userOnboarding/Edit_icon_disabled.svg";
        showTemplate: boolean = true;
        saveMsg: string = "";
        showTemplateAfterLoad: boolean = false;
        authMethod = ['Access Token', 'UserId/Password'];
        editAgentId: number = 0;
        selectTool(): void {
            this.toolCategory = this.homeController.selectedToolCategory;
            this.toolName = this.homeController.selectedToolName;
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

        disableActionButtons(): void {
            var self = this;
            self.buttonDisableStatus = false;
            self.saveButtonUrl = "dist/icons/svg/oneToolsConfigIcons/Save_icon_MouseOver.svg";
            self.removeButtonUrl = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_MouseOver.svg";
            self.downloadButtonUrl = "dist/icons/svg/oneToolsConfigIcons/Download_icon_MouseOver.svg";
            self.editIconSrc = "dist/icons/svg/userOnboarding/Edit_icon_MouseOver.svg";
        }
        toolsInfo(): void {
            var self = this;
            self.timezoneList = self.singleToolConfigService.readTimeZonelist();
            self.singleToolConfigService
                .readToolsConfiguration(self.toolName, self.toolCategory).then(function (data) {
                    self.dataArray = data.data;
                    if (self.dataArray.length != 0) {
                        for (let jsonObject of self.dataArray) {
                            var insertObject = new ToolConfigurationDetail();
                            insertObject.agentId = jsonObject.agentId;
                            insertObject.category = jsonObject.toolCategory;
                            insertObject.toolName = jsonObject.toolName;
                            insertObject.ToolConfigurationDataModel = jsonObject;
                            delete insertObject.ToolConfigurationDataModel['agentId'];
                            delete insertObject.ToolConfigurationDataModel['category'];
                            delete insertObject.ToolConfigurationDataModel['toolName'];
                            self.ToolConfigurationPageModel.toolsConfigRows.push(insertObject);
                        }
                        self.showTemplateAfterLoad = true;
                    }
                    else {
                        var rowLength = 1;
                        var insertObject = new ToolConfigurationDetail();
                        insertObject.agentId = rowLength;
                        insertObject.category = self.toolCategory;
                        insertObject.toolName = self.toolName;
                        insertObject.ToolConfigurationDataModel = new ToolConfigurationDataModel();
                        self.ToolConfigurationPageModel.toolsConfigRows.push(insertObject);
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
            self.editAgentId = agentId;
        }
        addAction(): void {
            this.saveMsg = "";
            var agentIdArray = [];
            var self = this;
            for (var i = 0; i < self.ToolConfigurationPageModel.toolsConfigRows.length; i++) {
                if (self.ToolConfigurationPageModel.toolsConfigRows[i]['agentId'] !== undefined)
                    agentIdArray.push(self.ToolConfigurationPageModel.toolsConfigRows[i]['agentId']);
            };
            agentIdArray.sort(self.agentIdSort);
            var rowLength = parseInt(agentIdArray[agentIdArray.length - 1]) + 1;
            var insertObject = new ToolConfigurationDetail();
            insertObject.agentId = rowLength;
            insertObject.category = self.toolCategory;
            insertObject.toolName = self.toolName;
            insertObject.ToolConfigurationDataModel = new ToolConfigurationDataModel();
            self.ToolConfigurationPageModel.toolsConfigRows.push(insertObject);
        }
        agentIdSort(a: number, b: number): number {
            var d = a - b;
            return d;
        };
        downloadConfig(): void {
            var self = this;
            var agentIdArray = [];
            for (var i = 0; i < self.ToolConfigurationPageModel.toolsConfigRows.length; i++) {
                agentIdArray.push(self.ToolConfigurationPageModel.toolsConfigRows[i].agentId);
            }
            if (agentIdArray.indexOf(self.selectedrows) > -1) {
                self.singleToolConfigService
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
                var ToolConfigurationPageModel = self.ToolConfigurationPageModel;
                for (let j = 0; j < ToolConfigurationPageModel.toolsConfigRows.length; j++) {
                    if (ToolConfigurationPageModel.toolsConfigRows[j]['agentId'] === self.selectedrows) {
                        self.singleToolConfigService
                            .deleteToolsConfig(self.toolName, self.toolCategory,ToolConfigurationPageModel.toolsConfigRows[j]['agentId'])
                            .then(function (data) {
                                if (data.status === 'success') {
                                    self.saveMsg = "Data deleted sucessfully!";
                                    ToolConfigurationPageModel.toolsConfigRows = [];
                                    self.singleToolConfigService
                                        .readToolsConfiguration(self.toolName, self.toolCategory).then(function (data) {
                                            self.dataArray = [];
                                            self.selectedrows = 0;
                                            self.dataArray = data.data;
                                            if (self.dataArray.length != 0) {
                                                for (let jsonObject of self.dataArray) {
                                                    var insertObject = new ToolConfigurationDetail();
                                                    insertObject.agentId = jsonObject.agentId;
                                                    insertObject.category = jsonObject.toolCategory;
                                                    insertObject.toolName = jsonObject.toolName;
                                                    insertObject.ToolConfigurationDataModel = jsonObject;
                                                    delete insertObject.ToolConfigurationDataModel['agentId'];
                                                    delete insertObject.ToolConfigurationDataModel['category'];
                                                    delete insertObject.ToolConfigurationDataModel['toolName'];
                                                    self.ToolConfigurationPageModel.toolsConfigRows.push(insertObject);
                                                }
                                            }
                                            else if (ToolConfigurationPageModel.toolsConfigRows.length === 0) {
                                                var rowLength = 1;
                                                var insertObject = new ToolConfigurationDetail();
                                                insertObject.agentId = rowLength;
                                                insertObject.category = self.toolCategory;
                                                insertObject.toolName = self.toolName;
                                                insertObject.ToolConfigurationDataModel = new ToolConfigurationDataModel();
                                                self.ToolConfigurationPageModel.toolsConfigRows.push(insertObject);
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
                for (let i = 0; i < self.ToolConfigurationPageModel.toolsConfigRows.length; i++) {
                    if (self.ToolConfigurationPageModel.toolsConfigRows[i]['agentId'] === self.selectedrows) {
                        var obj = self.ToolConfigurationPageModel.toolsConfigRows[i].ToolConfigurationDataModel;
                        obj['agentId'] = self.ToolConfigurationPageModel.toolsConfigRows[i]['agentId'];
                        obj['category'] = self.toolCategory;
                        obj['toolName'] = self.toolName;
                        self.singleToolConfigService
                            .saveToolsConfiguration(JSON.stringify(obj, null, 2));
                        self.saveMsg = "Data saved sucessfully!";
                        break;
                    }
                }
            }
        }
        openDialog(params, selectedOperation): void {
            var self = this;
            if (selectedOperation === 'delete' && Object.keys(self.ToolConfigurationPageModel.toolsConfigRows[0]).length === 5) {
                self.saveMsg = "please save data first!";
            }
            else {
                var statusObject = {
                    'status': false
                }
                self.$mdDialog.show({
                    controller: ShowSingleToolConfirmationDialogController,
                    controllerAs: 'showSingleToolConfirmationDialogController',
                    templateUrl: './dist/modules/singleToolConfiguration/view/oneToolConfirmationDialog.tmp.html',
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
        editConfigurations(params): void {
            var self = this;
            var toolConfigurationData;
            for (let i = 0; i < self.ToolConfigurationPageModel.toolsConfigRows.length; i++) {
                if (self.ToolConfigurationPageModel.toolsConfigRows[i]['agentId'] === self.editAgentId) {
                    toolConfigurationData = self.ToolConfigurationPageModel.toolsConfigRows[i];
                }
            }
            self.$mdDialog.show({
                controller: EditSingleToolConfigurationController,
                controllerAs: 'editSingleToolConfigurationController',
                templateUrl: './dist/modules/singleToolConfiguration/view/editSingleToolConfigurationHtml.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                preserveScope: true,
                clickOutsideToClose: true,
                locals: {
                    toolConfigurationData: toolConfigurationData
                },
                bindToController: true,
            })
        }
    }
}