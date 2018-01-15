import { BasePanelCtrl } from '../insightsCore/BaseModule';
declare class InsightsChartsPanelCtrl extends BasePanelCtrl {
    private annotationsSrv;
    private $sanitize;
    private $window;
    /** @ngInject */
    constructor($scope: any, $injector: any, annotationsSrv: any, $sanitize: any, $window: any);
    chartContainerId: string;
    containerHeight: number;
    handlePreRender(dataSourceResponse: any): void;
}
export { InsightsChartsPanelCtrl, InsightsChartsPanelCtrl as PanelCtrl };
