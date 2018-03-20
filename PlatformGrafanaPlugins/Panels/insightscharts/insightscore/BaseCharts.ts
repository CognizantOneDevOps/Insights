///<reference path="../../../../headers/common.d.ts" />
import $ from 'jquery';
import { ChartModel, ChartData, ColumnModel, ContainerModel } from './ChartModel';
var google;

export class BaseCharts {
    static supportedDataTypes = ['string', 'number', 'boolean', 'date', 'datetime', 'timeofday'];
    private chartEditorWrapper: any = {};
    constructor(private chartModel: ChartModel) {
        this.loadGoogleCharts();
    }

    /**
     * Check if the google charts library is loaded or not. If not, then add new script tag.
     */
    loadGoogleCharts() {
        if ($('#googleChartLoaderScript').length === 0) {
            google = {};
            $('<script>', {
                src: 'https://www.gstatic.com/charts/loader.js',
                id: 'googleChartLoaderScript',
                type: 'text/javascript'
            }).appendTo('body');
        }
    }

    getChartEditorWarpper() {
        return this.chartEditorWrapper;
    }

    renderChart(isEditChart: boolean) {
        google = window['google'];
        if (this.chartModel.dataArray && this.chartModel.dataArray.length > 0) {
            let containerElem = document.getElementById(this.chartModel.container.id);
            //Check if the container is loaded. Once loaded, then initiate the google charts loader.
            if (containerElem && google && google.charts) {
                if (isEditChart) {
                    google.charts.load('45', { 'packages': ['corechart', 'charteditor'] });
                    google.charts.setOnLoadCallback(this.executeEditChart.bind(this));
                } else {
                    if (this.chartModel.chartOptions) {
                        google.charts.load('45', { 'packages': ['corechart'] });
                        google.charts.setOnLoadCallback(this.executeRenderChart.bind(this));
                    }
                }
            } else {
                let self = this;
                setTimeout(function () {
                    self.renderChart(isEditChart);
                }, 50);
            }
        }
    }

    private transformData(data) {
        if (this.chartModel.transformDataInstruction === undefined || this.chartModel.transformDataInstruction === null) {
            return data;
        } else {
            let transformDataFunc = new Function('data', this.chartModel.transformDataInstruction);
            return transformDataFunc(data);
        }
    }

    private joinDataTables(dataTables: any[]) {
        if (dataTables && dataTables.length > 0) {
            if (this.chartModel.joinInstructions === undefined || this.chartModel.joinInstructions === null) {
                return dataTables[0];
            } else {
                let joinFunc = new Function('dataTables', this.chartModel.joinInstructions);
                return joinFunc(dataTables);
            }
        }
    }

    private buildDataTables(dataArray: ChartData[]) {
        let dataTables = [];
        if (dataArray) {
            for (let data of dataArray) {
                if (data.columns) {
                    let typeMapping = [];
                    for (let column of data.columns) {
                        typeMapping.push({ label: column.name, type: column.type });
                    }
                    dataTables.push(this.convertData(data.data, typeMapping));
                }
            }
        }
        return dataTables;
    }

    /**
     * Use executeRenderChart for peoperly configured chart options.
     */
    private executeRenderChart() {
        if (google.visualization[this.chartModel.chartType]) {
            let dataTables = this.buildDataTables(this.chartModel.dataArray);
            let data = this.joinDataTables(dataTables);
            data = this.transformData(data);
            let containerElem = document.getElementById(this.chartModel.container.id);
            let chartOptions = this.chartModel.chartOptions;
            chartOptions['height'] = this.chartModel.container.height;
            chartOptions['width'] = '100%';
            var chart = new google.visualization[this.chartModel.chartType](containerElem);
            chart.draw(data, chartOptions);
        } else {
            google.charts.load('current', { 'packages': [this.chartModel.chartType.toLowerCase()] });
            google.charts.setOnLoadCallback(this.executeRenderChart.bind(this));
        }
    }

    /**
     * Use loadChartEditor for editing the chart and create proper chart options.
     * Will mostly be used in editor panel
     */
    private executeEditChart() {
        //var data = this.convertData(this.queryData, this.typeMapping);
        let dataTables = this.buildDataTables(this.chartModel.dataArray);
        let data = this.joinDataTables(dataTables);
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
    }

    appendChartContainer() {
        var dialog = $('.google-visualization-charteditor-dialog');
        var self = this;
        if (dialog.length === 0) {
            setTimeout(function () {
                self.appendChartContainer();
            }, 50);
        } else {
            dialog.children().each(function () {
                $('#' + self.chartModel.container.id).append(this);
            });
            dialog.hide();
        }
    }

    private convertData(dataRows, typeMapping) {
        let data = new google.visualization.DataTable();
        for (let column of typeMapping) {
            data.addColumn(column['type'], column['label']);
        }
        for (let row of dataRows) {
            let rowArray = [];
            for (let column of typeMapping) {
                rowArray.push(this.convertToType(row[column['label']], column['type']));
            }
            data.addRow(rowArray);
        }
        return data;
    }

    private convertToType(data, type) {
        if (data === undefined || data === null) {
            return null;
        } else {
            if (type === 'string') {
                return data.toString();
            } else if (type === 'number') {
                return Number(data);
            } else if (type === 'boolean') {
                if (typeof data === 'boolean') {
                    return data;
                }
                return (data === "true");
            } else if (type === 'date') {
                if (typeof data === 'number') {
                    let dataStr = data.toString();
                    if (dataStr.length < 13) {
                        let appendZeros = '0000000000000';
                        data = dataStr + appendZeros.substring(0, (13 - dataStr.length));
                        return new Date(Number(data));
                    } else {
                        return new Date(Number(data));
                    }
                } else {
                    return new Date(data);
                }
            } else if (type === 'datetime') {
            } else if (type === 'timeofday') {
            } else {
                throw new TypeError('Unknown Type passed.');
            }
        }
    }
}
