System.register([], function (exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var InsightsChartEditorModel, InsightsChartTargetModel;
    return {
        setters: [],
        execute: function () {
            InsightsChartEditorModel = /** @class */ (function () {
                function InsightsChartEditorModel(targets, transformInstrctions, joinInstructions, chartOptions) {
                    this.targets = targets;
                    this.transformInstrctions = transformInstrctions;
                    this.joinInstructions = joinInstructions;
                    this.chartOptions = chartOptions;
                }
                ;
                return InsightsChartEditorModel;
            }());
            exports_1("InsightsChartEditorModel", InsightsChartEditorModel);
            InsightsChartTargetModel = /** @class */ (function () {
                function InsightsChartTargetModel(id, columnModel) {
                    this.id = id;
                    this.columnModel = columnModel;
                }
                return InsightsChartTargetModel;
            }());
            exports_1("InsightsChartTargetModel", InsightsChartTargetModel);
        }
    };
});
//# sourceMappingURL=models.js.map