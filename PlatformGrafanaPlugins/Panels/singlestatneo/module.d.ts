/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { MetricsPanelCtrl } from 'app/plugins/sdk';
declare class PipelinePanelCtrl extends MetricsPanelCtrl {
    private annotationsSrv;
    private $sanitize;
    static templateUrl: string;
    dataSourceResponse: any;
    validQuery: boolean;
    finalStat: any;
    uniqueSingleStatPanelMetaData: {
        inputProperties: {
            'color': string;
            'font-size': string;
        };
    };
    panelDefaults: {
        uniqueSingleStatPanelMetaData: {
            inputProperties: {
                'color': string;
                'font-size': string;
            };
        };
    };
    neo4jHandledData: any;
    fieldsStatArray: any;
    neo4jDataStatus: boolean;
    /** @ngInject */
    constructor($scope: any, $injector: any, annotationsSrv: any, $sanitize: any);
    onInitEditMode(): void;
    onInitPanelActions(actions: any): void;
    issueQueries(datasource: any): any;
    onDataError(err: any): void;
    onDataReceived(dataList: any): void;
    setSelectedFieldUi(): void;
    render(): void;
    link(scope: any, elem: any, attrs: any, ctrl: any): void;
}
export { PipelinePanelCtrl, PipelinePanelCtrl as PanelCtrl };
