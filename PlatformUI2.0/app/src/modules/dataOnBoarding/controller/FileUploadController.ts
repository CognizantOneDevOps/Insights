/// <reference path="../../../_all.ts" />

module ISightApp {

    export class FileUploadController {

        static $inject = ['$scope','$cookies','$http','dataOnBoardingService']
        constructor(private $scope, private $cookies,private $http, private dataOnBoardingService) {            
        this.initApp(this.$scope,this.$cookies,this.$http,this.dataOnBoardingService);
        }

        initApp($scope,$cookies,$http,dataOnBoardingService) : void {
          
            var dropbox = document.getElementById("dropbox")
            $scope.dropText = 'Drop files here...'
            $scope.tableData = [];
            $scope.lines = [];
                  

            // init event handlers
            function dragEnterLeave(evt) {
                evt.stopPropagation()
                evt.preventDefault()
                $scope.$apply(function(){
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
                $scope.$apply(function(){
                    $scope.dropText = ok ? 'Drop files here...' : 'Only files are allowed!'
                    $scope.dropClass = ok ? 'over' : 'not-available'
                })
            }, false)
            dropbox.addEventListener("drop", function(evt) {
                console.log('drop evt:', JSON.parse(JSON.stringify(evt.dataTransfer)))
                evt.stopPropagation()
                evt.preventDefault()
                $scope.$apply(function(){
                    $scope.dropText = 'Drop files here...'
                    $scope.dropClass = ''
                })
                var files = evt.dataTransfer.files
                if (files.length > 0) {
                    $scope.$apply(function(){
                        $scope.files = []
                        for (var i = 0; i < files.length; i++) {
                            $scope.files.push(files[i])
                        }
                    })
                }
            }, false)
            //============== DRAG & DROP =============

            $scope.setFiles = function(element) {
            $scope.$apply(function($scope) {
              console.log('files:', element.files);
              // Turn the FileList object into an Array
                $scope.files = []
                for (var i = 0; i < element.files.length; i++) {
                  $scope.files.push(element.files[i])
                }
              $scope.progressVisible = false
              });
            };
            
            function csv2Array(fileInput){
            //read file from input
            let fileReaded = fileInput;

            let reader: FileReader = new FileReader();
            reader.readAsText(fileReaded);

            reader.onload = (e) => {
              let csv: string = reader.result;
              let allTextLines = csv.split(/\r|\n|\r/);
              let headers = allTextLines[0].split(',');
              
              for (let i = 1; i < allTextLines.length; i++) {
                // split content based on comma
                let data = allTextLines[i].split(',');
                if (data.length === headers.length) {
                  let tarr = {};
                  for (let j = 0; j < headers.length; j++) {
                    tarr[headers[j]] = data[j];
                  }

                  // log each row to see output 
                  console.log(tarr);
                  $scope.lines.push(tarr);
                }
              }
              // all rows in the csv file 
              console.log(">>>>>>>>>>>>>>>>>", $scope.lines);
            } 
            }


            $scope.previewData = function() {

                var fd = new FormData()
                for (var i in $scope.files) {
                    fd.append("file", $scope.files[i])
                    csv2Array( $scope.files[i]);
                }
                var authToken = $cookies.get('Authorization');
               
               //dataOnBoardingService.getAllHierarchyDetails()
               //.then(function (data) {
                 //   $scope.tableData = data.data.details;
                  // console.log($scope.tableData);
                //});

             };            

            $scope.uploadFile = function() {

                var fd = new FormData()
                for (var i in $scope.files) {
                    fd.append("file", $scope.files[i])
                    csv2Array( $scope.files[i]);
                }
                var authToken = $cookies.get('Authorization');
               
               $http.post("https://10.155.143.184/PlatformService/admin/hierarchyDetails/uploadHierarchyDetails", fd, {
                  headers: { 'Content-Type': undefined,'Authorization': authToken },
                  transformRequest: angular.identity
                }).then(function (data, status, headers, config) {

               });
            };

         
            function uploadProgress(evt) {
                $scope.$apply(function(){
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
                $scope.$apply(function(){
                    $scope.progressVisible = false
                })
                alert("The upload has been canceled by the user or the browser dropped the connection.")
            }

            


        }
    }
}

