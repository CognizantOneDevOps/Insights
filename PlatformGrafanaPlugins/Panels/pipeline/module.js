//<reference path="../../../headers/common.d.ts" />
System.register(["angular", "lodash", "app/plugins/sdk", "./editor", "./transformers"], function (exports_1, context_1) {
    "use strict";
    var __extends = (this && this.__extends) || (function () {
        var extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return function (d, b) {
            extendStatics(d, b);
            function __() { this.constructor = d; }
            d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
        };
    })();
    var __moduleName = context_1 && context_1.id;
    var angular_1, lodash_1, sdk_1, editor_1, transformers_1, PipelinePanelCtrl;
    return {
        setters: [
            function (angular_1_1) {
                angular_1 = angular_1_1;
            },
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (sdk_1_1) {
                sdk_1 = sdk_1_1;
            },
            function (editor_1_1) {
                editor_1 = editor_1_1;
            },
            function (transformers_1_1) {
                transformers_1 = transformers_1_1;
            }
        ],
        execute: function () {//<reference path="../../../headers/common.d.ts" />
            PipelinePanelCtrl = /** @class */ (function (_super) {
                __extends(PipelinePanelCtrl, _super);
                //rowModel, ToolsModel
                /** @ngInject */
                function PipelinePanelCtrl($scope, $injector, annotationsSrv, $sanitize, $window) {
                    var _this = _super.call(this, $scope, $injector) || this;
                    _this.annotationsSrv = annotationsSrv;
                    _this.$sanitize = $sanitize;
                    _this.$window = $window;
                    _this.pipelinePanelMetaData = {};
                    _this.panelDefaults = {
                        pipelinePanelMetaData: _this.pipelinePanelMetaData
                    };
                    _this.showScroll = false;
                    _this.count = 1;
                    _this.functionBlockFixCount = 3;
                    _this.showNoData = 0;
                    lodash_1.default.defaults(_this.panel, _this.panelDefaults);
                    _this.pipelinePanelMetaData = _this.panel.pipelinePanelMetaData;
                    _this.events.on('data-received', _this.onDataReceived.bind(_this));
                    _this.events.on('data-error', _this.onDataError.bind(_this));
                    _this.events.on('data-snapshot-load', _this.onDataReceived.bind(_this));
                    _this.events.on('init-edit-mode', _this.onInitEditMode.bind(_this));
                    _this.events.on('init-panel-actions', _this.onInitPanelActions.bind(_this));
                    _this.screenWidth = $window.innerWidth;
                    return _this;
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
                PipelinePanelCtrl.prototype.render = function () {
                    this.pipelineCloneData = angular_1.default.copy(this.pipelinePanelMetaData);
                    this.pipelineCloneData = new transformers_1.Transformers().insertValueProperty(this.dataSourceResponse, this.pipelineCloneData);
                    this.widthComputation();
                    return _super.prototype.render.call(this, this.dataSourceResponse);
                };
                PipelinePanelCtrl.prototype.widthComputation = function () {
                    this.showScroll = false;
                    var functionalBlockLength = 0;
                    if (this.pipelineCloneData["toolsMetaData"] !== undefined) {
                        functionalBlockLength = this.pipelineCloneData["toolsMetaData"].length;
                    }
                    this.totalHeight = 70 * functionalBlockLength;
                    this.start = this.count;
                    this.end = functionalBlockLength;
                    var pipelineTotalWdth = this.screenWidth - 245;
                    var whiteframeCompWdth = 330; //fixed width with margin
                    this.toolsCompWidth = whiteframeCompWdth * functionalBlockLength;
                    var totalWdt = this.toolsCompWidth + 245;
                    if (totalWdt >= this.screenWidth) {
                        this.showScroll = true;
                    }
                    if (this.showScroll) {
                        this.start = this.count;
                        this.end = functionalBlockLength - (functionalBlockLength - this.functionBlockFixCount);
                    }
                };
                PipelinePanelCtrl.prototype.nextFuncBlock = function (start, end) {
                    var functionalBlockLength = this.pipelineCloneData["toolsMetaData"].length;
                    if (this.showScroll && end !== functionalBlockLength) {
                        this.start = start + 1;
                        this.end = end + 1;
                    }
                };
                PipelinePanelCtrl.prototype.prevFuncBlock = function (start, end) {
                    var functionalBlockLength = this.pipelineCloneData["toolsMetaData"].length;
                    if (this.showScroll && start !== 1 && end !== this.functionBlockFixCount) {
                        this.start = start - 1;
                        this.end = end - 1;
                    }
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
            }(sdk_1.MetricsPanelCtrl));
            exports_1("PipelinePanelCtrl", PipelinePanelCtrl);
            exports_1("PanelCtrl", PipelinePanelCtrl);
        }
    };
});
//# sourceMappingURL=module.js.map