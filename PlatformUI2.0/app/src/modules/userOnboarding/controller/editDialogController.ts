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

    export class EditDialogController {
        static $inject = ['userOnboardingService', '$mdDialog', '$route'];

        constructor(private userOnboardingService: IUserOnboardingService, private $mdDialog, private $route) {
            this.selectedUserDtl = this['locals'].selectedUserDtl;
            this.userEmailId = this.selectedUserDtl[0].selectedEmailAdd;
            this.appUserDetails = this['locals'].appUserDetails;
            this.appData = this['locals'].appData;
            this.selectedRole = this['locals'].selectedRole;
            this.saveMsgArr = this['locals'].saveMsgArr;
        }

        selectedUserDtl: {};
        userEmailId: string;
        appUserDetails = [];
        appData = [];
        selectedRole: string;
        saveMsgArr= {
            'saveMsg':'',
        }

        editUser(): void {
            var self = this;
            var orgId = this.appUserDetails[0].orgId;
            this.userOnboardingService
                .updateUserRoleOrg(orgId, self.selectedUserDtl[0].selectedUserId, self.selectedRole)
                .then(function (data) {
                    for (var key in self.appUserDetails) {
                        var newUserDtl = self.appUserDetails[key];
                        if (newUserDtl['login'] === self.selectedUserDtl[0].selectedLoginName) {
                            self.appData['appInfo'][key]['role'] = self.selectedRole;
                            self.appData['appInfo'][key]['editRole'] = false;
                            self.appData['appInfo'][key]['newUser'] = false;
                        }
                    }
                    
                    self.saveMsgArr['saveMsg'] = 'User edited Sucessfully';
                });
            this.hide();
        }

        hide(): void {
            this.$mdDialog.hide();
        }

        cancel(): void {
            this.$mdDialog.cancel();
        }
    }
}
