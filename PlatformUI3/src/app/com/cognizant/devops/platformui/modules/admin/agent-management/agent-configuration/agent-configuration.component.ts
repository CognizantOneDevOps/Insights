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
import { Component, OnInit, PipeTransform, Pipe, ViewChild, ElementRef } from '@angular/core';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { AgentService } from '@insights/app/modules/admin/agent-management/agent-management-service';
import { Router, ActivatedRoute, ParamMap, NavigationExtras } from '@angular/router';
import { AgentConfigItem } from '@insights/app/modules/admin/agent-management/agent-configuration/agentConfigItem';
import { AdminComponent } from '@insights/app/modules/admin/admin.component';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-agent-configuration',
  templateUrl: './agent-configuration.component.html',
  styleUrls: ['../agent-management.component.css', './../../../home.module.css']
})
export class AgentConfigurationComponent implements OnInit {
  datatypeVal: boolean;
  validationArr = {};
  osLists = {};
  configDesc = {};
  configAbbr = [];
  selectedOS: string;
  versionList = [];
  toolsArr = [];
  toolVersionData: any;
  showMessage: string;
  showConfig: boolean = false;
  showThrobber: boolean = false;
  editAgentDetails = {};
  headerData = [];
  updatedConfigdata = {};
  updatedConfigParamdata = {};
  configData: string;
  item = {};
  defaultConfigdata = {};
  tempConfigdata: string;
  versionChangeddata = {};
  uploadedFile: File;
  isTypeError: string = "";
  files = [];
  fileUploadSuccessMessage: string = "";
  fileUploadErrorMessage: string = "";
  trackingUploadedFileContentStr: string = "";
  showTrackingJsonUploadButton: boolean;
  buttonDisableStatus: boolean = true;
  isRegisteredTool: boolean = false;
  btnValue: string;
  dynamicData: string;
  selectedTool: string;
  selectedVersion: string;
  receivedParam: any;
  agentConfigItems: AgentConfigItem[] = [];
  selectedAgentKey: string;
  agentConfigstatus: string;
  agentConfigstatusCode: string;
  subTitleName: string;
  subTitleInfoText: string;
  @ViewChild('fileInput') myFileDiv: ElementRef;
  regex = new RegExp("[a-zA-Z0-9_]*", 'gi');
  regexlabel = new RegExp("^[a-zA-Z0-9._]+$");
  labelData: string = "";
	oldLabelData: string = "";
	labelHealth: string = "";
	oldLabelHealth: string = "";
  color = 'accent';
  vault = false;

  constructor(public config: InsightsInitService, public agentService: AgentService,
    private router: Router, private route: ActivatedRoute,
    public messageDialog: MessageDialogService) {

  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.receivedParam = JSON.parse(params["agentparameter"]);
      //this.toolVersionData = JSON.parse(params["versionAndToolInfo"]);
      //console.log(this.receivedParam);
      //console.log(this.receivedParam.detailedArr);
      //console.log(this.toolVersionData);
      this.showThrobber = true;
      this.initializeVariable();
      this.getOsList()
      this.getOsVersionTools();
    });

  }

  initializeVariable() {
    if (this.receivedParam.type == "update") {
      this.btnValue = "Update";
      this.subTitleName = "Update an Agent"
      this.subTitleInfoText = "(You may add/edit/delete agent from the main page)";
      this.buttonDisableStatus = true;
      this.defaultConfigdata = {};
      if (this.receivedParam.detailedArr != null) {
        this.selectedOS = this.receivedParam.detailedArr.osVersion;
        this.selectedVersion = this.receivedParam.detailedArr.agentVersion;
        this.selectedTool = this.receivedParam.detailedArr.toolName;
        this.selectedAgentKey = this.receivedParam.detailedArr.agentKey;
        this.getDbAgentConfig();
        this.showTrackingJsonUploadButton = false;
      }
    } else if (this.receivedParam.type == "new") {
      this.btnValue = "Add";//Register
      this.subTitleName = "Add an Agent"
      this.subTitleInfoText = "(You may edit the Agent from the main page after adding the agent)";
      this.selectedOS = undefined;
      this.selectedVersion = undefined
      this.selectedTool = undefined;
      this.showTrackingJsonUploadButton = true;
      if (this.receivedParam.detailedArr != null) {
        this.validationArr = this.receivedParam.detailedArr;
      }
      //console.log(this.validationArr);
    }
  }

  getOsList() {
    var agentsListFromUiConfig = this.config.getAgentsOsList();
    if (agentsListFromUiConfig !== undefined) {
      this.osLists = agentsListFromUiConfig;
    }

  }

  async getOsVersionTools() {
    var self = this;
    var selversion;
    self.toolsArr = [];
    this.toolVersionData = await this.agentService.getDocRootAgentVersionTools()
    if (this.toolVersionData.status == "success") {
      if (this.selectedVersion) {
        this.toolsArr = this.toolVersionData.data[this.selectedVersion];
        for (var key in this.toolVersionData.data) {
          this.versionList.push(key);
        }
      } else {
        for (var key in this.toolVersionData.data) {
          this.versionList.push(key);
        }
      }
    } else {
      self.showMessage = "Problem with Docroot URL (or) Platform service. Please try again";
      self.messageDialog.showApplicationsMessage(self.showMessage, "ERROR");
    }
    self.showThrobber = false;
  }

  versionOnChange(key, type): void {
    var self = this;
    if (type == "validate") {
      if (self.selectedVersion === undefined || self.selectedTool === undefined || self.selectedOS === undefined) {
        self.buttonDisableStatus = true;
      } else {
        self.buttonDisableStatus = false;
      }
    } else if (type == "Update") {
      self.showConfig = false;
      self.showMessage = "";
      self.configData = JSON.stringify(self.agentConfigItems);
      self.agentService.getDocrootAgentConfig(key, self.selectedTool)
        .then(function (vdata) {
          self.showConfig = true;
          self.versionChangeddata = JSON.parse(vdata.data);
          self.mergeConfigelement(self.versionChangeddata);
        })
        .catch(function (vdata) {
          self.showMessage = "Something wrong with service, Please try again";
          self.messageDialog.showApplicationsMessage(self.showMessage, "ERROR");
        });
    } else {
      self.buttonDisableStatus = true;
      self.selectedTool = "";
      self.toolsArr = [];
      self.toolsArr = this.toolVersionData.data[key];
    }
  }


  async getAgentConfig(version, toolName) {
    var self = this;
    self.isRegisteredTool = false;
    /* self.checkValidation(); */

    if (!self.isRegisteredTool) {
      this.agentConfigItems = [];
      self.showConfig = false;
      self.showMessage = "";

      var agentConfigResponse = await self.agentService.getDocrootAgentConfig(version, toolName)
      //console.log(agentConfigResponse);
      //console.log(agentConfigResponse.data);

      if (agentConfigResponse.status == "success") {
        self.showConfig = true;
        self.dynamicData = JSON.stringify(self.defaultConfigdata['dynamicTemplate'], undefined, 4);
        this.defaultConfigdata = JSON.parse(agentConfigResponse.data);
        this.getconfigDataParsed(self.defaultConfigdata);
        self.configLabelMerge();
        if (self.selectedOS === undefined || self.dynamicData == '') {
          self.buttonDisableStatus = true;
        } else {
          self.buttonDisableStatus = false;
        }

      } else {
        self.buttonDisableStatus = true;
        self.showMessage = "Something wrong with service, Please try again";
        self.messageDialog.showApplicationsMessage(self.showMessage, "ERROR");
      }
    } /* else {
      self.buttonDisableStatus = true;
      self.showConfig = false;
      self.showMessage = " <b> " + toolName.charAt(0).toUpperCase() + toolName.slice(1) + " </b> agent is already registered, Please select other tool.";
      self.messageDialog.showApplicationsMessage(self.showMessage, "WARN");
    } */
  }

  getconfigDataParsed(data) {
    if (data != undefined) {
      for (let configDatakey of Object.keys(data)) {
        let agentConfig = new AgentConfigItem();
        let agentConfigChilds: AgentConfigItem[] = [];
        let value = data[configDatakey];
        if (value == undefined || value == null) {
          value = "";
        }
        if (typeof (data[configDatakey]) == 'object' && configDatakey != 'dynamicTemplate') {
          for (let configinnerDatakey of Object.keys(value)) {
            let agentConfigChild = new AgentConfigItem();
            agentConfigChild.setData(configinnerDatakey, value[configinnerDatakey], typeof (value[configinnerDatakey]))
            agentConfigChilds.push(agentConfigChild);
          }
          agentConfig.setData(configDatakey, value, typeof (value), agentConfigChilds);
        } else if (configDatakey == 'dynamicTemplate') {
          agentConfig.setData(configDatakey, JSON.stringify(value, undefined, 4), typeof (value));
        } else {
          agentConfig.setData(configDatakey, value, typeof (value));
        }
        this.agentConfigItems.push(agentConfig);
      }
      //console.log(this.agentConfigItems.length);
    }
  }

  getAgentConfigItems(filtername: any) {
    if (filtername == 'object') {
      return this.agentConfigItems.filter(item => (item.type == filtername && item.key != 'dynamicTemplate' && item.key != 'vault' && item.key != 'agentSecretDetails'));
    } else if (filtername == 'dynamicTemplate') {
      return this.agentConfigItems.filter(item => item.key == 'dynamicTemplate');
    } else {
      return this.agentConfigItems.filter(item => item.type != 'object');
    }
  }

  async getDbAgentConfig() {
    var self = this;
    self.showConfig = false;
    self.showThrobber = true;
    self.showMessage = "";
    if (this.selectedAgentKey != undefined) {
      var agentData = await self.agentService.getDbAgentConfig(this.selectedAgentKey)

      if (agentData != undefined) {
        //console.log(this.selectedAgentKey + "   " + agentData);
        self.showConfig = true;
        self.showThrobber = false;
        this.defaultConfigdata = JSON.parse(agentData.data.agentJson);
        this.getconfigDataParsed(this.defaultConfigdata);
        this.configLabelMerge();
        this.vault = agentData.data.vault;
      } else {
        self.showThrobber = false;
        self.showMessage = "Something wrong with service, Please try again";
        self.messageDialog.showApplicationsMessage(self.showMessage, "ERROR");
      }
    }


  }

  async saveData(actionType) {
    var self = this;
    this.agentConfigstatus = undefined;
    this.agentConfigstatusCode = undefined;
    this.updatedConfigParamdata = undefined;
    this.updatedConfigParamdata = {};

    for (let configParamData of this.agentConfigItems) {
      if (configParamData.key == "agentSecretDetails") {
        this.updatedConfigParamdata["agentSecretDetails"] = configParamData.value;
      } else if (configParamData.key != "dynamicTemplate" && configParamData.type == "object") {
        this.item = {};
        for (let configinnerData of configParamData.children) {
          this.item[configinnerData.key] = this.checkDatatype(configinnerData.value);
        }
        this.updatedConfigParamdata[configParamData.key] = this.item;
      } else if (configParamData.key != "dynamicTemplate" && configParamData.type != "object") {
        this.updatedConfigParamdata[configParamData.key] = this.checkDatatype(configParamData.value);
      } else if (configParamData.key == "dynamicTemplate") {
        this.updatedConfigParamdata["dynamicTemplate"] = JSON.parse(configParamData.value);
      }
    }

    //console.log(this.updatedConfigParamdata);
    this.labelData = String(this.updatedConfigParamdata["publish"]["data"]);
    this.oldLabelData = String(this.defaultConfigdata["publish"]["data"]);
    this.labelHealth = String(this.updatedConfigParamdata["publish"]["health"]);
    this.oldLabelHealth = String(this.defaultConfigdata["publish"]["health"]);
    var agentId: string = String(this.updatedConfigParamdata['agentId']);
    var oldAgentId: string = String(this.defaultConfigdata['agentId']);
    var checkAgentId = this.regex.test(agentId);
    if (actionType == "Add") {
      //console.log("selected agentId ===== " + agentId + "  oldAgentId " + oldAgentId + "  checkAgentId  " + checkAgentId + "   " + this.regex);
      if (agentId == undefined || agentId == "" || agentId == "NaN") {
        self.messageDialog.showApplicationsMessage("Please enter valid agentId,It should not be blank ", "ERROR");
        agentId = undefined;
      } else if (!checkAgentId) {
        //console.log(this.regex);
        agentId = undefined;
        self.messageDialog.showApplicationsMessage("Please enter valid agentId, and only contain alphanumeric character and underscore ", "ERROR");
      }
      else if (agentId.toLowerCase() == self.selectedTool.toLowerCase()) {
        agentId = undefined;
        self.messageDialog.showApplicationsMessage("AgentID and Tool name cannot be same ", "ERROR");
      }
     else if (this.labelData != this.oldLabelData) {
      this.validateLabel(this.labelData, "DATA");
     }
     else if (this.labelHealth != this.oldLabelHealth) {
      this.validateLabel(this.labelHealth, "HEALTH");
     }
    } else {
      if (agentId != oldAgentId) {
        self.messageDialog.showApplicationsMessage("You are not allow to change AgentId while update ", "ERROR");
        agentId = undefined;
      }
      else if (this.labelData != this.oldLabelData) {
        this.validateLabel(this.labelData, "DATA");
       }
       else if (this.labelHealth != this.oldLabelHealth) {
        this.validateLabel(this.labelHealth, "HEALTH");
       }
    }

    if (this.updatedConfigParamdata && agentId != undefined && this.labelData != undefined && this.labelHealth != undefined) {


      self.configData = "";
      self.configData = JSON.stringify(self.updatedConfigParamdata);
      var agentAPIRequestJson = {};
      let registerAgentRes: any;
      //console.log(this.configData)
      if (actionType == "Update") {

        agentAPIRequestJson['agentId'] = this.selectedAgentKey
        agentAPIRequestJson['configJson'] = self.configData
        agentAPIRequestJson['toolName'] = self.selectedTool
        agentAPIRequestJson['agentVersion'] = self.selectedVersion
        agentAPIRequestJson['osversion'] = self.selectedOS
        agentAPIRequestJson['vault'] = this.vault
        try {
          var updateAgentRes = await self.agentService.updateAgentV2(JSON.stringify(agentAPIRequestJson));

          self.agentConfigstatus = updateAgentRes.status;
          //console.log(updateAgentRes);
          if (updateAgentRes.status == "success") {
            self.sendStatusMsg("updated");
            self.agentConfigstatus = "Agent updated Successfully"
            self.agentConfigstatusCode = "SUCCESS";
          } else {
            self.sendStatusMsg("update");
            self.agentConfigstatus = "Agent update Failed";
            self.agentConfigstatusCode = "ERROR";
          }
        } catch (e) {
          console.error("Error is: ", e)
          self.sendStatusMsg("update");
          self.agentConfigstatus = "Agent update Failed";
          self.agentConfigstatusCode = "ERROR";
        }
      } else {
        agentAPIRequestJson['toolName'] = self.selectedTool
        agentAPIRequestJson['agentVersion'] = self.selectedVersion
        agentAPIRequestJson['osversion'] = self.selectedOS
        agentAPIRequestJson['configJson'] = self.configData
        agentAPIRequestJson['trackingDetails'] = self.trackingUploadedFileContentStr
        agentAPIRequestJson['vault'] = this.vault
        try {
          registerAgentRes = await self.agentService.registerAgentV2(JSON.stringify(agentAPIRequestJson));
          self.agentConfigstatus = registerAgentRes.status;
          if (registerAgentRes.status == "success") {
            self.sendStatusMsg("registered");
            self.agentConfigstatus = " Agent Registered Successfully"
            self.agentConfigstatusCode = "SUCCESS";
          } else {
            self.sendStatusMsg("register");
            self.agentConfigstatus = "Agent Registration Failed. Please check the logs for more details.";
            self.agentConfigstatusCode = "ERROR";
          }
        } catch (e) {
          console.error("Error is: ", e)
          self.sendStatusMsg("register");
          self.agentConfigstatus = "Agent Registration Failed. Please check the logs for more details.";
          self.agentConfigstatusCode = "ERROR";
        }

      }

      //console.log(this.agentConfigstatus)
      if (this.agentConfigstatusCode == "SUCCESS") {
        if (this.agentConfigstatus) {
          let navigationExtras: NavigationExtras = {
            skipLocationChange: true,
            queryParams: {
              "agentstatus": this.agentConfigstatus,
              "agentConfigstatusCode": this.agentConfigstatusCode
            }
          };
          this.router.navigate(['InSights/Home/agentmanagement'], navigationExtras);
        }
      } else {
        self.messageDialog.showApplicationsMessage(this.agentConfigstatus, "ERROR");
      }
    }

  }

  validateLabel(labelName, labelType) {
    
      var checkLabel = this.regexlabel.test(labelName);
      if (!checkLabel) {
        this.messageDialog.showApplicationsMessage("Please enter valid label name, and it contains only alphanumeric character,underscore & dot ", "ERROR");
        if(labelType == "DATA") {
          this.labelData = undefined;
        } 
        else if(labelType == "HEALTH") {
          this.labelHealth = undefined;
        }
      }
      else if(labelName == "" || labelName == undefined|| labelName == "NaN") {
        this.messageDialog.showApplicationsMessage("Please enter valid Label name, it cannot be blank", "ERROR");
        this.labelData = undefined;
        this.labelHealth = undefined;
      }
      else if (checkLabel) {
        var count = (labelName.match(/\./g) || []).length;
        if (count > 1) {
          var splittedLength = labelName.split(".").length;
          if (labelType == "DATA" && (labelName.split(".")[splittedLength - 1] == "" || labelName.split(".")[splittedLength - 1] != "DATA" )) {
            this.messageDialog.showApplicationsMessage("Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.DATA", "ERROR");
            this.labelData = undefined;
          }
          else if (labelType == "HEALTH" && (labelName.split(".")[splittedLength - 1] == "" || labelName.split(".")[splittedLength - 1] != "HEALTH")) {
            this.messageDialog.showApplicationsMessage("Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.HEALTH", "ERROR");
            this.labelHealth = undefined;
          }
        }
        else {
          if(labelType == "DATA") {
            this.messageDialog.showApplicationsMessage("Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.DATA", "ERROR");
            this.labelData = undefined;
          }
          else if(labelType == "HEALTH"){
            this.messageDialog.showApplicationsMessage("Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.HEALTH", "ERROR");
            this.labelHealth = undefined;
          }
        }
      }
  }

  sendStatusMsg(Msg): void {
    this.showMessage = Msg
  }

  checkDatatype(dataVal) {

    if (typeof (dataVal) == "boolean") {
      return dataVal;
    } else if (isNaN(dataVal)) {
      if (dataVal == "true") {
        this.datatypeVal = true;
        return this.datatypeVal;
      } else if (dataVal == "false") {
        this.datatypeVal = false;
        return this.datatypeVal;
      } else {
        return dataVal;
      }
    } else {
      return parseInt(dataVal);
    }
  }

  mergeConfigelement(versionChangeddataObj): void {
    var self = this;
    var unknownKeys = [];
    /*Add Extra field*/
    for (var vkeys in versionChangeddataObj) {
      if (self.findDataType(vkeys, versionChangeddataObj) == 'object' && vkeys != "dynamicTemplate") {

        if (!self.defaultConfigdata.hasOwnProperty(vkeys)) {
          self.defaultConfigdata[vkeys] = versionChangeddataObj[vkeys];
        }
        for (var vkeys1 in versionChangeddataObj[vkeys]) {
          if (!self.defaultConfigdata[vkeys].hasOwnProperty(vkeys1)) {
            self.defaultConfigdata[vkeys][vkeys1] = versionChangeddataObj[vkeys][vkeys1];
          }
        }
      } else {
        if (!self.defaultConfigdata.hasOwnProperty(vkeys)) {
          self.defaultConfigdata[vkeys] = versionChangeddataObj[vkeys];
        }
      }
    }

    /*Delete Unnecessay filed*/

    for (var dkeys in self.defaultConfigdata) {

      if (self.findDataType(dkeys, self.defaultConfigdata) == 'object' && dkeys != "dynamicTemplate") {

        if (!versionChangeddataObj.hasOwnProperty(dkeys)) {
          delete self.defaultConfigdata[dkeys];
        }

        for (var dkeys1 in self.defaultConfigdata[dkeys]) {
          if (!versionChangeddataObj[dkeys].hasOwnProperty(dkeys1)) {
            delete self.defaultConfigdata[dkeys][dkeys1];
          }
        }

      } else {
        if (!versionChangeddataObj.hasOwnProperty(dkeys)) {
          delete self.defaultConfigdata[dkeys];
        }
      }
    }
    //console.log(JSON.stringify(self.defaultConfigdata));
    this.agentConfigItems = [];
    this.getconfigDataParsed(self.defaultConfigdata);
    this.configLabelMerge();
  }


  configLabelMerge(): void {
    this.configDesc = InsightsInitService.configDesc;
    for (let configParamData of this.agentConfigItems) {
      if (this.configDesc.hasOwnProperty(configParamData.key)) {
        this.configAbbr[configParamData.key] = this.configDesc[configParamData.key];
      } else {
        this.configAbbr[configParamData.key] = configParamData.key;
      }
    }
  }

  findDataType(key, arr): string {
    return typeof (arr[key]);
  }

  /* checkValidation(): void {
    var self = this;
    for (var key in self.validationArr) {
      if (self.validationArr[key]['tool'] == self.selectedTool) {
        self.isRegisteredTool = true;
        self.selectedTool = "";
      }
    }
  } */
  uploadFile() {
    this.trackingUploadedFileContentStr = "";
    var uploadedFile = this.myFileDiv.nativeElement.files;
    for (var i = 0; i < uploadedFile.length; i++) {
      var testFileExt = this.checkFile(uploadedFile[i], ".json");
      if (!testFileExt) {
        this.fileUploadErrorMessage = "Please upload only json file";
      }
      //console.log(testFileExt);
      if (testFileExt) {
        //console.log(uploadedFile[i]);
        this.getTrackingFileContentToString(uploadedFile[i]);
        //console.log(this.trackingUploadedFileContentStr);
        setTimeout(() => {
          //console.log(uploadedFile[i]);
        }, 5000);
        this.fileUploadSuccessMessage = "File uploaded successfully!";
      }
    }
  }

  getTrackingFileContentToString(trackingJsonFileArray): void {
    var self = this;
    var reader = new FileReader();
    reader.readAsText(trackingJsonFileArray);
    reader.onload = () => {
      this.trackingUploadedFileContentStr = reader.result;
      if (this.trackingUploadedFileContentStr == "") {
        this.fileUploadErrorMessage = "Unable to read file ,Please try again ";
      }
    };
  }

  checkFile(sender, validExts) {
    if (sender) {
      var fileExt = sender.name;
      fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
      if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      }
      else return true;
    }
  }

  cancelFileUpload() {
    this.trackingUploadedFileContentStr = "";
    this.fileUploadSuccessMessage = "";
    this.fileUploadErrorMessage = "";
  }

  cancelChange(actionType) {
    var title = "Cancel Agent";
    var dialogmessage = "Are you sure you want to discard your changes?";
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "30%");
    dialogRef.afterClosed().subscribe(result => {
      //console.log('The dialog was closed  ' + result);
      if (result == 'yes') {
        let navigationExtras: NavigationExtras = {
          skipLocationChange: true,
          queryParams: {
            "agentstatus": undefined,
            "agentConfigstatusCode": ""
          }
        };
        this.router.navigate(['InSights/Home/agentmanagement'], navigationExtras);
      }
    });
  }
}
