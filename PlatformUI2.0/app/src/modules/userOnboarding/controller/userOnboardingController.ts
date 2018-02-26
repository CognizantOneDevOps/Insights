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
    export class UserOnboardingController {
        static $inject = ['$route', '$location', '$window', '$mdDialog', 'userOnboardingService', 'roleService',
            'restEndpointService', '$sce', '$timeout', '$rootScope', 'authenticationService', '$cookies'];
        constructor(private $route, private $location, private $window, private $mdDialog, private userOnboardingService: IUserOnboardingService, private roleService: IRoleService,
            private restEndpointService: IRestEndpointService, private $sce,
            private $timeout, private $rootScope, private authenticationService: IAuthenticationService, private $cookies) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            self.getHost();
            self.userOnboardingService.getCurrentUserOrgs().
                then(function (orgData) {
                    var orgDataArray = orgData.data;
                    self.getUserAdminOrgs(orgDataArray);
                    self.authenticationService.getGrafanaCurrentOrgAndRole()
                        .then(function (data) {
                            self.getCurrentOrgName(data, orgDataArray);
                        });
                });
            self.userIframeStyle = 'width:100%; height:1600px;';
            var receiveMessage = function (evt) {
                var height = parseInt(evt.data);
                if (!isNaN(height)) {
                    self.userIframeStyle = 'width:100%; height:' + (evt.data + 20) + 'px !important';
                    $timeout(0);
                }
            }
            window.addEventListener('message', receiveMessage, false);
        }
        userIframeStyle: String;
        homeController: HomePageController;
        userListUrl: String = '';
        iframeWidth = window.innerWidth;
        iframeHeight = window.innerHeight;
        iframeStyle: String = '';
        allOrgDataArray = [];
        adminOrgDataArray = [];
        userCurrentOrgName: String = '';
        showSwitchOptions: boolean = false;

        getUserAdminOrgs(orgDataArray) {
            var self = this;
            self.allOrgDataArray = orgDataArray
            //console.log(self.allOrgDataArray);
            for (var org in self.allOrgDataArray) {
                if ((self.allOrgDataArray[org].role) === 'Admin') {
                    self.adminOrgDataArray.push(self.allOrgDataArray[org]);
                }
                //console.log(self.adminOrgDataArray);
            }
        }

        getCurrentOrgName(currentOrgData, orgDataArray) {
            var self = this;
            var userCurrentOrgData = currentOrgData;
            var allOrgData = orgDataArray;
            var currentOrgId = currentOrgData.grafanaCurrentOrg;
            for (var i in allOrgData) {
                if (allOrgData[i].orgId == currentOrgId) {
                    self.userCurrentOrgName = allOrgData[i].name;
                }
            }
            //console.log(self.userCurrentOrgName);
        }

        switchAccessGroup(orgId) {
            var self = this;
            self.userOnboardingService.switchUserOrg(orgId)
                .then(function (selOrgStatus) {
                });
            self.authenticationService.getGrafanaCurrentOrgAndRole()
                .then(function (data) {
                    self.$cookies.put('grafanaRole', data.grafanaCurrentOrgRole);
                    self.$cookies.put('grafanaOrg', data.grafanaCurrentOrg);
                });

            self.refreshIframe();
        }

        refreshIframe() {
            var myIframe = (<HTMLIFrameElement>document.getElementById('iSightIframe'));
            setTimeout(function () {
                myIframe.src = myIframe.src;
            }, 500);
        }

        /*showAccessGroupOptions() {
            var self = this;
            self.showSwitchOptions = true;
        }*/

        getHost() {
            var self = this;
            /*self.userListUrl = self.$sce.trustAsResourceUrl('http://localhost:3000/dashboard/script/CustomiSight.js?url=http://localhost:3000/org/users');*/
            self.restEndpointService.getGrafanaHost1().then(function (response) {
                var grafanaEndPoint = response.grafanaEndPoint;
                //console.log(grafanaEndPoint);
                self.userListUrl = self.$sce.trustAsResourceUrl(grafanaEndPoint + '/dashboard/script/CustomiSight.js?url=' + grafanaEndPoint + '/org/users');
            });
            //console.log(this.userListUrl);
        }
    }
}