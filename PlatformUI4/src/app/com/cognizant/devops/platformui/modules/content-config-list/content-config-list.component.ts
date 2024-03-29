/*******************************************************************************
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
 ******************************************************************************/

import { Component, OnInit, ViewChild } from "@angular/core";
import { Router, NavigationExtras } from "@angular/router";
import { MatDialog } from "@angular/material/dialog";
import { FileUploadDialog } from "../fileUploadDialog/fileUploadDialog.component";
import { MatPaginator } from "@angular/material/paginator";
import { MatTableDataSource } from "@angular/material/table";
import { KpiService } from "../kpi-addition/kpi-service";
import { MessageDialogService } from "../application-dialog/message-dialog-service";
import { ContentService } from "./content-service";
import { InsightsUtilService } from "@insights/common/insights-util.service";
import { Sort } from "@angular/material/sort";

@Component({
  selector: "app-content-config",
  templateUrl: "./content-config-list.component.html",
  styleUrls: ["./content-config-list.component.scss", "./../home.module.scss"],
})
export class ContentConfigComponent implements OnInit {
  displayedColumns = [];
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;
  contentDatasource = new MatTableDataSource<any>();
  enableEdit: boolean = false;
  onRadioBtnSelect: boolean = false;
  contentList: any;
  data: any[];
  kpiId: number;
  kpiName: string;
  toolname: string;
  groupName: string;
  category: string;
  refreshRadio: boolean = false;
  selCont: any;
  showConfirmMessage: string;
  type: string;
  enableRefresh: boolean = false;
  action: any;
  MAX_ROWS_PER_TABLE = 5;
  selectedIndex: number = -1;

  constructor(
    public messageDialog: MessageDialogService,
    public router: Router,
    public dialog: MatDialog,
    public kpiService: KpiService,
    public contentService: ContentService,
    public insightsUtil : InsightsUtilService
  ) {}

  ngOnInit() {
    this.getAllActiveContent();
    this.displayedColumns = [
      "radio",
      "ContentId",
      "ContentName",
      "KpiId",
      "ExpectedTrend",
      "ResultField",
    ];
  }
  ngAfterViewInit() {
    this.contentDatasource.paginator = this.paginator;
  }

  goToNextPage() {
    this.paginator.nextPage();
    this.selectedIndex = -1;
  }

  goToPrevPage() {
    this.paginator.previousPage();
    this.selectedIndex = -1;
  }

  addnewContent() {
    this.contentService.setType("ADD");
    this.router.navigate(["InSights/Home/contentConfigAdd"], {
      skipLocationChange: true,
    });
  }
  uploadFile() {
    const dialogRef = this.dialog.open(FileUploadDialog, {
      panelClass: "custom-dialog-container",
      width: "40%",
      height: "40%",
      disableClose: true,
      data: {
        type: "CONTENT",
        multipleFileAllowed: false,
        header: "Upload Json File",
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.getAllActiveContent();
      }
    });
  }
  edit() {
    this.contentService.setType("EDIT");
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        contentId: this.selCont.contentId,
        contentName: this.selCont.contentName,
        kpiId: this.selCont.kpiId,
        expectedTrend: this.selCont.expectedTrend,
        directionThreshold: this.selCont.directionOfThreshold,
        resultField: this.selCont.resultField,
        action: this.selCont.action,
        isActive: this.selCont.isActive,
        message: JSON.stringify(this.selCont.message),
        threshold: this.selCont.threshold,
        thresholds: JSON.stringify(this.selCont.thresholds),
        category: this.selCont.category,
      },
    };
    this.router.navigate(["InSights/Home/contentConfigAdd"], navigationExtras);
  }
  applyFilter(filterValue: string) {
    this.contentDatasource.filter = filterValue.trim();
    this.selectedIndex = -1;
  }
  refresh() {
    this.getAllActiveContent();
    this.refreshRadio = false;
    this.onRadioBtnSelect = false;
  }
  enableButtons(element) {
    this.onRadioBtnSelect = true;
  }
  public async getAllActiveContent() {
    var self = this;
    self.refreshRadio = false;
    this.contentList = [];
    this.contentList = await this.contentService.loadContentList();
    if (this.contentList != null && this.contentList.status == "success") {
      this.contentDatasource.data = this.contentList.data.sort(
        (a, b) => a.kpiId > b.kpiId
      );
      this.contentDatasource.paginator = this.paginator;
    }
  }
  list() {
    this.getAllActiveContent();
  }

  uninstallContent() {
    var self = this;
    let data = self.selCont;
    var title = "Delete Content";
    var dialogmessage =
      "Do you want to delete content <b>" +
      self.selCont.contentId +
      "</b>? <br><br> <b> Please note: </b> The action of deleting content id " +
      "<b>" +
      self.selCont.contentId +
      "</b> CANNOT be UNDONE. Do you want to continue? ";
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title,
      dialogmessage,
      this.selCont.toolName,
      "DELETE",
      "37%"
    );
    dialogRef.afterClosed().subscribe((result) => {
      if (result == "yes") {
        var contentAPIRequestJson = {};
        contentAPIRequestJson["contentId"] = self.selCont.contentId;

        self.contentService
          .contentUninstall(JSON.stringify(contentAPIRequestJson))
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

  sortData(sort: Sort) {
    const data = this.contentList.data.slice();
    if (!sort.active || sort.direction === '') {
      this.contentDatasource.data = data;
      return;
    }

    this.contentDatasource.data = data.sort((a, b) => {
      const isAsc = sort.direction === 'asc';
      return this.insightsUtil.compare(a[sort.active], b[sort.active], isAsc)
    });
    this.contentDatasource.paginator = this.paginator;
  }
}
