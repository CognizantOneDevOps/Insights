/********************************************************************************
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
System.register([], function(exports_1) {
    var InferenceDatasource;
    return {
        setters:[],
        execute: function() {
            ///<reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
            //import angular from 'angular';
            //import _ from 'lodash';
            //import * as dateMath from 'app/core/utils/datemath';
            InferenceDatasource = (function () {
                /** @ngInject */
                function InferenceDatasource(instanceSettings, $q, backendSrv, templateSrv) {
                    this.$q = $q;
                    this.backendSrv = backendSrv;
                    this.templateSrv = templateSrv;
                    this.type = instanceSettings.type;
                    this.url = instanceSettings.url;
                    this.name = instanceSettings.name;
                }
                InferenceDatasource.prototype.query = function (options) {
                    var deferred = this.$q.defer();
                    if (options) {
                        var targets = options.targets;
                        var resultDataContents = [];
                        for (var i in targets) {
                            var inputData = {
                                "vectorType": targets[i].vectorType,
                                "vectorSchedule": targets[i].vectorSchedule,
                                "displayMessage": targets[i].displayMessage,
                                "chartType": targets[i].chartType
                            };
                            resultDataContents.push(inputData);
                        }
                        this.backendSrv.datasourceRequest({
                            url: this.url,
                            method: 'POST',
                            data: JSON.stringify(resultDataContents)
                        }).then(function (response) {
                            if (response.status === 200) {
                                deferred.resolve({ data: response.data });
                            }
                            else {
                                deferred.resolve({ status: "success", message: "No data returned", title: "success" });
                            }
                        });
                    }
                    return deferred.promise;
                };
                InferenceDatasource.prototype.testDatasource = function () {
                    var inferenceInputData = {
                        "data": {
                            "vectorType": "testDataSource",
                            "vectorSchedule": "testing",
                            "displayMessage": "testing datasource message {0} to {1}"
                        }
                    };
                    var deferred = this.$q.defer();
                    try {
                        this.backendSrv.datasourceRequest({
                            url: this.url + "/testDataSource",
                            method: 'GET',
                            data: JSON.stringify(inferenceInputData)
                        }).then(function (response) {
                            if (response.status === 200) {
                                deferred.resolve({ status: "success", message: "Data source is working", title: "Success" });
                            }
                            else {
                                deferred.resolve({ status: "failure", message: "Unable to connect to Datasource", title: "Failure" });
                            }
                        });
                    }
                    catch (error) {
                        deferred.resolve({ status: "failure", message: "Unable to connect to Datasource", title: "Failure" });
                    }
                    return deferred.promise;
                };
                return InferenceDatasource;
            })();
            exports_1("default", InferenceDatasource);
        }
    }
});
//# sourceMappingURL=datasource.js.map