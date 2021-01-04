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

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationExtras, Router } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { FileSystemService } from '../file-system.service';

@Component({
  selector: 'app-filesystem-config',
  templateUrl: './filesystem-config.component.html',
  styleUrls: ['../file-system.component.css', './../../home.module.css']
})
export class FileSystemConfigComponent implements OnInit {
  fileName: string;
  fileType: string='JSON';
  fileTypeList = [];
  module: string;
  fileModuleList = [];
  valid: boolean = true;
  regex = new RegExp('^[a-zA-Z0-9_]*$');
  file: any;
  fileExt: any;
  disableInputFields: boolean = false;
  scriptReg: string = "<script>";
  errorMessage: string;
  configData: any;
  fileUploadMsg = "Upload valid file.";


  constructor(public fileSystemService: FileSystemService, public router: Router, private route: ActivatedRoute,
    public messageDialog: MessageDialogService) {

  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.fileTypeList = JSON.parse(params.fileTypes);
      this.fileModuleList = JSON.parse(params.modules);
      this.configData = JSON.parse(params.configdetails);
      console.log(params);
      if (this.configData.type == 'edit') {
        this.fileName = this.configData.data.fileName;
        this.fileType = this.configData.data.fileType;
        this.module = this.configData.data.fileModule;
        this.disableInputFields = true;
        this.fileUploadMsg = "Upload edited file."
      }
    });
  }

  ngAfterViewInit() {

  }

  validateAndUpload() {
    var dt = (<HTMLInputElement>document.getElementById("file"));
    var checkFileName = this.regex.test(this.fileName);
    if (this.fileName == '' || this.fileName == null) {
      this.messageDialog.showApplicationsMessage('Please fill file name.', 'ERROR');
    } else if (this.fileType == '' || this.fileType == null) {
      this.messageDialog.showApplicationsMessage('Please select file type.', 'ERROR');
    } else if (this.module == '' || this.module == null) {
      this.messageDialog.showApplicationsMessage('Please select file module.', 'ERROR');
    } else if (!checkFileName) {
      this.messageDialog.showApplicationsMessage(
        'Please enter valid file name, it contains only alphanumeric character and underscore.', 'ERROR');
    } else if (dt.value == '') {
      this.messageDialog.showApplicationsMessage('Please select a file to upload.', 'ERROR');
    } else if (this.fileExt.replace(/\./g, '') != this.fileType.toLowerCase()) {
      this.messageDialog.showApplicationsMessage('Uploaded file is not of file type <b>' + this.fileType + '</b>.', 'ERROR');
    } else if (!this.valid) {
      this.messageDialog.showApplicationsMessage(this.errorMessage, 'ERROR');
    } else {
      this.uploadConfigFile();
    }
  }

  uploadConfigFile() {
    var dialogmessage = 'Do you want to save file <b>' + this.fileName + '</b> ?';
    var title = 'Save '+ this.fileName;
    const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, '', 'ALERT', '40%');
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.fileSystemService.uploadFileWithDetails(this.file, this.fileName, this.fileType, this.module)
          .subscribe(response => {
            if (response.status == 'success') {
              this.messageDialog.showApplicationsMessage('<b>' + this.fileName + '</b> saved successfully.', "SUCCESS");
              let navigationExtras: NavigationExtras = {
                skipLocationChange: true,
                queryParams: {
                }
              };
              this.router.navigate(['InSights/Home/filesystem'], navigationExtras);
            } else if (response.status == 'failure') {
              this.messageDialog.showApplicationsMessage(response.message, 'ERROR');
            }
            else {
              this.messageDialog.showApplicationsMessage('Failed to save file. Please check logs.', 'ERROR');
            }
          });
      }
    });
  }

  reset() {
    if (this.configData.type == 'edit') {
      var dt = (<HTMLInputElement>document.getElementById("file"))
      dt.value = "";
    } else {
      this.fileName = "";
      this.fileType = "";
      this.module = "";
      var dt = (<HTMLInputElement>document.getElementById("file"))
      dt.value = "";
    }

  }

  refresh() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
      }
    };
    this.router.navigate(['InSights/Home/filesystem'], navigationExtras);
  }

  onFileChanged(event) {
    if (event.target.files && event.target.files[0]) {
      this.file = event.target.files.item(0);
      console.log(this.file);
    }
    var fileName = this.file.name;
    this.fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
    console.log(this.fileExt);
    let fileReader = new FileReader();
    fileReader.onload = () => {
      if (this.fileExt == '.json') {
        this.valid = this.validateJson(fileReader.result.toString());
      } else if (this.fileExt == '.html') {
        this.valid = this.validateHtml(fileReader.result.toString())
      }
    };
    fileReader.readAsText(event.target.files[0]);


  }

  validateJson(message) {
    try {
      JSON.parse(message);
    } catch (e) {
      this.errorMessage = 'Uploaded file is not a valid JSON. ' + e;
      return false;
    }
    return true;
  }

  validateHtml(message: string) {
    if (message.search(this.scriptReg) != -1) {
      this.errorMessage = 'Uploaded file contains script tag.';
      return false;
    } else {
      return true;
    }
  }


}