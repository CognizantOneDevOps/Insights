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
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';

@Injectable()
export class KpiService {
    type: any;
    kpiArr=[];
    public fileUploadSubject=new BehaviorSubject<any>('');
    public setKpiSubject=new BehaviorSubject<any>([]);
    kpi: any;

    constructor(private restCallHandlerService: RestCallHandlerService) {
    }
    loadKpiCategory(): Promise<any> {
        return this.restCallHandlerService.get("KPI_CATEGORY");
      }
    loadKpiDataSource():Promise<any>{
        return this.restCallHandlerService.get("KPI_DATASOURCE");
    }
    saveDataforKpi(kpiAPIRequestJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("SAVE_DATA_KPI", kpiAPIRequestJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    updateDataforKpi(kpiAPIRequestJson: string): Promise<any> {
        return this.restCallHandlerService.postWithData("UPDATE_KPI", kpiAPIRequestJson, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    loadKpiList(): Promise<any> {
        return this.restCallHandlerService.get("LIST_KPI");
    }

    kpiUninstall(kpiId: string): Promise<any> {
        return this.restCallHandlerService.postWithData("DELETE_KPI",  kpiId ,"", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    uploadFile(formData): Promise<any> {
        return this.restCallHandlerService.postFormData("UPLOAD_BULK_KPI", formData).toPromise();
    }
    setType(type){
        this.type=type;
    }
    getType(){
        return this.type;
    }
    setKpiIdArr(arr){
     this.kpiArr=arr;
    }
    getKpiIdArr(){
        return this.kpiArr;
    }
    setSelectedKpi(kpi){
        this.kpi=kpi;
    }
    getSelectedKpi(){
        return this.kpi;
    }
}
