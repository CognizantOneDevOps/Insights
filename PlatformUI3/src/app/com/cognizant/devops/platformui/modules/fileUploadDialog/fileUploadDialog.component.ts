import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
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


  constructor(public router: Router, public dialogRef: MatDialogRef<FileUploadDialog>, private restCallHandlerService: RestCallHandlerService,
    public dialog: MatDialog, public messageDialog: MessageDialogService, public kpiService: KpiService, public contentService: ContentService) { }

  ngOnInit() {
  }

  onFileChanged(event) {
    
    let fileReader = new FileReader();
    this.selectedFile = <File>event.target.files[0];
    fileReader.onload = () => {
      this.valid = this.validateJson(fileReader.result.toString());
    };
    fileReader.readAsText(event.target.files[0]);
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
      this.messageDialog.showApplicationsMessage("File is not a valid JSON <br><br>" + this.jsonError, 'ERROR');
    } else {
      if (this.contentService.getFileType() === 'CONTENT') {
        this.uploadFileContent();
      }
      else {
        this.uploadFileKpi();
      }
    }
  }
  uploadFileKpi() {
    var self = this;
    if (this.selectedFile) {
      var uploadedFile = this.selectedFile;
      console.log(uploadedFile);
      let fd = new FormData();
      fd.append('file', uploadedFile, uploadedFile.name);
      this.restCallHandlerService.postFormData('UPLOAD_BULK_KPI', fd).toPromise().then(function (response) {
        if (response.status === "success") {
          self.messageDialog.showApplicationsMessage("<b>" + response.data, "INFO");
          self.dialogRef.close();
          self.kpiService.fileUploadSubject.next('REFRESH');
        }
        else {
          self.messageDialog.showApplicationsMessage("<b>" + "Error Creating KPI", "ERROR");
        }

      })
    }
    else {
      self.messageDialog.showApplicationsMessage("<b>" + "Please upload a file!", "WARN");
    }
  }
  uploadFileContent() {
    var self = this;
    if (this.selectedFile) {
      var uploadedFile = this.selectedFile;
      console.log(uploadedFile);
      let fd = new FormData();
      var displayMsg = '<ul>';
      fd.append('file', uploadedFile, uploadedFile.name);
      this.restCallHandlerService.postFormData('UPLOAD_BULK_CONTENT', fd).toPromise().then(function (response) {
        if (response.status === "success") {
          self.messageDialog.showApplicationsMessage("<b>" + response.data, "INFO");
          self.dialogRef.close();
          self.kpiService.fileUploadSubject.next('REFRESH');
        }
        else {
          self.messageDialog.showApplicationsMessage("<b>" + "Error Creating Content.Please check logs", "ERROR");
        }
      })
    } else {
      self.messageDialog.showApplicationsMessage("<b>" + "Please upload a file!", "WARN");
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
  }
  closeDialog() {
    this.dialogRef.close();
  }


}
