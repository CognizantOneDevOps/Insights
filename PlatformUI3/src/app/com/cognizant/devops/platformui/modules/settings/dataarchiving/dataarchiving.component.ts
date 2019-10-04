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

import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { DataArchivingService } from '@insights/app/modules/settings/dataarchiving/dataarchiving-service';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MatTable } from '@angular/material';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';


@Component({
  selector: 'app-dataarchiving',
  templateUrl: './dataarchiving.component.html',
  styleUrls: ['./dataarchiving.component.css', './../../home.module.css']
})
export class DataArchivingComponent implements OnInit {

  settingsType: string;
  showConfirmMessage: string;
  showThrobber: boolean;
  serviceResponseForList: any;
  settingData = {};
  nextRunTime: string;
  lastRunTime: string;
  settingJsonstring: string;
  showApplicationMessage: String = "";
  dataJsonObj = {};
  sendJsonObj = {};
  activeFlag: string;
  lastModifiedByUser: String;
  iseditdisabled: boolean = false;
  displayedColumns = [];
  currentUserName: String;
  displayedColumnsNameMapping = [];
  backupRecord = [
    { value: '10', name: '10' },
    { value: '100', name: '100' },
    { value: '1000', name: '1000' }
  ];
  dataFreqRecord = [
    { value: 'Daily', name: 'Daily' },
    { value: 'Weekly', name: 'Weekly' },
    { value: 'Monthly', name: 'Monthly' }
  ];

  constructor(private dataArchivingService: DataArchivingService, private dataShare: DataSharedService,
    public messageDialog: MessageDialogService) {
    this.listData();
  }

  ngOnInit() {
    //this.dataShare.currentUser.subscribe(user => this.currentUserName = user)
    this.currentUserName = this.dataShare.getUserName();
    console.log(this.currentUserName);
    this.setInitailData();
  }

  async listData() {

    this.setInitailData();
    this.serviceResponseForList = await this.dataArchivingService.listDatapurgingdata("DATAPURGING");
    //console.log(this.serviceResponseForList);
    if (this.serviceResponseForList != null) {
      this.showThrobber = false;
      if (this.serviceResponseForList.status == "success") {
        if (this.serviceResponseForList.data != undefined) {
          this.iseditdisabled = false;
          this.settingData = JSON.parse(this.serviceResponseForList.data.settingsJson);
          for (let record of this.displayedColumnsNameMapping) {
            if (this.settingData.hasOwnProperty(record.key)) {
              record.value = this.settingData[record.key];
            } else {
              record.value = ""
            }
          }
        } else {
          this.iseditdisabled = true;
        }
      } else {
        this.showConfirmMessage = "Something wrong with service, please try again";
      }
    }
    setTimeout(() => {
      this.showConfirmMessage = "";
    }, 3000);
  }

  public setInitailData() {
    this.displayedColumnsNameMapping = [
      {
        key: 'backupRetentionInDays', value: "", displayName: 'Input number of days for Data Retention',
        infoText: "(please input only numeric values)", isReadOnly: false,
        type: "input", record: this.dataJsonObj
      },
      {
        key: 'backupFileLocation', value: "", displayName: 'Data Archival Location',
        infoText: "(please copy and paste the path of destination folder)", isReadOnly: false,
        type: "input", record: this.dataJsonObj
      },
      {
        key: 'dataArchivalFrequency', value: "", displayName: 'Please select the frequency of Data Archival',
        infoText: "(Daily / Weekly / Monthly)", isReadOnly: false,
        type: "list", record: this.dataFreqRecord
      },
      {
        key: 'rowLimit', displayName: 'Number of Records to Archive',
        infoText: "(once the limit is attained multiple files will be created)", isReadOnly: false,
        type: "list", record: this.backupRecord
      },
      {
        key: 'backupFileFormat', value: "JSON", displayName: 'Backup file format',
        infoText: "(Backup files will be archived only in JSON file format)", isReadOnly: true,
        type: "text", record: this.dataJsonObj
      },
      {
        key: 'lastRunTime', displayName: 'Last Runtime',
        infoText: "", isReadOnly: true,
        type: "text", record: this.dataJsonObj
      },
      {
        key: 'nextRunTime', displayName: 'Next Runtime',
        infoText: "", isReadOnly: true,
        type: "text", record: this.dataJsonObj
      }
    ];
    this.displayedColumns = ['displayName', 'input'];
  }

  public editData() {
    this.iseditdisabled = true;
  }

  public saveData(actionType) {
    for (let record of this.displayedColumnsNameMapping) {
      this.sendJsonObj[record.key] = (record.value == undefined ? "" : record.value)
    }
    //console.log(this.sendJsonObj)
    var self = this;
    this.settingsType = "DATAPURGING";
    this.activeFlag = "Y";
    this.lastModifiedByUser = this.currentUserName;
    this.settingJsonstring = JSON.stringify(self.sendJsonObj);
    //console.log(self.settingJsonstring);
    this.dataArchivingService.saveDatapurging(self.settingsType, self.activeFlag, self.lastModifiedByUser.toString(), self.settingJsonstring)
      .then(function (data) {
        //console.log("Setting " + data);
        if (data.status == "success") {
          //self.showConfirmMessage = "Settings saved successfully";
          self.showApplicationMessage = "Settings saved successfully"
          self.messageDialog.showApplicationsMessage("Settings saved successfully", "SUCCESS");
        } else {
          self.showConfirmMessage = "Failed to save settings";
          self.showApplicationMessage = "Failed to save settings"
          self.messageDialog.showApplicationsMessage("Failed to save settings", "ERROR");
        }
        self.listData();
      })
      .catch(function (data) {
        //self.showConfirmMessage = "Failed to save settings";
        self.showApplicationMessage = "Failed to save settings"
        self.messageDialog.showApplicationsMessage("Failed to save settings", "ERROR");
        self.listData();
      });

    this.iseditdisabled = true;
  }

}
