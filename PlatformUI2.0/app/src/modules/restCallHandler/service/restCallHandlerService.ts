/*******************************************************************************
 * Copyright 2017 Cognizant Technology Solutions
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

/// <reference path="../../../_all.ts" />
   
module ISightApp {
    export interface IRestCallHandlerService {
        
        get(url: string, requestParams?: Object, additionalheaders?: Object): ng.IPromise<any>;
        post(url: string, requestParams?: Object, additionalheaders?: Object): ng.IPromise<any>;
    }

    export class RestCallHandlerService implements IRestCallHandlerService {
        static $inject = ['$cookies', '$resource', 'restEndpointService', 'restAPIUrlService'];
        constructor(private $cookies, private $resource, private restEndpointService, private restAPIUrlService:IRestAPIUrlService) { }
        
        get(url: string, requestParams?: Object, additionalheaders?: Object): ng.IPromise<any> {
            var headers;
            var authToken = this.$cookies.get('Authorization');
            var defaultHeader = {
                                    'Authorization': authToken
                                };
            if (this.checkValidObject(additionalheaders)) {
                headers = this.extend(defaultHeader,additionalheaders);
            }else {
                headers = defaultHeader;
            }
            var restcallUrl = this.constructGetUrl(url,requestParams);
            var resource = this.$resource(restcallUrl,
                {},
                {
                    allData: {
                        method: 'GET',
                        headers: headers
                    }
                });
            return resource.allData().$promise;
        }
      

        post(url: string, requestParams?: Object, additionalheaders?: Object) {

            var restCallUrl = this.restAPIUrlService.getRestCallUrl(url);

            var headers;
            var authToken = this.$cookies.get('Authorization');
            var defaultHeader = {
                                    'Authorization': authToken
                                };
            
            if (this.checkValidObject(additionalheaders)) {
                headers = this.extend(defaultHeader,additionalheaders);
            }else {
                headers = defaultHeader;
            }
            var resource = this.$resource(restCallUrl,
                {},
                {
                    allData: {
                        method: 'POST',
                        headers: headers,
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
                });
            return resource.allData(requestParams).$promise;

        }

        extend(obj : Object, src: Object) {
            for (var key in src) {
                if (src.hasOwnProperty(key)) obj[key] = src[key];
            }
            return obj;
        }

        checkValidObject(obj : Object) {
            if(typeof(obj) != 'undefined' && obj != null && obj.constructor == Object && Object.keys(obj).length !== 0){
               return true;
            }
            return false;
        }

        constructGetUrl(url : string, requestParams : Object) {
            var selectedUrl =  this.restAPIUrlService.getRestCallUrl(url);
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



    }
}
