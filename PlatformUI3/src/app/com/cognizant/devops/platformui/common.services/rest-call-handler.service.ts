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
import { Observable } from 'rxjs'
import { RestAPIurlService } from '@insights/common/rest-apiurl.service'
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { DataSharedService } from '@insights/common/data-shared-service';

@Injectable()
export class RestCallHandlerService {
  asyncResult: any;
  constructor(private http: HttpClient, private restAPIUrlService: RestAPIurlService,
    private dataShare: DataSharedService) {

  }

  public async get(url: string, requestParams?: Object, additionalheaders?: Object): Promise<any> {

    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var dataresponse;
      var authToken = this.dataShare.getAuthorizationToken();
      const headers = new HttpHeaders()
        .set("Authorization", authToken);
      var restCallUrl = this.constructGetUrl(url, requestParams);
      this.asyncResult = await this.http.get(restCallUrl, { headers }).toPromise();
      //console.log(this.asyncResult)
      return this.asyncResult;
    }
    else {
      console.log("SessionTimedout")
    }
  }

  public post(url: string, requestParams?: Object, additionalheaders?: Object): Observable<any> {
    var isSessionExpired = false
    if (url != "USER_AUTHNTICATE") {
      isSessionExpired = this.dataShare.validateSession();
    }
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      //console.log(restCallUrl);
      var dataresponse;
      var headers;
      var authToken = this.dataShare.getAuthorizationToken();
      var defaultHeader = {
        'Authorization': authToken
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
        transformRequest: function (data) {
          if (data && Object.keys(data).length !== 0 && data.constructor == Object) {
            var postParameter = '';
            for (var key in data) {
              //console.log(key+""+ requestParams[key]);
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
      //console.log(allData)
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
      //console.log(restCallUrl);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();
      let params = new HttpParams();
      for (var key in requestParams) {
        // console.log(key + " " + requestParams[key]);
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }
      headers = new HttpHeaders();
      headers = headers.set('Authorization', authToken);
      for (var key in additionalheaders) {
        //console.log(key + " " + additionalheaders[key]);
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      //console.log(httpOptions);
      dataresponse = this.http.post(restCallUrl, {}, httpOptions);
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
      var dataresponse = this.http.post(restCallUrl, fd, {
        headers: {
          'Authorization': authToken
        },
      })
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
      var dataresponse = this.http.post(restCallUrl, fd, {
        headers: {
          'Authorization': authToken
        },
      })
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
  }


  public postFormDataWithParameter(url: string, data: any, requestParams?: Object, additionalheaders?: Object): Observable<any> {
    var isSessionExpired = this.dataShare.validateSession();
    if (!isSessionExpired) {
      var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);
      //console.log(restCallUrl);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();

      let params = new HttpParams();

      for (var key in requestParams) {
        // console.log(key + " " + requestParams[key]);
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }

      headers = new HttpHeaders();
      headers = headers.set('Authorization', authToken);

      for (var key in additionalheaders) {
        //console.log(key + " " + additionalheaders[key]);
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      //console.log(httpOptions);
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
      //console.log(restCallUrl);
      var dataresponse;
      let headers;
      var authToken = this.dataShare.getAuthorizationToken();

      let params = new HttpParams();

      for (var key in requestParams) {
        // console.log(key + " " + requestParams[key]);
        if (requestParams.hasOwnProperty(key)) {
          params = params.set(key, requestParams[key]);
        }
      }

      headers = new HttpHeaders();
      headers = headers.set('Authorization', authToken);

      for (var key in additionalheaders) {
        //console.log(key + " " + additionalheaders[key]);
        if (headers.hasOwnProperty(key)) {
          headers = headers.set(key, additionalheaders[key]);
        }
      }
      var httpOptions = {
        headers: headers,
        params: params
      }
      //console.log(httpOptions);
      dataresponse = this.http.post(restCallUrl, data, httpOptions);
      return dataresponse;
    } else {
      console.log("Session Expire")
    }
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
}
