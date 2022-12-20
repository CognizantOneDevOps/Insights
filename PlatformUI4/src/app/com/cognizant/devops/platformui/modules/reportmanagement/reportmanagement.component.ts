/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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
import { DatePipe } from "@angular/common";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { MatDialog } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatRadioChange } from "@angular/material/radio";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { MatSort } from "@angular/material/sort";
import { MatTableDataSource } from "@angular/material/table";
import { Router, NavigationExtras } from "@angular/router";
import { DataSharedService } from "@insights/common/data-shared-service";
import { ReportManagementService } from "@insights/app/modules/reportmanagement/reportmanagement.service";
import { WorkflowHistoryDetailsDialog } from "@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog";
import { saveAs as importedSaveAs } from "file-saver";

@Component({
  selector: "app-reportmanagement",
  templateUrl: "./reportmanagement.component.html",
  styleUrls: ["./reportmanagement.component.scss", "./../home.module.scss"],
})
export class ReportManagementComponent implements OnInit {
  timeZone: string = "";
  timeZoneAbbr: string = "";
  enableRefresh: boolean = true;
  disableRetry: boolean = true;
  disableStartImmediate: boolean = true;
  configParams: string;
  selectedReport: any;
  CheckboxVar: boolean;
  showPagination: boolean = true;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  MAX_ROWS_PER_TABLE = 6;
  @ViewChild(MatSort) sort: MatSort;
  displayedColumns: string[];
  userDataSourceFromService: any;
  userDataSource = new MatTableDataSource<any>();
  name: string = "PRODUCTIVITY_REPORT";
  detailedRecords = [];
  showThrobber: boolean = false;
  isActive: boolean;
  reoccurence: boolean;
  reportSourceList = { data: [] };
  changeState: boolean;
  dateObj: Date;
  dateObjFormatted: String;
  clicked = new Array();
  disableDelete: boolean = true;
  disableEdit: boolean = true;
  disableDownload: boolean = true;
  executionRecords: any;
  previousActiveIndex = -1;
  pageRefreshed: boolean = false;
  reportdisplayName;
  reponseForReportTemplate: any;
  templatesList = [];
  reponseForschedule: any;
  scheduleList = [];
  selectedIndex: number;
  currentPageIndex: number = -1;
  totalPages: number = -1;

  constructor(
    private dialog: MatDialog,
    public dataShare: DataSharedService,
    private messageDialog: MessageDialogService,
    public router: Router,
    public reportmanagementService: ReportManagementService,
    public datePipe: DatePipe
  ) {
    this.showThrobber = true;
    this.displayedColumns = [
      "select",
      "reportName",
      "schedule",
      "lastRun",
      "nextRun",
      "status",
      "reoccurence",
      "active",
      "details",
    ];
    this.loadReportTemplate();
    this.getAssesmentReport();
    this.userDataSource.paginator = this.paginator;
  }

  async loadReportTemplate() {
    this.reponseForReportTemplate =
      await this.reportmanagementService.getReportTemplate();
    if (
      this.reponseForReportTemplate.data != null &&
      this.reponseForReportTemplate.status == "success"
    ) {
      this.templatesList = this.reponseForReportTemplate.data;
      console.log(this.templatesList);
    } else {
      this.messageDialog.openSnackBar(
        "Failed to load the report templates.Please check logs for more details.",
        "error"
      );
    }
    this.loadSchedule();
  }

  async loadSchedule() {
    this.reponseForschedule = await this.reportmanagementService.getSchedule();
    if (
      this.reponseForschedule != null &&
      this.reponseForschedule.status == "success"
    ) {
      this.scheduleList = this.reponseForschedule.data;
    } else {
      this.messageDialog.openSnackBar(
        "Failed to load the schedules.Please check logs for more details.",
        "error"
      );
    }
  }

  async getAssesmentReport() {
    var UserDetail = {};
    UserDetail["orgName"] = this.dataShare.getOrgName();
    UserDetail["userName"] = this.dataShare.getLoginName();
    this.detailedRecords = [];
    this.userDataSourceFromService =
      await this.reportmanagementService.getAssesmentReport(
        JSON.stringify(UserDetail)
      );
    console.log(this.userDataSourceFromService);
    this.reportSourceList.data = this.userDataSourceFromService.data;
    this.showThrobber = false;
    this.showPagination = true;
    var dataArray = this.userDataSourceFromService.data;
    if (dataArray != undefined) {
      dataArray.forEach((key) => {
        var obj = key;
        if (typeof obj["lastRun"] !== "undefined") {
          if (obj["lastRun"] === "") {
            obj["lastRun"] = "-";
          } else {
            obj["lastRun"] = obj["lastRun"] * 1000;
            this.dateObj = new Date(obj["lastRun"]);
            obj["lastRun"] = this.dataShare.convertDateToSpecificDateFormat(
              this.dateObj,
              "yyyy-MM-dd HH:mm:ss"
            );
          }
        }
        if (typeof obj["nextRun"] !== "undefined") {
          if (obj["nextRun"] === "") {
            obj["nextRun"] = "-";
          } else {
            obj["nextRun"] = obj["nextRun"] * 1000;
            this.dateObj = new Date(obj["nextRun"]);
            obj["nextRun"] = this.dataShare.convertDateToSpecificDateFormat(
              this.dateObj,
              "yyyy-MM-dd HH:mm:ss"
            );
          }
        }
        this.detailedRecords.push(obj);
      });
      this.userDataSource.data = this.detailedRecords;
      this.userDataSource.paginator = this.paginator;
      this.totalPages = Math.ceil(
        this.userDataSource.data.length / this.MAX_ROWS_PER_TABLE
      );

      this.detailedRecords = [];
    }
    this.userDataSource.data.forEach((element) => {
      this.clicked.push(true);
    });
  }

  updateStatus(event: MatSlideToggleChange, id, element) {
    var self = this;
    var title = "Update Active/Inactive State";
    var state = element.isActive ? "Active" : "Inactive";
    var dialogmessage =
      "Are you sure you want to <b>" +
      state +
      "</b> report <b>" +
      self.selectedReport.reportName +
      "</b> ?";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedReport.reportName,
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        this.changeState = event.checked;
        var setStatusRequestJson = {};
        setStatusRequestJson["id"] = id;
        setStatusRequestJson["isActive"] = event.checked;
        this.reportmanagementService
          .setActiveStatus(JSON.stringify(setStatusRequestJson))
          .then(function (data) {
            if (data.status == "success") {
              console.log("status changed");
            } else {
              this.messageDialog.openSnackBar(
                "Failed to update state.Please check logs for more details.",
                "error"
              );
            }
          });
      } else {
        element.isActive = !event.checked;
      }
    });
  }

  sendMail(){
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        source:"Report",
      },
    };
    this.router.navigate(["InSights/Home/email-configuration"], navigationExtras)
  }

  Refresh() {
    this.selectedIndex = -1;
    this.selectedReport = "";
    this.clicked = [];
    this.userDataSource.data.forEach((element) => {
      this.clicked.push(true);
    });
    this.pageRefreshed = true;
    this.disableDelete = true;
    this.disableEdit = true;
    this.disableStartImmediate = true;
    this.disableDownload = true;
    this.list();
  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), "");
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.userDataSource.paginator = this.paginator;
    this.userDataSource.data.forEach((element) => {
      this.clicked.push(true);
    });
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  ngAfterViewInit() {
    this.userDataSource.paginator = this.paginator;
  }

  editReportConfiguration() {
    if (this.selectedReport === undefined) {
      this.messageDialog.openSnackBar(
        "Please select a record to edit",
        "error"
      );
      return;
    }
    const paramCheck = this.reportSourceList.data.filter(
      (f) => f.reportName === this.selectedReport.reportName
    );
    const param = paramCheck.length > 0 ? paramCheck[0] : {};
    this.configParams = JSON.stringify({ type: "edit", data: param });
    console.log("Inside ReportManagement:",this.configParams);
    this.naivagate();
  }

  addReport() {
    this.configParams = JSON.stringify({ type: "save" });
    console.log(" config param", this.configParams);
    this.naivagate();
  }

  deleteReport() {
    var self = this;
    var title = "Delete Report";
    var dialogmessage =
      "Do you want to delete <b>" +
      self.selectedReport.reportName +
      "</b> ? <br> <b> Please note: </b> The action of deleting " +
      "<b>" +
      self.selectedReport.reportName +
      "</b> cannot be undone.<br> Do you want to continue ? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedReport.reportName,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        self.reportmanagementService
          .deleteAssesmentReport(self.selectedReport.configId)
          .then(function (data) {
            if (data.status == "success") {
              self.messageDialog.openSnackBar(
                "<b>" +
                  self.selectedReport.reportName +
                  "</b> deleted successfully.",
                "success"
              );
              self.Refresh();
            } else if (data.message == "Executions found in history") {
              self.messageDialog.openSnackBar(
                "Report cannot be deleted as executions were found in the history.You can change the state to INACTIVE if required.",
                "error"
              );
            } else {
              self.messageDialog.openSnackBar(
                "Unable to delete report.Please check logs.",
                "error"
              );
            }
          })
          .catch(function (data) {});
      }
    });
  }

  list() {
    this.getAssesmentReport();
  }

  naivagate() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        reportparam: this.configParams,
        reportTemplates: JSON.stringify(this.templatesList),
        scheduleList: JSON.stringify(this.scheduleList),
      },
    };
    this.router.navigate(
      ["InSights/Home/report-configuration"],
      navigationExtras
    );
    
  }

  showWorkflowHistoryDetailsDialog(reportName: String, workflowId: String) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(WorkflowHistoryDetailsDialog, {
        disableClose: true,
        panelClass: "custom-dialog-container",
        height: "1170px",
        width: "550px",
        data: {
          reportName: reportName,
          workflowId: workflowId,
          timeZone: this.timeZone,
        },
      });
    }
  }

  radioChange(event: MatRadioChange, index) {
    this.selectedIndex = index + this.currentPageIndex;
    this.disableDelete = false;
    this.disableEdit = false;
    this.disableStartImmediate = false;
    if (this.previousActiveIndex == -1) {
      this.clicked[index] = false;
      this.previousActiveIndex = index;
    } else if (this.pageRefreshed && index === this.previousActiveIndex) {
      this.clicked[this.previousActiveIndex] = false;
      this.pageRefreshed = false;
    } else {
      this.clicked[index] = false;
      this.clicked[this.previousActiveIndex] = true;
      this.previousActiveIndex = index;
    }
    if (event.value.status == "ABORTED") {
      this.disableRetry = false;
    } else {
      this.disableRetry = true;
    }
    if (
      event.value.status == "NOT_STARTED" ||
      event.value.status == "RESTART"
    ) {
      this.disableStartImmediate = false;
    } else {
      this.disableStartImmediate = true;
    }
    if (
      event.value.status == "NOT_STARTED" ||
      event.value.status == "IN_PROGRESS"
    ) {
      this.disableDownload = true;
    } else {
      this.disableDownload = false;
    }
  }

  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }

  retry() {
    var statusRequestJson = {};
    statusRequestJson["configId"] = this.selectedReport.configId;
    statusRequestJson["status"] = "RESTART";
    var message = "Status has been updated to RESTART.";
    this.updateReportStatus(statusRequestJson, message);
  }

  startImmediate() {
    var statusRequestJson = {};
    console.log(
      "runimmediate for " +
        this.selectedReport.reportName +
        "   " +
        this.selectedReport.runimmediate
    );
    if (!this.selectedReport.runimmediate) {
      statusRequestJson["configId"] = this.selectedReport.configId;
      statusRequestJson["runimmediate"] = true;
      var message =
        "Report has been scheduled successfully. Execution will start within 5 min."; //to
      var title = "Start Report Execution";
      var dialogmessage =
        "Do you want to execute <b>" +
        this.selectedReport.reportName +
        "</b> immediately ? ";
      const dialogRefStatus = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        this.selectedReport.reportName,
        "ALERT",
        "31%"
      );
      dialogRefStatus.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.updateReportStatus(statusRequestJson, message);
        }
      });
    } else {
      this.messageDialog.showApplicationsMessage(
        "Report already scheduled to run immediately",
        "WARN"
      );
    }
  }

  private updateReportStatus(statusRequestJson: {}, message: string) {
    var self = this;
    this.reportmanagementService
      .setRetryStatus(JSON.stringify(statusRequestJson))
      .then(function (data) {
        if (data.status == "success") {
          self.messageDialog.openSnackBar(message, "success");
          self.list();
        } else {
          self.messageDialog.openSnackBar(
            "Failed to update the report state.Please check logs for more details String.",
            "error"
          );
        }
      });
  }

  async validationForPDF() {
    var self = this;
    var configIdJson = {};
    configIdJson["configid"] = self.selectedReport.configId;
    self.executionRecords =
      await self.reportmanagementService.getPDFExecutionId(
        JSON.stringify(configIdJson)
      );
    if (
      self.executionRecords != null &&
      self.executionRecords.status == "success"
    ) {
      let executionRecordsData = self.executionRecords.data;
      if (executionRecordsData.length == 0) {
      } else {
        let status = executionRecordsData.status;
        let executionid = executionRecordsData.executionId;
        let workflowid = executionRecordsData.workflowId;
        if (!status) {
          if (executionid == -1) {
            self.messageDialog.openSnackBar("No PDF found.", "error");
          } else {
            var title = "Download PDF";
            var dialogmessage =
              "Current report generation is in <b>" +
              this.selectedReport.status +
              "</b> state. Do you want to download last generated report?";
            const dialogRef = self.messageDialog.showConfirmationMessage(
              title,
              dialogmessage,
              this.selectedReport.reportName,
              "ALERT",
              "30%"
            );
            dialogRef.afterClosed().subscribe((result) => {
              if (result == "yes") {
                self.downloadPDF(executionid, workflowid);
              } else {
              }
            });
          }
        } else {
          self.downloadPDF(executionid, workflowid);
        }
      }
    }
  }

  downloadPDF(executionid, workflowid) {
    var PDFRequestJson = {};
    var pdfFileName = this.selectedReport.reportName + ".pdf";
    PDFRequestJson["pdfName"] = this.selectedReport.reportName;
    PDFRequestJson["executionId"] = executionid;
    PDFRequestJson["workflowId"] = workflowid;
    var request = btoa(JSON.stringify(PDFRequestJson));
    console.log(request);
    this.reportmanagementService.downloadPDF(request).then(function (data) {
      console.log(data);
      importedSaveAs(data, pdfFileName);
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
