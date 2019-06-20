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
import { MatAutocompleteModule, MatInputModule } from '@angular/material';
import { element } from '../../../../../../../../node_modules/@angular/core/src/render3/instructions';
//import { Control} from '@angular/common';

@Component({
    selector: 'app-bulkupload',
    templateUrl: './bulkupload.component.html',
    styleUrls: ['./bulkupload.component.css', './../home.module.css']
})

export class BulkUploadComponent implements OnInit {

    rows: FormArray;
    toolsArr = [];
    labelsArr: any;
    toolsDetail = [];
    toolVersionData: any;
    versionList = [];
    decsendinglist = [];
    selectedFile: File = null;
    toolNameSaveEnable: boolean = false;
    fileNameSaveEnable: boolean = false;
    refresh: boolean = false;
    selectedTool = [];
    lableName = [];
    labelName: any;

    dataarr = []
    constructor(private fb: FormBuilder, private router: Router, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService, private bulkuploadService: BulkUploadService) {

        this.rows = this.fb.array([]);
        for (let number of [1]) {
            this.rows.push(this.createItemFormGroup());
        }
    }
    ngOnInit() {
        this.getLabelTools();
    }
    onAddRow() {

        this.rows.push(this.createItemFormGroup());

    }
    createItemFormGroup(): FormGroup {

        return this.fb.group({
            toolName: null,
            labelName: null,
            fileName: null
        });
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
                console.log(labelName)
                this.toolsArr.push(toolName);
            }
        }
        catch (error) {
            console.log(error);
        }
    }

    onFileChanged(event) {
        this.selectedFile = <File>event.target.files[0];
    }
    toolNameenableSave() {
        this.toolNameSaveEnable = true;
    }
    fileNameenableSave() {
        this.fileNameSaveEnable = true;
    }

    Refresh() {

        this.toolNameSaveEnable = true;
        this.refresh = false;
    }
    uploadFile() {
        this.toolNameSaveEnable = true;
    }
    onToolSelect(toolname): void {
        var self = this;
        if (toolname === undefined) {
        }
        else {
            for (let key of this.toolsDetail) {
                console.log(key)
                console.log(this.toolsArr)
                if (key.toolName = toolname) {
                    var labelname = this.toolsArr.indexOf(toolname)
                    console.log(labelname)
                    this.labelsArr = this.toolsDetail[labelname].label;
                    console.log(this.labelsArr)
                }

            }

        }


    }
    async saveData() {

        var rowcount = 0;
        const fd = new FormData();
        for (let element of this.rows.value) {
            var toolName = (element.toolName);
            var labelName = (element.labelName);
            var fileName = element.fileName;
            if ((toolName == null)) {
                if (element.fileName == null) {
                    rowcount = 0
                    break;
                }
                else {
                    console.log(element.toolName)
                    rowcount = rowcount + 1;
                    break;
                }

            }
            else if (element.fileName == null) {
                console.log(element.toolName)
                rowcount = rowcount + 1;
                break;

            }
        }
        if (rowcount == 0) {

            fd.append('file', this.selectedFile, this.selectedFile.name);
            console.log(this.selectedFile)
            console.log(this.selectedFile.name)
            let upload = await this.bulkuploadService.uploadFile(fd, toolName, labelName);
            this.messageDialog.showApplicationsMessage("You have successfully uploaded the file to Neo4J", "SUCCESS");
        }
        else {
            this.messageDialog.showApplicationsMessage("Please select file", "ERROR");
        }
    }

}