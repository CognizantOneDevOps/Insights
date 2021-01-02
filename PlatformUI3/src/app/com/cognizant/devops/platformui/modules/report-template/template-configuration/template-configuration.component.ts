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

import { Component, OnInit, ViewChild } from "@angular/core";
import { Router, NavigationExtras, ActivatedRoute } from "@angular/router";
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { KpiDetailItem } from "./kpi-detail-items";
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { ReportTemplateService } from '../report-template-service';
import { KpiListDialog } from '@insights/app/modules/kpiList-Dialog/kpiList-Dialog.component';
import { KpiService } from "../../kpi-addition/kpi-service";


@Component({
    selector: 'app-template-configuration',
    templateUrl: './template-configuration.component.html',
    styleUrls: ['../report-template-list.component.css', './../../home.module.css']
})
export class ReportTemplateConfig implements OnInit {
    buttonName: string;
    showKpiList: boolean = false;
    visualizationUtilList = [];
    vTypeList = [];
    displayedColumns = [];
    kpiId: string;
    vType: string;
    vQuery: string;
    reportId: string;
    templateName: string;
    description: string;
    visualizationUtil: string;
    kpiDetailItems: KpiDetailItem[] = [];
    isEdit: boolean = false;
    disableId: boolean = false;
    regex = new RegExp('^[a-zA-Z0-9_]*$');
    receivedParam: any;
    disableInputFields: boolean = false;
    deletedKpiList = [];
    status: boolean = true;
    kpiListDatasource = new MatTableDataSource<any>();
    @ViewChild(MatPaginator) paginator: MatPaginator;

    constructor(public router: Router, private route: ActivatedRoute, public messageDialog: MessageDialogService,
        public reportTemplateService: ReportTemplateService, private dialog: MatDialog, public kpiService: KpiService) {
        this.buttonName = 'ADD';
        this.displayedColumns = ['kpiId', 'vType', 'edit', 'remove'];
    }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            console.log(params);
            this.receivedParam = JSON.parse(params.templateParam);
            this.visualizationUtilList = JSON.parse(params.visualizaionUtil);
            this.vTypeList = JSON.parse(params.vType);
        });
        this.initializeTemplateDetails();
    }

    ngAfterViewInit() {
        this.kpiListDatasource.paginator = this.paginator;
    }
    isEditKpi() {
        if (this.isEdit) {
            var index = this.kpiDetailItems.findIndex(({ kpiId }) => kpiId === this.kpiId);
            if (index >= 0) {
                this.kpiDetailItems.splice(index, 1);
            }
        }
        let kpiDetailConfig = new KpiDetailItem();
        this.vType === undefined ? this.vType = " ":this.vType;
        this.vQuery === undefined ? this.vQuery = " ":this.vQuery;
        kpiDetailConfig.setData(this.kpiId, this.vType + '_' + this.kpiId, this.vQuery);
        this.kpiDetailItems.push(kpiDetailConfig);
        this.kpiListDatasource.data = this.kpiDetailItems;
        this.kpiListDatasource.paginator = this.paginator;
        this.showKpiList = true;
        this.resetKpiDetails();
        console.log('Add', this.kpiDetailItems);

    }
    addKpi() {
        if (this.visualizationUtil !== 'LEDGERPDF') {
            if (this.kpiId == undefined || this.vType == undefined || this.vQuery == undefined ||
                this.vType == '' || this.kpiId == '' || this.vQuery == '') {
                this.messageDialog.showApplicationsMessage('Please fill mandatory fields.', 'ERROR');

            }
            else if (this.kpiDetailItems.find(({ kpiId }) => kpiId === this.kpiId && !this.isEdit)) {
                this.messageDialog.showApplicationsMessage('Kpi Id already exists. Please select unique Kpi Id.', 'ERROR');
            }
            else {
                this.isEditKpi();
            }
        } else {
            if (this.kpiId == undefined || this.kpiId == '') {
                this.messageDialog.showApplicationsMessage('Please fill mandatory fields.', 'ERROR');

            }
            else if (this.kpiDetailItems.find(({ kpiId }) => kpiId === this.kpiId && !this.isEdit)) {
                this.messageDialog.showApplicationsMessage('Kpi Id already exists. Please select unique Kpi Id.', 'ERROR');
            }
            else {
                this.isEditKpi();
            }
        }
    }

    resetKpiDetails() {
        this.buttonName = 'ADD';
        this.kpiId = '';
        this.vType = '';
        this.vQuery = '';
        this.isEdit = false;
        this.disableId = false;
    }

    editKpiDetails(id: String) {
        this.disableId = true;
        this.buttonName = 'UPDATE';
        var listData = this.kpiDetailItems.find(({ kpiId }) => kpiId === id);
        this.kpiId = listData.kpiId;
        this.vType = listData.vType.split('_')[0].toString();
        this.vQuery = listData.vQuery;
        this.isEdit = true;
    }

    deleteKpi(id: string) {
        var index = this.kpiDetailItems.findIndex(({ kpiId }) => kpiId === id);
        const dialogRef = this.messageDialog.showConfirmationMessage("Delete", "Are you sure you want to remove KPI <b>" + id + "</b> ?", id, "ALERT", "40%");
        dialogRef.afterClosed().subscribe(result => {
            if (result == 'yes') {
                this.kpiDetailItems.splice(index, 1);
                this.kpiListDatasource.data = this.kpiDetailItems;
                this.kpiListDatasource.paginator = this.paginator;
                console.log('delete', id, this.kpiDetailItems);
            }
        });
    }

    initializeTemplateDetails() {
        if (this.receivedParam.type == "edit") {
            this.disableInputFields = true;
            this.reportId = this.receivedParam.data.reportId;
            this.templateName = this.receivedParam.data.templateName;
            this.description = this.receivedParam.data.description;
            this.status = this.receivedParam.data.isActive;
            this.visualizationUtil = this.receivedParam.data.visualizationutil;
            console.log(this.visualizationUtil);
            var receivedKpiDetails = this.receivedParam.data.kpiDetails;
            receivedKpiDetails.forEach(element => {
                let kpiDetails = new KpiDetailItem();
                kpiDetails.setData(element.kpiId.toString(), element.vType, element.vQuery);
                this.kpiDetailItems.push(kpiDetails);
            });
            this.kpiListDatasource.data = this.kpiDetailItems;
            this.kpiListDatasource.paginator = this.paginator;
            this.showKpiList = true;
            console.log(this.kpiDetailItems);
        }
    }

    validateTemplateData() {
        var checkname = this.regex.test(this.templateName);
        if (this.templateName == undefined || this.description == undefined || this.visualizationUtil == undefined ||
            this.templateName == '' || this.description == '' || this.visualizationUtil == '') {
            this.messageDialog.showApplicationsMessage('Please fill mandatory fields.', 'ERROR');
        } else if (!checkname) {
            this.messageDialog.showApplicationsMessage(
                'Please enter valid report template name, it contains only alphanumeric character and underscore.', 'ERROR');
        } else if (this.kpiDetailItems.length == 0) {
            this.messageDialog.showApplicationsMessage('Please add kpi details for report template.', 'ERROR');
        } else {
            this.saveTemplate();
        }
    }

    saveTemplate() {
        var templateAPIRequestJson = {};
        var kpiConfigs = [];
        var self = this;
        templateAPIRequestJson['reportName'] = this.templateName;
        templateAPIRequestJson['description'] = this.description;
        templateAPIRequestJson['isActive'] = this.status;
        templateAPIRequestJson['visualizationutil'] = this.visualizationUtil;
        this.kpiDetailItems.forEach(element => {
            var vConfigs = { kpiId: element.kpiId, visualizationConfigs: [{ vType: element.vType, vQuery: element.vQuery }] };
            kpiConfigs.push(vConfigs);
        });
        templateAPIRequestJson['kpiConfigs'] = kpiConfigs;
        console.log(templateAPIRequestJson);
        if (this.receivedParam.type == "save") {
            var dialogmessage = "You have created a new Report Template <b>" + this.templateName + "</b> .Do you want to continue ? ";
            var title = "Save Report " + this.templateName;
            const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage, "", "ALERT", "40%");
            dialogRef.afterClosed().subscribe(result => {
                if (result == "yes") {
                    this.reportTemplateService.saveReportTemplate(JSON.stringify(templateAPIRequestJson))
                        .then(function (response) {
                            if (response.status == "success") {
                                self.messageDialog.showApplicationsMessage("<b>" + self.templateName + "</b> saved successfully.", "SUCCESS");
                                self.refresh();
                            } else if (response.message === ' report template already exists in database ' + self.reportId) {
                                self.messageDialog.showApplicationsMessage(
                                    '<b>' + self.reportId + '</b> already exists. Please try again with a new report Id.', 'ERROR');
                            } else {
                                self.messageDialog.showApplicationsMessage("Failed to save the report.Please check logs.", "ERROR");
                            }
                        });
                }
            });
        }
        if (this.receivedParam.type == "edit") {
            templateAPIRequestJson['reportId'] = this.reportId;
            var dialogmessage =" You have updated <b>" + self.templateName +"</b>. Do you want to continue ? ";
            var title = "Update " + self.templateName;
            const dialogRef = this.messageDialog.showConfirmationMessage(title, dialogmessage,"","ALERT","40%");
            dialogRef.afterClosed().subscribe(result => {
                if (result == "yes") {
                    this.reportTemplateService.updateReportTemplate(JSON.stringify(templateAPIRequestJson))
                    .then(function (response) {
                        if (response.status == "success") {
                            self.messageDialog.showApplicationsMessage(
                              "<b>" + self.templateName + "</b> updated successfully.","SUCCESS");
                            self.refresh();
                        } else {
                            self.messageDialog.showApplicationsMessage(
                              "Failed to update <b>" + self.templateName + "</b> . Please check logs.","ERROR");
                        }
                    })
                }
            });
        }
    }

    reset() {
        if(this.receivedParam.type == "save") {
            this.templateName = '';
        }
        this.description = '';
        this.visualizationUtil = '';
        this.kpiId = '';
        this.vType = '';
        this.vQuery = '';
        this.kpiDetailItems = [];
        this.kpiListDatasource.data = this.kpiDetailItems;
        this.kpiListDatasource.paginator = this.paginator;
    }

    refresh() {
        let navigationExtras: NavigationExtras = {
            skipLocationChange: true,
            queryParams: {

            }
        };
        this.router.navigate(["InSights/Home/reportTemplate"], navigationExtras);
    }

    kpiSelectDialog() {
        var dialogRef = this.dialog.open(KpiListDialog, {
            panelClass: 'showjson-dialog-container',
            height: "500px",
            width: "550px",
            disableClose: true,

        });
        dialogRef.afterClosed().subscribe(result => {
            this.kpiService.setKpiSubject.subscribe(res => {
                this.kpiId = res.kpiId.toString();
            })
        });
    }

}
