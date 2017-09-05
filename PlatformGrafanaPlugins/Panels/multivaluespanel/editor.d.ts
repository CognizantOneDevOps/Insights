/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { PipelineModel, FieldModel } from './pipelineModel';
export declare class PipelinePanelEditorCtrl {
    private $q;
    private uiSegmentSrv;
    private $timeout;
    pipelinePanel: any;
    pipelinePanelCtrl: any;
    pipelinePanelMetaData: any;
    dataSourceResponse: any;
    fieldsList: FieldModel[];
    pipelinesList: PipelineModel[];
    fieldsOptionArray: any;
    cloneUiArray: any;
    pipelineFieldsDropdownMenu: any[];
    receivedDataStatus: boolean;
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any, $timeout: any);
    getPipelineData(): void;
    render(): void;
    onAddField(pipelineId: string): void;
    fieldsSort(a: number, b: number): number;
    onRemoveField(pipelineId: string, fieldNo: number): void;
}
/** @ngInject */
export declare function pipelinePanelEditor($q: any, uiSegmentSrv: any): {
    restrict: string;
    scope: boolean;
    templateUrl: string;
    controller: typeof PipelinePanelEditorCtrl;
    controllerAs: string;
};
