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
import { Component, ElementRef, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute, NavigationExtras, Router } from "@angular/router";
import { AgentConfigItem } from "@insights/app/modules/admin/agent-management/agent-configuration/agentConfigItem";
import { AgentService } from "@insights/app/modules/admin/agent-management/agent-management-service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { InsightsInitService } from "@insights/common/insights-initservice";

@Component({
  selector: "app-agent-configuration",
  templateUrl: "./agent-configuration.component.html",
  styleUrls: [
    "./agent-configuration.component.scss",
    "./../../../home.module.scss",
  ],
})
export class AgentConfigurationComponent implements OnInit {
  datatypeVal: boolean;
  validationArr = {};
  osLists = {};
  configDesc = {};
  configAbbr = [];
  selectedOS: string;
  versionList = [];
  versionListSorted = [];
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
  trackingUploadedFileContentStr: any = "";
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
  selectedType: string;
  isWebhook: boolean = false;
  subTitleInfoText: string;
  @ViewChild("fileInput") myFileDiv: ElementRef;
  regex = new RegExp("[a-zA-Z0-9_]*", "gi");
  regexlabel = new RegExp("^[a-zA-Z0-9._]+$");
  labelData: string = "";
  oldLabelData: string = "";
  labelHealth: string = "";
  oldLabelHealth: string = "";
  color = "accent";
  vault = false;
  agentType = [];
  isROIAgent: boolean = false;
  validSecretDetails = true;
  secretProperties = [
    "accessToken",
    "password",
    "passwd",
    "apiToken",
    "secret_access_key",
    "awsSecretkey",
    "awsAccesskey",
    "authtoken",
    "access_key_id",
    "neo4j_password",
    "elasticsearch_passwd",
    "docker_repo_passwd",
    "apiKey",
    "applicationKey"
  ];
  fileName: String = "";
  fileNameTooltip: String = "";
  isEdit: boolean = false;

  constructor(
    public config: InsightsInitService,
    public agentService: AgentService,
    private router: Router,
    private route: ActivatedRoute,
    public messageDialog: MessageDialogService,
    private dataShare: DataSharedService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.receivedParam = JSON.parse(params["agentparameter"]);
      this.showThrobber = true;
      this.initializeVariable();
      this.getOsList();
      this.getOsVersionTools();
    });
  }

  initializeVariable() {
    this.agentType = ["Agent", "Webhook", "ROIAgent"];
    if (this.receivedParam.type == "update") {
      this.isEdit = true;
      this.btnValue = "Update";
      this.subTitleName = "Update an Agent";
      this.subTitleInfoText =
        "(You may add/edit/delete agent from the main page)";
      this.buttonDisableStatus = true;
      this.defaultConfigdata = {};
      if (this.receivedParam.detailedArr != null) {
        this.selectedOS = this.receivedParam.detailedArr.osVersion;
        this.selectedVersion = this.receivedParam.detailedArr.agentVersion;
        this.selectedTool = this.receivedParam.detailedArr.toolName;
        this.selectedAgentKey = this.receivedParam.detailedArr.agentKey;
        this.isWebhook = this.receivedParam.detailedArr.iswebhook;
        this.getDbAgentConfig();
        if (this.isWebhook) {
          this.selectedType = "Webhook";
        } else {
          this.selectedType = "Agent";
        }

        this.showTrackingJsonUploadButton = false;
      }
    } else if (this.receivedParam.type == "new") {
      this.btnValue = "Add"; //Register
      this.subTitleName = "Add an Agent";
      this.subTitleInfoText =
        "(You may edit the Agent from the main page after adding the agent)";
      this.selectedOS = undefined;
      this.selectedVersion = undefined;
      this.selectedTool = undefined;
      this.showTrackingJsonUploadButton = true;
      if (this.receivedParam.detailedArr != null) {
        this.validationArr = this.receivedParam.detailedArr;
      }
    }
  }

  getOsList() {
    var agentsListFromUiConfig = this.config.getAgentsOsList();
    if (agentsListFromUiConfig !== undefined) {
      this.osLists = agentsListFromUiConfig;
    }
  }

  async getOsVersionTools() {
    console.log("Inside OS Version Tools");
    var self = this;
    var selversion;
    self.toolsArr = [];
    this.toolVersionData =
      await this.agentService.getDocRootAgentVersionTools();
    console.log(this.toolVersionData);
    if (this.toolVersionData.status == "success") {
      if (this.selectedVersion) {
        this.toolsArr = this.toolVersionData.data[this.selectedVersion];
        for (var key in this.toolVersionData.data) {
          this.versionList.push(key);
        }
        this.versionListSorted = this.versionList.sort((n1, n2) => n1 > n2 ? -1 : 1);
      } else {
        for (var key in this.toolVersionData.data) {
          this.versionList.push(key);
        }
        this.versionListSorted = this.versionList.sort((n1, n2) => n1 > n2 ? -1 : 1);
      }
    } else {
      self.showMessage =
        "Problem with Docroot URL (or) Platform service. Please try again";
      self.messageDialog.openSnackBar(self.showMessage, "error");
    }
    self.showThrobber = false;
  }

  checkType(): void {
    if (this.selectedType == "Webhook") {
      this.isWebhook = true;
    } else {
      this.isWebhook = false;
    }
    console.log(
      "Inside checkType " + this.selectedType + "    " + this.isWebhook
    );
    this.agentConfigItems = [];
    this.selectedVersion = undefined;
    this.selectedTool = undefined;
    this.showConfig = false;
  }

  versionOnChange(key, type): void {
    console.log("Inside versionOnchange:", key, type);
    var self = this;
    if (type == "validate") {
      if (
        self.selectedVersion === undefined ||
        self.selectedTool === undefined ||
        self.selectedOS === undefined
      ) {
        self.buttonDisableStatus = true;
      } else {
        self.buttonDisableStatus = false;
      }
    } else if (type == "Update") {
      self.showConfig = false;
      self.showMessage = "";
      self.configData = JSON.stringify(self.agentConfigItems);
      self.agentService
        .getDocrootAgentConfig(
          key,
          self.selectedTool,
          self.isWebhook,
          self.selectedType
        )
        .then(function (vdata) {
          self.showConfig = true;
          self.versionChangeddata = JSON.parse(vdata.data);
          self.mergeConfigelement(self.versionChangeddata);
        })
        .catch(function (vdata) {
          self.showMessage = "Something wrong with service, Please try again";
          self.messageDialog.openSnackBar(self.showMessage, "error");
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

    if (!self.isRegisteredTool) {
      this.agentConfigItems = [];
      self.showConfig = false;
      self.showMessage = "";

      console.log(self.selectedType);
      var agentConfigResponse = await self.agentService.getDocrootAgentConfig(
        version,
        toolName,
        this.isWebhook,
        self.selectedType
      );

      if (agentConfigResponse.status == "success") {
        self.showConfig = true;
        self.dynamicData = JSON.stringify(
          self.defaultConfigdata["dynamicTemplate"],
          undefined,
          4
        );
        this.defaultConfigdata = JSON.parse(agentConfigResponse.data);
        this.getconfigDataParsed(self.defaultConfigdata);
        self.configLabelMerge();
        if (self.selectedOS === undefined || self.dynamicData == "") {
          self.buttonDisableStatus = true;
        } else {
          self.buttonDisableStatus = false;
        }
      } else {
        var message = "-";
        if (
          "message" in agentConfigResponse &&
          agentConfigResponse.message.indexOf("Not_Webhook_Agent") !== -1
        ) {
          message = "This is not a webhook Agent";
        } else if (
          "message" in agentConfigResponse &&
          agentConfigResponse.message.indexOf("This is not a ROI agent.") !== -1
        ) {
          message = "This is not a ROI Agent";
        } else {
          message = "Something wrong with service, Please try again";
        }
        self.buttonDisableStatus = true;
        self.showMessage = message;
        self.messageDialog.openSnackBar(self.showMessage, "error");
      }
    }
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
        var inputType = this.secretProperties.find(
          (element) => element === configDatakey
        )
          ? "password"
          : "text";
        if (
          typeof data[configDatakey] == "object" &&
          configDatakey != "dynamicTemplate"
        ) {
          for (let configinnerDatakey of Object.keys(value)) {
            let agentConfigChild = new AgentConfigItem();
            var childKeyInputType = this.secretProperties.find(
              (element) => element === configinnerDatakey
            )
              ? "password"
              : "text";
            agentConfigChild.setData(
              this.getRandomNumber(),
              configinnerDatakey,
              value[configinnerDatakey],
              typeof value[configinnerDatakey],
              childKeyInputType
            );
            agentConfigChilds.push(agentConfigChild);
          }
          agentConfig.setData(
            this.getRandomNumber(),
            configDatakey,
            value,
            typeof value,
            inputType,
            agentConfigChilds
          );
        } else if (configDatakey == "dynamicTemplate") {
          agentConfig.setData(
            this.getRandomNumber(),
            configDatakey,
            JSON.stringify(value, undefined, 4),
            typeof value,
            inputType
          );
        } else {
          agentConfig.setData(
            this.getRandomNumber(),
            configDatakey,
            value,
            typeof value,
            inputType
          );
        }
        this.agentConfigItems.push(agentConfig);
      }
    }
  }

  getRandomNumber(): number {
    return Math.random();
  }

  getAgentConfigItems(filtername: any) {
    if (filtername == "object") {
      return this.agentConfigItems.filter(
        (item) =>
          item.type == filtername &&
          item.key != "dynamicTemplate" &&
          item.key != "vault" &&
          item.key != "agentSecretDetails"
      );
    } else if (filtername == "dynamicTemplate") {
      return this.agentConfigItems.filter(
        (item) => item.key == "dynamicTemplate"
      );
    } else {
      return this.agentConfigItems.filter((item) => item.type != "object");
    }
  }

  async getDbAgentConfig() {
    var self = this;
    self.showConfig = false;
    self.showThrobber = true;
    self.showMessage = "";
    if (this.selectedAgentKey != undefined) {
      var agentData = await self.agentService.getDbAgentConfig(
        this.selectedAgentKey
      );

      if (agentData != undefined) {
        self.showConfig = true;
        self.showThrobber = false;
        this.defaultConfigdata = JSON.parse(agentData.data.agentJson);
        this.getconfigDataParsed(this.defaultConfigdata);
        this.configLabelMerge();
        this.vault = agentData.data.vault;
        this.isWebhook = agentData.data.iswebhook;
      } else {
        self.showThrobber = false;
        self.showMessage = "Something wrong with service, Please try again";
        self.messageDialog.openSnackBar(self.showMessage, "error");
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
        this.updatedConfigParamdata["agentSecretDetails"] =
          configParamData.value;
      } else if (
        configParamData.key != "dynamicTemplate" &&
        configParamData.type == "object"
      ) {
        this.item = {};
        for (let configinnerData of configParamData.children) {
          this.item[configinnerData.key] = this.checkDatatype(
            configinnerData.value,
            configinnerData.type
          );
        }
        this.updatedConfigParamdata[configParamData.key] = this.item;
      } else if (
        configParamData.key != "dynamicTemplate" &&
        configParamData.type != "object"
      ) {
        this.updatedConfigParamdata[configParamData.key] = this.checkDatatype(
          configParamData.value,
          configParamData.type
        );
      } else if (configParamData.key == "dynamicTemplate") {
        this.updatedConfigParamdata["dynamicTemplate"] = JSON.parse(
          configParamData.value
        );
      }
    }

    this.labelData = String(this.updatedConfigParamdata["publish"]["data"]);
    this.oldLabelData = String(this.defaultConfigdata["publish"]["data"]);
    this.labelHealth = String(this.updatedConfigParamdata["publish"]["health"]);
    this.oldLabelHealth = String(this.defaultConfigdata["publish"]["health"]);
    var agentId: string = String(this.updatedConfigParamdata["agentId"]);
    var oldAgentId: string = String(this.defaultConfigdata["agentId"]);
    var checkAgentId = this.regex.test(agentId);
    if (actionType == "Add") {
      if (agentId == undefined || agentId == "" || agentId == "NaN") {
        self.messageDialog.openSnackBar(
          "Please enter valid agentId, It should not be blank ",
          "error"
        );
        agentId = undefined;
      } else if (!checkAgentId) {
        agentId = undefined;
        self.messageDialog.openSnackBar(
          "Please enter valid agentId, and only contain alphanumeric character and underscore ",
          "error"
        );
      } else if (agentId.toLowerCase() == self.selectedTool.toLowerCase()) {
        agentId = undefined;
        self.messageDialog.openSnackBar(
          "AgentID and Tool name cannot be same ",
          "error"
        );
      } else if (this.labelData != this.oldLabelData) {
        this.validateLabel(this.labelData, "DATA");
      } else if (this.labelHealth != this.oldLabelHealth) {
        this.validateLabel(this.labelHealth, "HEALTH");
      }
    } else {
      if (agentId != oldAgentId) {
        self.messageDialog.openSnackBar(
          "You are not allow to change AgentId while update ",
          "error"
        );
        agentId = undefined;
      } else if (this.labelData != this.oldLabelData) {
        this.validateLabel(this.labelData, "DATA");
      } else if (this.labelHealth != this.oldLabelHealth) {
        this.validateLabel(this.labelHealth, "HEALTH");
      } else if (!this.vault) {
        self.validSecretDetails = true;
        var secretDetails = self.updatedConfigParamdata["agentSecretDetails"];
        secretDetails.forEach((element) => {
          if (this.updatedConfigParamdata[element] == "*****") {
            var dialog = self.messageDialog.openSnackBar(
              "You have disabled vault, please enter correct <b>" +
                element +
                "</b>.",
              "error"
            );
            self.validSecretDetails = false;
          }
        });
      }
    }

    if (
      this.updatedConfigParamdata &&
      agentId != undefined &&
      this.labelData != undefined &&
      this.labelHealth != undefined &&
      this.validSecretDetails
    ) {
      self.configData = "";
      self.configData = JSON.stringify(self.updatedConfigParamdata);
      var agentAPIRequestJson = {};
      let registerAgentRes: any;
      console.log(this.selectedType);
      if (actionType == "Update") {
        agentAPIRequestJson["agentId"] = this.selectedAgentKey;
        agentAPIRequestJson["configJson"] = self.configData;
        agentAPIRequestJson["toolName"] = self.selectedTool;
        agentAPIRequestJson["agentVersion"] = self.selectedVersion;
        agentAPIRequestJson["osversion"] = self.selectedOS;
        agentAPIRequestJson["vault"] = this.vault;
        agentAPIRequestJson["isWebhook"] = this.isWebhook;
        agentAPIRequestJson["type"] = this.selectedType;
        try {
          var updateAgentRes = await self.agentService.updateAgentV2(
            JSON.stringify(agentAPIRequestJson)
          );

          self.agentConfigstatus = updateAgentRes.status;
          if (updateAgentRes.status == "success") {
            self.sendStatusMsg("updated");
            self.agentConfigstatus = "Agent updated Successfully";
            self.agentConfigstatusCode = "SUCCESS";
          } else {
            self.sendStatusMsg("update");
            self.agentConfigstatus = "Agent update Failed";
            self.agentConfigstatusCode = "ERROR";
          }
        } catch (e) {
          console.error("Error is: ", e);
          self.sendStatusMsg("update");
          self.agentConfigstatus = "Agent update Failed";
          self.agentConfigstatusCode = "ERROR";
        }
      } else {
        agentAPIRequestJson["toolName"] = self.selectedTool;
        agentAPIRequestJson["agentVersion"] = self.selectedVersion;
        agentAPIRequestJson["osversion"] = self.selectedOS;
        agentAPIRequestJson["configJson"] = self.configData;
        agentAPIRequestJson["trackingDetails"] =
          self.trackingUploadedFileContentStr;
        agentAPIRequestJson["vault"] = this.vault;
        agentAPIRequestJson["isWebhook"] = this.isWebhook;
        agentAPIRequestJson["type"] = this.selectedType;
        try {
          registerAgentRes = await self.agentService.registerAgentV2(
            JSON.stringify(agentAPIRequestJson)
          );
          self.agentConfigstatus = registerAgentRes.status;
          if (registerAgentRes.status == "success") {
            self.sendStatusMsg("registered");
            self.agentConfigstatus = " Agent Registered Successfully";
            self.agentConfigstatusCode = "SUCCESS";
          } else {
            self.sendStatusMsg("register");
            self.agentConfigstatus =
              "Agent Registration Failed. Please check the logs for more details.";
            self.agentConfigstatusCode = "ERROR";
          }
        } catch (e) {
          console.error("Error is: ", e);
          self.sendStatusMsg("register");
          self.agentConfigstatus =
            "Agent Registration Failed. Please check the logs for more details.";
          self.agentConfigstatusCode = "ERROR";
        }
      }

      if (this.agentConfigstatusCode == "SUCCESS") {
        if (this.agentConfigstatus) {
          let navigationExtras: NavigationExtras = {
            skipLocationChange: true,
            queryParams: {
              agentstatus: this.agentConfigstatus,
              agentConfigstatusCode: this.agentConfigstatusCode,
            },
          };
          this.router.navigate(
            ["InSights/Home/agentmanagement"],
            navigationExtras
          );
        }
      } else {
        self.messageDialog.openSnackBar(this.agentConfigstatus, "error");
      }
    }
  }

  validateLabel(labelName, labelType) {
    var checkLabel = this.regexlabel.test(labelName);
    if (!checkLabel) {
      this.messageDialog.openSnackBar(
        "Please enter valid label name, and it contains only alphanumeric character,underscore & dot ",
        "error"
      );
      if (labelType == "DATA") {
        this.labelData = undefined;
      } else if (labelType == "HEALTH") {
        this.labelHealth = undefined;
      }
    } else if (
      labelName == "" ||
      labelName == undefined ||
      labelName == "NaN"
    ) {
      this.messageDialog.openSnackBar(
        "Please enter valid Label name, it cannot be blank",
        "error"
      );
      this.labelData = undefined;
      this.labelHealth = undefined;
    } else if (checkLabel) {
      var count = (labelName.match(/\./g) || []).length;
      if (count > 1) {
        var splittedLength = labelName.split(".").length;
        if (
          labelType == "DATA" &&
          (labelName.split(".")[splittedLength - 1] == "" ||
            labelName.split(".")[splittedLength - 1] != "DATA")
        ) {
          this.messageDialog.openSnackBar(
            "Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.DATA",
            "error"
          );
          this.labelData = undefined;
        } else if (
          labelType == "HEALTH" &&
          (labelName.split(".")[splittedLength - 1] == "" ||
            labelName.split(".")[splittedLength - 1] != "HEALTH")
        ) {
          this.messageDialog.openSnackBar(
            "Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.HEALTH",
            "error"
          );
          this.labelHealth = undefined;
        }
      } else {
        if (labelType == "DATA") {
          this.messageDialog.openSnackBar(
            "Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.DATA",
            "error"
          );
          this.labelData = undefined;
        } else if (labelType == "HEALTH") {
          this.messageDialog.openSnackBar(
            "Invalid label Name. Please follow the nomenclature TOOL_CATEGORY.LABEL_NAME.HEALTH",
            "error"
          );
          this.labelHealth = undefined;
        }
      }
    }
  }

  sendStatusMsg(Msg): void {
    this.showMessage = Msg;
  }

  checkDatatype(dataVal, type) {
    if (dataVal === "") {
      if (type == "boolean") {
        return false;
      } else if (type == "string") {
        return "";
      } else if (type == "number") {
        return 0;
      }
    }
    if (typeof dataVal == "boolean") {
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
      if (
        self.findDataType(vkeys, versionChangeddataObj) == "object" &&
        vkeys != "dynamicTemplate"
      ) {
        if (!self.defaultConfigdata.hasOwnProperty(vkeys)) {
          self.defaultConfigdata[vkeys] = versionChangeddataObj[vkeys];
        }
        for (var vkeys1 in versionChangeddataObj[vkeys]) {
          if (!self.defaultConfigdata[vkeys].hasOwnProperty(vkeys1)) {
            self.defaultConfigdata[vkeys][vkeys1] =
              versionChangeddataObj[vkeys][vkeys1];
          }
        }
      } else {
        if (!self.defaultConfigdata.hasOwnProperty(vkeys)) {
          self.defaultConfigdata[vkeys] = versionChangeddataObj[vkeys];
        }
      }
    }

    /*Delete Unnecessay filed*/
    /*
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
    */
    //console.log(JSON.stringify(self.defaultConfigdata));
    this.agentConfigItems = [];
    this.getconfigDataParsed(self.defaultConfigdata);
    this.configLabelMerge();
  }

  configLabelMerge(): void {
    this.configDesc = InsightsInitService.configDesc;
    for (let configParamData of this.agentConfigItems) {
      if (this.configDesc.hasOwnProperty(configParamData.key)) {
        this.configAbbr[configParamData.key] =
          this.configDesc[configParamData.key];
      } else {
        this.configAbbr[configParamData.key] = configParamData.key;
      }
    }
  }

  findDataType(key, arr): string {
    return typeof arr[key];
  }
  uploadFile() {
    this.trackingUploadedFileContentStr = "";
    var uploadedFile = this.myFileDiv.nativeElement.files;
    if (uploadedFile.length === 0) {
      this.fileUploadErrorMessage = "Please select files to upload";
      this.messageDialog.openSnackBar(this.fileUploadErrorMessage, "error");
      return;
    }
    for (var i = 0; i < uploadedFile.length; i++) {
      var testFileExt = this.checkFile(uploadedFile[i], ".json");

      if (!testFileExt) {
        this.fileUploadErrorMessage = "Please upload only json file";
        this.messageDialog.openSnackBar(this.fileUploadErrorMessage, "error");
        this.cancelFileUpload();
        return;
      }
      if (testFileExt) {
        this.getTrackingFileContentToString(uploadedFile[i]);
        setTimeout(() => {}, 5000);
        this.fileUploadSuccessMessage = "File uploaded successfully!";
        this.messageDialog.openSnackBar(
          this.fileUploadSuccessMessage,
          "success"
        );
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
      fileExt = fileExt.substring(fileExt.lastIndexOf("."));
      if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      } else return true;
    }
  }

  cancelFileUpload() {
    this.myFileDiv.nativeElement.files = new DataTransfer().files;
    this.trackingUploadedFileContentStr = "";
    this.fileUploadSuccessMessage = "";
    this.fileUploadErrorMessage = "";
    this.fileName = null;
    this.fileNameTooltip = null;
  }

  cancelChange(actionType) {
    var title = "Cancel Agent";
    var dialogmessage = "Are you sure you want to discard your changes?";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        let navigationExtras: NavigationExtras = {
          skipLocationChange: true,
          queryParams: {
            agentstatus: undefined,
            agentConfigstatusCode: "",
          },
        };
        this.router.navigate(
          ["InSights/Home/agentmanagement"],
          navigationExtras
        );
      }
    });
  }

  onFileChanged(event) {
    const file: File = event.target.files[0];
    this.fileName = this.dataShare.getCustomizeName(file.name);
    this.fileNameTooltip = file.name;
  }

  redirectToLandingPage() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
    };
    this.router.navigate(["InSights/Home/agentmanagement"], navigationExtras);
  }
}
