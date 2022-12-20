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
import { Component, OnInit } from "@angular/core";
import { MatDatepickerInputEvent } from "@angular/material/datepicker";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { DataSharedService } from "@insights/common/data-shared-service";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { ViewKPIDialog } from "@insights/app/modules/reportmanagement/report-configuration/view-kpi-dialog";
import { AddTasksDialog } from "@insights/app/modules/reportmanagement/report-configuration/add-task";
import { ReportManagementService } from "@insights/app/modules/reportmanagement/reportmanagement.service";
import { DataArchivingService } from "@insights/app/modules/settings/dataarchiving/dataarchiving-service";
import { EmailConfigurationDialog } from "@insights/app/modules/reportmanagement/report-configuration/email-configuration-dialog";
import { DashboardPreviewConfigDialog } from "@insights/app/modules/dashboard-pdf-download/dashboard-preview-configuration-dialog";
import { MileStoneService } from "@insights/app/modules/mile-stone/mile-stone.service";

@Component({
  selector: "app-report-configuration",
  templateUrl: "./report-configuration.component.html",
  styleUrls: ["../reportmanagement.component.scss", "./../../home.module.scss"],
})
export class ReportConfigComponent implements OnInit {
  startDateInput: Date = null;
  endDateInput: Date = null;
  showThrobber: boolean = false;
  startdate: String = null;
  enddate: String;
  disableRefresh: boolean = false;
  reportName: string = "";
  datasource: string = "";
  today = new Date();
  reportdisplayName: string = "";
  selectedReport: any = { reportId: 0, description: "" };
  disableInputFields: boolean = false;
  schedule: string;
  configParams: string;
  isReoccuring: boolean = false;
  reponseForschdeule: any;
  listOfSchedule = [];
  listOfReports = [];
  receivedParam: any;
  reponseForReportTemplate: any;
  customeScheduleSelected: boolean = false;
  isUpdate: boolean;
  templateName: string;
  taskidInOrder = [];
  viewListDisabled: boolean = true;
  taskListTobeSaved = [];
  enablecanel: boolean = false;
  enableadd: boolean = true;
  showStartDate: boolean = false;
  regex = new RegExp("^[a-zA-Z0-9_]*$");
  activeDataArchivalRecordsResponse: any;
  activeDataArchivalRecords: any;
  dataSourceList = [];
  frequencyPlaceholder: string = "Select frequency";
  inputDataSourcePlaceholder: string = "Select datasource";
  emailDetails: any = null;
  selectedReportId: number;
  enableEmailDetails: boolean = false;
  emailReg = "email";
  receivedReportTemplates = [];
  receivedScheduleList = [];
  selectedMilestone: any = { milestoneId: 0, id: 0 };
  isROITemplate: boolean = false;
  mileStoneName: any = null;
  responseForMilestone: any;
  listOfMilestones = [];
  templateType: string;


  constructor(
    private route: ActivatedRoute,
    private dataShare: DataSharedService,
    private dialog: MatDialog,
    public messageDialog: MessageDialogService,
    public router: Router,
    public reportmanagementservice: ReportManagementService,
    public milestoneservice: MileStoneService,
    private dataArchivingService: DataArchivingService
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.receivedParam = JSON.parse(params["reportparam"]);
      console.log("RecievedParams :", this.receivedParam);
      this.receivedReportTemplates = JSON.parse(params["reportTemplates"]);
      this.receivedScheduleList = JSON.parse(params["scheduleList"]);
      this.receivedReportTemplates = JSON.parse(params["reportTemplates"]);
      this.receivedScheduleList = JSON.parse(params["scheduleList"]);
    });
    this.initializeVariable();
  }

  ngAfterViewInit() { }

  async initializeVariable() {
    if (this.receivedParam.type == "edit") {
      this.disableRefresh = false;
      this.enablecanel = true;
      this.enableadd = false;
      this.disableInputFields = true;
      this.isUpdate = true;
      this.isReoccuring = this.receivedParam.data.isReoccuring;
      this.reportName = this.receivedParam.data.reportName;
      if (this.receivedParam.data.asseementreportdisplayname == "") {
        this.reportdisplayName = this.receivedParam.data.reportName;
      } else {
        this.reportdisplayName =
          this.receivedParam.data.asseementreportdisplayname;
      }
      this.listOfReports.push(this.receivedParam.data.template);
      this.schedule = this.receivedParam.data.schedule;
      //this.schedule = this.receivedParam.data.schedule;
      this.datasource = this.receivedParam.data.inputDatasource;
      this.inputDataSourcePlaceholder = this.receivedParam.data.inputDatasource;
      this.emailDetails = this.receivedParam.data.emailDetails;
      if (this.schedule == "ONETIME") {
        this.customeScheduleSelected = true;
        this.showStartDate = true;
        this.endDateInput = this.receivedParam.data.enddate;
        this.startDateInput = this.receivedParam.data.startdate;
      }
      if (
        this.schedule == "BI_WEEKLY_SPRINT" ||
        this.schedule == "TRI_WEEKLY_SPRINT"
      ) {
        this.showStartDate = true;
      }
      this.taskListTobeSaved = this.receivedParam.data.taskDesc;
      this.taskidInOrder = this.taskListTobeSaved;
      this.selectedReport = this.receivedParam.data.template;
      this.selectedReportId = this.selectedReport.reportId;
      this.templateName = this.receivedParam.data.template.templateName;
      this.isReoccuring = this.receivedParam.data.isReoccuring;
      this.viewListDisabled = false;
      this.templateType = this.receivedParam.data.template.templateType
      if (this.templateType != undefined && this.templateType == 'ROITemplate') {
        this.loadMilestones();
      }
      this.selectedMilestone.id = this.receivedParam.data.milestoneId;
      if (this.selectedMilestone.id != 0 && this.selectedMilestone.id != undefined) {
        this.isROITemplate = true;
      }
      for (var element of this.taskListTobeSaved) {
        var str = element.description.toLowerCase();
        if (str.search(this.emailReg) !== -1) {
          this.enableEmailDetails = true;
        }
      }
    } else {
      this.listOfReports = this.receivedReportTemplates;
      this.isUpdate = false;
      this.disableRefresh = false;
      this.addReport();
    }
  }

  Refresh() {
    if (this.receivedParam.type == "edit") {
      if (this.schedule == "ONETIME") {
        this.startDateInput = this.receivedParam.data.startdate;
        this.endDateInput = this.receivedParam.data.enddate;
      }
      else
        this.isReoccuring = this.receivedParam.data.isReoccuring;
      this.disableRefresh = false;
    } else {
      this.reportName = "";
      this.reportdisplayName = "";
      this.selectedReport = { reportId: 0, description: "" };
      this.startDateInput = null;
      this.endDateInput = null;
      this.isReoccuring = false;
      this.schedule = undefined;
      this.datasource = "";
      this.deleteTasks();
      this.disableRefresh = false;
      this.enableEmailDetails = null;
      this.viewListDisabled = true;
      this.selectedMilestone = { milestoneId: 0 };
    }
  }

  async addReport() {
    this.selectedReport = { reportId: 0, templateName: "" };
    this.listOfSchedule = this.receivedScheduleList;
    this.activeDataArchivalRecordsResponse =
      await this.dataArchivingService.listActiveArchivedRecord();
    this.activeDataArchivalRecords =
      this.activeDataArchivalRecordsResponse.data;
    for (var record of this.activeDataArchivalRecords) {
      if (record.sourceUrl) {
        this.dataSourceList.push(record.sourceUrl);
      }
    }
  }

  addTasks() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      const dialogRef = this.dialog.open(AddTasksDialog, {
        panelClass: "custom-dialog-container",
        height: "75% !important",
        width: "75%",
        disableClose: true,
        data: {
          title: "Add Tasks",
          message: this.taskListTobeSaved,
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result == null) {
        } else {
          this.taskidInOrder = [];
          this.taskListTobeSaved = [];
          this.taskListTobeSaved = result;
          this.taskListTobeSaved;
          let enableEmailFlag = false;
          if (this.taskListTobeSaved.length == 0) {
            this.enableadd = true;
            this.enablecanel = false;
          } else {
            this.enableadd = false;
            this.enablecanel = true;
          }
          for (var element of this.taskListTobeSaved) {
            var tasklist;
            tasklist = { taskId: element.taskId, sequence: element.sequence };
            this.taskidInOrder.push(tasklist);
            var str = element.description.toLowerCase();
            if (str.search(this.emailReg) !== -1) {
              enableEmailFlag = true;
            }
          }
          if (enableEmailFlag) {
            this.enableEmailDetails = true;
          } else {
            this.enableEmailDetails = false;
            this.emailDetails = null;
          }
        }
      });
    }
  }

  deleteTasks() {
    this.taskListTobeSaved = [];
    this.enableadd = true;
    this.enablecanel = false;
    this.taskidInOrder = [];
    this.enableEmailDetails = false;
    this.emailDetails = null;
  }

  getTemplateName() {
    for (var data of this.listOfReports) {
      console.log("data:", data);
      if (data.reportId == this.selectedReportId) {
        this.templateName = data.templateName;
        console.log("Inside GetTemplate:", this.templateName)
        if (data.templateType !== undefined && data.templateType == 'ROITemplate') {
          this.isROITemplate = true;
          this.listOfSchedule = ["ONETIME"];
          this.loadMilestones();
        }
        else {
          this.isROITemplate = false;
          this.listOfSchedule = this.receivedScheduleList;
        }
      }
    }
    this.viewListDisabled = false;
  }
  async loadMilestones() {
    console.log("Inside lOadMilestones");
    this.responseForMilestone = await this.milestoneservice.fetchMileStoneConfig();
    console.log(this.responseForMilestone.data.length)
    if (this.responseForMilestone.data != null && this.responseForMilestone.status == 'success') {
      this.listOfMilestones = [];
      for (var record of this.responseForMilestone.data) {
        if (record.status == "COMPLETED") {
          this.listOfMilestones.push(record);
        }
      }
      if (this.listOfMilestones.length < 1) {
        this.isROITemplate = false;
        this.selectedReport = { reportId: 0, description: "" };
        this.messageDialog.showApplicationsMessage(
          "No milestone has been completed.Please configure the report once the milestone is completed.",
          "ERROR"
        );
      }
    }
    else {
      this.messageDialog.showApplicationsMessage(
        "Failed to load the milestones.Please check logs for more details.",
        "ERROR"
      );
    }
  }

  getMilestoneName() {
    for (var data of this.listOfMilestones) {
      if (data.id == this.selectedMilestone.id) {
        this.mileStoneName = data.mileStoneName;
      }
    }
  }


  viewListOfKPISofSelectedReport() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      const dialogRef = this.dialog.open(ViewKPIDialog, {
        panelClass: "custom-dialog-container",
        width: "75%",
        disableClose: true,
        data: {
          title: "List Of Kpi(s) of " + this.templateName,
          id: this.selectedReportId,
        },
      });
    }
  }

  redirectToLandingPage() {
    this.list();
  }

  checkSchedule() {
    console.log("inside checkschedule");
    if (this.schedule == "ONETIME") {
      this.customeScheduleSelected = true;
      this.showStartDate = true;
    } else if (
      this.schedule == "BI_WEEKLY_SPRINT" ||
      this.schedule == "TRI_WEEKLY_SPRINT"
    ) {
      this.showStartDate = true;
      this.customeScheduleSelected = false;
    } else {
      this.showStartDate = false;
      this.customeScheduleSelected = false;
    }
  }

  validateAndSaveReportData() {
    var isValidated = true;
    var checkname = this.regex.test(this.reportName);
    let messageDialogText;
    if (
      this.reportName === "" ||
      this.selectedReport.id === 0 ||
      this.schedule === undefined ||
      this.taskidInOrder.length === 0
    ) {
      isValidated = false;
      messageDialogText = "Please fill mandatory fields";
    } else if (!checkname) {
      isValidated = false;
      messageDialogText =
        "Please enter valid report name, and it contains only alphanumeric character and underscore ";
    } else {
      if (this.schedule == "ONETIME") {
        if (this.startDateInput == null || this.endDateInput == null) {
          isValidated = false;
          messageDialogText = "Please fill mandatory fields";
        }
        if (this.startDateInput > this.endDateInput) {
          isValidated = false;
          messageDialogText = "Start date should be less than End date";
        }
      } else if (
        this.schedule == "BI_WEEKLY_SPRINT" ||
        this.schedule == "TRI_WEEKLY_SPRINT"
      ) {
        if (this.startDateInput == null) {
          isValidated = false;
          messageDialogText = "Please fill mandatory fields";
        }
      }
    }
    if (this.enableEmailDetails && this.emailDetails == null) {
      isValidated = false;
      messageDialogText = "Please add Mailing Details";
    }
    if (isValidated) {
      this.editSaveData();
    } else {
      this.messageDialog.openSnackBar(messageDialogText, "error");
    }
  }

  getToolTipData() {
    var tooltipdata = [];
    for (var task of this.taskListTobeSaved) {
      tooltipdata.push(task.description);
      tooltipdata.push("\n ");
    }
    return tooltipdata;
  }

  editSaveData() {
    var reportAPIRequestJson = {};
    var self = this;
    var message;

    if (this.receivedParam.type == "save") {
      reportAPIRequestJson["reportName"] = self.reportName;
      reportAPIRequestJson["asseementreportdisplayname"] =
        self.reportdisplayName;
      reportAPIRequestJson["reportTemplate"] = self.selectedReportId;
      reportAPIRequestJson["milestoneId"] = self.selectedMilestone.id;
      reportAPIRequestJson["schedule"] = self.schedule;
      reportAPIRequestJson["startdate"] = this.startdate;
      reportAPIRequestJson["enddate"] = this.enddate;
      reportAPIRequestJson["isReoccuring"] = this.isReoccuring;
      reportAPIRequestJson["datasource"] = this.datasource;
      reportAPIRequestJson["tasklist"] = this.taskidInOrder;
      reportAPIRequestJson["emailDetails"] = this.emailDetails;
      reportAPIRequestJson["emailList"] = "abcd@abcd.com";
      reportAPIRequestJson["orgName"] = this.dataShare.getOrgName();
      reportAPIRequestJson["userName"] = this.dataShare.getLoginName();
      var dialogmessage =
        " You have created a new Report <b>" +
        self.reportName +
        "</b> .Do you want continue ? ";
      var title = "Save Report " + this.reportName;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "30%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.reportmanagementservice
            .saveDataforReport(JSON.stringify(reportAPIRequestJson))
            .then(function (response) {
              console.log(response);
              if (response.status == "success") {
                message = "<b>" + self.reportName + "</b> saved successfully.";
                if (response.data.dashboardResponse != null) {
                  message =
                    message + "<br>But " + response.data.dashboardResponse;
                }
                self.messageDialog.openSnackBar(message, "success");
                if (response.data.dashboardUrl != null) {
                  console.log(response.data.dashboardUrl);
                  var dashUrl =
                    InsightsInitService.grafanaHost +
                    "/dashboard/script/iSight_ui3.js?url=" +
                    encodeURIComponent(response.data.dashboardUrl);
                  self.previewDashboard(dashUrl);
                }
                self.list();
              } else {
                if (
                  response.message ==
                  "Email Task not present for sending mail in the provided email details"
                ) {
                  self.messageDialog.openSnackBar(
                    "Email Task not present for sending mail in the provided email details",
                    "error"
                  );
                } else if (
                  response.message == "Email Details not present for Email task"
                ) {
                  self.messageDialog.openSnackBar(
                    "Email Details not present for Email task.",
                    "error"
                  );
                } else if (
                  response.message ==
                  "Assessment Report with the given Report name already exists"
                ) {
                  self.messageDialog.openSnackBar(
                    "Assessment Report with the given Report name already exists.",
                    "error"
                  );
                } else {
                  self.messageDialog.openSnackBar(
                    "Failed to save the report.Please check logs.",
                    "error"
                  );
                }
              }
            });
        }
      });
    }
    if (this.receivedParam.type == "edit") {
      reportAPIRequestJson["isReoccuring"] = this.isReoccuring;
      reportAPIRequestJson["emailDetails"] = this.emailDetails;
      reportAPIRequestJson["id"] = this.receivedParam.data.configId;
      reportAPIRequestJson["tasklist"] = this.taskidInOrder;
      reportAPIRequestJson["emailList"] = "abcd@abcd.com";
      reportAPIRequestJson["orgName"] = this.dataShare.getOrgName();
      reportAPIRequestJson["userName"] = this.dataShare.getLoginName();
      var dialogmessage =
        " You have updated Report <b>" +
        self.reportName +
        "</b> .Do you want continue ? ";
      var title = "Update Report " + this.reportName;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        "",
        "ALERT",
        "30%"
      );
      dialogRef.afterClosed().subscribe((result) => {
        if (result == "yes") {
          this.reportmanagementservice
            .updateDataforReport(JSON.stringify(reportAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                (message =
                  "<b>" + self.reportName + "</b> updated successfully."),
                  self.messageDialog.openSnackBar(message, "success");
                self.list();
              } else {
                console.error(response);
                self.messageDialog.openSnackBar(
                  "Failed to update the report.Please check logs.",
                  "error"
                );
              }
            });
        }
      });
    }
  }

  previewDashboard(dashUrl) {
    const dialogRef = this.dialog.open(DashboardPreviewConfigDialog, {
      panelClass: "custom-dialog-container",
      width: "75%",
      height: "84%",
      disableClose: true,
      data: {
        route: dashUrl,
        isAssessmentReport: true,
      },
    });
  }

  list() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        reportparam: this.configParams,
      },
    };
    this.router.navigate(["InSights/Home/reportmanagement"], navigationExtras);
  }

  getstartDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.startDateInput = new Date(event.value);
    this.startdate = this.dataShare.convertDateToSpecificDateFormat(
      this.startDateInput,
      "yyyy-MM-dd'T'HH:mm:ss'Z'"
    );
  }

  getendDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.endDateInput = new Date(event.value);
    this.enddate = this.dataShare.convertDateToSpecificDateFormat(
      this.endDateInput,
      "yyyy-MM-dd'T'HH:mm:ss'Z'"
    );
  }

  addEmailConfig() {
    var self = this;
    var details = "";
    var type = "";
    var isSessionExpired = self.dataShare.validateSession();
    if (!isSessionExpired) {
      if (self.emailDetails || this.receivedParam.type == "edit") {
        details = self.emailDetails;
        type = "edit";
      } else {
        details = "";
        type = "save";
      }
      const dialogRef = self.dialog.open(EmailConfigurationDialog, {
        panelClass: "custom-dialog-container",
        height: "600px !important",
        width: "900px",
        disableClose: true,
        data: {
          emailDetails: details,
          type: type,
        },
      });

      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          self.emailDetails = result;
        }
      });
    }
  }
}
