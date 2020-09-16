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
import { DataArchivingService } from '@insights/app/modules/settings/dataarchiving/dataarchiving-service';
import { DataSharedService } from '@insights/common/data-shared-service';
import {
  MatTableDataSource,
  MatRadioChange,
  MatSlideToggleChange,
  MatPaginator
} from '@angular/material';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DatePipe } from '@angular/common';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { MatDialog } from '@angular/material';
import { DataArchiveDetailsDialog } from '@insights/app/modules/settings/dataarchiving/data-archive-details/data-archive-details-dialog';
import { DataArchiveConfigureURLDialog } from '@insights/app/modules/settings/dataarchiving/data-archive-configureurl/data-archive-configureurl-dialog';

@Component({
  selector: 'app-dataarchiving',
  templateUrl: './dataarchiving.component.html',
  styleUrls: ['./dataarchiving.component.css', './../../home.module.css']
})
export class DataArchivingComponent implements OnInit {
  displayedColumns = [];
  archivalDatasource = new MatTableDataSource<any>();
  showAdd: boolean = false;
  showList: boolean = true;
  enableDelete: boolean = false;
  enableRefresh: boolean = false;
  enableEdit: boolean = false;
  enableBrowse: boolean = false;
  enableAdd: boolean = true;
  refreshRadio: boolean = false;
  archivalName: any;
  archiveNameList: any = [];
  archiveList: any;
  status: boolean;
  action: boolean;
  showMessage: string;
  showDetail: boolean = false;
  showConfirmMessage: string;
  sourceUrl: any;
  startDate: string = null;
  endDate: string = null;
  noOfDays: number = null;
  selectedArchivedData: any;
  dataSourceUrl: string;
  actionType: any;
  startDateInput: Date = null;
  endDateInput: Date = null;
  today = new Date();
  regex = new RegExp('^[a-zA-Z0-9_]*$');
  count: number;
  containerCount: number;
  dateObj: Date;
  dateObjFormatted: String;
  timeZone: string = '';
  MAX_ROWS_PER_TABLE = 5;
  previousActiveIndex = -1;
  clicked = new Array();
  pageRefreshed: boolean = false;
  archivedRecordDetailData = { data: [] };
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(
    private dataArchivingService: DataArchivingService,
    private datepipe: DatePipe,
    private dialog: MatDialog,
    public messageDialog: MessageDialogService,
    private dataShare: DataSharedService
  ) {
    this.getExistingArchivedData();
  }

  ngOnInit() {
    var rightNow = this.dataShare.getTimeZone();
    this.timeZone = rightNow
      .split(/\s/)
      .reduce((response, word) => (response += word.slice(0, 1)), '');
    this.archivalDatasource.paginator = this.paginator;
    this.archivalDatasource.data.forEach(element => {
      this.clicked.push(true);
    });
  }

  ngAfterViewInit() {
    this.archivalDatasource.paginator = this.paginator;
  }

  add() {
    this.enableBrowse = false;
    this.enableDelete = false;
    this.showDetail = false;
    this.enableAdd = false;
    if (this.containerCount < 5) {
      this.showAdd = true;
      this.showList = false;
    } else {
      this.messageDialog.showApplicationsMessage(
        'Max count of reload is set to 5. Please delete unused containers to proceed.',
        'WARN'
      );
      this.showDetail = true;
      this.enableAdd = true;
    }
    this.archivalName = '';
    this.startDate = '';
    this.endDate = '';
    this.noOfDays = undefined;
    this.actionType = 'save';
    this.startDateInput = undefined;
    this.endDateInput = undefined;
  }

  radioChange(event: MatRadioChange, index) {
    this.enableDelete = true;
    this.enableBrowse = true;
    this.enableEdit = true;
    if (this.previousActiveIndex == -1 && event.value.status != 'INPROGRESS') {
      this.clicked[index] = false;
      this.previousActiveIndex = index;
    } else if (
      this.pageRefreshed &&
      index === this.previousActiveIndex &&
      event.value.status != 'INPROGRESS'
    ) {
      this.clicked[this.previousActiveIndex] = false;
      this.pageRefreshed = false;
    } else if (event.value.status != 'INPROGRESS') {
      this.clicked[index] = false;
      this.clicked[this.previousActiveIndex] = true;
      this.previousActiveIndex = index;
    } else if (event.value.status == 'INPROGRESS') {
      this.clicked[this.previousActiveIndex] = true;
      this.previousActiveIndex = index;
    }
  }

  public async getExistingArchivedData() {
    var self = this;
    this.archiveNameList = [];
    this.archiveList = [];
    this.archiveList = await self.dataArchivingService.listArchivedRecord();
    if (this.archiveList != null && this.archiveList.status == 'success') {
      this.archivalDatasource.data = this.archiveList.data.sort(
        (a, b) => a.archivalName > b.archivalName
      );
      this.archivedRecordDetailData.data = this.archiveList.data;
      this.archivalDatasource.paginator = this.paginator;
      this.count = 0;
      this.containerCount = 0;
      self.showDetail = true;
      for (var element of this.archivalDatasource.data) {
        if (this.count < this.archivalDatasource.data.length) {
          this.dateObj = new Date(
            this.archivalDatasource.data[this.count].expiryDate * 1000
          );
          this.dateObjFormatted = this.dataShare.convertDateToSpecificDateFormat(this.dateObj, "yyyy-MM-dd HH:mm:ss");
          this.archivalDatasource.data[
            this.count
          ].expiryDate = this.dateObjFormatted;
          this.dateObj = new Date(
            this.archivalDatasource.data[this.count].createdOn * 1000
          );
          this.dateObjFormatted = this.dataShare.convertDateToSpecificDateFormat(this.dateObj, "yyyy-MM-dd HH:mm:ss");
          this.archivalDatasource.data[
            this.count
          ].createdOn = this.dateObjFormatted;
          if (
            this.archivalDatasource.data[this.count].status == 'ACTIVE' ||
            this.archivalDatasource.data[this.count].status == 'INPROGRESS'
          ) {
            this.containerCount += 1;
          }
          if (this.archivalDatasource.data[this.count].status == 'ACTIVE') {
            this.archivalDatasource.data[this.count].action = true;
          } else if (
            this.archivalDatasource.data[this.count].status == 'INACTIVE'
          ) {
            this.archivalDatasource.data[this.count].action = false;
          } else {
            this.archivalDatasource.data[this.count].action = undefined;
          }
          this.count += 1;
        }
      }

      this.archivalDatasource.data.forEach(element => {
        this.clicked.push(true);
      });
      this.displayedColumns = [
        'radio',
        'ArchivalName',
        'DataSourceUrl',
        'ExpiryDate',
        'Status',
        'Action'
      ];
      setTimeout(() => {
        this.showConfirmMessage = '';
      }, 3000);
    } else {
      self.showMessage = 'Something wrong with Service.Please try again.';
      self.messageDialog.showApplicationsMessage(
        'Something wrong with Service.Please try again.',
        'ERROR'
      );
    }
  }

  validateArchiveData() {
    var checkname = this.regex.test(this.archivalName);
    if (
      this.archivalName == '' ||
      this.startDateInput == undefined ||
      this.endDateInput == undefined ||
      this.noOfDays == undefined
    ) {
      this.messageDialog.showApplicationsMessage(
        'Please fill mandatory fields.',
        'ERROR'
      );
    } else if (!checkname) {
      this.messageDialog.showApplicationsMessage(
        'Please enter valid archival name, it contains only alphanumeric character and underscore',
        'ERROR'
      );
    } else if (this.startDateInput > this.endDateInput) {
      this.messageDialog.showApplicationsMessage(
        'Start date should be less than End date',
        'ERROR'
      );
    } else {
      this.SaveData();
    }
  }

  async SaveData() {
    var self = this;
    this.showDetail = false;
    var archiveAPIRequestJson = {};
    if (this.actionType == 'save') {
      archiveAPIRequestJson['archivalName'] = self.archivalName;
      archiveAPIRequestJson['startDate'] = self.startDate;
      archiveAPIRequestJson['endDate'] = self.endDate;
      archiveAPIRequestJson['daysToRetain'] = self.noOfDays;
      archiveAPIRequestJson['author'] = this.dataShare.getUserName();
      var dialogmessage =
        ' You have created a new Archive data <b>' +
        self.archivalName +
        '</b>. Do you want to continue ? ';
      var title = 'Save ' + this.archivalName;
      const dialogRef = this.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        '',
        'ALERT',
        '40%'
      );
      dialogRef.afterClosed().subscribe(result => {
        if (result == 'yes') {
          this.dataArchivingService
            .saveArchivalRecord(JSON.stringify(archiveAPIRequestJson))
            .then(function (response) {
              if (response.status == 'success') {
                self.messageDialog.showApplicationsMessage(
                  '<b>' +
                  self.archivalName +
                  '</b> saved successfully. Data is being provisioned, Please revisit this screen after sometime.',
                  'SUCCESS'
                );
                self.getExistingArchivedData();
                self.refresh();
                self.showDetail = true;
              } else if (response.message === 'Archival Name already exists.') {
                self.messageDialog.showApplicationsMessage(
                  '<b>' +
                  self.archivalName +
                  '</b> already exists. Please try again with a new name.',
                  'ERROR'
                );
              } else if (response.message === 'Data Archival agent not present.') {
                self.messageDialog.showApplicationsMessage(
                  'Please register agent before saving record.',
                  'ERROR'
                );
              }
              else {
                self.messageDialog.showApplicationsMessage(
                  'Failed to save the archive data.Please check logs.',
                  'ERROR'
                );
              }
            });
        }
      });
    }
  }

  delete() {
    var self = this;
    if (
      self.selectedArchivedData.status == 'INACTIVE' ||
      self.selectedArchivedData.status == 'INPROGRESS'
    ) {
      var title = 'Delete';
      var dialogmessage =
        'Do you want to delete <b>' +
        self.selectedArchivedData.archivalName +
        '</b> ? <br> <b> Please note: </b> The action of deleting ' +
        '<b>' +
        self.selectedArchivedData.archivalName +
        '</b> cannot be undone. Do you want to continue ? ';
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        self.selectedArchivedData.archivalName,
        'ALERT',
        '40%'
      );
      dialogRef.afterClosed().subscribe(result => {
        if (result == 'yes') {
          this.dataArchivingService
            .deleteArchivedData(self.selectedArchivedData.archivalName)
            .then(function (data) {
              if (data.status == 'success') {
                self.messageDialog.showApplicationsMessage(
                  '<b>' +
                  self.selectedArchivedData.archivalName +
                  '</b> deleted successfully.',
                  'SUCCESS'
                );
                self.getExistingArchivedData();
                self.refresh();
              }
            })
            .catch(function (data) {
              self.showConfirmMessage = 'service_error';
              self.getExistingArchivedData();
            });
        }
      });
    } else {
      self.messageDialog.showApplicationsMessage(
        'Please inactivate container before deleting it.',
        'WARN'
      );
    }
  }

  refresh() {
    this.selectedArchivedData = '';
    this.clicked = [];
    this.archivalDatasource.data.forEach(element => {
      this.clicked.push(true);
    });
    this.pageRefreshed = true;
    this.showList = true;
    this.showAdd = false;
    this.getExistingArchivedData;
    this.enableDelete = false;
    this.enableBrowse = false;
    this.enableAdd = true;
    this.enableEdit = false;
    this.showDetail = true;
  }

  getstartDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.startDateInput = new Date(event.value);
    this.startDate = this.dataShare.convertDateToSpecificDateFormat(this.startDateInput, "yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  getendDate(type: string, event: MatDatepickerInputEvent<Date>) {
    this.endDateInput = new Date(event.value);
    this.endDate = this.dataShare.convertDateToSpecificDateFormat(this.endDateInput, "yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  updateStatus(event: MatSlideToggleChange, selectedArchivedData) {
    var self = this;
    this.action = event.checked;
    var title = 'Update Status';
    if (this.action == true && selectedArchivedData.status == 'INACTIVE') {
      var dialogmessage =
        'Do you want to make container <b>' +
        self.selectedArchivedData.archivalName +
        ' ACTIVE</b> ? ';
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        self.selectedArchivedData.archivalName,
        'ALERT',
        '40%'
      );
      dialogRef.afterClosed().subscribe(result => {
        if (result == 'yes') {
          this.dataArchivingService
            .activateArchivedData(selectedArchivedData.archivalName)
            .then(function (data) {
              if (data.status == 'success') {
                self.messageDialog.showApplicationsMessage(
                  'Container activated successfully.',
                  'SUCCESS'
                );
                self.getExistingArchivedData();
                self.refresh();
                selectedArchivedData.action = true;
              } else {
                self.messageDialog.showApplicationsMessage(
                  'Failed to update the status.Please check logs for more details.',
                  'ERROR'
                );
              }
            });
        }
        selectedArchivedData.action = false;
      });
    } else if (
      this.action == false &&
      selectedArchivedData.status == 'ACTIVE'
    ) {
      var dialogmessage =
        'Do you want to make container <b>' +
        self.selectedArchivedData.archivalName +
        ' INACTIVE</b>? ';
      const dialogRef = self.messageDialog.showConfirmationMessage(
        title,
        dialogmessage,
        self.selectedArchivedData.archivalName,
        'ALERT',
        '30%'
      );
      dialogRef.afterClosed().subscribe(result => {
        if (result == 'yes') {
          this.dataArchivingService
            .inactivateArchivedData(selectedArchivedData.archivalName)
            .then(function (data) {
              if (data.status == 'success') {
                self.messageDialog.showApplicationsMessage(
                  'Container inactivated successfully.',
                  'SUCCESS'
                );
                self.getExistingArchivedData();
                self.refresh();
                selectedArchivedData.action = false;
              } else {
                self.messageDialog.showApplicationsMessage(
                  'Failed to update the status.Please check logs for more details.',
                  'ERROR'
                );
              }
            });
        }
        selectedArchivedData.action = true;
      });
    }
  }

  onNavigate(sourceUrl: any): void {
    window.open(sourceUrl, '_blank');
    this.refresh();
  }

  archiveRecordsDetails() {
    const paramCheck = this.archivedRecordDetailData.data.filter(
      f => f.archivalName === this.selectedArchivedData.archivalName
    );
    const param = paramCheck.length > 0 ? paramCheck[0] : {};
    this.dialog.open(DataArchiveDetailsDialog, {
      panelClass: 'data-archive-details-dialog-container',
      disableClose: true,
      width: '450px',
      data: { record: param }
    });
  }

  configureURL() {
    var sourceURL;
    const paramCheck = this.archivedRecordDetailData.data.filter(
      f => f.archivalName === this.selectedArchivedData.archivalName
    );
    const param = paramCheck.length > 0 ? paramCheck[0] : {};
    var name = param.archivalName;
    if ('sourceUrl' in param) {
      sourceURL = param.sourceUrl;
    } else {
      sourceURL = '';
    }

    let dialogRef = this.dialog.open(DataArchiveConfigureURLDialog, {
      panelClass: 'data-archive-configureurl-dialog-container',
      disableClose: true,
      width: '650px',
      data: { name: name, sourceURL: sourceURL }
    });

    dialogRef.afterClosed().subscribe(result => {
      this.getExistingArchivedData();
      this.refresh();
    });
  }

  clearValues() {
    this.archivalName = '';
    this.startDateInput = null;
    this.endDateInput = null;
    this.noOfDays = null;
  }
}
