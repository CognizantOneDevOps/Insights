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

import { Component, OnInit, Inject, ViewChild } from "@angular/core";
import {
  MatDialog,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { RestCallHandlerService } from "@insights/common/rest-call-handler.service";
import { HealthCheckService } from "./healthcheck.service";
import { TitleCasePipe } from "@angular/common";
import { DatePipe } from "@angular/common";
import { DataSharedService } from "@insights/common/data-shared-service";

@Component({
  selector: "healthcheck-show-details-dialog",
  templateUrl: "./healthcheck-show-details-dialog.html",
  styleUrls: [
    "./../home.module.scss",
    "./healthcheck-show-details-dialog.scss",
  ],
})
export class ShowDetailsDialog implements OnInit {
  showContent: boolean;
  showThrobber: boolean = false;
  checkResponseData: boolean;
  pathName: string;
  detailType: string;
  columnLength: number;
  resultsLength: number = 6;
  agentDetailedNode = [];
  agentDetailedDatasource = new MatTableDataSource([]);
  agentFailureDetailsDatasource = new MatTableDataSource([]);
  agentFailureRecords = [];
  headerArrayDisplay = [];
  masterHeader = new Map<String, String>();
  finalHeaderToShow = new Map<String, String>();
  displayedColumns: string[] = ["inSightsTime", "message"];
  headerSet = new Set();
  showAgentFailureTab: boolean = false;
  timeZoneAbbr: string = "";

  constructor(
    public dialogRef: MatDialogRef<ShowDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private restCallHandlerService: RestCallHandlerService,
    private healthCheckService: HealthCheckService,
    private titlecase: TitleCasePipe,
    public datePipe: DatePipe,
    private dataShare: DataSharedService
  ) {
    this.timeZoneAbbr = this.dataShare.getTimeZoneAbbr();
    this.fillMasterHeaderData();
  }

  ngOnInit() {
    this.detailType = this.data.detailType;
    if (
      this.detailType == "Platform Service" ||
      this.detailType == "Platform Workflow" ||
      this.detailType == "Platform Engine" ||
      this.detailType == "Platform WebhookSubscriber" ||
      this.detailType == "Platform WebhookEngine" ||
      this.detailType == "Platform DataArchivalEngine"
    ) {
      this.loadDetailsDialogInfo();
    } else {
      this.loadDetailsDialogInfo();
      this.loadAgentFailureDetails();
    }
  }

  ngAfterViewInit() { }

  fillMasterHeaderData() {
    this.masterHeader.set("execId", "Execution ID");
    this.masterHeader.set(
      "inSightsTime",
      "Execution Time (" + this.timeZoneAbbr + ")"
    );
    this.masterHeader.set("status", "Status");
    this.masterHeader.set("message", "Message");
  }

  loadDetailsDialogInfo(): void {
    this.showThrobber = true;
    this.showContent = !this.showThrobber;
    this.checkResponseData = true;
    this.healthCheckService
      .loadHealthConfigurations(
        this.data.toolName,
        this.data.categoryName,
        this.data.agentId
      )
      .then((data) => {
        console.log(data);
        this.showThrobber = false;
        this.showContent = !this.showThrobber;
        var dataArray = data.data;
        console.log(dataArray);
        if (dataArray != undefined) {
          this.pathName = this.data.pathName;
          if (
            dataArray.length === 0 &&
            this.data.detailType != "Platform Service"
          ) {
            this.checkResponseData = false;
          }

          if (
            this.detailType == "Platform Service" ||
            this.detailType == "Platform Workflow" ||
            this.detailType == "Platform Engine" ||
            this.detailType == "Platform WebhookSubscriber" ||
            this.detailType == "Platform WebhookEngine" ||
            this.detailType == "Platform DataArchivalEngine"
          ) {
            this.showAgentFailureTab = false;
          } else {
            this.showAgentFailureTab = true;
          }

          dataArray.forEach((obj) => {

            if (typeof obj["inSightsTime"] !== "undefined") {
              var utcSeconds=  obj["inSightsTime"];
              var inSightsTimeX = new Date(0); 
              inSightsTimeX.setUTCSeconds(utcSeconds);  
              obj["inSightsTime"] = this.datePipe.transform(
                inSightsTimeX,
                "yyyy-MM-dd HH:mm:ss"
              );       
            }
            if (typeof obj["status"] !== "undefined") {
              obj["status"] = this.titlecase.transform(obj["status"]);
            }
            if (typeof obj["message"] !== "undefined") {
              obj["message"] = obj["message"].slice(0, 120);
            }
            this.agentDetailedNode.push(obj);
            for (var attr in obj) {
              // fill data array in set , Only those header which mention in masterHeader
              if (this.masterHeader.has(attr)) {
                this.headerSet.add(attr);
              }
            }
          })
          this.agentDetailedDatasource.data = this.agentDetailedNode;
          console.log(this.agentDetailedDatasource.data);

          this.showSelectedField();
        }
      });
  }

  loadAgentFailureDetails(): void {
    this.healthCheckService
      .getAgentFailureDetails(
        this.data.toolName,
        this.data.categoryName,
        this.data.agentId
      )
      .then((data) => {
        // Method body
        var failureRecords = data.data;
        console.log(failureRecords);
        if (failureRecords != undefined) {
          failureRecords.forEach((obj) => {
            if (typeof obj["inSightsTime"] !== "undefined") {
              obj["inSightsTime"] = this.dataShare.formatInsightsTime(obj["inSightsTime"]);
            }

            if (typeof obj["message"] !== "undefined") {
              obj["message"] = obj["message"];
            }
            this.agentFailureRecords.push(obj);
          })
          this.agentFailureDetailsDatasource.data = this.agentFailureRecords;
        }
      });
  }

  showSelectedField(): void {
    //Define sequence of headerSet according to mater array and remove unwanted header
    this.masterHeader.forEach((value: string, key: string) => {
      if (this.headerSet.has(key)) {
        this.finalHeaderToShow.set(key, value);
      }
    });
    this.columnLength = this.finalHeaderToShow.size;
    // create headerArrayDisplay from map keys
    if (this.finalHeaderToShow.size > 0) {
      this.headerArrayDisplay = Array.from(this.finalHeaderToShow.keys());
    }
  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

  hideTextOverflow(text: any) {
    if (text !== undefined && text.length > 50) {
      return text.slice(0, 50) + "...";
    } else {
      return text;
    }
  }
}
