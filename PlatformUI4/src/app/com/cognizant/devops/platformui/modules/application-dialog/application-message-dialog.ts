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
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router, NavigationExtras } from '@angular/router';
import { DataSharedService } from '@insights/common/data-shared-service';

@Component({
    selector: 'application-message-dialog',
    templateUrl: 'application-message-dialog.html',
    styleUrls: ['./../home.module.scss']
})

export class ApplicationMessageDialog implements OnInit {
    colorCode: String;
    dialogTitle: String;
    svgCustomIcon: String;
    isRedirect: boolean = false;

    constructor(public dialogRef: MatDialogRef<ApplicationMessageDialog>, @Inject(MAT_DIALOG_DATA) public data: any, public router: Router, private dataShare: DataSharedService,
    ) {

        if (data.values == true) {
            this.isRedirect = true;

        }
        if (data.type == 'SUCCESS') {
            this.colorCode = "#000000";
            this.dialogTitle = "Success";
            this.svgCustomIcon = "success";
        } else if (data.type == 'WARN') {
            this.colorCode = "#000000"
            this.dialogTitle = "Warning";
            this.svgCustomIcon = "warning";
        } else if (data.type == 'ERROR') {
            this.colorCode = "#000000";
            this.dialogTitle = "Error";
            this.svgCustomIcon = "error";
        } else if (data.type == 'INFO') {
            this.colorCode = "#000000";
            this.dialogTitle = "Info";
            this.svgCustomIcon = "success";
        }
    }

    ngOnInit() {
    }

    onCloseClick(): void {
        this.dialogRef.close();
    }

    closeShowDetailsDialog(): void {
        this.dialogRef.close();
    }
    onOkClick(): void {
        this.dialogRef.close()
        this.router.navigateByUrl('/login');

    }
}