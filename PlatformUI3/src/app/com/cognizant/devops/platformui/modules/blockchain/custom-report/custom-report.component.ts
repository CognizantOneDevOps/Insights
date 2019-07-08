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

import { Component, OnInit, ViewChild } from '@angular/core';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { MatTableDataSource, MatSort, MatPaginator, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { Router, NavigationExtras, ActivatedRoute } from '@angular/router';
import { QueryBuilderService } from './custom-report-service';

@Component({
    selector: 'app-custom-report',
    templateUrl: './custom-report.component.html',
    styleUrls: ['./custom-report.component.css', './../../home.module.css']
  })
  export class CustomReportComponent implements OnInit {

     
    reportSourceList = {"data": []};
    selectedAgent: any;
    displayedColumns: string[];
    showList: boolean = false;
    showThrobber: boolean;
    showMessage: string;
    showConfirmMessage: string;
    showDetail: boolean = false;
    MAX_ROWS_PER_TABLE = 10;
    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;
    userDataSource = new MatTableDataSource<any>();
    configParams: string;
    receivedParam: any;


    ngOnInit(){
        console.log("compoennt --");
        this.route.queryParams.subscribe(params => {
            if (params["querystatus"] != undefined) {
              this.receivedParam = params["querystatus"];
              var queryConfigstatusCode = params["queryConfigstatusCode"];
              var showConfirmMessage = this.receivedParam;
              console.log(queryConfigstatusCode + " " + showConfirmMessage);
              if (queryConfigstatusCode == undefined) {
                queryConfigstatusCode = 'WARN';
              }
              setTimeout(() => this.messageDialog.showApplicationsMessage(showConfirmMessage, queryConfigstatusCode));
            }
        });
    }

    ngAfterViewInit() {
        this.userDataSource.sort = this.sort;
        this.userDataSource.paginator = this.paginator;
    }
    

    constructor(public messageDialog: MessageDialogService,public router: Router,
        private route: ActivatedRoute, private queryBuilderService:QueryBuilderService) {
        this.getCustomReports();
    }

    addReport(){
        console.log('add');
        this.configParams = JSON.stringify({ 'type': 'add', 'data': this.reportSourceList });
        this.naivagate();
    }

    naivagate() {
        let navigationExtras: NavigationExtras = {
            skipLocationChange: true,
            queryParams: {
              "reportparam": this.configParams
            }
          };
          //console.log(navigationExtras);
          this.router.navigate(['InSights/Home/reportconfiguration'], navigationExtras);
    }

    editReport(){
        console.log('editReport');
        console.log(this.selectedAgent);
        if(this.selectedAgent === undefined){
            this.messageDialog.showApplicationsMessage("Please select a record to edit", "ERROR");
            return;
        }
        const paramCheck = this.reportSourceList.data.filter(f => f.reportName === this.selectedAgent.reportName);
        const param = paramCheck.length > 0 ? paramCheck[0]: {};
        console.log(param);
        this.configParams = JSON.stringify({ 'type': 'update', 'data': param });
        this.naivagate();
    }

    async deleteReport() {
        if(this.selectedAgent === undefined){
            this.messageDialog.showApplicationsMessage("Please select a record to Delete", "ERROR");
            return;
        }
        console.log('deleteReport', this.selectedAgent.reportName);
        let result = await this.queryBuilderService.deleteQuery(this.selectedAgent.reportName);
        if (result.status == "success") {
            this.getCustomReports();
            setTimeout(() => this.messageDialog.showApplicationsMessage("Deleted Successfully","SUCCESS"));
        } else {
            this.messageDialog.showApplicationsMessage("DB Operation Failed!", "ERROR");
        }
    }

   async getCustomReports() {
        this.showThrobber = true;
        this.userDataSource = new MatTableDataSource();
        let custReportList = await this.queryBuilderService.fetchQueries();
        console.log(custReportList);
        if (custReportList != null && custReportList.data.length > 0) {
          this.showThrobber = false;
          this.showDetail = true;
          this.displayedColumns = ['select', 'reportName','frequency'];
          this.userDataSource.data = custReportList.data;
          this.reportSourceList.data = custReportList.data;
          this.userDataSource.sort = this.sort;
          this.userDataSource.paginator = this.paginator;
        } else {
            this.addReport();
          //this.showMessage = "Something wrong with Service, Please try again.";
          //this.messageDialog.showApplicationsMessage("Something wrong with Service, Please try again.", "ERROR");
        }
      }

      async testReport(){
        console.log('testReport');
        console.log(this.selectedAgent);
        if(this.selectedAgent === undefined){
            this.messageDialog.showApplicationsMessage("Please select a record to edit", "ERROR");
            return;
        }
        let result = await this.queryBuilderService.testQuery(this.selectedAgent.reportName ,this.selectedAgent.frequency);
        console.log(result);
       }

  }