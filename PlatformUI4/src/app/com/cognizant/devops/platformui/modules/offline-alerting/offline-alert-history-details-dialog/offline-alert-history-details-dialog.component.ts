/********************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
 * the License. */

import { OfflineAlertingService } from "@insights/app/modules/offline-alerting/offline-alerting-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { TitleCasePipe, DatePipe } from "@angular/common";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { WorkflowHistoryDetailsDialog } from "@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { Component, OnInit, ViewChild, Inject } from "@angular/core";

@Component({
  selector: "app-offline-alert-history-details-dialog",
  templateUrl: "./offline-alert-history-details-dialog.component.html",
  styleUrls: [
    "./offline-alert-history-details-dialog.component.scss",
    "./../../home.module.scss",
  ],
})
export class OfflineAlertHistoryDetailsDialogComponent implements OnInit {
  showContent: boolean;
  showThrobber: boolean = false;
  checkResponseData: boolean;
  alertName: string;
  columnLength: number;
  showDetail: boolean = false;
  detailedRecords = [];
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
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  currentPageIndex: number = -1;
  totalPages: number = -1;
  constructor(
    public dialogRef: MatDialogRef<WorkflowHistoryDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public offlineAlertingService: OfflineAlertingService,
    private titlecase: TitleCasePipe,
    public datePipe: DatePipe,
    public messageDialog: MessageDialogService,
    public dataShare: DataSharedService
  ) {
    this.fillMasterHeaderData();
  }

  ngOnInit() {
    this.loadAlertHistoryDetailsDialog();
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.alertName = this.data.alertName;
    this.timezone = this.dataShare.getTimeZone();
    this.timezoneAbbr = this.dataShare.getTimeZoneAbbr();
  }

  ngAfterViewInit() {
    this.executionRecordsDetailedDatasource.paginator = this.paginator;
  }

  /**
   * method to set Headers of the Dialog
   *
   */
  fillMasterHeaderData() {
    this.masterHeader.set("executionid", "Execution ID");
    this.masterHeader.set("currentTask", "Current Task");
    this.masterHeader.set("taskStatus", "Task Status");
    this.masterHeader.set("startTime", "Start Time");
    this.masterHeader.set("endTime", "End Time");
    this.masterHeader.set("statusLog", "Status Log");
  }

  /**
   * method to load records in the Dialog
   *
   */
  async loadAlertHistoryDetailsDialog() {
    this.showThrobber = true;
    this.showContent = !this.showThrobber;
    this.checkResponseData = true;
    var configIdJson = {};
    configIdJson["alertName"] = this.data.alertName;
    this.recordList =
      await this.offlineAlertingService.getAlertExecutionRecords(
        JSON.stringify(configIdJson)
      );
    if (this.recordList != null && this.recordList.status == "success") {
      this.showThrobber = false;
      this.showContent = !this.showThrobber;
      var dataArray = this.recordList.data.records;
      if (dataArray.length === 0) {
        this.checkResponseData = false;
      }
      this.executionRecordsDetailedDatasource.paginator = this.paginator;
      if (dataArray != undefined) {
        dataArray.forEach((key) => {
          var obj = key;
          if (typeof obj["startTime"] !== "undefined") {
            if (obj["startTime"] === 0) {
              obj["startTime"] = "-";
            } else {
              this.dateObj = new Date(obj["startTime"]);
              this.dateObjFormatted = this.datePipe.transform(
                this.dateObj,
                "yyyy-MM-dd HH:mm:ss"
              );
              obj["startTime"] = this.dateObjFormatted;
            }
          }
          if (typeof obj["endTime"] !== "undefined") {
            if (obj["endTime"] === 0) {
              obj["endTime"] = "-";
            } else {
              this.dateObj = new Date(obj["endTime"]);
              this.dateObjFormatted = this.datePipe.transform(
                this.dateObj,
                "yyyy-MM-dd HH:mm:ss"
              );
              obj["endTime"] = this.dateObjFormatted;
            }
          }
          if (typeof obj["taskStatus"] !== "undefined") {
            if (obj["taskStatus"].toLowerCase() === "null") {
              obj["taskStatus"] = "-";
            } else {
              obj["taskStatus"] = this.titlecase.transform(obj["taskStatus"]);
            }
          }
          if (typeof obj["statusLog"] !== "undefined") {
            if (obj["statusLog"].toLowerCase() === "null") {
              obj["statusLog"] = "-";
            } else {
              obj["statusLog"] = obj["statusLog"];
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
