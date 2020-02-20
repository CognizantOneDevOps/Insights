/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
import { Component, OnInit, Injectable } from '@angular/core';
import { BusinessMappingService } from './businessmapping.service';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { BehaviorSubject } from 'rxjs';
import { ToolLabelMapping } from '@insights/app/modules/admin/businessmapping/toolLabelMapping';
import { SelectionModel } from '@angular/cdk/collections';
import { MatTableDataSource } from '@angular/material';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DataSharedService } from '@insights/common/data-shared-service';

@Component({
  selector: 'app-businessmapping',
  templateUrl: './businessmapping.component.html',
  styleUrls: ['./businessmapping.component.css', './../../home.module.css']
})

export class BusinessMappingComponent implements OnInit {

  selectedTool: any;
  toolMappingLabels: ToolLabelMapping[] = [];
  toolDataSource: any = [];
  toolList: any = [];
  toolPropertyDataSource: any = [];
  displayedToolColumns: any = [];
  displayedColumns: any = [];
  selection: any = new SelectionModel(true, []);
  spans = [];
  isEditData = false;
  isListView = false;
  noToolsData = false;
  disableAdd = false;
  filteredToolList:any;
  label: String = undefined;
  toolPropertyList = {};
  selectedToolMappingLabels: ToolLabelMapping[] = [];
  selectedMappingTool: any = undefined;
  subHeading: String = "";
  now: any;
  currentUserName: String;
  masterToolPropertiesData: any;
  actionType: any;
  noToolsPropertyData = false;
  additionalProperties = ['inSightsTime', 'categoryName', 'inSightsTimeX', 'toolName','labelName',
    'uuid', 'type', 'businessmappinglabel', 'propertiesString', 'id', 'deleted', 'adminuser'];
  unwantedLabel = ['businessmappinglabel', 'propertiesString', 'id', 'type', 'deleted']
  selectedLabel: any;
  labelSourceListDatasourceSelected: any = []
  constructor(private businessMappingService: BusinessMappingService, public messageDialog: MessageDialogService,
    private dataShare: DataSharedService) {
    this.getToolInfo();
  }

  ngOnInit() {
    this.selectedTool = undefined;
    this.isListView = false;
    this.toolDataSource = [];
    this.selectedMappingTool = undefined;
    this.now = new Date();
    this.currentUserName = this.dataShare.getUserName();
  }


  // Loads Register Agent List
  async getToolInfo() {
    try {
      var dictResponse = await this.businessMappingService.loadToolsAndCategories();
      this.filteredToolList = dictResponse.data;
      if (dictResponse != null) {
        for (var key in dictResponse.data) {
          var toolname =dictResponse.data[key].toolName;
          if(this.toolList.indexOf(toolname)== -1){
            this.toolList.push(toolname);
          }
        }
     }
     console.log(this.toolList);
    } catch (error) {
      console.error(error);
    }
  }

  selectLabel(selectedToolData) {
    this.disableAdd = false;
    this.masterToolPropertiesData = undefined;
    this.selectedTool = selectedToolData;
    var self = this;
    this.labelSourceListDatasourceSelected = [];
    this.filteredToolList.filter(toolData => {
      if (toolData.toolName == selectedToolData) {
        if (toolData.labelName != null) {
          self.labelSourceListDatasourceSelected.push(toolData)
        }
      }
    } );
  }

  getToolMapping(selectedLabel) {
    var self = this;
    this.businessMappingService.loadToolProperties(this.selectedLabel.labelName, selectedLabel.categoryName)
      .then(function (data) {
        if (data.data.length <= 1) { //>
          self.noToolsPropertyData = true;
        }
        self.selectedMappingTool = undefined;
        self.masterToolPropertiesData = data;
      });
    
    this.displayToolMappingDetail();
  }

  displayToolMappingDetail() {
    var self = this;
    self.isEditData = false;
    this.disableAdd = false;
    var toolDataSourceArray = [];
    self.businessMappingService.getToolMapping(this.selectedLabel.toolName)
      .then(function (usersMappingResponseData) {
        if (usersMappingResponseData.status == "success") {
          if (usersMappingResponseData.data != undefined) {
            usersMappingResponseData.data = self.clubProperties(usersMappingResponseData.data, true);
            toolDataSourceArray = usersMappingResponseData.data;
            if (toolDataSourceArray.length == 0) {
              self.noToolsData = true;
            } else {
              self.noToolsData = false;
            }
          }
        } else {
          self.messageDialog.showApplicationsMessage("Something went wrong with service,Please try again ", "WARN");
        }
        self.displayedColumns = ['radio', 'mappinglabel', 'properties']
        self.isListView = true;
        self.subHeading = "List of Business Mapping Labels";
        self.toolDataSource = new MatTableDataSource(toolDataSourceArray);
      });
  }

  clubProperties(jsonData, isArray) {
    if (isArray) {
      var length = jsonData.length;
      for (let i = 0; i < length; i++) {
        let propString = undefined;
        for (let key of Object.keys(jsonData[i])) {
          if (this.additionalProperties.indexOf(key) > -1) {
          } else {
            if (propString == undefined) {
              propString = key + " <b> : </b>" + jsonData[i][key];
            } else {
              propString += "" + "<br>" + key + " <b> : </b>" + jsonData[i][key];
            }
          }
        }
        jsonData[i]['propertiesString'] = propString;
      }
    } else {
      let propString = undefined;
      for (let key of Object.keys(jsonData)) {
        if (this.additionalProperties.indexOf(key) > -1) {
        } else {
          if (propString == undefined) {
            propString = key + " <b> : </b>" + jsonData[key];
          } else {
            propString += "" + "<br>" + key + " <b> : </b>" + jsonData[key];
          }
        }
      }
      jsonData['propertiesString'] = propString;
    }
    return jsonData;
  }

  async loadToolProperties(selectedTool) {
    try {
      this.toolPropertyDataSource = [];
      this.toolMappingLabels = [];
      this.displayedToolColumns = ['checkbox', 'toolproperties', 'propertyValue', 'propertyLabel'];
      const regex = new RegExp("orgLevel", 'gi')
      if (this.masterToolPropertiesData.data.length <= 1) { //>
        this.noToolsPropertyData = true;
      } else {
        this.noToolsPropertyData = false;
      }
      if (this.masterToolPropertiesData.data.length > 1 && this.masterToolPropertiesData.data != undefined && this.masterToolPropertiesData.status == "success") {
        if (this.actionType == 'edit') {
          var existingKeys = Object.keys(this.selectedMappingTool);
          for (let masterData of this.masterToolPropertiesData.data) {
            var checkvalue = regex.test(masterData);
            if (checkvalue) {
              // Skip Key changes
            } else {
              if (existingKeys.indexOf(masterData) == -1) {
                let toolMappingLabel = new ToolLabelMapping(masterData, masterData, "", "a", true);
                this.toolMappingLabels.push(toolMappingLabel);
              } else {
                let toolMappingLabel;
                if (this.unwantedLabel.indexOf(masterData) == -1) {
                  if (this.additionalProperties.indexOf(masterData) > -1) {
                    toolMappingLabel = new ToolLabelMapping(masterData, masterData, this.selectedMappingTool[masterData], "a", false);
                  } else {
                    toolMappingLabel = new ToolLabelMapping(masterData, masterData, this.selectedMappingTool[masterData], "a", true);
                  }
                  this.toolMappingLabels.push(toolMappingLabel);
                }
              }
            }
          }
          this.label = this.selectedMappingTool.businessmappinglabel;
        } else if (this.actionType == "add") {
          for (var key in this.masterToolPropertiesData.data) {
            var checkvalue = regex.test(this.masterToolPropertiesData.data[key]);
            if (checkvalue) {
              // Skip Key changes
            } else {
              let toolMappingLabel;
              if (this.additionalProperties.indexOf(this.masterToolPropertiesData.data[key]) > -1) {
                toolMappingLabel = new ToolLabelMapping(key, this.masterToolPropertiesData.data[key], "", "a", false);
              } else {
                toolMappingLabel = new ToolLabelMapping(key, this.masterToolPropertiesData.data[key], "", "a", true);
              }
              this.toolMappingLabels.push(toolMappingLabel);
            }
          }
        }
        this.cacheSpan('label', d => d.id);
        this.toolPropertyDataSource = this.getToolPropertyDataSource();
      } else {
        this.toolPropertyDataSource = [];
        this.noToolsPropertyData = true;
      }
      this.toolPropertyDataSource = new MatTableDataSource(this.toolPropertyDataSource);
      this.toolPropertyDataSource.data.forEach(row => {
        if (row.value != "") {
          this.selection.select(row)
        }
      }
      );
    } catch (error) {
      console.log(error);
    }
  }
  getToolPropertyDataSource() {
    return this.toolMappingLabels.filter(a => a.editProperties == true)
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.toolPropertyDataSource.data.length;
    return numSelected === numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. readChange*/
  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.toolPropertyDataSource.data.forEach(row => this.selection.select(row));
  }

  statusEdit(selectedElement) {
    this.isListView = true;
    this.isEditData = true;
  }

  cacheSpan(key, accessor) {
    for (let i = 0; i < this.toolMappingLabels.length;) {
      let currentValue = accessor(this.toolMappingLabels[i].label);
      let count = 1;
      // Iterate through the remaining rows to see how many match the current value as retrieved through the accessor.
      for (let j = i + 1; j < this.toolMappingLabels.length; j++) {
        if (currentValue != accessor(this.toolMappingLabels[j].label)) {
          break;
        }
        count++;
      }
      if (!this.spans[i]) {
        this.spans[i] = {};
      }
      //console.log(key + "  " + count);
      // Store the number of similar values that were found (the span) and skip i to the next unique row.
      this.spans[i][key] = count;
      i += count;//+
    }
  }

  getRowSpan(col, index) {
    return this.spans[index] && this.spans[index][col];
  }

  editData() {
    const numSelected = this.selection.selected.length
    this.isListView = false;
    this.noToolsData = false
    this.noToolsPropertyData = false
    this.actionType = "edit";
    this.selection.clear()
    this.loadToolProperties(this.selectedTool);
    this.isEditData = false;
    this.disableAdd = true;
    this.subHeading = "Edit Business Mapping Label for " + this.selectedMappingTool.businessmappinglabel;
  }

  addToolLabelData() {
    var self=this;
    this.isEditData = false;
    this.disableAdd = false;
    this.noToolsData = false
    this.noToolsPropertyData = false
    this.isListView = false;
    this.actionType = "add"
    this.subHeading = "Add Business Mapping Label";
    this.label = undefined;
    this.selection.clear()
   
    
    self.loadToolProperties(this.selectedTool);
  }

  saveData() {
    var toolMappingParameter;
    this.toolPropertyList = {};
    const numSelected = this.selection.selected.length;
    if (numSelected == 0) {
      this.messageDialog.showApplicationsMessage("Please select at least one Tool Property to create a Label", "WARN");
    } else if (this.label == "" || this.label == undefined) {
      this.messageDialog.showApplicationsMessage("Mapping Label value is <b>MANDATORY</b> it should not be empty", "WARN");
    } else {
      //Validate Label
      var selectedData = this.selection.selected;
      let validationMessage = '';
      selectedData.forEach(
        row => {
          if (row.value.length == 0) {
            validationMessage = "Value should not be empty for propety key <b>" + row.key + "</b>";
          } else {
            let toolMappingLabelSelected = new ToolLabelMapping(row.id, row.key, row.value, this.label, true);
            this.selectedToolMappingLabels.push(toolMappingLabelSelected);
            this.toolPropertyList[row.key] = row.value;
          }
        }
      );
      this.toolPropertyList = this.clubProperties(this.toolPropertyList, false);
      validationMessage = this.validateData(validationMessage);
      if (validationMessage == '') {
        if (this.actionType == "add") {
          this.toolPropertyList['labelName'] = this.selectedLabel.labelName;
          this.toolPropertyList['toolName'] = this.selectedLabel.toolName;
          this.toolPropertyList['categoryName'] = this.selectedLabel.categoryName;
          this.toolPropertyList['businessmappinglabel'] = this.label;
          this.toolPropertyList['adminuser'] = this.currentUserName;//'admin'
          this.toolPropertyList['inSightsTimeX'] = this.now;
          this.toolPropertyList['inSightsTime'] = this.now.getTime();
          delete this.toolPropertyList['propertiesString'];
          toolMappingParameter = JSON.stringify(this.toolPropertyList);
          this.callEditOrSaveDataAPI(toolMappingParameter);
        } else if (this.actionType == "edit") {
          for (let selectedData of this.toolMappingLabels) {
            /*if (selectedData.key == 'businessmappinglabel') {
              this.agentPropertyList[selectedData.key] = this.label
            } else {*/
            if (!selectedData.editProperties) {
              this.toolPropertyList[selectedData.key] = selectedData.value;
            } /*else {
              this.agentPropertyList[selectedData.key] = this.selectedMappingAgent[selectedData.key]
            }*/
            /* }*/
          }
          this.toolPropertyList['businessmappinglabel'] = this.label;
          this.toolPropertyList['adminuser'] = this.currentUserName;;
          this.toolPropertyList['inSightsTimeX'] = this.now;
          this.toolPropertyList['inSightsTime'] = this.now.getTime();
          this.toolPropertyList['toolName'] = this.selectedLabel.toolName;
          this.toolPropertyList['labelName'] = this.selectedLabel.labelName;
          this.toolPropertyList['categoryName'] = this.selectedLabel.categoryName;
          this.toolPropertyList['uuid'] = this.selectedMappingTool['uuid'];
          delete this.toolPropertyList['propertiesString'];
          toolMappingParameter = JSON.stringify(this.toolPropertyList);
          this.callEditOrSaveDataAPI(toolMappingParameter);
        }
      } else {
        this.messageDialog.showApplicationsMessage(validationMessage, "ERROR");
        this.selection.clear()
      }
    }
  }

  public validateData(validationMessage): any {
    //let validationMessage = '';
    if (validationMessage == '') {
      var labelArray = this.label.split(":");
      var str: string = String(this.label);
      var format = /[ !@#$%^&*()+\=\[\]{};'"\\|,.<>\/?]/;
      if (format.test(str) == true) {
        validationMessage = "Please check blank space and special charecters are not allowed, It should be like Label1:Label2:Label3";
      }
      if (this.actionType == "add") {
        for (let data of this.toolDataSource.data) {
          if (data.businessmappinglabel == this.label) {
            validationMessage = "Mapping Label already exists for tool <b>" + this.selectedLabel.toolName + " </b>.";
          }
          if (data.propertiesString == this.toolPropertyList['propertiesString']) {
            validationMessage = "Properties with same name and value are already exists for tool <b>" + this.selectedTool.toolName + " </b>."
          }
        }
      } else if (this.actionType == "edit") {
        for (let data of this.toolDataSource.data) {
          if (data.businessmappinglabel == this.label && data.propertiesString == this.toolPropertyList['propertiesString']) {
            validationMessage = "Properties with same name and value <b> " + this.toolPropertyList['propertiesString'] + " </b> already exists for tool <b>" + this.selectedTool.toolName + " </b>.<br>Please edit necessary values if applicable";
          }
        }
      }
    }
    return validationMessage;
  }

  callEditOrSaveDataAPI(toolMappingParameter: any) {
    var self = this;
    var title = this.actionType == "add" ? "Save Business Mapping Label" : " Edit Business Mapping Label";
    var dialogmessage = this.actionType == "add" ? "Are you sure do you want to save changes to the Business Mapping Label <b> " + this.label + " </b>?"
      : "Please note: The changes will be applied from next data collection. <br> Are you sure do you want to save changes to the Business Mapping Label <b> " + this.label + " </b>?";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "32%");
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.disableAdd = false;
        if (this.actionType == "add") {
          this.businessMappingService.saveToolMapping(toolMappingParameter)
            .then(function (saveResponsedata) {
              if (saveResponsedata.status == "success") {
                self.messageDialog.showApplicationsMessage("Business Mapping Label <b>" + self.label + "</b> saved successfully.", "SUCCESS");
              } else {
                self.messageDialog.showApplicationsMessage("Unable to save the Business Mapping Label <b> " + self.label + " </b>. " + saveResponsedata.message, "ERROR");
              }
              self.displayToolMappingDetail();
            });
        } else if (this.actionType == "edit") {
          this.businessMappingService.editToolMapping(toolMappingParameter)
            .then(function (editResponsedata) {
              if (editResponsedata.status == "success") {
                self.messageDialog.showApplicationsMessage("The changes you made to the Business Mapping Label <b> " + self.label + " </b> have been updated successfully.", "SUCCESS");
              } else {
                self.messageDialog.showApplicationsMessage("Unable to edit Business Mapping Label <b> " + self.label + " </b>. " + editResponsedata.message, "ERROR");
              }
              self.displayToolMappingDetail();
            });
        }
      } else {
        this.selectedToolMappingLabels = [];
        this.label = undefined;
        this.displayToolMappingDetail();
        this.selection.clear();
      }
    });
  }

  deleteMapping() {
    var self = this;
    if (this.selectedMappingTool.uuid != undefined || this.selectedMappingTool.uuid != "") {
      var title = "Delete Business Mapping Label";
       var dialogmessage = "<b>PLEASE NOTE:</b><br><br>Deleting Business Mapping Label <b>" + this.selectedMappingTool.businessmappinglabel + "</b>, this action <b>CANNOT BE UNDONE.</b><br><br>Once Business Mapping Label<b> " + this.selectedMappingTool.businessmappinglabel + "</b> is deleted, data related to  will be retained in the previously gathered data. However, if you create a new Business Mapping Label with the same name, it may impact other functionalities.</b><br><br>Are you sure, do you want to Delete Business Mapping Label <b>" + this.selectedMappingTool.businessmappinglabel + "</b>?";
      const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "");
      dialogRef.afterClosed().subscribe(result => {
        if (result == 'yes') {
          this.businessMappingService.deleteToolMapping(this.selectedMappingTool.uuid)
            .then(function (deleteResponsedata) {
              if (deleteResponsedata.status == "success") {
                self.messageDialog.showApplicationsMessage("Label delete Successfully ", "SUCCESS");
              } else {
                self.messageDialog.showApplicationsMessage("Unable to delete label " + deleteResponsedata.message, "ERROR");
              }
              self.displayToolMappingDetail();
            });
        } else {
          this.selectedToolMappingLabels = [];
          this.label = undefined;
          this.displayToolMappingDetail();
          this.selection.clear()
        }
      });
    } else {
      self.messageDialog.showApplicationsMessage("Unable to delete Label , Please tray again later", "ERROR");
    }
  }
}
