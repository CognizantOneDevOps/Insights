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
import { Observable } from 'rxjs';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';


export interface IHealthCheckService {
    loadServerHealthConfiguration(): Promise<any>;
    loadHealthConfigurations(toolName: string, toolCategory: string, agentId: String): Promise<any>;
    downloadLog(fileName: string): Observable<any>;
    getAgentFailureDetails(toolName: string, toolCategory: string, agentId: String): Promise<any>;
}




@Injectable()
export class HealthCheckService implements IHealthCheckService {

    constructor(private restCallHandlerService: RestCallHandlerService, private httpClient: HttpClient,
        private dataShare: DataSharedService) {
    }

    loadServerHealthConfiguration(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("INSIGHTS_COMP_STATUS");
    }
    loadServerAgentConfiguration(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("AGENT_COMP_STATUS");
    }
    loadHealthConfigurations(toolName: string, toolCategory: string, agentId: String): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("HEALTH_TOOL", { 'tool': toolName, 'category': toolCategory, 'agentId': agentId });
    }
    downloadLog(fileName): Observable<Blob> {
        let authToken = this.dataShare.getAuthorizationToken();
        let headers_object = new HttpHeaders();
        headers_object = headers_object.append("Authorization", authToken);
        let params = new HttpParams();
        params = params.append("logFileName", fileName + ".log");
        return this.httpClient.get("/PlatformService/traceability/getReportLog", { headers: headers_object, responseType: 'blob', params });
    }

       getAgentFailureDetails(toolName: string, toolCategory: string, agentId: String): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("AGENTS_FAILURE_DETAILS", { 'tool': toolName, 'category': toolCategory, 'agentId': agentId });
    }

}

