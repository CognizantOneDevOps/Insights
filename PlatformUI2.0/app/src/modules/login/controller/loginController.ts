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
    export class LoginController {
        static $inject = ['loginService', 'restEndpointService', '$location', '$document', '$cookies'];
        constructor(private loginService: ILoginService, private restEndpointService: IRestEndpointService, private $location, private $document, private $cookies) {


        }
        self;
        logMsg: string;
        isLoginError: boolean;
        isDisabled: boolean;
        showThrobber: boolean = false;
        cookies: string;
        usernameVal: string;
        passwordVal: string;
        

        userAuthentication(username: string, password: string): void {
            if (username === '' || username === undefined || password === '' || password === undefined) {
                this.logMsg = '';
            } else {
                var self = this;
                this.isDisabled = true;
                this.showThrobber = true;
                var token = 'Basic ' + btoa(username + ":" + password);
                this.loginService.loginUserAuthentication(username, password)
                    .then(function (data) {
                        var grafcookies = data.data;
                        if (data.status === 'SUCCESS') {
                            self.showThrobber = false;
                            var date = new Date();
                            var minutes = 30;
                            date.setTime(date.getTime() + (minutes * 60 * 1000));
                            self.$cookies.put('Authorization', token, { expires: date });
                            self.$cookies.put('DashboardSessionExpiration', new Date(new Date().getTime() + 86400 * 1000));
                            this.cookies = "";
                            for (var key in grafcookies) {
                                //this.cookies += key+ '=' +grafcookies[key];
                                self.$cookies.put(key, grafcookies[key], { expires: date });
                            }
                            //self.$cookies.put('grafanaCookies',{'grafanaOrg':grafcookies.grafanaOrg,'grafanaRole':grafcookies.grafanaRole,'grafana_remember':grafcookies.grafana_remember, 'grafana_sess':grafcookies.grafana_sess, 'grafana_user':grafcookies.grafana_user});
                            //self.$cookies.put('cookies', this.cookies);
                            self.$location.path('/InSights/home');
                            var uniqueString = "grfanaLoginIframe";
                            var iframe = document.createElement("iframe");
                            iframe.id = uniqueString;
                            document.body.appendChild(iframe);
                            iframe.style.display = "none";
                            iframe.contentWindow.name = uniqueString;
                            // construct a form with hidden inputs, targeting the iframe
                            var form = document.createElement("form");
                            form.target = uniqueString;
                            //form.action = "http://localhost:3000/login";
                            
                            // repeat for each parametergetGrafanaHost()
                            var input = document.createElement("input");
                            input.type = "hidden";
                            input.name = "user";
                            input.value = username;
                            form.appendChild(input);

                            var input1 = document.createElement("input");
                            input1.type = "hidden";
                            input1.name = "password";
                            input1.value = password;
                            form.appendChild(input1);

                            var input2 = document.createElement("input");
                            input2.type = "hidden";
                            input2.name = "email";
                            input2.value = '';
                            form.appendChild(input2);
                            self.restEndpointService.getGrafanaHost1().then(function(response){
                                form.action = response.grafanaEndPoint + "/login";
                                console.log("form action "+form.action);
                                form.method = "POST";
                                document.body.appendChild(form);
                                form.submit();
                            });
                        } else if (data.error.message) {
                            self.showThrobber = false;
                            self.isLoginError = true;
                            self.logMsg = data.error.message;
                            self.isDisabled = false;
                        }
                    });
            }
        }
    }
}
