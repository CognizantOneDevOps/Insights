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
import { Component, OnInit, Inject, ViewChild } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { ReportManagementService } from "@insights/app/modules/reportmanagement/reportmanagement.service";

@Component({
  selector: "view-kpi-dialog",
  templateUrl: "./view-kpi-dialog.html",
  styleUrls: ["../reportmanagement.component.scss", "./../../home.module.scss"],
})
export class ViewKPIDialog implements OnInit {
  listOfKpis = new MatTableDataSource<any>();
  responseOfKPIlist: any;
  id: any;
  displayedColumns: string[];
  showPagination: boolean = true;
  currentPageIndex: number = -1;
  MAX_ROWS_PER_TABLE = 5;
  totalPages: number = -1;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(
    public reportmanagementService: ReportManagementService,
    public dialogRef: MatDialogRef<ViewKPIDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    console.log("data id", data.id);
    this.id = data.id;
    this.getListOfKPIS();
    this.displayedColumns = ["kpiId", "kpiName"];
  }

  ngOnInit() {
    this.listOfKpis.paginator = this.paginator;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  ngAfterViewInit() {
    this.listOfKpis.paginator = this.paginator;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  async getListOfKPIS() {
    this.responseOfKPIlist = await this.reportmanagementService.getKPISList(
      this.id
    );
    this.listOfKpis.data = this.responseOfKPIlist.data;
    this.listOfKpis.paginator = this.paginator;
    this.totalPages = Math.ceil(
      this.responseOfKPIlist.data.length / this.MAX_ROWS_PER_TABLE
    );
  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }
  goToNextPage() {
    this.paginator.nextPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
  goToPrevPage() {
    this.paginator.previousPage();
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }
}
