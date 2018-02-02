/// <reference path="../../../_all.ts" />
module ISightApp {

    export class FileUploadController {

        static $inject = ['$scope', '$cookies', '$http', 'dataOnBoardingService', 'restAPIUrlService']
        constructor(private $scope, private $cookies, private $http, private dataOnBoardingService, private restAPIUrlService:IRestAPIUrlService) {

            var self = this;
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];

            this.initApp(this.$scope, this.$cookies, this.$http, this.dataOnBoardingService, homePageController,restAPIUrlService);
        }

        initApp($scope, $cookies, $http, dataOnBoardingService, homePageController,restAPIUrlService): void {

            var dropbox = document.getElementById("dropbox")
            $scope.dropText = 'Drop files here...'
            $scope.tableData = [];
            $scope.lines = [];
            $scope.headers = [];

            // init event handlers
            function dragEnterLeave(evt) {
                evt.stopPropagation()

                evt.preventDefault()
                $scope.$apply(function() {
                    $scope.dropText = 'Drop files here...'
                    $scope.dropClass = ''
                })

            }
            dropbox.addEventListener("dragenter", dragEnterLeave, false)
            dropbox.addEventListener("dragleave", dragEnterLeave, false)
            dropbox.addEventListener("dragover", function(evt) {
                evt.stopPropagation()
                evt.preventDefault()
                var clazz = 'not-available'
                var ok = evt.dataTransfer && evt.dataTransfer.types && evt.dataTransfer.types.indexOf('Files') >= 0
                $scope.$apply(function() {
                    $scope.dropText = ok ? 'Drop files here...' : 'Only files are allowed!'
                    $scope.dropClass = ok ? 'over' : 'not-available'
                })
            }, false)
            dropbox.addEventListener("drop", function(evt) {
                console.log('drop evt:', JSON.parse(JSON.stringify(evt.dataTransfer)))
                evt.stopPropagation()
                evt.preventDefault()
                $scope.$apply(function() {
                    $scope.dropText = 'Drop files here...'
                    $scope.dropClass = ''
                })
                var files = evt.dataTransfer.files
                if (files.length > 0) {
                    $scope.$apply(function() {
                        $scope.files = []
                        for (var i = 0; i < files.length; i++) {
                            $scope.files.push(files[i])
                        }
                    })
                }
            }, false)
            //============== DRAG & DROP =============

            $scope.setFiles = function(element) {
                $scope.showUploadingThrobber = true;
                $scope.isTypeError = false;
               var testFileExt = checkFile(element.files[0],".csv");
                $scope.showUploadingThrobber = false
                if(testFileExt){
                    $scope.$apply(function($scope) {
                        
                        // Turn the FileList object into an Array
                        $scope.files = []
                        for (var i = 0; i < element.files.length; i++) {
                            $scope.files.push(element.files[i])
                        }
                        $scope.progressVisible = false;
                        
                    });
                }
            };

            function csv2Array(fileInput) {
                //read file from input
                let fileReaded = fileInput;

                let reader: FileReader = new FileReader();
                reader.readAsText(fileReaded);

                reader.onload = (e) => {
                    let csv: string = reader.result;
                    let allTextLines = csv.split(/\r|\n|\r/);
                    allTextLines[0] = allTextLines[0].replace(/"/g, "");
                    let headers = allTextLines[0].split(',');
                    $scope.headers = headers;
                    $scope.lines = [];
                    for (let i = 1; i < allTextLines.length; i++) {
                        // split content based on comma

                        allTextLines[i] = allTextLines[i].replace(/"/g, "");
                        let data = allTextLines[i].split(',');
                        if (data.length === headers.length) {
                            let tarr = {};
                            for (let j = 0; j < headers.length; j++) {
                                tarr[headers[j]] = data[j];
                            }

                            $scope.lines.push(tarr);
                        }
                    }
                   
                    $scope.$apply();
                }
            }


            $scope.previewData = function() {

                csv2Array($scope.files[0]);
                
            };

            $scope.uploadFile = function() {

                var fd = new FormData();
                for (var i in $scope.files) {
                    fd.append("file", $scope.files[i])
                }
                $scope.showThrobber = true;
                var authToken = $cookies.get('Authorization');
                var restCallUrl = restAPIUrlService.getRestCallUrl("UPLOAD_HIERARCHY_DETAILS");
                $scope.showDisabled = true;
                $http.post(restCallUrl, fd, {
                    headers: {
                        'Content-Type': undefined,
                        'Authorization': authToken
                    },
                    transformRequest: angular.identity
                }).then(function(data, status, headers, config) {
                    $scope.showThrobber = false;
                    $scope.showDisabled= false;
                    homePageController.templateName = 'dataTaggingDetails';

                },function(data){
                    $scope.showThrobber = false;
                    $scope.showDisabled= false;
                    $scope.showError = true;
                   
                });


            };


            function uploadProgress(evt) {
                $scope.$apply(function() {
                    if (evt.lengthComputable) {
                        $scope.progress = Math.round(evt.loaded * 100 / evt.total)
                    } else {
                        $scope.progress = 'unable to compute'
                    }
                })
            }

            function uploadComplete(evt) {
                /* This event is raised when the server send back a response */
                alert(evt.target.responseText)
            }

            function uploadFailed(evt) {
                alert("There was an error attempting to upload the file.")
            }

            function uploadCanceled(evt) {
                $scope.$apply(function() {
                    $scope.progressVisible = false
                })
                alert("The upload has been canceled by the user or the browser dropped the connection.")
            }

            function checkFile(sender, validExts) {
             if(sender){
                var fileExt = sender.name;
                fileExt = fileExt.substring(fileExt.lastIndexOf('.'));
                if (validExts.indexOf(fileExt) < 0 && fileExt != "") {
                    $scope.isTypeError = true;
                    (<HTMLInputElement>document.getElementById("fileInp")).value = "";
                    
                    return false;
                }
                else return true;

                }
            }


        }
    }
}