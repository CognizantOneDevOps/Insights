/*******************************************************************************
 * Copyright 2024 Cognizant Technology Solutions
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
import { RestCallHandlerService } from './../../common.services/rest-call-handler.service';
import { Injectable } from '@angular/core';

@Injectable()
export class KafkaNeo4jService {

  constructor(private restCallHandlerService: RestCallHandlerService) { }

  getNeo4jScalingConfigs() {
    return this.restCallHandlerService.get("GET_SCALING_CONFIG")
  }

  saveNeo4jScalingConfigs(sourceConfig, replicaConfig): Promise<any> {
    return this.restCallHandlerService
      .postWithData("SAVE_NEO4J_SCALING_CONFIG", JSON.stringify({ "sourceStreamsConfig": sourceConfig, "replicaConfig": replicaConfig }), "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  getAllReplicas() {
    return this.restCallHandlerService.get("GET_ALL_REPLICAS")
  }

  deleteReplica(replicaName: string): Promise<any> {
    return this.restCallHandlerService
      .postWithParameter("DELETE_REPLICA", { replicaName :replicaName }, {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  resyncAllReplicas(): Promise<any> {
    return this.restCallHandlerService
      .post("RESYNC_REPLICAS",  {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }
  
  getLogDetails() {
    return this.restCallHandlerService.get("LOG_DETAILS")
  }

}
