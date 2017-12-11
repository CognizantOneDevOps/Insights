/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { ToolModel, PipelineModel } from './pipelineModel';
export declare class PipelinePanelEditorCtrl {
    private $q;
    private uiSegmentSrv;
    pipelinePanel: any;
    pipelinePanelCtrl: any;
    pipelinePanelMetaData: any;
    dataSourceResponse: any;
    toolsMetaData: ToolModel[];
    addColumnSegment: any;
    clonedPipelinePanelMetaData: any;
    pipelineDataModel: PipelineModel[];
    showToolDetails: boolean;
    showFieldDetails: boolean;
    errMsg: string;
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any);
    getPipelineData(): void;
    getPipelineName(): void;
    getPipelineColor(): void;
    render(): void;
    onAssignData(categoryname: any): void;
    onAddCategory(): void;
    getObjectLength(obj: any): number;
    onRemoveCategory(category: any): void;
    getFieldOptions(): any;
    addField(indexValue: any): void;
    removeField(field: any, indexValue: any): void;
    checkFieldMapping(): boolean;
}
/** @ngInject */
export declare function pipelinePanelEditor($q: any, uiSegmentSrv: any): {
    restrict: string;
    scope: boolean;
    templateUrl: string;
    controller: typeof PipelinePanelEditorCtrl;
    controllerAs: string;
};
