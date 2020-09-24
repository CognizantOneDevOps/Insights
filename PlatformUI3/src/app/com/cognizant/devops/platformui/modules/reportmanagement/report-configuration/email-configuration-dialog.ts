
/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from '@insights/common/data-shared-service';

@Component({
    selector: 'email-configuration-dialog',
    templateUrl: 'email-configuration-dialog.html',
    styleUrls: ['email-configuration-dialog.css', './../../home.module.css']
})

export class EmailConfigurationDialog {

    senderEmailAddress: string = '';
    receiverEmailAddress: string = '';
    mailSubject: string = '';
    mailBodyTemplate: string = '';
    receiverCCEmailAddress: string = '';
    receiverBCCEmailAddress: string = '';
    buttonName: string;
    scriptReg: string = "<script>";
    constructor(public dialogRef: MatDialogRef<EmailConfigurationDialog>, @Inject(MAT_DIALOG_DATA) public data: any, public messageDialog: MessageDialogService,public dataShare: DataSharedService,) {
        this.initializeVariables();
    }

    initializeVariables() {
        console.log("type: ", this.data.type);
        if (this.data.type == 'edit' && this.data.emailDetails) {
            console.log("Details: ", this.data.emailDetails);
            this.senderEmailAddress = this.data.emailDetails.senderEmailAddress;
            this.receiverEmailAddress = this.data.emailDetails.receiverEmailAddress;
            this.receiverCCEmailAddress = this.data.emailDetails.receiverCCEmailAddress;
            this.receiverBCCEmailAddress = this.data.emailDetails.receiverBCCEmailAddress;
            this.mailSubject = this.data.emailDetails.mailSubject;
            this.mailBodyTemplate = this.data.emailDetails.mailBodyTemplate.replace(/#/g, "<").replace(/~/g, ">");
            this.buttonName = 'EDIT';
        }
        else {
            this.senderEmailAddress = '';
            this.receiverEmailAddress = '';
            this.receiverCCEmailAddress='';
            this.receiverBCCEmailAddress='';
            this.mailSubject = '';
            this.mailBodyTemplate = '';
            this.buttonName = 'ADD';
        }
    }

    closeEmailConfigDialog() {
        this.dialogRef.close(null);
    }

    saveEmailConfig() {
        let isValidated = true;
        let self = this;
        let messageDialogText;
        if (!this.receiverEmailAddress && !this.receiverCCEmailAddress && !this.receiverBCCEmailAddress) {
            isValidated = false;
            messageDialogText = "Please enter one of the recipient email address";
        }
        else if (
            this.senderEmailAddress === '' ||
            this.mailSubject === '' ||
            this.mailBodyTemplate === ''
        ) {
            isValidated = false;
            messageDialogText = "Please fill mandatory fields";
        }
        else if (!this.dataShare.validateEmailAddresses(self.senderEmailAddress)) {
            isValidated = false;
            messageDialogText = "Error in From email address format.";
        } else {
            if (self.receiverEmailAddress) {
                isValidated = self.dataShare.validateEmailAddresses(self.receiverEmailAddress);
                if(!isValidated){
                    messageDialogText = "Error in To email address format.";
                }
            }
            if (isValidated == true) {
                if (self.receiverCCEmailAddress) {
                    isValidated = self.dataShare.validateEmailAddresses(self.receiverCCEmailAddress);
                    if(!isValidated){
                        messageDialogText = "Error in Cc email address format.";
                    }
                }
            }
            if (isValidated == true) {
                if (this.receiverBCCEmailAddress) {
                    isValidated = self.dataShare.validateEmailAddresses(self.receiverBCCEmailAddress);
                    if(!isValidated){
                        messageDialogText = "Error in Bcc email address format.";
                    }
                }
            }
        }
        if (this.mailBodyTemplate.search(this.scriptReg) !== -1) {
            isValidated = false;
            messageDialogText = "Mail body templates cannot have script tags.";
        }
        console.log("The mail body template: ", this.mailBodyTemplate);
        if (isValidated) {
            this.mailBodyTemplate = this.mailBodyTemplate.replace(/</g, "#").replace(/>/g, "~");
            let emailDetailsJson = { 'senderEmailAddress': self.senderEmailAddress, 'receiverEmailAddress': self.receiverEmailAddress, 'mailSubject': self.mailSubject, 'mailBodyTemplate': self.mailBodyTemplate, 'receiverCCEmailAddress': self.receiverCCEmailAddress, 'receiverBCCEmailAddress': self.receiverBCCEmailAddress }
            this.dialogRef.close(emailDetailsJson);
        } else {
            this.messageDialog.showApplicationsMessage(messageDialogText, "ERROR");
        }

    }
}
