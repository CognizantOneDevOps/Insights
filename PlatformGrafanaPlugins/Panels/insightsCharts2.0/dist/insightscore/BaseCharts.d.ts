/// <reference path="../../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import { ChartModel } from './ChartModel';
export declare class BaseCharts {
    private chartModel;
    static supportedDataTypes: string[];
    private chartEditorWrapper;
    constructor(chartModel: ChartModel);
    /**
     * Check if the google charts library is loaded or not. If not, then add new script tag.
     */
    loadGoogleCharts(): void;
    getChartEditorWarpper(): any;
    renderChart(isEditChart: boolean): void;
    private transformData(data);
    private joinDataTables(dataTables);
    private buildDataTables(dataArray);
    /**
     * Use executeRenderChart for peoperly configured chart options.
     */
    private executeRenderChart();
    /**
     * Use loadChartEditor for editing the chart and create proper chart options.
     * Will mostly be used in editor panel
     */
    private executeEditChart();
    applyTheme(chartOptions: any): any;
    appendChartContainer(): void;
    private convertData(dataRows, typeMapping);
    private convertToType(data, type);
}
