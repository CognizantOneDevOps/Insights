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
import { CookieService } from 'ngx-cookie-service';
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

export interface IAuthenticationService {
    getAuthentication(authToken: string, msg: string): void;
    validateSession(): void;
    logout(): Promise<any>;
    getGrafanaCurrentOrgAndRole(): Promise<any>;
    getCurrentUserOrgs(): Promise<any>;
    getUsers(): Promise<any> ;
}
 

@Injectable()
export class GrafanaAuthenticationService implements IAuthenticationService {
    response: any;
    location: Location;
    constructor(location: Location, private router: Router,
        private cookieService: CookieService, private restCallHandlerService: RestCallHandlerService
    ) {


    }

    public getAuthentication(authToken: string, msg: string): void {
        if (authToken === undefined) {
            this.router.navigate(['/login']);
        } else {
            var msg = "auth token exists";
        }
    }

    public validateSession(): void {
        var authToken = this.cookieService.get('Authorization');
        //console.log(authToken)
        if (authToken === undefined) {
            this.cookieService.delete('Authorization');
            this.router.navigate(['/login']);
        } else {
            var dashboardSessionExpirationTime = this.cookieService.get('DashboardSessionExpiration');
            var date = new Date();
            //console.log(dashboardSessionExpirationTime)
            if (new Date(dashboardSessionExpirationTime) > date) {
                var minutes = 30;
                //console.log("Inside validateSession");
                date.setTime(date.getTime() + (minutes * 60 * 1000));
                this.cookieService.set('Authorization', authToken, date);
            } else {
                this.cookieService.delete('Authorization');
                this.router.navigate(['/login']);
            }
        }
    }

    public logout(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("LOGOUT");
    }

    public getGrafanaCurrentOrgAndRole(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("GRAPANA_CURRENT_ROLE_ORG");
    }

    public getCurrentUserOrgs(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS");
    }

    public switchUserOrg(orgId: number): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.postWithParameter("ACCESS_GROUP_MANAGEMENT_SWITCH_ORGS", { "orgId": orgId }, { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
    }

   public getUsers(): Promise<any> {
            var restHandler = this.restCallHandlerService;
             return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS");
    }
}
