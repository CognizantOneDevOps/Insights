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
    export class DataOnBoardingController {
        static $inject = ['restEndpointService', 'onboardProjectService', 'dataTaggingService', '$cookies', '$location', '$window', '$mdDialog', 'roleService'];
        constructor(private restEndpointService: IRestEndpointService, private onboardProjectService, private dataTaggingService: IDataTaggingService, private $cookies,
            private $location, private $window, private $mdDialog, private roleService: IRoleService) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            this.onboardProjectService
                .getAllOrg()
                .then(function (data) {
                    var orgDataArray = data.data;
                    self.orgList = orgDataArray;
                    self.createAction(self.orgList[0].name);
                    //self.selectedProj = self.orgList[0].name;
                });

            this.onboardProjectService
                .getAllHierarchyName()
                .then(function (data) {
                    var orgDataArray = data.data;
                    self.hierarchyList = orgDataArray;
                    self.selectedHierarchy = self.hierarchyList[0];
                    self.onSelectionOfHierarchy(self.hierarchyList[0]);
                });
            this.toolSelection(1);
        }
        showAddApplication: boolean = false;
        homeController: HomePageController;
        addNewApplicationName: string;
        orgList = [];
        notAuthorizeMsg: string = "";
        selectedProj: string;
        orgId: number;
        toolsRowModel = new ToolRowsModel();
        oboardProjectModel = new OnboardProjectDataModel();
        toolsList = [];
        fieldList = [];
        projectDetails = {};
        toolCategory: string;
        showThrobber: boolean;
        saveMsg: string;
        fieldValList = [];
        fetchedToolsData = [];
        toolsFieldsArrayJson = {};
        toolsFieldsValueArrayJson = {};
        saveToolCategory: string = "";
        searchToolData: string = "";
        searchSelectedToolData: string = "";
        saveMsgColor: string;
        toolCategoryArray = [];
        showTemplateAfterLoad: boolean = false;
        disableAddButtonStatus: boolean = true;
        addButtonIcon: string = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
        disableSaveButtonStatus: boolean = true;
        saveButtonIcon: string = "dist/icons/svg/oneToolsConfigIcons/Save_icon_Disabled.svg";
        showApplicationAddedMessage: boolean = false;
        disableDeleteButtonStatus: boolean = true;
        deleteButtonIcon: string = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_disabled.svg";

        selectedHierarchy: string;
        hierarchyList = [];
        noHierarchyMsg: string;

        heirarchy = new Mapping();
        heirarchyDefModel = new MappingDefinitionModel();

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
        goToOnBoard(): void {
            this.homeController.templateName = 'userOnboarding';
        }

        goToDataTagging(): void {
            this.homeController.templateName = 'dataTagging';
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

        searchtool(): void {
            var self = this;
            self.searchSelectedToolData = self.searchToolData;
        }
        toolSelection(rowId: number): void {
            var self = this;
            self.onboardProjectService
                .getToolName()
                .then(function (data) {
                    self.toolsList[rowId] = data.data;
                    for (let i = 0; i < self.toolsList[rowId].length; i++) {
                        self.toolsFieldsArrayJson[self.toolsList[rowId][i]] = [];
                    }
                });
        }
        createAction(orgName: string): void {
            this.showThrobber = true;
            this.toolsRowModel.toolRows = [];
            this.saveMsg = "";
            for (var key in this.orgList) {
                var orgDtl = this.orgList[key];
                if (orgDtl.name === orgName) {
                    this.orgId = orgDtl.id;
                }
            }
            //this.fetchProjectMapping();
        }

        onSelectionOfHierarchy(hierarchyName): void {
            this.showThrobber = true;
            this.toolsRowModel.toolRows = [];
            this.noHierarchyMsg = '';
            this.saveMsg = "";
            var self = this;
            this.fetchProjectMapping();
        }

        fetchAutocompleteArray(): void {
            var self = this;
            self.toolCategoryArray = [];
            for (var i = 0; i < self.toolsRowModel.toolRows.length; i++) {
                if (self.toolCategoryArray.indexOf(self.toolsRowModel.toolRows[i].selectedTool) === -1) {
                    self.toolCategoryArray.push(self.toolsRowModel.toolRows[i].selectedTool);
                }
            }
        }
        fetchProjectMapping(): void {
            var self = this;
            self.onboardProjectService
                .fetchProjectMappingByHierarchyName(self.selectedHierarchy)
                .then(function (data) {
                    var fetchedToolsData = data.data;
                    if (fetchedToolsData.length != 0) {
                        self.showThrobber = false; 
                        self.noHierarchyMsg = '';
                        self.disableAddButtonStatus = false;
                        self.addButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_MouseOver.svg";
                        for (let jsonObject of fetchedToolsData) {
                            var fetchedData = new OnboardProjectDataModel();
                            fetchedData.selectedFieldName = jsonObject["fieldName"];
                            fetchedData.selectedTool = jsonObject["toolName"];
                            fetchedData.toolCategory = jsonObject["category"];
                            fetchedData.selectedFieldValues = jsonObject["fieldValue"];
                            fetchedData.toolId = jsonObject["rowId"];
                            fetchedData.isSaved = true;
                            self.toolsRowModel.toolRows.push(fetchedData);
                        }
                        self.fetchAutocompleteArray();
                    } else if (self.hierarchyList.length !== 0) {
                        self.showThrobber = false; 
                        self.disableAddButtonStatus = true;
                        self.addButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
                        self.toolsRowModel.toolRows = [];
                        var rowLength = self.toolsRowModel.toolRows.length + 1;
                        var insertTool = new OnboardProjectDataModel();
                        insertTool.toolId = rowLength;
                        insertTool.toolCategory = "";
                        insertTool.isSaved = false;
                        self.toolsRowModel.toolRows.push(insertTool);
                    } else if (self.hierarchyList.length === 0) {
                        self.showThrobber = false;
                        self.noHierarchyMsg = "No Hierarchy Added. Please add hierarchy using Data Tagging";
                    }
                    self.checkRecordCount();
                    self.updateTable();
                    self.showTemplateAfterLoad = true;
                    self.showThrobber = false;
                });
        }
        fieldSelection(selectedTool: string, toolId: number): void {
            var self = this;
            for (let i = 0; i < self.toolsRowModel.toolRows.length; i++) {
                if (self.toolsRowModel.toolRows[i].toolId === toolId) {
                    self.onboardProjectService
                        .getToolcat(self.toolsRowModel.toolRows[i].selectedTool)
                        .then(function (data) {
                            if (self.toolsRowModel.toolRows[i].toolCategory === undefined) {
                                self.toolsRowModel.toolRows[i].toolCategory = data.data[0];
                            }
                        });
                }
            }
            if (self.toolsFieldsArrayJson[selectedTool].length !== 0) {
            }
            else {
                self.onboardProjectService
                    .getPrjtMappingFields(selectedTool)
                    .then(function (data) {
                        if (data.data.nodes[0] !== undefined) {
                            var fieldListObject = data.data.nodes[0].propertyMap;
                            var fieldsListArray = Object.keys(fieldListObject);
                            for (var tool in self.toolsFieldsArrayJson) {
                                if (tool === selectedTool) {
                                    self.toolsFieldsArrayJson[tool] = fieldsListArray;
                                }
                            }
                        }
                    });
            }
        }
        fieldValueSelection(selectedTool: string, selectedField: string, toolId: number): void {
            var self = this;
            self.projectToolMapping(selectedTool, toolId);

            self.onboardProjectService
                .getPrjtMappingFieldVal(selectedTool, selectedField)
                .then(function (data) {
                    var fieldValListArry = data.data.nodes;
                    var propertyVal = [];
                    for (var key in fieldValListArry) {
                        if (fieldValListArry[key] !== undefined) {
                            propertyVal.push(fieldValListArry[key].propertyMap.row);
                        }
                        var rowSpecificToolId = selectedTool + "_" + toolId;
                        self.toolsFieldsValueArrayJson[rowSpecificToolId] = propertyVal;
                    }
                });
        }
        addAction(toolName: string, rowId: number): void {
            this.disableAddButtonStatus = true;
            this.addButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
            this.disableSaveButtonStatus = true;
            this.saveButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Save_icon_Disabled.svg";
            this.saveMsg = "";
            var toolsIdArray = [];
            for (var i = 0; i < this.toolsRowModel.toolRows.length; i++) {
                if (this.toolsRowModel.toolRows[i]['toolId'] !== undefined)
                    toolsIdArray.push(this.toolsRowModel.toolRows[i]['toolId']);
            };
            toolsIdArray.sort(this.agentIdSort);
            var rowLength = parseInt(toolsIdArray[toolsIdArray.length - 1]) + 1;
            var insertTool = new OnboardProjectDataModel();
            insertTool.toolId = rowLength;
            insertTool.toolCategory = "";
            insertTool.isSaved = false;
            this.toolsRowModel.toolRows.push(insertTool);
            this.updateTable();
        }
        agentIdSort(a: number, b: number): number {
            var d = a - b;
            return d;
        };
        projectToolMapping(toolName: string, toolId: number): void {
            var self = this;
            self.onboardProjectService
                .getToolcat(toolName)
                .then(function (data) {
                    if (data.data[0] !== undefined) {
                        self.saveToolCategory = data.data[0];
                        for (var i = 0; i < self.toolsRowModel.toolRows.length; i++) {
                            if (self.toolsRowModel.toolRows[i].toolId === toolId) {
                                if (self.toolsRowModel.toolRows[i].toolCategory !== undefined)
                                    self.toolsRowModel.toolRows[i].toolCategory = self.saveToolCategory;
                            }
                        }
                    }
                });
        }
        saveAction(status: boolean): void {
            if (status === true) {
                var self = this;
                self.onboardProjectService
                    .fetchProjectMappingByHierarchyName(this.selectedHierarchy)
                    .then(function (data) {
                        var fetchedData = data.data;
                        var toolsRowArray = self.toolsRowModel.toolRows;
                        if (fetchedData.length != 0) {
                            for (var row in toolsRowArray) {
                                var flag = 0;
                                var rowDetails = toolsRowArray[row];
                                let retObj = fetchedData.find(element => {
                                    return rowDetails.selectedTool === element.toolName &&
                                        rowDetails.selectedFieldName === element.fieldName &&
                                        rowDetails.selectedFieldValues === element.fieldValue;
                                })
                                if (retObj === undefined) {
                                    if (rowDetails.selectedFieldName !== undefined && rowDetails.selectedFieldValues !== undefined) {
                                        rowDetails.isSaved = true;
                                        if (self.selectedProj === undefined) {
                                            self.dataTaggingService.getHierarchyMappingByName(self.selectedHierarchy).then(function (data) {
                                                self.selectedProj = data.data[0];
                                            });
                                        }
                                        self.onboardProjectService
                                            .addProjectMapping(self.orgId, rowDetails.toolId, rowDetails.toolCategory, rowDetails.selectedTool, rowDetails.selectedFieldName,
                                            rowDetails.selectedFieldValues, self.selectedProj, "1", "BU", self.selectedHierarchy)
                                            .then(function (data) {
                                                var orgDataArray = data;
                                                if (data.status === "success") {
                                                    self.checkRecordCount();
                                                    self.saveMsg = "Data saved sucessfully !";
                                                    self.fetchAutocompleteArray();
                                                }
                                                else {
                                                    self.saveMsg = "Failed to save data !";
                                                    rowDetails.isSaved = false;
                                                }
                                            });
                                    }
                                }
                            }
                        }
                        else if (fetchedData.length === 0) {
                            for (var row in toolsRowArray) {
                                var rowDetails = toolsRowArray[row];
                                rowDetails.isSaved = true;
                                self.onboardProjectService
                                    .addProjectMapping(self.orgId, rowDetails.toolId, rowDetails.toolCategory, rowDetails.selectedTool, rowDetails.selectedFieldName,
                                    rowDetails.selectedFieldValues, self.selectedProj, "1", "BU", self.selectedHierarchy)
                                    .then(function (data) {
                                        var orgDataArray = data;
                                        if (data.status === "success") {
                                            self.checkRecordCount();
                                            self.saveMsg = "Data saved sucessfully !";
                                            self.fetchAutocompleteArray();
                                        }
                                        else {
                                            self.saveMsg = "Failed to save data !";
                                            rowDetails.isSaved = false;
                                        }
                                    });
                            }
                        }
                    });
            }
        }
        deleteAction(status, toolName: string, category: string, toolId: number, isSaved: boolean): void {
            var self = this;
            if (isSaved === true) {
                if (status === true && toolName !== undefined) {
                    this.toolsRowModel.toolRows = [];
                    this.onboardProjectService
                        .deleteToolMapping(self.orgId, category, toolName, toolId)
                        .then(function (data) {
                            if (data.status === "success") {
                                self.saveMsg = "Data deleted sucessfully !";
                            }
                            else {
                                self.saveMsg = "Failed to delete data !";
                            }
                            self.fetchProjectMapping();
                        });
                }
            }
            else {
                for (var i = 0; i < this.toolsRowModel.toolRows.length; i++) {
                    var obj = this.toolsRowModel.toolRows[i];
                    if (obj.toolId === toolId) {
                        this.toolsRowModel.toolRows.splice(i, 1);
                        i--
                        break;
                    }
                }
                this.changeButtonstatus();
                self.updateTable();
            }
        }
        openDialog(params, selectedOperation, toolName, category, toolId, isSaved): void {

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
                    operationName: 'Data Mapping'
                },
                bindToController: true,
                onRemoving: function () {
                    if (selectedOperation === 'save') {
                        self.saveAction(statusObject.status);
                    }
                    else if (selectedOperation === 'delete') {
                        self.deleteAction(statusObject.status, toolName, category, toolId, isSaved);
                    }
                }
            })
        }
        changeButtonstatus(): void {
            var self = this;
            self.disableAddButtonStatus = false;
            self.addButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_MouseOver.svg";
            self.disableSaveButtonStatus = false;
            self.saveButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Save_icon_MouseOver.svg";
        }
        totalRows: number = 10;//total items per page
        maxSize = 4;// total pages blocks will be displayed
        totalItems: number;//total no of items available
        currentPage = 1;//current page selected
        begin = 0;
        end = 10;
        paginatedDataOnboardedArray = [];
        showPaginationBar: boolean = false;
        setPage(pageNo) {
            this.currentPage = pageNo;
        };
        pageChanged() {
            //this.$log.log('Page changed to: ' + this.currentPage);
        };
        updateResult() {
            var self = this;
            self.begin = (self.currentPage - 1) * self.totalRows;
            self.end = self.begin + 10;
            self.updateTable();
        }
        updateTable() {
            var self = this;
            self.totalItems = self.toolsRowModel.toolRows.length;
            if (self.totalItems > 10) {
                self.showPaginationBar = true;
            }
            else {
                self.showPaginationBar = false;
            }
            self.paginatedDataOnboardedArray = [];
            self.paginatedDataOnboardedArray = self.toolsRowModel.toolRows.slice(self.begin, self.end);
        }

        checkRecordCount() {
            var self = this;
            if (self.toolsRowModel.toolRows.length === 1 && self.toolsRowModel.toolRows[0].isSaved === false) {
                self.disableDeleteButtonStatus = true;
                self.deleteButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_disabled.svg";
            }
            else {
                self.disableDeleteButtonStatus = false;
                self.deleteButtonIcon = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_MouseOver.svg";
            }

        }

        /*addDataMapping(params): void {
            var self = this;
            self.$mdDialog.show({
                controller: DataMappingController,
                controllerAs: 'dataMappingController',
                templateUrl: './dist/modules/dataOnBoarding/view/dataMappingDialog.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                preserveScope: true,
                clickOutsideToClose: true,
                locals: {
                    hierarchyList: self.hierarchyList,
                    orgList: self.orgList,
                    hierarchyMapping: self.hierarchyMapping
                },
                bindToController: true,
            })
        }*/
    }
}
