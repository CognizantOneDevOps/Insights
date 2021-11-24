/*******************************************************************************
 * Copyright 2021 Cognizant Technology Solutions
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
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { DataSharedService } from '@insights/common/data-shared-service';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';

@Injectable({
  providedIn: 'root'
})
export class WorkflowTaskManagementService {

  constructor(private restCallHandlerService: RestCallHandlerService, private httpClient: HttpClient) {
  }
  getWorkFlowTask(): Promise<any> {
    return this.restCallHandlerService.get("GET_TASK_DETAIL");
  }
  deleteWorkflow(taskId: number): Promise<any> {
    return this.restCallHandlerService.postWithParameter("DELETE_TASK_DETAIL", { 'taskId': taskId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
  }
  getWorkFlowType(): Promise<any> {
    return this.restCallHandlerService.get("GET_ALL_WORKFLOW_TYPE");
  }
  saveDataforWorkflow(workflowMappingJson: string): Promise<any> {
    return this.restCallHandlerService.postWithData("SAVE_TASK_DETAIL", workflowMappingJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
  }
  updateDataforWorkflow(workflowMappingJson: string): Promise<any> {
    return this.restCallHandlerService.postWithData("UPDATE_TASK_DETAIL", workflowMappingJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
  }
}