/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
import { DataSharedService } from "@insights/common/data-shared-service";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { OfflineService } from "@insights/app/modules/offline-data-processing/offline-service";
import { Component, OnInit, Inject } from "@angular/core";

@Component({
  selector: "app-offline-details",
  templateUrl: "./offline-details.component.html",
  styleUrls: ["./offline-details.component.scss", "./../../home.module.scss"],
})
export class OfflineDetailsComponent implements OnInit {
  displayedColumns = ["status", "retryCount", "message"];
  offlineDatasource = [{ status: "", retryCount: "", message: "" }];
  queryName: String;
  status: string;
  message: string;
  retryCount: string;
  timeZoneAbbr: String = "";
  showThrobber: boolean = false;
  timezone: String;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<OfflineDetailsComponent>,
    public offlineService: OfflineService,
    public dataShare: DataSharedService
  ) {}

  ngOnInit(): void {
    this.getOfflineDetails();
    this.queryName = this.data.element.queryName;
    this.timezone = this.dataShare.getTimeZone();
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
  }

  getOfflineDetails() {
    this.retryCount = this.data.element.retryCount;
    this.status = this.data.element.status;
    this.message = this.data.element.message;
    this.offlineDatasource = [
      {
        status: this.status,
        retryCount: this.retryCount,
        message: this.message,
      },
    ];
  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

  hideTextOverflow(text: any) {
    if (text !== undefined && text.length > 70) {
      return text.slice(0, 70) + "...";
    } else {
      return text;
    }
  }
}
