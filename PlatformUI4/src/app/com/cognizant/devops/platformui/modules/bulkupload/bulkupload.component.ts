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
import { Component, OnInit, ElementRef, ViewChild } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { Router } from "@angular/router";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import {
  FormGroup,
  FormBuilder,
  Validators,
  FormControl,
  FormArray,
  NgForm,
} from "@angular/forms";
import { BulkUploadService } from "./bulkupload.service";

@Component({
  selector: "app-bulkupload",
  templateUrl: "./bulkupload.component.html",
  styleUrls: ["./bulkupload.component.scss", "./../home.module.scss"],
})
export class BulkUploadComponent implements OnInit {
  rows: FormArray;
  toolsArr = [];
  labelsArr = [];
  fileNameArr = [];
  fileFormDataArr = [];
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
  showConfirmationPopup: boolean = true;
  InsightsTimeField: any;
  InsightsTimeFormat: any;
  dataarr = [];
  fileName: string;
  @ViewChild('fileInput') myFileDiv: ElementRef;
  constructor(
    private fb: FormBuilder,
    private router: Router,
    private dialog: MatDialog,
    public messageDialog: MessageDialogService,
    private dataShare: DataSharedService,
    private bulkuploadService: BulkUploadService
  ) {
    this.userForm();
    this.rows = this.fb.array([]);
    for (let number of [1, 2, 3, 4, 5]) {
      this.rows.push(this.createItemFormGroup());
    }
  }
  ngOnInit() {
    this.getLabelTools();
  }
  cancelUpload(index) {
    this.fileNameArr[index] = null;
    (<HTMLInputElement>document.getElementById("file" + index)).value = '';
  }

  cancelAllUpload() {
    for (let index of [0, 1, 2, 3, 4]) {
      this.fileNameArr[index] = null;
      (<HTMLInputElement>document.getElementById("file" + index)).value = '';
    }
  }

  userForm() {
    this.rows = this.fb.array([]);
    for (let number of [1, 2, 3, 4, 5]) {
      this.rows.push(this.createItemFormGroup());
    }
  }
  onAddRow() {
    this.rows.push(this.createItemFormGroup());
  }
  createItemFormGroup(): FormGroup {
    const fileFormData = new FormData();
    return this.fb.group({
      toolName: new FormControl("", Validators.required),
      labelName: new FormControl("", Validators.required),
      InsightsTimeField: new FormControl("", Validators.required),
      InsightsTimeFormat: new FormControl("", Validators.required),
      fileName: new FormControl("", Validators.required),
      fileFormData: new FormControl("", Validators.required),
      status: new FormControl("", Validators.required),
      tooltipmessage: new FormControl("", Validators.required),
    });
  }
  async getLabelTools() {
    var self = this;
    try {
      self.toolsDetail = [];
      let toollabelresponse = await this.bulkuploadService.loadUiServiceLocation();
      if (toollabelresponse.status == "success") {
        this.toolsDetail = toollabelresponse.data;
        for (var element of this.toolsDetail) {
          var toolName = element.toolName;
          var labelName = element.label;
          this.toolsArr.push(toolName);
        }
      }
      else {
        this.messageDialog.openSnackBar(toollabelresponse.message, "error");
      }
    } catch (error) {
      //  console.log(error);
    }
  }
  onFileChanged(event, row, index) {
    this.selectedFile = <File>event.target.files[0];
    row.value.fileFormData = <File>event.target.files[0];
    this.fileNameArr[index] = row.value.fileFormData.name;
    this.fileFormDataArr[index] = row.value.fileFormData;
    //row.value.fileFormData.fileName = this.selectedFile.name;
    row.value.fileName = this.selectedFile.name;
    this.rows.value[index] = row.value;
    this.rows.value[index].fileName = this.selectedFile.name;
    this.rows.value[index].fileFormData = row.value.fileFormData;
    this.rows.value[index].status = null;
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
    this.InsightsTimeField = "";
    this.InsightsTimeFormat = "";
    this.userForm();
    this.cancelAllUpload();
    this.refresh = false;
    var index = 0;
  }
  uploadFile() {
    this.toolNameSaveEnable = true;
  }
  onToolSelect(toolname, index, row): void {
    var self = this;
    if (toolname === undefined) {
    } else {
      var i = 0;
      var labelnameIndex = this.toolsArr.indexOf(toolname);
      this.labelsArr[index] = this.toolsDetail[labelnameIndex].label;
      row.value.labelName = this.toolsDetail[labelnameIndex].label;
    }
  }
  validation() {
    this.showConfirmationPopup = true;
    var labelRegex = /^[a-zA-Z0-9:_]+$/;
    var i = 0;
    for (let element of this.rows.value) {

      var message = null;
      var fd = new FormData();
      var toolName = element.toolName;
      var labelName = element.labelName;
      var InsightsTimeField = element.InsightsTimeField;
      var InsightsTimeFormat = element.InsightsTimeFormat;
      var fileFormData = element.fileFormData;
      var fileName = element.fileName;
      if (fileName == '') {
        fileName = this.fileNameArr[i];
        element.fileName = fileName;
      } if (fileFormData == '') {
        fileFormData = this.fileFormDataArr[i];
        element.fileFormData = fileFormData;
      }
      i++;
      if (
        !toolName &&
        !labelName &&
        !InsightsTimeField &&
        !InsightsTimeFormat &&
        !fileFormData &&
        !fileName
      ) {
        continue;
      } else {
        if (!element.toolName) {
          element.status = "Fail";
          message = "No tool selected ";
          element.tooltipmessage =
            "No tool selected. Please select the file again.";
        } else if (!element.fileName) {
          element.status = "Fail";
          message = message + "No File Selected";
          element.tooltipmessage =
            "No File Selected. Please select the file again.";
          this.showConfirmationPopup = false;
        } else if (!labelRegex.test(element.labelName)) {
          element.status = "Fail";
          message =
            message +
            "Label name should only contain alphanumeric,colon and underscore";
          element.tooltipmessage =
            "Label name should only contain alphanumeric,colon and underscore. Please select the file again.";
          this.showConfirmationPopup = false;
        } else if (!element.InsightsTimeField) {
          element.status = "Fail";
          message = message + "Insight Time Field cannot be empty.";
          element.tooltipmessage =
            "Insight Time Field cannot be empty. Please select the file again.";
          this.showConfirmationPopup = false;
        } else if (element.toolName != null && element.fileName != null) {
          var bytes = element.fileFormData["size"];
          var testFileExt = this.checkFile(element.fileFormData, ".csv");
          if (bytes > 2097152) {
            element.status = "Fail";
            element.tooltipmessage = "File Size greater than 2 MB.";
            this.toolTipMessage =
              "File Size greater than 2 MB. Please select the file again.";
            this.showConfirmationPopup = false;
          } else if (!testFileExt) {
            element.status = "Fail";
            element.tooltipmessage = "Incorrect file format.";
            this.toolTipMessage =
              "Incorrect file format. Please select the file again.";
            this.showConfirmationPopup = false;
          }
        }
      }
      if (message != null) {
        this.showConfirmationPopup = false;
        element.toolTipMessage = message;
      }
    }
    if (this.showConfirmationPopup) {
      this.saveData();
    }
  }
  async saveData() {
    var title = "Upload the Data";
    var dialogmessage =
      "You are uploading file(s) to Neo4j. Please ensure the .csv file(s) are in correct format and contain unique data to avoid duplication of data.For more information you may click on the help(?) icon.Do you want to proceed ?";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "32%"
    );
    dialogRef.afterClosed().subscribe(async (result) => {
      if (result == "yes") {
        var failcount = 0;
        var rowcount = 0;
        var successCount = 0;
        var successtool = "";
        var numberOfValidEntries = 0;
        var i = 0;
        for (let element of this.rows.value) {
          var fd = new FormData();
          var toolName = element.toolName;
          var labelName = element.labelName;
          var InsightsTimeField = element.InsightsTimeField;
          var InsightsTimeFormat = element.InsightsTimeFormat;
          var dt = (<HTMLInputElement>document.getElementById("file" + i));
          var fileName = dt.value;
          if (
            toolName != null &&
            labelName != null &&
            element.fileFormData != null &&
            dt.value != null
          ) {
            if (element.status == "Success") {
              continue;
            }
            numberOfValidEntries = numberOfValidEntries + 1;
            var bytes = element.fileFormData["size"];
            var testFileExt = this.checkFile(element.fileFormData, ".csv");
            element.status = "Pending";
            var fileData = element.fileFormData;
            if (toolName == null) {
              if (element.fileData == null) {
                rowcount = 0;
                break;
              }
            } else if (dt.value == null) {
              rowcount = rowcount + 1;
              break;
            }
            if (rowcount == 0) {
              if (bytes > 2097152) {
                element.status = "Fail";
                failcount = failcount + 1;
                element.tooltipmessage = "File Size greater than 2 MB.";
                this.toolTipMessage = "File Size greater than 2 MB.";
              } else if (!testFileExt) {
                element.status = "Fail";
                failcount = failcount + 1;
                element.tooltipmessage = "Incorrect file format.";
                this.toolTipMessage = "Incorrect file format.";
              } else {
                fd.append("file", fileData, fileData.name);
                setTimeout(() => {
                  "";
                }, 2000);
                var finalStatus;
                let upload = await this.bulkuploadService.uploadFile(
                  fd,
                  toolName,
                  labelName,
                  InsightsTimeField,
                  InsightsTimeFormat
                );
                finalStatus = upload.status;
                if (finalStatus == "success") {
                  element.status = "Success";
                  element.tooltipmessage = "success";
                  successCount = successCount + 1;
                  successtool = successtool + element.toolName + ", ";
                } else {
                  element.status = "Fail";
                  failcount = failcount + 1;
                  this.toolTipMessage = upload.message;
                  element.tooltipmessage =
                    this.toolTipMessage + ". Please select the file again.";
                }
              }
            } else {
              failcount = failcount + 1;
              this.toolTipMessage = "No File Selected";
              element.tooltipmessage = "No File Selected";
              element.status == "Fail";
            }
          } else if (
            toolName != null &&
            labelName != null &&
            element.fileFormData == null &&
            dt.value == null
          ) {
            numberOfValidEntries = numberOfValidEntries - 1;
            element.status = "Fail";
            failcount = failcount + 1;
            this.toolTipMessage = "No File Selected";
            element.tooltipmessage = this.toolTipMessage;
          } else {
            break;
          }
          this.cancelUpload(i);
          i = i + 1;
        }
        if (
          successCount == numberOfValidEntries &&
          failcount == 0 &&
          successCount != 0
        ) {
          successtool = successtool.slice(0, -2);
          this.messageDialog.openSnackBar("You have successfully uploaded the file to Neo4J for <b>" + successtool + "</b>", "success");
        } else if (
          successCount == numberOfValidEntries &&
          failcount == 0 &&
          successCount == 0
        ) {
          this.messageDialog.openSnackBar("<b> Something went wrong in selecting file</b>", "error");
        } else {
          var errorMessage =
            "Failed to Upload the Data for some files.Please click on the failure icon for more details.";
          this.messageDialog.openSnackBar(errorMessage, "error");
        }
      }
    });
  }
  checkFile(sender, validExts) {
    if (sender) {
      var fileExt = sender.name;
      fileExt = fileExt.substring(fileExt.lastIndexOf("."));
      fileExt = fileExt.toLowerCase();
      if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      } else {
        return true;
      }
    }
  }

}
