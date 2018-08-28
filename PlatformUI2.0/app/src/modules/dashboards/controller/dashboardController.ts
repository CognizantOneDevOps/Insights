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
    export class DashboardController {
        static $inject = ['elasticSearchService', 'dashboardService', '$sce',
            '$mdSidenav', '$location', '$timeout', 'restEndpointService', '$rootScope', '$cookies', 'authenticationService', '$resource'];

        constructor(
            private elasticSearchService: IElasticSearchService,
            private dashboardService: IDashboardService,
            private $sce,
            private $mdSidenav,
            private $location,
            private $timeout,
            private restEndpointService: IRestEndpointService,
            private $rootScope,
            private $cookies,
            private authenticationService: IAuthenticationService,
            private $resource
        ) {
            var self = this;
            $rootScope.$watch('refreshDashboard', function () {
                self.getDashboards();
            });

            this.loadorganizations();
            self.iframeStyle = 'width:100%; height:500px;';
            var receiveMessage = function (evt) {
                var height = parseInt(evt.data);
                if (!isNaN(height)) {
                    self.iframeStyle = 'width:100%; height:' + (evt.data + 20) + 'px';
                    $timeout(0);
                } else {
                    self.dashboardTitle = evt.data;
                    $timeout(0);
                }
            }
            window.addEventListener('message', receiveMessage, false);
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            this.homeController.templateName = 'dashboards';
            this.homeController.imageurl5 = "dist/icons/svg/landingPage/playlist_normal.svg";

        }

        //new DashboardModel('Continuous Delivery Measurement Dashboard', 'Pipeline_Dashboard', null, "./dist/modules/pipeline/view/pipelineView.html", 'Pipeline Dashboard', false)
        iSightDashboards = [];
        dashboardTitle: string;
        selectedOrgUrl: string;
        defaultOrg: number;
        homeController: HomePageController;
        selectedApp: string;

        icon = {
            iconSrc: 'dist/icons/svg/ic_dashboard_24px.svg'
        }


        selectedDashboard: DashboardModel;
        iframeStyle: String;
        //grafanahostUrl: string;
        trustedHost: boolean = false;
        trustedUrl: boolean = false;
        orgList = [];
        dashboards = [];
        shouldReload: boolean;

        toggleSideNav(navId: string, closeSideNav: boolean): void {
            if (closeSideNav) {
                this.$mdSidenav(navId).close();
            } else {
                this.$mdSidenav(navId).toggle();
            }
        }

        setScrollBarPosition() {
            setTimeout(function () {
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }, 500);
        }

        setSelectedDashboard = function (dashboard) {
            var self = this;
            self.selectedDashboard = dashboard;
            self.dashboardTitle = dashboard.title;
            if (dashboard.iframeUrl && !dashboard.trustedUrl) {
                self.selectedDashboard.iframeUrl = self.$sce.trustAsResourceUrl(dashboard.iframeUrl);
                self.selectedDashboard.trustedUrl = true;
                self.setScrollBarPosition();
            }
        };

        loadorganizations(): void {
            var self = this;
            this.dashboardService
                .getCurrentUserOrgs()
                .then(function (orgData) {
                    var orgDataArray = orgData.data;
                    self.orgList = orgDataArray;
                    self.dashboardService
                        .getUsers()
                        .then(function (userData) {
                            var grafanaOrgId = userData.data.orgId;
                            self.defaultOrg = grafanaOrgId;
                            for (var key in self.orgList) {
                                var orgDtl = self.orgList[key];
                                if (orgDtl.id === grafanaOrgId) {
                                    self.selectedApp = orgDtl.name;
                                }
                            }
                            self.getDashboards();
                        });
                });
        }

        switchOrganizations(orgId): void {
            var self = this;
            self.defaultOrg = orgId;
            self.checkStyle(orgId);
            self.dashboardService
                .switchUserOrg(orgId)
                .then(function (selOrgStatus) {
                    self.$rootScope.refreshDashboard = new Date();
                    if (selOrgStatus.status === 'success') {
                        self.getDashboards();
                    }
                    self.authenticationService.getGrafanaCurrentOrgAndRole()
                        .then(function (data) {
                            if (data.grafanaCurrentOrgRole === 'Admin') {
                                self.homeController.showAdminTab = true;
                                if (self.homeController.showInsightsTab) {
                                    self.homeController.selectedIndex = 2;
                                } else {
                                    self.homeController.selectedIndex = 1;
                                }

                            } else {
                                self.homeController.showAdminTab = false;
                                if (self.homeController.showInsightsTab) {
                                    self.homeController.selectedIndex = 1;
                                } else {
                                    self.homeController.selectedIndex = 0;
                                }
                            }

                            self.$cookies.put('grafanaRole', data.grafanaCurrentOrgRole);
                            self.$cookies.put('grafanaOrg', data.grafanaCurrentOrg);
                            if (data.userName != undefined) {
                                self.homeController.userName = data.userName.replace(/['"]+/g, '');
                            }
                            self.homeController.userRole = data.grafanaCurrentOrgRole;
                            self.homeController.userCurrentOrg = data.grafanaCurrentOrg;
                            self.authenticationService.getCurrentUserOrgs()
                                .then(function (orgdata) {
                                    self.homeController.userCurrentOrgName = orgdata.data.filter(function (i) {
                                        return i.orgId == self.homeController.userCurrentOrg;
                                    });
                                });
                        });



                });

            self.iframeStyle = 'width:100%; height:200px;';

        }

        checkStyle(orgId: number): string {
            if (orgId == this.defaultOrg) {
                return "background-color: #f1f1f1";
            } else {
                return "";
            }
        }

        getDashboards() {
            var self = this;
            this.elasticSearchService
                .loadKibanaIndex()
                .then(function (dashboardData) {
                    var dataArray = dashboardData.dashboards;
                    var model = [];
                    dataArray.forEach(element => {
                        model.push(new DashboardModel(element.title, element.id, element.url, null, element.title, false));
                    });
                    self.dashboards = model;
                    self.setSelectedDashboard(model[0]);
                    if (self.homeController.selectedDashboardUrl && self.homeController.selectedDashboardUrl.trim().length != 0) {
                        var dashbmodel = new DashboardModel(null, null, self.homeController.selectedDashboardUrl, null, null, false);
                        self.setSelectedDashboard(dashbmodel);
                    }
                    if (self.selectedDashboard) {
                        self.dashboardTitle = self.selectedDashboard.title;
                    }
                });
            this.homeController.templateName = 'dashboards';
        }

    }
}
