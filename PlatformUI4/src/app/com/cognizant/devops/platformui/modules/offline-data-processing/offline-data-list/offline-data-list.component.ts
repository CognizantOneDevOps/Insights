/*******************************************************************************
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
 ******************************************************************************/
import { OfflineDetailsComponent } from "@insights/app/modules/offline-data-processing/offline-details/offline-details.component";
import { DataSharedService } from "@insights/common/data-shared-service";
import { MatRadioChange } from "@angular/material/radio";
import { OfflineService } from "@insights/app/modules/offline-data-processing/offline-service";
import { Component, OnInit } from "@angular/core";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { FileUploadDialog } from "@insights/app/modules/fileUploadDialog/fileUploadDialog.component";
import { ContentService } from "@insights/app/modules/content-config-list/content-service";
import { ViewChild } from "@angular/core";
import { Router, NavigationExtras } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import cronstrue from "cronstrue";

@Component({
  selector: "app-offline-data-list",
  templateUrl: "./offline-data-list.component.html",
  styleUrls: ["./../../home.module.scss", "./offline-data-list.component.scss"],
})
export class OfflineDataListComponent implements OnInit {
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  offlineDatasource = new MatTableDataSource<any>();
  displayedColumns = [];
  onRadioBtnSelect: boolean = false;
  selectedOfflineData: any;
  MAX_ROWS_PER_TABLE = 5;
  selectedIndex: number = -1;
  currentPageIndex: number = -1;
  totalPages: number = 1;
  currentPageValue: number;
  offlineList: any;
  toolName: string;
  queryName: string;
  cronSchedule: string;
  lastExecutionTime: number;
  recordsProcessed: string;
  queryProcessingTime: number;
  isActive: boolean;
  status: string;
  showConfirmMessage: string;
  dateObj: Date;
  timeZoneAbbr: String = "";
  isActivate: boolean = false;
  schedule: any;

  constructor(
    public router: Router,
    public dialog: MatDialog,
    private messageDialog: MessageDialogService,
    public contentService: ContentService,
    public offlineService: OfflineService,
    public dataShare: DataSharedService
  ) {}

  ngOnInit(): void {
    this.getAllOfflineData();
    this.displayedColumns = [
      "radio",
      "toolName",
      "queryName",
      "schedule",
      "active",
      "status",
      "lastExecutionTime",
      "queryProcessingTime",
      "recordsProcessed",
      "details",
    ];
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();

    this.offlineService.offlineUploadSubject.subscribe((res) => {
      if (res === "REFRESH") {
        this.getAllOfflineData();
      }
    });
  }

  ngAfterViewInit() {
    this.offlineDatasource.paginator = this.paginator;
  }

  addnewOfflineData() {
    this.offlineService.setType("ADD");
    this.router.navigate(["InSights/Home/offlineConfiguration"], {
      skipLocationChange: true,
    });
  }

  uploadFile() {
    this.contentService.setFileType("OFFLINE_DATA");
    const dialogRef = this.dialog.open(FileUploadDialog, {
      panelClass: "custom-dialog-container",
      width: "40%",
      height: "40%",
      disableClose: true,
      data: {
        type: "OFFLINE_DATA",
        multipleFileAllowed: false,
        header: "Upload Json File",
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.getAllOfflineData();
      }
    });
  }

  edit() {
    this.offlineService.setType("EDIT");
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        toolName: this.selectedOfflineData.toolName,
        queryName: this.selectedOfflineData.queryName,
        cronSchedule: this.selectedOfflineData.cronSchedule,
        cypherQuery: this.selectedOfflineData.cypherQuery,
      },
    };

    this.router.navigate(
      ["InSights/Home/offlineConfiguration"],
      navigationExtras
    );
  }

  refresh() {
    this.selectedIndex = -1;
    this.getAllOfflineData();
    this.onRadioBtnSelect = false;
  }

  enableButtons(event: MatRadioChange, index) {
    this.onRadioBtnSelect = true;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.selectedIndex = index + this.currentPageValue;
  }

  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  applyFilter(filterValue: string) {
    this.offlineDatasource.filter = filterValue.trim();
  }

  changeLastExecution(x: any) {
    return x == undefined ? 0 : x;
  }

  public async getAllOfflineData() {
    var self = this;
    this.offlineList = [];
    this.offlineList = await this.offlineService.loadOfflineList();
    if (this.offlineList != null && this.offlineList.status == "success") {
      this.offlineList.data.sort((a, b) =>
        this.changeLastExecution(a.lastRunTime) >
        this.changeLastExecution(b.lastRunTime)
          ? -1
          : 1
      );
      this.offlineList.data.forEach((offlineData) => {
        if (offlineData.lastRunTime != 0) {
          this.dateObj = new Date(offlineData.lastRunTime * 1000);
          offlineData["lastExecutionTime"] =
            this.dataShare.convertDateToSpecificDateFormat(
              this.dateObj,
              "yyyy-MM-dd HH:mm a"
            );
        }
      });
      this.offlineDatasource.data = this.offlineList.data;

      this.totalPages = Math.ceil(
        this.offlineDatasource.data.length / this.MAX_ROWS_PER_TABLE
      );
      this.offlineDatasource.paginator = this.paginator;
    }
  }

  updateOfflineQueryStatus(event: MatSlideToggleChange, element) {
    var self = this;
    var title = "Execute Offline Query";
    var state = element.isActive ? "Active" : "Inactive";
    var dialogmessage = "Are you sure you want to change offline data status?";
    element.isActive = event.checked;
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      "Status change",
      "DELETE",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var updateStatusConfig = {};
        updateStatusConfig["queryName"] = element.queryName;
        updateStatusConfig["isActive"] = element.isActive;
        this.offlineService
          .updateOfflineConfigStatus(updateStatusConfig)
          .then(function (response) {
            if (response.status === "success") {
              self.messageDialog.openSnackBar(
                "Updated Successfully!",
                "success"
              );
              self.refresh();
            } else {
              self.messageDialog.openSnackBar(
                "Failed to update query. Please try after some time",
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

  deleteOfflineConfig() {
    var self = this;
    let data = self.selectedOfflineData;

    var title = "Delete Offline Query";
    var dialogmessage =
      "Do you want to delete offline query <b>" +
      self.selectedOfflineData.queryName +
      "</b>? <br> <b> Please note: </b> The action of deleting an offline query " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedOfflineData.toolName,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var offlineAPIRequestJson = {};
        offlineAPIRequestJson["queryName"] = self.selectedOfflineData.queryName;

        self.offlineService
          .deleteOfflineData(JSON.stringify(offlineAPIRequestJson))
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

  showDetailsDialog(offlineElement) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(OfflineDetailsComponent, {
        panelClass: "custom-dialog-container",
        height: "45%",
        width: "75%",
        disableClose: true,
        data: {
          element: offlineElement,
        },
      });
    }
  }

  getTitle(element) {
    return cronstrue.toString(element);
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
}
