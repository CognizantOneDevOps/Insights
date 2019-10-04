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
    selector: 'add-group-message-dialog',
    templateUrl: 'add-group-message-dialog.html',
    styleUrls: ['./../home.module.css', './user-onboarding.component.css']
})

export class AddGroupMessageDialog implements OnInit {
    accessGroupName: String;
    message: string = "";
    constructor(public dialogRef: MatDialogRef<AddGroupMessageDialog>, @Inject(MAT_DIALOG_DATA) public data: any) {
    }

    ngOnInit() {
    }

    onNoClick(): void {
        this.dialogRef.close();
    }

    onYesClick() {
        if (this.accessGroupName != undefined) {
            this.dialogRef.close(this.accessGroupName)
        } else {
            this.message = "Please enter valid access Group Name";
        }
    }
    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }
}