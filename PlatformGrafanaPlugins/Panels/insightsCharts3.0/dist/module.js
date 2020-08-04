System.register(['./editor', './insightscore/BaseModule', './insightscore/BaseCharts', './insightscore/ChartModel', 'angular'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var editor_1, BaseModule_1, BaseCharts_1, ChartModel_1, angular_1;
    var InsightsChartsPanelCtrl;
    return {
        setters:[
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
            },
            function (angular_1_1) {
                angular_1 = angular_1_1;
            }],
        execute: function() {
            InsightsChartsPanelCtrl = (function (_super) {
                __extends(InsightsChartsPanelCtrl, _super);
                /** @ngInject */
                function InsightsChartsPanelCtrl($scope, $injector, annotationsSrv, $sanitize, $window) {
                    _super.call(this, $scope, $injector);
                    this.annotationsSrv = annotationsSrv;
                    this.$sanitize = $sanitize;
                    this.$window = $window;
                    _super.prototype.registerEditor.call(this, editor_1.InsightsChartsEditorCtrl);
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
                        var chartOptions = angular_1.default.copy(insightsPanelData['chartOptions'], {});
                        var chartDataArray = [];
                        for (var _i = 0; _i < parsedResponseArray.length; _i++) {
                            var parsedResponse = parsedResponseArray[_i];
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
            })(BaseModule_1.BasePanelCtrl);
            exports_1("InsightsChartsPanelCtrl", InsightsChartsPanelCtrl);
            exports_1("PanelCtrl", InsightsChartsPanelCtrl);
        }
    }
});
//# sourceMappingURL=module.js.map