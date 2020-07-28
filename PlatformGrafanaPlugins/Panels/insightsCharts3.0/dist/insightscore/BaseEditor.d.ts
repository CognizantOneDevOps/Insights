/// <reference path="../../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import { BaseParser } from './BaseParser';
export declare class BaseEditorCtrl {
    private $q;
    private uiSegmentSrv;
    panel: any;
    panelCtrl: any;
    insightsPanelData: any;
    dataSourceResponse: any;
    responseParser: BaseParser;
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any);
    /** @ngInject */
    static loadEditorCtrl($q: any, uiSegmentSrv: any): {
        restrict: string;
        scope: boolean;
        templateUrl: any;
        controller: any;
        controllerAs: any;
    };
    protected getPanel(): any;
    protected getDataSourceRespone(): any;
    protected getInsightsPanelData(): any;
    protected getResponseParser(): BaseParser;
    protected getDatasourceType(): any;
    render(): void;
}
