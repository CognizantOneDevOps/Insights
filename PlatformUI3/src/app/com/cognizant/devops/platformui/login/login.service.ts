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


@Injectable()
export class LoginService {
    response: any;
    grafanaresponse: any;
    constructor(private restCallHandlerService: RestCallHandlerService ) {
    }

    public loginUserAuthentication(username: string, password: string): Promise<any> {
        var token = 'Basic ' + btoa(username + ":" + password);
        this.response = this.restCallHandlerService.post("USER_AUTHNTICATE", {}, { 'Authorization': token })
        //console.log(this.response.toPromise());
        return this.response.toPromise();
    }

    public loginGrafanaFromApp(username: string, password: string): Promise<any> {
        let grafanaURL = "http://"+username+":"+password+"@localhost:3000/api/org";
        this.grafanaresponse = this.restCallHandlerService.getJSONUsingObservable(grafanaURL).toPromise();
        //console.log(this.grafanaresponse)
        return this.grafanaresponse;
    }
}
