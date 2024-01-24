/*******************************************************************************
 * Copyright 2020 Cognizant Technology Solutions
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
import { Component, OnInit, ElementRef, ViewChild, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { Router } from '@angular/router';
import { KpiService } from '../kpi-addition/kpi-service';
import { ContentService } from '../content-config-list/content-service';
import { OfflineService } from '@insights/app/modules/offline-data-processing/offline-service';
import { FormBuilder, FormControl, FormGroup,Validators,ReactiveFormsModule } from "@angular/forms";

@Component({
  selector: "app-file-upload",
  templateUrl: "./fileUploadDialog.component.html",
  styleUrls: ["./fileUploadDialog.component.scss", "./../home.module.scss"],
})
export class FileUploadDialog implements OnInit {
  @ViewChild("fileInput") myFileDiv: ElementRef;
  fileUploadErrorMessage: string;
  selectedFile: File = null;
  jsonError: any;
  fileUploadType = "";
  formDataFiles = new FormData();
  multipleFileAllowed = false;
  receivedData: any;
  dialogHeader: string;
  scriptReg: string = "<script>";
  validJson: boolean = true;
  validHtml: boolean = true;
  index: number = 0;
  fileNameArr = "";
  flag:boolean =false;
  options=[];
  filteredOptions = this.options;
  list:any;
  isDeleteKeyword:boolean =false;
  formGroup: FormGroup;
  queryGroup = "";

  constructor(
    public router: Router,
    public dialogRef: MatDialogRef<FileUploadDialog>,
    private restCallHandlerService: RestCallHandlerService,
    public dialog: MatDialog,
    public messageDialog: MessageDialogService,
    public kpiService: KpiService,
    public offlineService :OfflineService,
    public contentService: ContentService,
    public formBuilder: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.receivedData = data;
    if (data.type == "OFFLINE_DATA") {
      this.list = data.list;
      this.initForm();
      this.setQueryGroups();
    }
    this.dialogHeader = data.header;
    if (data.type != null && data.type != undefined) {
      this.fileUploadType = data.type;
      console.log(this.fileUploadType);
    }
    if (data.multipleFileAllowed != undefined) {
      this.multipleFileAllowed = data.multipleFileAllowed;
    }
    console.log(this.fileUploadType);
    console.log(" multipleFileAllowed " + this.multipleFileAllowed);
  }

  ngOnInit() {}

  onFileChanged(event) {
    console.log("Inside File Changed");
    this.formDataFiles = new FormData();
    console.log(event.target.files.length);
    if (this.multipleFileAllowed) {
      console.log("Inside Multiple File");
      for (let i = 0; i < event.target.files.length; i++) {
        console.log(event.target.files[i].name);
        this.formDataFiles.append(
          "files",
          <File>event.target.files[i],
          event.target.files[i].name
        );
        let reader = new FileReader();
        reader.onload = () => {
          if (event.target.files[i].type == "application/json") {
            this.validateJson(reader.result.toString());
          } else if (event.target.files[i].type == "text/html") {
            this.validateHtml(reader.result.toString());
          }
        };
        reader.readAsText(event.target.files[i]);
      }
    } else {
      let fileReader = new FileReader();
      this.formDataFiles.append(
        "file",
        <File>event.target.files[0],
        event.target.files[0].name
      );
      fileReader.onload = () => {
        this.validateJson(fileReader.result.toString());
        if (this.fileUploadType == "OFFLINE_DATA") {
          this.isDeleteKeyword = this.checkDeleteKeyword(fileReader.result.toString());
        }
      };
      fileReader.readAsText(event.target.files[0]);
    }
    this.fileNameArr = event.target.files[0].name;
    if (this.fileUploadType == "OFFLINE_DATA") {
      console.log("DELETE", this.isDeleteKeyword)
      const defaultQueryGRoup = this.fileNameArr.substring(0, this.fileNameArr.lastIndexOf('.'));
      this.formGroup.get('queryGroup').setValue(defaultQueryGRoup);
      this.flag = true;
    }
  }

  validateJson(message) {
    try {
      JSON.parse(message);
    } catch (e) {
      this.jsonError = e;
      this.validJson = false;
      return false;
    }
    return true;
  }

  validateHtml(message: string) {
    if (message.search(this.scriptReg) != -1) {
      this.validHtml = false;
      return false;
    }
  }
  

  initForm(){
this.formGroup=this.formBuilder.group({
queryGroup: [""]
})

this.formGroup.get('queryGroup').valueChanges.subscribe(response => {
  this.queryGroup=response;
this.filterData(response);
} )
  }

  filterData(enteredData){
this.filteredOptions=this.options.filter(item => {
  return item.toLowerCase().indexOf(enteredData.toLowerCase()) > -1
})
  }

  setQueryGroups(){
    if (this.list.data) {
      if (this.list.data.length > 0) {
        this.options = Array.from(new
          Set(this.list.data
            .filter(offlineData =>
              offlineData.queryGroup !== undefined && offlineData.queryGroup !== null).map(offlineData => offlineData.queryGroup)
            .filter(queryGroup => queryGroup.trim() !== ''))).sort();
        this.filteredOptions = this.options;
      }
    }
  }

  upload() {
    console.log("Inside Upload")
    if (this.flag && this.queryGroup.trim().length === 0) {
      this.messageDialog.openSnackBar(
        "Please fill Query Group field",
        "error"
      );
    } else if (this.flag && !this.validateQueryGroup(this.queryGroup)) {
      this.messageDialog.openSnackBar(
        "Please provide valid Query Group",
        "error"
      );
    }
    else if (this.validJson === false) {
      this.messageDialog.openSnackBar(
        "Uploaded file is not a valid JSON. " + this.jsonError,
        "error"
      );
    } else if (this.validHtml === false) {
      this.messageDialog.openSnackBar(
        "Attached HTML file contains script tag.",
        "error"
      );
    } else if (this.isDeleteKeyword === true) {
      this.alert();
    } else {
      if (this.fileUploadType === "CONTENT") {
        this.uploadFileContent();
      } else if (this.fileUploadType === "KPI") {
        this.uploadFileKpi();
      } else if (this.fileUploadType === "ATTACH_FILES") {
        this.uploadTemplateDesighFile();
      } else if (this.fileUploadType === "REPORT_TEMPLATE") {
        this.uploadReportTemplate();
      } else if (this.fileUploadType === "OFFLINE_DATA") {
        this.uploadFileOfflineData();
    }
  }
}
  uploadFileKpi() {
    var self = this;
    console.log(this.formDataFiles)
    if (this.formDataFiles.has("file")) {
      console.log("Inside Upload KPI");
      this.restCallHandlerService
        .postFormData("UPLOAD_BULK_KPI", this.formDataFiles)
        .toPromise()
        .then(function (response) {
          console.log(response);
          if (response.status === "success") {
            self.messageDialog.openSnackBar(response.data, "success");
            self.dialogRef.close();
            self.kpiService.fileUploadSubject.next("REFRESH");
          } else {
            self.messageDialog.openSnackBar("Error Creating KPI", "error");
          }
        });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please choose a valid JSON file to upload.",
        "WARN"
      );
    }
  }
  uploadFileOfflineData(){
    var self = this;
    if (this.formDataFiles.has("file")) {
      self.dialogRef.close(true);
      const fileExtension = this.fileNameArr.slice(this.fileNameArr.lastIndexOf("."));
      const modifiedFileName = this.formGroup.value['queryGroup'] + fileExtension;
      this.formDataFiles.set('file', this.formDataFiles.get('file'), modifiedFileName);
      this.restCallHandlerService
        .postFormData("UPLOAD_BULK_OFFLINE_DATA", this.formDataFiles)
        .toPromise()
        .then(function (response) {
          if (response.status === "success") {
            self.messageDialog.openSnackBar(response.data, "success");
            self.dialogRef.close(true);
          } else {
            self.messageDialog.openSnackBar("Failed to save offline data.", "error");
          }
        });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please choose a valid JSON file to upload.",
        "WARN"
      );
    }
  }
  uploadFileContent() {
    var self = this;
    if (this.formDataFiles.has("file")) {
      this.restCallHandlerService
        .postFormData("UPLOAD_BULK_CONTENT", this.formDataFiles)
        .toPromise()
        .then(function (response) {
          console.log(response);
          if (response.status === "success") {
            self.messageDialog.openSnackBar(
              response.data,
              "success"
            );
            self.dialogRef.close(true);
          } else {
            self.messageDialog.openSnackBar(
              "Error Creating Content.Please check logs",
              "error"
            );
          }
        });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please choose a valid JSON file to upload.",
        "WARN"
      );
    }
  }

  uploadTemplateDesighFile() {
    var self = this;
    if (this.formDataFiles.has("files")) {
      console.log(this.formDataFiles);
      this.restCallHandlerService
        .postFormDataWithParameter(
          "UPLOAD_REPORT_DESIGN_TEMPLATE",
          this.formDataFiles,
          {
            reportId: this.receivedData.reportId,
          }
        )
        .toPromise()
        .then(function (response) {
          if (response.status === "success") {
            self.messageDialog.openSnackBar(
              response.data,
              "success"
            );
            self.dialogRef.close(true);
          } else {
            self.messageDialog.openSnackBar(
              "Error uploading Report Template files",
              "error"
            );
          }
        });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please choose a file to upload.",
        "WARN"
      );
    }
  }

  uploadReportTemplate() {
    var self = this;
    if (this.formDataFiles.has("file")) {
      console.log(this.formDataFiles);
      this.restCallHandlerService
        .postFormData("UPLOAD_REPORT_TEMPLATE", this.formDataFiles)
        .toPromise()
        .then(function (response) {
          console.log(response);
          if (response.status === "success") {
            self.messageDialog.openSnackBar(
              response.data,
              "success"
            );
            self.dialogRef.close(true);
          } else {
            self.messageDialog.openSnackBar(
              "Failed to save report template. Please check logs for more details.",
              "error"
            );
          }
        });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please choose a valid JSON file to upload.",
        "WARN"
      );
    }
  }

  checkFile(sender, validExts) {
    if (sender) {
      var fileExt = sender.name;
      fileExt = fileExt.substring(fileExt.lastIndexOf("."));
      if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      } else return true;
    }
  }
  cancelFileUpload() {
    var dummy = <HTMLInputElement>document.getElementById("file");
    dummy.value = "";
    this.formDataFiles = new FormData();
    this.validJson = true;
    this.validHtml = true;
  }
  closeDialog() {
    this.dialogRef.close();
  }

  cancelUpload(index) {
    this.cancelFileUpload(); //Enters the cancelFile upload to remove image url and change if directive
    console.log("Inside cancelUpload");
    this.fileNameArr = null;
    if (this.fileUploadType === "OFFLINE_DATA") {
      this.flag = false;
      this.isDeleteKeyword = false;
      this.formGroup.get('queryGroup').setValue("");
    }
  }

  onFileChangedUpload(event, index) {
    console.log("Inside onFileChanged");
    this.selectedFile = <File>event.target.files[0];
    console.log(this.selectedFile);
    this.fileNameArr = this.selectedFile.name;
    this.onSelectFile(event);
  }

  onSelectFile(event) {
    console.log("Inside onSelectFile");
    if (event.target.files && event.target.files[0]) {
      var reader = new FileReader();
      reader.readAsDataURL(event.target.files[0]); // read file as data url
      reader.onload = (event: any) => {
        // called once readAsDataURL is completed Image
      };
    }
  }

  alert() {
    var self = this;
    var dialogmessage = "<b> Caution</b>: The file contains Cypher queries with the <b>'DELETE'</b> keyword. Uploading this file may lead to data deletion. Are you sure you want to continue? ";
    var title = "Delete Keyword in Uploaded File ";
    const dialogRef = this.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "",
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.uploadFileOfflineData();
      } else {
        self.dialogRef.close(true);
      }
    });
  }

  checkDeleteKeyword(jsonContent: string): boolean {
    try {
      const jsonData = JSON.parse(jsonContent);
      if (Array.isArray(jsonData)) {
        for (const item of jsonData) {
          if (item.cypherQuery && item.cypherQuery.toLowerCase().includes('delete')) {
            return true;
          }
        }
      }
    } catch (error) {
      console.error('Error parsing JSON:', error);
    }
    return false;
  }

  validateQueryGroup(queryGroup): boolean {
    // Regular expression pattern for numbers, alphabets, underscore, space or hyphen
    const pattern = /^(?=.*[a-zA-Z0-9_-])[\w\s-]+$/;
    return pattern.test(queryGroup);
  }
}
