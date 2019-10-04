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
import { Observable } from '../../../../../../../../../node_modules/rxjs';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';

export interface IQueryBuilderService {
    saveOrUpdateQuery(form: any, fileName: any, user: string): Promise<any>;
    fetchQueries(): Promise<any>;
    deleteQuery(reportnmae): Promise<any>;
    uploadFile(formData: FormData): Promise<any>;
    downloadFile(filepath): Observable<any>;
    testQuery(reportname, frequency): Promise<any>;
}

@Injectable()
export class QueryBuilderService implements IQueryBuilderService {

    constructor(private restCallHandlerService: RestCallHandlerService, private httpClient: HttpClient,
        private dataShare: DataSharedService) {
    }

    saveOrUpdateQuery(form: any, fileName: any, user: string): Promise<any> {
        console.log(form);
        let queryObj = { 'reportName': form.reportname, 'frequency': form.frequency, 'subscribers': form.subscribers, 'fileName': fileName, 'queryType': form.querytype, 'user': user };
        console.log(queryObj);
        return this.restCallHandlerService.postFormData("CREATE_UPDATE_CYPHER_QUERY", queryObj).toPromise();
    }

    fetchQueries(): Promise<any> {
        return this.restCallHandlerService.get("FETCH_CYPHER_QUERY");
    }

    deleteQuery(reportname): Promise<any> {
        return this.restCallHandlerService.postFormData("DELETE_CYPHER_QUERY", reportname).toPromise();
    }

    uploadFile(formData): Promise<any> {
        return this.restCallHandlerService.postFormData("UPLOAD_QUERY_FILE", formData).toPromise();
    }

    // downloadFile(filepath) : Observable<any>{
    //     return this.restCallHandlerService.getObserve("DOWNLOAD_CYPHER_QUERY",{ 'path': filepath });
    // }


    downloadFile(filepath): Observable<Blob> {
        let authToken = this.dataShare.getAuthorizationToken();
        let headers_object = new HttpHeaders();
        headers_object = headers_object.append("Authorization", authToken);
        let params = new HttpParams();
        params = params.append("path", filepath);
        return this.httpClient.get("/PlatformService/blockchain/queryBuilder/getFileContents", { headers: headers_object, responseType: 'blob', params });
    }

    testQuery(reportname, frequency): Promise<any> {
        let queryObj = { 'reportName': reportname, 'frequency': frequency };
        console.log('queryObj', queryObj);
        return this.restCallHandlerService.get("TEST_QUERY", queryObj);
    }


}