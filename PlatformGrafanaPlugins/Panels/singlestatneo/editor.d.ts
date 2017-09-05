/// <reference path="../../../../../public/app/headers/common.d.ts" />
export declare class PipelinePanelEditorCtrl {
    private $q;
    private uiSegmentSrv;
    uniqueSingleStatPanel: any;
    uniqueSingleStatPanelCtrl: any;
    uniqueSingleStatPanelMetaData: any;
    dataSourceResponse: any;
    fontsize: any;
    fieldsStatArray: any;
    selectedfield: any;
    neo4jDataStatus: any;
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any);
    getFontSizeValue(): void;
    setFontSizeValue(): void;
    setSelectedField(): void;
    render(): void;
}
/** @ngInject */
export declare function pipelinePanelEditor($q: any, uiSegmentSrv: any): {
    restrict: string;
    scope: boolean;
    templateUrl: string;
    controller: typeof PipelinePanelEditorCtrl;
    controllerAs: string;
};
