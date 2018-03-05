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
    export class HomePageController {
        static $inject = ['$location', '$window', '$cookies', '$rootScope', 'authenticationService', 'restEndpointService', '$sce', '$timeout', '$mdDialog', 'aboutService', '$resource','restAPIUrlService'];
        constructor(private $location, private $window, private $cookies, private $rootScope, private authenticationService: IAuthenticationService, private restEndpointService: IRestEndpointService, private $sce, private $timeout, private $mdDialog, private aboutService: IAboutService, private $resource,private restAPIUrlService:IRestAPIUrlService) {
            var self = this;
            this.authenticationService.validateSession();
            this.isValidUser = true;
            self.iframeStyle = 'width:100%; height:1600px;';
            var receiveMessage = function (evt) {
                var height = parseInt(evt.data);
                if (!isNaN(height)) {
                    self.iframeStyle = 'width:100%; height:' + (evt.data + 20) + 'px';
                    $timeout(0);
                }
            }
            window.addEventListener('message', receiveMessage, false);

            self.authenticationService.getGrafanaCurrentOrgAndRole()
                .then(function (data) {
                    if (data.grafanaCurrentOrgRole === 'Admin') {
                        self.showAdminTab = true;
                    } else {
                        self.showAdminTab = false;
                    }
                     self.$cookies.put('grafanaRole', data.grafanaCurrentOrgRole);
                    self.$cookies.put('grafanaOrg', data.grafanaCurrentOrg);
                    if(data.userName != undefined){
                        self.userName = data.userName.replace(/['"]+/g, '');
                    }
                    self.userRole = data.grafanaCurrentOrgRole;
                    self.userCurrentOrg = data.grafanaCurrentOrg;
                    self.authenticationService.getCurrentUserOrgs()
                    .then(function (orgdata){
                         self.userCurrentOrgName = orgdata.data.filter(function(i) {
                            return i.orgId == self.userCurrentOrg;
                        });
                    });
                    let location = self.$location;
                    let uiConfigJsonUrl: string = location.absUrl().replace(location.path(), "");
                    if (uiConfigJsonUrl.length > uiConfigJsonUrl.lastIndexOf('/')) {
                        uiConfigJsonUrl = uiConfigJsonUrl.substr(0, uiConfigJsonUrl.lastIndexOf('/'));
                    }
                    uiConfigJsonUrl += "/uiConfig.json"
                    var configResource = self.$resource(uiConfigJsonUrl);
                    var data = configResource.get().$promise.then(function (data) {
                    self.showInsightsTab = data.showInsightsTab;
                    if(self.showAdminTab){
                        if(self.showInsightsTab){
                            self.selectedIndex = 1;
                           self.templateName = 'insights';
                        }else{
                             self.selectedIndex = 1;
                            self.templateName = 'dashboards';
                
                        }
                    }else{
                        if(self.showInsightsTab){
                            self.selectedIndex = 0;
                            self.templateName = 'insights';
                        }else{
                             self.selectedIndex = 0;
                             self.templateName = 'dashboards';
                
                        }
                    }
                    });
                   
                });

               // self.selectedIndex = 2;
                //self.templateName = 'dashboards';
            
             var restCallUrl = restAPIUrlService.getRestCallUrl("GET_LOGO_IMAGE");
                var resource = this.$resource(restCallUrl,
                {},
                {
                    allData: {
                        method: 'GET'
                    }
                });
                
                resource.allData().$promise.then(function(data){
                    if(data.data.encodedString && data.data.encodedString.length > 0){
                        self.imageSrc = 'data:image/jpg;base64,' + data.data.encodedString;                    
                    }else{
                        self.showDefaultImg = true;
                    }
                   
                });
              
        }
        isValidUser: boolean = false;
        /*start for common code */
        isTabSeleted: boolean = false;
        imageurl1: string = "dist/icons/svg/landingPage/Admin_icon_selected.svg";
        imageurl2: string = "dist/icons/svg/landingPage/Dashboard_icon_normal.svg";
        imageurl3: string = "dist/icons/svg/landingPage/Healthcheck_icon_normal.svg";
        imageurl4: string = "dist/icons/svg/landingPage/Help_icon_normal.svg";
        imageurl5: string = "dist/icons/svg/landingPage/playlist_normal.svg";
        imageurl6: string = "dist/icons/svg/landingPage/logout_normal.svg";
        imageurl7: string = "dist/icons/svg/landingPage/magnifying_glass.svg";
        aboutImg: string = "dist/icons/svg/landingPage/about_normal.svg";
        playListUrl: String = '';
        templateName: string = 'insights';
        showInsightsTab: boolean;
        shouldReload: boolean;
        selectedToolName: string;
        selectedToolCategory: string;
        footerMinHeight: string = 'min-height:' + (window.innerHeight - 146) + 'px;';
        footerHeight: string = '';
        mainContentMinHeight: string = 'min-height:' + (window.innerHeight - 146 - 96) + 'px';
        mainContentMinHeightWoSbTab: string = 'min-height:' + (window.innerHeight - 146 - 48) + 'px';
        iframeStyle = 'width:100%; height:1600px;border:0';
        iframeWidth = window.innerWidth - 20;
        iframeHeight = window.innerHeight;
        aboutMeContent = {};
        showAdminTab: boolean = false;
        showThrobber: boolean;
        selectDashboard: boolean = false;
        selectedIndex: Number;
        selectedDashboardUrl: string = '';
        userName: string = '';
        userRole: string = '';
        userCurrentOrg: string = '';
        userCurrentOrgName: string = '';
		imageSrc: string = "dist/icons/svg/landingPage/Cognizant_Insights.svg";
        showDefaultImg: boolean = false;

        public redirect(iconId: string): void {
            if (iconId == 'dashboard') {
                this.$location.path('/InSights/dashboard/');
            } else if (iconId == 'settings') {
                this.$location.path('/InSights/toolsConfig');
            } else if (iconId == 'graphview') {
                this.$location.path('/InSights/explore');
            } else if (iconId == 'userview') {
                this.$location.path('/InSights/roles');
            } else if (iconId == 'prjtmapping') {
                this.$location.path('/InSights/onboardProject');
            } else if (iconId == 'healthcheck') {
                this.$location.path('/InSights/agent');
            }
        }

        public showAboutInsights() {
            let self = this;
            this.aboutService.loadDetails().then(function (data) {
                console.log(data);
                self.aboutMeContent['data'] = data
                self.$mdDialog.show({
                    controller: AboutDialogeController,
                    controllerAs: 'aboutDialogeController',
                    templateUrl: './dist/modules/homepage/view/aboutInsights.tmpl.html',
                    parent: angular.element(document.body),
                    preserveScope: true,
                    clickOutsideToClose: true,
                    bindToController: true,
                    locals: {
                        data: data
                    }
                })
            });
        }

        public closeDialog() {
            this.$mdDialog.cancel();
            this.$mdDialog.hide();
        }

        selectAct(tabName: string): void {
            this.authenticationService.validateSession();
            this.templateName = tabName;

             if ('playlist' === tabName) {
                var self = this;
                self.restEndpointService.getGrafanaHost1().then(function(response){
                    var grafanaEndPoint =  response.grafanaEndPoint;
                    self.playListUrl = self.$sce.trustAsResourceUrl(grafanaEndPoint + '/dashboard/script/iSight.js?url=' + grafanaEndPoint + '/playlists');
                });  
               // this.playListUrl = this.$sce.trustAsResourceUrl(self.restEndpointService.getGrafanaHost()  + '/dashboard/script/iSight.js?url=' + self.restEndpointService.getGrafanaHost() + '/playlists');
               //this.playListUrl = self.restEndpointService.getGrafanaHost() + '/dashboard/script/iSight.js?url=' + self.restEndpointService.getGrafanaHost() + '/playlists';
            } else {
                this.playListUrl = '';
            }
        }

        addSelectedImage(selectedTab: string): void {
            var self = this;
            if (selectedTab == 'Admin') {
                self.imageurl1 = "dist/icons/svg/landingPage/Admin_icon_selected.svg";
            }
            else if (selectedTab == 'Dashboards') {
                self.imageurl2 = "dist/icons/svg/landingPage/Dashboard_icon_selected.svg";
            }
            else if (selectedTab == 'HealthCheck') {
                self.imageurl3 = "dist/icons/svg/landingPage/Healthcheck_icon_selected.svg";
            }
            else if (selectedTab == 'Help') {
                self.imageurl4 = "dist/icons/svg/landingPage/Help_icon_selected.svg";
            }
            else if (selectedTab == 'Playlists') {
                self.imageurl5 = "dist/icons/svg/landingPage/playlist_selected.svg";
            }
        }

        removeSelectedImage(selectedTab: string): void {
            var self = this;
            if (selectedTab == 'Admin') {
                self.imageurl1 = "dist/icons/svg/landingPage/Admin_icon_normal.svg";
            }
            else if (selectedTab == 'Dashboards') {

                self.imageurl2 = "dist/icons/svg/landingPage/Dashboard_icon_normal.svg";
            }
            else if (selectedTab == 'HealthCheck') {
                self.imageurl3 = "dist/icons/svg/landingPage/Healthcheck_icon_normal.svg";

            }
            else if (selectedTab == 'Help') {
                self.imageurl4 = "dist/icons/svg/landingPage/Help_icon_normal.svg";
            }
            else if (selectedTab == 'Playlists') {
                self.imageurl5 = "dist/icons/svg/landingPage/playlist_normal.svg";
            }
        }

        addHoverImage(hoverTab: string): void {
            if (hoverTab === "about") {
                this.aboutImg = "dist/icons/svg/landingPage/about_selected.svg";
            }
            else if (hoverTab === 'Help') {
                this.imageurl4 = "dist/icons/svg/landingPage/Help_icon_selected.svg";
            }
            else if (hoverTab === 'Logout') {
                this.imageurl6 = "dist/icons/svg/landingPage/logout_selected.svg";
            }
        }

        removeHoverImage(hoverTab: string): void {
            var hover = this;
            if (hoverTab === 'about') {
                this.aboutImg = "dist/icons/svg/landingPage/about_normal.svg";
            }
            else if (hoverTab === 'Help') {
                this.imageurl4 = "dist/icons/svg/landingPage/Help_icon_normal.svg";
            } else if (hoverTab === 'Logout') {
                this.imageurl6 = "dist/icons/svg/landingPage/logout_normal.svg";
            }
        }

        logout(): void {
            //this.$cookies.remove('Authorization');
            //this.$cookies.remove('grafanaOrg');
            var self = this;
            this.authenticationService.logout()
                .then(function (data) {
                    //console.log(data);
                           	var uniqueString = "grfanaLoginIframe";
                            var iframe = document.createElement("iframe");
                            iframe.id = uniqueString;
                            document.body.appendChild(iframe);
                            iframe.style.display = "none";
                            iframe.contentWindow.name = uniqueString;
                            // construct a form with hidden inputs, targeting the iframe
                            var form = document.createElement("form");
                            form.target = uniqueString;
                            self.restEndpointService.getGrafanaHost1().then(function(response){
                                form.action = response.grafanaEndPoint + "/logout";
                               // console.log("form action "+form.action);
                                form.method = "GET";
                                document.body.appendChild(form);
                                form.submit();
                            });
									
                });
            var cookieVal = this.$cookies.getAll();
            for (var key in cookieVal) {
                cookieVal[key] = '';
                this.$cookies.put(key, cookieVal[key]);
                this.$cookies.remove(key);
            }
            this.$location.path('/iSight/login');
        }
    }
}
