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
import { Component, OnInit, ViewChild } from "@angular/core";
import { Router, NavigationExtras } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "../../application-dialog/message-dialog-service";
import { ReportManagementService } from "../../reportmanagement/reportmanagement.service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { OutcomeService } from "../outcome.service";
import { OutcomeProvider } from "../outcome.provider";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { Sort } from "@angular/material/sort";
import { InsightsUtilService } from "@insights/common/insights-util.service";

@Component({
  selector: "app-outcome-list",
  templateUrl: "./outcome-list.component.html",
  styleUrls: ["./outcome-list.component.scss", "./../../home.module.scss"],
})
export class OutcomeListComponent implements OnInit {
  displayedColumns = [];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  outcomeDatasource = new MatTableDataSource<any>();
  enableEdit: boolean = false;
  onRadioBtnSelect: boolean = false;
  dashConfigList: any;
  data: any[];
  kpiId: number;
  kpiName: string;
  toolname: string;
  groupName: string;
  category: string;
  refreshRadio: boolean = false;
  outcome: any;
  showConfirmMessage: string;
  type: string;
  enableRefresh: boolean = false;
  MAX_ROWS_PER_TABLE = 6;
  orgArr = [];
  orgName: any;
  isDatainProgress: boolean = false;
  disablebutton = [];
  timeZone: string = "";
  count: number;
  selectedIndex: -1;
  currentPageValue: number;
  outcomeList : any = {};

  constructor(
    public messageDialog: MessageDialogService,
    public dataShare: DataSharedService,
    public router: Router,
    public dialog: MatDialog,
    public reportmanagementService: ReportManagementService,
    private outcomeService: OutcomeService,
    private outcomeProvider: OutcomeProvider,
    public insightsUtil : InsightsUtilService
  ) {}

  ngOnInit() {
    this.getAllConfig();
    this.displayedColumns = [
      "radio",
      "outcomeName",
      "outcomeType",
      "toolName",
      "isActive",
    ];
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  ngAfterViewInit() {
    this.outcomeDatasource.paginator = this.paginator;
  }

  add() {
    this.outcomeService.iconClkSubject.next("CLICK");
    this.router.navigate(["InSights/Home/outcome"], {
      skipLocationChange: true,
    });
  }
  edit() {
    this.outcomeService.iconClkSubject.next("CLICK");
    let outcomeJson = this.outcome;
    let config =
      outcomeJson.configJson != null ? JSON.parse(outcomeJson.configJson) : {};
    console.log(outcomeJson);
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        id: outcomeJson.id,
        outcomeName: outcomeJson.outcomeName,
        outcomeType: outcomeJson.outcomeType,
        toolName: outcomeJson.insightsTools.toolName,
        toolId: outcomeJson.insightsTools.id,
        category: outcomeJson.insightsTools.category,
        toolQueue: outcomeJson.insightsTools.agentCommunicationQueue,
        toolStatus: outcomeJson.insightsTools.isActive,
        isActive: outcomeJson.isActive,
        toolConfigJson: JSON.parse(outcomeJson.insightsTools.toolConfigJson),
        newRelicAppId: config.newRelicAppId,
        splunkIndex: config.splunkIndex,
        metricName: config.metricName,
        metricKey: config.metricKey,
        logKey: config.logKey,
        createdDate: outcomeJson.createdDate,
        metricUrl: outcomeJson.metricUrl,
        parameters: outcomeJson.requestParameters,
      },
    };
    this.outcomeProvider.storage = outcomeJson.insightsTools.toolConfigJson;
    this.router.navigate(["InSights/Home/editOutcome"], navigationExtras);
  }
  refresh() {
    this.selectedIndex = -1;
    this.getAllConfig();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;
  }
  enableButtons(i) {
    this.onRadioBtnSelect = true;
    this.disablebutton[i] = false;
    this.selectedIndex = i + this.currentPageValue;
  }

  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  async getAllConfig() {
    this.isDatainProgress = true;
    var self = this;
    self.refreshRadio = false;
    this.outcomeList = await this.outcomeService.loadOutcomeList();
    if (this.outcomeList.status === "success") {
      this.count = 0;
      this.outcomeDatasource.data = this.outcomeList.data;
    }
    this.isDatainProgress = false;
    this.outcomeDatasource.paginator = this.paginator;
  }

  applyFilter(filterValue: string) {
    this.outcomeDatasource.filter = filterValue.trim();
  }

  list() {
    this.getAllConfig();
  }

  delete() {
    var self = this;
    let data = self.outcome;

    var title = "Delete Outcome";
    var dialogmessage =
      "Do you want to delete a Outcome <b>" +
      "</b>? <br><br> <b> Please note: </b> The action of deleting a Outcome " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.outcome.id,
      "DELETE",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        self.outcomeService
          .deleteOutcome(JSON.stringify(self.outcome.id))
          .then(function (data) {
            console.log(data);
            if (data.status === "success") {
              self.messageDialog.openSnackBar(
              "Deleted Successfully",
                "success"
              );
              self.list();
            } else if (data.status == "failure" && data.message != null) {
              self.messageDialog.openSnackBar(data.message, "error");
            } else {
              self.messageDialog.openSnackBar(
                "Failed to delete Please check logs for details.",
                "error"
              );
            }
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }

  updateStatus(event: MatSlideToggleChange, element) {
    console.log(element);
    var self = this;
    var title = "Update Active/Inactive State";
    var state = element.isActive ? "Active" : "Inactive";
    var dialogmessage = "Are you sure you want to change outcome status?";
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
        updateStatusConfig["id"] = element.id;
        updateStatusConfig["isActive"] = element.isActive;
        this.outcomeService
          .updateOutcomeConfigStatus(updateStatusConfig)
          .then(function (response) {
            console.log(response);
            if (response.status === "success") {
              self.messageDialog.openSnackBar(
                "Updated Successfully",
                "success"
              );
              self.getAllConfig();
            } else {
              self.messageDialog.openSnackBar(
                "Failed to update state. Please check logs for more details.",
                "error"
              );
            }
          });
      } else {
        element.isActive = !event.checked;
      }
    });
  }

  goToNextPage() {
    this.paginator.nextPage();
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }
  goToPrevPage() {
    this.paginator.previousPage();
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  sortData(sort: Sort) {
    const data = this.outcomeList.data.slice();
    if (!sort.active || sort.direction === '') {
      this.outcomeDatasource.data = data;
      return;
    }

    this.outcomeDatasource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      if(sort.active === "toolName")
        return this.insightsUtil.compare(a.insightsTools.toolName, b.insightsTools.toolName, isAsc)
      else
        return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc)
    });
    this.outcomeDatasource.paginator = this.paginator;
  }
}
