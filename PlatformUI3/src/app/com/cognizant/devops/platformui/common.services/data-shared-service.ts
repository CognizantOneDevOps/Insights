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
import { DatePipe } from '@angular/common';
import { CookieService } from 'ngx-cookie-service';
import { Router } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material';
import { ApplicationMessageDialog } from '@insights/app/modules/application-dialog/application-message-dialog';
import * as CryptoJS from 'crypto-js';
import { v4 as uuid } from 'uuid';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { AutheticationProtocol } from '@insights/common/insights-enum';

@Injectable()
export class DataSharedService {
  sessionExpireMessage: String = "";
  private userSource = new BehaviorSubject<String>('admin');
  currentUser = this.userSource.asObservable();

  constructor(@Inject(SESSION_STORAGE) private storage: StorageService, private datePipe: DatePipe,
    private cookieService: CookieService, public router: Router,
    public dialog: MatDialog) { }

  public changeUser(user: String) {
    this.userSource.next(user)
  }

  public uploadOrFetchLogo(imageSrc: any) {
    if (imageSrc != 'DefaultLogo') {
      this.storage.set("customerLogo", imageSrc);
    } else {
      this.storage.set("customerLogo", "DefaultLogo");
    }
  }

  public getCustomerLogo(): any {
    return this.storage.get("customerLogo");
  }

  public removeCustomerLogoFromSesssion(): void {
    this.storage.remove("customerLogo");
  }

  public setUserName(userNameStr: String): string {
    let userName = "";
    if (InsightsInitService.autheticationProtocol == AutheticationProtocol.SAML.toString()
      || InsightsInitService.autheticationProtocol == AutheticationProtocol.Kerberos.toString()
      || InsightsInitService.autheticationProtocol == AutheticationProtocol.JWT.toString()) {
      userName = this.getSSOUserName();
    } else if (InsightsInitService.autheticationProtocol == AutheticationProtocol.NativeGrafana.toString()) {
      userName = userNameStr != undefined ? userNameStr.replace(/['"]+/g, '') : "";
    }
    this.storage.set("userName", userName);
    return userName;
  }

  public getUserName() {
    return this.storage.get("userName");
  }

  public getOrgId() {
    return this.storage.get("orgId");
  }

  public setSSOUserName(ssouserName: String) {
    this.storage.set("SsoUserName", ssouserName);
  }

  public getSSOUserName() {
    return this.storage.get("SsoUserName");
  }

  public logoutInitilize() {
    if (InsightsInitService.autheticationProtocol == AutheticationProtocol.SAML.toString()
      || InsightsInitService.autheticationProtocol == AutheticationProtocol.JWT.toString()) {
      this.router.navigate(['/logout/2']);
    } else if (InsightsInitService.autheticationProtocol == AutheticationProtocol.NativeGrafana.toString()) {
      this.router.navigate(['/logout/1']);
    }

  }

  public getlogoutDisplay(): boolean {
    return InsightsInitService.enableLogoutButton;
  }

  public setSSOLogoutURL(logoutURL: String) {
    this.storage.set("SSOLogoutUrl", logoutURL);
  }

  public getSSOLogoutURL() {
    return this.storage.get("SSOLogoutUrl");
  }

  public getWebAuthToken() {
    return this.storage.get("SSO-USER");
  }

  public setWebAuthToken(strWebAuth: string) {
    this.storage.set("SSO-USER", strWebAuth);
  }

  public setAuthorizationToken(strAuthorization: string) {
    if (InsightsInitService.autheticationProtocol == AutheticationProtocol.SAML.toString()
      || InsightsInitService.autheticationProtocol == AutheticationProtocol.Kerberos.toString()
      || InsightsInitService.autheticationProtocol == AutheticationProtocol.JWT.toString()) {
      this.storage.set("Authorization", strAuthorization);
    } else if (InsightsInitService.autheticationProtocol == AutheticationProtocol.NativeGrafana.toString()) {
      var auth_uuid = uuid();
      auth_uuid = auth_uuid.substring(0, 15);
      var auth = this.encryptData(auth_uuid, strAuthorization) + auth_uuid;
      this.storage.set("Authorization", auth);
    }
  }

  public getAuthorizationToken() {
    return this.storage.get("Authorization");
  }

  public removeAuthorization() {
    this.storage.remove('Authorization');
    this.storage.remove('SSO-USER');
  }

  public setOrgAndRole(orgName: String, orgId: any, role: String) {
    this.storage.set("userRole", role);
    this.storage.set("orgName", orgName);
    this.storage.set("orgId", orgId);
  }

  public getTimeZone() {
    return this.storage.get("timeZone");
  }

  public getStorageService(): StorageService {
    return this.storage;
  }

  public getStoragedProperty(key: string): any {

    return this.storage.get(key);
  }

  public storeTimeZone() {
    var date = new Date();
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
    var dateWithTimeZone = this.datePipe.transform(date, 'yyyy-MM-ddTHH:mm:ssZ', zoneOffset);
    return dateWithTimeZone;
  }

  public setSession() {
    var date = new Date();
    var minutes = 30;
    date.setTime(date.getTime() + (minutes * 60 * 1000));
    var dateSessionExpiration = date.getTime();
    this.storage.set("dateSessionExpiration", dateSessionExpiration);
  }

  public validateSession(): boolean {
    var authToken = this.getAuthorizationToken();
    this.sessionExpireMessage = "The existing session has expired. You will be redirected to the home page. Request you to Login again to continue using Insights. Thank you!";
    var varDateSessionExpiration = this.storage.get('dateSessionExpiration')
    if (authToken === undefined) {
      this.storage.remove('Authorization');
      if (InsightsInitService.autheticationProtocol == "NativeGrafana") {
        this.router.navigate(['/login']);
      }
    } else {
      if (InsightsInitService.autheticationProtocol == "NativeGrafana") {
        var varDateSessionExpirationTime = new Date(this.storage.get('dateSessionExpiration'));
        var date = new Date();
        // console.log(dashboardSessionExpirationTime + "  ===== " + date);
        if (varDateSessionExpiration == undefined) {
          this.clearSessionData()
          return true;
        }
        if ((varDateSessionExpirationTime < date)) {
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

  public encryptData(keys, value): string {
    var encryptedValue = CryptoJS.AES.encrypt(value, keys);
    return encryptedValue.toString();
  }

  public getCurrentYear() {
    var Year = new Date().getFullYear();
    return Year;
  }

  getCustomizeName(name): String {
    var returnName: String = "";
    if (name != undefined && name.length > 16) {
      returnName = (name.substring(0, 16)) + '..';
    } else {
      returnName = (name);
    }
    return returnName;
  }

  validateEmailAddresses(emailAddress):boolean {
    var emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    var isValidated = true;
    var emails = emailAddress.replace(/\s/g, "").split(",");
    for (var email of emails) {
      if (!emailRegex.test(email)) {
        isValidated = false;
        break;
      }
    }
    return isValidated;
  }

  convertDateToSpecificDateFormat(dateObject,format){
    var formattedDate;
    formattedDate = this.datePipe.transform(
      dateObject,
      format
    );
    return formattedDate;
  }
}