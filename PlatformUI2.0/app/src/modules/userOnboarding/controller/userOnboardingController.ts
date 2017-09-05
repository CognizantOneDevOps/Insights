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
        static $inject = ['$location', '$window', '$mdDialog', 'userOnboardingService', 'roleService'];
        constructor(private $location, private $window, private $mdDialog, private userOnboardingService: IUserOnboardingService, private roleService: IRoleService) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            self.userOnboardingService
                .getAllOrg()
                .then(function (data) {
                    var appDataArray = data.data;
                    self.apps = appDataArray;
                    self.selectedApp = self.apps[0].name;
                    self.defaultAppData['defaultApp'] = self.apps[0].id;

                    self.userOnboardingService
                        .getOrgUserInfo(self.defaultAppData['defaultApp'])
                        .then(function (data) {
                            if (data.status === 'success') {
                                //self.showThrobber = false;
                                self.appUserDetails = data.data;
                                self.appData['appInfo'] = self.appUserDetails;
                                self.end = self.appData['appInfo'].length;
                                self.begin = (self.appData['appInfo'].length) - 10;
                                self.updateTable();
                            }
                        });
                });
            this.userOnboardingService
                .getAllUsers()
                .then(function (data) {
                    self.allUserList = data.data;

                });
            this.showActions = false;

        }

        apps = [];
        selectedApp: string;
        defaultAppData: any = {};
        appData = [];
        appUserDetails: any = {};
        allUserList = {};
        searchedUser: string = '';
        searchText: string;
        selectedRole: string;

        showSearchedRec: boolean;
        isValidUser: boolean;
        showThrobber: boolean;
        invalidUserMsg: string = '';
        showUserDetails: boolean;
        userData = {};
        grafanaUserId: string;
        selectedRadioUser: string;
        homeController: HomePageController;

        userOnboardDataModel = new UserOnboardingModel();
        selectedUserRowsModel = new SelectedUserRowsModel();
        editIconSrc: string = "dist/icons/svg/userOnboarding/Edit_icon_disabled.svg";
        saveIconSrc: string = "dist/icons/svg/userOnboarding/Save_icon_Disabled.svg";
        deleteIconSrc: string = "dist/icons/svg/userOnboarding/Delete_icon_disabled.svg";
        showActions: boolean = false;
        radioSelected: boolean;
        //saveMsg: string = '';
        saveMsgArr = {
            'saveMsg': '',
        }
        addNewApplicationName: string;
        showAddApplication: boolean = false;
        newUserArray = [];
        selectedUser: string;
        showApplicationAddedMessage: boolean = false;

        selectApp(): void {
            var orgId;
            this.searchText = '';
            this.searchedUser = '';
            this.saveMsgArr['saveMsg'] = '';
            var self = this;
            this.userOnboardingService
                .getAllOrg()
                .then(function (data) {
                    var appDataArray = data.data;
                    for (var key of appDataArray) {
                        if (key.name === self.selectedApp) {
                            orgId = key.id;
                        }
                    }
                    self.userOnboardingService
                        .getOrgUserInfo(orgId)
                        .then(function (data) {
                            self.appUserDetails = data.data;
                            self.appData['appInfo'] = self.appUserDetails
                            self.updateResult();
                        });
                });

        }

        searchUser(searchStrng: string): void {
            this.saveMsgArr['saveMsg'] = '';
            var self = this;
            this.showThrobber = true;
            this.showSearchedRec = false;
            this.isValidUser = false;
            this.invalidUserMsg = '';
            //var appUserDetails = this.appUserDetails;
            var appUserDetails = this.appData['appInfo'];
            //console.log(this.appUserDetails);
            //console.log(this.appData['appInfo']);
            for (var key in appUserDetails) {
                var allUsersArray = appUserDetails[key];
                var userLogin = allUsersArray["login"];
                var userEmail = allUsersArray["email"];
                if (searchStrng == userLogin || searchStrng == userEmail) {
                    this.paginatedUserOnboardedArray['paginatedArr'] = [];
                    this.showSearchedRec = true;
                    this.showPaginationJson['showPaginationBar'] = false;
                    this.searchText = searchStrng;
                    this.paginatedUserOnboardedArray['paginatedArr'].push(allUsersArray);
                    break;
                } else if (searchStrng === '' || searchStrng === undefined) {
                    this.showSearchedRec = true;
                    this.updateTable();
                    break;
                }
            }
            if (!this.showSearchedRec) {
                this.userOnboardingService.userSearch(this.searchedUser)
                    .then(function (data) {
                        if (data.status === 'SUCCESS') {
                            self.isValidUser = true;
                            self.showThrobber = false;
                            self.showUserDetails = true;
                            self.invalidUserMsg = '';
                            self.showPaginationJson['showPaginationBar'] = false;
                            self.paginatedUserOnboardedArray['paginatedArr'] = [];
                            self.userData = data.data;
                            var newUserArray = self.userData;
                            var selectedUser = {};
                            selectedUser['email'] = newUserArray["emailAddress"];
                            selectedUser['login'] = newUserArray["employeeId"];
                            selectedUser['familyName'] = newUserArray["familyName"];
                            selectedUser['newUser'] = true;
                            self.appData['appInfo'].push(selectedUser);
                            self.paginatedUserOnboardedArray['paginatedArr'].push(self.appData['appInfo']);
                            self.newUserArray.push(selectedUser);
                            self.selectedUser = newUserArray["employeeId"];
                            //self.addAction();
                            self.radioSelection(searchStrng, true);
                            self.updateResult();
                        } else if (data.error) {
                            self.invalidUserMsg = 'Invalid User';
                            self.isValidUser = false;
                            self.showUserDetails = false;
                            self.showThrobber = false;
                        }

                    });
            }
        }

        radioSelection(login, newUser: boolean): void {
            this.saveMsgArr['saveMsg'] = '';
            this.selectedRadioUser = login;
            this.radioSelected = true;
            this.addAction();

            var appUserDataArray = this.appData['appInfo'];
            var selectedUserData = new UserOnboardingModel();
            this.selectedUserRowsModel = new SelectedUserRowsModel();
            for (var key in appUserDataArray) {
                var selectedUserDtl = appUserDataArray[key];
                if (selectedUserDtl['login'] === this.selectedRadioUser) {
                    selectedUserData.selectedLoginName = login;
                    selectedUserData.selectedEmailAdd = selectedUserDtl['email'];
                    selectedUserData.selectedRole = selectedUserDtl['role'];
                    selectedUserData.familyName = selectedUserDtl['familyName'];
                    selectedUserData.selectedUserId = selectedUserDtl["userId"]
                    selectedUserData.editRole = false;
                    selectedUserData.newUser = newUser;
                    selectedUserData.selectedOrgId = selectedUserDtl['orgId'];
                    this.selectedUserRowsModel.selectedUserRow.push(selectedUserData);
                }
            }
        }
        isUserAlreadyAdded: boolean = false;

        addAction(): void {
            this.showActions = true;
            this.editIconSrc = "dist/icons/svg/userOnboarding/Edit_icon_MouseOver.svg";
            this.saveIconSrc = "dist/icons/svg/userOnboarding/Save_icon_MouseOver.svg";
            this.deleteIconSrc = "dist/icons/svg/userOnboarding/Delete_icon_MouseOver.svg";
        }

        addNewUserToOrg(param): void {
            var roleSelected = this.selectedRole;
            var orgUserDetails = [];
            orgUserDetails = this.appData['appInfo'];
            this.saveMsgArr['saveMsg'] = '';
            var orgId = orgUserDetails[0].orgId;
            var orgName = '';
            this.userOnboardingService
                .getAllOrg()
                .then(function (data) {
                    var appDataArray = data.data;
                    for (var key of appDataArray) {
                        if (key.id === orgId) {
                            orgName = key.name;
                        }
                    }
                });
            if (this.selectedUserRowsModel.selectedUserRow[0].newUser &&
                roleSelected === null || roleSelected === undefined) {
                this.saveMsgArr['saveMsg'] = 'Please select role';
            } else {
                this.saveMsgArr['saveMsg'] = '';
                if (!this.selectedUserRowsModel.selectedUserRow[0].editRole) {
                    if (roleSelected != null || roleSelected != undefined) {
                        var self = this;
                        this.userOnboardingService
                            .getAllUsers()
                            .then(function (data) {
                                self.allUserList = data.data;
                                for (var key in self.allUserList) {
                                    var allUserDtlArray = self.allUserList[key];
                                    var userId = allUserDtlArray['id'];
                                    if (allUserDtlArray['login'] === self.selectedRadioUser) {
                                        self.isUserAlreadyAdded = true;
                                        break;
                                    }
                                }
                                if (self.isUserAlreadyAdded) {
                                    self.userOnboardingService
                                        .addUserToOrg(orgId, orgName, self.selectedUserRowsModel.selectedUserRow[0].selectedLoginName,
                                        self.selectedRole)
                                        .then(function (data) {
                                            self.addUserToOrgAction(data, userId);
                                        });
                                }
                                else if (!self.isUserAlreadyAdded) {
                                    self.userOnboardingService
                                        .addUser(self.selectedUserRowsModel.selectedUserRow[0].familyName,
                                        self.selectedUserRowsModel.selectedUserRow[0].selectedEmailAdd,
                                        self.selectedUserRowsModel.selectedUserRow[0].selectedLoginName)
                                        .then(function (addUserdata) {
                                            if (addUserdata.message === "User created") {
                                                self.grafanaUserId = addUserdata.id;
                                                self.userOnboardingService
                                                    .addUserToOrg(orgId, orgName,
                                                    self.selectedUserRowsModel.selectedUserRow[0].selectedLoginName,
                                                    self.selectedRole)
                                                    .then(function (data) {
                                                        self.addUserToOrgAction(data, self.grafanaUserId);
                                                    });
                                            } else {
                                                for (var i = 0; i < self.getToolDataLength(self.appData['appInfo']); i++) {
                                                    var newUserDtl = self.appData['appInfo'][i];
                                                    if (newUserDtl['login'] === self.selectedRadioUser) {
                                                        self.appData['appInfo'].splice(i, 1);
                                                    }
                                                }
                                                self.saveMsgArr['saveMsg'] = 'Failed to create user';
                                            }
                                        });
                                }
                            });
                        this.searchedUser = '';
                        this.showSearchedRec = false;
                    } else {
                        this.saveMsgArr['saveMsg'] = 'User already saved';
                    }
                }
                else if (this.selectedUserRowsModel.selectedUserRow[0].editRole) {
                    this.editUserFromOrg(param);
                }
            }
        }

        getToolDataLength(obj): number {
            return Object.keys(obj).length;
        }
        addUserToOrgAction(data, grafanaUserId): void {
            var orgUserDetails = this.appData['appInfo'];
            for (var key in orgUserDetails) {
                if (data.message = "User added to organization") {
                    var newUserDtl = orgUserDetails[key];
                    if (newUserDtl['login'] === this.selectedRadioUser) {
                        this.appData['appInfo'][key]['role'] = this.selectedRole;
                        this.appData['appInfo'][key]['userId'] = grafanaUserId;
                        this.appData['appInfo'][key]['newUser'] = false;
                        this.updateTable();
                    }
                    this.saveMsgArr['saveMsg'] = 'User added to organization';
                } else {
                    this.saveMsgArr['saveMsg'] = 'Failed to add user to organization';
                    this.appData['appInfo'][key]['newUser'] = false;
                }
            }

        }

        showEditedUser(): void {
            // this.saveMsg = '';
            this.saveMsgArr['saveMsg'] = '';
            var orgId = this.appUserDetails[0].orgId;
            var appUserData = this.appData['appInfo'];
            for (var key in appUserData) {
                var selectedUserDtl = appUserData[key];
                if (this.selectedUserRowsModel.selectedUserRow[0].selectedLoginName === selectedUserDtl['login']) {
                    this.appData['appInfo'][key]['editRole'] = true;
                    this.selectedUserRowsModel.selectedUserRow[0].editRole = true;
                }
            }
        }

        editUserFromOrg(params): void {
            this.saveMsgArr['saveMsg'] = ''
            var self = this;
            this.$mdDialog.show({
                controller: EditDialogController,
                controllerAs: 'editDialogController',
                templateUrl: './dist/modules/userOnboarding/view/editDialog.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                clickOutsideToClose: true,
                locals: {
                    selectedUserDtl: this.selectedUserRowsModel.selectedUserRow,
                    appUserDetails: this.appData['appInfo'],
                    appData: self.appData,
                    selectedRole: this.selectedRole,
                    saveMsgArr: this.saveMsgArr
                },
                bindToController: true
            })
        }

        deleteUserFromOrg(params): void {
            this.saveMsgArr['saveMsg'] = '';
            this.searchText = '';
            this.searchedUser = '';
            var self = this;
            this.$mdDialog.show({
                controller: DeleteDialogController,
                controllerAs: 'deleteDialogController',
                templateUrl: './dist/modules/userOnboarding/view/deleteDialog.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                clickOutsideToClose: true,
                locals: {
                    appUserDetails: this.appData['appInfo'],
                    selectedUserDtl: this.selectedUserRowsModel.selectedUserRow,
                    appData: this.appData,
                    saveMsgArr: this.saveMsgArr,
                    paginatedUserOnboardedArray: this.paginatedUserOnboardedArray,
                    showPaginationJson: this.showPaginationJson
                },
                bindToController: true
            })
        }

        saveUser(params): void {
            var self = this;
            this.$mdDialog.show({
                controller: SaveDialogController,
                controllerAs: 'saveDialogController',
                templateUrl: './dist/modules/userOnboarding/view/saveDialog.tmp.html',
                parent: angular.element(document.body),
                targetEvent: params,
                clickOutsideToClose: true,
                locals: {
                    appUserDetails: this.appData['appInfo'],
                    selectedRadioUser: this.selectedRadioUser,
                    radioSelected: this.radioSelected,
                    appData: self.appData
                },
                bindToController: true
            })
        }

        restoreRoleData(userLogin): void {
            var appUserData = this.appData['appInfo'];
            for (var key in appUserData) {
                var selectedUserDtl = appUserData[key];
                if (userLogin === selectedUserDtl['login']) {

                    if (this.selectedUserRowsModel.selectedUserRow[0].newUser) {
                        this.searchedUser = '';
                        this.appData['appInfo'].splice(key, 1);
                    } else if (!this.selectedUserRowsModel.selectedUserRow[0].newUser) {
                        this.appData['appInfo'][key]['editRole'] = false;
                    }
                }
            }
        }
        goToDataOnBoard(): void {
            this.homeController.templateName = 'dataOnboarding';
        }
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
        addApplicationConfirmation(status): void {
            var self = this;
            if (status === true) {
                self.showApplicationAddedMessage = true;
                this.roleService
                    .createOrg(self.addNewApplicationName)
                    .then(function (data) {
                        var newAppData = {};
                        newAppData["name"] = self.addNewApplicationName;
                        newAppData["id"] = data.orgId;
                        newAppData["totalusers"] = 1;
                    });
            }
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
        totalRows: number = 10;//total items per page
        maxSize = 4;// total pages blocks will be displayed
        totalItems: number;//total no of items available
        currentPage = 1;//current page selected
        begin = 0;
        end = 10;
        paginatedUserOnboardedArray = [];
        showPaginationJson ={
            'showPaginationBar' : false,
        }
        
        setPage(pageNo) {
            this.currentPage = pageNo;
        };
        pageChanged() {
            //this.$log.log('Page changed to: ' + this.currentPage);
        };
        updateResult() {
            var self = this;
            self.end = (this.appData['appInfo'].length - ((self.currentPage - 1) * 10));
            if ((self.end - 10) < 0) {
                self.begin = 0;
            }
            else {
                self.begin = self.end - 10;
            }
            self.updateTable();
        }
        updateTable() {
            var self = this;
            self.totalItems = self.appData['appInfo'].length;
            if (self.totalItems > 10) {
                self.showPaginationJson['showPaginationBar'] = true;
            }
            else {
                self.showPaginationJson['showPaginationBar'] = false;
            }
            self.paginatedUserOnboardedArray['paginatedArr'] = [];
            self.paginatedUserOnboardedArray['paginatedArr'] = self.appData['appInfo'].slice(self.begin, self.end);
            //console.log(self.paginatedUserOnboardedArray);
        }
    }
}