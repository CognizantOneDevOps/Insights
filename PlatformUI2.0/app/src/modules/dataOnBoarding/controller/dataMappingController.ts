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

    export class DataMappingController {
        static $inject = ['$mdDialog', '$route', 'dataTaggingService', 'restEndpointService'];

        constructor(private $mdDialog, private $route, private dataTaggingService: IDataTaggingService, private restEndpointService: IRestEndpointService) {
            this.orgList = this['locals'].orgList;
            this.hierarchyList = this['locals'].hierarchyList;
            this.hierarchyMapping = this['locals'].hierarchyMapping;
            this.addOrgMapping();
            this.fetchHierarchyMapping();
            this.showActions = false;
        }

        hierarchyList = [];
        count: number = 1;
        orgList = [];
        addButtIcon: string = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
        deleteButtIcon: string = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_disabled.svg";
        editButtIcon: string = "dist/icons/svg/userOnboarding/Edit_icon_disabled.svg";
        showActions: boolean = false;
        mapping = new Mapping();
        mappingDefModel = new MappingDefinitionModel();
        shuldAddEntity: boolean = false;
        mappingDefinationArray = [];
        hierarchyMapping = {};
        selectedProj: string;

        hide(): void {
            this.$mdDialog.hide();
        }

        cancel(): void {
            this.$mdDialog.cancel();
        }

        addOrgMapping(): void {
            this.showActions = false;
            var rowLength = this.mappingDefModel.mappingDefinitionRows.length + 1;
            var insertMapping = new Mapping();
            insertMapping.rowId = rowLength;
            this.mappingDefModel.mappingDefinitionRows.push(insertMapping);
            this.addButtIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
            console.log(this.mappingDefModel.mappingDefinitionRows)
        }

        changeButtonstatus(): void {
            this.showActions = true;
            this.addButtIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_MouseOver.svg";
            this.deleteButtIcon = "dist/icons/svg/oneToolsConfigIcons/Delete_icon_MouseOver.svg";
            this.editButtIcon = "dist/icons/svg/userOnboarding/Edit_icon_MouseOver.svg";
        }

        addEntity(): void {
            this.shuldAddEntity = true;
        }

        addOrgHierarchyMapping(): void {
            var self = this;
            var mappingArr = self.mappingDefModel.mappingDefinitionRows;
            for (var mapping in mappingArr) {
                var hierarchyMappRowDetails = mappingArr[mapping];
                if (hierarchyMappRowDetails.hierarchyName  != undefined && hierarchyMappRowDetails.orgName != undefined) {
                    self.dataTaggingService
                        .addHierarchyMapping(hierarchyMappRowDetails.rowId, hierarchyMappRowDetails.hierarchyName, hierarchyMappRowDetails.orgName, 1)
                        .then(function (data) {
                            if (data.status === "success") {
                                hierarchyMappRowDetails.isMappingDefSaved = true;
                            } else {
                                hierarchyMappRowDetails.isMappingDefSaved = false;
                            }
                        });
                }
            }
            this.hide();
        }

        fetchHierarchyMapping(): void {
            var self = this;
            self.dataTaggingService.getAllHierarchyMapping()
                .then(function (data) {
                    var fetchedHierarchyMapData = data.data;
                    if (fetchedHierarchyMapData.length != 0) {
                        self.showActions = true;
                        self.addButtIcon = "dist/icons/svg/actionIcons/Add_icon_MouseOver.svg";
                        self.deleteButtIcon = "dist/icons/svg/actionIcons/Delete_icon_MouseOver.svg";
                        self.mappingDefModel.mappingDefinitionRows = [];
                        for (let jsonObject of fetchedHierarchyMapData) {
                            var fetchedData = new Mapping();
                            fetchedData.hierarchyName = jsonObject["hierarchyName"];
                            fetchedData.orgName = jsonObject["orgName"];
                            fetchedData.orgId = jsonObject["orgId"];
                            fetchedData.rowId = jsonObject["rowId"];
                            fetchedData.isMappingDefSaved = true;
                            self.mappingDefModel.mappingDefinitionRows.push(fetchedData);
                        }
                    } else if (fetchedHierarchyMapData.length === 0) {
                        self.addButtIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
                        self.mappingDefModel.mappingDefinitionRows = [];
                        var rowLength = self.mappingDefModel.mappingDefinitionRows.length + 1;
                        var insertTool = new Mapping();
                        insertTool.rowId = rowLength;
                        insertTool.isMappingDefSaved = false;
                        self.mappingDefModel.mappingDefinitionRows.push(insertTool);
                    }
                });
        }

        deleteAction(hierarchyName: string, orgName: string, rowId: number, isMappingDefSaved: boolean): void {
            this.showActions = false;
            if (isMappingDefSaved === true) {
                if (hierarchyName != undefined) {
                    var self = this;
                    self.dataTaggingService
                        .deleteHierarchyMap(hierarchyName, orgName)
                        .then(function (data) {
                            if (data.status === "success") {
                                self.fetchHierarchyMapping();
                            }
                        });

                }
            }
            else {
                for (var i = 0; i < this.mappingDefModel.mappingDefinitionRows.length; i++) {
                    var obj = this.mappingDefModel.mappingDefinitionRows[i];
                    if (obj.rowId === rowId && rowId != 1) {
                        this.mappingDefModel.mappingDefinitionRows.splice(i, 1);
                        i--
                        break;
                    } else {
                        this.mappingDefModel.mappingDefinitionRows = [];
                        var rowLength = this.mappingDefModel.mappingDefinitionRows.length + 1;
                        var insertMapping = new Mapping();
                        insertMapping.rowId = rowLength;
                        this.mappingDefModel.mappingDefinitionRows.push(insertMapping);
                        this.addButtIcon = "dist/icons/svg/oneToolsConfigIcons/Add_icon_disabled.svg";
                    }
                }
                this.changeButtonstatus();
            }
        }
    }
}

