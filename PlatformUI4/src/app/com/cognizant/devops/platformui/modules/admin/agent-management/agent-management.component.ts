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
import { AgentService } from "@insights/app/modules/admin/agent-management/agent-management-service";
import { SelectionModel } from "@angular/cdk/collections";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatSelect } from "@angular/material/select";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import { ClipboardService } from "ngx-clipboard";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { HealthCheckService } from "@insights/app/modules/healthcheck/healthcheck.service";
import { ShowDetailsDialog } from "@insights/app/modules/healthcheck/healthcheck-show-details-dialog";
import { AgentDownloadDialogComponent } from "./agent-download-dialog/agent-download-dialog.component";

@Component({
  selector: "app-agent-management",
  templateUrl: "./agent-management.component.html",
  styleUrls: ["./agent-management.component.scss", "./../../home.module.scss"],
})
export class AgentManagementComponent implements OnInit {
  validationArr = {};
  showConfirmMessage: string;
  showList: boolean = false;
  showThrobber: boolean;
  showMessage: string;
  data = [];
  displayedColumns = [];
  tableParams = [];
  buttonDisableStatus: boolean = true;
  runDisableStatus: string = "";
  agentListDatasource = new MatTableDataSource<any>();
  showDetail: boolean = false;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  agentList: any;
  selectedAgent: any;
  selectTool: any;
  agentNameList: any = [];
  agentparameter = {};
  receivedParam: any;
  toolVersionData: any;
  versionList = [];
  MAX_ROWS_PER_TABLE = 5;
  isCopyLinkDisabled = true;
  timeZone: string = "";
  timeZoneAbbr: string = "";
  selectedIndex: number;
  currentPageValue: number;
  currentPageIndex: number = -1;
  totalPages: number = -1;

  constructor(
    public agentService: AgentService,
    public router: Router,
    private route: ActivatedRoute,
    public dialog: MatDialog,
    public messageDialog: MessageDialogService,
    private changeDetectorRefs: ChangeDetectorRef,
    private dataShare: DataSharedService,
    private _clipboardService: ClipboardService,
    public initConfig: InsightsInitService,
    private healthCheckService: HealthCheckService
  ) {
    this.getRegisteredAgents();
  }

  ngOnInit() {
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.timeZone = this.dataShare.getTimeZone();
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.route.queryParams.subscribe((params) => {
      if (params["agentstatus"] != undefined) {
        this.receivedParam = params["agentstatus"];
        var agentConfigstatusCode = params["agentConfigstatusCode"];
        var showConfirmMessage = this.receivedParam;
        if (agentConfigstatusCode == undefined) {
          agentConfigstatusCode = "WARN";
        }
        setTimeout(() =>
          this.messageDialog.openSnackBar(
            showConfirmMessage,
            agentConfigstatusCode
          )
        );
      }
    });
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  ngAfterViewInit() {
    this.agentListDatasource.paginator = this.paginator;
  }

  public async getRegisteredAgents() {
    var self = this;
    self.isCopyLinkDisabled = true;
    self.showList = false;
    self.showThrobber = true;
    self.buttonDisableStatus = true;
    self.runDisableStatus = "";
    this.agentNameList = [];
    this.agentList = await self.agentService.loadAgentServices(
      "DB_AGENTS_LIST"
    );
    if (this.agentList != null && this.agentList.status == "success") {
      this.agentListDatasource.data = this.agentList.data.sort(
        (a, b) => a.toolName > b.toolName
      );
      this.agentListDatasource.paginator = this.paginator;
      this.agentNameList.push("All");
      for (var data of this.agentList.data) {
        if (this.agentNameList.indexOf(data.toolName) == -1) {
          this.agentNameList.push(data.toolName);
        }
      }
      self.showDetail = true;
      this.displayedColumns = [
        "radio",
        "ToolName",
        "ToolCategory",
        "AgentKey",
        "Type",
        "OS",
        "Version",
        "Status",
        "Time",
        "CurrentStatus",
        "Details",
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
      this.agentListDatasource.data.length / this.MAX_ROWS_PER_TABLE
    );
    self.selectTool = " ";
  }
  private consolidatedArr(detailArr): void {
    var self = this;
    this.validationArr = {};
    for (var i = 0; i < detailArr.length; i++) {
      this.validationArr[i] = {
        os: detailArr[i].osVersion,
        version: detailArr[i].agentVersion,
        tool: detailArr[i].toolName,
      };
    }
  }
  selectToolAgent(toolSelect) {
    var agentListDatasourceSelected = [];
    if (toolSelect != "All") {
      this.agentList.data.filter((av) => {
        if (av.toolName == toolSelect) {
          agentListDatasourceSelected.push(av);
        }
      });
    } else {
      console.log(this.agentList);
      agentListDatasourceSelected = this.agentList.data.sort(
        (a, b) => a.toolName > b.toolName
      );
    }
    this.agentListDatasource.data = agentListDatasourceSelected;
    this.agentListDatasource.paginator = this.paginator;
    this.totalPages = Math.ceil(
      this.agentListDatasource.filteredData.length / this.MAX_ROWS_PER_TABLE
    );
    if (this.totalPages < this.currentPageIndex) {
      this.currentPageIndex = this.paginator.pageIndex;
    } else {
      this.currentPageIndex = this.paginator.pageIndex + 1;
    }
    this.changeDetectorRefs.detectChanges();
  }

  statusEdit(element, index) {
    this.selectedIndex = index + this.currentPageValue;
    this.runDisableStatus = element.agentStatus;
    this.buttonDisableStatus = false;
    if (element.iswebhook) {
      this.isCopyLinkDisabled = false;
    } else {
      this.isCopyLinkDisabled = true;
    }
  }

  agentStartStopAction(actType): void {
    var self = this;
    if (this.selectedAgent == undefined) {
      this.showConfirmMessage = "other";
      self.showMessage = "Please select Agent";
    } else {
      self.agentService
        .agentStartStop(
          this.selectedAgent.agentKey,
          self.selectedAgent.toolName,
          self.selectedAgent.osVersion,
          actType
        )
        .then(function (data) {
          if (actType == "START") {
            if (data.status == "success") {
              self.messageDialog.openSnackBar(
                "Agent Started Successfully",
                "success"
              );
            } else {
              self.messageDialog.openSnackBar(
                "Agent Start failed,Please try again later",
                "error"
              );
            }
          } else {
            if (data.status == "success") {
              self.messageDialog.openSnackBar(
                "Agent Stopped Successfully",
                "success"
              );
            } else {
              self.messageDialog.openSnackBar(
                "Agent Stop failed,Please try again later",
                "error"
              );
            }
          }

          self.getRegisteredAgents();
        })
        .catch(function (data) {
          self.showConfirmMessage = "service_error";
          self.getRegisteredAgents();
        });
    }
  }

  async addAgentData() {
    this.consolidatedArr(this.agentListDatasource);
    this.agentparameter = JSON.stringify({
      type: "new",
      detailedArr: this.validationArr,
    });
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        agentparameter: this.agentparameter,
      },
    };
    this.router.navigate(
      ["InSights/Home/agentconfiguration"],
      navigationExtras
    );
  }

  async editAgent() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.agentparameter = JSON.stringify({
        type: "update",
        detailedArr: this.selectedAgent,
      });
      let navigationExtras: NavigationExtras = {
        skipLocationChange: true,
        queryParams: {
          agentparameter: this.agentparameter,
        },
      };
      this.router.navigate(
        ["InSights/Home/agentconfiguration"],
        navigationExtras
      );
    }
  }

  uninstallAgent() {
    var self = this;
    if (self.selectedAgent.agentStatus == "STOP") {
      var title = "Delete Agent";
      var dialogmessage =
        "Note: Uninstalling the Agent doesn't delete the data that has been collected. The agent could be re-registered again, and the data collection would be resumed from the last run time. <br> <br> Do you want to uninstall <b> " +
        self.selectedAgent.toolName +
        " </b> on <b>" +
        self.selectedAgent.osVersion +
        " </b> ? ";
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        this.selectedAgent.toolName,
        "DELETE",
        "40%"
      );

      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          self.agentService
            .agentUninstall(
              self.selectedAgent.agentKey,
              self.selectedAgent.toolName,
              self.selectedAgent.osVersion
            )
            .then(function (data) {
              if (data.status === "success") {
                self.messageDialog.openSnackBar(
                  "<b> Successfully uninstalled Agent </b>",
                  "success"
                );
                self.getRegisteredAgents();
              }
              if (data.status === "failure") {
                self.messageDialog.openSnackBar(
                  "<b>" + data.message + "</b>",
                  "error"
                );
                self.getRegisteredAgents();
              }
            })
            .catch(function (data) {
              self.showConfirmMessage = "service_error";
              self.getRegisteredAgents();
            });
        }
      });
    } else {
      self.messageDialog.showApplicationsMessage(
        "Please stop the Agent before uninstalling!",
        "WARN"
      );
    }
  }

  copyInputMessage(inputElement) {
    var hostname = this.initConfig.getWebhookHost();
    var value_to_copy =
      hostname +
      "/PlatformInsightsWebHook/insightsDevOpsWebHook?webHookName=" +
      inputElement.agentKey;
    console.log(value_to_copy);
    this._clipboardService.copyFromContent(value_to_copy);
  }

  // Displays Show Details dialog box when Details column is clicked
  showDetailsDialog(toolName: string, categoryName: string, agentId: string) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var rcategoryName = categoryName.replace(/ +/g, "");
      var rtoolName =
        toolName.charAt(0).toUpperCase() + toolName.slice(1).toLowerCase();
      var filePath =
        "${INSIGHTS_HOME}/logs/PlatformAgent/log_" + agentId + ".log";
      var detailType = rtoolName;
      let showDetailsDialog = this.dialog.open(ShowDetailsDialog, {
        panelClass: "custom-dialog-container",
        height: "85%",
        width: "85%",
        disableClose: true,
        data: {
          toolName: toolName,
          categoryName: categoryName,
          pathName: filePath,
          detailType: detailType,
          agentId: agentId,
          timeZone: this.timeZone,
        },
      });
    } else {
    }
  }
  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  goToNextPage() {
    this.selectedIndex = -1;
    this.paginator.nextPage();
    if (this.totalPages === 0) {
      this.currentPageIndex = 0;
    } else {
      this.currentPageIndex = this.paginator.pageIndex + 1;
    }
  }
  goToPrevPage() {
    this.selectedIndex = -1;
    this.paginator.previousPage();
    if (this.totalPages === 0) {
      this.currentPageIndex = 0;
    } else {
      this.currentPageIndex = this.paginator.pageIndex + 1;
    }
  }

  showAgentDownloadDialog() {
    let isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      let showDetailsDialog = this.dialog.open(AgentDownloadDialogComponent, {
        panelClass: "custom-dialog-container",
        height: "35%",
        width: "40%",
        disableClose: true,
        data: {},
      });
    }
  }
}
