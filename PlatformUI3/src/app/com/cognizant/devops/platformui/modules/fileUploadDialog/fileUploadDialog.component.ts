import { Component, OnInit, ElementRef, ViewChild, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { Router } from '@angular/router';
import { KpiService } from '../kpi-addition/kpi-service';
import { ContentService } from '../content-config-list/content-service';

@Component({
  selector: 'app-file-upload',
  templateUrl: './fileUploadDialog.component.html',
  styleUrls: ['./fileUploadDialog.component.css', './../home.module.css']
})
export class FileUploadDialog implements OnInit {
  @ViewChild('fileInput') myFileDiv: ElementRef;
  fileUploadErrorMessage: string;
  selectedFile: File = null;
  jsonError: any;
  valid: boolean;
  fileUploadType = '';
  formDataFiles = new FormData();
  multipleFileAllowed = false;
  receivedData: any;


  constructor(public router: Router, public dialogRef: MatDialogRef<FileUploadDialog>,
    private restCallHandlerService: RestCallHandlerService, public dialog: MatDialog,
    public messageDialog: MessageDialogService, public kpiService: KpiService,
    public contentService: ContentService, @Inject(MAT_DIALOG_DATA) public data: any) {
    this.receivedData = data;
    if (data.type != null && data.type != undefined) {
      this.fileUploadType = data.type;
    }
    if (data.multipleFileAllowed != undefined) {
      this.multipleFileAllowed = data.multipleFileAllowed;
    }
    console.log(this.fileUploadType);
    console.log(" multipleFileAllowed " + this.multipleFileAllowed);
  }

  ngOnInit() {
  }

  onFileChanged(event) {
    this.formDataFiles = new FormData();
    console.log(event.target.files.length);
    if (this.multipleFileAllowed) {
      for (let i = 0; i < event.target.files.length; i++) {
        console.log(event.target.files[i].name)
        this.formDataFiles.append('files', <File>event.target.files[i], event.target.files[i].name);
        if (event.target.files[i].type == 'application/json') {
          var reader = new FileReader();
          reader.onload = () => {
            this.valid = this.validateJson(reader.result.toString());
          }
          reader.readAsText(event.target.files[i]);
        }
      }
      //this.valid = true;
    } else {
      let fileReader = new FileReader();
      this.formDataFiles.append('file', <File>event.target.files[0], event.target.files[0].name);
      fileReader.onload = () => {
        this.valid = this.validateJson(fileReader.result.toString());
      };
      fileReader.readAsText(event.target.files[0]);
    }
  }

  validateJson(message) {
    try {
      JSON.parse(message);
    } catch (e) {
      this.jsonError = e;
      return false;
    }
    return true;
  }

  upload() {
    if (this.valid === false) {
      this.messageDialog.showApplicationsMessage("File is not a valid JSON. " + this.jsonError, 'ERROR');
    } else {
      if (this.fileUploadType === 'CONTENT') {
        this.uploadFileContent();
      } else if (this.fileUploadType === 'KPI') {
        this.uploadFileKpi();
      } else if (this.fileUploadType === "ATTACH_FILES") {
        this.uploadTemplateDesighFile();
      } else if (this.fileUploadType === "REPORT_TEMPLATE") {
        this.uploadReportTemplate();
      }
    }
  }
  uploadFileKpi() {
    var self = this;
    if (this.formDataFiles.has('file')) {
      this.restCallHandlerService.postFormData('UPLOAD_BULK_KPI', this.formDataFiles).toPromise().then(function (response) {
        if (response.status === "success") {
          self.messageDialog.showApplicationsMessage(response.data, "SUCCESS");
          self.dialogRef.close();
          self.kpiService.fileUploadSubject.next('REFRESH');
        } else {
          self.messageDialog.showApplicationsMessage("Error Creating KPI", "ERROR");
        }

      })
    }
    else {
      self.messageDialog.showApplicationsMessage("Please choose a valid JSON file to upload.", "WARN");
    }
  }
  uploadFileContent() {
    var self = this;
    if (this.formDataFiles.has('file')) {
      this.restCallHandlerService.postFormData('UPLOAD_BULK_CONTENT', this.formDataFiles).toPromise().then(function (response) {
        if (response.status === "success") {
          self.messageDialog.showApplicationsMessage(response.data, "SUCCESS");
          self.dialogRef.close(true);
        }
        else {
          self.messageDialog.showApplicationsMessage("Error Creating Content.Please check logs", "ERROR");
        }
      })
    } else {
      self.messageDialog.showApplicationsMessage("Please choose a valid JSON file to upload.", "WARN");
    }
  }

  uploadTemplateDesighFile() {
    var self = this;
    if (this.formDataFiles.has('files')) {
      console.log(this.formDataFiles);
      this.restCallHandlerService.postFormDataWithParameter('UPLOAD_REPORT_DESIGN_TEMPLATE', this.formDataFiles, {
        reportId: this.receivedData.reportId
      }).toPromise().then(function (response) {
        if (response.status === "success") {
          self.messageDialog.showApplicationsMessage(response.data, "SUCCESS");
          self.dialogRef.close(true);
        }
        else {
          self.messageDialog.showApplicationsMessage("Error uploading Report Template files", "ERROR");
        }
      })
    }
    else {
      self.messageDialog.showApplicationsMessage("Please choose a file to upload.", "WARN");
    }
  }

  uploadReportTemplate() {
    var self = this;
    if (this.formDataFiles.has('file')) {
      console.log(this.formDataFiles)
      this.restCallHandlerService.postFormData('UPLOAD_REPORT_TEMPLATE', this.formDataFiles).toPromise().then(function (response) {
        if (response.status === "success") {
          self.messageDialog.showApplicationsMessage(response.data, "SUCCESS");
          self.dialogRef.close(true);
        }
        else {
          self.messageDialog.showApplicationsMessage("Failed to save report template. Please check logs for more details.", "ERROR");
        }
      })
    } else {
      self.messageDialog.showApplicationsMessage("Please choose a valid JSON file to upload.", "WARN");
    }
  }

  checkFile(sender, validExts) {
    if (sender) {
      var fileExt = sender.name;
      fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
      if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
        return false;
      }
      else return true;
    }
  }
  cancelFileUpload() {
    var dummy = (<HTMLInputElement>document.getElementById("file"))
    dummy.value = "";
    this.formDataFiles = new FormData();
    this.valid = true;
  }
  closeDialog() {
    this.dialogRef.close();
  }


}
