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

import { DatePipe } from "@angular/common";
import { Component, OnInit, ViewChild } from "@angular/core";
import { MatDatepickerInputEvent } from "@angular/material/datepicker";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatRadioChange } from "@angular/material/radio";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { Sort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataArchiveConfigureURLDialog } from "@insights/app/modules/settings/dataarchiving/data-archive-configureurl/data-archive-configureurl-dialog";
import { DataArchiveDetailsDialog } from "@insights/app/modules/settings/dataarchiving/data-archive-details/data-archive-details-dialog";
import { DataArchivingService } from "@insights/app/modules/settings/dataarchiving/dataarchiving-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { InsightsUtilService } from "@insights/common/insights-util.service";

@Component({
  selector: "app-dataarchiving",
  templateUrl: "./dataarchiving.component.html",
  styleUrls: ["./dataarchiving.component.scss", "./../../home.module.scss"],
})
export class DataArchivingComponent implements OnInit {
  displayedColumns = [];
  archivalDatasource = new MatTableDataSource<any>();
  showAdd: boolean = false;
  showList: boolean = true;
  enableDelete: boolean = false;
  enableRefresh: boolean = false;
  enableEdit: boolean = false;
  enableBrowse: boolean = false;
  enableAdd: boolean = true;
  refreshRadio: boolean = false;
  archivalName: any;
  archiveNameList: any = [];
  archiveList: any;
  status: boolean;
  action: boolean;
  currentPageValue: number;
  showMessage: string;
  showDetail: boolean = false;
  showConfirmMessage: string;
  selectedIndex: number;
  currentPageIndex: number = -1;
  sourceUrl: any;
  startDate: string = null;
  endDate: string = null;
  noOfDays: number = null;
  selectedArchivedData: any;
  dataSourceUrl: string;
  actionType: any;
  startDateInput: Date = null;
  endDateInput: Date = null;
  today = new Date();
  regex = new RegExp("^[a-zA-Z0-9_]*$");
  count: number;
  containerCount: number;
  dateObj: Date;
  dateObjFormatted: String;
  timeZone: string = "";
  timeZoneAbbr: string = "";
  MAX_ROWS_PER_TABLE = 6;
  previousActiveIndex = -1;
  clicked = new Array();
  pageRefreshed: boolean = false;
  archivedRecordDetailData = { data: [] };
  hideEdit: boolean = true;
  totalPages: number = -1;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(
    private dataArchivingService: DataArchivingService,
    private datepipe: DatePipe,
    private dialog: MatDialog,
    public messageDialog: MessageDialogService,
    private dataShare: DataSharedService,
    public insightsUtil : InsightsUtilService,
  ) {
    this.getExistingArchivedData();
  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), "");
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.archivalDatasource.paginator = this.paginator;
    this.archivalDatasource.data.forEach((element) => {
      this.clicked.push(true);
    });
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  ngAfterViewInit() {
    this.archivalDatasource.paginator = this.paginator;
  }
  goToNextPage() {
    this.paginator.nextPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  goToPrevPage() {
    this.paginator.previousPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }
  add() {
    this.enableBrowse = false;
    this.enableDelete = false;
    this.showDetail = false;
    this.enableAdd = false;
    if (this.containerCount < 5) {
      this.showAdd = true;
      this.showList = false;
    } else {
      this.messageDialog.showApplicationsMessage(
        "Maximum 5 <b> ACTIVE </b> containers allowed.",
        "WARN"
      );
      this.showDetail = true;
      this.enableAdd = true;
    }
    this.archivalName = "";
    this.startDate = "";
    this.endDate = "";
    this.noOfDays = undefined;
    this.actionType = "save";
    this.startDateInput = undefined;
    this.endDateInput = undefined;
  }

  radioChange(event: MatRadioChange, index) {
    console.log("edit"+this.enableEdit);
    this.enableEdit = true;
    this.enableBrowse = true;
    this.selectedIndex = index + this.currentPageValue;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    if (event.value.status == "ERROR" || event.value.status == "TERMINATED") {
      this.enableDelete = true;
    } else {
      this.enableDelete = false;
    }
    if (
      this.previousActiveIndex == -1 &&
      (event.value.status == "ACTIVE" || event.value.status == "INACTIVE")
    ) {
      this.clicked[index] = false;
      this.previousActiveIndex = index;
    } else if (
      this.pageRefreshed &&
      index === this.previousActiveIndex &&
      (event.value.status == "ACTIVE" || event.value.status == "INACTIVE")
    ) {
      this.clicked[this.previousActiveIndex] = false;
      this.pageRefreshed = false;
    } else if (
      event.value.status == "ACTIVE" ||
      event.value.status == "INACTIVE"
    ) {
      this.clicked[index] = false;
      this.clicked[this.previousActiveIndex] = true;
      this.previousActiveIndex = index;
    } else if (
      event.value.status == "INPROGRESS" ||
      event.value.status == "ERROR" ||
      event.value.status == "TERMINATED" ||
      event.value.status == "ERROR_REMOVE_CONTAINER"
    ) {
      this.clicked[this.previousActiveIndex] = true;
      this.previousActiveIndex = index;
    }
  }

  public async getExistingArchivedData() {
    var self = this;
    this.archiveNameList = [];
    this.archiveList = [];
    this.archiveList = await self.dataArchivingService.listArchivedRecord();
    if (this.archiveList != null && this.archiveList.status == "success") {
      this.archivalDatasource.data = this.archiveList.data.sort(
        (a, b) => a.archivalName > b.archivalName
      );
      this.archivedRecordDetailData.data = this.archiveList.data;
      this.archivalDatasource.paginator = this.paginator;
      this.count = 0;
      this.containerCount = 0;
      self.showDetail = true;
      for (var element of this.archivalDatasource.data) {
        if (this.count < this.archivalDatasource.data.length) {
          if (this.archivalDatasource.data[this.count].expiryDate == 0) {
            this.archivalDatasource.data[this.count].expiryDate = "-";
          } else {
            this.dateObj = new Date(
              this.archivalDatasource.data[this.count].expiryDate * 1000
            );
            this.dateObjFormatted =
              this.dataShare.convertDateToSpecificDateFormat(
                this.dateObj,
                "yyyy-MM-dd HH:mm:ss"
              );
            this.archivalDatasource.data[this.count].expiryDate =
              this.dateObjFormatted;
          }
          if (this.archivalDatasource.data[this.count].boltPort == 0) {
            this.archivalDatasource.data[this.count].boltPort = "-";
          }

          this.dateObj = new Date(
            this.archivalDatasource.data[this.count].createdOn * 1000
          );
          this.dateObjFormatted =
            this.dataShare.convertDateToSpecificDateFormat(
              this.dateObj,
              "yyyy-MM-dd HH:mm:ss"
            );
          this.archivalDatasource.data[this.count].createdOn =
            this.dateObjFormatted;
          if (
            this.archivalDatasource.data[this.count].status == "ACTIVE" ||
            this.archivalDatasource.data[this.count].status == "INPROGRESS"
          ) {
            this.containerCount += 1;
          }
          if (this.archivalDatasource.data[this.count].status == "ACTIVE") {
            this.archivalDatasource.data[this.count].action = true;
          } else if (
            this.archivalDatasource.data[this.count].status == "INACTIVE"
          ) {
            this.archivalDatasource.data[this.count].action = false;
          } else {
            this.archivalDatasource.data[this.count].action = undefined;
          }
          this.count += 1;
        }
      }

      this.archivalDatasource.data.forEach((element) => {
        this.clicked.push(true);
      });
      this.displayedColumns = [
        "radio",
        "ArchivalName",
        "DataSourceUrl",
        "BoltPort",
        "ExpiryDate",
        "Status",
        "Action",
      ];
      setTimeout(() => {
        this.showConfirmMessage = "";
      }, 3000);
    } else {
      self.showMessage = "Something wrong with Service.Please try again.";
      self.messageDialog.openSnackBar(
        "Something wrong with Service.Please try again.",
        "error"
      );
    }
    this.totalPages = Math.ceil(
      this.archivalDatasource.data.length / this.MAX_ROWS_PER_TABLE
    );
  }

  validateArchiveData() {
    var checkname = this.regex.test(this.archivalName);
    if (
      this.archivalName == "" ||
      this.startDateInput == undefined ||
      this.endDateInput == undefined ||
      this.noOfDays == undefined
    ) {
      this.messageDialog.openSnackBar("Please fill mandatory fields.", "error");
    } else if (!checkname) {
      this.messageDialog.openSnackBar(
        "Please enter valid archival name, it contains only alphanumeric character and underscore",
        "error"
      );
    } else if (this.startDateInput > this.endDateInput) {
      this.messageDialog.openSnackBar(
        "Start date should be less than End date",
        "error"
      );
    } else {
      this.SaveData();
    }
  }

  async SaveData() {
    var self = this;
    this.showDetail = false;
    var archiveAPIRequestJson = {};
    if (this.actionType == "save") {
      archiveAPIRequestJson["archivalName"] = self.archivalName;
      archiveAPIRequestJson["startDate"] = self.startDate;
      archiveAPIRequestJson["endDate"] = self.endDate;
      archiveAPIRequestJson["daysToRetain"] = self.noOfDays;
      archiveAPIRequestJson["author"] = this.dataShare.getUserName();
      var dialogmessage =
        " You have created a new Archive data <b>" +
        self.archivalName +
        "</b>. Do you want to continue ? ";
      var title = "Save " + this.archivalName;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.dataArchivingService
            .saveArchivalRecord(JSON.stringify(archiveAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                self.messageDialog.openSnackBar(
                  "<b>" +
                    self.archivalName +
                    "</b> saved successfully. Data is being provisioned, Please revisit this screen after sometime.",
                  "success"
                );
                self.getExistingArchivedData();
                self.refresh();
                self.showDetail = true;
              } else if (response.message === "Archival Name already exists.") {
                self.messageDialog.openSnackBar(
                  "<b>" +
                    self.archivalName +
                    "</b> already exists. Please try again with a new name.",
                  "error"
                );
              } else if (
                response.message === "Data Archival agent not present."
              ) {
                self.messageDialog.openSnackBar(
                  "Please register agent before saving record.",
                  "error"
                );
              } else {
                self.messageDialog.openSnackBar(
                  "Failed to save the archive data.Please check logs.",
                  "error"
                );
              }
            });
        }
      });
    }
  }

  delete() {
    var self = this;
    if (
      self.selectedArchivedData.status == "ERROR" ||
      self.selectedArchivedData.status == "TERMINATED"
    ) {
      var title = "Delete";
      var dialogmessage =
        "Do you want to delete <b>" +
        self.selectedArchivedData.archivalName +
        "</b> ? <br> <b> Please note: </b> The action of deleting " +
        "<b>" +
        self.selectedArchivedData.archivalName +
        "</b> cannot be undone. Do you want to continue ? ";
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        self.selectedArchivedData.archivalName,
        "DELETE",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.dataArchivingService
            .deleteArchivedData(self.selectedArchivedData.archivalName)
            .then(function (data) {
              if (data.status == "success") {
                self.messageDialog.openSnackBar(
                  "<b>" +
                    self.selectedArchivedData.archivalName +
                    "</b> deleted successfully.",
                  "success"
                );
                self.getExistingArchivedData();
                self.refresh();
              } else if (data.status == "failure" && data.message != null) {
                self.messageDialog.openSnackBar(data.message, "error");
              } else {
                self.messageDialog.openSnackBar(
                  "Failed to delete <b>" +
                    self.selectedArchivedData.archivalName +
                    "</b>. Please check logs for details.",
                  "error"
                );
              }
            })
            .catch(function (data) {
              self.showConfirmMessage = "service_error";
              self.getExistingArchivedData();
            });
        }
      });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please inactivate container before deleting it.",
        "WARN"
      );
    }
  }

  refresh() {
    this.selectedIndex = -1;
    this.selectedArchivedData = "";
    this.clicked = [];
    this.archivalDatasource.data.forEach((element) => {
      this.clicked.push(true);
    });
    this.pageRefreshed = true;
    this.showList = true;
    this.showAdd = false;
    this.getExistingArchivedData;
    this.enableDelete = false;
    this.enableBrowse = false;
    this.enableAdd = true;
    this.enableEdit = false;
    this.showDetail = true;
  }

  getstartDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.startDateInput = new Date(event.value);
    this.startDate = this.dataShare.convertDateToSpecificDateFormat(
      this.startDateInput,
      "yyyy-MM-dd'T'HH:mm:ss'Z'"
    );
  }

  getendDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.endDateInput = new Date(event.value);
    this.endDate = this.dataShare.convertDateToSpecificDateFormat(
      this.endDateInput,
      "yyyy-MM-dd'T'HH:mm:ss'Z'"
    );
  }

  updateStatus(event: MatSlideToggleChange, selectedArchivedData) {
    var self = this;
    this.action = event.checked;
    var title = "Update Status";
    if (this.action == true && selectedArchivedData.status == "INACTIVE") {
      var dialogmessage =
        "Do you want to make container <b>" +
        self.selectedArchivedData.archivalName +
        " ACTIVE</b> ? ";
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        self.selectedArchivedData.archivalName,
        "ALERT",
        "40%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.dataArchivingService
            .activateArchivedData(selectedArchivedData.archivalName)
            .then(function (data) {
              if (data.status == "success") {
                self.messageDialog.openSnackBar(
                  "Container activated successfully.",
                  "success"
                );
                self.getExistingArchivedData();
                self.refresh();
                selectedArchivedData.action = true;
              } else {
                self.messageDialog.openSnackBar(
                  "Failed to update the status.Please check logs for more details.",
                  "error"
                );
              }
            });
        }
        selectedArchivedData.action = false;
      });
    } else if (
      this.action == false &&
      selectedArchivedData.status == "ACTIVE"
    ) {
      var dialogmessage =
        "Do you want to make container <b>" +
        self.selectedArchivedData.archivalName +
        " INACTIVE</b>? ";
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        self.selectedArchivedData.archivalName,
        "ALERT",
        "30%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.dataArchivingService
            .inactivateArchivedData(selectedArchivedData.archivalName)
            .then(function (data) {
              if (data.status == "success") {
                self.messageDialog.openSnackBar(
                  "Container inactivated successfully.",
                  "success"
                );
                self.getExistingArchivedData();
                self.refresh();
                selectedArchivedData.action = false;
              } else {
                self.messageDialog.openSnackBar(
                  "Failed to update the status.Please check logs for more details.",
                  "error"
                );
              }
            });
        }
        selectedArchivedData.action = true;
      });
    }
  }

  onNavigate(sourceUrl: any, status: any): void {
    if (status === "ACTIVE") {
      window.open(sourceUrl, "_blank");
      this.refresh();
    }
  }

  archiveRecordsDetails() {
    const paramCheck = this.archivedRecordDetailData.data.filter(
      (f) => f.archivalName === this.selectedArchivedData.archivalName
    );
    const param = paramCheck.length > 0 ? paramCheck[0] : {};
    this.dialog.open(DataArchiveDetailsDialog, {
      panelClass: "custom-dialog-container",
      disableClose: true,
      width: "35%",
      data: { record: param },
    });
  }

  configureURL() {
    var sourceURL;
    const paramCheck = this.archivedRecordDetailData.data.filter(
      (f) => f.archivalName === this.selectedArchivedData.archivalName
    );
    const param = paramCheck.length > 0 ? paramCheck[0] : {};
    var name = param.archivalName;
    if ("sourceUrl" in param) {
      sourceURL = param.sourceUrl;
    } else {
      sourceURL = "";
    }

    let dialogRef = this.dialog.open(DataArchiveConfigureURLDialog, {
      panelClass: "custom-dialog-container",
      disableClose: true,
      width: "45%",
      data: { name: name, sourceURL: sourceURL },
    });

    dialogRef.afterClosed().subscribe((result) => {
      this.getExistingArchivedData();
      this.refresh();
    });
  }

  clearValues() {
    this.archivalName = "";
    this.startDateInput = null;
    this.endDateInput = null;
    this.noOfDays = null;
  }

  sortData(sort: Sort) {
    const data = this.archiveList.data.slice();
    if (!sort.active || sort.direction === '') {
      this.archivalDatasource.data = data;
      return;
    }

    this.archivalDatasource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc)
    });
    this.archivalDatasource.paginator = this.paginator;
  }
}
