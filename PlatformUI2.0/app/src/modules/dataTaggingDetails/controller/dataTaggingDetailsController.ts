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
            $scope.list = [
              {
                name: 'Billing1',
                opened: true,
                children: [
                  {
                    name: 'Invoicebill',
                    children: [
                      {
                        name: 'Dep1',
                        title: 'Leader'
                      },
                      {
                        name: 'Dep2',
                        title: 'Senior F2E'
                      },
                      {
                        name: 'Dep3',
                        title: 'Junior F2E',
                        children: [
                        {
                          name: 'project1',
                          title: 'Leader'
                        }
                      ]
                      }
                    ]
                  },
                  {
                    name: 'Ledgerbill',
                    children: [
                      {
                        name: 'dep1',
                        title: 'Leader'
                      },
                      {
                        name: 'dep2',
                        title: 'Intern'
                      }
                    ]
                  }
                ]
              },
              {
                name: 'Billing3',
                children: [{
                  name: 'invoicebill',
                  title: 'Designer'
                }]
              },
              {
                name: 'Billing4',
                children: [{
                  name: 'Ledgerbill',
                  title: 'Robot'
                }]
              }
            ];
            var self = this;

             self.dataTaggingDetailsService.getHierarchyMapping()
                .then(function (data) {
                    self.showThrobber = false;
                   // $scope.list = data;
                });

           this.init($scope,$filter);
        }
    showThrobber:boolean = true;
    


   init($scope, $filter) : void{
     $scope.initCheckbox = function (item, parentItem) {
      return item.selected = parentItem && parentItem.selected || item.selected || false;
    };
    $scope.toggleCheckbox = function (item, parentScope) {
      if (item.children != null) {
        $scope.$broadcast('changeChildren', item);
      }
      if (parentScope.item != null) {
        return $scope.$emit('changeParent', parentScope);
      }
    };
    $scope.clickHandler = function(){
       console.log("in click handler");
       var child1 = document.getElementById("project1");
       var parent:HTMLElement  = child1.parentElement.parentElement;
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