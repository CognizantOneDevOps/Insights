/// <reference path="../../node_modules/grafana-sdk-mocks/app/headers/common.d.ts" />
import { MetricsPanelCtrl } from 'app/plugins/sdk';
import { BaseParser } from './BaseParser';
declare abstract class BasePanelCtrl extends MetricsPanelCtrl {
    static templateUrl: string;
    dataSourceResponse: any;
    insightsPanelData: any;
    panelEditor: any;
    panel: any;
    responseParser: BaseParser;
    /** @ngInject */
    constructor($scope: any, $injector: any);
    initializeBaseParser(): void;
    initializeInsightsPanelMetadata(): void;
    registerEditor(editor: any): void;
    getInsightsPanelData(): any;
    getPanel(): any;
    getDatasourceType(): any;
    getResponseParser(): BaseParser;
    onInitEditMode(): void;
    buildEditorCtrlData(): {
        templateUrl: string;
        controller: any;
        controllerAs: string;
    };
    onInitPanelActions(actions: any): void;
    issueQueries(datasource: any): void;
    onDataError(err: any): void;
    onDataReceived(dataList: any): void;
    render(): void;
    abstract handlePreRender(dataSourceResponse: any): any;
    link(scope: any, elem: any, attrs: any, ctrl: any): void;
}
export { BasePanelCtrl };
