import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { MessageDialogService } from '../application-dialog/message-dialog-service';
import { Router } from '@angular/router';
import { KpiService } from '../kpi-addition/kpi-service';

@Component({
  selector: 'app-file-upload',
  templateUrl: './fileUploadDialog.component.html',
  styleUrls: ['./fileUploadDialog.component.css', './../home.module.css']
})
export class FileUploadDialog implements OnInit {
  @ViewChild('fileInput') myFileDiv: ElementRef;
  fileUploadErrorMessage: string;
  selectedFile: File = null;


  constructor(public router: Router, public dialogRef: MatDialogRef<FileUploadDialog>, private restCallHandlerService: RestCallHandlerService,
    public dialog: MatDialog, public messageDialog: MessageDialogService, public kpiService: KpiService) { }

  ngOnInit() {
  }

  onFileChanged(event) {
    this.selectedFile = <File>event.target.files[0];
    console.log(this.selectedFile);
  }
  uploadFile() {
    var self = this;
    if (this.selectedFile) {
      var uploadedFile = this.selectedFile;
      console.log(uploadedFile);
      let fd = new FormData();
      var displayMsg = '<ul>';
      fd.append('file', uploadedFile, uploadedFile.name);
      this.restCallHandlerService.postFormData('UPLOAD_BULK_KPI', fd).toPromise().then(function (response) {
        if (response.status === "success") {

          response.data.forEach(res => {
            displayMsg = displayMsg + "<li>" + res + "</li>"
          })
          self.messageDialog.showApplicationsMessage("<b>" + displayMsg, "SUCCESS");
          self.dialogRef.close();
          self.kpiService.fileUploadSubject.next('REFRESH');
        }
        else {
          self.messageDialog.showApplicationsMessage("<b>" + "Error Creating KPI", "ERROR");
        }

      })
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
