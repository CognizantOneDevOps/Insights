/* Copyright 2022 Cognizant Technology Solutions
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

import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { KpiService } from '../kpi-addition/kpi-service';

@Component({
  selector: "app-kpiList-Dialog",
  templateUrl: "./kpiList-Dialog.component.html",
  styleUrls: ["./kpiList-Dialog.component.scss", "./../home.module.scss"],
})
export class KpiListDialog implements OnInit {
  kpiList;
  displayedColumns: string[];
  kpiDatasource = new MatTableDataSource<any>();
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  selectedKpi: any;
  MAX_ROWS_PER_TABLE = 5;
  selectedIndex: number = -1;
  currentPageIndex: number = -1;
  totalPages: number = -1;
  currentPageValue: number;

  constructor(
    public kpiService: KpiService,
    public dialogRef: MatDialogRef<KpiListDialog>
  ) {
    this.displayedColumns = ["radio", "kpiId", "kpiName", "category"];
  }
  ngOnInit() {
    this.getAllActiveKpi();
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;
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

  applyFilter(filterValue: string) {
    this.kpiDatasource.filter = filterValue.trim();
  }
  setKpiValue() {
    this.kpiService.setKpiSubject.next(this.selectedKpi);
  }

  onOkClick() {
    let data = this.selectedKpi;
    this.setKpiValue();
    this.dialogRef.close();
  }

  public async getAllActiveKpi() {
    var self = this;
    this.kpiList = [];
    this.kpiList = await this.kpiService.loadKpiList();
    this.kpiList.data.forEach((kpi) => {
      let kpiIdArr = [];
      kpiIdArr.push(kpi.kpiId);
    });

    if (this.kpiList != null && this.kpiList.status == "success") {
      this.kpiDatasource.data = this.kpiList.data.sort(
        (a, b) => a.kpiId > b.kpiId
      );

      this.kpiDatasource.paginator = this.paginator;
      this.totalPages = Math.ceil(
        this.kpiDatasource.data.length / this.MAX_ROWS_PER_TABLE
      );
    }
  }
  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }
}
