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
import { v4 as uuid } from "uuid";
import { DataSharedService } from "@insights/common/data-shared-service";

export interface IServerConfigurationService {
  loadServerConfigurations(): Promise<any>;
}

@Injectable()
export class ServerConfigurationService implements IServerConfigurationService {
  constructor(
    private restCallHandlerService: RestCallHandlerService,
    private dataShare: DataSharedService
  ) {}

  loadServerConfigurations(): Promise<any> {
    return this.restCallHandlerService.get("SERVER_CONFIG_TEMPLATE");
  }

  saveServerConfigurations(serverConfigJson: string): Promise<any> {
    var auth_uuid = uuid();
    auth_uuid = auth_uuid.substring(0, 15);
    var dataValue = this.dataShare.encryptAES(auth_uuid, serverConfigJson);
    return this.restCallHandlerService
      .postWithData("SAVE_SERVER_CONFIG", dataValue, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  getServerConfigStatus(): Promise<any> {
    return this.restCallHandlerService.get("GET_SERVER_CONFIG_STATUS");
  }
}
