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


export interface IGrafanaDashboardService {
    loadOrganizations(): Promise<any>;
    getCurrentUserOrgs(): Promise<any>;
    getUsers(): Promise<any>;
}




@Injectable()
export class GrafanaDashboardService implements IGrafanaDashboardService {

    constructor(private restCallHandlerService: RestCallHandlerService) {
    }

    loadOrganizations(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ORGS_GET");
    }



    getCurrentUserOrgs(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_CURRENT_USER_ORGS");
    }

    getUsers(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("ACCESS_GROUP_MANAGEMENT_GET_USERS");
    }

    searchDashboard(): Promise<any> {
        var restHandler = this.restCallHandlerService;
        return restHandler.get("SEARCH_DASHBOARD");
    }

}

