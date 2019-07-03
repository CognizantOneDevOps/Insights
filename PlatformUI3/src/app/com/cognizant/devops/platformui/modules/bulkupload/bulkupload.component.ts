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
import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
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
    @ViewChild('myInput')
    myFileDiv: ElementRef;

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
    fileToBeUploaded: FormData;
    lableName = [];
    questionmark: string = "";
    toolTipMessage: string = "";
    labelName: any;
    uploadForm: FormGroup;
    successIconEnable: boolean = false;
    failIconEnable: boolean = false;

    dataarr = []
    constructor(private fb: FormBuilder, private router: Router, private dialog: MatDialog, public messageDialog: MessageDialogService, private dataShare: DataSharedService, private bulkuploadService: BulkUploadService) {
        this.userForm();
        //  this.questionmark = "Please ensure that the .CSV file is in correct format to avoid failure in uploading the file. For example" + '<br> <b>' + "In the header do not use quotes.+In textual data do not use spaces.E.g.run time, instead use run_time"


        this.rows = this.fb.array([]);
        for (let number of [1, 2, 3, 4, 5]) {

            this.rows.push(this.createItemFormGroup());

        }
    }
    ngOnInit() {
        this.getLabelTools();
    }
    userForm() {
        this.rows = this.fb.array([]);
        for (let number of [1, 2, 3, 4, 5]) {

            this.rows.push(this.createItemFormGroup());
            // console.log(this.rowcss)
        }
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
            status: null,
            tooltipmessage: null
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
        console.log(this.selectedFile.name)
        row.value.fileFormData = <File>event.target.files[0];

    }
    toolNameenableSave() {
        this.toolNameSaveEnable = true;
    }
    fileNameenableSave() {
        this.fileNameSaveEnable = true;
    }

    Refresh() {

        this.toolNameSaveEnable = false;
        this.refresh = false;
        this.selectedTool = [];
        this.labelsArr = [];
        this.userForm();
        this.refresh = false;
        // document.getElementById("uploadCaptureInputFile").value = "";

        // this.myFileDiv.nativeElement.value = "";
        //nativeElement.value = "";
        this.myFileDiv.nativeElement.disabled = true;
        this.myFileDiv.nativeElement.disabled;

        //  console.log(this.selectedTool)
        /*  this.selectedTool = [];
         this.labelsArr = [];s
         this.fileToBeUploaded = null; */
        // this.Refresh.
        // this.rows.push(this.createItemFormGroup());
        console.log(this.rows.value)
        var index = 0;


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

        var title = "Upload the Data";
        var dialogmessage = "You are uploading file(s) to Neo4j. Please ensure the .csv file(s) are in correct format and contain unique data to avoid duplication of data.For more information you may click on the help(?) icon.Do you want to proceed ?";

        const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");

        dialogRef.afterClosed().subscribe(async result => {
            if (result == 'yes') {

                var failcount = 0;
                var rowcount = 0;
                var successCount = 0;
                var numberOfValidEntries = 0;
                //  console.log(this.fileNameSaveEnable)
                //  console.log(this.rows.value);
                for (let element of this.rows.value) {
                    // this.toolTipMessage = "";
                    console.log(element);
                    var fd = new FormData();
                    var toolName = (element.toolName);
                    var labelName = (element.labelName);
                    var fileName = element.fileName;
                    // console.log(element.fileFormData)
                    if (toolName != null && labelName != null && element.fileFormData != null && element.fileName != null) {
                        if (element.status == "Success") {
                            console.log("In Continue loop")
                            continue;
                        }
                        numberOfValidEntries = numberOfValidEntries + 1;
                        var bytes = element.fileFormData["size"];
                        var testFileExt = this.checkFile(element.fileFormData, ".csv");
                        element.status = 'Pending';
                        var fileData = element.fileFormData;
                        if ((toolName == null)) {
                            if (element.fileData == null) {
                                rowcount = 0
                                console.log("All Nulll")
                                break;
                            }
                            /* else {
                                // console.log(element.toolName)
                                rowcount = rowcount + 1;
                                break;
                            } */

                        }
                        else if (element.fileName == null) {
                            // console.log(element.toolName)
                            rowcount = rowcount + 1;
                            console.log("No fILe present")
                            break;

                        }



                        if (rowcount == 0) {

                            if (bytes > 2097152) {
                                // this.size = true
                                // this.messageDialog.showApplicationsMessage("Please select a of file size less than 2MB.", "ERROR");
                                element.status = 'Fail';
                                failcount = failcount + 1;
                                element.tooltipmessage = "File Size greater than 2 MB."
                                this.toolTipMessage = "File Size greater than 2 MB.";
                                console.log(this.toolTipMessage);
                                // break;
                            } else if (!testFileExt) {
                                // this.messageDialog.showApplicationsMessage("Please select a valid .CSV file", "ERROR");
                                element.status = 'Fail'
                                failcount = failcount + 1;
                                element.tooltipmessage = "Incorrect file format.";
                                this.toolTipMessage = "Incorrect file format.";
                                console.log("Incorrect file format.")
                                // break;
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
                                // console.log(toolName);
                                var finalStatus
                                let upload = await this.bulkuploadService.uploadFile(fd, toolName, labelName);
                                // const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "30%");
                                /* this.bulkuploadService.uploadFile(fd, toolName, labelName).then(
                                    (upload) => { */
                                console.log("Service check")
                                console.log(successCount)
                                // let upload2 = await this.bulkuploadService.uploadFile(fd, toolName, labelName);
                                console.log(upload)
                                //  this.toolTipMessage = upload.message;
                                finalStatus = upload.status

                                // console.log(this.toolTipMessage)
                                if (finalStatus == 'success') {
                                    element.status = 'Success'
                                    element.tooltipmessage = "success"
                                    console.log("Success upload done")
                                    //this.myFileDiv.nativeElement.disabled = true;
                                    //  console.log(this.myFileDiv.nativeElement.disabled)
                                    successCount = successCount + 1;
                                    console.log(successCount)

                                    // this.successIconEnable = true;

                                }
                                else {
                                    element.status = 'Fail'
                                    failcount = failcount + 1;

                                    // var errorMessage = "Something went wrong in uploading the file, " + element.fileFormData.name + ". Please check the format and try again."
                                    //  var errorMessage = "Failed to Upload the Data.Please click on the icon for more details."
                                    // this.messageDialog.showApplicationsMessage(errorMessage, "ERROR");
                                    //  this.failIconEnable = true;
                                    this.toolTipMessage = upload.message
                                    console.log(this.toolTipMessage)
                                    element.tooltipmessage = this.toolTipMessage


                                    // if (this.toolTipMessage == "Error in file format")

                                    console.log("Something is wrong...failed")                                       //break;
                                }
                                //  });

                            }

                        }
                        else {
                            // this.messageDialog.showApplicationsMessage("Please select a file.", "ERROR");
                            failcount = failcount + 1;
                            this.toolTipMessage = "No File Selected"
                            element.tooltipmessage = "No File Selected"
                            element.status == 'Fail';
                            console.log("Problem in file select")
                        }
                    }
                    else if (toolName != null && labelName != null && element.fileFormData == null && element.fileName == null) {
                        numberOfValidEntries = numberOfValidEntries - 1;
                        element.status == 'Fail';
                        failcount = failcount + 1;
                        //  var messageinPopUp = "Please select a File for " + element.toolName;
                        this.toolTipMessage = "No File Selected"
                        element.tooltipmessage = "No File Selected"
                        console.log(".......some uploadding fole miss")
                        // this.messageDialog.showApplicationsMessage(messageinPopUp, "ERROR");
                        //  break;
                    }
                    else {
                        break;
                    }
                    /* if (element.status == 'Fail' && failcount != 0) {
                        console.log("failing")
                        failcount = failcount + 1;
                        break;
                    } */

                }


                console.log(successCount)
                console.log(numberOfValidEntries)
                console.log(failcount)

                if (successCount == numberOfValidEntries && failcount == 0) {
                    this.messageDialog.showApplicationsMessage("You have successfully uploaded the file to Neo4J", "SUCCESS");
                }
                else {

                    var errorMessage = "Failed to Upload the Data for some files.Please click on the failure icon for more details."
                    this.messageDialog.showApplicationsMessage(errorMessage, "ERROR");
                }

            }
        });

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