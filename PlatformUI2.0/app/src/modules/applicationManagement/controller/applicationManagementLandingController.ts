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
    export class ApplicationManagementController {
        static $inject = ['$location', '$window', '$mdDialog', 'roleService', '$mdSidenav', '$route', '$cookies', 'onboardProjectService', '$log'];
        constructor(private $location, private $window, private $mdDialog, private roleService: IRoleService, private $mdSidenav, private $route, private $cookies, private onboardProjectService, private $log) {
            var grafanaRoleVal = $cookies.get('grafanaRole');
            var self = this;
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            self.homeController = homePageController;
            self.getOrgs();

        }

        applicationsDetails = [];
        searchOrg: string = '';
        showTable: boolean = false;
        searchApplicationResult: string = '';
        addNewApplicationName: string = "";
        showAddApplication: boolean = false;
        homeController: HomePageController;
        showTemplateAfterLoad: boolean = false;
        showMsgOnNoDataOnboarded: string = 'No Data Onboarded';
        applicationConfigured: boolean = true;
        appDataArray = [];
        paginatedAppDataArray = [];
        showPaginationBar: boolean = false;
        showApplicationAddedMessage: boolean = false;
        /* start code for buttons bar*/
        addApplication(params, addedApplicationName): void {
            var self = this;
            var statusObject = {
                'status': false
            }
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
        }
        showAddApplicationBox(): void {
            this.showApplicationAddedMessage = false;
            if (this.showAddApplication === false) {
                this.showAddApplication = true;
            }
            else {
                this.showAddApplication = false;
            }
        }
        goToUserOnBoard(): void {
            this.homeController.templateName = 'userOnboarding';
        }
        goToDataOnBoard(): void {
            this.homeController.templateName = 'dataOnboarding';
        }
        addApplicationConfirmation(status): void {
            var self = this;
            if (status === true) {
                self.showApplicationAddedMessage = true;
                this.roleService
                    .createOrg(self.addNewApplicationName)
                    .then(function (data) {
                        self.getOrgs();
                    });
            }
        }
        /* end code for buttons bar*/
        calculateOrgDetails(applicationsDetails): void {
            var self = this;
            self.applicationsDetails = [];
            self.applicationsDetails = applicationsDetails;
            for (let i = 0; i < self.applicationsDetails.length; i++) {

                self.applicationsDetails[i].totalusers = '';
                let orgId = self.applicationsDetails[i].id;
                self.roleService
                    .getOrgUserInfo(orgId)
                    .then(function (data) {
                        if (data.status = "success") {
                            self.applicationsDetails[i].totalusers = data.data.length;
                        }
                    });
                self.applicationsDetails[i].dataOnboarded = "";
                self.onboardProjectService
                    .fetchProjectMappingByOrgId(orgId)
                    .then(function (data) {
                        if (data.status = "success") {
                            var toolsDataArray = data.data;
                            if (toolsDataArray.length > 0) {
                                for (let j = 0; j < toolsDataArray.length; j++) {
                                    self.applicationsDetails[i].dataOnboarded = self.applicationsDetails[i].dataOnboarded.concat("   ");
                                    self.applicationsDetails[i].dataOnboarded = self.applicationsDetails[i].dataOnboarded.concat(toolsDataArray[j].toolName);
                                    self.applicationsDetails[i].dataOnboarded = self.applicationsDetails[i].dataOnboarded.concat("   ");
                                    if (j !== toolsDataArray.length - 1) {
                                        self.applicationsDetails[i].dataOnboarded = self.applicationsDetails[i].dataOnboarded.concat(",");
                                    }
                                }
                            }
                            else {
                                self.applicationsDetails[i].dataOnboarded = self.showMsgOnNoDataOnboarded;
                            }
                            if (self.applicationsDetails[self.applicationsDetails.length - 1].dataOnboarded !== "") {
                                self.showTemplateAfterLoad = true;
                            }
                        }
                    });
            }
        }
        filterSearchValue(applicationSearch: string): void {
            var self = this;
            self.searchApplicationResult = applicationSearch;
        }
        totalRows: number = 10;//total items per page
        maxSize = 4;// total pages blocks will be displayed
        totalItems: number;//total no of items available
        currentPage = 1;//current page selected
        begin = 0;
        end = 10;
        setPage(pageNo) {
            this.currentPage = pageNo;
        };

        pageChanged() {
            //this.$log.log('Page changed to: ' + this.currentPage);
        };
        updateResult() {
            var self = this;
            self.begin = (self.currentPage - 1) * self.totalRows;
            self.end = self.begin + 10;
            self.updateTable();
        }
        updateTable() {
            var self = this;
            self.totalItems = self.appDataArray.length;
            self.paginatedAppDataArray = [];
            self.paginatedAppDataArray = self.appDataArray.slice(self.begin, self.end);
            self.calculateOrgDetails(self.paginatedAppDataArray);

        }
        getOrgs() {
            var self = this;
            self.roleService
                .getAllOrg()
                .then(function (data) {
                    if (data.status === 'success') {
                        self.appDataArray = data.data;
                        if (self.appDataArray !== undefined && self.appDataArray.length !== 0) {
                            self.showTable = true;
                            self.updateTable();
                            self.applicationConfigured = true;
                            if (self.appDataArray.length > 10) {
                                self.showPaginationBar = true;

                            }
                        }
                        else if (self.appDataArray.length === 0) {
                            self.applicationConfigured = false;
                        }

                    }
                });
        }
        /*end for application landing page*/
    }
}