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

import { Component, OnInit, ViewChild } from "@angular/core";
import { Router, NavigationExtras } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { FileUploadDialog } from "../fileUploadDialog/fileUploadDialog.component";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { KpiService } from "../kpi-addition/kpi-service";
import { MessageDialogService } from "../application-dialog/message-dialog-service";
import { ContentService } from "../content-config-list/content-service";

@Component({
  selector: "app-kpi-creation",
  templateUrl: "./kpi-creation.component.html",
  styleUrls: ["./kpi-creation.component.scss", "./../home.module.scss"],
})
export class KpiCreationComponent implements OnInit {
  displayedColumns = [];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  kpiDatasource = new MatTableDataSource<any>();
  enableEdit: boolean = false;
  onRadioBtnSelect: boolean = false;
  kpiList: any;
  data: any[];
  kpiId: number;
  kpiName: string;
  toolname: string;
  groupName: string;
  category: string;
  refreshRadio: boolean = false;
  selectedKpi: any;
  showConfirmMessage: string;
  type: string;
  enableRefresh: boolean = false;
  MAX_ROWS_PER_TABLE = 5;
  selectedIndex: number = -1;
  currentPageIndex: number = -1;
  totalPages: number = -1;
  currentPageValue: number;

  constructor(
    public messageDialog: MessageDialogService,
    public router: Router,
    public dialog: MatDialog,
    public kpiService: KpiService,
    public contentService: ContentService
  ) {}

  ngOnInit() {
    this.getAllActiveKpi();
    this.displayedColumns = [
      "radio",
      "KpiId",
      "KpiName",
      "ToolName",
      "GroupName",
      "Category",
      "ResultField",
    ];
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE;
    this.currentPageIndex = this.paginator.pageIndex + 1;

    this.kpiService.fileUploadSubject.subscribe((res) => {
      if (res === "REFRESH") {
        this.getAllActiveKpi();
      }
    });
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

  hideTextOverflow(text: any) {
    if (text !== undefined && text.length > 50) {
      return text.slice(0, 45) + "...";
    } else {
      return text;
    }
  }

  addnewKpi() {
    this.kpiService.setType("ADD");
    this.router.navigate(["InSights/Home/kpiaddition"], {
      skipLocationChange: true,
    });
  }
  uploadFile() {
    this.contentService.setFileType("KPI");
    this.dialog.open(FileUploadDialog, {
      panelClass: "custom-dialog-container",
      width: "40%",
      height: "40%",
      disableClose: true,
      data: {
        type: "KPI",
        multipleFileAllowed: false,
        header: "Upload Json File",
      },
    });
  }
  edit() {
    this.kpiService.setType("EDIT");
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        kpiId: this.selectedKpi.kpiId,
        kpiName: this.selectedKpi.kpiName,
        selectedTool: this.selectedKpi.toolname,
        category: this.selectedKpi.category,
        groupName: this.selectedKpi.groupName,
        dataSource: this.selectedKpi.datasource,
        dbQuery: this.selectedKpi.dBQuery,
        isActive: this.selectedKpi.isActive,
        resultField: this.selectedKpi.resultField,
        outputDatasource: this.selectedKpi.outputDatasource,
        usecase: this.selectedKpi.usecase,
      },
    };

    this.router.navigate(["InSights/Home/kpiaddition"], navigationExtras);
  }
  refresh() {
    this.getAllActiveKpi();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;
  }
  enableButtons() {
    this.onRadioBtnSelect = true;
    this.currentPageIndex = this.paginator.pageIndex + 1;
  }

  public async getAllActiveKpi() {
    var self = this;
    self.refreshRadio = false;
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

      this.totalPages = Math.ceil(
        this.kpiDatasource.data.length / this.MAX_ROWS_PER_TABLE
      );
      this.kpiDatasource.paginator = this.paginator;
      console.log(this.kpiDatasource.paginator);
    }
  }
  applyFilter(filterValue: string) {
    this.kpiDatasource.filter = filterValue.trim();
  }

  list() {
    this.getAllActiveKpi();
  }
  uninstallKpi() {
    var self = this;
    let data = self.selectedKpi;

    var title = "Delete KPI";
    var dialogmessage =
      "Do you want to delete a Kpi <b>" +
      self.selectedKpi.kpiId +
      "</b>? <br> <b> Please note: </b> The action of deleting a Kpi " +
      "<b>" +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selectedKpi.toolName,
      "DELETE",
      "35%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var kpiAPIRequestJson = {};
        kpiAPIRequestJson["kpiId"] = self.selectedKpi.kpiId;

        self.kpiService
          .kpiUninstall(JSON.stringify(kpiAPIRequestJson))
          .then(function (data) {
            if (data.status === "success") {
              self.messageDialog.openSnackBar(
                "<b>" + data.data.message + "</b>",
                "success"
              );
              self.list();
            }
            if (data.status === "failure") {
              self.messageDialog.openSnackBar(
                "<b>" + data.message + "</b>",
                "error"
              );
              self.list();
            }
          })
          .catch(function (data) {
            self.showConfirmMessage = "service_error";
          });
      }
    });
  }
}
