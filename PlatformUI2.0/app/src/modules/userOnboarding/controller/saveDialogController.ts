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

    export class SaveDialogController {
        static $inject = ['userOnboardingService', '$mdDialog', '$route'];

        constructor(private userOnboardingService: IUserOnboardingService, private $mdDialog, private $route) {
            this.appUserDetails = this['locals'].appUserDetails;
            this.selectedRadioUser = this['locals'].selectedRadioUser
            this.appData = this['locals'].appData;
            this.radioUserSel = this['locals'].radioSelected;
            
            for (var key in this.appUserDetails) {
                var selectedUserDtl = this.appUserDetails[key];
                if (selectedUserDtl['login'] === this.selectedRadioUser) {
                    this.userId = selectedUserDtl["userId"];
                    this.userEmail = selectedUserDtl["email"];
                }
            }
        }

        appUserDetails=[];
        selectedRadioUser: string;
        radioUserSel: boolean;
        appData: any = {};
        userEmail: string;
        userId: number;

        deleteUser(): void {
            var selectedUser = {};
            var orgId = this.appUserDetails[0].orgId;
            var self = this;
            self.userOnboardingService
                .deleteUserFromOrg(this.userId, orgId)
                .then(function(data) {

                });
            self.userOnboardingService
                .getOrgUserInfo(orgId)
                .then(function(data) {
                    var appUserDetails = data.data;
                    self.appData['appInfo'] = appUserDetails;
                     if(data.status === "success"){
                        self.hide();
                    }
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
