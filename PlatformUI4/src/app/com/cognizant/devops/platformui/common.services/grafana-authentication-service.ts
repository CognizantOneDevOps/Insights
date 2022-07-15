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
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { DataSharedService } from '@insights/common/data-shared-service';
import { RestAPIurlService } from '@insights/common/rest-apiurl.service'
import { BehaviorSubject } from 'rxjs';

export interface IAuthenticationService {
    getCurrentUserOrgs(): Promise<any>;
    getUsers(): Promise<any>;
    getLogoImage(): Promise<any>;
}


@Injectable()
export class GrafanaAuthenticationService implements IAuthenticationService {
    public serverConfigSubject = new BehaviorSubject<any>('');
    public onOkSubject=new BehaviorSubject<any>('');
    public iconClkSubject=new BehaviorSubject<any>('');
    private restHandler: RestCallHandlerService;

    response: any;
    location: Location;
    constructor(location: Location, private router: Router,
        private dataShare: DataSharedService, private restCallHandlerService: RestCallHandlerService,
        private restAPIUrlService: RestAPIurlService) {
    }
    public getDashboardByUid(uuid:any,orgId:any): Promise<any>{
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GET_DASHBOARD_BY_UID",{'uuid': uuid, 'orgId': orgId});

    }
    public saveDashboardAsPDF(dashData:any): Promise<any> {
        return this.restCallHandlerService.postWithData("SAVE_GRAFANA_DASHBOARD_CONFIG", dashData, "", { 'Cntent-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    public updateDashboardAsPDF(dashData:any): Promise<any> {
        return this.restCallHandlerService.postWithData("UPDATE_DASHBOARD_CONFIGS", dashData, "", { 'Cntent-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    deleteDashboard(id: any): Promise<any> {
        return this.restCallHandlerService.postWithParameter("DELETE_DASHBOARD_CONFIGS", {'id': id} , { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    public getTemplateByQuery(query:any): Promise<any>{
        var restHandler = this.restCallHandlerService;
        return restHandler.postWithData("GET_TEMPLATE_BY_QUERY", query ,"",{'Content-Type':'application/json'}).toPromise();

    }
    public fetchDashboardConfigs(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("FETCH_DASHBOARD_CONFIGS");
    }    
    public getCurrentUserOrgs(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS");
    }

    public getCurrentUserWithOrgs(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS_WITH_ORGS");
    }

    public switchUserOrg(orgId: number): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.postWithParameter("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS", { "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    public getUsers(): Promise<any> {
        console.log("Inside getUsers");
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS");
    }

    public getLogoImage(): any {
        var restCallUrl = this.restAPIUrlService.getRestCallUrl("GET_LOGO_IMAGE");
        return this.restCallHandlerService.getJSON(restCallUrl);
    }

    currentUserDetail(login: string) {
        var restHandler = this.restCallHandlerService;
        return restHandler.postWithData("CURRENT_USER_DETAIL", login, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }
    downloadPDF(PDFRequestJson: string) {
        return this.restCallHandlerService.postWithPDFData("DOWNLOAD_REPORT_PDF", PDFRequestJson,"",{ 'Content-Type': 'application/json' },{'responseType':'blob'}).toPromise();
    }
    getExecutionId(workflowId:string){
        return this.restCallHandlerService.postWithData("GET_EXECUTIONID", workflowId, "", { 'Content-Type': 'application/json' }).toPromise();
    }

    setRestartStatus(idStatusMapping: string){
        console.log(idStatusMapping);
        return this.restCallHandlerService.postWithData("SET_DASHBOARD_STATUS", idStatusMapping, { 'Content-Type': 'application/json' }).toPromise();
    }

    getEmailConfigurationStatus() : Promise<any>{
        return this.restCallHandlerService.get("GET_EMAIL_CONFIGURATION_STATUS");
    }

    setActiveState(jsonObject : string) : Promise<any> {
        return this.restCallHandlerService.postWithData("SET_DASHBOARD_ACTIVE_STATE", jsonObject, "", { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

    editUserPreference(theme: String): Promise<any> {
        return this.restCallHandlerService
        .putWithParameter(
            "UPDATE_UESRS_PREFERENCE",
            { themePreference: theme },
            { "Content-Type": "application/json" }
        )
        .toPromise();
    }

  getUserPreferenceTheme(): Promise<any> {
    return this.restCallHandlerService.get("GET_USER_PREFERENCE_THEME");
  }

}
