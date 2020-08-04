System.register([], function(exports_1) {
    var ChartModel, ColumnModel, ContainerModel, ChartData;
    return {
        setters:[],
        execute: function() {
            ChartModel = (function () {
                function ChartModel(chartType, chartOptions, dataArray, container, transformDataInstruction, joinInstructions) {
                    this.chartType = chartType;
                    this.chartOptions = chartOptions;
                    this.dataArray = dataArray;
                    this.container = container;
                    this.transformDataInstruction = transformDataInstruction;
                    this.joinInstructions = joinInstructions;
                }
                return ChartModel;
            })();
            exports_1("ChartModel", ChartModel);
            ColumnModel = (function () {
                function ColumnModel(name, type) {
                    this.name = name;
                    this.type = type;
                }
                return ColumnModel;
            })();
            exports_1("ColumnModel", ColumnModel);
            ContainerModel = (function () {
                function ContainerModel(id, height) {
                    this.id = id;
                    this.height = height;
                }
                return ContainerModel;
            })();
            exports_1("ContainerModel", ContainerModel);
            ChartData = (function () {
                function ChartData(id, data, columns) {
                    this.id = id;
                    this.data = data;
                    this.columns = columns;
                }
                return ChartData;
            })();
            exports_1("ChartData", ChartData);
        }
    }
});
//# sourceMappingURL=ChartModel.js.map