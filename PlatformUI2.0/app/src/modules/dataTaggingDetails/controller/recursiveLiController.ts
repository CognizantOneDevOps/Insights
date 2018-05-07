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
    export class RecursiveLiController {
        static $inject = ['$location', '$window', '$mdDialog','$scope',
  '$filter','dataTaggingDetailsService','$rootScope'];
        constructor(private $location, private $window, private $mdDialog, private $scope, private $filter,private dataTaggingDetailsService,$rootScope) {

            
           this.init($scope,$filter,dataTaggingDetailsService,$rootScope);
        }
    showThrobber:boolean = true;
    


   init($scope, $filter, dataTaggingDetailsService,$rootScope) : void{

        $scope.clickHandler = function(e) {
                        var a = document.getElementById(e.target.id);
                        var els = [];
                        while(a.parentElement) {
                          
                           if(a.parentElement.tagName == "LI"){
                                   console.log(a.parentElement.dataset.val);
                                    els.unshift(a.parentElement.dataset.val);
                           }
                           a = a.parentElement;
                        }
                        console.log(els);
                        var level1 = els[0];
                        var level2 = els[1];
                        var level3 = els[2];
                        var level4 = els[3];
                        //dataTaggingDetailsService.getHierarchyProperties(level1,level2,level3,level4)
                        //.then(function (data) {
                        //    console.log(data);
                        //});
                        $scope.test = {"toolProperty3": "",
                                "uuid": "ecae50b0-033f-11e8-855b-005056b1008e",
                                "toolProperty4": "",
                                "toolProperty1": "GIT description1",
                                "toolProperty2": "GIT description2",
                                "level_3": "Dep1",
                                "level_4": "project1",
                                "propertyValue1": "NextGenBilling1",
                                "propertyValue4\"": "\"",
                                "propertyValue3": "",
                                "propertyValue2": "value2",
                                "\"id": "\"1",
                                "level_1": "Billing1",
                                "level_2": "Invoicebill",
                                "toolName": "GIT"};
                        $scope.$parent.test = $scope.test;
                        $scope.myVar = {"toolProperty3": "",
                                "uuid": "ecae50b0-033f-11e8-855b-005056b1008e",
                                "toolProperty4": "",
                                "toolProperty1": "GIT description1",
                                "toolProperty2": "GIT description2",
                                "level_3": "Dep1",
                                "level_4": "project1",
                                "propertyValue1": "NextGenBilling1",
                                "propertyValue4\"": "\"",
                                "propertyValue3": "",
                                "propertyValue2": "value2",
                                "\"id": "\"1",
                                "level_1": "Billing1",
                                "level_2": "Invoicebill",
                                "toolName": "GIT"};
                        $scope.$parent.myVar = $scope.myVar;
                       $scope.$parent.myVar = {"toolProperty3": "",
                                "uuid": "ecae50b0-033f-11e8-855b-005056b1008e",
                                "toolProperty4": "",
                                "toolProperty1": "GIT description1",
                                "toolProperty2": "GIT description2",
                                "level_3": "Dep1",
                                "level_4": "project1",
                                "propertyValue1": "NextGenBilling1",
                                "propertyValue4\"": "\"",
                                "propertyValue3": "",
                                "propertyValue2": "value2",
                                "\"id": "\"1",
                                "level_1": "Billing1",
                                "level_2": "Invoicebill",
                                "toolName": "GIT"};

                        $scope.$apply();
                         $scope.$parent.$apply();

                    };
                    
    
   }


    
    }
}