System.register([], function(exports_1) {
    var InsightsChartEditorModel, InsightsChartTargetModel;
    return {
        setters:[],
        execute: function() {
            InsightsChartEditorModel = (function () {
                function InsightsChartEditorModel(targets, transformInstrctions, joinInstructions, chartOptions) {
                    this.targets = targets;
                    this.transformInstrctions = transformInstrctions;
                    this.joinInstructions = joinInstructions;
                    this.chartOptions = chartOptions;
                }
                ;
                return InsightsChartEditorModel;
            })();
            exports_1("InsightsChartEditorModel", InsightsChartEditorModel);
            InsightsChartTargetModel = (function () {
                function InsightsChartTargetModel(id, columnModel) {
                    this.id = id;
                    this.columnModel = columnModel;
                }
                return InsightsChartTargetModel;
            })();
            exports_1("InsightsChartTargetModel", InsightsChartTargetModel);
        }
    }
});
//# sourceMappingURL=models.js.map