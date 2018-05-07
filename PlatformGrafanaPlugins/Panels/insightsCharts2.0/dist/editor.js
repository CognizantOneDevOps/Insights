System.register(['lodash', './insightscore/BaseEditor', './insightscore/BaseCharts', './insightscore/ChartModel', './models'], function(exports_1) {
    var __extends = (this && this.__extends) || function (d, b) {
        for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
    var lodash_1, BaseEditor_1, BaseCharts_1, ChartModel_1, models_1;
    var InsightsChartsEditorCtrl;
    return {
        setters:[
            function (lodash_1_1) {
                lodash_1 = lodash_1_1;
            },
            function (BaseEditor_1_1) {
                BaseEditor_1 = BaseEditor_1_1;
            },
            function (BaseCharts_1_1) {
                BaseCharts_1 = BaseCharts_1_1;
            },
            function (ChartModel_1_1) {
                ChartModel_1 = ChartModel_1_1;
            },
            function (models_1_1) {
                models_1 = models_1_1;
            }],
        execute: function() {
            InsightsChartsEditorCtrl = (function (_super) {
                __extends(InsightsChartsEditorCtrl, _super);
                /** @ngInject */
                function InsightsChartsEditorCtrl($scope, $q, uiSegmentSrv) {
                    _super.call(this, $scope, $q, uiSegmentSrv);
                    this.supportedDataTypes = BaseCharts_1.BaseCharts.supportedDataTypes;
                    this.insightsPanelData = _super.prototype.getInsightsPanelData.call(this);
                    this.chartVisible = false;
                    this.insightsChartEditorModel = this.insightsPanelData['insightsChartEditorModel'];
                    //chipsModel: any = {};
                    this.columnCache = {};
                    this.process();
                }
                InsightsChartsEditorCtrl.prototype.process = function () {
                    if (this.getResponseParser()) {
                        this.loadDataAndColumns();
                        //this.updateChipsModel();
                        this.loadChartEditor();
                    }
                    else {
                        var self_1 = this;
                        setTimeout(function () {
                            self_1.process();
                        }, 50);
                    }
                };
                /*updateChipsModel() {
                  if (this.insightsChartEditorModel) {
                    for (let target of this.insightsChartEditorModel.targets) {
                      let columnModel = target.columnModel;
                      let chips = [];
                      for (let column of columnModel) {
                        if (column.type) {
                          chips.push({ name: column.name, type: 'new' });
                        }
                      }
                      this.chipsModel[target.id] = chips;
                    }
                  }
                }*/
                InsightsChartsEditorCtrl.prototype.loadDataAndColumns = function () {
                    //this.parsedResponseArray = new BaseParser().parseNeo4jResponseArray(super.getDataSourceRespone());
                    this.parsedResponseArray = this.getResponseParser().parseResponse(_super.prototype.getDataSourceRespone.call(this));
                    if (this.parsedResponseArray.length > 0) {
                        var insightsChartTargetModelArray = [];
                        for (var _i = 0, _a = this.parsedResponseArray; _i < _a.length; _i++) {
                            var responseData = _a[_i];
                            var preConfiguredColumns = [];
                            if (this.insightsChartEditorModel) {
                                for (var _b = 0, _c = this.insightsChartEditorModel.targets; _b < _c.length; _b++) {
                                    var target = _c[_b];
                                    if (target.id === responseData.target) {
                                        preConfiguredColumns = target.columnModel;
                                        break;
                                    }
                                }
                            }
                            var columnModelArray = [];
                            for (var _d = 0, _e = responseData.columns; _d < _e.length; _d++) {
                                var column = _e[_d];
                                for (var _f = 0; _f < preConfiguredColumns.length; _f++) {
                                    var preConfiguredColumn = preConfiguredColumns[_f];
                                    if (column.name === preConfiguredColumn.name) {
                                        column.type = preConfiguredColumn.type;
                                        break;
                                    }
                                }
                                columnModelArray.push(column);
                            }
                            insightsChartTargetModelArray.push(new models_1.InsightsChartTargetModel(responseData.target, columnModelArray));
                        }
                        if (this.insightsChartEditorModel === undefined) {
                            this.insightsChartEditorModel = new models_1.InsightsChartEditorModel(insightsChartTargetModelArray, undefined, undefined, undefined);
                        }
                        else {
                            this.insightsChartEditorModel.targets = insightsChartTargetModelArray;
                        }
                    }
                };
                /*querySearch(query, targetId) {
                  if (this.columnCache[targetId] === undefined) {
                    let columns: string[] = [];
                    for (let target of this.insightsChartEditorModel.targets) {
                      if (target.id === targetId) {
                        let columnArray = target.columnModel;
                        for (let columnModel of columnArray) {
                          columns.push(columnModel.name);
                        }
                      }
                    }
                    this.columnCache[targetId] = columns;
                  }
                  var results = query ? this.columnCache[targetId].filter(this.createFilterFor(query)) : [];
                  return results;
                }
              
                createFilterFor(query) {
                  var lowercaseQuery = angular.lowercase(query);
                  return function filterFn(columnName) {
                    return (columnName.toLowerCase().indexOf(lowercaseQuery) === 0);
                  };
                }
              
                transformChip(chip) {
                  if (angular.isObject(chip)) {
                    return chip;
                  }
                  return { name: chip, type: 'new' };
                }*/
                InsightsChartsEditorCtrl.prototype.loadChartEditor = function () {
                    if (this.parsedResponseArray.length > 0) {
                        var chartDataArray = [];
                        for (var _i = 0, _a = this.parsedResponseArray; _i < _a.length; _i++) {
                            var parsedResponse = _a[_i];
                            var columnModel = [];
                            for (var _b = 0, _c = this.insightsChartEditorModel.targets; _b < _c.length; _b++) {
                                var target = _c[_b];
                                if (target.id === parsedResponse.target) {
                                    var availabelColumns = target.columnModel;
                                    var columnTypesConfigured = true;
                                    for (var _d = 0; _d < availabelColumns.length; _d++) {
                                        var column = availabelColumns[_d];
                                        if (column.type === undefined) {
                                            columnTypesConfigured = false;
                                            break;
                                        }
                                    }
                                    if (columnTypesConfigured) {
                                        columnModel = target.columnModel;
                                    }
                                    /*let chips: any[] = this.chipsModel[parsedResponse.target];
                                    for (let chip of chips) {
                                      for (let column of availabelColumns) {
                                        if (chip.name === column.name) {
                                          columnModel.push(column);
                                          break;
                                        }
                                      }
                                    }*/
                                    break;
                                }
                            }
                            if (columnModel.length > 0) {
                                chartDataArray.push(new ChartModel_1.ChartData(parsedResponse.target, parsedResponse.data, columnModel));
                            }
                        }
                        if (chartDataArray.length > 0) {
                            var insightsPanelData = this.getInsightsPanelData();
                            var containerModel = new ChartModel_1.ContainerModel('chartEditorContainer', 600);
                            var chartType = insightsPanelData['chartType'];
                            var chartOptions = insightsPanelData['chartOptions'];
                            if (this.insightsChartEditorModel.chartOptions) {
                                var additionalOptions = this.parseAdditionalChartOptions();
                                chartOptions = lodash_1.default.defaults(additionalOptions, chartOptions);
                                if (insightsPanelData['chartOptions'] === undefined) {
                                    insightsPanelData['chartOptions'] = chartOptions;
                                }
                            }
                            var chartModel = new ChartModel_1.ChartModel(chartType, chartOptions, chartDataArray, containerModel, this.insightsChartEditorModel.transformInstrctions, this.insightsChartEditorModel.joinInstructions);
                            var chart = new BaseCharts_1.BaseCharts(chartModel);
                            chart.renderChart(true);
                            this.chartVisible = true;
                            this.chartEditorWarpper = chart.getChartEditorWarpper();
                        }
                    }
                };
                InsightsChartsEditorCtrl.prototype.parseAdditionalChartOptions = function () {
                    try {
                        return JSON.parse(this.insightsChartEditorModel.chartOptions);
                    }
                    catch (error) {
                        return {};
                    }
                };
                InsightsChartsEditorCtrl.prototype.saveChartOptions = function () {
                    if (this.chartEditorWarpper['chartEditor']) {
                        var chartEditor = this.chartEditorWarpper['chartEditor'].getChartWrapper();
                        this.insightsPanelData['chartType'] = chartEditor.getChartType();
                        var chartOptions = chartEditor.getOptions();
                        if (this.insightsChartEditorModel.chartOptions) {
                            var additionalOptions = this.parseAdditionalChartOptions();
                            chartOptions = lodash_1.default.defaults(additionalOptions, chartOptions);
                        }
                        this.insightsPanelData['chartOptions'] = chartOptions;
                        if ('Elasticsearch' === this.getDatasourceType()) {
                        }
                        this.insightsPanelData['insightsChartEditorModel'] = this.insightsChartEditorModel;
                        this.render();
                    }
                };
                return InsightsChartsEditorCtrl;
            })(BaseEditor_1.BaseEditorCtrl);
            exports_1("InsightsChartsEditorCtrl", InsightsChartsEditorCtrl);
        }
    }
});
//# sourceMappingURL=editor.js.map