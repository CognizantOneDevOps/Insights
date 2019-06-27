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

import { Component, OnInit, Inject, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA, MatPaginator, MatTableDataSource } from '@angular/material';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { HealthCheckService } from './healthcheck.service';


@Component({
  selector: 'healthcheck-show-details-dialog',
  templateUrl: './healthcheck-show-details-dialog.html',
  styleUrls: ['./healthcheck-show-details-dialog.css']
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
  headerArrayDisplay = [];
  masterHeader = new Map<String, String>();
  finalHeaderToShow = new Map<String, String>();
  @ViewChild(MatPaginator) paginator: MatPaginator;
  headerSet = new Set();

  constructor(public dialogRef: MatDialogRef<ShowDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private restCallHandlerService: RestCallHandlerService,
    private healthCheckService: HealthCheckService) {
    this.fillMasterHeaderData();
  }

  ngOnInit() {
    this.loadDetailsDialogInfo();
    this.detailType = this.data.detailType;
  }

  ngAfterViewInit() {
    this.agentDetailedDatasource.paginator = this.paginator;
  }

  fillMasterHeaderData() {
    this.masterHeader.set("execId", "Execution ID");
    this.masterHeader.set("inSightsTimeX", "Execution Time ("+this.data.timeZone+")");
    this.masterHeader.set("status", "Status");
    this.masterHeader.set("message", "Message");
  }

  loadDetailsDialogInfo(): void {
    this.showThrobber = true;
    this.showContent = !this.showThrobber;
    this.checkResponseData = true;
    this.healthCheckService.loadHealthConfigurations(this.data.toolName, this.data.categoryName, this.data.agentId)
      .then((data) => {
        this.showThrobber = false;
        this.showContent = !this.showThrobber;
        var dataArray = data.data.nodes;
        if (dataArray != undefined) {
          this.pathName = this.data.pathName;
          if (dataArray.length === 0 && this.data.detailType != "Platform Service") {
            this.checkResponseData = false;
          }
          for (var key in dataArray) {
            var dataNodes = dataArray[key];
            for (var node in dataNodes) {
              if (node == "propertyMap") {
                var obj = dataNodes[node];
                if (typeof obj["message"] !== "undefined") {
                  obj["message"] = obj["message"].slice(0, 100);
                }
                this.agentDetailedNode.push(obj);
                for (var attr in obj) {
                  // fill data array in set , Only those header which mention in masterHeader
                  if (this.masterHeader.has(attr)) {
                    this.headerSet.add(attr);
                  }
                }
              }
            }
          }
          this.agentDetailedDatasource.data = this.agentDetailedNode;
          this.agentDetailedDatasource.paginator = this.paginator;
          this.showSelectedField();
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


}
