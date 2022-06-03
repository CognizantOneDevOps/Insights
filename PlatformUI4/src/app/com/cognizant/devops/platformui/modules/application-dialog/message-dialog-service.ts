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
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SnackbarComponent } from './snackbar-message';

@Injectable()
export class MessageDialogService implements OnInit {


    constructor(public dialog: MatDialog, private dataShare: DataSharedService,
        private snackBar: MatSnackBar,
    ) { }
    ngOnInit() {
    }
    public showApplicationsMessage(message, type, height?): MatDialogRef<ApplicationMessageDialog> {
        let contHeight;
        if (height === '' || height === undefined) {
            contHeight = '25%'
        } else {
            contHeight = height;
        }
        var isSessionExpired = this.dataShare.validateSession();
        if (!isSessionExpired) {
            const dialogRef = this.dialog.open(ApplicationMessageDialog, {
                panelClass: 'custom-dialog-container',
                width: '40%',
                height: contHeight,
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
    openSnackBar(message: string, action: string) {
        let panelCls: string;
        if (action.toUpperCase() === 'SUCCESS') {
            if (message.length > 68) {
                panelCls = 'snackBar-style-success-2liner'
            } else if (message.length > 136) {
                panelCls = 'snackBar-style-success-3liner'
            } else {
                panelCls = 'snackBar-style-success'
            }
        }
        if (action.toUpperCase() === 'ERROR') {
            if (message.length > 68) {
                panelCls = 'snackBar-style-error-2liner'
            } else if (message.length > 136) {
                panelCls = 'snackBar-style-error-3liner'
            } else {
                panelCls = 'snackBar-style-error'
            }
        }
        this.snackBar.openFromComponent(SnackbarComponent, {
            data: message,
            duration: 20000,
            panelClass: panelCls,
            verticalPosition: 'bottom',
            horizontalPosition: 'center',
        });
    }

    public showConfirmationMessage(title, message, value, type, height, themeclass?): MatDialogRef<ConfirmationMessageDialog> {
        var isSessionExpired = this.dataShare.validateSession();
        if (!isSessionExpired) {
            const dialogRef = this.dialog.open(ConfirmationMessageDialog, {
                panelClass: 'custom-dialog-container',
                width: '50%',
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