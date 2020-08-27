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

import { DatePipe } from "@angular/common";
import { Component, Inject, OnInit } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material";
import { DataSharedService } from "@insights/common/data-shared-service";

@Component({
  selector: "data-archive-details-dialog",
  templateUrl: "./data-archive-details-dialog.html",
  styleUrls: ["./data-archive-details-dialog.css"]
})
export class DataArchiveDetailsDialog implements OnInit {
  timezone: String;
  authorName: String;
  archivalName: string;
  startDate: string;
  endDate: string;
  daystoRetain: number;
  createdOnDate: string;
  constructor(
    public dialogRef: MatDialogRef<DataArchiveDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public datePipe: DatePipe,
    public dataShare: DataSharedService
  ) {}

  ngOnInit() {
    this.loadDataArchiveDetailsDialog();
    this.archivalName = this.data.record.archivalName;
    console.log("The data:", this.data);
    this.timezone = this.dataShare.getTimeZone();
  }

  ngAfterViewInit() {}

  /**
   * method to load records in the Dialog
   *
   */
  loadDataArchiveDetailsDialog() {
    var tempDate, dateObj;
    console.log("Author Name: ", this.data.record.author);
    if (this.data.record.author) {
      this.authorName = this.data.record.author;
    } else {
      this.authorName = "-";
    }
    this.createdOnDate = this.data.record.createdOn;
    tempDate = this.data.record.startDate * 1000;
    dateObj = new Date(tempDate);
    this.startDate = this.datePipe.transform(dateObj, "yyyy-MM-dd HH:mm:ss");
    tempDate = this.data.record.endDate * 1000;
    dateObj = new Date(tempDate);
    this.endDate = this.datePipe.transform(dateObj, "yyyy-MM-dd HH:mm:ss");
    this.daystoRetain = this.data.record.daysToRetain;
  }

  /**
   * method to close the dialog
   */
  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }
}
