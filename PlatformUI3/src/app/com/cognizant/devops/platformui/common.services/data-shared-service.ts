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
import { Router, NavigationExtras } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { ApplicationMessageDialog } from '@insights/app/modules/application-dialog/application-message-dialog';

@Injectable()
export class DataSharedService {

  sessionExpireMessage: String = "";
  private userSource = new BehaviorSubject<String>('admin');
  currentUser = this.userSource.asObservable();

  constructor(@Inject(SESSION_STORAGE) private storage: StorageService, private datePipe: DatePipe, public router: Router, private cookieService: CookieService, public dialog: MatDialog) { }

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
  public setAuthorizationToken(strAuthorization: string) {
    this.storage.set("Authorization", strAuthorization);
  }

  public getAuthorizationToken() {
    return this.storage.get("Authorization");
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

  public setSession() {
    var date = new Date();
    var minutes = 30;
    date.setTime(date.getTime() + (minutes * 60 * 1000));
    var dateDashboardSessionExpiration = date.getTime();
    // console.log(dateDashboardSessionExpiration + "  @@@@@@  " + date)
    this.storage.set("dateDashboardSessionExpiration", dateDashboardSessionExpiration);
  }

  public validateSession(): boolean {
    var authToken = this.getAuthorizationToken();
    this.sessionExpireMessage = "The existing session has expired. You will be redirected to the home page. Request you to Login again to continue using Insights. Thank you!";
    var sessionStorageDateDashboardSessionExpiration = this.storage.get('dateDashboardSessionExpiration')
    if (authToken === undefined) {
      this.storage.remove('Authorization');
      this.router.navigate(['/login']);
    } else {
      var dashboardSessionExpirationTime = new Date(this.storage.get('dateDashboardSessionExpiration'));
      var date = new Date();
      // console.log(dashboardSessionExpirationTime + "  ===== " + date);
      if (sessionStorageDateDashboardSessionExpiration == undefined) {
        this.clearSessionData()
        return true;
      }
      if ((dashboardSessionExpirationTime < date)) {
        var dialogRef = this.sessionExpiredMessage(this.sessionExpireMessage, "WARN", true);
        this.clearSessionData()
        return true;

      } else {
        //console.log("session present");
        var minutes = 30;
        date.setTime(date.getTime() + (minutes * 60 * 1000));
        this.storage.set('Authorization', authToken);
        this.setSession()
        return false;
      }
    }
  }

  clearSessionData(): void {
    this.deleteAllPreviousCookies();
    this.storage.clear();
  }


  deleteAllPreviousCookies(): void {
    let allCookies = this.cookieService.getAll();
    for (let key of Object.keys(allCookies)) {
      this.cookieService.delete(key);
    }
  }

  //Method used only for session expired
  public sessionExpiredMessage(message, type, values): MatDialogRef<ApplicationMessageDialog> {
    console.log(" in sessionExpiredMessage ")
    const dialogRef = this.dialog.open(ApplicationMessageDialog, {
      panelClass: 'DialogBox',
      width: '40%',
      height: '33%',
      disableClose: true,
      data: {
        title: "Message",
        message: message,
        type: type,
        values: true
      }
    });

    return dialogRef;

  }
}