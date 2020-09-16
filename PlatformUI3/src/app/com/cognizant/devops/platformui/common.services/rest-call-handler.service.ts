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
import { Observable, throwError } from 'rxjs'
import { RestAPIurlService } from '@insights/common/rest-apiurl.service'
import { HttpClient, HttpHeaders, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';
import 'rxjs/Rx';
import { MessageDialogService } from '@insights/app/modules/application-dialog/message-dialog-service';
import { Router, NavigationExtras } from '@angular/router';
import { AutheticationProtocol, RequestHeader } from '@insights/common/insights-enum';

@Injectable()
export class RestCallHandlerService {
  asyncResult: any;
  constructor(private http: HttpClient, private restAPIUrlService: RestAPIurlService,
    private dataShare: DataSharedService, public messageDialog: MessageDialogService,
    private router: Router) {

  }

  public async get(url: string, requestParams?: Object, additionalheaders?: Object): Promise<any> {

    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      const headers = new HttpHeaders()
        .set(RequestHeader.AUTH_TOKEN, authToken)
        .set(RequestHeader.WEBAUTHUSER, webAuthToken);
      var restCallUrl = this.constructGetUrl(url, requestParams);
      this.asyncResult = await this.http.get(restCallUrl, { headers, withCredentials: true })
        .toPromise().catch(err => { this.handleTokenError(err) });
      return this.asyncResult;
    }
    else {
      console.log("SessionTimedout")
    }
  }

  public async getSSO(url: string, requestParams?: Object, additionalheaders?: Object): Promise<any> {
    var authToken = this.dataShare.getAuthorizationToken();
    var webAuthToken = this.dataShare.getWebAuthToken();
    const headers = new HttpHeaders()
      .set(RequestHeader.AUTH_TOKEN, authToken)
    var restCallUrl = this.constructGetUrl(url, requestParams);
    console.log("restCallUrl " + restCallUrl);
    this.asyncResult = await this.http.get(restCallUrl, { headers, withCredentials: true }).toPromise();
    return this.asyncResult;
  }

  public getSSOJWT(restCallUrl: string, authToken2: string): Observable<any> {
    var authToken = this.dataShare.getAuthorizationToken();
    var webAuthToken = this.dataShare.getWebAuthToken();
    const headers = new HttpHeaders()
      .set(RequestHeader.AUTH_TOKEN, authToken2)
    console.log("restCallUrl " + restCallUrl);
    var asyncResult2 = this.http.get(restCallUrl, { headers, withCredentials: true, observe: 'response' });//.toPromise()
    console.log(asyncResult2)
    return asyncResult2;
  }

  public async getSSOLogout(url: string, requestParams?: Object, additionalheaders?: Object): Promise<any> {
    var authToken = this.dataShare.getAuthorizationToken();
    var webAuthToken = this.dataShare.getWebAuthToken();
    const headers = new HttpHeaders()
      .set(RequestHeader.AUTH_TOKEN, authToken)
      .set(RequestHeader.WEBAUTHUSER, webAuthToken);
    var restCallUrl = this.constructGetUrl(url, requestParams);
    console.log("restCallUrl " + restCallUrl);
    this.asyncResult = await this.http.get(restCallUrl, { headers, withCredentials: true }).toPromise();
    return this.asyncResult;
  }

  public post(url: string, requestParams?: Object, additionalheaders?: Object): Observable<any> {
    var isSessionExpired = false
    if (url != "USER_AUTHNTICATE") {
      isSessionExpired = this.dataShare.validateSession();
    }
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var dataresponse;
      var headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      var defaultHeader = {
        'Authorization': authToken,
        'user': webAuthToken
      };
      if (this.checkValidObject(additionalheaders)) {
        headers = this.extend(defaultHeader, additionalheaders);
      } else {
        headers = defaultHeader;
      }
      headers = defaultHeader;
      var allData = {
        method: 'POST',
        headers: headers,
        withCredentials: true,
        transformRequest: function (data) {
          if (data && Object.keys(data).length !== 0 && data.constructor == Object) {
            var postParameter = '';
            for (var key in data) {
              if (data.hasOwnProperty(key)) {
                postParameter = postParameter.concat(key + '=' + requestParams[key] + '&');
              }
            }
            postParameter = postParameter.slice(0, -1);
            return postParameter;
          }
          return;
        }
      }
      dataresponse = this.http.post(restCallUrl, {}, allData);
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }

  public postWithParameter(url: string, requestParams?: Object, additionalheaders?: Object): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      let params = new HttpParams();
      for (var key in requestParams) {
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }
      headers = new HttpHeaders();
      headers = headers.set(RequestHeader.AUTH_TOKEN, authToken);
      headers = headers.set(RequestHeader.WEBAUTHUSER, webAuthToken);

      for (var key in additionalheaders) {
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      dataresponse = this.http.post(restCallUrl, {}, httpOptions).catch((e: any) => Observable.throw(this.handleTokenError(e)));
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }

  public postWithImage(url: string, imageFile: any): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var fd = new FormData();
      fd.append("file", imageFile);
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      var dataresponse = this.http.post(restCallUrl, fd, {
        headers: {
          'Authorization': authToken,
          'user': webAuthToken
        },
      }).catch((e: any) => Observable.throw(this.handleTokenError(e)))
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }

  public postFormData(url: string, fd: any): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      var dataresponse = this.http.post(restCallUrl, fd, {
        headers: {
          'Authorization': authToken,
          'user': webAuthToken
        },
      }).catch((e: any) => Observable.throw(this.handleTokenError(e)))
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }


  public postFormDataWithParameter(url: string, data: any, requestParams?: Object, additionalheaders?: Object): Observable<any> {

    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      let params = new HttpParams();
      for (var key in requestParams) {
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }

      headers = new HttpHeaders();
      headers = headers.set(RequestHeader.AUTH_TOKEN, authToken);
      headers = headers.set(RequestHeader.WEBAUTHUSER, webAuthToken);

      for (var key in additionalheaders) {
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      dataresponse = this.http.post(restCallUrl, data, httpOptions);
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }

  public postWithData(url: string, data: String, requestParams?: Object, additionalheaders?: Object): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      let params = new HttpParams();

      for (var key in requestParams) {
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }

      headers = new HttpHeaders();
      headers = headers.set(RequestHeader.AUTH_TOKEN, authToken);
      headers = headers.set(RequestHeader.WEBAUTHUSER, webAuthToken);
      for (var key in additionalheaders) {
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      dataresponse = this.http.post(restCallUrl, data, httpOptions);
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }

  public postWithAgentData(url: string, data: String, requestParams?: Object, additionalheaders?: Object): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var dataresponse = null;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      let params = new HttpParams();

      for (var key in requestParams) {
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }

      headers = new HttpHeaders();
      headers = headers.set(RequestHeader.AUTH_TOKEN, authToken);
      headers = headers.set(RequestHeader.WEBAUTHUSER, webAuthToken);
      for (var key in additionalheaders) {
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      dataresponse = this.http.post(restCallUrl, data, httpOptions)
        .catch((e: HttpErrorResponse) => {
          this.handleTokenError(e)
          throw e.error.message
        });
      return dataresponse;
    } else {
      console.log("Session Expired")
    }
  }

  handleTokenError(error: HttpErrorResponse) {
    console.error("Inside handleTokenError in rest-call-Handle.service " + error.url)
    console.error(error.status);
    if (error.status === 810 || error.status === 811 || error.status === 812 || error.status === 814) {
      console.error(error);
      let navigationExtras: NavigationExtras = {
        skipLocationChange: true,
        queryParams: {
          "message": error.error
        }
      };
      this.router.navigate(['/logout/' + error.status], navigationExtras);
    } else {
      console.error('An error occurred', error);
    }
  }

  private handleError(error: any) {
    console.error('An error occurred', error);
    return Observable.of(false);
  }
  private extend(obj: Object, src: Object) {
    for (var key in src) {
      if (src.hasOwnProperty(key)) obj[key] = src[key];
    }
    return obj;
  }

  private checkValidObject(obj: Object) {
    if (obj != null && obj.constructor == Object && Object.keys(obj).length !== 0) {
      return true;
    }
    return false;
  }


  private constructGetUrl(url: string, requestParams: Object) {
    var selectedUrl = this.restAPIUrlService.getRestCallUrl(url); //url
    if (this.checkValidObject(requestParams)) {
      selectedUrl = selectedUrl.concat('?');
      for (var key in requestParams) {
        if (requestParams.hasOwnProperty(key)) {
          selectedUrl = selectedUrl.concat(key + '=' + requestParams[key] + '&');
        }
      }
      selectedUrl = selectedUrl.slice(0, -1);
    }
    return selectedUrl;
  }

  public getJSON(url): Promise<any> {
    return this.http.get(url).toPromise()
  }

  public getJSONUsingObservable(url): Observable<any> {
    return this.http.get(url)
  }

  public postWithPDFData(url: string, data: String, requestParams?: Object, additionalheaders?: Object, responseType?: Object): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var webAuthToken = this.dataShare.getWebAuthToken();
      let responseTypeValue = responseType['responseType'];
      let params = new HttpParams();
      for (var key in requestParams) {
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }

      headers = new HttpHeaders();
      headers = headers.set(RequestHeader.AUTH_TOKEN, authToken);
      headers = headers.set(RequestHeader.WEBAUTHUSER, webAuthToken);
      for (var key in additionalheaders) {
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params,
        responseType: responseTypeValue
      }
      dataresponse = this.http.post(restCallUrl, data, httpOptions);
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }
}
