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
import { DataSharedService } from '@insights/common/data-shared-service';
import { Router, ParamMap, ActivatedRoute } from '@angular/router';
import { RestCallHandlerService } from '@insights/common/rest-call-handler.service';
import { LoginService } from '@insights/app/login/login.service';
import { HttpErrorResponse } from "@angular/common/http/src/response";
import { RestAPIurlService } from '@insights/common/rest-apiurl.service'
import { CookieService } from 'ngx-cookie';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';


@Component({
  selector: 'app-ssologin',
  templateUrl: './ssologin.component.html',
  styleUrls: ['./ssologin.component.css', './../login/login.component.css']
})
export class SSOLoginComponent implements OnInit {

  showThrobber: boolean = false;

  constructor(private restCallHandlerService: RestCallHandlerService, private dataShare: DataSharedService,
    public router: Router, private cookieService: CookieService,
    private loginService: LoginService, private restAPIUrlService: RestAPIurlService,
    private route: ActivatedRoute, public messageDialog: MessageDialogService) {
    console.log("Inside new ssoLogin component constructor");
    this.dataShare.setSession();
  }

  ngOnInit() {
    this.showThrobber = true;
    this.getSSODetail();
  }

  public getSSODetail() {
    this.loginService.loginSSOUserDetail()
      .then(response => {
        console.log("in success ");
        this.handleTokenSuccess(response)
      }).catch(err => { console.log(err); this.handleTokenError(err) });
  }

  handleTokenSuccess(apiRes: any) {
    console.log(" inside handleTokenSuccess " + apiRes);
    if (apiRes.status = "success") {
      var resData = apiRes.data;
      if (this.isEmptyObject(resData)) {
        console.log("Response is empty ");
      }
      if (resData != null || resData != undefined || !this.isEmptyObject(resData)) {
        let headers = apiRes.headers;
        var date = new Date();
        var minutes = 30;
        date.setTime(date.getTime() + (minutes * 60 * 1000));
        for (var key in resData) {
          var value = resData[key];
          //console.log("  key  " + key + "  keyvalue " + value);
          if (value == "" || value == undefined) {
            console.log("value is not define for cookie " + key);
          } else {
            if (key != "jtoken" && key != "postLogoutURL" && key != "insights-sso-givenname") {
              this.cookieService.put(key, value, { storeUnencoded: true, path: '/' })
            }
            if (key == "insights-sso-token") {
              this.dataShare.setWebAuthToken(resData["insights-sso-token"]);
              this.cookieService.put("username", resData["insights-sso-token"], { storeUnencoded: true, path: '/' });
            } else if (key == "jtoken") {
              this.dataShare.setAuthorizationToken(resData["jtoken"]);
            } else if (key == "insights-sso-givenname") {
              this.dataShare.setSSOUserName(resData["insights-sso-givenname"]);
            } else if (key == "postLogoutURL") {
              this.dataShare.setSSOLogoutURL(resData["postLogoutURL"]);
            }
          }
        }
        this.callHomePage();
      } else {
        console.log(" resData is undefined or null");
      }
    } else {
      console.log(" API response status " + apiRes.status)
      var requestmessage = " Unable to login on your organization page, try after removeing your browser cache/cookies if issue persist  Please contact to you Administrator or check service log for more detail."
      this.messageDialog.showApplicationsMessage(requestmessage, "ERROR");
    }
  }

  isEmptyObject(obj) {
    return (obj && (Object.keys(obj).length === 0));
  }

  handleTokenError(error: HttpErrorResponse) {
    console.log("Inside handleTokenError")
    console.log(error.status);
    if (error.status === 401) {
    } else if (error.status === 403) {
    }
  }

  callHomePage() {
    var self = this;
    this.showThrobber = false;
    setTimeout(() => {
      self.router.navigateByUrl('/InSights/Home');
    }, 1);
  }

  callSSOLogin() {
    var url = this.restAPIUrlService.getRestCallUrl("SSO_URL");
    console.log(url);
    setTimeout(() => window.location.replace(url), 4000);
  }

}
