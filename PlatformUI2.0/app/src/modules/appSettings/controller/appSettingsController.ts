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
    interface FileReaderEventTarget extends EventTarget {
    result:string
}

interface FileReaderEvent extends Event {
    target: FileReaderEventTarget;
    getMessage():string;
}
    export class AppSettingsController {
        static $inject = ['$location', '$window', '$mdDialog','$scope',
  '$filter','appSettingsService', '$resource', '$http', '$route', '$cookies', 'restAPIUrlService'];
        constructor(private $location, private $window, private $mdDialog, private $scope, private $filter,private appSettingsService, private $resource, private $http, private $route,private $cookies, private restAPIUrlService:IRestAPIUrlService,) {
           
            var self = this;
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];
            self.homeController = homePageController;
            self.selectedImageSrc = self.homeController.imageSrc;       
            
            this.init($scope,$filter,appSettingsService, $resource, $http, $route,$window,self.homeController,self.selectedImageSrc,$cookies,restAPIUrlService);
        }
       homeController: HomePageController;
       selectedImage: string = 'logo';
       selectedImageSrc: string;
       maxSizeErr: boolean = false;

       init($scope, $filter, appSettingsService, $resource, $http, $route, $window,homeController,selectedImageSrc,$cookies,restAPIUrlService) : void{

            $scope.uploadFile = function () {
            var file = $scope.myFile;
            var fd = new FormData();
            fd.append("file", file);
           
            $scope.showThrobber = true;
            var authToken = $cookies.get('Authorization');
            var restcallAPIUrl = restAPIUrlService.getRestCallUrl("UPLOAD_IMAGE");
            var self = this;
            $http.post(restcallAPIUrl, fd, {
                    headers: {
                        'Content-Type': undefined,
                        'Authorization': authToken
                    },
                    transformRequest: angular.identity
            }).then(function(data, status, headers, config) {
                   $scope.showSuccess = true;
                   var fileVal=<HTMLInputElement>document.getElementById("myFileField");
                   fileVal.value = null;
                   $scope.imageSrc = "#";  
                   homeController.showDefaultImg = false;                    
                   $scope.showThrobber = false;
                   homeController.imageSrc= 'data:image/jpg;base64,' + data.data.data.encodedString;
                   selectedImageSrc = 'data:image/jpg;base64,' + data.data.data.encodedString;
                 },function(data){
                    $scope.showThrobber = false;
                    $scope.showError = true;
            });


             
          };

          $scope.getFile = function () {
              $scope.progress = 0;
               /* let reader: FileReader = new FileReader();
                  reader.onload = function(event: FileReaderEvent) {
                      $scope.imageSrc = event.target.result
                      
                      $scope.$apply()
                  };
               reader.readAsDataURL($scope.file); */
          };

          $scope.selectImage = function(){


          };
   
        }

  

    
    }
}
