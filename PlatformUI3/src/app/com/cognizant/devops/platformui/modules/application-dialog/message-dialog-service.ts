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
import { ApplicationMessageDialog } from '@insights/app/modules/application-dialog/application-message-dialog';
import { ConfirmationMessageDialog } from '@insights/app/modules/application-dialog/confirmation-message-dialog';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { DataSharedService } from '@insights/common/data-shared-service';


@Injectable()
export class MessageDialogService {

    constructor(public dialog: MatDialog, private dataShare: DataSharedService) {

    }

    public showApplicationsMessage(message, type): MatDialogRef<ApplicationMessageDialog> {
        var isSessionExpired = this.dataShare.validateSession();
        if (!isSessionExpired) {
            const dialogRef = this.dialog.open(ApplicationMessageDialog, {
                panelClass: 'DialogBox',
                width: '40%',
                height: '32%',
                disableClose: true,
                data: {
                    title: "Message",
                    message: message,
                    type: type
                }
            });
            return dialogRef;
        }
        else {
            console.log("Session Expire")
        }
    }

    public showConfirmationMessage(title, message, value, type, height): MatDialogRef<ConfirmationMessageDialog> {
        var isSessionExpired = this.dataShare.validateSession();
        if (!isSessionExpired) {
            const dialogRef = this.dialog.open(ConfirmationMessageDialog, {
                panelClass: 'DialogBox',
                width: '45%',
                height: height,
                disableClose: true,
                data: {
                    title: title,
                    message: message,
                    value: value,
                    type: type,
                    height: height

                }
            });
            return dialogRef;
        }
        else {
            console.log("Session Expire")
        }
    }
}