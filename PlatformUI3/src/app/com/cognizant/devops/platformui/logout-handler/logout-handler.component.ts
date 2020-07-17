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
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { InsightsInitService } from '@insights/common/insights-initservice';
import { DataSharedService } from '@insights/common/data-shared-service';
import { CookieService } from 'ngx-cookie-service';
import { LoginService } from '@insights/app/login/login.service';
import { AutheticationProtocol } from '@insights/common/insights-enum';

@Component({
  selector: 'app-logout-handler',
  templateUrl: './logout-handler.component.html',
  styleUrls: ['./logout-handler.component.css']
})
export class LogoutHandlerComponent implements OnInit {

  id: string;
  receivedMessage: string = "";
  logoutUrl: string;

  constructor(private route: ActivatedRoute, private router: Router,
    public messageDialog: MessageDialogService, private loginService: LoginService,
    private dataShare: DataSharedService, private cookieService: CookieService,
    private config: InsightsInitService) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.id = params['id'];
      console.log(" Logout code " + this.id);
      if (this.id == "1") {//normal logout
        this.logoutApplication(1);
      } else if (this.id == "2") {//SSO Logout
        this.logoutApplication(2);
      } else if (this.id.startsWith("8")) {
        //console.log(" Response from logout service " + this.id);
        this.logoutProcesingWithMassage();
      } else {
        this.logoutProcesingWithMassage();
      }
    });
    //this.logoutUrl=this.dataShare.getSSOLogoutURL();
    //console.log("this.logoutUrl  "+this.logoutUrl);
  }

  public logoutApplication(logouttype: number): void {
    var self = this;
    if (logouttype == 2) {
      console.log("In sso logout ");
      this.loginService.ssoInsightsLogout().then(function (responsedata) {
        //console.log(responsedata);
        if (responsedata.status = "success") {
          if (InsightsInitService.autheticationProtocol == AutheticationProtocol.SAML.toString()) {
            self.loginService.singleLogoutSSO(self.dataShare.getSSOLogoutURL());
          } else {
            setTimeout(() => self.router.navigate(['/login']), 1);
          }
        }
      });
    } else {
      console.log("In normal logout ")
      var self = this.logoutGrafana();
      this.loginService.logout()
        .then(function (data) {
          console.log(data);
          //if (data.status == "success") {
			console.log(" logout status SUCCESS SUCCESS")
            setTimeout(() => self.router.navigate(['/login']), 1);
          //}
        });
    }
    this.deleteAllPreviousCookiesAndSessionValue();
  }

  private logoutGrafana() {
    if (this.config.getGrafanaHost()) {
      var self = this;
      var uniqueString = "grfanaLoginIframe";
      var iframe = document.createElement("iframe");
      iframe.id = uniqueString;
      document.body.appendChild(iframe);
      iframe.style.display = "none";
      iframe.contentWindow.name = uniqueString;
      // construct a form with hidden inputs, targeting the iframe
      var form = document.createElement("form");
      form.target = uniqueString;
      form.action = InsightsInitService.grafanaHost + "/logout";
      form.method = "GET";
      document.body.appendChild(form);
      form.submit();
    }
    return self;
  }

  logoutProcesingWithMassage() {
    console.log("inside logoutProcesingWithMassage ");
    var self = this;
    this.route.queryParams.subscribe(params => {
      console.log(params);
      if (params["message"] != undefined) {
        self.receivedMessage = params["message"];
      }
      if (params["logout_url"] != undefined) {
        self.logoutUrl = params["logout_url"];
      } else {
        this.logoutUrl = this.dataShare.getSSOLogoutURL();
      }
      //console.log(this.receivedMessage);
      self.logoutProcessing(this.receivedMessage);
    });
  }

  public logoutProcessing(message: string) {

    var self = this;
    if (message != "" && message != undefined) {
      var msg = "There was error in application, Please login again or contact to your Administractor : " + message + "(" + this.id + ")";
      const dialogRef = this.messageDialog.showConfirmationMessage("Logout Message", msg, "ERROR", "ALERT", "30%")
      dialogRef.afterClosed().subscribe(result => {
        //console.log('The dialog was closed  ' + result);
        if (result == 'yes') {
          if (InsightsInitService.autheticationProtocol == "SAML") {
            console.error(" logging out from sso " + self.logoutUrl);
            if (this.logoutUrl != undefined) {
              this.loginService.singleLogoutSSO(self.logoutUrl);
            } else {
              console.error("No logout URL found");
            }
          } else {
            setTimeout(() => self.router.navigate(['/login']), 1);
          }
          this.deleteAllPreviousCookiesAndSessionValue();
        }
      });
    } else {
      this.deleteAllPreviousCookiesAndSessionValue();
    }
  }

  deleteAllPreviousCookiesAndSessionValue(): void {
    //console.log("deleteAllPreviousCookiesAndSessionValue  ==== ");
    let allCookies = this.cookieService.getAll();
    for (let key of Object.keys(allCookies)) {
      this.cookieService.delete(key);
    }
    this.dataShare.setAuthorizationToken(undefined);
    this.dataShare.setWebAuthToken(undefined);
    this.dataShare.removeAuthorization();
  }

}
