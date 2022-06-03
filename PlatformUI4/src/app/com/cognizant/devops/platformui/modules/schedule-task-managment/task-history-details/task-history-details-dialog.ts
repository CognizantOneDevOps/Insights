/*********************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
 *******************************************************************************/

import { DatePipe, TitleCasePipe } from "@angular/common";
import { Component, Inject, OnInit, ViewChild } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { TaskManagementService } from "@insights/app/modules/schedule-task-managment/task-management-service";

@Component({
  selector: "task-history-details-dialog",
  templateUrl: "./task-history-details-dialog.html",
  styleUrls: ["./task-history-details-dialog.scss", "./../../home.module.scss"],
})
export class TaskHistoryDetailsDialog implements OnInit {
  showContent: boolean;
  showThrobber: boolean = false;
  checkResponseData: boolean;
  componentName: string;
  columnLength: number;
  showDetail: boolean = false;
  detailedRecords = [];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  executionRecordsDetailedDatasource = new MatTableDataSource([]);
  headerArrayDisplay = [];
  masterHeader = new Map<String, String>();
  finalHeaderToShow = new Map<String, String>();
  headerSet = new Set();
  recordList: any;
  MAX_ROWS_PER_TABLE = 6;
  timezone: String;
  timezoneAbbr: String = "";
  dateObj: Date;
  dateObjFormatted: String;
  currentPageIndex: number = -1;
  totalPages: number = -1;

  constructor(
    public dialogRef: MatDialogRef<TaskHistoryDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public timerTaskService: TaskManagementService,
    private titlecase: TitleCasePipe,
    public datePipe: DatePipe,
    public messageDialog: MessageDialogService,
    public dataShare: DataSharedService
  ) {
    this.fillMasterHeaderData();
  }

  ngOnInit() {
    this.loadTaskHistoryDetailsDialog();
    this.componentName = this.data.componentName;
    this.timezone = this.dataShare.getTimeZone();
    this.timezoneAbbr = this.dataShare.getTimeZoneAbbr();
  }

  ngAfterViewInit() {
    this.executionRecordsDetailedDatasource.paginator = this.paginator;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  /**
   * method to set Headers of the Dialog
   *
   */
  fillMasterHeaderData() {
    this.masterHeader.set("recordtimestamp", "Record Time");
    this.masterHeader.set("status", "Task Status");
    this.masterHeader.set("version", "Version");
    this.masterHeader.set("processingTime", "processing Time");
    this.masterHeader.set("message", "Message");
  }

  /**
   * method to load records in the Dialog
   *
   */
  async loadTaskHistoryDetailsDialog() {
    this.showThrobber = true;
    this.showContent = !this.showThrobber;
    this.checkResponseData = true;
    var taskJson = {};
    this.executionRecordsDetailedDatasource.paginator = this.paginator;
    taskJson["componentName"] = this.data.componentName;
    var recordList = await this.timerTaskService.getTaskExecutionRecords(
      JSON.stringify(taskJson)
    );
    if (recordList != null && recordList.status == "success") {
      this.showThrobber = false;
      this.showContent = !this.showThrobber;
      var dataArray = recordList.data;
      if (dataArray.length === 0) {
        this.checkResponseData = false;
      }
      if (dataArray != undefined) {
        dataArray.forEach((key) => {
          var obj = key;
          if (typeof obj["recordtimestamp"] !== "undefined") {
            if (obj["recordtimestamp"] === 0) {
              obj["recordtimestamp"] = "-";
            } else {
              this.dateObj = new Date(obj["recordtimestamp"]);
              this.dateObjFormatted = this.datePipe.transform(
                this.dateObj,
                "yyyy-MM-dd HH:mm:ss"
              );
              obj["recordtimestamp"] = this.dateObjFormatted;
            }
          }
          this.detailedRecords.push(obj);
          this.showDetail = true;
          for (var attr in obj) {
            // fill data array in set , Only those header which mention in masterHeader
            if (this.masterHeader.has(attr)) {
              this.headerSet.add(attr);
            }
          }
        });
        this.executionRecordsDetailedDatasource.data = this.detailedRecords;
        this.totalPages = Math.ceil(
          this.executionRecordsDetailedDatasource.data.length /
            this.MAX_ROWS_PER_TABLE
        );
        this.showSelectedField();
      }
    } else {
      this.closeShowDetailsDialog();
      this.messageDialog.openSnackBar(
        "Something wrong with Service.Please try again.",
        "error"
      );
    }
  }

  /**
   * method to display the header fields
   */
  showSelectedField(): void {
    //Define sequence of headerSet according to master array and remove unwanted header
    this.masterHeader.forEach((value: string, key: string) => {
      if (this.headerSet.has(key)) {
        this.finalHeaderToShow.set(key, value);
      }
    });
    this.columnLength = this.finalHeaderToShow.size;
    // create headerArrayDisplay from map keys
    if (this.finalHeaderToShow.size > 0) {
      this.headerArrayDisplay = Array.from(this.finalHeaderToShow.keys());
    }
  }

  /**
   * method to close the dialog
   */
  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

  goToNextPage() {
    this.paginator.nextPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  goToPrevPage() {
    this.paginator.previousPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  hideTextOverflow(text: any) {
    if (text !== undefined && text.length > 100) {
      return text.slice(0, 100) + "...";
    } else {
      return text;
    }
  }
}
