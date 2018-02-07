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
    export class DataTaggingDetailsController {
        static $inject = ['$location', '$window', '$mdDialog','$scope',
  '$filter','dataTaggingDetailsService'];
        constructor(private $location, private $window, private $mdDialog, private $scope, private $filter,private dataTaggingDetailsService) {
           
            var self = this;

            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            self.homeController = homePageController;
            self.dataTaggingDetailsService.getHierarchyMapping()
                .then(function (data) {
                    self.showThrobber = false;
                    $scope.list = data.data;
                    var level1 = $scope.list[0].name;
                  var level2 = $scope.list[0].children[0].name;
                  var level3 = $scope.list[0].children[0].children[0].name;
                  var level4 = $scope.list[0].children[0].children[0].children[0].name;
                 
                  self.showThrobber = false;
                  self.dataTaggingDetailsService.getHierarchyProperties(level1,level2,level3,level4)
                        .then(function (data) {
                         console.log(data);
                         $scope.hierarchyProperties = data.data;
                      });
                       
                });

             
            
              this.init($scope,$filter,dataTaggingDetailsService);
        }
    showThrobber:boolean = true;
     homeController: HomePageController;

    goToDataOnBoard(): void {
            this.homeController.templateName = 'dataOnboarding';
      }

   init($scope, $filter, dataTaggingDetailsService) : void{
   $scope.test = {};
   $scope.myVar = {};
     $scope.initCheckbox = function (item, parentItem) {
      return item.selected = parentItem && parentItem.selected || item.selected || false;
    };

    $scope.showHideProps = function(elementId){
      var listElem = document.getElementById(elementId);
      if( listElem.style.display == 'none'){
        listElem.style.display = 'block';
      }else{
        listElem.style.display = 'none'
      }
    };

     $scope.test = function(value) {
         
        var a = document.getElementById(value);
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
                        dataTaggingDetailsService.getHierarchyProperties(level1,level2,level3,level4)
                        .then(function (data) {
                         console.log(data);
                         $scope.hierarchyProperties = data.data;
                        });
                       

    }
    $scope.toggleCheckbox = function (item, parentScope) {
      if (item.children != null) {
        $scope.$broadcast('changeChildren', item);
      }
      if (parentScope.item != null) {
        return $scope.$emit('changeParent', parentScope);
      }
    };
    $scope.clickHandler1 = function(name){
       console.log("in click handler");
       console.log(name);
       var child1 = document.getElementById("project1");
       var parent:HTMLElement  = child1.parentElement.parentElement.parentElement;
       var contents = parent.innerHTML ;
       console.log(contents);
    };
    $scope.$on('changeChildren', function (event, parentItem) {
      var child, i, len, ref, results;
      ref = parentItem.children;
      results = [];
      for (i = 0, len = ref.length; i < len; i++) {
        child = ref[i];
        child.selected = parentItem.selected;
        if (child.children != null) {
          results.push($scope.$broadcast('changeChildren', child));
        } else {                 
          results.push(void 0);
        }
      }
      return results;
    });
    return $scope.$on('changeParent', function (event, parentScope) {
      var children;
      children = parentScope.item.children;
      parentScope.item.selected = $filter('selected')(children).length === children.length;
      parentScope = parentScope.$parent.$parent;
      if (parentScope.item != null) {
        return $scope.$broadcast('changeParent', parentScope);
      }
    });
  }

  showDetails(itemName): void{
     console.log("clicked");

  }

    
    }
}
