///<reference path="../../../headers/common.d.ts" />
System.register(["lodash", "./toolsInsightModel"], function (exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    /** @ngInject */
    function toolsInsightEditor($q, uiSegmentSrv) {
        'use strict';
        return {
            restrict: 'E',
            scope: true,
            templateUrl: 'public/app/plugins/panel/toolsinsights/editor.html',
            controller: ToolsInsightEditorCtrl,
            controllerAs: 'toolsInsightEditorCtrl'
        };
    }
    exports_1("toolsInsightEditor", toolsInsightEditor);
    var lodash_1, toolsInsightModel_1, ToolsInsightEditorCtrl;
    return {
        setters: [
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (toolsInsightModel_1_1) {
                toolsInsightModel_1 = toolsInsightModel_1_1;
            }
        ],
        execute: function () {///<reference path="../../../headers/common.d.ts" />
            ToolsInsightEditorCtrl = /** @class */ (function () {
                /** @ngInject */
                function ToolsInsightEditorCtrl($scope, $q, uiSegmentSrv) {
                    this.$q = $q;
                    this.uiSegmentSrv = uiSegmentSrv;
                    this.toolListData = [];
                    this.selectedToolSeq = [];
                    this.fieldList = [];
                    this.selectedFieldList = [];
                    this.toolDataJson = {};
                    this.selectedToolsDetailJson = {};
                    this.showFieldDetails = false;
                    this.toolMappingJson = [];
                    this.defaultMappingJson = [];
                    //super($scope, $q, uiSegmentSrv);
                    var self = this;
                    self.toolsInsightsPanelCtrl = $scope.ctrl;
                    //console.log(self.toolsInsightsPanelCtrl.dataSourceResponse);
                    if (self.toolsInsightsPanelCtrl.dataSourceResponse === undefined) {
                        self.toolsInsightsPanelCtrl.dataSourceResponse = [];
                    }
                    if (self.toolsInsightsPanelCtrl.dataSourceResponse !== undefined && self.toolsInsightsPanelCtrl.dataSourceResponse.length !== 0) {
                        var toolDataArray = self.toolsInsightsPanelCtrl.dataSourceResponse.results[0].data[0].row[0];
                        self.getToolListOptions(toolDataArray);
                    }
                    self.selectedToolSeq = self.toolsInsightsPanelCtrl.pipelineToolsArray;
                    if (self.toolsInsightsPanelCtrl.lastSelectedDetailJson !== undefined) {
                    }
                    if (self.selectedToolSeq === undefined) {
                        self.selectedToolSeq = [];
                    }
                    if (self.toolsInsightsPanelCtrl.toolsDetailJson !== undefined) {
                        self.selectedToolsDetailJson = self.toolsInsightsPanelCtrl.toolsDetailJson;
                        self.toolMappingJson = self.toolsInsightsPanelCtrl.toolDetailMappingJson;
                        self.selectedToolSeq = self.toolsInsightsPanelCtrl.selectedSeq;
                    }
                    self.checkEmptyToolList();
                    self.fieldList = self.toolsInsightsPanelCtrl.toolDetails;
                    self.toolsInsightsPanel = self.toolsInsightsPanelCtrl.panel.toolsInsightsPanelCtrl;
                    self.addColumnSegment = this.uiSegmentSrv.newPlusButton();
                    self.render();
                }
                //Use render method for refreshing the view.
                ToolsInsightEditorCtrl.prototype.render = function () {
                    this.toolsInsightsPanelCtrl.render();
                };
                ToolsInsightEditorCtrl.prototype.getToolListOptions = function (toolDataArray) {
                    this.toolDataJson = toolDataArray;
                    var toolData = new toolsInsightModel_1.Tools('', []);
                    var keysMap = Object.keys(toolDataArray[0]);
                    for (var i in toolDataArray) {
                        var toolListRow = toolDataArray[i][keysMap[0]];
                        if (this.toolListData.indexOf(toolListRow[i]) === -1) {
                            this.toolListData.push(toolListRow);
                        }
                    }
                };
                ToolsInsightEditorCtrl.prototype.getToolOptions = function () {
                    var _this = this;
                    var toolList = this.toolListData;
                    var segments = lodash_1.default.map(toolList, function (c) { return _this.uiSegmentSrv.newSegment({ value: c }); });
                    return this.$q.when(segments);
                };
                ToolsInsightEditorCtrl.prototype.addTool = function () {
                    var toolName = this.addColumnSegment.value;
                    var toolData = new toolsInsightModel_1.Tools('', []);
                    var idx = this.selectedToolSeq.indexOf(toolName);
                    if (idx === -1) {
                        this.selectedToolSeq.push(toolName);
                        this.selectedToolsDetailJson[toolName] = [];
                        this.toolMappingJson.push(toolData);
                        toolData.toolName = toolName;
                    }
                    //console.log(this.toolMappingJson);
                    var plusButton = this.uiSegmentSrv.newPlusButton();
                    this.addColumnSegment.html = plusButton.html;
                    this.addColumnSegment.value = plusButton.value;
                    this.onSubmitAction();
                    this.render();
                };
                ToolsInsightEditorCtrl.prototype.removeTool = function (tool, index) {
                    this.selectedToolSeq = lodash_1.default.without(this.selectedToolSeq, tool);
                    delete this.selectedToolsDetailJson[tool];
                    for (var i in this.toolMappingJson) {
                        if (this.toolMappingJson[i].toolName === tool)
                            /*delete this.toolMappingJson[i];*/
                            this.toolMappingJson.splice(index, 1);
                    }
                    //console.log(this.toolMappingJson);
                    if (this.selectedToolSeq.length === 0) {
                        this.toolsInsightsPanel["selectedSeq"] = [];
                    }
                    this.onSubmitAction();
                    this.render();
                };
                ToolsInsightEditorCtrl.prototype.getFieldOptions = function (selectedTool) {
                    var _this = this;
                    this.fieldList = [];
                    var keysMap = Object.keys(this.toolDataJson[0]);
                    for (var i in this.toolDataJson) {
                        var toolName = this.toolDataJson[i][keysMap[0]];
                        if (toolName === selectedTool) {
                            this.fieldList = this.toolDataJson[i][keysMap[1]];
                            break;
                        }
                    }
                    var fieldList = this.fieldList;
                    var segments = lodash_1.default.map(fieldList, function (c) { return _this.uiSegmentSrv.newSegment({ value: c }); });
                    return this.$q.when(segments);
                };
                ToolsInsightEditorCtrl.prototype.addFields = function (selectedToolNm) {
                    var field = new toolsInsightModel_1.Fields('', '');
                    var toolField = new toolsInsightModel_1.Tools('', []);
                    var fieldVal = this.addColumnSegment.value;
                    var index;
                    var toolMappingJsonData;
                    for (var i in this.toolMappingJson) {
                        if (selectedToolNm === this.toolMappingJson[i].toolName) {
                            toolMappingJsonData = this.toolMappingJson[i].fields;
                        }
                    }
                    if (toolMappingJsonData !== undefined && toolMappingJsonData.length === 0) {
                        index = -1;
                    }
                    if (toolMappingJsonData !== undefined && toolMappingJsonData.length !== 0) {
                        for (var i in toolMappingJsonData) {
                            var inside = toolMappingJsonData[i];
                            for (var j in inside) {
                                index = inside.fieldName.indexOf(fieldVal);
                                if (inside.fieldName === fieldVal) {
                                    index = 0;
                                    break;
                                }
                            }
                            if (index === 0) {
                                break;
                            }
                        }
                    }
                    if (index === -1) {
                        this.selectedFieldList.push(fieldVal);
                        this.selectedToolsDetailJson[selectedToolNm].push(field);
                        for (var i in this.toolMappingJson) {
                            if (this.toolMappingJson[i].toolName === selectedToolNm) {
                                this.toolMappingJson[i].fields.push(field);
                            }
                        }
                    }
                    if (fieldVal) {
                        this.showFieldDetails = true;
                        field.fieldName = fieldVal;
                        this.selectedFieldList.push(fieldVal);
                    }
                    //console.log(this.toolMappingJson);
                    var plusButton = this.uiSegmentSrv.newPlusButton();
                    this.addColumnSegment.html = plusButton.html;
                    this.addColumnSegment.value = plusButton.value;
                    this.onSubmitAction();
                    this.render();
                };
                ToolsInsightEditorCtrl.prototype.removeField = function (key, field, indexValue) {
                    this.selectedFieldList = lodash_1.default.without(this.selectedFieldList, field);
                    this.selectedToolsDetailJson[key].splice(indexValue, 1);
                    for (var i in this.toolMappingJson) {
                        if (this.toolMappingJson[i].toolName === key)
                            this.toolMappingJson[i].fields.splice(indexValue, 1);
                    }
                    //console.log(this.toolMappingJson);
                    this.onSubmitAction();
                    this.render();
                };
                ToolsInsightEditorCtrl.prototype.onSubmitAction = function () {
                    this.toolsInsightsPanel["selectedToolsSeq"] = this.selectedToolSeq;
                    this.toolsInsightsPanel["selectedSeq"] = this.selectedToolSeq;
                    var self = this;
                    self.toolsInsightsPanel["toolDetailMappingJson"] = self.toolMappingJson;
                    /* self.toolsInsightsPanel["toolDetailMappingJson"] = [];
                     setTimeout(function () {
                       self.toolsInsightsPanel["toolDetailMappingJson"] = self.toolMappingJson;
                       self.render();
                     }, 200);*/
                    //this.toolsInsightsPanel["toolDetailMappingJson"] = this.toolMappingJson;
                    this.toolsInsightsPanel["toolsDetailJson"] = this.selectedToolsDetailJson;
                    this.render();
                };
                ToolsInsightEditorCtrl.prototype.checkFieldMapping = function () {
                    var length = this.toolMappingJson.length;
                    var toolListDataLen = this.toolListData.length;
                    if (length !== 0 && toolListDataLen !== 0) {
                        return true;
                    }
                    else {
                        return false;
                    }
                };
                ToolsInsightEditorCtrl.prototype.checkValueMapping = function () {
                    var data = this.toolMappingJson;
                    var toolListDataLen = this.toolListData.length;
                    var count = 0;
                    for (var i in data) {
                        if (data[i].fields.length > 0) {
                            count = 1;
                        }
                    }
                    if (count === 1 && toolListDataLen !== 0) {
                        return true;
                    }
                    else {
                        return false;
                    }
                };
                ToolsInsightEditorCtrl.prototype.checkEmptyToolList = function () {
                    var DsResponse = this.toolsInsightsPanelCtrl.dataSourceResponse;
                    //console.log(DsResponse);
                    var DsResponseLen = this.toolsInsightsPanelCtrl.dataSourceResponse.length;
                    //console.log(DsResponseLen);
                    if (DsResponse !== undefined) {
                        if (DsResponseLen <= 1) {
                            return true;
                        }
                    }
                    else {
                        return false;
                    }
                };
                return ToolsInsightEditorCtrl;
            }());
            exports_1("ToolsInsightEditorCtrl", ToolsInsightEditorCtrl);
        }
    };
});
//# sourceMappingURL=editor.js.map