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
import { Injectable } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { HttpClient} from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';

@Injectable()
export class ReportManagementService {

    constructor(private restCallHandlerService: RestCallHandlerService, private httpClient: HttpClient,
        private dataShare: DataSharedService) {
    }
    saveDataforReport(reportJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("SAVE_REPORT", reportJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    getSchedule() {
        return this.restCallHandlerService.get("GET_SCHEDULE");
    }


    getReportTemplate() {
        return this.restCallHandlerService.get("GET_REPORT_TEMPLATE");
    }


    updateDataforReport(reportJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("UPDATE_REPORT", reportJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    deleteAssesmentReport(configId: string) {
        return this.restCallHandlerService.postWithParameter("DELETE_ASSESSMENT_REPORT", { 'configId': configId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    setActiveStatus(reportJson: string) {
        return this.restCallHandlerService.postWithData("STATE_CHANGE", reportJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    getAssesmentReport() {
        return this.restCallHandlerService.get("GET_ASSESSMENT_REPORT");
    }

    getKPISList(reportId: string) {
        return this.restCallHandlerService.postWithParameter("GET_KPI_LIST", { 'reportId': reportId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    getTasksList(workflowtype: string) {
        return this.restCallHandlerService.postWithParameter("GET_TASK_LIST", { 'workflowType': workflowtype }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    getWorkflowExecutionRecords(configIdJson: string) {
        return this.restCallHandlerService.postWithData("GET_WORKFLOW_EXECUTION_RECORDS", configIdJson, "", { 'Content-Type': 'application/json' }).toPromise();
    }

    setRetryStatus(configStatusMapping: string) {
        return this.restCallHandlerService.postWithData("SET_REPORT_STATUS", configStatusMapping, { 'Content-Type': 'application/json' }).toPromise();
    }

    downloadPDF(PDFRequestJson: string) {
        return this.restCallHandlerService.postWithPDFData("DOWNLOAD_REPORT_PDF", PDFRequestJson,"",{ 'Content-Type': 'application/json' },{'responseType':'blob'}).toPromise();
    }

    getPDFExecutionId(configIdJson:string){
        return this.restCallHandlerService.postWithData("GET_PDF_EXECUTIONID", configIdJson, "", { 'Content-Type': 'application/json' }).toPromise();
    }

}