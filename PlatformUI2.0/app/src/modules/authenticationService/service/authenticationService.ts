/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

/// <reference path="../../../_all.ts" />

module ISightApp {
    export interface IAuthenticationService {
      getAuthentication(authToken: string,msg: string): void;
      validateSession(): void;
      logout(): ng.IPromise<any>;
      getGrafanaCurrentOrgAndRole(): ng.IPromise<any>
    }

    export class AuthenticationService implements IAuthenticationService {
        static $inject = ['$location', '$cookies', '$resource', 'restEndpointService','restCallHandlerService'];
        constructor(private $location, private $cookies, private $resource, private restEndpointService: IRestEndpointService, private restCallHandlerService: IRestCallHandlerService) { }

        getAuthentication(authToken: string,msg: string): void  {
              if (authToken === undefined) {
                    this.$location.path('/InSights/login');
                } else {
                    var msg = "auth token exists";
                }
    		}
        
        validateSession():void {
            var authToken = this.$cookies.get('Authorization');
            if(authToken === undefined){
                this.$cookies.remove('Authorization');
                this.$location.path('/InSights/login');
            }else{
                var dashboardSessionExpirationTime = this.$cookies.get('DashboardSessionExpiration');
                var date = new Date();
                if(new Date(dashboardSessionExpirationTime) > date){
                    var minutes = 30;
                    date.setTime(date.getTime() + (minutes * 60 * 1000));
                    this.$cookies.put('Authorization', authToken, { expires: date });
                }else{
                    this.$cookies.remove('Authorization');
                    this.$location.path('/InSights/login');
                }
            }
        }
        
        logout(): ng.IPromise<any> {
            var restHandler = this.restCallHandlerService;
            return restHandler.get("LOGOUT");
        }
        
        getGrafanaCurrentOrgAndRole(): ng.IPromise<any> { 
            var restHandler = this.restCallHandlerService;
            return restHandler.get("GRAPANA_CURRENT_ROLE_ORG");
        }
    }
}
