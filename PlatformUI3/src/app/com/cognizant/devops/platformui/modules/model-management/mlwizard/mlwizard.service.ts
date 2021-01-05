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
import { Observable, BehaviorSubject } from 'rxjs';
import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';

export interface IMLWizardService {
  uploadDataWithConfig(id: string, csvFile: File, configObj: Object, trainingPerc: number, predictionColumn: string, numOfModels: string, taskDetails: string, ptype: string): Observable<any>;
  validateUsecaseID(id: string): Observable<any>;
}

@Injectable()
export class MLWizardService implements IMLWizardService {

  constructor(private restCallHandlerService: RestCallHandlerService, private httpClient: HttpClient,
    private dataShare: DataSharedService) { }
     sendHeaders = new BehaviorSubject<any>(' ');

  validateUsecaseID(id: string): Observable<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.postWithParameter("VALIDATE_USECASEID", {'usecase': id});
    
    // var authToken = this.dataShare.getAuthorizationToken();
    //     let params = new HttpParams();
    //     params = params.append("usecase", id);
    //     var headers_object = new HttpHeaders();
    //     headers_object = headers_object.append("Content-Type", "multipart/form-data");
    //     headers_object = headers_object.append("Authorization", authToken);
    //     return this.httpClient.post("http://localhost:8080/PlatformService/admin/trainmodels/validateUsecaseName", { headers: headers_object, params });
  }
  
  uploadDataWithConfig(id: string, csvFile: File, configObj: Object, trainingPerc: number, 
    predictionColumn: string, numOfModels: string, taskDetails: string, ptype: string): Observable<any> {
    const fd: FormData = new FormData();
    fd.append('file',csvFile);
    //fd.append('configuration', configObj.toString());
    var restHandler = this.restCallHandlerService;
    return restHandler.postFormDataWithParameter("UPLOAD_CSV", fd, { usecase: id, configuration: JSON.stringify(configObj), 
      trainingPerc: trainingPerc, predictionColumn: predictionColumn, numOfModels: numOfModels, taskDetails: taskDetails,predictionType:ptype });
  }

  //services for automl component
  splitNtrain(usecase: string, splitRatio: number, predictionColumn: string, numOfModels: number): Observable<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.postWithParameter("SPLIT_N_TRAIN",
    { usecase: usecase,
      splitRatio: splitRatio,
      predictionColumn: predictionColumn,
      numOfModels: numOfModels });
  }

  pollAutoMLStat(usecase: string, url: string): Promise<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.get("POLL_STATUS",{url: url, usecase: usecase});
  }

  getLeaderboard(usecase: string): Promise<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.get("GET_LEADERBOARD", { usecase: usecase });
  }
  //services for prediction component
  getPredictions(usecase: string, modelName: string ): Promise<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.get("PREDICT", {usecase: usecase, modelName: modelName});
  }

  saveMOJO(usecase: string, modelId: string): Promise<any> {
    var restHandler = this.restCallHandlerService;
    return restHandler.get("DOWNLOAD_MOJO", { usecase: usecase, modelId: modelId});
  }

  getPredictionTypes(): Promise<any> 
  {
    var restHandler = this.restCallHandlerService;
    return restHandler.get("GET_PREDICTION_TYPES");
  }

}
