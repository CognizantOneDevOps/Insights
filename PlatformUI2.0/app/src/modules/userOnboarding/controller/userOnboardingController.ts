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
            self.getApplicationDetail();
            self.userIframeStyle = 'width:100%; height:500px;';
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
        showAddApplication: boolean = false;
        addNewApplicationName: string = "";
        showAccessGroupAddedMessage: boolean = false;
        accessGroupMessageStatus: string = "";

        getApplicationDetail() {
            var self = this;
            self.adminOrgDataArray = [];

            self.userOnboardingService.getCurrentUserOrgs().
                then(function (orgData) {
                    var orgDataArray = orgData.data;
                    self.getUserAdminOrgs(orgDataArray);
                    self.authenticationService.getGrafanaCurrentOrgAndRole()
                        .then(function (data) {
                            self.getCurrentOrgName(data, orgDataArray);
                        });
                });
        }

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
            var grafanaVersion = this.homeController.grafanaVersion;
            /*self.userListUrl = self.$sce.trustAsResourceUrl('http://localhost:3000/dashboard/script/CustomiSight.js?url=http://localhost:3000/org/users');*/
            self.restEndpointService.getGrafanaHost1().then(function (response) {
                var grafanaEndPoint = response.grafanaEndPoint;
                //console.log(grafanaEndPoint);
                if (grafanaVersion >= 5) {
                    self.userListUrl = self.$sce.trustAsResourceUrl(grafanaEndPoint + '/dashboard/script/iSight.js?url=' + grafanaEndPoint + '/org/users');
                } else {
                    self.userListUrl = self.$sce.trustAsResourceUrl(grafanaEndPoint + '/dashboard/script/CustomiSight.js?url=' + grafanaEndPoint + '/org/users');
                }
            });
            //console.log(this.userListUrl);
        }

        showAddApplicationBox(): void {
            //this.showAccessGroupAddedMessage = false;
            if (this.showAddApplication === false) {
                this.showAddApplication = true;
            }
            else {
                this.showAddApplication = false;
            }
        }

        addApplication(params, addedApplicationName): void {
            var self = this;
            var statusObject = {
                'status': false
            }
            if (addedApplicationName !== "") {
                self.$mdDialog.show({
                    controller: ShowTemplateApplicationAddConformDialogController,
                    controllerAs: 'showTemplateApplicationAddConformDialogController',
                    templateUrl: './dist/modules/applicationManagement/view/conformApplicationAddDialogViewTemplate.tmp.html',
                    parent: angular.element(document.body),
                    targetEvent: params,
                    preserveScope: true,
                    clickOutsideToClose: true,
                    locals: {
                        statusObject: statusObject,
                        addedApplicationName: addedApplicationName,
                    },
                    bindToController: true,
                    onRemoving: function () { self.addApplicationConfirmation(statusObject.status) }
                })
            } else if (addedApplicationName === "") {
                self.accessGroupMessageStatus = "Error in adding access group";
                self.showAccessGroupAddedMessage = true;
                setTimeout(function () {
                    self.showAccessGroupAddedMessage = false;
                    self.accessGroupMessageStatus = "";
                }, 1000);
            }
        }

        addApplicationConfirmation(status): void {
            var self = this;
            if (status === true) {
                this.roleService
                    .createOrg(self.addNewApplicationName)
                    .then(function (data) {
                        self.showAddApplication = false;
                        self.getApplicationDetail();
                        self.accessGroupMessageStatus = "New access group added successfully";
                        self.showAccessGroupAddedMessage = true;
                        self.addNewApplicationName = "";
                        setTimeout(function () {
                            self.showAccessGroupAddedMessage = false;
                            self.accessGroupMessageStatus = "";
                        }, 1000);
                    });
            }
        }

    }
}