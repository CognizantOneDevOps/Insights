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

/// <reference path="_all.ts" />

module ISightApp {
    angular.module('iSightApp', ['ngMaterial', 'ngRoute', 'ngResource', 'ngMessages', 'ngCookies','ngAnimate','ui.bootstrap','googlechart','ngTable'])
        .service('pipelineService', PipelineService)
        .service('graphService', GraphService)
        .service('elasticSearchService', ElasticSearchService)
        .service('loginService', LoginService)
        .service('roleService', RoleService)
        .service('agentService', AgentService)
        .service('iconService', IconService)
        .service('restEndpointService', RestEndpointService)
        .service('authenticationService', AuthenticationService)
        .service('toolConfigService', ToolConfigService)
		.service('onboardProjectService', OnboardProjectService)
        .service('dashboardService', DashboardService)
		.service('userOnboardingService', UserOnboardingService)
        .service('aboutService', AboutService)
        .service('restAPIUrlService', RestAPIUrlService)
        .service('restCallHandlerService', RestCallHandlerService)
        .service('dataTaggingService', DataTaggingService)
        .service('dataOnBoardingService', DataOnBoardingService)
        .service('dataTaggingDetailsService', DataTaggingDetailsService)
       	.service('singleToolConfigService', SingleToolConfigService)        
        .service('insightsService', InsightsService)
		.service('platformServiceStatusService', PlatformServiceStatusService)
		.service('appSettingsService', AppSettingsService)

        .controller('pipelineController', PipelineController)
        .controller('homePageController', HomePageController)
        .controller('toolsConfigurationController', ToolsConfigurationController)
        .controller('configuredToolsController', ConfiguredToolsController)
        .controller('dashboardController', DashboardController)
        .controller('applicationManagementController', ApplicationManagementController)
        .controller('userOnboardingController', UserOnboardingController)
        .controller('dataOnBoardingController', DataOnBoardingController)
        .controller('oneToolConfigurationController', OneToolConfigurationController)
        .controller('agentController', AgentController)
		.controller('agentManagementController', AgentManagementController)
		.controller('agentListController', AgentListController)
        .controller('singleToolConfigurationController', SingleToolConfigurationController)
        .controller('dataTaggingController', DataTaggingController)
	    .controller('dataTaggingDetailsController', DataTaggingDetailsController)
        .controller('FileUploadController', FileUploadController)
        .controller('appSettingsController', AppSettingsController)
        .controller('insightsController', InsightsController)
		.controller('dataPurgingController', DataPurgingController)

        .component('footer', {
            templateUrl: './dist/components/footer/view/footerView.html',
            controller: FooterController,
            bindings: {}
        })
        .component('header', {
            templateUrl: './dist/components/header/view/headerView.html',
            controller: HeaderController,
            bindings: {
                title: '@',
                nav: '@'
            }
        })
        .component('throbber',{
            templateUrl:"./dist/components/throbber/view/throbberView.html",
            controller: ThrobberController,
            bindings:{}

        })
        .directive('includeReplace', function () {
            return {
                require: 'ngInclude',
                restrict: 'A',
                link: function(scope, tElem, tAttrs) {

                    tElem.replaceWith(tElem.children());
                }
            };
        })
        
       .directive('row', function() {
            return {
                restrict: 'EA',
                scope: { children:"=" , myVar: '=' ,clickHandler:"&",  'test': '=test', 'count': '=count' },
                controller: RecursiveLiController,
                templateUrl: './dist/modules/dataTaggingDetails/view/test.html'

            };
        })

        .directive('demoFileModel', function ($parse) {
            return {
                restrict: 'A', //the directive can be used as an attribute only
                    
                link: function (scope, element, attrs) {
                    var model = $parse(attrs.demoFileModel),
                    modelSetter = model.assign; //define a setter for demoFileModel
                    var maxSize = 1000000;
                    scope.showUploadBtn = false;
                    element.bind('change', function () {
                       
                        scope.maxSizeErr = false;
                        scope.$apply(function () {
                           
                            modelSetter(scope, (<HTMLInputElement>element[0]).files[0]);

                        });
                        var fileSize = (<HTMLInputElement>element[0]).files[0].size;
                        if (fileSize > maxSize){
                           scope.maxSizeErr = true;
						   scope.imageSrc = "#";
						   scope.showUploadBtn = false;
                        }else{
                        scope.showUploadBtn = true;
                        scope.file = (<HTMLInputElement>element[0]).files[0];
                        scope.getFile();
                        }
						scope.$apply();
                    });
                }
            };
        })
       
        .config(['$routeProvider', '$compileProvider',
            function($routeProvider, $compileProvider) {
                $routeProvider.
                    when('/InSights/pipeline', {
                        templateUrl: './dist/modules/pipeline/view/pipelineView.html',
                        controller: PipelineController,
                        controllerAs: 'pipelineController'
                    }).
                    when('/InSights/kibana', {
                        templateUrl: './dist/modules/kibana/view/kibanaView.html',
                        controller: KibanaDashboardController,
                        controllerAs: 'kibanaController'
                    }).
                    when('/InSights/login', {
                        templateUrl: './dist/modules/login/view/loginView.html',
                        controller: LoginController,
                        controllerAs: 'loginController'
                    }).
                    when('/InSights/home', {
                        templateUrl: './dist/modules/homepage/view/homePageView.html',
                        controller: HomePageController,
                        controllerAs: 'homePageController'
                    }).
                    when('/InSights/dashboard/', {
                        templateUrl: './dist/modules/dashboards/view/dashboardView.html',
                        controller: DashboardController,
                        controllerAs: 'dashboardController'
                    }).
                    when('/InSights/agent', {
                        templateUrl: './dist/modules/agent/view/agentView.html',
                        controller: AgentController,
                        controllerAs: 'agentController'
                    }).                    
                    when('/InSights/onboarding', {
                        templateUrl: './dist/modules/userOnboarding/view/userOnboardingView.html',
                        controller: UserOnboardingController,
                        controllerAs: 'userOnboardingController'
                    }).
                    when('/InSights/home/toolsConfig', {
                        templateUrl: './dist/modules/toolsConfiguration/view/toolsConfigurationView.html',
                        controller: ToolsConfigurationController,
                        controllerAs: 'toolsConfigurationController'
                    }).
        			 when('/InSights/oneTool/:toolCategory/:toolName', {
                         templateUrl: './dist/modules/oneToolConfig/view/oneToolConfigurationView.html',
                          controller: OneToolConfigurationController,
                        controllerAs: 'oneToolConfigurationController'
                     }).
                     when('/InSights/configuredTools', {
                         templateUrl: './dist/modules/configuredTools/view/configuredToolsView.html',
                         controller: ConfiguredToolsController,
                         controllerAs: 'configuredToolsController'
                     }).
					 when('/InSights/applicationManagement', {
                         templateUrl: './dist/modules/applicationManagement/view/applicationManagementView.html',
                         controller: ApplicationManagementController,
                         controllerAs: 'applicationManagementController'
                     }).
					  when('/InSights/userOnboarding', {
                         templateUrl: './dist/modules/userOnboarding/view/userOnboardingView.html',
                         controller: UserOnboardingController,
                         controllerAs: 'userOnboardingController'
                     }).
					 when('/InSights/dataOnBoarding', {
                         templateUrl: './dist/modules/dataOnBoarding/view/dataOnBoardingView.html',
                         controller: DataOnBoardingController,
                         controllerAs: 'dataOnBoardingController'
                     }).
                     when('/InSights/dataTagging', {
                         templateUrl: './dist/modules/dataTagging/view/dataTaggingView.html',
                         controller: DataTaggingController,
                         controllerAs: 'dataTaggingController'
                     }).
                     when('/InSights/insights', {
                         templateUrl: './dist/modules/insights/view/insightsView.html',
                         controller: InsightsController,
                         controllerAs: 'insightsController'
                     }).
					 when('/InSights/agentManagement', {
                         templateUrl: './dist/modules/agent/view/agentManagementView.html',
                        controller: AgentManagementController,
                        controllerAs: 'agentManagementController'
                     }).
					  when('/InSights/agentList', {
                         templateUrl: './dist/modules/agent/view/agentListView.html',
                        controller: AgentListController,
                        controllerAs: 'agentListController'
                     }).

                    otherwise({
                        redirectTo: '/InSights/login'
                    });
                $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):|data:image\//);
            }]
        ).run(function(restEndpointService: IRestEndpointService,authenticationService: IAuthenticationService, $cookies) {
            restEndpointService.getServiceHost();
            /*angular.element(document).ready(function() {
                var authToken = $cookies.get('Authorization');
                var msg = '';
                authenticationService.getAuthentication(authToken,msg);


            });*/
          });
    }