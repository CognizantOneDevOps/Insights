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
import { DatePipe } from "@angular/common";
import { MatDatepickerInputEvent } from "@angular/material/datepicker";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";
import { MatDialog } from "@angular/material";
import { DataSharedService } from "@insights/common/data-shared-service";
import { ViewKPIDialog } from "@insights/app/modules/reportmanagement/report-configuration/view-kpi-dialog";
import { AddTasksDialog } from "@insights/app/modules/reportmanagement/report-configuration/add-task";
import { ReportManagementService } from "@insights/app/modules/reportmanagement/reportmanagement.service";
import { DataArchivingService } from "@insights/app/modules/settings/dataarchiving/dataarchiving-service";
import { EmailConfigurationDialog } from "@insights/app/modules/reportmanagement/report-configuration/email-configuration-dialog";

@Component({
  selector: "app-report-configuration",
  templateUrl: "./report-configuration.component.html",
  styleUrls: ["../reportmanagement.component.css", "./../../home.module.css"]
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
  emailAdded: boolean = false;
  emailUpdate: boolean = false;
  enableEmailDetails: boolean = false;
  emailReg = "email";

  constructor(
    private route: ActivatedRoute,
    private dataShare: DataSharedService,
    private dialog: MatDialog,
    private datepipe: DatePipe,
    public messageDialog: MessageDialogService,
    public router: Router,
    public reportmanagementservice: ReportManagementService,
    private dataArchivingService: DataArchivingService
  ) {
    this.showThrobber = true;
    this.loadScheduleAndReportTempalte();
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.receivedParam = JSON.parse(params["reportparam"]);
      console.log(this.receivedParam);
    });
  }

  ngAfterViewInit() { }
  public async loadScheduleAndReportTempalte() {
    this.reponseForReportTemplate = await this.reportmanagementservice.getReportTemplate();
    this.listOfReports = this.reponseForReportTemplate.data;
    if (this.listOfReports.length > 0) {
      this.initializeVariable();
    } else {
      this.messageDialog.showApplicationsMessage(
        "Failed to load the report templates.Please check logs for more details.",
        "ERROR"
      );
      this.list();
      this.showThrobber = false;
    }
    this.showThrobber = false;
  }

  initializeVariable() {
    if (this.receivedParam.type == "edit") {
      this.disableRefresh = false;
      this.enablecanel = true;
      this.enableadd = false;
      this.disableInputFields = true;
      this.isUpdate = true;
      this.emailUpdate = true;
      this.emailAdded = false;
      this.isReoccuring = this.receivedParam.data.isReoccuring;
      this.reportName = this.receivedParam.data.reportName;
      if (this.receivedParam.data.asseementreportdisplayname == "") {
        this.reportdisplayName = this.receivedParam.data.reportName;
      } else {
        this.reportdisplayName = this.receivedParam.data.asseementreportdisplayname;
      }
      this.schedule = this.receivedParam.data.schedule;
      this.frequencyPlaceholder = this.receivedParam.data.schedule;
      this.datasource = this.receivedParam.data.inputDatasource;
      this.inputDataSourcePlaceholder = this.receivedParam.data.inputDatasource;
      this.emailDetails = this.receivedParam.data.emailDetails;
      if (this.schedule == "ONETIME") {
        this.customeScheduleSelected = true;
        this.showStartDate = true;
        this.endDateInput = this.receivedParam.data.enddate;
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
      this.templateName = this.receivedParam.data.template.templateName;
      this.startDateInput = this.receivedParam.data.startdate;
      this.isReoccuring = this.receivedParam.data.isReoccuring;
      this.viewListDisabled = false;
      for (var element of this.taskListTobeSaved) {
        var str = element.description.toLowerCase();
        if (str.search(this.emailReg) !== -1) {
          this.enableEmailDetails = true;
        }
      }
    } else {
      this.isUpdate = false;
      this.emailUpdate = false;
      this.disableRefresh = false;
      this.addReport();
    }
  }

  Refresh() {
    if (this.receivedParam.type == "edit") {
      this.isReoccuring = false;
      this.deleteTasks();
      this.disableRefresh = false;
      this.emailAdded = false;
      this.emailUpdate = true;
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
      this.emailAdded = false;
      this.emailUpdate = false;
    }
  }

  async addReport() {
    this.selectedReport = { reportId: 0, templateName: "" };
    this.reponseForschdeule = await this.reportmanagementservice.getSchedule();
    this.listOfSchedule = this.reponseForschdeule.data;
    if (this.listOfSchedule.length < 0) {
      this.messageDialog.showApplicationsMessage(
        "Failed to load the schedules.Please check logs for more details.",
        "ERROR"
      );
      this.list();
      this.showThrobber = false;
    } else {
      this.activeDataArchivalRecordsResponse = await this.dataArchivingService.listActiveArchivedRecord();
      this.activeDataArchivalRecords = this.activeDataArchivalRecordsResponse.data;
      for (var record of this.activeDataArchivalRecords) {
        if (record.sourceUrl) {
          this.dataSourceList.push(record.sourceUrl);
        }
      }
    }
  }

  addTasks() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      const dialogRef = this.dialog.open(AddTasksDialog, {
        panelClass: "showjson-dialog-container",
        height: "600px !important",
        width: "700px",
        disableClose: true,
        data: {
          title: "Add Tasks",
          message: this.taskListTobeSaved
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result == null) {
        } else {
          this.taskidInOrder = [];
          this.taskListTobeSaved = [];
          this.taskListTobeSaved = result;
          this.taskListTobeSaved;
          let enableEmailFlag=false;
          if (this.taskListTobeSaved.length == 0) {
            this.enableadd = true;
            this.enablecanel = true;
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
          if(enableEmailFlag){
            this.enableEmailDetails = true;
          }else{
            this.enableEmailDetails = false;
            this.emailDetails=null;
            this.emailAdded=false;
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
    this.emailAdded=false;
    this.emailDetails=null;
  }

  getTemplateName() {
    for (var data of this.listOfReports) {
      if (data.reportId == this.selectedReport.reportId) {
        this.templateName = data.templateName;
      }
    }
    this.viewListDisabled = false;
  }

  viewListOfKPISofSelectedReport() {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      const dialogRef = this.dialog.open(ViewKPIDialog, {
        panelClass: "showjson-dialog-container",
        width: "650px",
        disableClose: true,
        data: {
          title: "List Of Kpi(s) of " + this.templateName,
          id: this.selectedReport.reportId
        }
      });
    }
  }


  redirectToLandingPage() {
    this.list();
  }

  checkSchedule() {
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
    if(this.enableEmailDetails && !this.emailAdded){
        isValidated = false;
        messageDialogText = "Please add Mailing Details";
    }
    if (isValidated) {
      this.editSaveData();
    } else {
      this.messageDialog.showApplicationsMessage(messageDialogText, "ERROR");
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
    if (this.receivedParam.type == "save") {
      reportAPIRequestJson["reportName"] = self.reportName;
      reportAPIRequestJson["asseementreportdisplayname"] = self.reportdisplayName;
      reportAPIRequestJson["reportTemplate"] = self.selectedReport.reportId;
      reportAPIRequestJson["schedule"] = self.schedule;
      reportAPIRequestJson["startdate"] = this.startdate;
      reportAPIRequestJson["enddate"] = this.enddate;
      reportAPIRequestJson["isReoccuring"] = this.isReoccuring;
      reportAPIRequestJson["datasource"] = this.datasource;
      reportAPIRequestJson["tasklist"] = this.taskidInOrder;
      if(this.enableEmailDetails && this.emailAdded){
        reportAPIRequestJson["emailDetails"] = this.emailDetails;
      }else{
        reportAPIRequestJson["emailDetails"] = null;
      }
      reportAPIRequestJson["emailList"] = "abcd@abcd.com";
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
        "40%"
      );
      dialogRef.afterClosed().subscribe(result => {
        if (result == "yes") {
          this.reportmanagementservice
            .saveDataforReport(JSON.stringify(reportAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                self.messageDialog.showApplicationsMessage(
                  "<b>" + self.reportName + "</b> saved successfully.",
                  "SUCCESS"
                );
                self.list();
              } else {
                if (response.message == "Email Task not present for sending mail in the provided email details") {
                  self.messageDialog.showApplicationsMessage(
                    "Email Task not present for sending mail in the provided email details",
                    "ERROR"
                  );
                } else if (response.message == "Email Details not present for Email task") {
                  self.messageDialog.showApplicationsMessage(
                    "Email Details not present for Email task.",
                    "ERROR"
                  );
                } else if (response.message == "Assessment Report with the given Report name already exists") {
                  self.messageDialog.showApplicationsMessage(
                    "Assessment Report with the given Report name already exists.",
                    "ERROR"
                  );
                }
                else {
                  self.messageDialog.showApplicationsMessage(
                    "Failed to save the report.Please check logs.",
                    "ERROR"
                  );
                }
              }
            });
        }
      });
    }
    if (this.receivedParam.type == "edit") {
      reportAPIRequestJson["isReoccuring"] = this.isReoccuring;
      if(this.enableEmailDetails && this.emailAdded){
        reportAPIRequestJson["emailDetails"] = this.emailDetails;
      }else{
        reportAPIRequestJson["emailDetails"] = null;
      }
      reportAPIRequestJson["id"] = this.receivedParam.data.configId;
      reportAPIRequestJson["tasklist"] = this.taskidInOrder;
      reportAPIRequestJson["emailList"] = "abcd@abcd.com";
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
        "40%"
      );
      dialogRef.afterClosed().subscribe(result => {
        if (result == "yes") {
          this.reportmanagementservice
            .updateDataforReport(JSON.stringify(reportAPIRequestJson))
            .then(function (response) {
              if (response.status == "success") {
                self.messageDialog.showApplicationsMessage(
                  "<b>" + self.reportName + "</b> updated successfully.",
                  "SUCCESS"
                );
                self.list();
              } else {
                self.messageDialog.showApplicationsMessage(
                  "Failed to update the report.Please check logs.",
                  "ERROR"
                );
              }
            });
        }
      });
    }
  }

  list() {
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        reportparam: this.configParams
      }
    };
    this.router.navigate(["InSights/Home/reportmanagement"], navigationExtras);
  }

  getstartDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.startDateInput = new Date(event.value);
    this.startdate = this.dataShare.convertDateToSpecificDateFormat(this.startDateInput, "yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  getendDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.endDateInput = new Date(event.value);
    this.enddate = this.dataShare.convertDateToSpecificDateFormat(this.endDateInput, "yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  addEmailConfig() {
    var self = this;
    var details = '';
    var type = '';
    var isSessionExpired = self.dataShare.validateSession();
    if (!isSessionExpired) {
      if (this.emailAdded || this.receivedParam.type == "edit") {
        details = self.emailDetails;
        type = 'edit';
      }
      else {
        details = '';
        type = 'save';
      }
      const dialogRef = self.dialog.open(EmailConfigurationDialog, {
        panelClass: "showjson-dialog-container",
        width: "70%",
        disableClose: true,
        data: {
          emailDetails: details,
          type: type
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          self.emailDetails = result;
          self.emailAdded = true;
        }
      });
    }
  }
}
