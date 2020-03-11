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
import { Component, OnInit, ViewChild } from '@angular/core';
import { WebHookService } from '@insights/app/modules/webhook/webhook.service';
import { MatDialog } from '@angular/material';
import { Router } from "@angular/router";
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatTableDataSource, MatPaginator } from '@angular/material';
import { DataSharedService } from '@insights/common/data-shared-service';
import { BulkUploadService } from '@insights/app/modules/bulkupload/bulkupload.service'
import { InsightsInitService } from '@insights/common/insights-initservice';
import { ClipboardService } from 'ngx-clipboard'
import { DerivedOperations } from './derivedOperationsConfig';
export interface DataType {
    value: string;
    viewValue: string;
}
@Component({
    selector: 'app-webhook',
    templateUrl: './webhook.component.html',
    styleUrls: ['./webhook.component.css', './../home.module.css']
})
export class WebHookComponent implements OnInit {
    toolsArr = [];
    toolvalue: String = null;
    labelsArr = [];
    toolsDetail = [];
    showAddWebHook: boolean = false;
    enableWebhookicon: boolean = false;
    showWebhook: boolean = true;
    showMessage: string;
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
    mqChannelPrefix = "IPW_";
    regex = new RegExp("^[a-zA-Z0-9_]*$");
    regexlabel = new RegExp("^\\d+(:([A-Za-z]+|\\d+)){2}$");
    timeFieldRegex = new RegExp("^[a-zA-Z0-9]*$");
    derivedOperations = [];
    derivedOperationList: DerivedOperations[] = [];
    enableSaveForEdit: boolean = false;
    refreshDerivedOperations: string;
    @ViewChild(MatPaginator) paginator: MatPaginator;
    dataformats: DataType[] = [
        { value: 'json', viewValue: 'Json' },
    ];
    constructor(private router: Router, private _clipboardService: ClipboardService, private bulkuploadService: BulkUploadService, private initservice: InsightsInitService, private webhookService: WebHookService, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService) {
        this.getLabelTools();
        this.getRegisteredWebHooks();

    }
    ngOnInit() {
    }

    setWebhookUrl(selectedWebhookEnable) {
        var hostname = this.initservice.getWebhookHost();
        var value_to_copy = hostname + "/PlatformInsightsWebHook/insightsDevOpsWebHook?webHookName=" + selectedWebhookEnable.webhookName;
        console.log(value_to_copy)
        this.textToCopy = value_to_copy;
    }
    enableButtons(selectedWebhookEnable) {
        if (this.showAddWebHook == false) {
            this.enableDelete = true;
            this.enableEdit = true;
            this.radioRefresh = true;
            this.enableaddWebhook = false;
            this.setWebhookUrl(selectedWebhookEnable);
            if (selectedWebhookEnable.subscribeStatus == 'Unsubscribed') {
                this.enableunsubscribe = false;
                this.enablesubscribe = true;
            }
            else {
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
        console.log(this.webhookList);
        if (this.webhookList != null && this.webhookList.status == 'success') {
            this.webhookDatasource.data = this.webhookList.data.sort((a, b) => a.webhookName > b.webhookName);
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
                        this.webhookDatasource.data[counter].subscribeStatus = 'Subscribed'
                    }
                    else {
                        this.webhookDatasource.data[counter].subscribeStatus = 'Unsubscribed'
                    }
                    counter = counter + 1;
                } else {
                    break;
                }
            }
            self.showDetail = true;
            this.displayedColumns = ['radio', 'WebHookName', 'ToolName', 'LabelName', 'DataType', 'MqChannel', 'Status'];
            setTimeout(() => {
                this.showConfirmMessage = "";
            }, 3000);
        } else {
            self.showMessage = "Something wrong with Service.Please try again.";
            self.messageDialog.showApplicationsMessage("Something wrong with Service.Please try again.", "ERROR");
        }
        console.log(this.webhookDatasource.data);
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
        if (toolname === undefined) {
        }
        else {
            var i = 0;
            var labelnameIndex = this.toolsArr.indexOf(toolname)
            this.labelDisplay = this.labelsArr[labelnameIndex];

        }
    }
    async getLabelTools() {
        var self = this;
        try {
            self.toolsDetail = [];
            let toollabelresponse = await this.bulkuploadService.loadUiServiceLocation()
            if (toollabelresponse.status == "success") {
                this.toolsDetail = toollabelresponse.data;
            }
            console.log(this.toolsDetail)
            for (var element of this.toolsDetail) {
                var toolName = (element.toolName);
                var labelName = (element.label);
                this.toolsArr.push(toolName);
                this.labelsArr.push(labelName);
            }
        }
        catch (error) {
            //  console.log(error);
        }
    }

    addWebHook() {
        console.log(" In addWebHook ");
        this.showAddWebHook = true;
        this.enableWebhookicon = true;
        this.showWebhook = false;
        this.enableaddWebhook = false;
        this.webhookName = "";
        this.selectedTool = "";
        this.labelDisplay = ""
        this.mqchannel = "";
        this.responseTemplate = "";
        this.dataformat = ""
        this.enableRefresh = true;
        this.disableInputFields = false;
        this.derivedOperationList = [];
        this.actionType = "save";
        var jsonData = JSON.parse(this.webhookService.getSampleJSONResponse());
        for (var element of jsonData) {
            //var operationFieldsDataJson = JSON.parse(element.operationFields);
            let dervioplist = this.getDerivedOperations(element.wid, element.operationName, element.operationFields, this.webhookName);
            this.derivedOperationList.push(element);
        }
    }
    async editWebhook() {
        this.enableSaveForEdit = true;
        this.enableEdit = false;
        var isSessionExpired = this.dataShare.validateSession();
        this.enableDelete = false;
        if (!isSessionExpired) {
            this.disableInputFields = true;
            this.derivedOperationList = [];
            this.webhookName = this.selectedWebhook.webhookName;
            this.selectedTool = this.selectedWebhook.toolName;
            this.labelDisplay = this.selectedWebhook.labelDisplay;
            this.mqchannel = this.selectedWebhook.mqChannel;
            for (let i in this.selectedWebhook.derivedOperations) {
                let element = this.selectedWebhook.derivedOperations[i];
                console.log(element);
                var operationFieldsDataJson = JSON.parse(element.operationFields);
                let derivedOpslist = this.getDerivedOperations(element.wid, element.operationName, operationFieldsDataJson, this.webhookName);
                this.derivedOperationList.push(derivedOpslist);

            }
            if (this.selectedWebhook.responseTemplate != undefined) {
                this.responseTemplate = this.selectedWebhook.responseTemplate;
            }
            this.dataformat = this.selectedWebhook.dataFormat;
            if (this.selectedWebhook.subscribeStatus == 'Subscribed') {
                this.statussubscribe = true;
            }
            else {
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
        for (let index of this.derivedOperationList) {
            if (index.operationName == 'insightsTimex') {
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

    validateWebhookData(selectedWebhook) {
        var isValidated = false;
        let messageDialogText;
        if (selectedWebhook != undefined || selectedWebhook != null) {
            if (selectedWebhook.webhookName == "" || selectedWebhook.selectedTool == "" || selectedWebhook.labelDisplay == "" ||
                                                            selectedWebhook.mqchannel == "" || selectedWebhook.responseTemplate == "") {
                isValidated = false;                                                
                messageDialogText = "Please fill mandatory fields ";
            } else {
                isValidated = true;
            }
            for (var element of this.derivedOperationList) {
                var operationFieldsDataJson = JSON.parse(JSON.stringify(element.operationFields));
                if (operationFieldsDataJson.timeField == "" || 
                (operationFieldsDataJson.timeField != "" && operationFieldsDataJson.epochTime == true)) {
                    isValidated = false;
                    if(messageDialogText != "Please fill mandatory fields ") {
                    messageDialogText = "Please fill mandatory fields in InsightTimex";
                    }
                } else if (operationFieldsDataJson.mappingTimeField != "" && operationFieldsDataJson.mappingTimeFormat == "") {
                    isValidated = false;
                    messageDialogText = "Please fill time format for Time Field Mapping";
                } else {
                    isValidated = true;
                }
            }
        } else {
            if (this.actionType == "save") {
                if (this.webhookName == "" || this.selectedTool == "" || this.labelDisplay == "" ||
                                                        this.mqchannel == "" || this.responseTemplate == "") {
                    isValidated = false;
                    messageDialogText = "Please fill mandatory fields ";
                } else {
                    isValidated = true;
                }
                for (var element of this.derivedOperationList) {
                    var operationFieldsDataJson = JSON.parse(JSON.stringify(element.operationFields));
                    if (operationFieldsDataJson.timeField == "" || 
                    (operationFieldsDataJson.timeField != "" && operationFieldsDataJson.epochTime == true)) {
                        isValidated = false;
                        if(messageDialogText != "Please fill mandatory fields ") {
                            messageDialogText = "Please fill mandatory fields in InsightTimex";
                        }
                    } else if (operationFieldsDataJson.mappingTimeField != "" && operationFieldsDataJson.mappingTimeFormat == "") {
                        isValidated = false;
                        messageDialogText = "Please fill time format for Time Field Mapping";
                    } else {
                        isValidated = true;
                    }
                }
                var checkname = this.regex.test(this.webhookName);
                if (!checkname) {
                    isValidated = false;
                    messageDialogText = "Please enter valid webhook name, and it contains only alphanumeric character and underscore ";
                }
            } 
        }
        if (isValidated) {
            this.editSaveData(selectedWebhook);
        }else{
            this.messageDialog.showApplicationsMessage(messageDialogText, "ERROR");
        }
    }

    editSaveData(selectedWebhook) {
        var self = this;
        var webhookAPIRequestJson = {};

        if (this.actionType == 'save') {
            console.log("1", selectedWebhook);
            webhookAPIRequestJson['toolName'] = self.selectedTool
            webhookAPIRequestJson['labelDisplay'] = self.labelDisplay
            webhookAPIRequestJson['webhookName'] = self.webhookName
            webhookAPIRequestJson['dataformat'] = self.dataformat
            webhookAPIRequestJson['mqchannel'] = self.mqchannel
            webhookAPIRequestJson['responseTemplate'] = self.responseTemplate
            webhookAPIRequestJson['statussubscribe'] = self.statussubscribe
            webhookAPIRequestJson['derivedOperations'] = self.derivedOperationList
            console.log("webhookAPIRequestJson ", JSON.stringify(webhookAPIRequestJson))
            var dialogmessage = " You have created a new Webhook <b>" + self.webhookName + "</b> .Do you want continue? "
            const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
            dialogRef.afterClosed().subscribe(result => {
                if (result == 'yes') {
                    this.webhookService.saveDataforWebHook(JSON.stringify(webhookAPIRequestJson))
                        .then(function (response) {
                            if (response.status == "success") {
                                self.messageDialog.showApplicationsMessage("<b>" + self.webhookName + "</b> saved successfully.", "SUCCESS");
                                self.list();
                            } else if (response.message == "Webhook name already exists.") {
                                self.messageDialog.showApplicationsMessage("<b>" + self.webhookName + "</b> already exists. Please try again with a new name.", "ERROR");
                            } else if (response.message == "Incorrect Response Template") {
                                self.messageDialog.showApplicationsMessage("Incorrect Response Template.", "ERROR");
                            } else {
                                self.messageDialog.showApplicationsMessage("Failed to save the webhook.Please check logs.", "ERROR");
                            }
                        })
                }
            })
        } else if (this.actionType == 'edit') {
            console.log("1", selectedWebhook);
            webhookAPIRequestJson['toolName'] = self.selectedTool
            webhookAPIRequestJson['labelDisplay'] = self.labelDisplay
            webhookAPIRequestJson['webhookName'] = self.webhookName
            webhookAPIRequestJson['dataformat'] = self.dataformat
            webhookAPIRequestJson['mqchannel'] = self.mqchannel
            webhookAPIRequestJson['responseTemplate'] = self.responseTemplate
            webhookAPIRequestJson['statussubscribe'] = self.statussubscribe
            webhookAPIRequestJson['derivedOperations'] = self.derivedOperationList
            console.log("webhookAPIRequestJson in Edit", JSON.stringify(webhookAPIRequestJson))
            var title = "Update " + this.webhookName;
            var dialogmessage = " You have updated Webhook <b>" + self.webhookName + "</b> .Do you want continue? "
            const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
            dialogRef.afterClosed().subscribe(result => {
                if (result == 'yes') {
                    this.webhookService.updateforWebHook(JSON.stringify(webhookAPIRequestJson))
                        .then(function (data) {
                            if (data.status == "success") {
                                self.messageDialog.showApplicationsMessage("Changes made to <b>" + self.webhookName + "</b> are saved successfully.", "SUCCESS");
                                self.list();
                                self.radioRefresh = false;
                            } else {
                                self.messageDialog.showApplicationsMessage("Failed to save Webhook.Please check logs.", "ERROR");
                            }
                        })
                }
            })
        }
    }

    actionSubscribeOrUnsubscribe(status) {
        var self = this;
        var webhookAPIRequestJson = {};
        webhookAPIRequestJson['webhookName'] = this.selectedWebhook.webhookName;
        if (status == true) {
            this.statussubscribe = true;
            webhookAPIRequestJson['statussubscribe'] = true
        } else {
            this.statussubscribe = false;
            webhookAPIRequestJson['statussubscribe'] = false;
        }
        this.webhookService.updateforWebHookStatus(JSON.stringify(webhookAPIRequestJson))
            .then(function (data) {
                if (data.status == "success") {
                    if (status == true) {
                        self.messageDialog.showApplicationsMessage("You have subscribed to " + "<b> " + self.selectedWebhook.webhookName + "</b> successfully. You may unsubscribe by clicking the Unsubscribe icon later.", "SUCCESS");
                    } else {
                        self.messageDialog.showApplicationsMessage("You have unsubscribed to " + "<b> " + self.selectedWebhook.webhookName + "</b> successfully. You may subscribe by clicking the Subscribe icon later.", "SUCCESS");
                    }
                    self.list();
                    self.radioRefresh = false;
                } else {
                    self.messageDialog.showApplicationsMessage("Webhook Subscribe Failed!", "ERROR");
                }
            }
            )
    }

    uninstallWebHook() {
        var self = this;;
        var title = "Delete WebHook";
        var dialogmessage = "Do you want to delete <b>" + self.selectedWebhook.webhookName + "</b>? <br> <b> Please note: </b> The action of deleting " + "<b>" + self.selectedWebhook.webhookName + "</b> CANNOT be UNDONE. DO you want to continue? ";
        const dialogRef = self.messageDialog.showConfirmationMessage(title, dialogmessage, this.selectedWebhook.toolName, "ALERT", "40%");
        dialogRef.afterClosed().subscribe(result => {
            if (result == 'yes') {
                self.webhookService.webhookUninstall(self.selectedWebhook.webhookName).then(function (data) {
                    self.list()
                }).catch(function (data) {
                    self.showConfirmMessage = "service_error";
                });
            }
        });
    }


    changeMqChannel() {
        this.mqchannel = this.mqChannelPrefix + this.webhookName;
    }

    addTimeFieldMappings() {
        let dervioplist = this.getDerivedOperations(-1, "timeFieldSeriesMapping", JSON.parse("{\"mappingTimeField\":\"\",\"epochTime\":false,\"mappingTimeFormat\":\"\"}"), this.webhookName);
        this.derivedOperationList.push(dervioplist);
    }

    deleteTimeFieldMappings(i: number) {
        this.removeMappingRecord(i, 'timeFieldSeriesMapping');

    }

    addDataEnrichment() {
        let dervioplist = this.getDerivedOperations(-1, "dataEnrichment", JSON.parse("{\"sourceProperty\":\"\",\"keyPattern\":\"\",\"targetProperty\":\"\"}"), this.webhookName);
        this.derivedOperationList.push(dervioplist);
    }

    deleteEnrichmentData(i: number) {
        this.removeMappingRecord(i, 'dataEnrichment');
    }

    getOperationsFieldsJson(wid, operationName, operationFields, webhookName) {
        return { 'wid': wid, 'operationName': operationName, 'operationFields': operationFields, 'webhookName': webhookName };
    }

    getDerivedOperations(wid, operationName, operationFields, webhookName): DerivedOperations {
        let derivOperation = new DerivedOperations();
        derivOperation.setData(wid, operationName, operationFields, webhookName);
        return derivOperation;
    }

    getderivedOperationItems(filtername: any) {
        if (filtername == 'insightsTimex') {
            return this.derivedOperationList.filter(item => (item.operationName == filtername));
        } else if (filtername == 'timeFieldSeriesMapping') {
            return this.derivedOperationList.filter(item => (item.operationName == filtername && item.operationFields != ""));
        } else if (filtername == 'dataEnrichment') {
            return this.derivedOperationList.filter(item => (item.operationName == filtername && item.operationFields != ""));
        }
    }

    copyInputMessage(inputElement) {
        console.log("First " + this.textToCopy)
        var hostname = this.initservice.getWebhookHost();
        var value_to_copy = hostname + "/PlatformInsightsWebHook/insightsDevOpsWebHook?webHookName=" + inputElement.webhookName;
        console.log(value_to_copy)
        this.textToCopy = value_to_copy;
        console.log("Second" + this.textToCopy);
        this._clipboardService.copyFromContent(value_to_copy);
    }

    removeMappingRecord(i: number, filtername: string) {
        let recordToremove: DerivedOperations = this.getderivedOperationItems(filtername)[i];
        let indexToRemove = this.derivedOperationList.indexOf(recordToremove)
        this.derivedOperationList.splice(indexToRemove, 1);
    }
}
