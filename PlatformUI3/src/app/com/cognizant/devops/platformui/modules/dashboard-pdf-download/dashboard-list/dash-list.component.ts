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
import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, NavigationExtras } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MessageDialogService } from '../../application-dialog/message-dialog-service';
import { KpiService } from '../../kpi-addition/kpi-service';
import { GrafanaAuthenticationService } from '@insights/common/grafana-authentication-service';
import { DashboardDetailsDialog } from '../dashboard-details-dialog/dashboard-details-dialog';
import { ReportManagementService } from '../../reportmanagement/reportmanagement.service';
import { saveAs as importedSaveAs } from "file-saver";
import { WorkflowHistoryDetailsDialog } from '../../reportmanagement/workflow-history-details/workflow-history-details-dialog';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MatRadioChange } from '@angular/material/radio';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-dash-list',
  templateUrl: './dash-list.component.html',
  styleUrls: ['./dash-list.component.css','./../../home.module.css']
})
export class DashboardListComponent implements OnInit {
  displayedColumns = [];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  dashboardDatasource = new MatTableDataSource<any>();
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
  selectedDashboard: any;
  showConfirmMessage: string;
  type: string;
  enableRefresh: boolean = false;
  MAX_ROWS_PER_TABLE = 10;
  orgArr= [];
  isDatainProgress: boolean=false;
  disablebutton=[];
  timeZone: string = '';
  count: number;
  selectedIndex : number;
  currentPageValue : number;
  disableDownload : boolean = true;
  userName : string;
  changeState: boolean;
  isActive : boolean;

  constructor(public messageDialog: MessageDialogService,private grafanaService: GrafanaAuthenticationService,
    public dataShare: DataSharedService,public router: Router, public dialog: MatDialog,public reportmanagementService: ReportManagementService
    ) {
  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), '');
    this.getAllConfig();
    this.displayedColumns = ['radio', 'Title','Organisation', 'PdfType', 'ScheduleType', 'Status', 'Active', 'More'];
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;    
    this.userName = this.dataShare.getUserName();
    console.log(this.dashboardDatasource);
  }
  public async getOrgs() {
  }

  ngAfterViewInit() {
    this.dashboardDatasource.paginator = this.paginator;
  }

  add() {
    this.grafanaService.iconClkSubject.next('CLICK');
    let navigationExtras : NavigationExtras = {
      skipLocationChange : true,
      queryParams : {
        "userName" : this.userName
      }
    }
    this.router.navigate(['InSights/Home/dash-pdf-config'], navigationExtras);
  }
  edit() {
    this.grafanaService.iconClkSubject.next('CLICK');
    let dashboardJson=JSON.parse(this.selectedDashboard.dashboardJson)
    let emailDetails = dashboardJson.emailDetails
    console.log(emailDetails)
    console.log(this.selectedDashboard)
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "id":this.selectedDashboard.id,
        "pdfType":this.selectedDashboard.pdfType,
        "scheduleType":this.selectedDashboard.scheduleType,
        "status":this.selectedDashboard.status,
        "title": dashboardJson.title,
        "variables":this.selectedDashboard.variables,
        "orgName":this.selectedDashboard.orgName,
        "type":'edit',
        "dashboard":dashboardJson.dashboard,
        "organisation":dashboardJson.organisation,
        "range":dashboardJson.range,
        "from":dashboardJson.from,
        "to":dashboardJson.to,   
        "email":this.selectedDashboard.email,    
        "emailDetails": JSON.stringify(emailDetails),
        "rangeText":dashboardJson.rangeText,
        "workflowId": this.selectedDashboard.workflowId ,
        "loadTime":  dashboardJson.loadTime,
        "userName" : this.userName,
        "theme" : dashboardJson.theme
      }
    };
    this.router.navigate(['InSights/Home/edit-dashboard'], navigationExtras);
  }
  refresh() {
    this.getAllConfig();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;
    this.disableDownload = true;
  }
  enableButtons(event: MatRadioChange, i) {
    this.selectedIndex = i + this.currentPageValue ;
    this.onRadioBtnSelect = true;
    this.disablebutton[i] = false;
    if(event.value.status == 'NOT_STARTED') {
      this.disableDownload = true;
    } else {
      this.disableDownload = false;
    }
  }
    async getAllConfig() {
    this.isDatainProgress=true;
    this.selectedIndex = -1;
    var self = this;
    self.refreshRadio = false;
    // let currentUserWithOrgs = await this.grafanaService.getCurrentUserWithOrgs();
    // if (currentUserWithOrgs.status === "success") {
    //   this.orgArr = currentUserWithOrgs.data.orgArray;
    // }
    console.log(this.orgArr);
    this.dashConfigList = await this.grafanaService.fetchDashboardConfigs();
    if (this.dashConfigList != null && this.dashConfigList.status == "success") {
    this.dashboardDatasource.data = this.dashConfigList.data.filter(x=>x.source==='PLATFORM');
    }
    for(let i=0;i<this.dashboardDatasource.data.length;i++){
      this.disablebutton.push(true);
    }
    this.isDatainProgress=false;
    this.count = 0;
    // this.dashConfigList.data.forEach(element => {
    //   let dashJson=JSON.parse(element.dashboardJson);
    //   // let organisationObj = this.orgArr.filter(e => e.orgId === Number(dashJson.organisation));
    //   // this.dashboardDatasource.data[this.count].orgName = organisationObj[0].name;
    //   this.count += 1;
    // }); 
      this.dashboardDatasource.paginator = this.paginator;
    }
  
  applyFilter(filterValue: string) {
    this.dashboardDatasource.filter = filterValue.trim();
  }

  list() {
    this.getAllConfig();
  }
  showAllDetails(data) {
    let showDetailsDialog = this.dialog.open(DashboardDetailsDialog, {
        panelClass: 'showjson-dialog-container',
        width: '80%',
        height: '75%',
        disableClose: true,
        data: { cardData:data ,showCardDetail: true }
    });
  }
  delete() {
    var self = this;
    let data = self.selectedDashboard;

    var title = "Delete Dashboard";
    var dialogmessage =
      "Do you want to delete a Dashboard <b>" +
      "</b>? <br><br> <b> Please note: </b> The action of deleting a Dashboard " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedDashboard.id,
      "ALERT",
      "40%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
         self.grafanaService
           .deleteDashboard(JSON.stringify(self.selectedDashboard.id))
           .then(function (data) {
             if (data.status === "success") {
               self.messageDialog.showApplicationsMessage("<b>" + "Deleted Successfully" + "</b>", "SUCCESS");
               self.list();
             }
           })
           .catch(function (data) {
             self.showConfirmMessage = "service_error";
           });
       }
    });
  }
  getRowData(event){
    let data=event;
  }
  showWorkflowHistoryDetailsDialog(selectedDashboard) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(WorkflowHistoryDetailsDialog, {
        panelClass: 'workflow-history-details-dialog-container',
        disableClose: true,
        data: {
          reportName: selectedDashboard.title,
          workflowId: selectedDashboard.workflowId,
          timeZone: this.timeZone
        }
      });
    }
  }

  async downloadPDF() {
    var PDFRequestJson = {};
    let executionRecords =  await this.grafanaService.getExecutionId(
      this.selectedDashboard.workflowId
    );
    var pdfFileName =  this.selectedDashboard.title +'.pdf';
    PDFRequestJson['pdfName'] = this.selectedDashboard.title +'.pdf';
    PDFRequestJson['workflowId'] = this.selectedDashboard.workflowId;
    PDFRequestJson['executionId'] = executionRecords.data.executionId;
    this.grafanaService.downloadPDF(JSON.stringify(PDFRequestJson))
      .then(function (data) {
        importedSaveAs(data, pdfFileName);
      });

  }

  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex  * this.MAX_ROWS_PER_TABLE;
  }

  restart() {
    var statusRequestJson = {};
    statusRequestJson['id'] = this.selectedDashboard.id;
    statusRequestJson['status'] = "RESTART";
    var message = 'Status has been updated to RESTART.';
    console.log(statusRequestJson);
    this.updateReportStatus(statusRequestJson, message);
  }

  private updateReportStatus(statusRequestJson: {}, message: string) {
    var self = this;
    this.grafanaService.setRestartStatus(JSON.stringify(statusRequestJson))
      .then(function (data) {
        if (data.status == 'success') {
          self.messageDialog.showApplicationsMessage(message, 'SUCCESS');
          self.list();
        }
        else {
          self.messageDialog.showApplicationsMessage('Failed to update the Dashboard report state.Please check logs for more details String.', 'ERROR');
        }
      });
  }

  
  updateStatus(event: MatSlideToggleChange, id, element) {
    console.log(id, element);
    var self = this;
    var title = 'Update Active/Inactive State';
    var state = element.isActive ? 'Active' : 'Inactive';
    var dialogmessage =
      'Are you sure you want to <b>' +
      state +
      '</b> Dashboard report <b>' +
      self.selectedDashboard.title +
      '</b> ?';
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedDashboard.title,
      'ALERT',
      '45%'
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.changeState = event.checked;
        var setStatusRequestJson = {};
        setStatusRequestJson['id'] = id;
        setStatusRequestJson['isActive'] = event.checked;
        this.grafanaService
          .setActiveState(JSON.stringify(setStatusRequestJson))
          .then(function (data) {
            if (data.status == 'success') {
              console.log('active status changed');
            } else {
              this.messageDialog.showApplicationsMessage(
                'Failed to update active state.Please check logs for more details.',
                'ERROR'
              );
            }
          });
      } else {
        element.isActive = !event.checked;
      }
    });
  }
}

