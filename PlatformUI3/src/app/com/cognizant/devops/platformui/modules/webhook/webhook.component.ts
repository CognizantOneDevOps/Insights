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
import { ShowJsonDialog } from '@insights/app/modules/relationship-builder/show-correlationjson';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { from } from 'rxjs';
import { Router, NavigationExtras } from "@angular/router";
import { ActivatedRoute } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatTableDataSource, MatPaginator } from '@angular/material';
import { DataSharedService } from '@insights/common/data-shared-service';
import { BulkUploadService } from '@insights/app/modules/bulkupload/bulkupload.service'
import { count } from 'rxjs/operators';
import { ApplicationMessageDialog } from '../application-dialog/application-message-dialog';
//import { Control} from '@angular/common';
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
    toolsDetail = [];
    showAddWebHook: boolean = false;
    showWebhook: boolean = true;
    showMessage: string;
    displayedColumns = [];
    webhookNameList: any = [];
    webhookDatasource = new MatTableDataSource
        <any>();
    webhookList: any;
    showDetail: boolean = false;
    showConfirmMessage: string;
    selectedWebhook: any;
    webhookparameter = {};
    webhookName: any;
    selectedTool: any;
    eventToSubscribe: any;
    mqchannel: any;
    dataformat: any;
    actionType: any;
    statussubscribe: boolean = false;
    enableDelete: boolean = false;
    enableRefresh: boolean = false;
    enableEdit: boolean = false;
    enableunsubscribe: boolean = false;
    enablesubscribe: boolean = false;
    radioRefresh: boolean = false;
    regex = new RegExp("^[a-zA-Z0-9_]*$");
    @ViewChild(MatPaginator) paginator: MatPaginator;
    dataformats: DataType[] = [
        { value: 'text', viewValue: 'Text' },
        { value: 'json', viewValue: 'Json' },
    ];
    constructor(private router: Router, private bulkuploadService: BulkUploadService, private webhookService: WebHookService, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService) {
        this.getLabelTools();
        this.getRegisteredWebHooks();
    }
    ngOnInit() {
    }
    addWebHook() {
        this.showAddWebHook = true;
        this.showWebhook = false;
        this.webhookName = "";
        this.selectedTool = "";
        this.eventToSubscribe = ""
        this.mqchannel = "";
        this.dataformat = ""
    }
    enableButtons(selectedWebhookEnable) {
        if (this.showAddWebHook == false) {
            this.enableDelete = true;
            this.enableEdit = true;
            this.radioRefresh = true;
            //console.log(selectedWebhookEnable)
            if (selectedWebhookEnable.subscribeStatus == 'UNSUBSCRIBED') {
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
        this.webhookList = await self.webhookService.loadwebhookServices();
        if (this.webhookList != null && this.webhookList.status == 'success') {
            this.webhookDatasource.data = this.webhookList.data.sort((a, b) => a.webhookName > b.webhookName);
            this.webhookDatasource.paginator = this.paginator;
            // console.log(this.webhookList);
            this.webhookNameList.push("All");
            for (var data of this.webhookList.data) {
                if (this.webhookNameList.indexOf(data.webhookName) == -1) {
                    this.webhookNameList.push(data.webhookName);
                }
            }
            var counter = 0;
            for (var element of this.webhookDatasource.data) {
                if (counter
                    < this.webhookDatasource.data.length) {
                    // console.log(this.webhookDatasource.data[counter].toolName)
                    // console.log(this.webhookDatasource.data[counter].subscribeStatus);
                    if (this.webhookDatasource.data[counter].subscribeStatus == true) {
                        this.webhookDatasource.data[counter].subscribeStatus = 'SUBSCRIBED'
                    }
                    else {
                        this.webhookDatasource.data[counter].subscribeStatus = 'UNSUBSCRIBED'
                    }
                    counter = counter + 1;
                }
                else {
                    break;
                }
            }
            self.showDetail = true;
            //console.log(this.agentNameList);
            this.displayedColumns = ['radio', 'WebHookName', 'ToolName', 'EventName', 'DataType', 'MqChannel', 'Status'];
            setTimeout(() => {
                this.showConfirmMessage = "";
            }, 3000);
        } else {
            self.showMessage = "Something wrong with Service.Please try again.";
            self.messageDialog.showApplicationsMessage("Something wrong with Service.Please try again.", "ERROR");
        }
    }
    list() {
        this.showWebhook = true;
        this.showAddWebHook = false;
        this.getRegisteredWebHooks();
        this.enableDelete = false;
        this.enableEdit = false;
        this.enableRefresh = false;
        this.enablesubscribe = false;
        this.enableunsubscribe = false;
    }
    async getLabelTools() {
        var self = this;
        try {
            self.toolsDetail = [];
            let toollabelresponse = await this.bulkuploadService.loadUiServiceLocation()
            if (toollabelresponse.status == "success") {
                this.toolsDetail = toollabelresponse.data;
            }
            for (var element of this.toolsDetail) {
                var toolName = (element.toolName);
                var labelName = (element.label);
                this.toolsArr.push(toolName);
                //  console.log(this.toolsArr)
            }
        }
        catch (error) {
            //  console.log(error);
        }
    }
    actionSubscribeOrUnsubscribe(status, selectedWebhook) {
        var self = this;
        if (status == true) {
            this.statussubscribe = true;
            this.webhookService.updateforWebHook(this.selectedWebhook.webhookName, this.selectedWebhook.toolName, this.selectedWebhook.eventName, this.selectedWebhook.dataFormat, this.selectedWebhook.mqChannel, this.statussubscribe)
                .then(function (data) {
                    //  console.log("WeBhook " + data);
                    if (data.status == "success") {
                        self.messageDialog.showApplicationsMessage("Webhook Subscribed Successfully!", "SUCCESS");
                        self.getRegisteredWebHooks();
                        self.list();
                        self.radioRefresh = false;
                    } else {
                        self.messageDialog.showApplicationsMessage("Webhook Subscribe Failed!", "ERROR");
                    }
                })
        }
        else {
            this.statussubscribe = false;
            this.webhookService.updateforWebHook(this.selectedWebhook.webhookName, this.selectedWebhook.toolName, this.selectedWebhook.eventName, this.selectedWebhook.dataFormat, this.selectedWebhook.mqChannel, this.statussubscribe)
                .then(function (data) {
                    // console.log("WeBhook " + data);
                    if (data.status == "success") {
                        self.messageDialog.showApplicationsMessage("Webhook Unsubscribed Successfully!", "SUCCESS");
                        self.getRegisteredWebHooks();
                        self.list();
                        self.radioRefresh = false;
                    } else {
                        self.messageDialog.showApplicationsMessage("Webhook Unsubscribe Failed!", "ERROR");
                    }
                })
        }
    }
    saveData(webhookName, selectedTool, eventToSubscribe, dataformat, mqchannel) {
        var self = this;

        if (webhookName == "") {
            this.messageDialog.showApplicationsMessage("Please select a Webhook Name ", "ERROR");
        }
        else if (selectedTool == "") {
            this.messageDialog.showApplicationsMessage("Please select a Tool ", "ERROR");
        }

        else if (eventToSubscribe == "") {
            this.messageDialog.showApplicationsMessage("Please select an Event to Subscribe ", "ERROR");
        }
        else if (dataformat == "") {
            this.messageDialog.showApplicationsMessage("Please select a Data Format ", "ERROR");
        }
        else if (mqchannel == "") {
            this.messageDialog.showApplicationsMessage("Please select a MQ Channel ", "ERROR");
        }


        else {



            var checkname = this.regex.test(webhookName);
            if (!checkname) {
                webhookName = undefined;
                this.messageDialog.showApplicationsMessage("Please enter valid name, and it contains only alphanumeric character and underscore ", "ERROR");
            }
            else {

                if (this.actionType == 'edit') {
                    var title = "Edit Webhook"
                    var dialogmessage = " Are you sure you want to update the webhook?"
                    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");

                    dialogRef.afterClosed().subscribe(result => {
                        if (result == 'yes') {

                            this.webhookService.updateforWebHook(webhookName, selectedTool, eventToSubscribe, dataformat, mqchannel, this.statussubscribe)
                                .then(function (data) {
                                    console.log("WeBhook " + data);
                                    if (data.status == "success") {
                                        self.messageDialog.showApplicationsMessage("Webhook saved successfully.", "SUCCESS");

                                        self.list();
                                        self.radioRefresh = false;
                                    } else {
                                        self.messageDialog.showApplicationsMessage("Failed to save Webhook", "ERROR");
                                    }
                                })
                        }
                    })
                }
                else {
                    var title = "Save Webhook"
                    var dialogmessage = " Are you sure you want to save the webhook?"
                    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");

                    dialogRef.afterClosed().subscribe(result => {
                        if (result == 'yes') {
                            this.webhookService.saveDataforWebHook(webhookName, selectedTool, eventToSubscribe, dataformat, mqchannel, false)
                                .then(function (data) {
                                    //   console.log("WeBhook " + data);
                                    if (data.status == "success") {
                                        self.messageDialog.showApplicationsMessage("WebHook saved successfully", "SUCCESS");
                                        self.list();
                                    } else {
                                        this.list();
                                        self.messageDialog.showApplicationsMessage("Failed to save the webhook", "ERROR");
                                    }
                                })
                        }
                    })
                }
            }
        }
    }
    uninstallWebHook() {
        var self = this;
        //console.log("uninstall agent " + JSON.stringify(this.selectedAgent));
        var title = "Delete WebHook";
        var dialogmessage = "Do you want to uninstall <b> " + self.selectedWebhook.webhookName + "</b> <b>  </b> ? ";
        const dialogRef = self.messageDialog.showConfirmationMessage(title, dialogmessage, this.selectedWebhook.toolName, "ALERT", "40%");
        dialogRef.afterClosed().subscribe(result => {
            //console.log('The dialog was closed  ' + result);
            if (result == 'yes') {
                self.webhookService.webhookUninstall(self.selectedWebhook.webhookName).then(function (data) {
                    self.getRegisteredWebHooks();
                    self.list()
                }).catch(function (data) {
                    self.showConfirmMessage = "service_error";
                    self.getRegisteredWebHooks();
                });
            }
        });
    }
    async editWebhook() {
        var isSessionExpired = this.dataShare.validateSession();
        this.enableDelete = false;
        if (!isSessionExpired) {
            //  console.log(this.selectedWebhook);
            this.webhookName = this.selectedWebhook.webhookName;
            this.selectedTool = this.selectedWebhook.toolName;
            this.eventToSubscribe = this.selectedWebhook.eventName
            this.mqchannel = this.selectedWebhook.mqChannel;
            this.dataformat = this.selectedWebhook.dataFormat;
            if (this.selectedWebhook.subscribeStatus == 'SUBSCRIBED') {
                this.statussubscribe = true;
            }
            else {
                this.statussubscribe = false;
            }
            this.actionType = "edit";
            this.showAddWebHook = true;
            this.showWebhook = false;
        }
    }
    Refresh() {
        this.radioRefresh = false;
        this.enablesubscribe = false;
        this.enableDelete = false;
        this.enableEdit = false;
        this.enableunsubscribe = false;
    }
}
