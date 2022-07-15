/********************************************************************************
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
 * the License. */

import { Component, Inject, OnInit, ViewChild } from "@angular/core";
import { MatDialogRef, MAT_DIALOG_DATA } from "@angular/material/dialog";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { MLWizardService } from "@insights/app/modules/model-management/mlwizard/mlwizard.service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { DataSharedService } from "@insights/common/data-shared-service";
import * as XLSX from "xlsx";

@Component({
    selector: "prediction-show-details-dialog",
    templateUrl: "./prediction-show-details-dialog.html",
    styleUrls: ["./prediction-show-details-dialog.scss","../../../../home.module.scss"]
})
export class PredictionShowDetailsDialog implements OnInit {
  predictionDetailedDatasource = new MatTableDataSource([]);
  @ViewChild(MatPaginator) paginator: MatPaginator;
  showPredictionDetails: boolean = true;
  usecase: string;
  model_id: string;
  headerArrayDisplay = [];
  predictionlist: any;
  masterHeader = new Map<String, String>();
  finalHeaderToShow = new Map<String, String>();
  headerSet = new Set();
  columnLength: number;
  showThrobber: boolean = false;
  timezone: String;
  targetColumn: string = null;
  sheetName: string = null;
  selectedIndex: number;
  totalPages: number = -1;
  currentPageIndex: number = -1;
  MAX_ROWS_PER_TABLE = 6;
  constructor(
    public dialogRef: MatDialogRef<PredictionShowDetailsDialog>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private mlwizardService: MLWizardService,
    public messageDialog: MessageDialogService,
    public dataShare: DataSharedService
  ) {}

  ngOnInit() {
    this.usecase = this.data.usecase;
    this.model_id = this.data.model_id;
    this.targetColumn = this.data.targetColumn;
    this.timezone = this.dataShare.getTimeZone();
    this.getPrediction();
  }

  ngAfterViewInit() {
    this.predictionDetailedDatasource.paginator = this.paginator;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  async getPrediction() {
    this.showThrobber = true;
    this.predictionlist = await this.mlwizardService.getPredictions(
      this.usecase,
      this.model_id
    );
    this.selectedIndex = -1;
    if (
      this.predictionlist != null &&
      this.predictionlist.status == "success"
    ) {
      this.showThrobber = false;
      console.log(this.predictionlist);
      this.headerArrayDisplay = this.predictionlist.data.Fields;
      this.predictionDetailedDatasource.data = this.predictionlist.data.Data;
      this.predictionDetailedDatasource.paginator = this.paginator;
      this.totalPages = Math.ceil(
        this.predictionDetailedDatasource.data.length / this.MAX_ROWS_PER_TABLE
      );

      var tagetIndex = this.headerArrayDisplay.indexOf(this.targetColumn);
      if (tagetIndex > 0) {
        this.headerArrayDisplay.splice(tagetIndex, 1);
        this.headerArrayDisplay.unshift(this.targetColumn);
      }
      var predictIndex = this.headerArrayDisplay.indexOf("predict");
      if (predictIndex > 0) {
        this.headerArrayDisplay.splice(predictIndex, 1);
        this.headerArrayDisplay.unshift("predict");
      }
      for (var x of this.predictionlist.data.Data) {
        var obj = x;
        for (let key in x) {
          this.finalHeaderToShow.set(key, obj[key]);
        }
      }
      console.log(this.finalHeaderToShow);
    } else {
      this.closeShowDetailsDialog();
      this.messageDialog.openSnackBar(
        "Something wrong with Service.Please try again.",
        "error"
      );
    }
  }

  closeShowDetailsDialog(): void {
    this.dialogRef.close();
  }

  exportToExcel() {
    this.sheetName =
      this.model_id.length > 31
        ? this.model_id.slice(0, 28) + ".."
        : this.model_id;
    const workSheet = XLSX.utils.json_to_sheet(
      this.predictionDetailedDatasource.data
    );
    const workBook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workBook, workSheet, this.sheetName);
    XLSX.writeFile(workBook, this.usecase + "-" + this.model_id + ".xlsx");
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
}
