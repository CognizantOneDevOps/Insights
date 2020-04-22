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
import { Injectable } from "@angular/core";
import { RestCallHandlerService } from "@insights/common/rest-call-handler.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

export interface IBulkUploadService {
  getDocRootAgentVersionTools(): Promise<any>;
  loadUiServiceLocation(): Promise<any>;
  uploadFile(
    formData: any,
    toolName: string,
    labelName: string,
    insightTimex: any,
    insightTime: any
  ): Promise<any>;
}

@Injectable()
export class BulkUploadService implements IBulkUploadService {
  constructor(private restCallHandlerService: RestCallHandlerService) {}
  getDocRootAgentVersionTools(): Promise<any> {
    return this.restCallHandlerService.get("DOCROOT_AGENT_VERSION_TOOLS");
  }

  uploadFile(
    formData: any,
    toolName: string,
    labelName: string,
    InsightsTimeField: any,
    InsightsTimeFormat: any
  ): Promise<any> {
    return this.restCallHandlerService
      .postFormDataWithParameter("UPLOAD_FILE", formData, {
        toolName: toolName,
        label: labelName,
        insightsTimeField: InsightsTimeField,
        insightsTimeFormat: InsightsTimeFormat,
      })
      .toPromise();
  }

  loadUiServiceLocation(): Promise<any> {
    return this.restCallHandlerService.get("TOOLNAME_LABELNAME_JSON");
  }
}
