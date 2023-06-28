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

import { Injectable } from '@angular/core';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { HttpClient } from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';


export interface ITraceablityService {

}

@Injectable()
export class TraceabiltyService implements ITraceablityService {

    constructor(private restCallHandlerService: RestCallHandlerService, private httpClient: HttpClient,
        private dataShare: DataSharedService) {
    }

    getAssetHistory(toolName: string, toolField: string, toolValue: string,type: string): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GET_DETAILS", {'toolName': toolName, 'fieldName': toolField, 'fieldValue': toolValue,'type':type });
    }

    getEpicIssues(toolName: string, toolField: string, toolValue: string,type:string): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GET_EPIC_ISSUES", {'toolName': toolName, 'fieldName': toolField, 'fieldValue': toolValue,'type':type });
    }

    getIssuesPipeline(issue:string): Promise<any>{
        var restHandler = this.restCallHandlerService;
       // return restHandler.get("GET_ISSUES_PIPELINE", {'issue': issue});
        return this.restCallHandlerService.postWithData("GET_ISSUES_PIPELINE", issue, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    public getToolDisplayProperties()  {
       return this.restCallHandlerService.get("GET_TOOL_PROPERTIES", {});
    }

    
    getAsssetDetails(toolName: string, cachestring: string) {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GET_TOOL_DETAILS", {'toolName': toolName, 'cacheKey': cachestring });
    }
    getAvailableTools()
    {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GET_TOOL_LIST", { });
    }
    getToolKeyset(toolName: string)
    {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GET_TOOL_KEYSET", {'toolName': toolName });   
    }

    downloadTraceabiltyPDF(toolName: string, toolField: string, toolValue: string, type: string, file: any) {
        var restHandler = this.restCallHandlerService;
        var bodyObj = { 'toolName': toolName, 'fieldName': toolField, 'fieldValue': toolValue, 'type': type, 'file': file };
        return restHandler.postWithPDFData("GET_TRACEABILITY_PDF", JSON.stringify(bodyObj), {}, { 'Content-Type': 'application/json' }, { 'responseType': 'blob' }).toPromise();
    }
}