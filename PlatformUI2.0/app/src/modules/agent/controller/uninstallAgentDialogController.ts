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
    export class UninstallAgentDialogController {
        static $inject = ['agentService', '$mdDialog', '$route', '$location'];
        constructor(private agentService: IAgentService, private $mdDialog, private $route, private $location) {
            var self = this;
            self.statusObject = self['locals'].statusObject;
            self.agentKey = self['locals'].agentKey;
            self.toolName = self['locals'].toolName;
            self.osVersion = self['locals'].osVersion;

            var elem = document.querySelector('#agentTemplateContainer');
            var agentListControllerScope = angular.element(elem).scope();
            var agentController = agentListControllerScope['agentListController'];
            this.agentListController = agentController;
        }
        statusObject: boolean;
        agentKey: any;
        toolName: string;
        osVersion: string;
        agentListController: AgentListController;

        confirmation(): void {
            var self = this;
            self.agentService.agentUninstall(self.agentKey, self.toolName, self.osVersion).then(function (data) {

            }).catch(function (data) {
                self.agentListController.showConfirmMessage = "service_error";
                self.agentListController.getRegisteredAgents();
            });
            self.agentListController.getRegisteredAgents();
            self.hide();
        }

        hide(): void {
            this.$mdDialog.hide();
        }

        cancel(): void {
            this.$mdDialog.cancel();
        }
    }
}