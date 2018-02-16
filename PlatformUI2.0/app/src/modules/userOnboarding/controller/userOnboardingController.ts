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
        static $inject = ['$location', '$window', '$mdDialog', 'userOnboardingService', 'roleService', 'restEndpointService', '$sce', '$timeout'];
        constructor(private $location, private $window, private $mdDialog, private userOnboardingService: IUserOnboardingService, private roleService: IRoleService, private restEndpointService: IRestEndpointService, private $sce, private $timeout) {
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            this.homeController = homePageController;
            var self = this;
            self.getHost();

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

        getHost() {
            var self = this;
            /*self.userListUrl = self.$sce.trustAsResourceUrl('http://localhost:3000/dashboard/script/CustomiSight.js?url=http://localhost:3000/org/users');*/
            self.restEndpointService.getGrafanaHost1().then(function (response) {
                var grafanaEndPoint = response.grafanaEndPoint;
                console.log(grafanaEndPoint);
                self.userListUrl = self.$sce.trustAsResourceUrl(grafanaEndPoint + '/dashboard/script/CustomiSight.js?url=' + grafanaEndPoint + '/org/users');
            });
            //console.log(this.userListUrl);
        }
    }
}