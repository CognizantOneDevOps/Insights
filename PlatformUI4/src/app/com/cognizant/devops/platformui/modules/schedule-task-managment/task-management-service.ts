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
import { Injectable } from "@angular/core";
import { RestCallHandlerService } from "@insights/common/rest-call-handler.service";
import { HttpClient } from "@angular/common/http";
import { DataSharedService } from "@insights/common/data-shared-service";

@Injectable()
export class TaskManagementService {
  constructor(
    private restCallHandlerService: RestCallHandlerService,
    private httpClient: HttpClient,
    private dataShare: DataSharedService
  ) {}
  getTaskList() {
    return this.restCallHandlerService.get("GET_SCHEDULE_TASK_LIST");
  }

  getTaskExecutionRecords(taskJson: string) {
    return this.restCallHandlerService
      .postWithData("GET_SCHEDULE_TASK_HISTORY_LIST", taskJson, "", {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  saveOrUpdateTaskDefinitionRecords(saveJson: string) {
    return this.restCallHandlerService
      .postWithData("SCHEDULE_TASK_SAVE_EDIT", saveJson, "", {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  updateStatuOfTaskDefinition(statusUpdateJson: string) {
    return this.restCallHandlerService
      .postWithData("SCHEDULE_TASK_STATUS_UPDATE", statusUpdateJson, "", {
        "Content-Type": "application/json",
      })
      .toPromise();
  }

  deleteTaskDefinitionUpdate(statusUpdateJson: string) {
    return this.restCallHandlerService
      .postWithData("DELETE_SCHEDULE_TASK_STATUS", statusUpdateJson, "", {
        "Content-Type": "application/json",
      })
      .toPromise();
  }
}
