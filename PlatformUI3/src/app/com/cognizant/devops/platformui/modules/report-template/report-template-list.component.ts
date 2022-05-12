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
import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, NavigationExtras } from '@angular/router';
import { ReportTemplateService } from './report-template-service';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatRadioChange } from '@angular/material/radio';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { MatTableDataSource } from '@angular/material/table';
import { KpiReportListDialog } from './report-template-kpi-list/kpi-report-List-Dialog.component';
import { DataSharedService } from '@insights/common/data-shared-service';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { KpiService } from '../kpi-addition/kpi-service';
import { FileUploadDialog } from '../fileUploadDialog/fileUploadDialog.component';
import { ContentService } from '../content-config-list/content-service';



@Component({
  selector: 'app-report-template',
  templateUrl: './report-template-list.component.html',
  styleUrls: ['./report-template-list.component.css', './../home.module.css']
})
export class ReportTemplateComponent implements OnInit {
  displayedColumns = [];
  MAX_ROWS_PER_TABLE = 10;
  reportList: any;
  visualizationUtilResponse: any;
  visualizationUtilList = [];
  templateTypeResponse: any;
  templateTypeList = [];
  chartTypeResponse: any;
  chartTypeList = [];
  kpiResponse: any;
  showThrobber: boolean = false;
  selectedTemplate: any;
  enableEdit: boolean = false;
  enableDelete: boolean = false;
  visualizationUtil: string;
  configParams: string;
  kpiDetailsResponse: any;
  kpiDetailsList = [];
  templateDatasource = new MatTableDataSource<any>();
  formDataFiles = new FormData();
  enableAttachFile: boolean = false;
  selectedIndex : -1;
  currentPageValue : number;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;


  constructor(public reportTemplateService: ReportTemplateService, private dialog: MatDialog,
    public dataShare: DataSharedService, private messageDialog: MessageDialogService, public router: Router,
    public kpiService: KpiService, public contentService: ContentService) {
    this.showThrobber = true;
    this.displayedColumns = ['radio', 'reportTemplateName', 'reportTemplateDescription', 'visualizationutil', 'active', 'details'];
    this.getVisualizationUtilList();
    this.getTemplateTypeList();
    this.getChartTypeList();
    this.getAllReportTemplate();
  }

  ngOnInit() {
    console.log(this.paginator)
    this.currentPageValue = this.paginator.pageIndex * this.MAX_ROWS_PER_TABLE; 
  }

  ngAfterViewInit() {
    this.templateDatasource.paginator = this.paginator;
  }

  async getVisualizationUtilList() {
    this.visualizationUtilResponse = await this.reportTemplateService.loadVisualizationUtil();
    if (this.visualizationUtilResponse.data != null && this.visualizationUtilResponse.status == 'success') {
      this.visualizationUtilList = this.visualizationUtilResponse.data;
      console.log(this.visualizationUtilList);
    }

  }

  async getTemplateTypeList() {
    this.templateTypeResponse = await this.reportTemplateService.loadTemplateType();
    if (this.templateTypeResponse.data != null && this.templateTypeResponse.status == 'success') {
      this.templateTypeList = this.templateTypeResponse.data;
      console.log(this.templateTypeList);
    }

  }

  async getChartTypeList() {
    this.chartTypeResponse = await this.reportTemplateService.loadChartTypeList();
    if (this.chartTypeResponse.data != null && this.chartTypeResponse.status == 'success') {
      this.chartTypeList = this.chartTypeResponse.data;
      console.log(this.chartTypeList);
    }
  }

  public async getAllReportTemplate() {
    var self = this;
    this.reportList = [];
    this.reportList = await this.reportTemplateService.loadReportTemplateList();
    if (this.reportList != null && this.reportList.status == "success") {
      this.showThrobber = false;
      this.templateDatasource.data = this.reportList.data;
      this.templateDatasource.paginator = this.paginator;
    }
  }

  async getKpiDetails(reportId: string) {
    this.kpiDetailsResponse = await this.reportTemplateService.loadKpiDetails(reportId);
    if (this.kpiDetailsResponse.data != null && this.kpiDetailsResponse.status == 'success') {
      this.kpiDetailsList = this.kpiDetailsResponse.data;
      console.log(this.kpiDetailsList);
    } else {
      this.messageDialog.showApplicationsMessage(
        "Failed to load kpi details for report templates.Please check logs for more details.",
        "ERROR"
      );
    }
  }

  async showKpiDetailsDialog(reportId: string, templateName: string) {
    await this.getKpiDetails(reportId);
    console.log(reportId, this.kpiDetailsList);
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      this.dialog.open(KpiReportListDialog, {
        panelClass: 'showjson-dialog-container',
        disableClose: true,
        data: {
          templateName: templateName,
          kpiDetails: JSON.stringify(this.kpiDetailsList)
        }
      });
    }
  }

  add() {
    this.configParams = JSON.stringify({ type: 'save' });
    this.navigate();
  }

  async editTemplate() {
    await this.getKpiDetails(this.selectedTemplate.reportId);
    var templateData = this.templateDatasource.data.find(
      ({ templateName }) => templateName === this.selectedTemplate.templateName);
      console.log(templateData);
    templateData["kpiDetails"] = this.kpiDetailsList;
    this.configParams = JSON.stringify({ type: 'edit', data: templateData });
    this.navigate();
  }

  navigate() {
    if (this.visualizationUtilList.length == 0) {
      this.messageDialog.showApplicationsMessage(
        "Failed to load visualization util for report templates.Please check logs for more details.", "ERROR");
    }
    if (this.templateTypeList.length == 0) {
      this.messageDialog.showApplicationsMessage(
        "Failed to load template type for report templates.Please check logs for more details.", "ERROR");
    }
    if (this.chartTypeList.length == 0) {
      this.messageDialog.showApplicationsMessage(
        "Failed to load vtypes for report templates.Please check logs for more details.", "ERROR");
    }
    let navigationExtras: NavigationExtras = {
      skipLocationChange: true,
      queryParams: {
        templateParam: this.configParams,
        visualizaionUtil: JSON.stringify(this.visualizationUtilList),
        templateType: JSON.stringify(this.templateTypeList),
        vType: JSON.stringify(this.chartTypeList)
      }
    };
    this.router.navigate(['InSights/Home/template-configuration'], navigationExtras);
  }

  radioChange(event: MatRadioChange, index) {
    this.selectedIndex = index + this.currentPageValue ;
    this.enableDelete = true;
    this.enableEdit = true;
    this.enableAttachFile = true;

  }

  changeCurrentPageValue() {
    this.selectedIndex = -1;
    this.currentPageValue = this.paginator.pageIndex  * this.MAX_ROWS_PER_TABLE;
  }

  refresh() {
    this.selectedIndex = -1;
    this.selectedTemplate = '';
    this.getAllReportTemplate;
    this.enableDelete = false;
    this.enableEdit = false;
    this.enableAttachFile = false;
  }


  attachFiles() {
    const dialogRef = this.dialog.open(FileUploadDialog, {
      panelClass: 'DialogBox',
      width: '34%',
      height: '25%',
      disableClose: true,
      data: {
        type: 'ATTACH_FILES',
        multipleFileAllowed: true,
        reportId: this.selectedTemplate.reportId,
        header:'Attach Files'
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.refresh();
      }
    });
  }


  delete() {
    var self = this;
    var deleteReportTemplateJson = {};
    deleteReportTemplateJson["reportId"] = self.selectedTemplate.reportId;
    console.log(deleteReportTemplateJson);
    var title = 'Delete';
    var dialogmessage = 'Do you want to delete <b>' + self.selectedTemplate.templateName +
      '</b> ? <br> <b> Please note: </b> The action of deleting ' + '<b>' + self.selectedTemplate.templateName +
      '</b> cannot be undone. Do you want to continue ? ';
    const dialogRef = self.messageDialog.showConfirmationMessage(title, dialogmessage, self.selectedTemplate.templateName, 'ALERT', '40%');
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        this.reportTemplateService.deleteReportTemplate(JSON.stringify(deleteReportTemplateJson))
          .then(function (response) {
            if (response.status == 'success') {
              self.messageDialog.showApplicationsMessage('<b>' + self.selectedTemplate.templateName +
                '</b> deleted successfully.', 'SUCCESS');
              self.getAllReportTemplate();
              self.refresh();
            } else if (response.status == 'failure' && response.message != null) {
              self.messageDialog.showApplicationsMessage(response.message, "ERROR");
            } else {
              self.messageDialog.showApplicationsMessage("Failed to delete <b>" +
                self.selectedTemplate.templateName + "</b>. Please check logs for details.", "ERROR");
            }
          }).catch(function (response) { });
      }
    });
  }

  uploadTemplateJson() {
    const dialogRef = this.dialog.open(FileUploadDialog, {
      panelClass: 'DialogBox',
      width: '34%',
      height: '25%',
      disableClose: true,
      data: {
        type: 'REPORT_TEMPLATE',
        multipleFileAllowed: false,
        header:'Upload Json File'
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.getAllReportTemplate();
      }
    });

  }

  updateTemplateStatus(event: MatSlideToggleChange, id, element) {
    console.log(event, id);
    console.log(event.checked);
    var self = this;
    var statusUpdateJson = {};
    var title = 'Update Active/Inactive State';
    var state = element.isActive ? 'Active' : 'Inactive';
    var dialogmessage =
      'Are you sure you want to make <b>' + element.templateName + '</b> status <b>' + state + '</b> ?';
    const dialogRef = self.messageDialog.showConfirmationMessage(
      title, dialogmessage, element.templateName, 'ALERT', '40%');
    dialogRef.afterClosed().subscribe(result => {
      if (result == 'yes') {
        statusUpdateJson["reportId"] = id;
        statusUpdateJson["isActive"] = event.checked;
        console.log(statusUpdateJson);
        self.reportTemplateService.updateReportTemplateStatus(JSON.stringify(statusUpdateJson))
          .then(function (response) {
            if (response.status == 'success') {
              self.messageDialog.showApplicationsMessage(
                'Status updated successfully.', 'SUCCESS');
            } else {
              self.messageDialog.showApplicationsMessage(
                'Failed to update state. Please check logs for more details.', 'ERROR');
            }
          })
      } else {
        element.isActive = !event.checked;
      }
    });
  }

}
