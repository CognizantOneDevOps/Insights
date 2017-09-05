///<reference path="../../../headers/common.d.ts" />
System.register(['lodash', 'app/plugins/sdk', './transformers', './editor'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var lodash_1, sdk_1, transformers_1, editor_1;
    var PipelinePanelCtrl;
    return {
        setters:[
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (sdk_1_1) {
                sdk_1 = sdk_1_1;
            },
            function (transformers_1_1) {
                transformers_1 = transformers_1_1;
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
                    this.validQuery = false;
                    this.finalStat = 0;
                    this.uniqueSingleStatPanelMetaData = {
                        inputProperties: {
                            'color': 'black',
                            'font-size': '30px'
                        }
                    };
                    this.panelDefaults = {
                        uniqueSingleStatPanelMetaData: this.uniqueSingleStatPanelMetaData
                    }; //rowModel, ToolsModel
                    this.neo4jDataStatus = false;
                    lodash_1.default.defaults(this.panel, this.panelDefaults);
                    this.uniqueSingleStatPanelMetaData = this.panel.uniqueSingleStatPanelMetaData;
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
                    var self = this;
                    self.finalStat = 0;
                    self.neo4jDataStatus = false;
                    self.dataSourceResponse = dataList;
                    if (self.datasource !== undefined &&
                        self.datasource.constructor.name !== undefined &&
                        self.datasource.type === 'neo4j') {
                        self.validQuery = true;
                        self.neo4jHandledData = transformers_1.neo4jDataParser(self.dataSourceResponse);
                        console.log(self.neo4jHandledData);
                        if (self.neo4jHandledData['data'] !== undefined) {
                            self.fieldsStatArray = self.neo4jHandledData['columns'];
                            self.neo4jDataStatus = true;
                            if (self.neo4jHandledData['data'].length === 0) {
                                self.finalStat = 0;
                            }
                            else {
                                self.finalStat = 'Please Select Field';
                            }
                        }
                    }
                    else {
                        self.validQuery = false;
                        self.finalStat = 'Please select Neo4j datasource';
                    }
                    this.render();
                };
                PipelinePanelCtrl.prototype.setSelectedFieldUi = function () {
                    var self = this;
                    if (self.neo4jHandledData['data'] !== undefined) {
                        var neoDataObject = self.neo4jHandledData['data'][0];
                        for (var key in neoDataObject) {
                            if (neoDataObject.hasOwnProperty(key)) {
                                if (key === self.uniqueSingleStatPanelMetaData['selectedfield']) {
                                    self.finalStat = neoDataObject[key];
                                }
                            }
                        }
                    }
                };
                PipelinePanelCtrl.prototype.render = function () {
                    var self = this;
                    if (self.neo4jDataStatus === true) {
                        self.setSelectedFieldUi();
                    }
                    return _super.prototype.render.call(this, self.dataSourceResponse);
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