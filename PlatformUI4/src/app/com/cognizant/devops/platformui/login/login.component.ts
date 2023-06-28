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

import {
  Component,
  OnInit,
  ElementRef,
  ViewChild,
  AfterViewInit,
  ChangeDetectorRef,
  AfterContentChecked,
  HostListener,
  ViewEncapsulation,
} from "@angular/core";
import { LoginService } from "@insights/app/login/login.service";
import { InsightsInitService } from "@insights/common/insights-initservice";
import { RestAPIurlService } from "@insights/common/rest-apiurl.service";
import { RestCallHandlerService } from "@insights/common/rest-call-handler.service";
import { CookieService } from "ngx-cookie-service"; //ngx-cookie
import { Router, ActivatedRoute, ParamMap, Params } from "@angular/router";
import { FormGroup, FormControl, Validators } from "@angular/forms";
import { DatePipe } from "@angular/common";
import { DataSharedService } from "@insights/common/data-shared-service";
import { ImageHandlerService } from "@insights/common/imageHandler.service";
import { MessageDialogService } from "@insights/app/modules/application-dialog/message-dialog-service";
import { GrafanaAuthenticationService } from "@insights/common/grafana-authentication-service";
import { AutheticationProtocol } from "@insights/common/insights-enum";
import { DomSanitizer } from "@angular/platform-browser";

export interface ILoginComponent {
  createAndValidateForm(): void;
  userAuthentication(): void;
}

@Component({
  selector: "app-login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  providers: [DatePipe],
  encapsulation: ViewEncapsulation.None,
})
export class LoginComponent implements OnInit, ILoginComponent, AfterViewInit {
  logMsg: string;
  isLoginError: boolean;
  isDisabled: boolean;
  showThrobber: boolean = false;
  cookies: string;
  username: string;
  password: string;
  imageSrc: string = "";
  resourceImage: any;
  loginForm: FormGroup;
  imageAlt: String = "";
  displayLoginPage: boolean = true;
  loginRequestSent: boolean = false;
  year: any;
  eventData: string;
  version: any;
  productName = {};

  constructor(
    private loginService: LoginService,
    private restAPIUrlService: RestAPIurlService,
    private restCallHandlerService: RestCallHandlerService,
    private cookieService: CookieService,
    private router: Router,
    private dataShare: DataSharedService,
    private datePipe: DatePipe,
    private imageHandeler: ImageHandlerService,
    private route: ActivatedRoute,
    public messageDialog: MessageDialogService,
    private grafanaService: GrafanaAuthenticationService,
    private activatedRoute: ActivatedRoute,
    private sanitizer: DomSanitizer
  ) {
    var self = this;
    this.loginRequestSent = false;
    console.log(" in login constructer");
  }

  ngOnInit() {
    var self = this;
    this.version = InsightsInitService.version;
    this.productName = InsightsInitService.product;
    console.log(
      "autheticationProtocol " + InsightsInitService.autheticationProtocol
    );
    if (
      InsightsInitService.autheticationProtocol ==
        AutheticationProtocol.SAML.toString() 
    ) {
      console.log(" SSO is enable calling saml login");
      this.dataShare.storeTimeZone();
      this.deleteAllPreviousCookies();
      this.displayLoginPage = false;
      setTimeout(() => {
        this.loginService.loginSSO();
      }, 1000);
    } else if (
      InsightsInitService.autheticationProtocol ==
      AutheticationProtocol.NativeGrafana.toString()
    ) {
      console.log("Continue on login page ");
      this.deleteAllPreviousCookies();
      this.createAndValidateForm();
      this.dataShare.storeTimeZone();
      this.dataShare.removeAuthorization();
      this.dataShare.setWebAuthToken("-");
    } else if (
      InsightsInitService.autheticationProtocol ==
      AutheticationProtocol.JWT.toString()
    ) {
      console.log("JWT SSO is enable ");
      this.dataShare.storeTimeZone();
      this.deleteAllPreviousCookies();
      this.createAndValidateForm();
      this.dataShare.storeTimeZone();
      this.dataShare.removeAuthorization();
      this.dataShare.setWebAuthToken("-");
      this.displayLoginPage = false;
      console.log("Event Data " + this.eventData);
    }
    this.year = this.dataShare.getCurrentYear();
  }

  ngAfterViewInit() {
    console.log(" ngAfterViewInit ");
    var self = this;
    if (
      InsightsInitService.autheticationProtocol ==
        AutheticationProtocol.NativeGrafana.toString() ||
      InsightsInitService.autheticationProtocol ==
        AutheticationProtocol.JWT.toString()
    ) {
      this.getImageAsyncData();
    }
    function receiveMessage(event) {
      console.log(
        " in receiveMessage ngAfterViewInit event origin " + event.origin
      );
      if (event.origin == InsightsInitService.jwtTokenOriginServerURL) {
        console.log(event.data);
        self.eventData = event.data;
        console.log(event);
        console.log(" origin " + event.origin);
        console.log(
          " event origin " +
            InsightsInitService.jwtTokenOriginServerURL +
            "  " +
            event.origin
        );
        self.callJWTAUTH();
      } else {
      }
    }
    if (
      InsightsInitService.autheticationProtocol ==
      AutheticationProtocol.JWT.toString()
    ) {
      console.log(
        " in ngAfterViewInit for  " +
          AutheticationProtocol.JWT.toString() +
          "  " +
          this.eventData
      );
      window.addEventListener("message", receiveMessage, false);
    }
  }

  public createAndValidateForm() {
    this.loginForm = new FormGroup({
      username: new FormControl("", Validators.required),
      password: new FormControl("", Validators.required),
    });
  }

  callJWTAUTH() {
    console.log("callJWTAUTH Event Data " + this.eventData);
    if (this.eventData != undefined) {
      var tokenData = JSON.parse(this.eventData);
      console.log(tokenData);
      var messageToken = tokenData.insightsMessageToken;
      console.log(" JWT SSO is enable token received  " + messageToken);
      if (messageToken != undefined) {
        setTimeout(() => {
          this.loginService.loginSSOJWT(messageToken);
        }, 1000);
      } else {
        console.error(" Unable to parse received token  " + this.eventData);
      }
    } else {
      console.error(" data not received from parent ");
    }
  }

  async getImageAsyncData() {
    try {
      this.resourceImage = await this.grafanaService.getLogoImage();
      this.dataShare.removeCustomerLogoFromSesssion();
      if (this.resourceImage.data.encodedString.length>0) {
        this.imageSrc = this.sanitizer.sanitize(
          0,
          "data:image/jpg;base64," + this.resourceImage.data.encodedString
        );
        this.imageHandeler.addImage("customer_logo_uploded", this.imageSrc);
        this.dataShare.uploadOrFetchLogo(this.imageSrc);
        (<HTMLInputElement>document.getElementById("logoimg")).src =
          this.imageSrc;
      } else {
        this.imageSrc = this.sanitizer.sanitize(
          0,
          "icons/png/insights.png"
        );
        (<HTMLInputElement>document.getElementById("logoimg")).src =
          this.imageSrc;
        this.imageAlt = "Cognizant log";
        this.dataShare.uploadOrFetchLogo("DefaultLogo");
      }
    } catch (error) {
      console.log(error);
    }
  }
  clearLog() {
    this.logMsg = "";
  }

  public userAuthentication(): void {
    this.deleteAllPreviousCookies();
    this.loginRequestSent = true;
    this.username = this.loginForm.value.username;
    this.password = this.loginForm.value.password;
    if (
      (this.username === "" || this.password === "") 
    ) {
      this.logMsg = "";
    } else if (
      InsightsInitService.autheticationProtocol ==
      AutheticationProtocol.NativeGrafana.toString()
    ) {
      var self = this;
      this.isDisabled = true;
      this.showThrobber = true;
      var token = "Basic " + btoa(this.username + ":" + this.password);
      this.dataShare.setAuthorizationToken(token);
      this.loginService
        .loginUserAuthentication(this.username, this.password)
        .then((data) => {
          var grafcookies = data.data;
          if (data.status === "success") {
            //SUCCESS
            self.showThrobber = false;
            var date = new Date();
            var minutes = 30;
            date.setTime(date.getTime() + minutes * 60 * 1000);

            this.dataShare.setAuthorizationToken(token);
            this.dataShare.setSession();
            this.cookies = "";
            console.log(grafcookies);
            for (var key in grafcookies) {
              if (
                key != "jtoken" &&
                key != "postLogoutURL" &&
                key != "insights-sso-givenname"
              ) {
                this.cookieService.set(key, grafcookies[key], 0, "/");
              } else if (key == "jtoken") {
                this.dataShare.setAuthorizationToken(grafcookies["jtoken"]);
              }
            }
            setTimeout(() => {
              self.router.navigate(["/InSights/Home"]);
            }, 2000);
          } else if (data.status === "failure") {
            self.showThrobber = false;
            self.isLoginError = true;
            self.logMsg = data.message;
            self.isDisabled = false;
            self.loginRequestSent = false;
          }
        })
        .catch(function (data) {
          if (data.status == 500) {
            self.logMsg = "Internal server error";
          } else if (data.status == 404) {
            self.logMsg = "Server Not found";
          } else if (data.status == 401 || data.status == 814) {
            self.logMsg = "Invalid Credentials. Please try again.";
          } else {
            self.logMsg = "Internal server error";
          }
          self.loginRequestSent = false;
          self.showThrobber = false;
          self.isLoginError = true;
          self.isDisabled = false;
        });
    } else if (
      InsightsInitService.autheticationProtocol ==
      AutheticationProtocol.JWT.toString()
    ) {
      setTimeout(() => {
        this.loginService.loginSSOJWT(this.password);
      }, 1000);
    }
  }

  deleteAllPreviousCookies(): void {
    console.log("in delete all cookies ");
    let allCookies = this.cookieService.getAll();
    let grafana_session_cookie = this.cookieService.get("grafana_session");
    if (grafana_session_cookie != undefined || grafana_session_cookie != "") {
      this.cookieService.set("grafana_session", undefined);
    }
    this.dataShare.deleteAllPreviousCookies();
    this.cookieService.deleteAll("/");
    for (let key of Object.keys(allCookies)) {
      this.cookieService.delete(key, "/");
    }
    console.log(" deleted all cookies ");
  }
}
