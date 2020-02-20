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
import { DataSharedService } from '@insights/common/data-shared-service';
import { Observable } from 'rxjs'
import { RestAPIurlService } from '@insights/common/rest-apiurl.service'

@Injectable()
export class LoginService {
    response: any;
    grafanaresponse: any;
    constructor(private restCallHandlerService: RestCallHandlerService, private dataShare: DataSharedService,
        private restAPIUrlService: RestAPIurlService, ) {
    }

    public loginUserAuthentication(username: string, password: string): Promise<any> {
        var token = this.dataShare.getAuthorizationToken();
        this.response = this.restCallHandlerService.post("USER_AUTHNTICATE", {}, { 'Authorization': token })
        console.log(this.response)
        return this.response.toPromise();
    }

    public loginSSOUserDetail(): Promise<any> {
        console.log("SSO Login Call");
        setTimeout(() => console.log("SSO Login Call"), 10);
        this.response = this.restCallHandlerService.getSSO("SSO_DETAIL", {}, {})
        return this.response;
    }

    public loginSSOUserDetailLogin(): Promise<any> {
        console.log("SSO Login Call");
        setTimeout(() => console.log("SSO Login Call"), 10);
        this.response = this.restCallHandlerService.getSSO("USER_AUTHNTICATE", {}, {})
        return this.response;
    }

    public loginSSO() {
        var url = this.restAPIUrlService.getRestCallUrl("SSO_URL");
        setTimeout(() => window.location.replace(url), 10);
    }
   
    public ssoInsightsLogout(): any {
         return this.restCallHandlerService.getSSO("SSO_INSIGHTS_URL_LOGOUT");
        //setTimeout(() => window.location.replace(url), 100);
    }
    
    public singleLogoutSSO(url) {
        console.log(" Inside singleLogoutSSO ");
        setTimeout(() => window.location.replace(url), 10);
    }
    
    public logoutSSO() {
        console.log(" Inside logoutSSO ");
        var singleurl = this.restAPIUrlService.getRestCallUrl("SSO_URL_LOGOUT");
        setTimeout(() => window.location.replace(singleurl), 10);
    }

    public logout(): Promise<any> {
        return this.restCallHandlerService.get("LOGOUT");
    }
}
