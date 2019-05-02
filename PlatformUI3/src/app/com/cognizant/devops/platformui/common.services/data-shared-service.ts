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

import { Injectable, Inject } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { SESSION_STORAGE, StorageService } from 'ngx-webstorage-service';
import { CommonModule, DatePipe } from '@angular/common';


@Injectable()
export class DataSharedService {

  private userSource = new BehaviorSubject<String>('admin');
  currentUser = this.userSource.asObservable();

  constructor(@Inject(SESSION_STORAGE) private storage: StorageService, private datePipe: DatePipe) { }

  public changeUser(user: String) {
    this.userSource.next(user)
  }

  public uploadOrFetchLogo(imageSrc: any) {
    //console.log("in uploadOrFetchLogo ")
    if (imageSrc != 'DefaultLogo') {
      this.storage.set("customerLogo", imageSrc);
    } else {
      this.storage.set("customerLogo", "DefaultLogo");
    }
  }

  public getCustomerLogo() {
    return this.storage.get("customerLogo");
  }

  public setUserName(userName: String) {
    this.storage.set("userName", userName);
  }

  public setOrgAndRole(orgName: String, orgId: any, role: String) {
    this.storage.set("userRole", role);
    this.storage.set("orgName", orgName);
    this.storage.set("orgId", orgId);
  }

  public getUserName() {
    return this.storage.get("userName");
  }

  public getStorageService(): StorageService {
    return this.storage;
  }

  public getTimeZone() {
    return this.storage.get("timeZone");
  }


 public getStoragedProperty(key: string): any {
  
    return this.storage.get(key);
  }
  
  public storeTimeZone() {
    var date = new Date();
    //const timeZoneOffset = date.getTimezoneOffset(); " ==== " + timeZoneOffset +
    var zone = this.datePipe.transform(date, 'ZZZZ')
    var zoneOffset = zone.slice(3, zone.length);
    var dateStr = new Date().toTimeString();
    var parts = dateStr.match(/\(([^)]+)\)/i); //time
    var timezone = parts[1];
    this.storage.set("timeZone", timezone);  
    this.storage.set("timeZoneOffSet", zone);

  }
  
 public convertDateToZone(dateStr: string): string {
    var date = new Date(dateStr);
    var zone = this.storage.get("timeZone");
    var zoneOffset = this.storage.get("timeZoneOffSet");
    //var utcDate = this.datePipe.transform(date, 'yyyy-MM-ddTHH:mm:ssZ', '+0000');
    var dateWithTimeZone = this.datePipe.transform(date, 'yyyy-MM-ddTHH:mm:ssZ', zoneOffset);//  '+0530' utcDate
    console.log(date + " ==== " + zone + " ==== " + zoneOffset + " ==== " + dateWithTimeZone + " ====  " + + " ====  " + dateWithTimeZone.toString());
    return dateWithTimeZone;
  }
 

}