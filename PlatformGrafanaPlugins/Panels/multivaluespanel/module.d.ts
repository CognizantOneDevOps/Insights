/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { MetricsPanelCtrl } from 'app/plugins/sdk';
declare class PipelinePanelCtrl extends MetricsPanelCtrl {
    private annotationsSrv;
    private $sanitize;
    static templateUrl: string;
    dataSourceResponse: any;
    pipelinePanelMetaData: any;
    ds: any;
    cloneUiArray: any;
    panelDefaults: {
        pipelinePanelMetaData: any;
    };
    /** @ngInject */
    constructor($scope: any, $injector: any, annotationsSrv: any, $sanitize: any);
    onInitEditMode(): void;
    onInitPanelActions(actions: any): void;
    issueQueries(datasource: any): any;
    onDataError(err: any): void;
    onDataReceived(dataList: any): void;
    setCloneUiData(): void;
    render(): void;
    link(scope: any, elem: any, attrs: any, ctrl: any): void;
}
export { PipelinePanelCtrl, PipelinePanelCtrl as PanelCtrl };
