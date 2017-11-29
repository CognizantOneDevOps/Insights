import { MetricsPanelCtrl } from 'app/plugins/sdk';
declare class PipelinePanelCtrl extends MetricsPanelCtrl {
    private annotationsSrv;
    private $sanitize;
    private $window;
    static templateUrl: string;
    dataSourceResponse: any;
    pipelinePanelMetaData: any;
    panelDefaults: any;
    pipelineCloneData: any;
    screenWidth: number;
    toolsCompWidth: number;
    showScroll: boolean;
    start: number;
    end: number;
    count: number;
    functionBlockFixCount: number;
    totalHeight: number;
    showNoData: number;
    /** @ngInject */
    constructor($scope: any, $injector: any, annotationsSrv: any, $sanitize: any, $window: any);
    onInitEditMode(): void;
    onInitPanelActions(actions: any): void;
    issueQueries(datasource: any): any;
    onDataError(err: any): void;
    onDataReceived(dataList: any): void;
    render(): void;
    widthComputation(): void;
    nextFuncBlock(start: any, end: any): void;
    prevFuncBlock(start: any, end: any): void;
    link(scope: any, elem: any, attrs: any, ctrl: any): void;
}
export { PipelinePanelCtrl, PipelinePanelCtrl as PanelCtrl };
