/*******************************************************************************
 * Copyright 2019 Cognizant Technology Solutions
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
import { Component, OnInit, ViewChild, ChangeDetectorRef } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { ModelManagementService } from "@insights/app/modules/model-management/model-management.service";
import { Router, NavigationExtras } from "@angular/router";
import { DataSharedService } from "@insights/common/data-shared-service";
import { WorkflowHistoryDetailsDialog } from "@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog";
import { MatRadioChange } from "@angular/material/radio";

@Component({
  selector: "app-model-management",
  templateUrl: "./model-management.component.html",
  styleUrls: ["./model-management.component.scss", "../home.module.scss"],
})
export class ModelManagementComponent implements OnInit {
  toDelete: string = null;
  UsecaseListDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  displayedColumns = [];
  timeZone: string = "";
  timeZoneAbbr: string = "";
  usecaseList: any;
  dateObj: Date;
  updatedData = [];
  MAX_ROWS_PER_TABLE = 6;
  enableRetry: boolean = false;
  enableLeaderboard: boolean = false;
  enableDelete: boolean = false;
  selectedUsecase: any;
  table;
  tbody;
  columnDefs = [
    { headerName: "Make", field: "make" },
    { headerName: "Model", field: "model" },
    { headerName: "Price", field: "price" },
  ];

  rowData = [
    { make: "Toyota", model: "Celica", price: 35000 },
    { make: "Ford", model: "Mondeo", price: 32000 },
    { make: "Porsche", model: "Boxter", price: 72000 },
  ];
  selectedIndex: number;
  totalPages: number = -1;
  currentPageIndex: number = -1;
  currentPageValue: number= -1;

  constructor(
    public messageDialog: MessageDialogService,
    public modelManagementService: ModelManagementService,
    private dataShare: DataSharedService,
    private changeDetectorRefs: ChangeDetectorRef,
    public router: Router,
    private dialog: MatDialog
  ) {
    this.displayedColumns = [
      "radio",
      "UsecaseName",
      "PredictionType",
      "PredictionColumn",
      "ModelName",
      "SplitRatio",
      "Created",
      "Status",
      "active",
      "details",
    ];
    this.getModelDetails();
    this.table = document.getElementsByTagName("table")[0];
    this.tbody = this.table.getElementsByTagName("tbody")[0];
  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), "");
    //doStuff
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  redirectToPrection(event) {
    let data = event;
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        usecase: event.usecase,
      },
    };
    this.router.navigate(["InSights/Home/prediction"], navigationExt);
  }
  ngAfterViewInit() {
    this.UsecaseListDatasource.paginator = this.paginator;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  add() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {},
    };
    this.router.navigate(["InSights/Home/mlwizard"], navigationExtras);
  }
  async getModelDetails() {
    this.updatedData = [];
    this.selectedIndex = -1;
    this.usecaseList = await this.modelManagementService.loadUsecaseDetails();
    console.log(this.usecaseList);

    if (this.usecaseList != null && this.usecaseList.status == "success") {
      this.UsecaseListDatasource.data = this.usecaseList.data.usecases.sort(
        (a, b) => a.usecaseName > b.usecaseName
      );
      var dataArray = this.UsecaseListDatasource.data;
      if (dataArray != undefined) {
        dataArray.forEach((key) => {
          if (typeof key.createdAt !== undefined) {
            if (key.createdAt === 0) {
              key.createdAt = "-";
            } else {
              this.dateObj = new Date(key.createdAt);
              key.createdAt = this.dataShare.convertDateToSpecificDateFormat(
                this.dateObj,
                "yyyy-MM-dd"
              );
            }
          }
          if (typeof key.updatedAt !== undefined) {
            if (key.updatedAt === 0) {
              key.updatedAt = "-";
            } else {
              this.dateObj = new Date(key["updatedAt"]);
              key["updatedAt"] = this.dataShare.convertDateToSpecificDateFormat(
                this.dateObj,
                "yyyy-MM-dd"
              );
            }
          }
          if (key.modelName == "") {
            key.modelName = "-";
          }
          this.updatedData.push(key);
        });
        this.totalPages = Math.ceil(
          this.UsecaseListDatasource.data.length / this.MAX_ROWS_PER_TABLE
        );
        this.UsecaseListDatasource.data = this.updatedData;
        this.UsecaseListDatasource.paginator = this.paginator;
        this.updatedData = [];
      }
    } else if (this.usecaseList.StatusCode == "204") {
      this.UsecaseListDatasource.data = this.updatedData;
      this.UsecaseListDatasource.paginator = this.paginator;
      this.messageDialog.openSnackBar(
        "No Usecase found, Please click on <b>+</b> icon to add Usecase.",
        "error"
      );
    } else {
      this.messageDialog.openSnackBar(
        "Something wrong with Service.Please try again.",
        "error"
      );
    }
  }

  setUsecase(ev: string) {
    this.toDelete = ev;
    console.log(this.toDelete);
  }

  onDelete() {
    var dialog;
    const dialogRef = this.messageDialog.showConfirmationMessage(
      "Delete",
      "This action will delete the usecase <b>" +
        this.selectedUsecase.usecaseName +
        "</b> along with all related files (MOJOs, csv etc.) and cannot be reverted. Would you like to continue?",
      this.toDelete,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        console.log("Delete the usecase: ", this.selectedUsecase.usecaseName);
        this.modelManagementService
          .deleteUsecase(this.selectedUsecase.usecaseName)
          .subscribe((event) => {
            console.log(event);
            if (event.status == "success" && event.data.statusCode == 1) {
              dialog = this.messageDialog.openSnackBar(
                "The usecase <b>" +
                  this.selectedUsecase.usecaseName +
                  "</b> and uploaded csv file both has been deleted succesfully.",
                "success"
              );
              this.refresh();
            } else if (
              event.status == "success" &&
              event.data.statusCode == 0
            ) {
              dialog = this.messageDialog.openSnackBar(
                "The usecase <b>" +
                  this.selectedUsecase.usecaseName +
                  "</b> has been deleted succesfully but failed to delete uploaded csv file",
                "success"
              );
              this.refresh();
            } else if (event.status == "failure" && event.StatusCode == 409) {
              this.messageDialog.openSnackBar(event.message, "error");
            } else {
              this.messageDialog.openSnackBar(
                event.message + ". Please try again",
                "error"
              );
            }
          });
      }
    });
  }

  refresh() {
    this.selectedIndex = -1;
    this.getModelDetails();
    this.enableDelete = false;
    this.selectedUsecase = "";
  }

  onSelect(selected, event: MatRadioChange, index) {
    this.enableDelete = true;
    this.selectedIndex = index + this.currentPageValue;
    if (selected.status == "LEADERBOARD_READY") {
      this.enableLeaderboard = true;
    } else {
      this.enableLeaderboard = false;
    }
  }

  navigateToLeaderboard() {
    let navigationExt: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        usecase: this.selectedUsecase.usecaseName,
        targetColumn: this.selectedUsecase.predictionColumn,
        predictionType: this.selectedUsecase.predictionType,
      },
    };
    this.router.navigate(["InSights/Home/prediction"], navigationExt);
  }

  showWorkflowHistoryDetailsDialog(usecaseName: String, workflowId: String) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(WorkflowHistoryDetailsDialog, {
        panelClass: "custom-dialog-container",
        height: "75%",
        width: "75%",
        disableClose: true,
        data: {
          reportName: usecaseName,
          workflowId: workflowId,
          timeZone: this.timeZone,
        },
      });
    }
  }

  updateUsecaseState(event: MatSlideToggleChange, name, element) {
    var self = this;
    var dialog;
    var statusUpdateJson = {};
    var title = "Update Active/Inactive State";
    var state = element.isActive ? "Active" : "Inactive";
    var dialogmessage =
      "Are you sure you want to make <b>" +
      element.usecaseName +
      "</b> state to <b>" +
      state +
      "</b> ?";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      element.usecaseName,
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        statusUpdateJson["usecaseName"] = name;
        statusUpdateJson["isActive"] = event.checked;
        console.log(statusUpdateJson);
        self.modelManagementService
          .usecaseStateUpdate(JSON.stringify(statusUpdateJson))
          .then(function (response) {
            if (response.status == "success") {
              dialog = self.messageDialog.openSnackBar(
                "Status updated successfully.",
                "SUCCESS"
              );
            } else if (
              response.status == "failure" &&
              response.message == "Usecase does not exists in database."
            ) {
              dialog = self.messageDialog.openSnackBar(
                "Usecase <b>" + name + "</b> does not exists in database.",
                "ERROR"
              );
            } else if (
              response.status == "failure" &&
              response.message ==
                "Usecase cannot be deactivated as it is attached to kpi."
            ) {
              dialog = self.messageDialog.openSnackBar(
                response.message,
                "ERROR"
              );
            } else {
              dialog = self.messageDialog.openSnackBar(
                "Failed to update state. Please check logs for more details.",
                "ERROR"
              );
            }
            dialog.afterClosed().subscribe((result) => {
              self.refresh();
            });
          });
      } else {
        element.isActive = !event.checked;
      }
    });
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
