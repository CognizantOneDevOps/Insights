/*
*******************************************************************************
 * Copyright 2022 Cognizant Technology Solutions
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
 *******************************************************************************/
import { Injectable } from '@angular/core';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { DataSharedService } from '@insights/common/data-shared-service';
import { ReportManagementService } from '../reportmanagement/reportmanagement.service';

@Injectable({
  providedIn: 'root'
})
export class MultipleEmailConfigService {

  type: any;
  flagStatus:boolean = this.reportmanagementservice.isReport;

  listSource: string;

  
   constructor(private http: HttpClient,
    public dataShare: DataSharedService,
    private restCallHandlerService: RestCallHandlerService,
    public reportmanagementservice: ReportManagementService,
    ) { }  

  
  setType(type) {
    this.type = type;
  }
  getType() {
    return this.type;
  }

  getSource(){
    let flag = false;
    flag = this.reportmanagementservice.isReport
    console.log("isReport:"+ flag);
    if (flag == true){ 
      this.listSource = "Report"
      this.reportmanagementservice.isReport = false
    }
    else if(flag == false) {
      this.listSource = "GRAFANADASHBOARDPDFREPORT"
    }
    return this.listSource;
  }

  fetchReportList(username: string,src: string){
    var restHandler = this.restCallHandlerService;
    
    
    return restHandler.get("GET_DASH_LIST",
    {
      source : src,
      userName: username
    }
    );
  }

  public saveMultiEmailConfig(config: any): Promise<any>{
    // var src = this.getSource();
    return this.restCallHandlerService
      .postWithData("SAVE_EMAIL_CONFIG", config, {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }

  public editMultiEmailConfig(config: any): Promise<any>{
    var src = this.getSource();
    return this.restCallHandlerService
      .postWithData("UPDATE_EMAIL_CONFIG", config, {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }



  public deleteData(id: string):Promise<any>{
    return this.restCallHandlerService.postWithParameter("DELETE_EMAIL_CONFIG", {'id': id} , { 'Content-Type': 'application/x-www-form-urlencoded' }).toPromise();
  }

  public getMailConfig(src:string){
    return this.restCallHandlerService.get("GET_EMAIL_CONFIG",{source:src});
  }

  setActiveStatus(updateEmailConfigString: string) {
    return this.restCallHandlerService
      .postWithData("ACTIVE_TOGGLE_CHANGE", updateEmailConfigString, "", {
        "Content-Type": "application/x-www-form-urlencoded",
      })
      .toPromise();
  }
}
