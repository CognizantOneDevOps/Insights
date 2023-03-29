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
import { BehaviorSubject } from "rxjs";

@Injectable()
export class OfflineService {
  type: any;
  offlineData: any;
  public offlineUploadSubject = new BehaviorSubject<any>("");
  public setOfflineSubject = new BehaviorSubject<any>([]);

  constructor(private restCallHandlerService: RestCallHandlerService) {}

  saveDataforOffline(offlineAPIRequestJson: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("SAVE_OFFLINE_DATA", offlineAPIRequestJson, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }
  updateDataforOffline(offlineAPIRequestJson: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("UPDATE_OFFLINE_DATA", offlineAPIRequestJson, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  public updateOfflineConfigStatus(config: any): Promise<any> {
    return this.restCallHandlerService
      .postWithData("UPDATE_OFFLINE_CONFIG_STATUS", config, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  loadOfflineList(): Promise<any> {
    return this.restCallHandlerService.get("LIST_OFFLINE_DATA");
  }

  deleteOfflineData(queryName: string): Promise<any> {
    return this.restCallHandlerService
      .postWithData("DELETE_QUERY", queryName, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }
  uploadFile(formData): Promise<any> {
    return this.restCallHandlerService
      .postFormData("UPLOAD_BULK_KPI", formData)
      .toPromise();
  }

  updateOfflineQueryStatus(reportTemplate: string) {
    return this.restCallHandlerService
      .postWithData("SET_REPORT_TEMPLATE_STATUS", reportTemplate, {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  loadToolNameArr(): Promise<any> {
    return this.restCallHandlerService.get("TOOLNAME_LABELNAME_JSON");
  }

  setType(type) {
    this.type = type;
  }
  getType() {
    return this.type;
  }

  setSelectedOfflineData(offlineData) {
    this.offlineData = offlineData;
  }
  getSelectedOfflineData() {
    return this.offlineData;
  }
}
