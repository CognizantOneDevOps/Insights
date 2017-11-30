/// <reference path="../../../../../public/app/headers/common.d.ts" />
import { Fields } from './toolsInsightModel';
export declare class ToolsInsightEditorCtrl {
    private $q;
    private uiSegmentSrv;
    toolsInsightsPanel: any;
    toolsInsightsPanelCtrl: any;
    dataSourceResponse: any;
    toolListData: any[];
    selectedToolSeq: any[];
    addColumnSegment: any;
    fieldList: any[];
    selectedFieldList: any[];
    toolDataJson: {};
    selectedToolsDetailJson: {};
    showFieldDetails: boolean;
    fieldVal: Fields[];
    toolMappingJson: any[];
    defaultMappingJson: any[];
    /** @ngInject */
    constructor($scope: any, $q: any, uiSegmentSrv: any);
    render(): void;
    getToolListOptions(toolDataArray: any): void;
    getToolOptions(): any;
    addTool(): void;
    removeTool(tool: any, index: any): void;
    getFieldOptions(selectedTool: any): any;
    addFields(selectedToolNm: any): void;
    removeField(key: any, field: any, indexValue: any): void;
    onSubmitAction(): void;
    checkFieldMapping(): boolean;
    checkValueMapping(): boolean;
    checkEmptyToolList(): boolean;
}
/** @ngInject */
export declare function toolsInsightEditor($q: any, uiSegmentSrv: any): {
    restrict: string;
    scope: boolean;
    templateUrl: string;
    controller: typeof ToolsInsightEditorCtrl;
    controllerAs: string;
};
