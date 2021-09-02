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
    this.displayedColumns = ['radio', 'Title','Organisation', 'PdfType', 'ScheduleType', 'Status', 'More'];
  }
  public async getOrgs() {
  }

  ngAfterViewInit() {
    this.dashboardDatasource.paginator = this.paginator;
  }

  add() {
    this.grafanaService.iconClkSubject.next('CLICK');
    this.router.navigate(['InSights/Home/dash-pdf-config'],{skipLocationChange:true});
  }
  edit() {
    this.grafanaService.iconClkSubject.next('CLICK');
    let dashboardJson=JSON.parse(this.selectedDashboard.dashboardJson)
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        "id":this.selectedDashboard.id,
        "emailBody":dashboardJson.emailBody,
        "email":this.selectedDashboard.email,
        "pdfType":this.selectedDashboard.pdfType,
        "scheduleType":this.selectedDashboard.scheduleType,
        "status":this.selectedDashboard.status,
        "title":this.selectedDashboard.title,
        "variables":this.selectedDashboard.variables,
        "orgName":this.selectedDashboard.orgName,
        "type":'edit',
        "dashboard":dashboardJson.dashboard,
        "organisation":dashboardJson.organisation,
        "range":dashboardJson.range,
        "from":dashboardJson.from,
        "to":dashboardJson.to,       
        "senderEmailAddress": dashboardJson.senderEmailAddress, 
        "receiverEmailAddress": dashboardJson.email, 
        "mailSubject": dashboardJson.mailSubject,
         "mailBodyTemplate": dashboardJson.mailBodyTemplate, 
         "receiverCCEmailAddress": dashboardJson.receiverCCEmailAddress, 
         "receiverBCCEmailAddress": dashboardJson.receiverBCCEmailAddress,
         "rangeText":dashboardJson.rangeText,
         "workflowId": this.selectedDashboard.workflowId ,
         "loadTime":  dashboardJson.loadTime   
      }
    };
    this.router.navigate(['InSights/Home/edit-dashboard'], navigationExtras);
  }
  refresh() {
    this.getAllConfig();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;

  }
  enableButtons(i) {
    this.onRadioBtnSelect = true;
    this.disablebutton[i] = false;
  }
    async getAllConfig() {
    this.isDatainProgress=true;
    var self = this;
    self.refreshRadio = false;
    let currentUserWithOrgs = await this.grafanaService.getCurrentUserWithOrgs();
    if (currentUserWithOrgs.status === "success") {
      this.orgArr = currentUserWithOrgs.data.orgArray;
    }

    this.dashConfigList = await this.grafanaService.fetchDashboardConfigs();
    if (this.dashConfigList != null && this.dashConfigList.status == "success") {
    this.dashboardDatasource.data = this.dashConfigList.data.filter(x=>x.source==='PLATFORM');
    }
    for(let i=0;i<this.dashboardDatasource.data.length;i++){
      this.disablebutton.push(true);
    }
    this.isDatainProgress=false;
    this.count = 0;
    this.dashConfigList.data.forEach(element => {
      let dashJson=JSON.parse(element.dashboardJson);
      let organisationObj = this.orgArr.filter(e => e.orgId === Number(dashJson.organisation));
      this.dashboardDatasource.data[this.count].orgName = organisationObj[0].name;
      this.count += 1;
    }); 
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
}



