/*******************************************************************************
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
 * the License.
 ******************************************************************************/

import { Component, OnInit, ViewChild } from "@angular/core";
import { Router, NavigationExtras } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { MatRadioChange } from "@angular/material/radio";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { OfflineAlertingService } from "@insights/app/modules/offline-alerting/offline-alerting-service";
import { OfflineAlertHistoryDetailsDialogComponent } from "@insights/app/modules/offline-alerting/offline-alert-history-details-dialog/offline-alert-history-details-dialog.component";
import { InsightsUtilService } from "@insights/common/insights-util.service";
import { Sort } from "@angular/material/sort";

@Component({
  selector: "app-offline-alert-list",
  templateUrl: "./offline-alert-list.component.html",
  styleUrls: [
    "./offline-alert-list.component.scss",
    "./../../home.module.scss",
  ],
})
export class OfflineAlertListComponent implements OnInit {
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  displayedColumns = [];
  alertDatasource = new MatTableDataSource<any>();
  enableEdit: boolean = false;
  onRadioBtnSelect: boolean = false;
  dashConfigList: any;
  alertName: string;
  panelName: string;
  frequency: number;
  threshold: number;
  trend: string;
  schedule: string;
  active: boolean;
  data: any[];
  refreshRadio: boolean = false;
  selectedAlertData: any;
  showConfirmMessage: string;
  type: string;
  MAX_ROWS_PER_TABLE = 6;
  orgArr = [];
  isDatainProgress: boolean = false;
  disablebutton = [];
  timeZone: string = "";
  selectedIndex: number;
  currentPageValue: number;
  disableDownload: boolean = true;
  userName: string;
  changeState: boolean;
  isActive: boolean;
  currentPageIndex: number = -1;
  enableEmail: boolean = false;
  dateObj: Date;
  timeZoneAbbr: String = "";

  constructor(
    public messageDialog: MessageDialogService,
    private offlineAlertService: OfflineAlertingService,
    public dataShare: DataSharedService,
    public router: Router,
    public dialog: MatDialog,
    public insightsUtil : InsightsUtilService,
  ) {}

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), "");
    this.getAllConfig();
    this.displayedColumns = [
      "radio",
      "Alert",
      "Dashboard",
      "Panel",
      "Trend",
      "Schedule",
      "NextRunTime",
      "Active",
      "Status",
      "Details",
    ];
    this.offlineAlertService.getEmailConfigurationStatus().then((response) => {
      this.enableEmail = response.data;
    });
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.userName = this.dataShare.getUserName();
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  public async getOrgs() {}

  ngAfterViewInit() {
    this.alertDatasource.paginator = this.paginator;
  }

  add() {
    this.offlineAlertService.setType("ADD");
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        userName: this.userName,
      },
    };
    this.router.navigate(
      ["InSights/Home/offlineAlertingConfig"],
      navigationExtras
    );
  }

  edit() {
    this.offlineAlertService.setType("EDIT");
    let alertJson = JSON.parse(this.selectedAlertData.alertJson);

    let emailDetails = alertJson.emailDetails;

    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        id: this.selectedAlertData.id,
        orgId: alertJson.orgId,
        dashUUID: alertJson.dashUUID,
        panelName: alertJson.panelName,
        alertName: this.selectedAlertData.alertName,
        threshold: this.selectedAlertData.threshold,
        frequency: this.selectedAlertData.frequency,
        trend: alertJson.trend,
        schedule: this.selectedAlertData.scheduleType,
        scheduleDateTime: alertJson.scheduleDateTime,
        filters: alertJson.filters,
        rawQuery: alertJson.rawQuery,
        cypherQuery: alertJson.cypherQuery,
        timeRangeText: alertJson.timeRangeText,
        from: alertJson.from,
        emailDetails: JSON.stringify(emailDetails),
        type: "edit",
      },
    };
    this.router.navigate(
      ["InSights/Home/offlineAlertingConfig"],
      navigationExtras
    );
  }

  refresh() {
    this.selectedIndex = -1;
    this.getAllConfig();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;
    this.disableDownload = true;
  }

  enableButtons(event: MatRadioChange, i) {
    this.selectedIndex = i + this.currentPageValue;
    this.onRadioBtnSelect = true;
    this.disablebutton[i] = false;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    if (event.value.status == "NOT_STARTED") {
      this.disableDownload = true;
    } else {
      this.disableDownload = false;
    }
  }

  async getAllConfig() {
    this.isDatainProgress = true;
    this.selectedIndex = -1;
    var self = this;
    self.refreshRadio = false;

    this.dashConfigList = await this.offlineAlertService.loadAlertingList();
    if (
      this.dashConfigList != null &&
      this.dashConfigList.status == "success"
    ) {
      this.dashConfigList.data.forEach((offlineData) => {
        if (offlineData.nextRunTime != 0) {
          offlineData["trend"]=offlineData.trend ==='ABOVE'?'>':'<';
          this.dateObj = new Date(offlineData.nextRunTime * 1000);
          offlineData["nextRunTime"] =
            this.dataShare.convertDateToSpecificDateFormat(
              this.dateObj,
              "yyyy-MM-dd HH:mm a"
            );
        }
      });
      this.alertDatasource.data = this.dashConfigList.data;
      this.isDatainProgress = false;
      this.alertDatasource.paginator = this.paginator;
    }
  }

  list() {
    this.getAllConfig();
  }

  deleteAlertConfig() {
    var self = this;
    let data = self.selectedAlertData;

    var title = "Delete Offline Alert";
    var dialogmessage =
      "Do you want to delete alert <b>" +
      self.selectedAlertData.alertName +
      "</b>? <br> <b> Please note: </b> The action of deleting an offline alert " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedAlertData.alertName,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var offlineAPIRequestJson = {};
        offlineAPIRequestJson["alertName"] = self.selectedAlertData.alertName;

        self.offlineAlertService
          .deleteAlertData(JSON.stringify(offlineAPIRequestJson))
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.openSnackBar(data.data.message, "success");
              self.refresh();
            }
            if (data.status === "failure") {
              self.messageDialog.openSnackBar(data.data.message, "error");
              self.refresh();
            }
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }

  getRowData(event) {
    let data = event;
  }

  showAlertHistoryDetailsDialog(alertName: String) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(OfflineAlertHistoryDetailsDialogComponent, {
        panelClass: "custom-dialog-container",
        disableClose: true,
        height: "85%",
        width: "85%",
        data: {
          alertName: alertName,
          timeZone: this.timeZone,
        },
      });
    }
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

  updateAlertStatus(event: MatSlideToggleChange, id, element) {
    var self = this;
    var title = "Update Offline Alert Status";
    var state = element.isActive ? "Active" : "Inactive";
    var dialogmessage =
      "Are you sure you want to <b>" +
      state +
      "</b> Alert <b>" +
      self.selectedAlertData.alertName +
      "</b> ?";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedAlertData.alertName,
      "ALERT",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.changeState = event.checked;
        var updateStatusRequestJson = {};
        updateStatusRequestJson["alertName"] = element.alertName;
        updateStatusRequestJson["isActive"] = event.checked;
        this.offlineAlertService
          .updateAlertConfigStatus(JSON.stringify(updateStatusRequestJson))
          .then(function (data) {
            if (data.status == "success") {
              self.messageDialog.openSnackBar(
                "Updated Successfully!",
                "success"
              );
              self.refresh();
            } else {
              this.messageDialog.openSnackBar(
                "Failed to update alert state. Please check logs for more details.",
                "error"
              );
              self.refresh();
            }
          });
      } else {
        element.isActive = !event.checked;
      }
    });
  }

  sortData(sort: Sort) {
    const data = this.dashConfigList.data.slice();
    if (!sort.active || sort.direction === '') {
      this.alertDatasource.data = data;
      return;
    }

    this.alertDatasource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc)
    });
    this.alertDatasource.paginator = this.paginator;
  }

}
