///<reference path="../../../headers/common.d.ts" />
System.register(['./pipelineModel'], function(exports_1) {
    var pipelineModel_1;
    var PipelinePanelEditorCtrl;
    /** @ngInject */
    function pipelinePanelEditor($q, uiSegmentSrv) {
        'use strict';
        return {
            restrict: 'E',
            scope: true,
            templateUrl: 'public/app/plugins/panel/multivaluespanel/editor.html',
            controller: PipelinePanelEditorCtrl,
            controllerAs: 'pipelinePanelEditorCtrl'
        };
    }
    exports_1("pipelinePanelEditor", pipelinePanelEditor);
    return {
        setters:[
            function (pipelineModel_1_1) {
                pipelineModel_1 = pipelineModel_1_1;
            }],
        execute: function() {
            PipelinePanelEditorCtrl = (function () {
                /** @ngInject */
                function PipelinePanelEditorCtrl($scope, $q, uiSegmentSrv, $timeout) {
                    this.$q = $q;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.$timeout = $timeout;
                    this.fieldsList = [];
                    this.fieldsOptionArray = [];
                    this.cloneUiArray = [];
                    this.pipelineFieldsDropdownMenu = [];
                    this.receivedDataStatus = false;
                    this.pipelinePanelCtrl = $scope.ctrl;
                    this.pipelinePanel = this.pipelinePanelCtrl.panel;
                    this.dataSourceResponse = this.pipelinePanelCtrl.dataSourceResponse;
                    if (this.dataSourceResponse === undefined || this.dataSourceResponse.length === 0) {
                        this.receivedDataStatus = false;
                    }
                    else {
                        this.receivedDataStatus = true;
                    }
                    this.pipelinePanelMetaData = this.pipelinePanel.pipelinePanelMetaData;
                    this.pipelinesList = this.pipelinePanelMetaData.pipelinesList;
                    if (this.pipelinesList === undefined) {
                        this.pipelinesList = [];
                        this.pipelinePanelMetaData['pipelinesList'] = this.pipelinesList;
                    }
                    this.cloneUiArray = this.pipelinePanelCtrl.cloneUiArray;
                    this.getPipelineData();
                    this.render();
                }
                PipelinePanelEditorCtrl.prototype.getPipelineData = function () {
                    var self = this;
                    var data = self.dataSourceResponse;
                    if (data !== undefined) {
                        //check for  if no of real pipelines  == 0
                        if (data.length === 0) {
                            self.pipelinePanelMetaData['pipelinesList'] = self.pipelinesList = [];
                        }
                        //check for  if no of real pipelines < no of saved pipelines
                        if (data.length !== 0) {
                            for (var i = 0; i < self.pipelinesList.length; i++) {
                                if (i >= 0) {
                                    var flag = 0;
                                    for (var _i = 0, _a = data.targets; _i < _a.length; _i++) {
                                        var target_1 = _a[_i];
                                        if (self.pipelinesList[i].pipelineRefId === target_1.refId) {
                                            if (target_1.target === "" || target_1.target === null) {
                                                self.pipelinesList.splice(i, 1);
                                                i = i - 1;
                                            }
                                            flag = 1;
                                            break;
                                        }
                                    }
                                    if (flag === 0) {
                                        self.pipelinesList.splice(i, 1);
                                        i = i - 1;
                                    }
                                }
                            }
                        }
                        //check for  if no of real pipelines > no of saved pipelines
                        if (data.length !== 0 && data.targets) {
                            if (data.targets.length === data.results.length) {
                                for (var i = 0; i < data.targets.length; i++) {
                                    var target = data.targets[i];
                                    var flag = 0;
                                    if (target.target === null || target.target === "") {
                                    }
                                    else {
                                        for (var _b = 0, _c = self.pipelinesList; _b < _c.length; _b++) {
                                            var pipeline = _c[_b];
                                            if (target.refId === pipeline.pipelineRefId) {
                                                flag = 1;
                                                self.pipelineFieldsDropdownMenu.push(data.results[i].columns);
                                                break;
                                            }
                                        }
                                        if (flag === 0) {
                                            self.fieldsList = [];
                                            self.fieldsList.push(new pipelineModel_1.FieldModel('', '', '', 1));
                                            self.pipelinesList.push(new pipelineModel_1.PipelineModel(target.refId, self.fieldsList));
                                            self.pipelineFieldsDropdownMenu.push(data.results[i].columns);
                                        }
                                    }
                                }
                            }
                        }
                    }
                };
                //Use render method for refreshing the view.
                PipelinePanelEditorCtrl.prototype.render = function () {
                    var self = this;
                    if (self.pipelinesList.length === 0) {
                        self.receivedDataStatus = false;
                    }
                    self.pipelinePanelCtrl.render();
                };
                PipelinePanelEditorCtrl.prototype.onAddField = function (pipelineId) {
                    var self = this;
                    var fieldsPosition = [];
                    for (var i = 0; i < self.pipelinesList.length; i++) {
                        if (self.pipelinesList[i].pipelineRefId === pipelineId) {
                            for (var j = 0; j < self.pipelinesList[i].fieldsList.length; j++) {
                                fieldsPosition.push(self.pipelinesList[i].fieldsList[j].fieldPosition);
                            }
                            fieldsPosition.sort(self.fieldsSort);
                            var currentFieldPosition = parseInt(fieldsPosition[fieldsPosition.length - 1]) + 1;
                            self.pipelinesList[i].fieldsList.push(new pipelineModel_1.FieldModel('', '', '', currentFieldPosition));
                            self.render();
                        }
                    }
                };
                PipelinePanelEditorCtrl.prototype.fieldsSort = function (a, b) {
                    var d = a - b;
                    return d;
                };
                PipelinePanelEditorCtrl.prototype.onRemoveField = function (pipelineId, fieldNo) {
                    var self = this;
                    var fieldsPosition = [];
                    for (var i = 0; i < self.pipelinesList.length; i++) {
                        if (self.pipelinesList[i].pipelineRefId === pipelineId) {
                            for (var j = 0; j < self.pipelinesList[i].fieldsList.length; j++) {
                                if (self.pipelinesList[i].fieldsList[j].fieldPosition === fieldNo) {
                                    self.pipelinesList[i].fieldsList.splice(j, 1);
                                    if (self.pipelinesList[i].fieldsList.length === 0) {
                                        self.pipelinesList.splice(i, 1);
                                        i = i - 1;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    this.render();
                };
                return PipelinePanelEditorCtrl;
            })();
            exports_1("PipelinePanelEditorCtrl", PipelinePanelEditorCtrl);
        }
    }
});
//# sourceMappingURL=editor.js.map