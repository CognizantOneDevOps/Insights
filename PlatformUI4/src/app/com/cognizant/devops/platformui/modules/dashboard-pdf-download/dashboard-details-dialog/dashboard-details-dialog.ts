/*********************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
import { DataSharedService } from "@insights/common/data-shared-service";

export interface TimelagElement {
  toolname: string;
  average: string;
}
@Component({
  selector: "dashboard-details-dialog",
  templateUrl: "./dashboard-details-dialog.html",
  styleUrls: ["./dashboard-details-dialog.scss"],
})
export class DashboardDetailsDialog implements OnInit {
  showContent: boolean;
  MAX_ROWS_PER_TABLE = 10;
  showThrobber: boolean = false;
  checkResponseData: boolean;
  pathName: string;
  detailType: string;
  columnLength: number;
  resultsLength: number = 6;
  agentDetailedNode = [];
  agentDetailedDatasource = new MatTableDataSource([]);
  @ViewChild(MatPaginator) paginator: MatPaginator;
  agentFailureDetailsDatasource = new MatTableDataSource([]);
  agentFailureRecords = [];
  headerArrayDisplay = [];
  key1 = [];
  key = [];
  dispplaytoolname: string;
  value = [];
  masterHeader = new Map<String, String>();
  finalHeaderToShow = new Map<String, String>();
  displayedColumns: string[] = ["inSightsTimeX", "message"];
  headerSet = new Set();
  showAgentFailureTab: boolean = false;
  timeZone: string = "";
  showToolDetailProp: boolean = true;
  showCardDetail: boolean = false;
  timelagArray = [];
  columnsToDisplay: string[] = ["Tools", "Handover Time"];
  timelagDataSource = new MatTableDataSource([]);
  formatedData: any;
  expandedElement: any;
  cardData: any;
  expandObjects: any[];

  constructor(
    public dialogRef: MatDialogRef<DashboardDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dataShare: DataSharedService
  ) {}
  ngOnInit() {
    this.getCardDetails();
  }
  getCardDetails() {
    this.formatedData = JSON.parse(this.data.cardData.dashboardJson);
  }
  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }
}
