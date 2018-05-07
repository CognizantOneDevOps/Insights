System.register(['jquery'], function(exports_1) {
    var jquery_1;
    var google, BaseCharts;
    return {
        setters:[
            function (jquery_1_1) {
                jquery_1 = jquery_1_1;
            }],
        execute: function() {
            BaseCharts = (function () {
                function BaseCharts(chartModel) {
                    this.chartModel = chartModel;
                    this.chartEditorWrapper = {};
                    this.loadGoogleCharts();
                }
                /**
                 * Check if the google charts library is loaded or not. If not, then add new script tag.
                 */
                BaseCharts.prototype.loadGoogleCharts = function () {
                    if (jquery_1.default('#googleChartLoaderScript').length === 0) {
                        google = {};
                        jquery_1.default('<script>', {
                            src: 'https://www.gstatic.com/charts/loader.js',
                            id: 'googleChartLoaderScript',
                            type: 'text/javascript'
                        }).appendTo('body');
                    }
                };
                BaseCharts.prototype.getChartEditorWarpper = function () {
                    return this.chartEditorWrapper;
                };
                BaseCharts.prototype.renderChart = function (isEditChart) {
                    google = window['google'];
                    if (this.chartModel.dataArray && this.chartModel.dataArray.length > 0) {
                        var containerElem = document.getElementById(this.chartModel.container.id);
                        //Check if the container is loaded. Once loaded, then initiate the google charts loader.
                        if (containerElem && google && google.charts) {
                            if (isEditChart) {
                                google.charts.load('45', { 'packages': ['corechart', 'charteditor'] });
                                google.charts.setOnLoadCallback(this.executeEditChart.bind(this));
                            }
                            else {
                                if (this.chartModel.chartOptions) {
                                    google.charts.load('45', { 'packages': ['corechart'] });
                                    google.charts.setOnLoadCallback(this.executeRenderChart.bind(this));
                                }
                            }
                        }
                        else {
                            var self_1 = this;
                            setTimeout(function () {
                                self_1.renderChart(isEditChart);
                            }, 50);
                        }
                    }
                };
                BaseCharts.prototype.transformData = function (data) {
                    if (this.chartModel.transformDataInstruction === undefined || this.chartModel.transformDataInstruction === null) {
                        return data;
                    }
                    else {
                        var transformDataFunc = new Function('data', this.chartModel.transformDataInstruction);
                        return transformDataFunc(data);
                    }
                };
                BaseCharts.prototype.joinDataTables = function (dataTables) {
                    if (dataTables && dataTables.length > 0) {
                        if (this.chartModel.joinInstructions === undefined || this.chartModel.joinInstructions === null) {
                            return dataTables[0];
                        }
                        else {
                            var joinFunc = new Function('dataTables', this.chartModel.joinInstructions);
                            return joinFunc(dataTables);
                        }
                    }
                };
                BaseCharts.prototype.buildDataTables = function (dataArray) {
                    var dataTables = [];
                    if (dataArray) {
                        for (var _i = 0; _i < dataArray.length; _i++) {
                            var data = dataArray[_i];
                            if (data.columns) {
                                var typeMapping = [];
                                for (var _a = 0, _b = data.columns; _a < _b.length; _a++) {
                                    var column = _b[_a];
                                    typeMapping.push({ label: column.name, type: column.type });
                                }
                                dataTables.push(this.convertData(data.data, typeMapping));
                            }
                        }
                    }
                    return dataTables;
                };
                /**
                 * Use executeRenderChart for peoperly configured chart options.
                 */
                BaseCharts.prototype.executeRenderChart = function () {
                    if (google.visualization[this.chartModel.chartType]) {
                        var dataTables = this.buildDataTables(this.chartModel.dataArray);
                        var data = this.joinDataTables(dataTables);
                        data = this.transformData(data);
                        var containerElem = document.getElementById(this.chartModel.container.id);
                        var chartOptions = this.chartModel.chartOptions;
                        chartOptions['height'] = this.chartModel.container.height;
                        chartOptions['width'] = '100%';
                        var chart = new google.visualization[this.chartModel.chartType](containerElem);
                        chart.draw(data, chartOptions);
                    }
                    else {
                        google.charts.load('current', { 'packages': [this.chartModel.chartType.toLowerCase()] });
                        google.charts.setOnLoadCallback(this.executeRenderChart.bind(this));
                    }
                };
                /**
                 * Use loadChartEditor for editing the chart and create proper chart options.
                 * Will mostly be used in editor panel
                 */
                BaseCharts.prototype.executeEditChart = function () {
                    //var data = this.convertData(this.queryData, this.typeMapping);
                    var dataTables = this.buildDataTables(this.chartModel.dataArray);
                    var data = this.joinDataTables(dataTables);
                    data = this.transformData(data);
                    if (this.chartModel.chartType === null || this.chartModel.chartType === undefined) {
                        this.chartModel.chartType = 'PieChart';
                    }
                    if (this.chartModel.chartOptions === undefined) {
                        this.chartModel.chartOptions = {
                            'width': 400,
                            'height': this.chartModel.container.height,
                            "backgroundColor": {
                                "fill": "#fbfbfb"
                            }
                        };
                    }
                    var wrapper = new google.visualization.ChartWrapper({
                        'chartType': this.chartModel.chartType,
                        'dataTable': data,
                        'options': this.chartModel.chartOptions
                    });
                    var chartEditor = new google.visualization.ChartEditor();
                    var self = this;
                    var redrawChart = function () {
                        chartEditor.getChartWrapper().draw(document.getElementById(self.chartModel.container.id));
                    };
                    google.visualization.events.addListener(chartEditor, 'ok', redrawChart);
                    chartEditor.openDialog(wrapper, {});
                    this.chartEditorWrapper['chartEditor'] = chartEditor;
                    this.appendChartContainer();
                };
                BaseCharts.prototype.appendChartContainer = function () {
                    var dialog = jquery_1.default('.google-visualization-charteditor-dialog');
                    var self = this;
                    if (dialog.length === 0) {
                        setTimeout(function () {
                            self.appendChartContainer();
                        }, 50);
                    }
                    else {
                        dialog.children().each(function () {
                            jquery_1.default('#' + self.chartModel.container.id).append(this);
                        });
                        dialog.hide();
                    }
                };
                BaseCharts.prototype.convertData = function (dataRows, typeMapping) {
                    var data = new google.visualization.DataTable();
                    for (var _i = 0; _i < typeMapping.length; _i++) {
                        var column = typeMapping[_i];
                        data.addColumn(column['type'], column['label']);
                    }
                    for (var _a = 0; _a < dataRows.length; _a++) {
                        var row = dataRows[_a];
                        var rowArray = [];
                        for (var _b = 0; _b < typeMapping.length; _b++) {
                            var column = typeMapping[_b];
                            rowArray.push(this.convertToType(row[column['label']], column['type']));
                        }
                        data.addRow(rowArray);
                    }
                    return data;
                };
                BaseCharts.prototype.convertToType = function (data, type) {
                    if (data === undefined || data === null) {
                        return null;
                    }
                    else {
                        if (type === 'string') {
                            return data.toString();
                        }
                        else if (type === 'number') {
                            return Number(data);
                        }
                        else if (type === 'boolean') {
                            if (typeof data === 'boolean') {
                                return data;
                            }
                            return (data === "true");
                        }
                        else if (type === 'date') {
                            if (typeof data === 'number') {
                                var dataStr = data.toString();
                                if (dataStr.length < 13) {
                                    var appendZeros = '0000000000000';
                                    data = dataStr + appendZeros.substring(0, (13 - dataStr.length));
                                    return new Date(Number(data));
                                }
                                else {
                                    return new Date(Number(data));
                                }
                            }
                            else {
                                return new Date(data);
                            }
                        }
                        else if (type === 'datetime') {
                        }
                        else if (type === 'timeofday') {
                        }
                        else {
                            throw new TypeError('Unknown Type passed.');
                        }
                    }
                };
                BaseCharts.supportedDataTypes = ['string', 'number', 'boolean', 'date', 'datetime', 'timeofday'];
                return BaseCharts;
            })();
            exports_1("BaseCharts", BaseCharts);
        }
    }
});
//# sourceMappingURL=BaseCharts.js.map