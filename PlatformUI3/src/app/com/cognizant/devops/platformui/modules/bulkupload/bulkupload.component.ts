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
import { MatAutocompleteModule, MatInputModule, MatProgressBarModule } from '@angular/material';
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
    labelsArr = [];
    toolsDetail = [];
    toolVersionData: any;
    versionList = [];
    decsendinglist = [];
    selectedFile: File = null;
    toolNameSaveEnable: boolean = false;
    fileNameSaveEnable: boolean = false;
    refresh: boolean = false;
    selectedTool = [];
    selectedLabel = [];
    lableName = [];
    toolTipMessage: string = "";
    labelName: any;
    uploadForm: FormGroup;
    successIconEnable: boolean = false;
    failIconEnable: boolean = false;

    dataarr = []
    constructor(private fb: FormBuilder, private router: Router, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService, private bulkuploadService: BulkUploadService) {

        this.rows = this.fb.array([]);
        for (let number of [1, 2, 3, 4, 5]) {
            this.rows.push(this.createItemFormGroup());

        }
    }
    ngOnInit() {
        this.getLabelTools();
    }
    onAddRow() {

        this.rows.push(this.createItemFormGroup());
        console.log(this.rows.value)

    }
    createItemFormGroup(): FormGroup {
        const fileFormData = new FormData();
        return this.fb.group({
            toolName: null,
            labelName: null,
            fileName: null,
            fileFormData: null,
            status: null
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
                //  console.log(labelName)
                this.toolsArr.push(toolName);
            }
        }
        catch (error) {
            //  console.log(error);
        }
    }

    onFileChanged(event, row) {
        this.selectedFile = <File>event.target.files[0];
        row.value.fileFormData = <File>event.target.files[0];
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
    onToolSelect(toolname, index, row): void {
        //console.log(row.value)
        for (let element of this.rows.value) {
            // console.log(element);
        }
        var self = this;
        if (toolname === undefined) {
        }
        else {
            var i = 0;
            // for (let key of this.toolsDetail) {

            //  console.log(key)
            //  console.log(this.toolsArr)
            // if (key.toolName = toolname) {
            var labelnameIndex = this.toolsArr.indexOf(toolname)
            console.log(labelnameIndex)

            this.labelsArr[index] = this.toolsDetail[labelnameIndex].label;
            row.value.labelName = this.toolsDetail[labelnameIndex].label;
            // i = i + 1;
            //  }

            // }
            //this.labelsArr.push(this.toolsDetail[labelnameIndex].label);
            console.log(this.labelsArr)

        }
        //console.log(row.value)

    }
    async saveData() {

        var rowcount = 0;
        var successCount = 0;
        var numberOfValidEntries = 0;
        console.log(this.fileNameSaveEnable)
        console.log(this.rows.value);
        for (let element of this.rows.value) {
            // this.toolTipMessage = "";
            console.log(element);
            var fd = new FormData();
            var toolName = (element.toolName);
            var labelName = (element.labelName);
            var fileName = element.fileName;
            // console.log(element.fileFormData)
            if (toolName != null && labelName != null && element.fileFormData) {
                if (element.status == "Success") {
                    continue;
                }


                numberOfValidEntries = numberOfValidEntries + 1;
                var bytes = element.fileFormData["size"];
                var testFileExt = this.checkFile(element.fileFormData, ".csv");
                element.status = 'Pending';
                var fileData = element.fileFormData;
                if ((toolName == null)) {
                    if (element.fileName == null) {

                        rowcount = 0
                        break;
                    }
                    else {
                        // console.log(element.toolName)
                        rowcount = rowcount + 1;
                        break;
                    }

                }
                else if (element.fileName == null) {
                    // console.log(element.toolName)
                    rowcount = rowcount + 1;
                    break;

                }

                if (rowcount == 0) {

                    if (bytes > 2097152) {
                        // this.size = true
                        // this.messageDialog.showApplicationsMessage("Please select a of file size less than 2MB.", "ERROR");
                        element.status = 'Fail';
                        this.toolTipMessage = "File Size greater than 2 MB.";
                        console.log(this.toolTipMessage);
                        break;
                    } else if (!testFileExt) {
                        // this.messageDialog.showApplicationsMessage("Please select a valid .CSV file", "ERROR");
                        element.status = 'Fail'
                        this.toolTipMessage = "Incorrect file format.";
                        break;
                    }
                    else {
                        fd.append('file', fileData, fileData.name);
                        //   console.log(this.selectedFile)
                        //  console.log(this.selectedFile.name)
                        setTimeout(() => {
                            //self.showThrobber = false;
                            //self.router.navigate(['/InSights/Home']);
                            ''
                        }, 2000);


                        let upload = await this.bulkuploadService.uploadFile(fd, toolName, labelName);
                        console.log(upload)
                        this.toolTipMessage = upload.message;
                        if (upload.status == 'success') {
                            element.status = 'Success'
                            successCount = successCount + 1;

                            // this.successIconEnable = true;

                        }
                        else {
                            element.status = 'Fail'
                            //  this.failIconEnable = true;
                            break;
                        }
                    }

                }
                else {
                    this.messageDialog.showApplicationsMessage("Please select file", "ERROR");
                }
            }
            else {
                break;
            }
        }
        if (successCount == numberOfValidEntries) {
            this.messageDialog.showApplicationsMessage("You have successfully uploaded the file to Neo4J", "SUCCESS");
        }

    }

    checkFile(sender, validExts) {
        if (sender) {
            var fileExt = sender.name;
            fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
            fileExt = fileExt.toLowerCase();
            if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
                return false;
            } else {
                return true;
            }
        }
    }

}