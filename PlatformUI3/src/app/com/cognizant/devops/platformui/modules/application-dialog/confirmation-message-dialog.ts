/*********************************************************************************
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
 *******************************************************************************/

import { Component, OnInit, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';

@Component({
    selector: 'confirmation-message-dialog',
    templateUrl: 'confirmation-message-dialog.html',
    styleUrls: ['./../home.module.css']
})

export class ConfirmationMessageDialog implements OnInit {
    colorCode: String;
    dialogTitle: String;
    svgCustomIcon: String;
    contentHeight: number;

    constructor(public dialogRef: MatDialogRef<ConfirmationMessageDialog>, @Inject(MAT_DIALOG_DATA) public data: any) {

        if (data.type == 'ALERT') {
            this.colorCode = "#000000";
            this.svgCustomIcon = "warning";
        }

        var receivedHeight = data.height.slice(0, -1);
        /* console.log(data.height + "  " + receivedHeight); */
        if (receivedHeight < 40) {
            this.contentHeight = 35;
        } else {
            this.contentHeight = 50;
        }
    }

    ngOnInit() {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }
    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }
}