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

import {
  Component,
  OnInit,
  ElementRef,
  ViewChild,
  Inject,
} from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { DataSharedService } from "@insights/common/data-shared-service";
import { ReportTemplateService } from "../report-template-service";

@Component({
  selector: "app-kpi-report-List-Dialog",
  templateUrl: "./kpi-report-List-Dialog.component.html",
  styleUrls: [
    "./kpi-report-List-Dialog.component.scss",
    "./../../home.module.scss",
  ],
})
export class KpiReportListDialog implements OnInit {
  kpiList;
  displayedColumns: string[];
  kpiDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  selectedKpi: any;
  templateName: string;
  MAX_ROWS_PER_TABLE = 5;
  selectedIndex: number = -1;
  currentPageIndex: number = -1;
  totalPages: number = -1;
  currentPageValue: number;
  timezone: string;
  timezoneAbbr: string = "";

  constructor(
    public templateService: ReportTemplateService,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dataShare: DataSharedService,
    public dialogRef: MatDialogRef<KpiReportListDialog>
  ) {
    this.displayedColumns = ["kpiId", "vType", "vQuery"];
    this.templateName = this.data.templateName;
    this.kpiDatasource.data = JSON.parse(data.kpiDetails);
    this.kpiDatasource.paginator = this.paginator;
    console.log(this.kpiDatasource.data);
    this.totalPages = Math.ceil(
      this.kpiDatasource.data.length / this.MAX_ROWS_PER_TABLE
    );
  }

  ngOnInit() {
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;
    this.timezone = this.dataShare.getTimeZone();
    this.timezoneAbbr = this.dataShare.getTimeZoneAbbr();
  }

  ngAfterViewInit() {
    this.kpiDatasource.paginator = this.paginator;
  }

  goToNextPage() {
    this.paginator.nextPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  goToPrevPage() {
    this.paginator.previousPage();
    this.selectedIndex = -1;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }
}
