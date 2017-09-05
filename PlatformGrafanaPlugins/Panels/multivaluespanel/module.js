///<reference path="../../../headers/common.d.ts" />
System.register(['lodash', 'app/plugins/sdk', './editor'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var lodash_1, sdk_1, editor_1;
    var PipelinePanelCtrl;
    return {
        setters:[
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (sdk_1_1) {
                sdk_1 = sdk_1_1;
            },
            function (editor_1_1) {
                editor_1 = editor_1_1;
            }],
        execute: function() {
            PipelinePanelCtrl = (function (_super) {
                __extends(PipelinePanelCtrl, _super);
                /** @ngInject */
                function PipelinePanelCtrl($scope, $injector, annotationsSrv, $sanitize) {
                    _super.call(this, $scope, $injector);
                    this.annotationsSrv = annotationsSrv;
                    this.$sanitize = $sanitize;
                    this.pipelinePanelMetaData = {};
                    this.cloneUiArray = [];
                    this.panelDefaults = {
                        pipelinePanelMetaData: this.pipelinePanelMetaData
                    }; //rowModel, ToolsModel
                    lodash_1.default.defaults(this.panel, this.panelDefaults);
                    this.pipelinePanelMetaData = this.panel.pipelinePanelMetaData;
                    this.events.on('data-received', this.onDataReceived.bind(this));
                    this.events.on('data-error', this.onDataError.bind(this));
                    this.events.on('data-snapshot-load', this.onDataReceived.bind(this));
                    this.events.on('init-edit-mode', this.onInitEditMode.bind(this));
                    this.events.on('init-panel-actions', this.onInitPanelActions.bind(this));
                }
                PipelinePanelCtrl.prototype.onInitEditMode = function () {
                    this.addEditorTab('Options', editor_1.pipelinePanelEditor, 2);
                };
                PipelinePanelCtrl.prototype.onInitPanelActions = function (actions) {
                    actions.push({ text: 'Export CSV', click: 'ctrl.exportCsv()' });
                };
                PipelinePanelCtrl.prototype.issueQueries = function (datasource) {
                    return _super.prototype.issueQueries.call(this, datasource);
                };
                PipelinePanelCtrl.prototype.onDataError = function (err) {
                    this.dataSourceResponse = [];
                    this.render();
                };
                PipelinePanelCtrl.prototype.onDataReceived = function (dataList) {
                    this.dataSourceResponse = dataList;
                    this.render();
                };
                PipelinePanelCtrl.prototype.setCloneUiData = function () {
                    var self = this;
                    var data = self.dataSourceResponse;
                    console.log(data);
                    if (data !== undefined && data.length !== 0 && data.results !== undefined) {
                        self.cloneUiArray = [];
                        if (data.targets.length === data.results.length) {
                            for (var i = 0; i < self.pipelinePanelMetaData.pipelinesList.length; i++) {
                                for (var j = 0; j < data.targets.length; j++) {
                                    if (self.pipelinePanelMetaData.pipelinesList[i].pipelineRefId === data.targets[j].refId) {
                                        for (var x = 0; x < self.pipelinePanelMetaData.pipelinesList[i].fieldsList.length; x++) {
                                            for (var y = 0; y < data.results[j].columns.length; y++) {
                                                if (self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldName === data.results[j].columns[y]) {
                                                    if (data.results[j].data[0] !== undefined) {
                                                        self.cloneUiArray.push({
                                                            fieldMapName: self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldMapName,
                                                            fieldValue: data.results[j].data[0].row[y],
                                                            fieldColor: 'background-color:' + self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldColor
                                                        });
                                                    }
                                                    else {
                                                        self.cloneUiArray.push({
                                                            fieldMapName: self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldMapName,
                                                            fieldValue: 0,
                                                            fieldColor: 'background-color:' + self.pipelinePanelMetaData.pipelinesList[i].fieldsList[x].fieldColor
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else {
                        self.cloneUiArray = [];
                    }
                };
                PipelinePanelCtrl.prototype.render = function () {
                    this.setCloneUiData();
                    return _super.prototype.render.call(this, this.dataSourceResponse);
                };
                //Essential
                PipelinePanelCtrl.prototype.link = function (scope, elem, attrs, ctrl) {
                    var data;
                    var panel = ctrl.panel;
                    var pageCount = 0;
                    var formaters = [];
                    function renderPanel() {
                    }
                    ctrl.events.on('render', function (renderData) {
                        data = renderData || data;
                        if (data) {
                            renderPanel();
                        }
                        ctrl.renderingCompleted();
                    });
                };
                PipelinePanelCtrl.templateUrl = 'module.html';
                return PipelinePanelCtrl;
            })(sdk_1.MetricsPanelCtrl);
            exports_1("PipelinePanelCtrl", PipelinePanelCtrl);
            exports_1("PanelCtrl", PipelinePanelCtrl);
        }
    }
});
//# sourceMappingURL=module.js.map