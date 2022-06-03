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
import { Injectable } from "@angular/core";
import { RestCallHandlerService } from "@insights/common/rest-call-handler.service";

@Injectable()
export class ReportTemplateService {
  constructor(private restCallHandlerService: RestCallHandlerService) {}

  loadReportTemplateList(): Promise<any> {
    return this.restCallHandlerService.get("LIST_REPORT_TEMPLATE");
  }

  loadKpiDetails(reportId: string) {
    return this.restCallHandlerService
      .postWithParameter(
        "LIST_TEMPLATE_KPI",
        { reportId: reportId },
        { "Content-Type": "application/x-www-form-urlencoded" }
      )
      .toPromise();
  }

  loadVisualizationUtil(): Promise<any> {
    return this.restCallHandlerService.get("GET_VISUALIZATION_UTIL");
  }

  loadTemplateType(): Promise<any> {
    return this.restCallHandlerService.get("GET_TEMPLATE_TYPE");
  }

  loadChartTypeList(): Promise<any> {
    return this.restCallHandlerService.get("GET_VTYPE_LIST");
  }

  saveReportTemplate(templateJson: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("SAVE_REPORT_TEMPLATE", templateJson, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  deleteReportTemplate(deleteReportTemplateJson: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("DELETE_REPORT_TEMPLATE", deleteReportTemplateJson, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  updateReportTemplateStatus(reportTemplate: string) {
    return this.restCallHandlerService
      .postWithData("SET_REPORT_TEMPLATE_STATUS", reportTemplate, {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  updateReportTemplate(templateJson: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("EDIT_REPORT_TEMPLATE", templateJson, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }
}
