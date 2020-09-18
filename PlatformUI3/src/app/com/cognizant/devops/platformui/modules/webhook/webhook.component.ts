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
import { Component, OnInit, ViewChild } from "@angular/core";
import { WebHookService } from "@insights/app/modules/webhook/webhook.service";
import { MatDialog } from "@angular/material";
import { Router } from "@angular/router";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { MatTableDataSource, MatPaginator } from "@angular/material";
import { DataSharedService } from "@insights/common/data-shared-service";
import { BulkUploadService } from "@insights/app/modules/bulkupload/bulkupload.service";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { ClipboardService } from "ngx-clipboard";
import { DerivedOperations } from "./derivedOperationsConfig";
import { ConditionalExpr } from "@angular/compiler";
export interface DataType {
  value: string;
  viewValue: string;
}
@Component({
  selector: "app-webhook",
  templateUrl: "./webhook.component.html",
  styleUrls: ["./webhook.component.css", "./../home.module.css"],
})
export class WebHookComponent implements OnInit {
  toolsArr = [];
  toolvalue: String = null;
  labelsArr = [];
  labelValueStack = [];
  toolsDetail = [];
  showAddWebHook: boolean = false;
  enableWebhookicon: boolean = false;
  showWebhook: boolean = true;
  showMessage: string;
  oldLabelValue: string;
  labelDisplay: string;
  displayedColumns = [];
  webhookNameList: any = [];
  webhookDatasource = new MatTableDataSource<any>();
  webhookList: any;
  showDetail: boolean = false;
  showConfirmMessage: string;
  selectedWebhook: any;
  webhookparameter = {};
  webhookName: any;
  selectedTool: any;
  eventToSubscribe: any;
  mqchannel: any;
  responseTemplate: any;
  dynamicTemplate: any;
  eventConfig: any;
  dataformat: any;
  actionType: any;
  value_to_copy: any;
  textToCopy: String;
  statussubscribe: boolean = false;
  enableDelete: boolean = false;
  enableRefresh: boolean = false;
  enableEdit: boolean = false;
  enableunsubscribe: boolean = false;
  enablesubscribe: boolean = false;
  radioRefresh: boolean = false;
  enableaddWebhook: boolean = true;
  disableInputFields: boolean = false;
  refreshRadio: boolean = false;
  isUpdateRequired: boolean = false;
  isToolSelected: boolean = false;
  isEventProcessing: boolean = false;
  fieldUsedForUpdate: string = "";
  dynamicTemplateSelected: boolean = false;
  responseTemplateSelected: boolean = false;
  mqChannelPrefix = "IPW_";
  regex = new RegExp("^[a-zA-Z0-9_]*$");
  regexlabel = new RegExp("^[a-zA-Z0-9:_]+$");
  timeFieldRegex = new RegExp("^[a-zA-Z0-9]*$");
  derivedOperations = [];
  derivedOperationList: DerivedOperations[] = [];
  enableSaveForEdit: boolean = false;
  refreshDerivedOperations: string;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  dataformats: DataType[] = [{ value: "json", viewValue: "Json" }];
  constructor(
    private router: Router,
    private _clipboardService: ClipboardService,
    private bulkuploadService: BulkUploadService,
    private initservice: InsightsInitService,
    private webhookService: WebHookService,
    private dialog: MatDialog,
    public messageDialog: MessageDialogService,
    private dataShare: DataSharedService
  ) {
    this.getLabelTools();
    this.getRegisteredWebHooks();
  }
  ngOnInit() {}

  setWebhookUrl(selectedWebhookEnable) {
    var hostname = this.initservice.getWebhookHost();
    var value_to_copy =
      hostname +
      "/PlatformInsightsWebHook/insightsDevOpsWebHook?webHookName=" +
      selectedWebhookEnable.webhookName;
    this.textToCopy = value_to_copy;
  }
  enableButtons(selectedWebhookEnable) {
    if (this.showAddWebHook == false) {
      this.enableDelete = true;
      this.enableEdit = true;
      this.radioRefresh = true;
      this.enableaddWebhook = false;
      this.setWebhookUrl(selectedWebhookEnable);
      if (selectedWebhookEnable.subscribeStatus == "Unsubscribed") {
        this.enableunsubscribe = false;
        this.enablesubscribe = true;
      } else {
        this.enableunsubscribe = true;
        this.enablesubscribe = false;
      }
    }
    this.enableRefresh = true;
  }
  public async getRegisteredWebHooks() {
    var self = this;
    this.webhookNameList = [];
    this.webhookList = [];
    this.webhookList = await self.webhookService.loadwebhookServices();
    if (this.webhookList != null && this.webhookList.status == "success") {
      this.webhookDatasource.data = this.webhookList.data.sort(
        (a, b) => a.webhookName > b.webhookName
      );
      this.webhookDatasource.paginator = this.paginator;
      this.webhookNameList.push("All");
      for (var data of this.webhookList.data) {
        if (this.webhookNameList.indexOf(data.webhookName) == -1) {
          this.webhookNameList.push(data.webhookName);
        }
      }
      var counter = 0;
      for (var element of this.webhookDatasource.data) {
        if (counter < this.webhookDatasource.data.length) {
          if (this.webhookDatasource.data[counter].subscribeStatus == true) {
            this.webhookDatasource.data[counter].subscribeStatus = "Subscribed";
          } else {
            this.webhookDatasource.data[counter].subscribeStatus =
              "Unsubscribed";
          }
          counter = counter + 1;
        } else {
          break;
        }
      }
      self.showDetail = true;
      this.displayedColumns = [
        "radio",
        "WebHookName",
        "ToolName",
        "LabelName",
        "DataType",
        "MqChannel",
        "Status",
      ];
      setTimeout(() => {
        this.showConfirmMessage = "";
      }, 3000);
    } else {
      self.showMessage = "Something wrong with Service.Please try again.";
      self.messageDialog.showApplicationsMessage(
        "Something wrong with Service.Please try again.",
        "ERROR"
      );
    }
  }
  list() {
    this.showWebhook = true;
    this.enableaddWebhook = true;
    this.showAddWebHook = false;
    this.enableWebhookicon = false;
    this.getRegisteredWebHooks();
    this.enableDelete = false;
    this.enableEdit = false;
    this.enableRefresh = false;
    this.enablesubscribe = false;
    this.enableunsubscribe = false;
  }
  onToolSelect(toolname): void {
    var self = this;
    this.isEventProcessing = false;
    if (toolname === undefined) {
      this.isToolSelected = false;
    } else {
      var i = 0;
      var labelnameIndex = this.toolsArr.indexOf(toolname);
      this.labelDisplay = this.labelsArr[labelnameIndex];
      this.isToolSelected = true;
    }
  }
  async getLabelTools() {
    var self = this;
    try {
      self.toolsDetail = [];
      let toollabelresponse = await this.bulkuploadService.loadUiServiceLocation();
      if (toollabelresponse.status == "success") {
        this.toolsDetail = toollabelresponse.data;
      }
      for (var element of this.toolsDetail) {
        var toolName = element.toolName;
        var labelName = element.label;
        this.toolsArr.push(toolName);
        this.labelsArr.push(labelName);
      }
    } catch (error) {
      console.log(error);
    }
  }

  addWebHook() {
    this.showAddWebHook = true;
    this.enableWebhookicon = true;
    this.showWebhook = false;
    this.enableaddWebhook = false;
    this.webhookName = "";
    this.selectedTool = "";
    this.labelDisplay = "";
    this.mqchannel = "";
    this.responseTemplate = "";
    this.dynamicTemplate = "";
    this.dataformat = "";
    this.enableRefresh = true;
    this.disableInputFields = false;
    this.derivedOperationList = [];
    this.isUpdateRequired = false;
    this.isEventProcessing = false;
    this.eventConfig = "";
    this.fieldUsedForUpdate = "";
    this.actionType = "save";
    var jsonData = JSON.parse(this.webhookService.getSampleJSONResponse());
    for (var element of jsonData) {
      let dervioplist = this.getDerivedOperations(
        element.wid,
        element.operationName,
        element.operationFields,
        this.webhookName
      );
      this.derivedOperationList.push(element);
    }
  }
  editWebhook() {
    this.enableSaveForEdit = true;
    this.enableEdit = false;
    var isSessionExpired = this.dataShare.validateSession();
    this.enableDelete = false;
    this.isToolSelected = true;
    if (!isSessionExpired) {
      this.disableInputFields = true;
      this.derivedOperationList = [];
      this.webhookName = this.selectedWebhook.webhookName;
      this.selectedTool = this.selectedWebhook.toolName;
      this.labelDisplay = this.selectedWebhook.labelDisplay;
      this.mqchannel = this.selectedWebhook.mqChannel;
      this.isUpdateRequired = this.selectedWebhook.isUpdateRequired;
      this.dynamicTemplate = this.selectedWebhook.dynamicTemplate;
      if (this.selectedWebhook.isEventProcessing != undefined) {
        this.isEventProcessing = this.selectedWebhook.isEventProcessing;
      } else {
        this.isEventProcessing = false;
      }
      if (this.selectedWebhook.dynamicTemplate != undefined) {
        this.dynamicTemplate = this.selectedWebhook.dynamicTemplate;
      } else {
        this.dynamicTemplate = "";
      }
      if (this.selectedWebhook.eventConfigJson != undefined) {
        this.eventConfig = this.selectedWebhook.eventConfigJson;
      } else {
        this.eventConfig = "";
      }
      if (this.selectedWebhook.fieldUsedForUpdate != undefined) {
        this.fieldUsedForUpdate = this.selectedWebhook.fieldUsedForUpdate;
      } else {
        this.fieldUsedForUpdate = "";
      }

      for (let i in this.selectedWebhook.derivedOperations) {
        let element = this.selectedWebhook.derivedOperations[i];
        var operationFieldsDataJson = JSON.parse(element.operationFields);
        let derivedOpslist = this.getDerivedOperations(
          element.wid,
          element.operationName,
          operationFieldsDataJson,
          this.webhookName
        );
        this.derivedOperationList.push(derivedOpslist);
      }
      if (this.selectedWebhook.responseTemplate != undefined) {
        this.responseTemplate = this.selectedWebhook.responseTemplate;
      } else {
        this.responseTemplate = "";
      }
      this.dataformat = this.selectedWebhook.dataFormat;
      if (this.selectedWebhook.subscribeStatus == "Subscribed") {
        this.statussubscribe = true;
      } else {
        this.statussubscribe = false;
      }
      this.actionType = "edit";
      this.enableWebhookicon = true;
      this.showAddWebHook = true;
      this.showWebhook = false;
      this.enableaddWebhook = false;
    }
  }
  Refresh() {
    this.refreshRadio = false;
    this.enablesubscribe = false;
    this.enableDelete = false;
    this.enableEdit = false;
    this.enableunsubscribe = false;
    this.selectedWebhook = "";
    this.webhookName = "";
    this.selectedTool = "";
    this.labelDisplay = "";
    this.dataformat = "";
    this.mqchannel = "";
    this.responseTemplate = "";
    this.dynamicTemplate = "";
    this.isUpdateRequired = false;
    this.fieldUsedForUpdate = "";
    this.isEventProcessing = false;
    this.eventConfig = "";
    this.isToolSelected = false;
    for (let index of this.derivedOperationList) {
      if (index.operationName == "insightsTimex") {
        index.operationFields = {};
      } else {
        index.markAsDelete();
      }
    }
    this.disableInputFields = false;
    if (this.showAddWebHook == false) {
      this.enableaddWebhook = true;
    }
  }

  validateWebhookData() {
    var isValidated = false;
    let messageDialogText;
    if (
      this.webhookName === "" ||
      this.selectedTool === "" ||
      this.labelDisplay === "" ||
      this.mqchannel === ""
    ) {
      isValidated = false;
      messageDialogText = "Please fill mandatory fields";
    } else if (
      this.responseTemplate === "" &&
      (this.dynamicTemplate === "" || this.dynamicTemplate === undefined)
    ) {
      isValidated = false;
      messageDialogText =
        "Please fill either Dynamic template or Response template ";
    } else {
      for (var element of this.derivedOperationList) {
        var operationName = element.operationName;
        var operationFieldsDataJson = JSON.parse(
          JSON.stringify(element.operationFields)
        );
        if (
          operationFieldsDataJson.timeField == "" ||
          (operationFieldsDataJson.epochTime != true &&
            operationFieldsDataJson.timeFormat == "")
        ) {
          isValidated = false;
          if (
            messageDialogText != "Please fill mandatory fields" &&
            messageDialogText !=
              "Please fill either Dynamic template or Response template " &&
            messageDialogText != "Incorrect Dynamic Template"
          ) {
            messageDialogText = "Please fill mandatory fields in InsightTimex";
          }
        } else if (
          operationFieldsDataJson.mappingTimeField != "" &&
          operationFieldsDataJson.mappingTimeFormat == ""
        ) {
          isValidated = false;
          messageDialogText = "Please fill time format for Time Field Mapping";
        } else {
          isValidated = true;
        }
      }
      if (this.isUpdateRequired) {
        if (
          this.fieldUsedForUpdate === "" ||
          this.fieldUsedForUpdate === undefined
        ) {
          isValidated = false;
          messageDialogText = "Please enter the node required for updation";
        }
      }
      if (this.isEventProcessing) {
        if (this.eventConfig === "" || this.eventConfig === undefined) {
          isValidated = false;
          messageDialogText =
            "Please fill event config required for event processing";
        }
      }
    }
    if (!this.isEventProcessing) {
      if (isValidated) {
        var checkname = this.regex.test(this.webhookName);
        var count = (this.labelDisplay.match(/:/g) || []).length;
        var checkLabel = this.regexlabel.test(this.labelDisplay);
        if (!checkname) {
          isValidated = false;
          messageDialogText =
            "Please enter valid webhook name, and it contains only alphanumeric character and underscore ";
        } else if (!checkLabel) {
          isValidated = false;
          messageDialogText =
            "Please enter valid label name, and it contains only alphanumeric character,underscore & colon ";
        } else if (checkLabel) {
          var count = (this.labelDisplay.match(/:/g) || []).length;
          if (count > 1) {
            var splittedLength = this.labelDisplay.split(":").length;
            if (
              this.labelDisplay.split(":")[splittedLength - 1] == "" ||
              this.labelDisplay.split(":")[splittedLength - 1] != "DATA"
            ) {
              isValidated = false;
              messageDialogText =
                "Invalid label Name. Please follow the nomenclature TOOL_CATEGORY:LABEL_NAME:DATA";
            }
          } else {
            isValidated = false;
            messageDialogText =
              "Invalid label Name. Please follow the nomenclature TOOL_CATEGORY:LABEL_NAME:DATA";
          }
        }
      }
    }
    if (isValidated) {
      if (
        !(this.dynamicTemplate === "" || this.dynamicTemplate === undefined)
      ) {
        try {
          JSON.parse(this.dynamicTemplate);
          isValidated = true;
        } catch (error) {
          isValidated = false;
          messageDialogText = "Incorrect Dynamic Template";
        }
      }
    }
    if (isValidated) {
      if (!(this.eventConfig === "" || this.eventConfig === undefined)) {
        try {
          JSON.parse(this.eventConfig);
          isValidated = true;
        } catch (error) {
          isValidated = false;
          messageDialogText = "Error while parsing eventConfig JSON";
        }
      }
    }
    if (isValidated) {
      this.editSaveData();
    } else {
      this.messageDialog.showApplicationsMessage(messageDialogText, "ERROR");
    }
  }

  editSaveData() {
    var self = this;
    var webhookAPIRequestJson = {};
    if (this.actionType == "save") {
      webhookAPIRequestJson["toolName"] = self.selectedTool;
      webhookAPIRequestJson["labelDisplay"] = self.labelDisplay;
      webhookAPIRequestJson["webhookName"] = self.webhookName;
      webhookAPIRequestJson["dataformat"] = self.dataformat;
      webhookAPIRequestJson["mqchannel"] = self.mqchannel;
      webhookAPIRequestJson["responseTemplate"] = self.responseTemplate;
      webhookAPIRequestJson["statussubscribe"] = self.statussubscribe;
      webhookAPIRequestJson["derivedOperations"] = self.derivedOperationList;
      webhookAPIRequestJson["dynamicTemplate"] = self.dynamicTemplate;
      webhookAPIRequestJson["isUpdateRequired"] = self.isUpdateRequired;
      webhookAPIRequestJson["fieldUsedForUpdate"] = self.fieldUsedForUpdate;
      webhookAPIRequestJson["eventConfig"] = self.eventConfig;
      webhookAPIRequestJson["isEventProcessing"] = self.isEventProcessing;
      console.log("dynamic template" + self.dynamicTemplate);
      console.log("eventConfig" + self.eventConfig);
      var dialogmessage =
        " You have created a new Webhook <b>" +
        self.webhookName +
        "</b> .Do you want continue? ";
      title = "Save Webhook " + this.webhookName;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.webhookService
            .saveDataforWebHook(JSON.stringify(webhookAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                self.messageDialog.showApplicationsMessage(
                  "<b>" + self.webhookName + "</b> saved successfully.",
                  "SUCCESS"
                );
                self.list();
              } else if (response.message === "Webhook name already exists") {
                self.messageDialog.showApplicationsMessage(
                  "<b>" +
                    self.webhookName +
                    "</b> already exists. Please try again with a new name.",
                  "ERROR"
                );
              } else if (response.message == "Incorrect Response Template") {
                self.messageDialog.showApplicationsMessage(
                  "Incorrect Response Template.",
                  "ERROR"
                );
              } else {
                self.messageDialog.showApplicationsMessage(
                  "Failed to save the webhook.Please check logs.",
                  "ERROR"
                );
              }
            });
        }
      });
    } else if (this.actionType == "edit") {
      webhookAPIRequestJson["toolName"] = self.selectedTool;
      webhookAPIRequestJson["labelDisplay"] = self.labelDisplay;
      webhookAPIRequestJson["webhookName"] = self.webhookName;
      webhookAPIRequestJson["dataformat"] = self.dataformat;
      webhookAPIRequestJson["mqchannel"] = self.mqchannel;
      webhookAPIRequestJson["responseTemplate"] = self.responseTemplate;
      webhookAPIRequestJson["statussubscribe"] = self.statussubscribe;
      webhookAPIRequestJson["derivedOperations"] = self.derivedOperationList;
      webhookAPIRequestJson["dynamicTemplate"] = self.dynamicTemplate;
      webhookAPIRequestJson["isUpdateRequired"] = self.isUpdateRequired;
      webhookAPIRequestJson["fieldUsedForUpdate"] = self.fieldUsedForUpdate;
      webhookAPIRequestJson["eventConfig"] = self.eventConfig;
      webhookAPIRequestJson["isEventProcessing"] = self.isEventProcessing;
      console.log("response======" + webhookAPIRequestJson);
      var title = "Update " + this.webhookName;
      var dialogmessage =
        " You have updated Webhook <b>" +
        self.webhookName +
        "</b> .Do you want continue? ";
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.webhookService
            .updateforWebHook(JSON.stringify(webhookAPIRequestJson))
            .then(function (data) {
              if (data.status == "success") {
                self.messageDialog.showApplicationsMessage(
                  "Changes made to <b>" +
                    self.webhookName +
                    "</b> are saved successfully.",
                  "SUCCESS"
                );
                self.list();
                self.radioRefresh = false;
              } else {
                self.messageDialog.showApplicationsMessage(
                  "Failed to save Webhook.Please check logs.",
                  "ERROR"
                );
              }
            });
        }
      });
    }
  }

  actionSubscribeOrUnsubscribe(status) {
    var self = this;
    var webhookAPIRequestJson = {};
    webhookAPIRequestJson["webhookName"] = this.selectedWebhook.webhookName;
    if (status == true) {
      this.statussubscribe = true;
      webhookAPIRequestJson["statussubscribe"] = true;
    } else {
      this.statussubscribe = false;
      webhookAPIRequestJson["statussubscribe"] = false;
    }
    this.webhookService
      .updateforWebHookStatus(JSON.stringify(webhookAPIRequestJson))
      .then(function (data) {
        if (data.status == "success") {
          if (status == true) {
            self.messageDialog.showApplicationsMessage(
              "You have subscribed to " +
                "<b> " +
                self.selectedWebhook.webhookName +
                "</b> successfully. You may unsubscribe by clicking the Unsubscribe icon later.",
              "SUCCESS"
            );
          } else {
            self.messageDialog.showApplicationsMessage(
              "You have unsubscribed to " +
                "<b> " +
                self.selectedWebhook.webhookName +
                "</b> successfully. You may subscribe by clicking the Subscribe icon later.",
              "SUCCESS"
            );
          }
          self.list();
          self.radioRefresh = false;
        } else {
          self.messageDialog.showApplicationsMessage(
            "Webhook Subscribe Failed!",
            "ERROR"
          );
        }
      });
  }

  uninstallWebHook() {
    var self = this;
    var title = "Delete WebHook";
    var dialogmessage =
      "Do you want to delete <b>" +
      self.selectedWebhook.webhookName +
      "</b>? <br> <b> Please note: </b> The action of deleting " +
      "<b>" +
      self.selectedWebhook.webhookName +
      "</b> CANNOT be UNDONE. DO you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedWebhook.toolName,
      "ALERT",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        self.webhookService
          .webhookUninstall(self.selectedWebhook.webhookName)
          .then(function (data) {
            self.list();
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }

  changeMqChannel() {
    this.mqchannel = this.mqChannelPrefix + this.webhookName;
  }

  onEventChange() {
    if (!this.isEventProcessing) {
      this.oldLabelValue = this.labelDisplay;
      this.labelDisplay =
        this.selectedTool +
        "_" +
        this.webhookName.toUpperCase() +
        "_" +
        "EVENT";
    } else if (this.isEventProcessing) {
      if (this.oldLabelValue === undefined) {
        var labelnameIndex = this.toolsArr.indexOf(this.selectedTool);
        this.oldLabelValue = this.labelsArr[labelnameIndex];
      }
      this.labelDisplay = this.oldLabelValue;
      this.eventConfig = "";
    }
  }

  addTimeFieldMappings() {
    let dervioplist = this.getDerivedOperations(
      -1,
      "timeFieldSeriesMapping",
      JSON.parse(
        '{"mappingTimeField":"","epochTime":false,"mappingTimeFormat":""}'
      ),
      this.webhookName
    );
    this.derivedOperationList.push(dervioplist);
  }

  deleteTimeFieldMappings(i: number) {
    this.removeMappingRecord(i, "timeFieldSeriesMapping");
  }

  addDataEnrichment() {
    let dervioplist = this.getDerivedOperations(
      -1,
      "dataEnrichment",
      JSON.parse('{"sourceProperty":"","keyPattern":"","targetProperty":""}'),
      this.webhookName
    );
    this.derivedOperationList.push(dervioplist);
  }

  deleteEnrichmentData(i: number) {
    this.removeMappingRecord(i, "dataEnrichment");
  }

  getOperationsFieldsJson(wid, operationName, operationFields, webhookName) {
    return {
      wid: wid,
      operationName: operationName,
      operationFields: operationFields,
      webhookName: webhookName,
    };
  }

  getDerivedOperations(
    wid,
    operationName,
    operationFields,
    webhookName
  ): DerivedOperations {
    let derivOperation = new DerivedOperations();
    derivOperation.setData(wid, operationName, operationFields, webhookName);
    return derivOperation;
  }

  getderivedOperationItems(filtername: any) {
    if (filtername == "insightsTimex") {
      return this.derivedOperationList.filter(
        (item) => item.operationName == filtername
      );
    } else if (filtername == "timeFieldSeriesMapping") {
      return this.derivedOperationList.filter(
        (item) => item.operationName == filtername && item.operationFields != ""
      );
    } else if (filtername == "dataEnrichment") {
      return this.derivedOperationList.filter(
        (item) => item.operationName == filtername && item.operationFields != ""
      );
    }
  }

  copyInputMessage(inputElement) {
    var hostname = this.initservice.getWebhookHost();
    var value_to_copy =
      hostname +
      "/PlatformInsightsWebHook/insightsDevOpsWebHook?webHookName=" +
      inputElement.webhookName;
    this.textToCopy = value_to_copy;
    this._clipboardService.copyFromContent(value_to_copy);
  }

  removeMappingRecord(i: number, filtername: string) {
    let recordToremove: DerivedOperations = this.getderivedOperationItems(
      filtername
    )[i];
    let indexToRemove = this.derivedOperationList.indexOf(recordToremove);
    this.derivedOperationList.splice(indexToRemove, 1);
  }
}
