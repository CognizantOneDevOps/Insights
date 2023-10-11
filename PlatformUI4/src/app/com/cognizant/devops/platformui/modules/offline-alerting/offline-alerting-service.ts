/*******************************************************************************
 * Copyright 2023 Cognizant Technology Solutions
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
export class OfflineAlertingService {
  type: any;

  constructor(private restCallHandlerService: RestCallHandlerService) {}

  public loadAlertingList(): Promise<any> {
    return this.restCallHandlerService.get("OFFLINE_ALERTS_LIST");
  }

  public saveAlertConfig(alertData: any): Promise<any> {
    return this.restCallHandlerService
      .postWithData("SAVE_ALERT_CONFIG", alertData, "", {
        "Cntent-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  public updateAlertConfig(alertData: any): Promise<any> {
    return this.restCallHandlerService
      .postWithData("UPDATE_ALERT_CONFIG", alertData, "", {
        "Cntent-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  deleteAlertData(alertName: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("DELETE_ALERT", alertName, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  getEmailConfigurationStatus(): Promise<any> {
    return this.restCallHandlerService.get("GET_EMAIL_CONFIGURATION_STATUS");
  }

  getDashboardList(orgId): Promise<any> {
    return this.restCallHandlerService
      .postWithParameter(
        "GET_DASHBOARD_LIST_BY_ORG",
        { orgId: orgId },
        { "Content-Type": "application/x-www-form-urlencoded" }
      )
      .toPromise();
  }

  public getDashboardByUid(uuid: any, orgId: any): Promise<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.get("GET_DASHBOARD_BY_UID", {
      uuid: uuid,
      orgId: orgId,
    });
  }

  public getTemplateByQuery(query: any): Promise<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler
      .postWithData("GET_TEMPLATE_BY_QUERY", query, "", {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  public updateAlertConfigStatus(config: any): Promise<any> {
    return this.restCallHandlerService
      .postWithData("UPDATE_OFFLINE_ALERT_STATUS", config, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  getAlertExecutionRecords(configIdJson: string) {
    return this.restCallHandlerService
      .postWithData("GET_ALERT_EXECUTION_RECORDS", configIdJson, "", {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  setType(type) {
    this.type = type;
  }
  getType() {
    return this.type;
  }
}
