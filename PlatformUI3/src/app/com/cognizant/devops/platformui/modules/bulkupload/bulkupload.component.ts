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
import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RelationLabel } from '@insights/app/modules/relationship-builder/relationship-builder.label';
import { from } from 'rxjs';
import { Router } from "@angular/router";
import { ActivatedRoute } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatTableDataSource } from '@angular/material';
import { DataSharedService } from '@insights/common/data-shared-service';
import { count } from 'rxjs/operators';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray, NgForm } from '@angular/forms'
import { BulkUploadService } from './bulkupload.service';
//import { Control} from '@angular/common';

@Component({
    selector: 'app-bulkupload',
    templateUrl: './bulkupload.component.html',
    styleUrls: ['./bulkupload.component.css', './../home.module.css']
})

export class BulkUploadComponent implements OnInit {

    rows: FormArray;
    toolsArr = [];
    toolVersionData: any;
    versionList = [];
    decsendinglist = [];
    constructor(private fb: FormBuilder, private router: Router, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService, private bulkuploadService: BulkUploadService) {

        this.rows = this.fb.array([]);
        for (let number of [1, 2, 3, 4, 5]) {

            this.rows.push(this.createItemFormGroup());
            //console.log(this.rows.value)
            //console.log(this.rows)
        }


    }

    ngOnInit() {
        this.getOsVersionTools();
    }
    onAddRow() {
        this.rows.push(this.createItemFormGroup());
    }
    createItemFormGroup(): FormGroup {

        return this.fb.group({
            toolName: null,
            fileName: null
        });
    }
    savedata() {
        console.log(this.rows.value)
    }

    async getOsVersionTools() {
        var self = this;

        self.toolsArr = [];
        this.toolVersionData = await this.bulkuploadService.getDocRootAgentVersionTools()
        console.log(this.toolVersionData.data)
        for (var value in this.toolVersionData.data) {
            this.versionList.push(value);
            this.decsendinglist = this.versionList.sort();
            this.decsendinglist = this.decsendinglist.reverse();
        }

        var version = this.decsendinglist[0];
        console.log(this.versionList)
        console.log(this.decsendinglist)
        console.log(version)
        this.toolsArr = this.toolVersionData.data[version];
        console.log(this.toolsArr)

    }


}