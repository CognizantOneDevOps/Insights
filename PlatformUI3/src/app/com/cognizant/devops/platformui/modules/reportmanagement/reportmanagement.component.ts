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
import { DatePipe } from '@angular/common';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import {
  MatTableDataSource,
  MatRadioChange,
  MatSort,
  MatPaginator,
  MatDialog,
  MatSlideToggleChange
} from '@angular/material';
import { Router, NavigationExtras } from '@angular/router';
import { DataSharedService } from '@insights/common/data-shared-service';
import { ReportManagementService } from '@insights/app/modules/reportmanagement/reportmanagement.service';
import { WorkflowHistoryDetailsDialog } from '@insights/app/modules/reportmanagement/workflow-history-details/workflow-history-details-dialog';

@Component({
  selector: 'app-reportmanagement',
  templateUrl: './reportmanagement.component.html',
  styleUrls: ['./reportmanagement.component.css', './../home.module.css']
})
export class ReportManagementComponent implements OnInit {
  timeZone: string = '';
  enableRefresh: boolean = true;
  disableRetry: boolean = true;
  configParams: string;
  selectedReport: any;
  CheckboxVar: boolean;
  showPagination: boolean = true;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  MAX_ROWS_PER_TABLE = 5;
  @ViewChild(MatSort) sort: MatSort;
  displayedColumns: string[];
  userDataSourceFromService: any;
  userDataSource = new MatTableDataSource<any>();
  name: string = 'PRODUCTIVITY_REPORT';
  detailedRecords = [];
  showThrobber: boolean = false;
  isActive: boolean;
  reportSourceList = { data: [] };
  changeState: boolean;
  dateObj: Date;
  dateObjFormatted: String;
  clicked = new Array();
  disableDelete: boolean = true;
  disableEdit: boolean = true;
  previousActiveIndex = -1;
  pageRefreshed: boolean = false;

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
      'select',
      'reportName',
      'schedule',
      'lastRun',
      'nextRun',
      'status',
      'active',
      'details'
    ];
    this.getAssesmentReport();
    this.userDataSource.paginator = this.paginator;
  }

  async getAssesmentReport() {
    this.detailedRecords = [];
    this.userDataSourceFromService = await this.reportmanagementService.getAssesmentReport();
    this.reportSourceList.data = this.userDataSourceFromService.data;
    this.showThrobber = false;
    this.showPagination = true;
    var dataArray = this.userDataSourceFromService.data;
    if (dataArray != undefined) {
      dataArray.forEach(key => {
        var obj = key;
        if (typeof obj['lastRun'] !== 'undefined') {
          if (obj['lastRun'] === '') {
            obj['lastRun'] = '-';
          } else {
            obj['lastRun'] = obj['lastRun'] * 1000;
            this.dateObj = new Date(obj['lastRun']);
            this.dateObjFormatted = this.datePipe.transform(
              this.dateObj,
              'yyyy-MM-dd HH:mm:ss'
            );
            obj['lastRun'] = this.dateObjFormatted;
          }
        }
        if (typeof obj['nextRun'] !== 'undefined') {
          if (obj['nextRun'] === '') {
            obj['nextRun'] = '-';
          } else {
            obj['nextRun'] = obj['nextRun'] * 1000;
            this.dateObj = new Date(obj['nextRun']);
            this.dateObjFormatted = this.datePipe.transform(
              this.dateObj,
              'yyyy-MM-dd HH:mm:ss'
            );
            obj['nextRun'] = this.dateObjFormatted;
          }
        }
        this.detailedRecords.push(obj);
      });
      this.userDataSource.data = this.detailedRecords;
      this.userDataSource.paginator = this.paginator;
    }
    this.userDataSource.data.forEach(element => {
      this.clicked.push(true);
    });
  }

  updateStatus(event: MatSlideToggleChange, id, element) {
    var self = this;
    var title = 'Update Active/Inactive State';
    var state = element.isActive ? 'Active' : 'Inactive';
    var dialogmessage =
      'Are you sure you want to <b>' +
      state +
      '</b> report <b>' +
      self.selectedReport.reportName +
      '</b> ?';
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedReport.reportName,
      'ALERT',
      '40%'
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.changeState = event.checked;
        var setStatusRequestJson = {};
        setStatusRequestJson['id'] = id;
        setStatusRequestJson['isActive'] = event.checked;
        this.reportmanagementService
          .setActiveStatus(JSON.stringify(setStatusRequestJson))
          .then(function(data) {
            if (data.status == 'success') {
              console.log('status changed');
            } else {
              this.messageDialog.showApplicationsMessage(
                'Failed to update state.Please check logs for more details.',
                'ERROR'
              );
            }
          });
      } else {
        element.isActive = !event.checked;
      }
    });
  }

  Refresh() {
    this.selectedReport = '';
    this.clicked = [];
    this.userDataSource.data.forEach(element => {
      this.clicked.push(true);
    });
    this.pageRefreshed = true;
    this.disableDelete = true;
    this.disableEdit = true;
  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), '');
    this.userDataSource.paginator = this.paginator;
    this.userDataSource.data.forEach(element => {
      this.clicked.push(true);
    });
  }
  ngAfterViewInit() {
    this.userDataSource.paginator = this.paginator;
  }

  editReportConfiguration() {
    if (this.selectedReport === undefined) {
      this.messageDialog.showApplicationsMessage(
        'Please select a record to edit',
        'ERROR'
      );
      return;
    }
    const paramCheck = this.reportSourceList.data.filter(
      f => f.reportName === this.selectedReport.reportName
    );
    const param = paramCheck.length > 0 ? paramCheck[0] : {};
    this.configParams = JSON.stringify({ type: 'edit', data: param });
    this.naivagate();
  }

  addReport() {
    this.configParams = JSON.stringify({ type: 'save' });
    this.naivagate();
  }

  deleteReport() {
    var self = this;
    var title = 'Delete Report';
    var dialogmessage =
      'Do you want to delete <b>' +
      self.selectedReport.reportName +
      '</b> ? <br> <b> Please note: </b> The action of deleting ' +
      '<b>' +
      self.selectedReport.reportName +
      '</b> cannot be undone. Do you want to continue ? ';
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedReport.reportName,
      'ALERT',
      '40%'
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        self.reportmanagementService
          .deleteAssesmentReport(self.selectedReport.configId)
          .then(function(data) {
            if (data.status == 'success') {
              self.messageDialog.showApplicationsMessage(
                '<b>' +
                  self.selectedReport.reportName +
                  '</b> deleted successfully.',
                'SUCCESS'
              );
              self.list();
              self.Refresh();
            } else if (data.message == 'Executions found in history') {
              self.messageDialog.showApplicationsMessage(
                'Report cannot be deleted as executions were found in the history.You can change the state to INACTIVE if required.',
                'ERROR'
              );
            } else {
              self.messageDialog.showApplicationsMessage(
                'Failed to save the report.Please check logs.',
                'ERROR'
              );
            }
          })
          .catch(function(data) {});
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
        reportparam: this.configParams
      }
    };
    this.router.navigate(
      ['InSights/Home/report-configuration'],
      navigationExtras
    );
  }

  showWorkflowHistoryDetailsDialog(reportName: String, configId: String) {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(WorkflowHistoryDetailsDialog, {
        panelClass: 'workflow-history-details-dialog-container',
        disableClose: true,
        data: {
          reportName: reportName,
          configId: configId,
          timeZone: this.timeZone
        }
      });
    }
  }

  radioChange(event: MatRadioChange, index) {
    this.disableDelete = false;
    this.disableEdit = false;
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
    if (event.value.status == 'ABORTED') {
      this.disableRetry = false;
    } else {
      this.disableRetry = true;
    }
  }

  retry() {
    var self = this;
    this.reportmanagementService
      .setRetryStatus(this.selectedReport.configId)
      .then(function(data) {
        if (data.status == 'success') {
          self.messageDialog.showApplicationsMessage(
            'Status has been updated to RESTART.',
            'SUCCESS'
          );
          self.list();
        } else {
          self.messageDialog.showApplicationsMessage(
            'Failed to update the state.Please check logs for more details.',
            'ERROR'
          );
        }
      });
  }
}
