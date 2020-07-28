/// <reference path="../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import { BaseEditorCtrl } from './insightscore/BaseEditor';
import { ParsedResponse } from './insightscore/BaseParser';
import { ChartModel } from './insightscore/ChartModel';
import { InsightsChartEditorModel } from './models';
export declare class InsightsChartsEditorCtrl extends BaseEditorCtrl {
    supportedDataTypes: string[];
    chartEditorWarpper: any;
    insightsPanelData: any;
    chartVisible: boolean;
    chartModel: ChartModel;
    insightsChartEditorModel: InsightsChartEditorModel;
    columnCache: any;
    parsedResponseArray: ParsedResponse[];
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any);
    process(): void;
    loadDataAndColumns(): void;
    loadChartEditor(): void;
    parseAdditionalChartOptions(): any;
    saveChartOptions(): void;
}
