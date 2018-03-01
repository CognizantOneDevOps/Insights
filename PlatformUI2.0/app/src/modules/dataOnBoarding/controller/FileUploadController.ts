/// <reference path="../../../_all.ts" />
module ISightApp {

    export class FileUploadController {

        static $inject = ['$scope', '$cookies', '$http', 'dataOnBoardingService', 'restAPIUrlService','$window']
        constructor(private $scope, private $cookies, private $http, private dataOnBoardingService, private restAPIUrlService:IRestAPIUrlService, private $window) {

            var self = this;
            var elem = document.querySelector('#homePageTemplateContainer');
            var homePageControllerScope = angular.element(elem).scope();
            var homePageController = homePageControllerScope['homePageController'];

            this.initApp(this.$scope, this.$cookies, this.$http, this.dataOnBoardingService, homePageController,restAPIUrlService, this.$window);
        }

        initApp($scope, $cookies, $http, dataOnBoardingService, homePageController,restAPIUrlService, $window): void {

            var dropbox = document.getElementById("dropbox")
            $scope.dropText = 'Drop files here...'
            $scope.tableData = [];
            $scope.lines = [];
            $scope.headers = [];
			$scope.showError = false;
            $scope.selectedType = "";
            $scope.showTextArea = false;
            
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

            $scope.changeSelected = function() {
                if($scope.selectedType && $scope.selectedType.length >0){
                    $scope.showTextArea = true;
                }
                
            };

            $scope.uploadFile = function() {

                var fd = new FormData();
                for (var i in $scope.files) {
                    fd.append("file", $scope.files[i])
                }
                fd.append("action",$scope.selectedType);
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
					if(data.data.status == "failure"){
						$scope.showError = true;
						$window.scrollTo(0, 0);
					}else{
						homePageController.templateName = 'dataTaggingDetails';
					}

                },function(data){
                    $scope.showThrobber = false;
                    $scope.showDisabled= false;
                    $scope.showError = true;
                   
                });


            };


            $scope.JSONToCSVConvertor = function() {
            dataOnBoardingService.getMetaData().then(function(response) {
                var JSONData = response.data;
                var ReportTitle = "Test";
                var ShowLabel = true;
                //If JSONData is not an object then JSON.parse will parse the JSON string in an Object
                var arrData = typeof JSONData != 'object' ? JSON.parse(JSONData) : JSONData;
                var colArr = ["metadata_id","level_1","level_2","level_3","level_4","toolName","toolProperty1","propertyValue1","toolProperty2","propertyValue2","toolProperty3","propertyValue3","toolProperty4","propertyValue4","Action"];
                var CSV = '';    
                //Set Report title in first row or line
                
                //CSV += ReportTitle + '\r\n\n';

                //This condition will generate the Label/Header
                if (ShowLabel) {
                    var row = "";
                    
                    //This loop will extract the label from 1st index of on array
                    for (var index in colArr) {
                           //Now convert each value to string and comma-seprated
                            row += colArr[index] + ',';
                    }

                    row = row.slice(0, -1);
                    
                    //append Label row with line break
                    CSV += row + '\r\n';
                }
                
                //1st loop is to extract each row
                for (var i = 0; i < arrData.length; i++) {
                    var row = "";
                    
                    //2nd loop will extract each column and convert it in string comma-seprated
                    for (var j=0; j< colArr.length;j++) {
                            row += arrData[i].propertyMap[colArr[j]];
                            if(j !== colArr.length-1 ){
                                row += ',';
                            }
                    }

                    row.slice(0, row.length - 1);
                    
                    //add a line break after each row
                    CSV += row + '\r\n';
                }

                if (CSV == '') {        
                    alert("Invalid data");
                    return;
                }   
                
                //Generate a file name
                var fileName = "BusinessMapping";
                //this will remove the blank-spaces from the title and replace it with an underscore
                //fileName += ReportTitle.replace(/ /g,"_");   
                
                //Initialize file format you want csv or xls
                var uri = 'data:text/csv;charset=utf-8,' + encodeURI(CSV);
                
                
                //this trick will generate a temp <a /> tag
                var link = <HTMLAnchorElement>document.createElement("a");    
                link.href = uri;
                
                //set the visibility hidden so it will not effect on your web-layout
                //link.style = "visibility:hidden";
                link.download = fileName + ".csv";
                
                //this part will append the anchor tag and remove it after automatic click
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
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