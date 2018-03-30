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
    export class DataTaggingController {
        static $inject = ['$location', '$window', '$mdDialog', 'dataTaggingService', 'roleService'];
        constructor(private $location, private $window, private $mdDialog, private dataTaggingService: IDataTaggingService, private roleService: IRoleService) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            this.showThrobber = true;
            self.dataTaggingService.getHierarchyDetails()
                .then(function (data) {
                    if (data.headers.length === 0) {
                        self.showThrobber = false;
                        self.noEntityAddedMsg['msg'] = "No Hierarchy Added"
                    } else {
                        self.showThrobber = false;
                        self.headerData['headers'] = data.headers;
                        var hierarchyRecords = data.records;
                        if (hierarchyRecords.length != 0) {
                            self.showActions = true;
                            self.fetchHierarchyData(data.records);
                        }
                    }
                });
        }

        homeController: HomePageController;
        addNewApplicationName: string;
        showAddApplication: boolean = false;
        showApplicationAddedMessage: boolean = false;
        dataTaggingModel = new DataTaggingModel();
        entityModel = new EntityModel();
        addButtIcon: string = "dist/icons/svg/actionIcons/Add_icon_disabled.svg";
        deleteButtIcon: string = "dist/icons/svg/actionIcons/Delete_icon_disabled.svg";
        editButtIcon: string = "dist/icons/svg/actionIcons/Edit_icon_disabled.svg";
        saveButtonIcon: string = "dist/icons/svg/actionIcons/Save_icon_Disabled.svg";
        showAddEntity: boolean = false;
        headerData = [];
        entityData = [];
        showActions: boolean = false;
        entity = new Entity();
        entityDefModel = new EntityDefinitionModel();
        noEntityAddedMsg = {
            'msg': '',
        }
        saveMsg: string = '';
        showThrobber: boolean;

        getToolDataLength(obj): number {
            return Object.keys(obj).length;
        }

        goToDataOnBoard(): void {
            this.homeController.templateName = 'dataOnboarding';
        }

        goToUserOnBoard(): void {
            this.homeController.templateName = 'userOnboarding';
        }

       
        addApplication(params, addedApplicationName): void {
            var self = this;
            var statusObject = {
                'status': false
            }
            self.$mdDialog.show({
                controller: ShowTemplateApplicationAddConformDialogController,
                controllerAs: 'showTemplateApplicationAddConformDialogController',
                templateUrl: './dist/modules/applicationManagement/view/conformApplicationAddDialogViewTemplate.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                preserveScope: true,
                clickOutsideToClose: true,
                locals: {
                    statusObject: statusObject,
                    addedApplicationName: addedApplicationName,
                },
                bindToController: true,
                onRemoving: function () { self.addApplicationConfirmation(statusObject.status) }
            })
        }
        addApplicationConfirmation(status): void {
            var self = this;
            if (status === true) {
                self.showApplicationAddedMessage = true;
                this.roleService
                    .createOrg(self.addNewApplicationName)
                    .then(function (data) {
                        var newAppData = {};
                        newAppData["name"] = self.addNewApplicationName;
                        newAppData["id"] = data.orgId;
                        newAppData["totalusers"] = 1;
                    });
            }
        }
        showAddApplicationBox(): void {
            this.showApplicationAddedMessage = false;
            if (this.showAddApplication === false) {
                this.showAddApplication = true;
            }
            else {
                this.showAddApplication = false;
            }
        }

        changeButtonstatus(): void {
            this.showActions = true;
            this.saveMsg = '';
            this.addButtIcon = "dist/icons/svg/actionIcons/Add_icon_MouseOver.svg";
            this.deleteButtIcon = "dist/icons/svg/actionIcons/Delete_icon_MouseOver.svg";
            this.editButtIcon = "dist/icons/svg/actionIcons/Edit_icon_MouseOver.svg";
            this.saveButtonIcon = "dist/icons/svg/actionIcons/Save_icon_MouseOver.svg"
        }

        idSort(a: number, b: number): number {
            var d = a - b;
            return d;
        };

        rowIdVal: number;
        levelList = [];
        addAction(): void {
            var self = this;
            self.levelList = [];
            for (var key in self.headerData['headers']) {
                self.levelList.push("");
            }
            var rowIdArray = [];
            for (var i = 0; i < self.entityModel.entityRows['entityInfo'].length; i++) {
                if (self.entityModel.entityRows['entityInfo'][i]['rowId'] !== undefined)
                    rowIdArray.push(self.entityModel.entityRows['entityInfo'][i]['rowId']);
            };
            rowIdArray.sort(self.idSort);
            var rowLength = parseInt(rowIdArray[rowIdArray.length - 1]) + 1;
            var insertEntity = new DataTaggingModel();
            insertEntity.rowId = rowLength;
            insertEntity.levels = self.levelList;
            insertEntity.isEntitySaved = false;
            self.entityModel.entityRows['entityInfo'].push(insertEntity);
            self.addButtIcon = "dist/icons/svg/actionIcons/Add_icon_disabled.svg";
            self.showActions = false;
        }

        showAddEntityBox(): void {
            this.showAddApplication = false;
            if (this.showAddEntity === false) {
                this.showAddEntity = true;
            }
            else {
                this.showAddEntity = false;
            }
        }

        openDialog(params, selectedOperation, rowId, levels, isEntitySaved): void {
            var self = this;
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
                    operationName: 'Data Tagging'
                },
                bindToController: true,
                onRemoving: function () {
                    if (selectedOperation === 'save') {
                        self.dataTaggingSaveAction();
                    }
                    else if (selectedOperation === 'delete') {
                        self.deleteAction(rowId, levels, isEntitySaved);
                    }
                }
            })
        }

        addEntity(params, entityName): void {
            var self = this;
            self.$mdDialog.show({
                controller: AddEntityDialogController,
                controllerAs: 'addEntityDialogController',
                templateUrl: './dist/modules/dataTagging/view/addEntityDialog.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                preserveScope: true,
                clickOutsideToClose: true,
                locals: {
                    headerData: self.headerData,
                    entityData: self.entityData,
                    noEntityAddedMsg: self.noEntityAddedMsg,
                    entityModel: self.entityModel
                },
                bindToController: true,
            })
        }

        dataTaggingSaveAction(): void {
            var self = this;
            self.dataTaggingService
                .getAllEntityData()
                .then(function (data) {
                    var fetchedData = data.data;
                    var entityArray = self.entityModel.entityRows['entityInfo'];
                    if (fetchedData.length != 0) {
                        for (var entityRow in entityArray) {
                            var count = 0;
                            var entityRowDetails = entityArray[entityRow];
                            var hierarchyName = self.buildHierarchyName(entityRowDetails.levels);
                            for (let jsonObject of fetchedData) {
                                if (hierarchyName === jsonObject["hierarchyName"]) {
                                    count++;
                                    break
                                }
                            }
                            if (count === 0) {
                                var hierarchyName = self.buildHierarchyName(entityRowDetails.levels);
                                self.dataTaggingService
                                    .addEntityData(entityRowDetails.rowId, entityRowDetails.levels[0], entityRowDetails.levels[1],
                                    entityRowDetails.levels[2], entityRowDetails.levels[3], entityRowDetails.levels[4],
                                    entityRowDetails.levels[5], hierarchyName)
                                    .then(function (data) {
                                        if (data.status === "success") {
                                            self.saveMsg = "Data saved sucessfully !";
                                            entityRowDetails.isEntitySaved = true;
                                        } else {
                                            self.saveMsg = "Data saved sucessfully !";
                                            entityRowDetails.isEntitySaved = false;
                                        }
                                    });
                            }
                        }
                    } else if (fetchedData.length === 0) {
                        for (var entityRow in entityArray) {
                            var entityRowDetailsData = entityArray[entityRow];
                            entityRowDetailsData.isEntitySaved = true;
                            var hierarchyNameData = self.buildHierarchyName(entityRowDetailsData.levels);
                            self.dataTaggingService
                                .addEntityData(entityRowDetailsData.rowId, entityRowDetailsData.levels[0], entityRowDetailsData.levels[1],
                                entityRowDetailsData.levels[2], entityRowDetailsData.levels[3], entityRowDetailsData.levels[4],
                                entityRowDetailsData.levels[5], hierarchyNameData)
                                .then(function (addData) {
                                    if (addData.status === "success") {
                                        self.saveMsg = "Data saved sucessfully !";
                                    } else {
                                        self.saveMsg = "Data saved sucessfully !";
                                        entityRowDetailsData.isEntitySaved = false;
                                    }
                                });
                        }
                    }
                });
        }

        hierarchyRec = [];
        fetchHierarchyData(fetchedHierarchyData): void {
            if (fetchedHierarchyData.length != 0) {
                //this.addButtIcon = "dist/icons/svg/actionIcons/Add_icon_MouseOver.svg";
                //this.deleteButtIcon = "dist/icons/svg/actionIcons/Delete_icon_MouseOver.svg";
                // this.entityData = [];
                this.entityModel.entityRows['entityInfo'] = [];
                var hierarchyDataRow = [];
                for (let hierarchyData of fetchedHierarchyData) {
                    var fetchedHierarchyDetails = new DataTaggingModel();
                    this.hierarchyRec = hierarchyData.record;
                    fetchedHierarchyDetails.levels = hierarchyData.record;
                    if (hierarchyData["rowId"] != undefined) {
                        fetchedHierarchyDetails.rowId = hierarchyData["rowId"];
                        fetchedHierarchyDetails.isEntitySaved = true;
                    } else {
                        var rowLength = this.entityModel.entityRows['entityInfo'].length + 1;
                        fetchedHierarchyDetails.rowId = rowLength;
                        fetchedHierarchyDetails.isEntitySaved = false;
                    }
                    hierarchyDataRow.push(fetchedHierarchyDetails)
                }
                this.entityModel.entityRows['entityInfo'] = hierarchyDataRow;
                this.entityData = this.hierarchyRec;
            }
        }

        buildHierarchyName(levels): string {
            var hierarchyName = '';
            var underscore = '.';
            var levelLength = Object.keys(levels).length;
            var count = 1;
            for (var key in levels) {
                if (levels[key] != 'undefined') {
                    hierarchyName += levels[key];
                    if (count !== levelLength) {
                        hierarchyName = hierarchyName + ".";
                        count++;
                    } else {
                        hierarchyName = hierarchyName;
                    }
                } else {
                    hierarchyName = hierarchyName.replace(/.\s*$/, "");;
                }
            }
            return hierarchyName;
        }

        deleteAction(rowId, levels, isEntitySaved): void {
            var hierarchyName = this.buildHierarchyName(levels);
            var self = this;
            if (isEntitySaved === true) {
                self.dataTaggingService.deleteEntityDataByHierarchy(hierarchyName)
                    .then(function (data) {
                        if (data.status === "success") {
                            self.saveMsg = "Data deleted sucessfully !";
                        }
                        else {
                            self.saveMsg = "Failed to delete data !";
                        }
                        self.dataTaggingService.getHierarchyDetails()
                            .then(function (data) {
                                var hierarchyRecords = data.records;
                                self.fetchHierarchyData(hierarchyRecords);
                            });
                    });
            }
            else {
                for (var i = 0; i < this.entityModel.entityRows['entityInfo'].length; i++) {
                    var obj = this.entityModel.entityRows['entityInfo'][i];
                    if (obj.rowId === rowId) {
                        this.entityModel.entityRows['entityInfo'].splice(i, 1);
                        if (rowId === 1) {
                            self.entityModel.entityRows['entityInfo'] = [];
                            self.levelList = [];
                            for (var key in self.headerData['headers']) {
                                self.levelList.push("");
                            }
                            var rowLength = self.entityModel.entityRows['entityInfo'].length + 1;
                            var insertTool = new DataTaggingModel();
                            insertTool.rowId = rowLength;
                            insertTool.levels = self.levelList;
                            insertTool.isEntitySaved = false;
                            self.entityModel.entityRows['entityInfo'].push(insertTool);
                        }
                        i--
                        break;
                    }
                }
                this.changeButtonstatus();
            }
        }
    }
}