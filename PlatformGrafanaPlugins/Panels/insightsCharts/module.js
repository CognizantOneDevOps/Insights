System.register(["./editor", "../insightsCore/BaseModule", "../insightsCore/BaseCharts", "../insightsCore/ChartModel"], function (exports_1, context_1) {
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
    var editor_1, BaseModule_1, BaseCharts_1, ChartModel_1, InsightsChartsPanelCtrl;
    return {
        setters: [
            function (editor_1_1) {
                editor_1 = editor_1_1;
            },
            function (BaseModule_1_1) {
                BaseModule_1 = BaseModule_1_1;
            },
            function (BaseCharts_1_1) {
                BaseCharts_1 = BaseCharts_1_1;
            },
            function (ChartModel_1_1) {
                ChartModel_1 = ChartModel_1_1;
            }
        ],
        execute: function () {
            InsightsChartsPanelCtrl = /** @class */ (function (_super) {
                __extends(InsightsChartsPanelCtrl, _super);
                /** @ngInject */
                function InsightsChartsPanelCtrl($scope, $injector, annotationsSrv, $sanitize, $window) {
                    var _this = _super.call(this, $scope, $injector) || this;
                    _this.annotationsSrv = annotationsSrv;
                    _this.$sanitize = $sanitize;
                    _this.$window = $window;
                    _super.prototype.registerEditor.call(_this, editor_1.InsightsChartsEditorCtrl);
                    return _this;
                }
                InsightsChartsPanelCtrl.prototype.handlePreRender = function (dataSourceResponse) {
                    if (this.getResponseParser() === undefined) {
                        return;
                    }
                    this.chartContainerId = this['pluginId'] + '_' + _super.prototype.getPanel.call(this)['id'] + '_' + new Date().getTime();
                    this.containerHeight = this['height'];
                    var parsedResponseArray = this.getResponseParser().parseResponse(dataSourceResponse);
                    var insightsPanelData = this.getInsightsPanelData();
                    var insightsChartEditorModel = this.insightsPanelData['insightsChartEditorModel'];
                    if (insightsChartEditorModel && parsedResponseArray.length > 0) {
                        var containerModel = new ChartModel_1.ContainerModel(this.chartContainerId, this.containerHeight);
                        var chartType = insightsPanelData['chartType'];
                        var chartOptions = insightsPanelData['chartOptions'];
                        var chartDataArray = [];
                        for (var _i = 0, parsedResponseArray_1 = parsedResponseArray; _i < parsedResponseArray_1.length; _i++) {
                            var parsedResponse = parsedResponseArray_1[_i];
                            for (var _a = 0, _b = insightsChartEditorModel.targets; _a < _b.length; _a++) {
                                var target = _b[_a];
                                if (parsedResponse.target === target.id) {
                                    if ('Elasticsearch' === this.getDatasourceType()) {
                                        chartDataArray.push(new ChartModel_1.ChartData(parsedResponse.target, parsedResponse.data, parsedResponse.columns));
                                    }
                                    else {
                                        chartDataArray.push(new ChartModel_1.ChartData(parsedResponse.target, parsedResponse.data, target.columnModel));
                                    }
                                    break;
                                }
                            }
                        }
                        var chartModel = new ChartModel_1.ChartModel(chartType, chartOptions, chartDataArray, containerModel, insightsChartEditorModel.transformInstrctions, insightsChartEditorModel.joinInstructions);
                        var chart = new BaseCharts_1.BaseCharts(chartModel);
                        chart.renderChart(false);
                    }
                };
                return InsightsChartsPanelCtrl;
            }(BaseModule_1.BasePanelCtrl));
            exports_1("InsightsChartsPanelCtrl", InsightsChartsPanelCtrl);
            exports_1("PanelCtrl", InsightsChartsPanelCtrl);
        }
    };
});
//# sourceMappingURL=module.js.map