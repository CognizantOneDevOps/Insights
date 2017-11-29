///<reference path="../../../headers/common.d.ts" />
System.register(["lodash", "angular", "./pipelineModel", "./transformers"], function (exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    /** @ngInject */
    function pipelinePanelEditor($q, uiSegmentSrv) {
        'use strict';
        return {
            restrict: 'E',
            scope: true,
            templateUrl: 'public/app/plugins/panel/pipeline/editor.html',
            controller: PipelinePanelEditorCtrl,
            controllerAs: 'pipelinePanelEditorCtrl'
        };
    }
    exports_1("pipelinePanelEditor", pipelinePanelEditor);
    var lodash_1, angular_1, pipelineModel_1, transformers_1, PipelinePanelEditorCtrl;
    return {
        setters: [
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (angular_1_1) {
                angular_1 = angular_1_1;
            },
            function (pipelineModel_1_1) {
                pipelineModel_1 = pipelineModel_1_1;
            },
            function (transformers_1_1) {
                transformers_1 = transformers_1_1;
            }
        ],
        execute: function () {///<reference path="../../../headers/common.d.ts" />
            PipelinePanelEditorCtrl = /** @class */ (function () {
                /** @ngInject */
                function PipelinePanelEditorCtrl($scope, $q, uiSegmentSrv) {
                    this.$q = $q;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.showToolDetails = false;
                    this.showFieldDetails = false;
                    this.errMsg = '';
                    //$scope.editor = this;
                    this.pipelinePanelCtrl = $scope.ctrl;
                    this.pipelinePanel = this.pipelinePanelCtrl.panel;
                    this.pipelinePanelMetaData = this.pipelinePanel.pipelinePanelMetaData;
                    this.toolsMetaData = this.pipelinePanelMetaData.toolsMetaData;
                    if (this.toolsMetaData === undefined) {
                        this.toolsMetaData = [];
                        this.pipelinePanelMetaData['toolsMetaData'] = this.toolsMetaData;
                    }
                    this.pipelineDataModel = this.pipelinePanelMetaData.pipelineDataModel;
                    if (this.pipelineDataModel === undefined) {
                        this.pipelineDataModel = [];
                        this.pipelinePanelMetaData['pipelineDataModel'] = this.pipelineDataModel;
                    }
                    this.dataSourceResponse = this.pipelinePanelCtrl.dataSourceResponse;
                    /*if (this.dataSourceResponse.results[0].columns === undefined) {
                       this.errMsg = "Cypher Query does not match the required Format";
                     }*/
                    this.addColumnSegment = this.uiSegmentSrv.newPlusButton();
                    this.getPipelineData();
                }
                PipelinePanelEditorCtrl.prototype.getPipelineData = function () {
                    var data = this.dataSourceResponse;
                    if (data !== undefined) {
                        //check for  if no of real pipelines <no of saved pipelines
                        for (var i = 0; i < this.pipelineDataModel.length; i++) {
                            var flag = 0;
                            for (var _i = 0, _a = data.targets; _i < _a.length; _i++) {
                                var target = _a[_i];
                                if (this.pipelineDataModel[i].pipelineRefId === target.refId) {
                                    flag = 1;
                                    break;
                                }
                            }
                            if (flag === 0) {
                                this.pipelineDataModel.splice(i, 1);
                                i = i - 1;
                            }
                        }
                        //check for  if no of saved pipelines < no of real pipelines
                        for (var _b = 0, _c = data.targets; _b < _c.length; _b++) {
                            var target = _c[_b];
                            var flag = 0;
                            for (var _d = 0, _e = this.pipelineDataModel; _d < _e.length; _d++) {
                                var pipeline = _e[_d];
                                if (target.refId === pipeline.pipelineRefId) {
                                    flag = 1;
                                    break;
                                }
                            }
                            if (flag === 0) {
                                this.pipelineDataModel.push(new pipelineModel_1.PipelineModel(target.refId, '', '', this.toolsMetaData));
                            }
                        }
                    }
                };
                PipelinePanelEditorCtrl.prototype.getPipelineName = function () {
                    this.render();
                };
                PipelinePanelEditorCtrl.prototype.getPipelineColor = function () {
                    this.render();
                };
                //Use render method for refreshing the view.
                PipelinePanelEditorCtrl.prototype.render = function () {
                    this.getPipelineData();
                    for (var x = 0; x < this.pipelineDataModel.length; x++) {
                        this.pipelineDataModel[x].toolsList = angular_1.default.copy(this.toolsMetaData);
                    }
                    this.clonedPipelinePanelMetaData = angular_1.default.copy(this.pipelinePanelMetaData);
                    if (this.getObjectLength(this.clonedPipelinePanelMetaData) !== 0) {
                        this.clonedPipelinePanelMetaData = new transformers_1.Transformers().insertValueProperty(this.dataSourceResponse, this.clonedPipelinePanelMetaData);
                    }
                    this.checkFieldMapping();
                    this.pipelinePanelCtrl.render();
                };
                PipelinePanelEditorCtrl.prototype.onAssignData = function (categoryname) {
                    this.render();
                };
                PipelinePanelEditorCtrl.prototype.onAddCategory = function () {
                    this.showToolDetails = true;
                    var position = this.toolsMetaData.length + 1;
                    this.toolsMetaData.push(new pipelineModel_1.ToolModel(position, '', '', []));
                    this.render();
                };
                PipelinePanelEditorCtrl.prototype.getObjectLength = function (obj) {
                    return Object.keys(obj).length;
                };
                PipelinePanelEditorCtrl.prototype.onRemoveCategory = function (category) {
                    ;
                    var index = lodash_1.default.indexOf(this.toolsMetaData, category);
                    this.toolsMetaData.splice(index, 1);
                    var count = 1;
                    for (var _i = 0, _a = this.toolsMetaData; _i < _a.length; _i++) {
                        var key = _a[_i];
                        key.position = count++;
                    }
                    this.render();
                };
                PipelinePanelEditorCtrl.prototype.getFieldOptions = function () {
                    var _this = this;
                    if (!this.dataSourceResponse) {
                        return this.$q.when([]);
                    }
                    var fieldList = new transformers_1.Transformers().getFields(this.dataSourceResponse);
                    var segments = lodash_1.default.map(fieldList, function (c) { return _this.uiSegmentSrv.newSegment({ value: c }); });
                    return this.$q.when(segments);
                };
                PipelinePanelEditorCtrl.prototype.addField = function (indexValue) {
                    var fieldLevelModel = new pipelineModel_1.FieldLevelModel('', '');
                    var field = this.addColumnSegment.value;
                    if (field) {
                        this.showFieldDetails = true;
                        fieldLevelModel.dbName = field;
                        this.toolsMetaData[indexValue].fieldList.push(fieldLevelModel);
                    }
                    var plusButton = this.uiSegmentSrv.newPlusButton();
                    this.addColumnSegment.html = plusButton.html;
                    this.addColumnSegment.value = plusButton.value;
                    this.render();
                };
                PipelinePanelEditorCtrl.prototype.removeField = function (field, indexValue) {
                    this.toolsMetaData[indexValue].fieldList = lodash_1.default.without(this.toolsMetaData[indexValue].fieldList, field);
                    this.render();
                };
                PipelinePanelEditorCtrl.prototype.checkFieldMapping = function () {
                    if (this.toolsMetaData[0] !== undefined) {
                        if (this.toolsMetaData[0].fieldList[0] !== undefined) {
                            if (this.toolsMetaData[0].fieldList[0].dbName === '') {
                                return false;
                            }
                            else {
                                return true;
                            }
                        }
                    }
                };
                return PipelinePanelEditorCtrl;
            }());
            exports_1("PipelinePanelEditorCtrl", PipelinePanelEditorCtrl);
        }
    };
});
//# sourceMappingURL=editor.js.map