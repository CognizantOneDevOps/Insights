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

    export class DeleteDialogController {
        static $inject = ['userOnboardingService', '$mdDialog', '$route'];

        constructor(private userOnboardingService: IUserOnboardingService, private $mdDialog, private $route) {
            this.selectedUserDtl = this['locals'].selectedUserDtl;
            this.appUserDetails = this['locals'].appUserDetails;
            this.appData = this['locals'].appData;
            this.userId = this.selectedUserDtl[0].selectedUserId;
            this.userEmail = this.selectedUserDtl[0].selectedEmailAdd;
            this.saveMsgArr = this['locals'].saveMsgArr;
            this.paginatedUserOnboardedArray = this['locals'].paginatedUserOnboardedArray;
            this.showPaginationJson = this['locals'].showPaginationJson;
            for (var key in this.appUserDetails) {
                var selectedUserDtl = this.appUserDetails[key];
                if (selectedUserDtl['login'] === this.selectedUserDtl[0].selectedLoginName) {                    
                    this.userId = selectedUserDtl["userId"]
                }
            }
        }

        appUserDetails = [];
        selectedUserDtl: {};
        appData = [];
        userEmail: string;
        userId: number;
        saveMsgArr = {
            'saveMsg': '',
        }
        begin = 0;
        end = 10;
        totalRows: number = 10;//total items per page
        maxSize = 4;// total pages blocks will be displayed
        totalItems: number;//total no of items available
        currentPage = 1;//current page selected
        paginatedUserOnboardedArray = [];
        showPaginationJson ={
            'showPaginationBar' : false,
        }
        showpaginationArr=[];

        deleteUser(): void {
            var selectedUser = {};
            var orgId = this.appUserDetails[0].orgId;
            var self = this;
            self.userOnboardingService
                .deleteUserFromOrg(self.userId, orgId)
                .then(function (data) {
                    self.userOnboardingService
                        .getOrgUserInfo(orgId)
                        .then(function (data) {
                            var appUserDetails = data.data;
                            self.appData['appInfo'] = appUserDetails;
                            self.end = self.appData['appInfo'].length;
                            self.begin = (self.appData['appInfo'].length) - 10;
                            self.totalItems = self.appData['appInfo'].length;
                            if (self.totalItems > 10) {
                                self.showPaginationJson['showPaginationBar'] = true;
                            }
                            else {
                                self.showPaginationJson['showPaginationBar'] = false;
                            }
                            self.paginatedUserOnboardedArray['paginatedArr'] = self.appData['appInfo'].slice(self.begin, self.end);
                            if (data.status === "success") {
                                self.saveMsgArr['saveMsg'] = 'User deleted Sucessfully';
                                self.hide();
                            }

                        });
                });
        }

        hide(): void {
            this.$mdDialog.hide();
        }

        cancel(): void {
            this.$mdDialog.cancel();
        }
    }
}