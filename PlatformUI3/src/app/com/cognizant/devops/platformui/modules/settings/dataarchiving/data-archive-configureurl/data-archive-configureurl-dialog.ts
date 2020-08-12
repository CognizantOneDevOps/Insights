/*********************************************************************************
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
 *******************************************************************************/

import { DatePipe } from '@angular/common';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { DataArchivingService } from '@insights/app/modules/settings/dataarchiving/dataarchiving-service';

@Component({
  selector: 'data-archive-configureurl-dialog',
  templateUrl: './data-archive-configureurl-dialog.html',
  styleUrls: [
    './data-archive-configureurl-dialog.css',
    './../../../home.module.css'
  ]
})
export class DataArchiveConfigureURLDialog implements OnInit {
  inputDataSourceURL: string;
  archivalName: string;
  
  constructor(
    public dialogRef: MatDialogRef<DataArchiveConfigureURLDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public datePipe: DatePipe,
    public dataShare: DataSharedService,
    public messageDialog: MessageDialogService,
    public dataArchivingService: DataArchivingService
  ) {}

  ngOnInit() {
    this.archivalName = this.data.name;
    this.inputDataSourceURL = this.data.sourceURL;
  }

  /**
   * method to save Data Source URL
   *
   */
  saveURL() {
    var requestJson = {};
    var self = this;
    requestJson['archivalName'] = self.archivalName;
    requestJson['sourceUrl'] = self.inputDataSourceURL;
    var dialogMessage =
      'Are you sure about updating the Data Source URL for <b> ' +
      self.archivalName +
      '</b> ?';
    var title = 'Update DataSourceURL for ' + self.archivalName;
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogMessage,
      '',
      'ALERT',
      '40%'
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        self.dataArchivingService
          .updateDataSourceURL(JSON.stringify(requestJson))
          .then(function(response) {
            if (response.status == 'success') {
              console.log('Inside success');
              self.messageDialog.showApplicationsMessage(
                'Data Source URL for <b>' +
                  self.archivalName +
                  '</b> updated successfully.',
                'SUCCESS'
              );
              self.closeShowDetailsDialog();
            } else {
              self.messageDialog.showApplicationsMessage(
                'Error updating Data Source URL for <b>' +
                  self.archivalName +
                  '</b>',
                'ERROR'
              );
            }
          });
      } else {
        self.closeShowDetailsDialog();
      }
    });
  }

  /**
   * method to close the dialog
   */
  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }
}
