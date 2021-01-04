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

import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatRadioChange } from '@angular/material/radio';
import { MatTableDataSource } from '@angular/material/table';
import { NavigationExtras, Router } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { saveAs as importedSaveAs } from "file-saver";
import { FileSystemService } from './file-system.service';



@Component({
  selector: 'app-filesystem-configuration',
  templateUrl: './file-system.component.html',
  styleUrls: ['./file-system.component.css', './../home.module.css']
})
export class FileSystemComponent implements OnInit {
  displayedColumns = [];
  MAX_ROWS_PER_TABLE = 10;
  selectedRow: any;
  disableDownload: boolean = true;
  enableDelete: boolean = false;
  fileStorageDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator) paginator: MatPaginator;
  fileTypeResponse: any;
  fileTypeList = [];
  fileModuleResponse: any;
  fileModuleList = [];
  configListResponse: any;
  configParams: string;
  enableEdit: boolean;


  constructor(public fileSystemService: FileSystemService, public router: Router, public messageDialog: MessageDialogService) {
    this.displayedColumns = ['radio', 'fileName', 'fileType', 'fileModule'];
    this.getFileTypeList();
    this.getFileModuleList();
    this.getConfigFileList();
  }

  ngOnInit() {

  }

  ngAfterViewInit() {
    this.fileStorageDatasource.paginator = this.paginator;
  }

  async getFileTypeList() {
    this.fileTypeResponse = await this.fileSystemService.loadFileType();
    if (this.fileTypeResponse.data != null && this.fileTypeResponse.status == 'success') {
      this.fileTypeList = this.fileTypeResponse.data;
      console.log(this.fileTypeList);
    }

  }

  async getFileModuleList() {
    this.fileModuleResponse = await this.fileSystemService.loadFileModule();
    if (this.fileModuleResponse.data != null && this.fileModuleResponse.status == 'success') {
      this.fileModuleList = this.fileModuleResponse.data;
      console.log(this.fileModuleList);
    }

  }

  public async getConfigFileList() {
    var self = this;
    this.configListResponse = await this.fileSystemService.loadConfigFilesList();
    if (this.configListResponse.data != null && this.configListResponse.status == "success") {
      this.fileStorageDatasource.data = this.configListResponse.data;
      this.fileStorageDatasource.paginator = this.paginator;
    }
  }


  add() {
    this.configParams = JSON.stringify({ type: 'save' });
    this.navigate();
  }

  update() {
    var fileDetail = this.configListResponse.data.find(({ fileName }) => fileName === this.selectedRow.fileName);
    this.configParams = JSON.stringify({ type: 'edit', data: fileDetail });
    this.navigate();
  }

  navigate() {
    if (this.fileTypeList.length == 0) {
      this.messageDialog.showApplicationsMessage(
        "Failed to load file types list. Please check logs for more details.", "ERROR");
    }
    if (this.fileModuleList.length == 0) {
      this.messageDialog.showApplicationsMessage(
        "Failed to load file module list. Please check logs for more details.", "ERROR");
    }

    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        configdetails: this.configParams,
        fileTypes: JSON.stringify(this.fileTypeList),
        modules: JSON.stringify(this.fileModuleList)
      }
    };
    this.router.navigate(['InSights/Home/file-system-configuration'], navigationExtras);

  }

  downloadSelectedFile() {
    var FileDetailJson = {};
    var filename = this.selectedRow.fileName + '.' + String(this.selectedRow.fileType).toLowerCase();
    FileDetailJson['fileName'] = this.selectedRow.fileName;
    FileDetailJson['fileType'] = this.selectedRow.fileType;
    this.fileSystemService.downloadConfigFile(JSON.stringify(FileDetailJson))
      .then(function (data) {
        importedSaveAs(data, filename);
      });
  }

  refresh() {
    this.selectedRow = '';
    this.enableDelete = false;
    this.disableDownload = true;
    this.enableEdit = false;
    this.getConfigFileList();
  }

  delete() {
    var self = this;
    var title = 'Delete';
    var dialogmessage = 'Do you want to delete <b>' + self.selectedRow.fileName +
      '</b> ? <br> <b> Please note: </b> The action of deleting ' + '<b>' + self.selectedRow.fileName +
      '</b> cannot be undone. Do you want to continue ? ';
    const dialogRef = self.messageDialog.showConfirmationMessage(title, dialogmessage, self.selectedRow.fileName, 'ALERT', '40%');
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.fileSystemService.deleteConfigFile(self.selectedRow.fileName)
          .then(function (response) {
            if (response.status == 'success') {
              self.messageDialog.showApplicationsMessage('<b>' + self.selectedRow.fileName +
                '</b> deleted successfully.', 'SUCCESS');
              self.refresh();
            } else if (response.status == 'failure' && response.message != null) {
              self.messageDialog.showApplicationsMessage(response.message, "ERROR");
            } else {
              self.messageDialog.showApplicationsMessage("Failed to delete <b>" +
                self.selectedRow.fileName + "</b>. Please check logs for details.", "ERROR");
            }
          }).catch(function (response) { });
      }
    });
  }

  radioChange(event: MatRadioChange, index) {
    this.enableDelete = true;
    this.disableDownload = false;
    this.enableEdit = true;
  }

}