/* Copyright 2022 Cognizant Technology Solutions
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
import { Component, Injectable, OnInit, ViewChild } from "@angular/core";
import { Sort } from "@angular/material/sort";

import { MatDialog } from "@angular/material/dialog";



import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { MessageDialogService } from "../application-dialog/message-dialog-service";
import { MatRadioChange } from "@angular/material/radio";
import { AddComponent } from "./dialogs/add/add.component";
import { MultipleEmailConfigService } from "./multiple-email-config.service";
import { ActivatedRoute,  Router } from "@angular/router";
import { DataSharedService } from "@insights/common/data-shared-service";
import { WorkflowHistoryDetailsDialog } from "@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog";
import { MatSlideToggleChange } from "@angular/material/slide-toggle";
import { InsightsUtilService } from "@insights/common/insights-util.service";

@Component({
  selector: "multiple-email-configuration",
  templateUrl: "./multiple-email-configuration.component.html",
  styleUrls: [
    "./multiple-email-configuration.component.scss",
    "./../home.module.scss",
  ],
})
export class MultipleEmailConfigurationComponent implements OnInit {
  displayedColumns = [];

  selectedDashboard: any;

  constructor(
    public dataShare: DataSharedService,
    public dialog: MatDialog,
    public messageDialog: MessageDialogService,
    public multiEmailConfig: MultipleEmailConfigService,
    public router: Router,
    public insightsUtil : InsightsUtilService,
    public route: ActivatedRoute
  ) {}

  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  emailDatasource = new MatTableDataSource<any>();
  isDatainProgress: boolean = false;
  currentPageIndex: number = -1;
  disableDelete: boolean = true;
  disableEdit: boolean = true;
  currentPageValue: number;
  selectedIndex: number;
  onRadioBtnSelect: boolean = false;
  selectedEmailConfig: any;
  isActive: boolean = false;
  disablebutton = [];
  email: string;
  batchName: string;
  clicked = new Array();
  schedule: string; 
  lastrun: string;
  nextrun: string;
  MAX_ROWS_PER_TABLE = 5;
  responseForMailConfig: any;
  mailConfigList: any;
  detailedRecords = [];
  dateObj: Date;
  userDataSource = new MatTableDataSource<any>();
  source: any;
  emailConfigSourceList = { data: [] };
  pageRefreshed: boolean = false;
  timeZone: string = "";
  timeZoneAbbr: string = "";

  ngOnInit() {
    this.displayedColumns = [
      "radio",
      "batchname",
      "schedule",
      "lastrun",
      "nextrun",
      "Active",
      "details",
    ];
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), "");
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.route.queryParams.subscribe((params) => {
      this.source = params.source;
    });
    this.getAllConfig();
    console.log("arr" + this.selectedEmailConfig);
  }

  async getAllConfig() {
    this.isDatainProgress = true;
    

    this.emailDatasource.paginator = this.paginator;
    

    var emailConfig = {};
    emailConfig["source"] = this.source;
    this.responseForMailConfig = await this.multiEmailConfig.getMailConfig(
      this.source
    );
    this.emailConfigSourceList.data = this.responseForMailConfig.data;

    console.log(this.responseForMailConfig);
    console.log("emailConfigSrc");
    console.log(this.emailConfigSourceList.data);
    if (
      this.responseForMailConfig.data != null &&
      this.responseForMailConfig.status == "success"
    ) {
      this.mailConfigList = this.responseForMailConfig.data;
      this.isDatainProgress = false;
      //this.showPagination = true;
      var dataArray = this.mailConfigList;
      if (dataArray != undefined) {
        dataArray.forEach((key) => {
          var obj = key;
          if (typeof obj["lastRun"] !== "undefined") {
            if (obj["lastRun"] === "" || obj["lastRun"] == 0) {
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
            if (obj["nextRun"] === "" || obj["nextRun"] == 0) {
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

      }
    } else {
      this.messageDialog.openSnackBar(
        "Failed to load the report templates.Please check logs for more details.",
        "error"
      );
    }
  }

  addEmailConfig() {
    var self=this;
    const dialogRef = this.dialog.open(AddComponent, {
      panelClass: "custom-dialog-container",
      height: "85%",
      width: "80%",
      data: { source: this.source, type: "ADD" },
    });
    dialogRef.afterClosed().subscribe(() => { 

      self.onRadioBtnSelect = false;
      this.Refresh();
});
    //  this.multiEmailConfig.setType("ADD");
  }

  enableButtons(event: MatRadioChange, i) {
    this.selectedIndex = i + this.currentPageValue;
    this.onRadioBtnSelect = true;
    this.disablebutton[i] = false;
    this.disableDelete = false;
    this.disableEdit = false;
    //this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  editEmailConfig() {
    var self=this;
    const selectedConfig = this.emailConfigSourceList.data.find(
      (config) => config.batchName === this.selectedEmailConfig.batchName
    );
    const dialogRef = this.dialog.open(AddComponent, {
      panelClass: "custom-dialog-container",
      height: "85%",
      width: "80%",
      disableClose: true,
      data: {
        source: this.source,
        type: "EDIT",
        selectedEmailConfig: selectedConfig,
      },
    });
    dialogRef.afterClosed().subscribe(() => { 

              self.onRadioBtnSelect = false;
              this.Refresh();
    });
  }

  deleteEmailConfig() {
    const selectedConfig = this.emailConfigSourceList.data.find(
      (config) => config.batchName === this.selectedEmailConfig.batchName
    );
    
    var self = this;
    var title = "Delete Dashboard";
    var dialogmessage =
      "Do you want to delete <b>" +
      selectedConfig.batchName +
      "</b>? <br><b> Please note: </b> The action of deleting " +
      "<b>" + 
      selectedConfig.batchName +
      "</b> cannot be undone.<br> Do you want to continue ? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.email,
      "DELETE",
      "36%"
    );
    dialogRef.afterClosed().subscribe((result) => { 
      if (result == "yes") {
        self.multiEmailConfig
          .deleteData(selectedConfig.groupTemplateId)
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.openSnackBar(
                "<b>" + "Deleted Successfully" + "</b>",
                "success"
              );
              self.onRadioBtnSelect = false;
              self.Refresh();
            }
          });
      }
    });
    // this.refreshEmployee();
  }

  deleteEmailConfig1() {
    const selectedConfig = this.emailConfigSourceList.data.find(
      (config) => config.batchName === this.selectedEmailConfig.batchName
    );

    var self = this;
    var title = "Delete Dashboard";
    var dialogmessage =
      "Do you want to delete " +
      selectedConfig.batchName +
      "<b>" +
      "</b>? <br><b> Please note: </b> The action of deleting an Email Entry " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.email,
      "DELETE",
      "36%"
    );
    dialogRef.afterClosed().subscribe((result) => { 
      if (result == "yes") {
        self.multiEmailConfig
          .deleteData(selectedConfig.groupTemplateId)
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.openSnackBar(
                "<b>" + "Deleted Successfully" + "</b>",
                "success"
              );
              self.onRadioBtnSelect = false;
              this.Refresh();
            }
          });
      }
    });
    // this.refreshEmployee();
  }

  Refresh() {
    this.selectedIndex = -1;
    this.selectedEmailConfig = "";
    this.clicked = [];
    this.userDataSource.data.forEach((element) => {
      this.clicked.push(true);
    });
    this.pageRefreshed = true;
    this.disableDelete = true;
    this.disableEdit = true;
    this.redirect();
  }

  list() {
    if(this.source=="GRAFANADASHBOARDPDFREPORT"){
      this.router.navigate(["InSights/Home/dash-pdf-download"], {
        skipLocationChange: true,
      });
    }
    else{
      this.router.navigate(["InSights/Home/reportmanagement"], {
        skipLocationChange: true,
      });
    }
    
  }

  redirect(){
    this.getAllConfig();
  }
  

  showWorkflowHistoryDetailsDialog(batchName: String, workflowId: String) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(WorkflowHistoryDetailsDialog, {
        disableClose: true,
        panelClass: "custom-dialog-container",
        height: "1170px",
        width: "550px",
        data: {
          reportName: batchName,
          workflowId: workflowId
        },
      });
    }
   }

   updateStatus(event: MatSlideToggleChange, id, element) {
    const selectedConfig = this.emailConfigSourceList.data.find(
      (config) => config.batchName === this.selectedEmailConfig.batchName
    );
    var self = this;
    var title = "Update Active/Inactive State";
    var state = element.isActive ? "Active" : "Inactive";
    var dialogmessage =
      "Are you sure you want to <b>" +
      state +
      "</b> report <b>" +
      selectedConfig.batchName +
      "</b> ?";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      selectedConfig.batchName,
      "ALERT",
      "30%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var setStatusForEmail = {};
        setStatusForEmail["id"] = id;
        setStatusForEmail["isActive"] = event.checked;
        this.multiEmailConfig
          .setActiveStatus(JSON.stringify(setStatusForEmail))
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

  ngAfterViewInit() {
    this.emailDatasource.paginator = this.paginator;
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
    this.currentPageIndex = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
  }
  sortData(sort: Sort) {
    const data =  this.detailedRecords.slice();
    if (!sort.active || sort.direction === '') {
      this.userDataSource.data = data;
      return;
    }

    this.userDataSource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc)
    });
    this.userDataSource.paginator = this.paginator;
  }
}
