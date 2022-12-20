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
import { DataSharedService } from "@insights/common/data-shared-service";

@Component({
  selector: 'app-filesystem-configuration',
  templateUrl: './file-system.component.html',
  styleUrls: ['./file-system.component.scss', './../home.module.scss']
})
export class FileSystemComponent implements OnInit {
  displayedColumns = [];
  MAX_ROWS_PER_TABLE = 7;
  selectedRow: any;
  disableDownload: boolean = true;
  enableDelete: boolean = false;
  fileStorageDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  fileTypeResponse: any;
  fileTypeList = [];
  fileModuleResponse: any;
  fileModuleList = [];
  configListResponse: any;
  configParams: string;
  enableEdit: boolean;
  currentPageValue: number;
  currentPageIndex: number = -1;
  selectedIndex: -1;
  totalPages: number = -1;
  timeZoneAbbr:String="";
  dateObj:Date;

  constructor(public fileSystemService: FileSystemService, public router: Router, public dataShare: DataSharedService, public messageDialog: MessageDialogService) {
    this.displayedColumns = ['radio', 'fileName', 'fileType', 'fileModule', 'lastUpdatedTime'];
    this.getFileTypeList();
    this.getFileModuleList();
    this.getConfigFileList();
  }

  ngOnInit() {
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
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
      var dataArray = this.configListResponse.data;
      if (dataArray != undefined) {
        dataArray.forEach((key) => {
          var obj = key;
          if (typeof obj["lastUpdatedTime"] !== "undefined") {
              this.dateObj = new Date(obj["lastUpdatedTime"]*1000);
              obj["lastUpdatedTime"] = this.dataShare.convertDateToSpecificDateFormat(
                this.dateObj,
                "yyyy-MM-dd HH:mm:ss"
              );
            }
        });
      }
      this.fileStorageDatasource.paginator = this.paginator;
    }
    this.totalPages = Math.ceil(this.fileStorageDatasource.data.length / this.MAX_ROWS_PER_TABLE);
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
      this.messageDialog.openSnackBar("<b>Failed to load file types list. Please check logs for more details.</b>", "error");
    }
    if (this.fileModuleList.length == 0) {
      this.messageDialog.openSnackBar("<b>Failed to load file module list. Please check logs for more details.</b>", "error");
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
    this.fileSystemService.downloadConfigFile(btoa(JSON.stringify(FileDetailJson)))
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
    const dialogRef = self.messageDialog.showConfirmationMessage(title, dialogmessage, self.selectedRow.fileName, 'DELETE', '35%');
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.fileSystemService.deleteConfigFile(self.selectedRow.fileName)
          .then(function (response) {
            if (response.status == 'success') {
              self.messageDialog.openSnackBar("<b>" + self.selectedRow.fileName + "</b> deleted successfully.", "success");
              self.refresh();
            } else if (response.status == 'failure' && response.message != null) {
              self.messageDialog.openSnackBar(response.message, "error");
            } else {
              self.messageDialog.openSnackBar("Failed to delete <b>" +
                self.selectedRow.fileName + "</b>. Please check logs for details.", "error");
            }
          }).catch(function (response) { });
      }
    });
  }

  radioChange(event: MatRadioChange, index) {
    this.selectedIndex = index + this.currentPageIndex;
    this.enableDelete = true;
    this.disableDownload = false;
    this.enableEdit = true;
  }
  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  goToNextPage() {
    this.paginator.nextPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  goToPrevPage() {
    this.paginator.previousPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
}
